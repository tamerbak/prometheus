package fr.protogen.masterdata.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.control.CommunicationControl;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.ProtogenKeyGenerator;
import fr.protogen.engine.utils.ProtogenParameters;
import fr.protogen.engine.utils.StringFormat;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.LocalDAO;
import fr.protogen.masterdata.DAO.OrganizationDAL;
import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.security.Md5;

@ManagedBean
@SessionScoped
public class UserServices {

	private String login;
	private String password;
	private String errorMsg = "";
	private String email;
	private String adress;
	private String tel;
	private int roleid;
	private String firstName;
	private String lastName;
	private char notifyMethod='e';
	private String message="";
	private boolean validform=false;
	private String activateKey;
	private boolean mailinvalid=false;
	private String countryCode="+33";
	private List<String> cps;
	private List<String> villes;
	private String selectedCP;
	private String selectedVille;

	public static boolean GPS=false;
	

	private CoreUser user;

	/*
	 * 	Creation entreprise
	 */
	private String sigle;
	private String raisonSociale;
	private String capital="00";
	private String typeEntreprise="40";
	private List<PairKVElement> typesEntreprises;
	private String referenceDeclarant;
	private String identifiantFiscal;
	private boolean firstStep=true;
	private String formeEntreprise="40";
	private List<PairKVElement> formesEntreprises;
	private String selectedActivite;
	private List<PairKVElement> activites;
	private List<String> selectedConvention = new ArrayList<String>();
	private List<PairKVElement> conventions;
	private String siretEntreprise;
	private String selectedConventions;
	private List<PairKVElement> nafs;
	private String selectedNaf;
	
	/*
	 * Nouvele ville / CP
	 */
	private String cityName="";
	private String selectedCityCP="";
	private String cpName;
	private String selectedCPPays;
	

	@PostConstruct
	public void energize(){
		if(!GPS)
			return;
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		LocalDAO d = new LocalDAO();
		cps = d.getCP(countryCode);
		villes = d.getVilles(cps.get(0));
		activites = d.getActivites();
		nafs = d.getNafs();
		
		selectedNaf = "0";
		selectedActivite = "0";
		
		if(params.containsKey("uid") && params.get("uid")!=null){
			String uid = params.get("uid").split("--")[1];
			String key = params.get("uid").split("--")[0];
			UserDAOImpl dao = new UserDAOImpl();

			boolean flag =dao.activate(Integer.parseInt(uid),key);
			if(flag)
				message="Votre compte est activé <a href=\""+ProtogenParameters.APPLICATION+"/prometheus/login.xhtml\" style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\">connectez-vous</a>";			
			else
				message="Ce lien de validation n'est pas valide, nous vous invitons à <a href=\"mailto:admin@gpsdelagestion.com\" style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\" >nous contacter</a>";
		} else if(params.containsKey("pwdcode") && params.get("pwdcode")!=null){
			UserDAOImpl dao = new UserDAOImpl();
			
			validform =dao.lookup(params.get("pwdcode"));
			activateKey=params.get("pwdcode");
		} else {
			validform=true;
		}
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		typesEntreprises = pde.getTypesEntreprises();
		formesEntreprises = pde.getFormesEntreprises();
	}
	
	public void dummyMethod(){
		return;
	}
	
	public void newCity(){
		LocalDAO d = new LocalDAO();
		d.persistCity(cityName, selectedCityCP);
		if(selectedCP!=null && selectedCP.equals(selectedCityCP))
			villes.add(cityName);
		cityName="";
	}
	
	public List<String> completeCP(String key){
		List<String> scps = new ArrayList<String>();
		
		if(key.equals("") || key.equals("*"))
			return cps;
		
		for(String cp : cps)
			if(cp.startsWith(key))
				scps.add(cp);
		
		if(scps.size()>0)
			selectedCP = scps.get(0);
		else
			selectedCP = "";
		
		return scps;
	}
	
	public void newCP(){
		LocalDAO d = new LocalDAO();
		if(d.persistCP(cpName, selectedCPPays)){
			cps.add(cpName);
			d.persistCity(cityName, cpName);
			if(cpName!=null && cpName.equals(selectedCityCP))
				villes.add(cityName);
			cityName="";
		}
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Code postal existant",""));
		
		
		cpName= "";
	}
	
	public String onNextPrevious(FlowEvent event){
		
		RequestContext.getCurrentInstance().execute("PF('dlg2').hide()");
		
		return event.getNewStep();
	}
	
	public String doChangePwd(){
		
		UserDAOImpl dao = new UserDAOImpl();
		dao.updatePassword(email,password);
		return "login";
	}
	
	public void activiteChange(){
		int idActivite = Integer.parseInt(selectedActivite);
		int idNaf = Integer.parseInt(selectedNaf);
		LocalDAO d = new LocalDAO();
		
		if(idActivite == 0 )
			nafs = d.getNafs();
		else
			nafs = d.getNafsByActivities(idActivite);
		
		List<PairKVElement> cvsAct = d.getConventionsCollectives(idActivite);
		List<PairKVElement> cvsNaf = d.getConventionsCollectivesByNaf(idNaf);
		
		if(idActivite == 0){
			conventions  = cvsAct;
			return;
		}
		
		
		conventions = new ArrayList<PairKVElement>();
		for(PairKVElement e : cvsAct){
			boolean flag = false;
			for(PairKVElement l : cvsNaf){
				if(l.getKey().equals(e.getKey())){
					flag = true;
					break;
				}
			}	
			if(flag)
				conventions.add(e);
		
		}
		
	}
	
	public void nafChange(){
		int idNaf = Integer.parseInt(selectedNaf);
		int idActivite = Integer.parseInt(selectedActivite);
		LocalDAO d = new LocalDAO();
		
		if(idNaf == 0 )
			activites = d.getActivites();
		else
			activites = d.getActivitiesByNaf(idNaf);
		
		List<PairKVElement> cvsAct = d.getConventionsCollectives(idActivite);
		List<PairKVElement> cvsNaf = d.getConventionsCollectivesByNaf(idNaf);
		
		if(idNaf == 0){
			conventions  = cvsNaf;
			return;
		}
		
		conventions = new ArrayList<PairKVElement>();
		for(PairKVElement e : cvsAct){
			boolean flag = false;
			for(PairKVElement l : cvsNaf){
				if(l.getKey().equals(e.getKey())){
					flag = true;
					break;
				}
			}	
			if(flag)
				conventions.add(e);
		
		}
		
		
	}
	
	public void changePassword(ActionEvent evt){
		String newPassword = ProtogenKeyGenerator.getInstance().generateKey();
		
		CommunicationControl ctrl = new CommunicationControl();
		ctrl.setSilent(true);
		UserDAOImpl dao = new UserDAOImpl();
		boolean flag = dao.changeActivation(email,newPassword);
		
		if(!flag){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'adresse fournie ne correspond à aucun utilisateur",""));
			return;
		}
		
		ctrl.setEmailTo(email);
		ctrl.setEmailSubject("GPS de la gestion : Changement de mot de passe");
		
		String htmlmessage = loadMessageChangePwd(newPassword);
		ctrl.setEmailMessage(htmlmessage);
		
		ctrl.sendMail();
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Message envoyé","Un message de validation a été envoyé contenant les instructions  pour changer votre mot de passe"));
		
	}
	
	public void doActivate(){
		UserDAOImpl dao = new UserDAOImpl();
		
		boolean flag =dao.activate(email,activateKey);
		if(flag)
			message="Votre compte a été activé avec succès ";
		else
			message="Ce lien de validation n'est pas valide, nous vous invitons à <a style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\" href=\"mailto:admin@gpsdelagestion.com\">nous contacter</a>";
		validform=false;
	}
	
	public String authenticate(){
		
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		session.invalidate();
		
		UserDAOImpl dao = new UserDAOImpl();
		
		user = dao.getUser(login, Md5.encode(password));
		
		if(user == null){
			errorMsg = "Vérifiez votre login et mot de passe";
			//FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Echec d'authentification","Identifiant ou mot de passe erroné"));
			return "login";
		}
		
		
		
		String formpage = dao.loadFormMode(user);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("FORM_MODE",formpage);
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("SUPER_ADMIN",new Boolean(user.getCoreRole().isSuperadmin()));
		
		errorMsg = "";
		
		OrganizationDAL odal = new OrganizationDAL();
		GOrganization organization = odal.loadUserOrganization(user);
		user.setOriginalOrganization(organization);
		
		String sessionkey = UUID.randomUUID().toString();
		ApplicationCache cache = ApplicationRepository.getInstance().buildCache(user.getAppKey(), sessionkey, user);
		cache.putUser(user);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("USER_KEY",sessionkey);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("APP_KEY",user.getAppKey());
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("USER_BOUND",new Boolean(user.getCoreRole().getBoundEntity()>0 && user.getBoundEntity()>0));
		
		ApplicationLoader dal = new ApplicationLoader();
		COrganization o = dal.loadOrganizationById(user.getOrganization().getId());
		user.setOrganization(o);
		
		
		
		return "protogen";
		
	}
	
	public void mailExistCheck(){
		if(email==null || email.length()==0)
			return;
		
		
		
	}
	
	public String signup(){
		/*
		 * 	Creer une entreprise
		 */
		EntrepriseService srv = new EntrepriseService();
		int ident = srv.insertEntreprise(sigle, raisonSociale, (new Double(capital.replaceAll(",", "\\."))), Integer.parseInt(typeEntreprise), referenceDeclarant, identifiantFiscal, Integer.parseInt(formeEntreprise), firstName+" "+lastName, siretEntreprise);
		srv.persistConventions(ident,selectedConvention);
		/*
		 * 	Creer un utilisateur
		 */
		UserDAOImpl dao = new UserDAOImpl();
		mailinvalid = dao.checkUser(email);
		if(mailinvalid){
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_FATAL,"L'adresse éléctronique que vous avez saisi est déjà utilisée pour un autre compte",""));
			return "";
		}
		
		generateAlerts(sigle, raisonSociale, capital,  firstName , lastName, adress, selectedVille, selectedCP, countryCode, tel);
		
		CoreUser u = new CoreUser();
		u.setAdress(adress+" - "+selectedVille+" - "+(selectedCP==null?"":selectedCP)+" - "+(countryCode.equals("+33")?"France":"Maroc"));
		
		u.setEmail(email);
		u.setAppKey("d3a37e86-364e-4428-ae6f-9646b5c8a78c");
		u.setCoreRole(new CoreRole(35, ""));
		u.setFirstName(firstName);
		u.setLastName(lastName);
		u.setLogin(email);
		u.setPassword(password);
		u.setTel(countryCode.replaceAll("\\+", "00")+tel);
		u.setThemeColor("css/colors.css?v=1");
		u.setThemeStyle("css/style.css?v=1");
		u.setOrgInstance(ident);
		
		dao = new UserDAOImpl();
		
		String uid = dao.insertUser(u);
		
		CommunicationControl ctrl = new CommunicationControl();
		
		ctrl.setSilent(true);
		ctrl.setEmailTo(email);
		ctrl.setEmailSubject("Bienvenue sur le GPS de la gestion");
		String htmlmessage = loadMessage(uid);
		ctrl.setEmailMessage(htmlmessage);
		
		ctrl.setSmsNumber(countryCode.replaceAll("\\+", "00")+tel);
		ctrl.setSmsMessage("Bienvenue sur la plate-forme GPS de la Gestion, veuillez rentrer ce code afin d'activer votre compte : "+uid);
		
		if(notifyMethod == 's'){
			ctrl.sendSMS();
			setMessage("Votre compte est créé. <br/>Vous avez reçu un message d’activation de votre compte." +
					"<br />Veuillez rentrer votre code dans <a href=\""+ProtogenParameters.APPLICATION+"/prometheus/confirm.xhtml\" style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\" >cette page</a> ");
		}else{
			ctrl.sendMail();
			setMessage("Votre compte est créé.<br/>Vous avez reçu un message d’activation de votre compte. ") ;
		}
		
		HttpSession session = (HttpSession) FacesContext
				.getCurrentInstance().getExternalContext().getSession(true);
		session.invalidate();
		
		if(uid!=null && uid.length()>0)
			return "registersuccess";
		return "";
	}
	
	
	private void generateAlerts(String sigle2, String raisonSociale2,
			String capital2, String firstName2, String lastName2,
			String adress2, String selectedVille2, String selectedCP2,
			String countryCode2, String tel2) {
		String msg = ""; 
		if(sigle == null || sigle.length()==0)
			msg = msg+","+" Le sigle ";
		if(raisonSociale2 == null || raisonSociale2.length()==0)
			msg = msg+","+" La raison sociale ";
		if(capital2 == null || capital2.length()==0)
			msg = msg+","+" Le capital de l'organisation ";
		if(firstName2 == null || firstName2.length()==0)
			msg = msg+","+" Le prénom ";
		if(lastName2 == null || lastName2.length()==0)
			msg = msg+","+" Le nom ";
		if(adress2 == null || adress2.length()==0)
			msg = msg+","+" L'adresse de l'utilisateur ";
		if(selectedVille2 == null || selectedVille2.length()==0)
			msg = msg+","+" La ville de l'utilisateur ";
		if(selectedCP2 == null || selectedCP2.length()==0)
			msg = msg+","+" Le code postal de l'utilisateur ";
		
		if(msg.length()>0){
			msg = msg.substring(1);
			String entete="";
			if(msg.split(",").length>1)
				entete="Les éléments suivants n'ont pas été renseignés";
			else
				entete="L'élément suivant n'a pas été renseigné";
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN, entete, msg));
		}
		
		
		
	}

	private String loadMessage(String uid) {
		String htmlmessage="";
		
		String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/lang/mail.html");
		htmlmessage =StringFormat.getInstance().fileToString(path);
		
		String url=ProtogenParameters.APPLICATION+"/prometheus/confirm.xhtml?uid="+uid;
		htmlmessage=htmlmessage.replaceAll("<<URL_HERE>>", url);
		
		return htmlmessage;
	}

	private String loadMessageChangePwd(String pwd) {
		String htmlmessage="";
		
		String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/lang/changepwd.html");
		htmlmessage =StringFormat.getInstance().fileToString(path);
		
		String url=ProtogenParameters.APPLICATION+"/prometheus/confirm-password.xhtml?pwdcode="+pwd;
		htmlmessage=htmlmessage.replaceAll("<<URL_HERE>>", url);
		
		return htmlmessage;
	}
	
	public CoreUser getUserByName(String prenom, String nom) {
		// TODO Auto-generated method stub
		
		UserDAOImpl dao = new UserDAOImpl();
		return dao.getUserByName(prenom, nom);
	}
	
	public void countryChange(){
		LocalDAO d = new LocalDAO();
		cps = d.getCP(countryCode);
		if(cps.size()>0)
			villes = d.getVilles(cps.get(0));
		else
			villes = new ArrayList<String>();
	}
	
	public void cpChange(){
		LocalDAO d = new LocalDAO();
		villes = d.getVilles(selectedCP);
	}
	
	public void paysChange(){
		selectedCP="";
	}
	public void villeBlur(){
		List<String> scps = new ArrayList<String>();
		
		if(selectedCP.equals("") || selectedCP.equals("*"))
			return ;
		
		for(String cp : cps)
			if(cp.startsWith(selectedCP))
				scps.add(cp);
		
		if(scps.size()>0)
			selectedCP = scps.get(0);
		else
			selectedCP = "";
		
		LocalDAO d = new LocalDAO();
		villes = d.getVilles(selectedCP);
	}
	
	
	public void changeSelectConventions(){
		System.out.println("Conventions changed : "+selectedConvention.size());
	}
	
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public CoreUser getUser() {
		return user;
	}

	public void setUser(CoreUser user) {
		this.user = user;
	}




	public String getEmail() {
		return email;
	}




	public void setEmail(String email) {
		this.email = email;
	}




	public String getAdress() {
		return adress;
	}




	public void setAdress(String adress) {
		this.adress = adress;
	}




	public String getTel() {
		return tel;
	}




	public void setTel(String tel) {
		this.tel = tel;
	}




	public int getRoleid() {
		return roleid;
	}




	public void setRoleid(int roleid) {
		this.roleid = roleid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public char getNotifyMethod() {
		return notifyMethod;
	}

	public void setNotifyMethod(char notifyMethod) {
		this.notifyMethod = notifyMethod;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isValidform() {
		return validform;
	}

	public void setValidform(boolean validform) {
		this.validform = validform;
	}

	public String getActivateKey() {
		return activateKey;
	}

	public void setActivateKey(String activateKey) {
		this.activateKey = activateKey;
	}

	public boolean isMailinvalid() {
		return mailinvalid;
	}

	public void setMailinvalid(boolean mailinvalid) {
		this.mailinvalid = mailinvalid;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public List<String> getCps() {
		return cps;
	}

	public void setCps(List<String> cps) {
		this.cps = cps;
	}

	public List<String> getVilles() {
		return villes;
	}

	public void setVilles(List<String> villes) {
		this.villes = villes;
	}
	public String getSelectedCP() {
		return selectedCP;
	}

	public void setSelectedCP(String selectedCP) {
		this.selectedCP = selectedCP;
	}

	public String getSelectedVille() {
		return selectedVille;
	}

	public void setSelectedVille(String selectedVille) {
		this.selectedVille = selectedVille;
	}

	public String getSigle() {
		return sigle;
	}

	public void setSigle(String sigle) {
		this.sigle = sigle;
	}

	public String getRaisonSociale() {
		return raisonSociale;
	}

	public void setRaisonSociale(String raisonSociale) {
		this.raisonSociale = raisonSociale;
	}

	public String getCapital() {
		return capital;
	}

	public void setCapital(String capital) {
		this.capital = capital;
	}

	public String getTypeEntreprise() {
		return typeEntreprise;
	}

	public void setTypeEntreprise(String typeEntreprise) {
		this.typeEntreprise = typeEntreprise;
	}

	public List<PairKVElement> getTypesEntreprises() {
		return typesEntreprises;
	}

	public void setTypesEntreprises(List<PairKVElement> typesEntreprises) {
		this.typesEntreprises = typesEntreprises;
	}

	public String getReferenceDeclarant() {
		return referenceDeclarant;
	}

	public void setReferenceDeclarant(String referenceDeclarant) {
		this.referenceDeclarant = referenceDeclarant;
	}

	public String getIdentifiantFiscal() {
		return identifiantFiscal;
	}

	public void setIdentifiantFiscal(String identifiantFiscal) {
		this.identifiantFiscal = identifiantFiscal;
	}

	public boolean isFirstStep() {
		return firstStep;
	}

	public void setFirstStep(boolean firstStep) {
		this.firstStep = firstStep;
	}

	public String getFormeEntreprise() {
		return formeEntreprise;
	}

	public void setFormeEntreprise(String formeEntreprise) {
		this.formeEntreprise = formeEntreprise;
	}

	public List<PairKVElement> getFormesEntreprises() {
		return formesEntreprises;
	}

	public void setFormesEntreprises(List<PairKVElement> formesEntreprises) {
		this.formesEntreprises = formesEntreprises;
	}

	public String getSelectedActivite() {
		return selectedActivite;
	}

	public void setSelectedActivite(String selectedActivite) {
		this.selectedActivite = selectedActivite;
	}

	public List<PairKVElement> getActivites() {
		return activites;
	}

	public void setActivites(List<PairKVElement> activites) {
		this.activites = activites;
	}

	public List<String> getSelectedConvention() {
		return selectedConvention;
	}

	public void setSelectedConvention(List<String> selectedConvention) {
		this.selectedConvention = selectedConvention;
	}

	public List<PairKVElement> getConventions() {
		return conventions;
	}

	public void setConventions(List<PairKVElement> conventions) {
		this.conventions = conventions;
	}
	
	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getSelectedCityCP() {
		return selectedCityCP;
	}

	public void setSelectedCityCP(String selectedCityCP) {
		this.selectedCityCP = selectedCityCP;
	}

	public String getCpName() {
		return cpName;
	}

	public void setCpName(String cpName) {
		this.cpName = cpName;
	}

	public String getSelectedCPPays() {
		return selectedCPPays;
	}

	public void setSelectedCPPays(String selectedCPPays) {
		this.selectedCPPays = selectedCPPays;
	}

	public String getSiretEntreprise() {
		return siretEntreprise;
	}

	public void setSiretEntreprise(String siretEntreprise) {
		this.siretEntreprise = siretEntreprise;
	}

	public String getSelectedConventions() {
		return selectedConventions;
	}

	public void setSelectedConventions(String selectedConventions) {
		this.selectedConventions = selectedConventions;
	}

	public List<PairKVElement> getNafs() {
		return nafs;
	}

	public void setNafs(List<PairKVElement> nafs) {
		this.nafs = nafs;
	}

	public String getSelectedNaf() {
		return selectedNaf;
	}

	public void setSelectedNaf(String selectedNaf) {
		this.selectedNaf = selectedNaf;
	}

	

	
	
}
