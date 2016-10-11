package fr.protogen.engine.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
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

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class InsuranceController implements Serializable {
	private String login;
	private String password;
	private CoreUser user;
	private String errorMsg;
	
	private String firstName;
	private String lastName;
	private String email;	
	private String adress;
	private String countryCode="+33";
	private String selectedCP;
	private String selectedVille;
	private List<String> villes;
	private String tel;
	private String notifyMethod;
	private List<String> cps;
	private String message;
	private String orias;
	
	private boolean validform;
	
	@PostConstruct	
	public void energize(){
		LocalDAO d = new LocalDAO();
		cps = d.getCP(countryCode);
		villes = d.getVilles(cps.get(0));
		
		
		/*
		 * Activation
		 */
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		if(params.containsKey("uid") && params.get("uid")!=null){
			String uid = params.get("uid").split("--")[1];
			String key = params.get("uid").split("--")[0];
			UserDAOImpl dao = new UserDAOImpl();
			validform=false;
			boolean flag =dao.activate(Integer.parseInt(uid),key);
			if(flag)
				message="Votre compte est activé <a href=\""+ProtogenParameters.APPLICATION+"/assurance/login-ginsurance.xhtml\" style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\">connectez-vous</a>";			
			else
				message="Ce lien de validation n'est pas valide, nous vous invitons à <a href=\"mailto:admin@gamentreprise.com\" style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\" >nous contacter</a>";
		}
	}
	
	public String authenticate(){
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		session.invalidate();
		
		UserDAOImpl dao = new UserDAOImpl();
		
		user = dao.getUser(login, Md5.encode(password));
		
		if(user == null){
			setErrorMsg("Vérifiez votre login et mot de passe");
			//FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Echec d'authentification","Identifiant ou mot de passe erroné"));
			return "login";
		}
		
		
		
		String formpage = dao.loadFormMode(user);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("FORM_MODE",formpage);
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("SUPER_ADMIN",new Boolean(user.getCoreRole().isSuperadmin()));
		
		setErrorMsg("");
		
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

	public void paysChange(){
		selectedCP = "";
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
	
	public void cpChange(){
		LocalDAO d = new LocalDAO();
		villes = d.getVilles(selectedCP);
	}
	
	public String signup(){
		UserDAOImpl dao = new UserDAOImpl();
		boolean mailinvalid = dao.checkUser(email);
		if(mailinvalid){
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_FATAL,"L'adresse éléctronique que vous avez saisi est déjà utilisée pour un autre compte",""));
			return "";
		}
		
		CoreUser u = new CoreUser();
		u.setAdress(adress+" - "+selectedVille+" - "+(selectedCP==null?"":selectedCP)+" - "+(countryCode.equals("+33")?"France":"Maroc"));
		
		u.setEmail(email);
		u.setAppKey("657e88a4-ca8b-4358-9b38-47d5b63180d7");
		u.setCoreRole(new CoreRole(99, ""));
		u.setFirstName(firstName);
		u.setLastName(lastName);
		u.setLogin(email);
		u.setPassword(password);
		u.setTel(countryCode.replaceAll("\\+", "00")+tel);
		u.setThemeColor("css/colors.css?v=1");
		u.setThemeStyle("css/style.css?v=1");
		u.setOrgInstance(0);
		
		dao = new UserDAOImpl();
		
		String uid = dao.insertUser(u);
		
		if(uid!= null && uid.length() > 0){
			int id = Integer.parseInt(
					uid.split("--")[1]
					);
			dao.persistUserInfo(id, "ORIAS",orias);
		}
		
		CommunicationControl ctrl = new CommunicationControl();
		
		ctrl.setSilent(true);
		ctrl.setEmailTo(email);
		ctrl.setEmailSubject("Bienvenue sur GInsurance");
		String htmlmessage = loadMessage(uid);
		ctrl.setEmailMessage(htmlmessage);
		
		ctrl.setSmsNumber(countryCode.replaceAll("\\+", "00")+tel);
		ctrl.setSmsMessage("Bienvenue sur la plate-forme GInsurance, veuillez rentrer ce code afin d'activer votre compte : "+uid);
		
		if(notifyMethod == "s"){
			ctrl.sendSMS();
			setMessage("Votre compte est créé. <br/>Vous avez reçu un message d’activation de votre compte." +
					"<br />Veuillez rentrer votre code dans <a href=\""+ProtogenParameters.APPLICATION+"/assurance/confirm.xhtml\" style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\" >cette page</a> ");
		}else{
			ctrl.sendMail();
			setMessage("Votre compte est créé.<br/>Vous avez reçu un message d’activation veuillez vérifier votre adresse de messagerie. ") ;
		}
		
		if(uid!=null && uid.length()>0)
			return "registersuccess-insurance";
		return "";
	}
	
	private String loadMessage(String uid) {
		String htmlmessage="";
		
		String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/lang/mail-assurance.html");
		htmlmessage =StringFormat.getInstance().fileToString(path);
		
		String url=ProtogenParameters.APPLICATION+"/assurance/confirm-insurance.xhtml?uid="+uid;
		htmlmessage=htmlmessage.replaceAll("<<URL_HERE>>", url);
		
		return htmlmessage;
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	
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

	public CoreUser getUser() {
		return user;
	}

	public void setUser(CoreUser user) {
		this.user = user;
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

	public List<String> getCps() {
		return cps;
	}

	public void setCps(List<String> cps) {
		this.cps = cps;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
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

	public List<String> getVilles() {
		return villes;
	}

	public void setVilles(List<String> villes) {
		this.villes = villes;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getNotifyMethod() {
		return notifyMethod;
	}

	public void setNotifyMethod(String notifyMethod) {
		this.notifyMethod = notifyMethod;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
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

	public String getOrias() {
		return orias;
	}

	public void setOrias(String orias) {
		this.orias = orias;
	}
}
