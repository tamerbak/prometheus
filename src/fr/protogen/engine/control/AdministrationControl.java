package fr.protogen.engine.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.shell.commands.ShowCommand;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.DualListModel;
import org.primefaces.model.ScheduleModel;
import org.primefaces.model.UploadedFile;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.control.ui.UITheme;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.FileManipulation;
import fr.protogen.engine.utils.PDFEngine;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.engine.utils.UIFilterElement;
import fr.protogen.importData.CheckStatus;
import fr.protogen.importData.DAL;
import fr.protogen.importData.DataFormatDriver;
import fr.protogen.importData.DataStructure;
import fr.protogen.importData.ExcelImportManager;
import fr.protogen.importData.Status;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.OCRDataAccess;
import fr.protogen.masterdata.DAO.OrganizationDAL;
import fr.protogen.masterdata.DAO.ScheduleDAO;
import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.CUserMailConfig;
import fr.protogen.masterdata.model.CoreProfil;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.CoreUserRole;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.GParametersPackage;
import fr.protogen.masterdata.model.OCRDriverBean;
import fr.protogen.masterdata.model.OCRHistory;
import fr.protogen.masterdata.model.SMenuitem;
import fr.protogen.masterdata.model.SResource;
import fr.protogen.masterdata.model.ScheduleEntry;
import fr.protogen.masterdata.services.HabilitationsService;
import fr.protogen.masterdata.services.TesseractDriver;

@ManagedBean
@SessionScoped
public class AdministrationControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7183490255374320897L;
	private List<SResource> resources = new ArrayList<SResource>();
	private SResource selectedResource = new SResource();
	private String newPassword = "";
	private String confirmPassword = "";
	private List<CoreUser> users = new ArrayList<CoreUser>();
	private CoreUser selectedUser;
	private List<CoreRole> roles = new ArrayList<CoreRole>();
	private List<CoreProfil> profils = new ArrayList<CoreProfil>();
	private int selectedProfilId=0;
	private List<CoreProfil> tprofils;
	/*
	 * Schedule
	 */
	private List<ScheduleEntry> userSchedule = new ArrayList<ScheduleEntry>();
	private ScheduleEntry handledEntry = new ScheduleEntry();
	private ScheduleModel eventModel;
	private boolean insertMode = false;
	private String title;
	private String description;
	private Date startAt;
	private Date endAt;
	private boolean rappel;
	private Date rappelAt;
	private int priority;

	/*
	 * Themes
	 */
	private String selectedTheme;
	// les css du theme developr
	private String colortheme;
	private String styletheme;
	
	/*
	 * DATA IMPORT
	 */
	private String importType;
	private String tempFile = "";
	private boolean validated = false;

	/*
	 * Options
	 */

	private List<String> superMenus;
	private List<String> menus;

	/*
	 * OCR
	 */
	private int selectedDriverId;
	private List<OCRDriverBean> drivers;
	private List<String> ocrFiles;

	/*
	 * Administration organisations
	 */
	private boolean superAdmin;
	private String selectedOrg;
	private List<PairKVElement> organizations = new ArrayList<PairKVElement>();
	private List<COrganization> objOrgs = new ArrayList<COrganization>();
	private String selectedInstance;
	private List<PairKVElement> instances = new ArrayList<PairKVElement>();

	private CoreUser nucreatingUser = new CoreUser();
	private String nuselectedTheme;
	private String nuselectedProfil;
	private List<CoreRole> nuprofiles = new ArrayList<CoreRole>();
	private String nuselectedOrg;
	private String nuselectedOrgInstance;
	private String nunewPassword;
	private String nuconfirmPassword;
	private String nuselectedEntity;
	private List<PairKVElement> entities = new ArrayList<PairKVElement>();

	/*
	 * GOrganization management
	 */
	private String orgName;
	private String selectedPkgId;
	private List<PairKVElement> pkgs;
	private List<String> selectedInstances;
	private List<PairKVElement> pkinstances;
	private String iselectedPkgId;
	private List<String> selectedRows;
	private List<PairKVElement> pkgRows;
	private boolean adminMode;
	private int selectedOrgId;
	private List<GOrganization> allOrganizations;

	/*
	 * GED
	 */
	/*
	 * Filters
	 */
	private String selectedEntity;
	private int idEntity;
	private String selectedBean;
	private List<PairKVElement> beans = new ArrayList<PairKVElement>();
	private String selectedDriver;

	private List<UIFilterElement> searchControls = new ArrayList<UIFilterElement>();
	private boolean searchControlsEnabled = false;

	/*
	 * Results table
	 */
	private List<OCRHistory> docs = new ArrayList<OCRHistory>();
	private OCRHistory selectedDoc;

	/*
	 * Themes
	 */
	private List<UITheme> themes = new ArrayList<UITheme>();

	/*
	 * Picture upload
	 */
	private UploadedFile file;
	private String filePath="";
	
	/*
	 * Profils secondaire
	 */
	private String selectedRoleSecId;
	private List<PairKVElement> rolesSec;

	/*
	 * USERS
	 */
	private List<CoreUser> allUsers = new ArrayList<CoreUser>();
	private List<CoreUser> filteredUsers;
	private DualListModel<String> picklistProfils;
	private DualListModel<String> picklistRoles;
	private List<CoreProfil> listeProfils;
	private List<CoreRole> listeRoles;
	private boolean updateUser = false;
	
	/*
	 * Configuration email
	 */
	private CUserMailConfig mailCfg;
	
	/*
	 * Nouvel utilisatteur
	 */
	private List<CoreProfil> selectedProfils;
	private List<Integer> selectedProfilsId;
	private List<CoreProfil> allProfils;
	private List<CoreRole> selectedRoles = new ArrayList<CoreRole>();
	private List<Integer> selectedRolesId = new ArrayList<Integer>();
	private List<CoreRole> profileRoles = new ArrayList<CoreRole>();
	private CoreRole selectedRolesTable[];
	private int selectedRID=0;
	private int selectedPID;
	
	@PostConstruct
	public void initialize() {

		boolean notinsession = (!FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().containsKey("USER_KEY") || FacesContext
				.getCurrentInstance().getExternalContext().getSessionMap()
				.get("USER_KEY") == null);

		if (notinsession) {
			try {

				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("login.xhtml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		tprofils= new ArrayList<CoreProfil>();
		ApplicationLoader dal = new ApplicationLoader();
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");
		String appkey = ApplicationRepository.getInstance().getCache(skey)
				.getAppKey();
		resources = dal.loadResources(appkey);

		drivers = dal.loadDrivers(appkey);

		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				skey);
		profils = cache.getProfils(); 
		roles = dal.loadApplicationRoles(cache.getAppKey());
		nuprofiles = roles;
		// Load users
		users = cache.getUsers();

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		updateUser = params.containsKey("USER_UPDATE") && params.get("USER_UPDATE").equals("TRUE");
		if(updateUser){
			int idu = Integer.parseInt(params.get("USER_ID"));
			for(CoreUser u : users){
				if(u.getId() == idu) {
					nucreatingUser = u;
					break;
				}
			}
		}
		
		HabilitationsService hsrv = new HabilitationsService();
		listeProfils = hsrv.loadProfils(cache.getAppKey());
		picklistProfils = new DualListModel<String>();
		picklistRoles = new DualListModel<String>();
		picklistProfils.setSource(new ArrayList<String>());
		picklistProfils.setTarget(new ArrayList<String>());
		picklistRoles.setSource(new ArrayList<String>());
		picklistRoles.setTarget(new ArrayList<String>());
		
		allProfils = listeProfils;
		profileRoles = new ArrayList<CoreRole>();
		for(CoreProfil p : listeProfils){
			picklistProfils.getSource().add(p.getLibelle());
		}
		
		for (CoreUser u : users) {
			for (CoreRole r : roles)
				if (r.getId() == u.getCoreRole().getId()) {
					u.setCoreRole(r);
					break;
				}
		}
		selectedUser = cache.getUser();

		cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();

		selectedTheme = user.getUserTheme();

		eventModel = new DefaultScheduleModel();

		ScheduleDAO dao = new ScheduleDAO();
		userSchedule = dao.loadSchedule(user);

		for (ScheduleEntry e : userSchedule) {
			eventModel.addEvent(new DefaultScheduleEvent(e.getTitle(), e
					.getStartAt(), e.getEndAt(), e));
		}

		/*
		 * Load options
		 */
		ApplicationLoader al = new ApplicationLoader();
		superMenus = al.loadInstalledOptions(selectedUser);
		menus = new ArrayList<String>();
		for (SMenuitem i : cache.getOptions()) {
			menus.add(i.getTitle());
		}

		/*
		 * Admin options
		 */
		superAdmin = ((Boolean) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("SUPER_ADMIN"))
				.booleanValue();

		
		
		objOrgs = al.loadOrganizations(appkey);
		organizations = new ArrayList<PairKVElement>();
		for (COrganization o : objOrgs)
			organizations.add(new PairKVElement(o.getId() + "", o.getLabel()));
		if (objOrgs != null && objOrgs.size() > 0) {
			selectedOrg = objOrgs.get(0).getId() + "";
			instances = objOrgs.get(0).getInstances();
		}
		OrganizationDAL odal = new OrganizationDAL();
		allOrganizations = odal.loadAllOrgs();	
		for(GOrganization o : allOrganizations)
			o = odal.populate(o);
		
		// GED
		drivers = dal.loadDrivers(appkey);

		// GOrganization
		orgName = cache.getOrganization().getName();
		pkgs = new ArrayList<PairKVElement>();

		for (GParametersPackage p : cache.getModels()) {
			pkgs.add(new PairKVElement(p.getId() + "", p.getNom()));
		}

		if (pkgs.size() > 0) {
			selectedPkgId = pkgs.get(0).getKey();
			int ip = Integer.parseInt(selectedPkgId);
			pkinstances = new ArrayList<PairKVElement>();

			String table = "";
			for (GParametersPackage i : cache.getModels()) {
				if (i.getId() == ip) {
					table = i.getEntity().getDataReference();
					break;
				}

			}

			pkinstances = odal.loadPackageInstances(ip, table);
			ProtogenDataEngine pde = new ProtogenDataEngine();
			pkgRows = pde.getDataKeys(table, false, 0);
		}

		/*
		 * Themes
		 */
		themes = new ArrayList<UITheme>();
		UITheme b = new UITheme();
		b.setId(1);
		b.setNom("Ciel clair");
		if (selectedUser.getUserTheme().equals("THEME:BLEU"))
			b.setImage("deepblueSelected.png");
		else
			b.setImage("deepblue.png");
		themes.add(b);

		UITheme r = new UITheme();
		r.setId(2);
		r.setNom("Rouge et noir");
		if (selectedUser.getUserTheme().equals("THEME:ROUGE"))
			r.setImage("redemptionSelected.png");
		else
			r.setImage("redemption.png");
		themes.add(r);

		UITheme n = new UITheme();
		n.setId(3);
		n.setNom("Optimisé");
		if (selectedUser.getUserTheme().equals("THEME:DEVELOPR"))
			n.setImage("newdesignSelected.png");
		else
			n.setImage("newdesign.png");
		themes.add(n);
		
		/*
		 * Roles secondaire
		 */
		rolesSec= new ArrayList<PairKVElement>();

		for (CoreUserRole cur : dal.getRolesSec(user)) {
			CoreRole role = cur.getCoreRole();
			rolesSec.add(new PairKVElement(role.getId() + "", role.getRole()));
		}
		
		/*
		 * Configuration mail
		 */
		setMailCfg(dal.loadMailConfig(selectedUser));
	}
	
	public String toEditUser(){
		String editingUser=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("USER_UPDATE");
		
		if(editingUser!=null && editingUser.equalsIgnoreCase("true")){
			int userId=Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("USERID"));
			for(CoreUser u : users){
				if(u.getId() == userId) {
					nucreatingUser = u;
					break;
				}
			}
			picklistProfils.setSource(new ArrayList<String>());
			picklistProfils.setTarget(new ArrayList<String>());
			picklistRoles.setSource(new ArrayList<String>());
			picklistRoles.setTarget(new ArrayList<String>());
			
			HabilitationsService srv = new HabilitationsService();
			
			//	Update profiles
			List<Integer> userRoles = srv.loadUsersRoles(userId);
			List<CoreRole> fullRoles=srv.loadRolesByProfil(listeProfils);
			
			List<String> roleSource=new ArrayList<String>();
			List<String> roleTarget=new ArrayList<String>();
			List<String> profilSource=new ArrayList<String>();
			List<String> profilTarget=new ArrayList<String>();
			List<Integer> selectedProfil=new ArrayList<Integer>();
			
			for(CoreRole r:fullRoles){
				if(userRoles.contains(r.getId())){
					roleTarget.add(r.getRole());
					if(r.getProfil()!=null && !selectedProfil.contains(r.getProfil().getId())){
						selectedProfil.add(r.getProfil().getId());
						profilTarget.add(r.getProfil().getLibelle());
					}
				}else if(r.getProfil()!=null && selectedProfil.contains(r.getProfil().getId())){
						roleSource.add(r.getRole());
					
				}
			}
			for(CoreProfil p : listeProfils){
				if(!selectedProfil.contains(p.getId())){
					profilSource.add(p.getLibelle());
				}
			}
			
			picklistProfils.setSource(profilSource);
			picklistProfils.setTarget(profilTarget);
			picklistRoles.setSource(roleSource);
			picklistRoles.setTarget(roleTarget);
			
			
		}
		
		
		return "parametrage-new-utilisateur";
	}
	public String toNewUser(){
		
			if(tprofils!=null){
				tprofils.clear();
			}
			if(listeRoles!=null){
				listeRoles.clear();
			}
			List<String> allProfils=picklistProfils.getSource();
			allProfils.addAll(picklistProfils.getTarget());
			picklistProfils.setSource(allProfils);
			picklistProfils.setTarget(new ArrayList<String>());
			picklistRoles.setSource(new ArrayList<String>());
			picklistRoles.setTarget(new ArrayList<String>());
			
			nucreatingUser=new CoreUser();
		
		
		return "parametrage-new-utilisateur";
	}
	public void selectedProfileChanged(){
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		ApplicationLoader dal = new ApplicationLoader();
		if(selectedProfilId == 0){
			roles = dal.loadApplicationRoles(cache.getAppKey());
			nuprofiles = roles;
			profileChanged();
			return;
		}
		
		CoreProfil pr = new CoreProfil();
		for(CoreProfil p : profils){
			if(p.getId() == selectedProfilId){
				pr = p;
				break;
			}
		}
		roles = dal.loadApplicationRoles(cache.getAppKey(),pr);
		nuprofiles = roles;
		profileChanged();
	}
	
	public void transferProfils(){
		selectedProfils = new ArrayList<CoreProfil>();
		for(Integer id : selectedProfilsId)
			for(CoreProfil p : allProfils)
				if(p.getId() == id.intValue()){
					selectedProfils.add(p);
					break;
				}
			
		
		if(selectedRoles == null)
			selectedRoles = new ArrayList<CoreRole>();
		
		HabilitationsService hsrv = new HabilitationsService();
		
		
		List<CoreRole> newRoles = hsrv.loadRolesByProfil(selectedProfils);
		
		for(CoreRole newR : newRoles){
			boolean flag = true;
			for(CoreRole r : profileRoles){
			
				if(r.getId() == newR.getId()){
					flag = false;
					break;
				}
			
			}
			if(flag)
				profileRoles.add(newR);
		}
		
	}
	
	public void transferRoles(){
		selectedRoles = new ArrayList<CoreRole>();
		if(selectedRolesTable != null){
			for(CoreRole r : selectedRolesTable){
				selectedRoles.add(r);
			}
		}
	}
	
	public void addProfilToList(){
		CoreProfil profil = null;
		for(CoreProfil p : allProfils){
			if(p.getId() == selectedPID){
				profil = p;
				break;
			}
		}
		if(profil == null)
			return;
		List<CoreProfil> singleton = new ArrayList<CoreProfil>();
		singleton.add(profil);
		
		HabilitationsService hsrv = new HabilitationsService();
		
		
		List<CoreRole> newRoles = hsrv.loadRolesByProfil(singleton);
		
		for(CoreRole newR : newRoles){
			boolean flag = true;
			for(CoreRole r : profileRoles){
			
				if(r.getId() == newR.getId()){
					flag = false;
					break;
				}
			
			}
			if(flag)
				profileRoles.add(newR);
		}
	}
	
	public void addRoleToList(){
		CoreRole role = null;
		for(CoreRole r : profileRoles){
			if(r.getId() == selectedRID){
				role = r;
				break;
			}
		}
		
		if(role == null)
			return;
		
		for(CoreRole r : selectedRoles){
			if(r.getId() == selectedRID)
				return;
		}
		
		selectedRoles.add(role);
	}
	
	public void onTransfer(TransferEvent evt){
		
		HabilitationsService hsrv = new HabilitationsService();
		
		List<CoreProfil> removedProfile = null;
		for(Object sp : evt.getItems())
			for(CoreProfil p : listeProfils)
				if(p.getLibelle().equals(sp.toString())){
					if(evt.isAdd()){
						tprofils.add(p);
					}
					if(evt.isRemove()){
						removedProfile=new ArrayList<CoreProfil>();
						tprofils.remove(p);
						removedProfile.add(p);
					}
					
					break;
				}
		
		listeRoles = hsrv.loadRolesByProfil(tprofils);
		if(evt.isRemove()){
			List<CoreRole> toremoveRoles=new ArrayList<CoreRole>();
			toremoveRoles=hsrv.loadRolesByProfil(removedProfile);
			for(CoreRole r : toremoveRoles){
				if(picklistRoles.getTarget().contains(r.getRole()))
					picklistRoles.getTarget().remove(r.getRole());
			}
		}
		picklistRoles.setSource(new ArrayList<String>());
		for(CoreRole r : listeRoles){
			if(picklistRoles.getTarget().contains(r.getRole()))
				continue;
			picklistRoles.getSource().add(r.getRole());
		}
	}
	
	public void onDummyTransfer(){
		
	}
	
	public void updateChosenTheme() {
		Map<String, String> p = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (!p.containsKey("thm"))
			return;

		int id = Integer.parseInt(p.get("thm"));
		String nom = "";
		if (id == 1) {
			selectedTheme = "THEME:BLEU";
			nom = themes.get(0).getNom();
			if(colortheme == null){
				colortheme= "css/admin.css";
				return;
			}if(colortheme.split(".css")[0].split("_").length != 1)
				colortheme = "css/admin_" + colortheme.split(".css")[0].split("_")[1]+ ".css";
			else
				colortheme= "css/admin.css";
		} else if (id == 2) {
			selectedTheme = "THEME:ROUGE";
			nom = themes.get(1).getNom();
			if(colortheme == null){
				colortheme= "css/layout.css";
				return;
			}if(colortheme.split(".css")[0].split("_").length != 1)
				colortheme = "css/layout_" + colortheme.split(".css")[0].split("_")[1]+ ".css";
			else
				colortheme= "css/layout.css";
		} else {
			selectedTheme = "THEME:DEVELOPR";
			nom = themes.get(2).getNom();
			if(colortheme == null){
				colortheme = "css/colors.css?v=1";
				styletheme = "css/style.css?v=1";
				return;
			}
			if(colortheme.split(".css")[0].split("_").length != 1){
				colortheme = "css/colors_" + colortheme.split(".css")[0].split("_")[1]+ ".css?v=1";
				styletheme = "css/style_" + styletheme.split(".css")[0].split("_")[1]+ ".css?v=1";
			}
			else{
				colortheme = "css/colors.css?v=1";
				styletheme = "css/style.css?v=1";
			}
		}

		FacesContext.getCurrentInstance().addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Thème choisi : "
						+ nom, ""));
	}

	public void updateColorTheme() {
		Map<String, String> p = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		if (p.containsKey("colorthm")) {
			if (selectedTheme.equals("THEME:DEVELOPR")) {
				colortheme = p.get("colorthm");
			}
		}
		if (p.containsKey("adminthm")) {
			if (selectedTheme.equals("THEME:BLEU")) {
				colortheme = p.get("adminthm");
			}
		}
		if (p.containsKey("layoutthm")) {
			if (selectedTheme.equals("THEME:ROUGE")) {
				colortheme = p.get("layoutthm");
			}
		}
		if (p.containsKey("stylethm"))
			styletheme = p.get("stylethm");
	}

	public String chPwd() {
		ApplicationLoader dal = new ApplicationLoader();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		cache.getUser().setUserTheme(selectedTheme);
		cache.getUser().setThemeColor(colortheme);
		cache.getUser().setThemeStyle(styletheme);
		CoreUser cu = cache.getUser();

		FrontController fctrl = (FrontController) FacesContext
				.getCurrentInstance().getExternalContext().getSessionMap()
				.get("FRONT_CTRL");
		fctrl.alterTheme();

		cu.setAdress(selectedUser.getAdress());
		cu.setFirstName(selectedUser.getFirstName());
		cu.setLastName(selectedUser.getLastName());
		cu.setTel(selectedUser.getTel());
		cu.setThemeColor(colortheme);
		cu.setThemeStyle(styletheme);
		cu.setUserTheme(selectedTheme);
		if (newPassword != null && newPassword.length() > 0)
			cu.setPassword(newPassword);

		String sopt = "";
		for (String t : superMenus) {
			for (SMenuitem i : cache.getOptions())
				if (i.getTitle().equals(t)) {
					sopt = sopt + i.getId() + ";";
					break;
				}

		}

		if (sopt.length() > 0)
			sopt = sopt.substring(0, sopt.length() - 1);

		cu.setSoptions(sopt);

		dal.updateApplicationUser(cu);

		FacesContext.getCurrentInstance().addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Profil mis à jour",
						"Votre compte a été mis à jour avec succès"));

		fctrl.logout();

		return "login";
	}

	public void saveUser() {
		ApplicationLoader dal = new ApplicationLoader();

		dal.updateApplicationUser(selectedUser);

		FacesContext.getCurrentInstance().addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès",
						"Mot de passe mis à jour"));

	}

	public String doUpdate() {
		FacesContext
				.getCurrentInstance()
				.getExternalContext()
				.getSessionMap()
				.put(ProtogenConstants.RES_TOUPDATE,
						new Integer(selectedResource.getId()));
		return "protogen-resupdate";
	}

	/*
	 * SCHEDULE MANAGEMENT
	 */
	public void onEventSelect(SelectEvent selectEvent) {
		// event = (ScheduleEvent) selectEvent.getObject();
	}

	public void onDateSelect(SelectEvent selectEvent) {
		// event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(),
		// (Date) selectEvent.getObject());
		insertMode = true;
	}

	@SuppressWarnings("unused")
	public void selectDateChange() {
		String dummy = "";
		insertMode = true;
		handledEntry = new ScheduleEntry();
	}

	public void onEventMove(ScheduleEntryMoveEvent event) {
		// FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
		// "Event moved", "Day delta:" + event.getDayDelta() + ", Minute delta:"
		// + event.getMinuteDelta());

	}

	public void onEventResize(ScheduleEntryResizeEvent event) {
		// FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
		// "Event resized", "Day delta:" + event.getDayDelta() +
		// ", Minute delta:" + event.getMinuteDelta());

	}

	public void validateEntry() {
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		handledEntry = new ScheduleEntry();
		handledEntry.setTitle(title);
		handledEntry.setDescription(description);
		handledEntry.setStartAt(startAt);
		handledEntry.setEndAt(endAt);
		handledEntry.setRappel(rappel);
		handledEntry.setRappelAt(rappelAt);
		handledEntry.setPriority(priority);
		handledEntry.setUser(user);
		ScheduleDAO dao = new ScheduleDAO();
		dao.insertEntry(user, handledEntry);
	}

	public String insertEntry() {
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		handledEntry = new ScheduleEntry();
		handledEntry.setTitle(title);
		handledEntry.setDescription(description);
		handledEntry.setStartAt(startAt);
		handledEntry.setEndAt(endAt);
		handledEntry.setRappel(rappel);
		handledEntry.setRappelAt(rappelAt);
		handledEntry.setPriority(priority);
		handledEntry.setUser(user);
		ScheduleDAO dao = new ScheduleDAO();
		dao.insertEntry(user, handledEntry);
		return "";
	}

	/*
	 * DATA IMPORT
	 */
	public void handleFileUpload(FileUploadEvent event) {
		try {
			InputStream is = event.getFile().getInputstream();
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			String pathServer = ec.getRealPath("") + "/tmp/";
			FileManipulation.getInstance(pathServer).saveTempFile(".xls", is,
					ec.getSessionMap().get("USER_KEY").toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void validateInput() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		tempFile = ec.getRealPath("") + "/tmp/"
				+ ec.getSessionMap().get("USER_KEY").toString() + ".xls";

		DataFormatDriver dtim = ExcelImportManager.getInstance();
		CheckStatus check = dtim.chechFormat(tempFile);
		if (check.getStatus() == Status.INFO) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", ""));
			validated = true;
		} else if (check.getStatus() == Status.WARNING) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Fichier validé avec des avertissements", check
									.getDescription()));
			validated = true;
		} else if (check.getStatus() == Status.ERROR) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Fichier non validé", check.getDescription()));
			validated = false;
		} else if (check.getStatus() == Status.FATAL) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL,
							"Fichier non validé", check.getDescription()));
			validated = false;
		}
		if (!validated) {
			String pathServer = ec.getRealPath("") + "/tmp/";
			FileManipulation.getInstance(pathServer).deleteTempFile(tempFile);
		}
	}

	public void importData() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		tempFile = ec.getRealPath("") + "/tmp/"
				+ ec.getSessionMap().get("USER_KEY").toString() + ".xls";

		DAL dt = DAL.getInstance();
		ExcelImportManager dtim = ExcelImportManager.getInstance();
		DataStructure data = dtim.importData(tempFile);
		CheckStatus check = dt.dataSave(data);
		if (check.getStatus() == Status.INFO) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", ""));
			validated = false;
		} else if (check.getStatus() == Status.WARNING) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Fichier importé avec des avertissements", check
									.getDescription()));
			validated = false;
		} else if (check.getStatus() == Status.ERROR) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Fichier non importé", check.getDescription()));
			validated = false;
		} else if (check.getStatus() == Status.FATAL) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL,
							"Fichier non importé", check.getDescription()));
			validated = false;
		}

		String pathServer = ec.getRealPath("") + "/tmp/";
		FileManipulation.getInstance(pathServer).deleteTempFile(tempFile);
	}

	/*
	 * OCR
	 */
	public void handleMultiFileUpload(FileUploadEvent event) {
		try {
			if (ocrFiles == null)
				ocrFiles = new ArrayList<String>();
			InputStream is = event.getFile().getInputstream();
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			
			String pathServer = ec.getRealPath("") + "/tmp/";

			File rdir = new File(pathServer);
			if(!rdir.exists())
				rdir.mkdir();
			
			String filename = event.getFile().getFileName();
			FileManipulation.getInstance(pathServer).saveFile(
					pathServer + ec.getSessionMap().get("USER_KEY").toString()
							+ "-" + filename, is);
			String fullPath = pathServer
					+ ec.getSessionMap().get("USER_KEY").toString() + "-"
					+ filename;
			if (event.getFile().getFileName().endsWith(".pdf")) {
				String pngFullPath = fullPath.replaceAll("pdf", "png");
				PDFEngine eng = new PDFEngine();
				eng.convertToPNG(fullPath, pngFullPath);
				fullPath = pngFullPath;
			}

			ocrFiles.add(fullPath);

			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("OCR_FILES", ocrFiles);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void ocrInit() {
		ocrFiles = new ArrayList<String>();
	}

	@SuppressWarnings("unchecked")
	public void ocrImportData() {
		ocrFiles = (List<String>) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("OCR_FILES");
		OCRDriverBean bean = new OCRDriverBean();
		for (OCRDriverBean b : drivers)
			if (b.getId() == selectedDriverId) {
				bean = b;
				break;
			}

		// OCRDriver driver = new OCRDriver(bean);
		TesseractDriver driver = new TesseractDriver(bean);
		driver.loadData(ocrFiles);
	}

	/*
	 * ORGANIZATONS
	 */
	
	public void updateToOrg(){
		GOrganization org = null;
		for(GOrganization g : allOrganizations){
			if(g.getId() == selectedOrgId){
				org = g;
				break;
			}
		}
		
		if(org == null)
			return;
		
		FrontController frtCtrl = (FrontController) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("FRONT_CTRL");
		frtCtrl.setOrganizationName(org.getName());
		ApplicationCache cache = ApplicationRepository.getInstance()
				.getCache(
						(String) FacesContext.getCurrentInstance()
								.getExternalContext().getSessionMap()
								.get("USER_KEY"));
		cache.updateOrganization(org);
	}
	
	public void orgChanged() {
		for (COrganization o : objOrgs) {
			if (o.getId() == Integer.parseInt(selectedOrg)) {
				instances = o.getInstances();
				break;
			}
		}
	}

	public void createOrgInstance() {
		ApplicationLoader dal = new ApplicationLoader();
		dal.saveOrganization(Integer.parseInt(selectedOrg),
				Integer.parseInt(selectedInstance));
		PairKVElement elm = null;

		for (PairKVElement e : instances)
			if (e.getKey().equals(selectedInstance)) {
				elm = e;
				break;
			}

		if (elm != null) {
			instances.remove(elm);
			for (COrganization org : objOrgs)
				if (org.getId() == Integer.parseInt(selectedOrg)) {
					org.getInstances().remove(elm);
					break;
				}
		}

	}

	public void deleteUser(){
		int idUser = 0;
		try{
			idUser = Integer.parseInt(
					FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("USERID")
					);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		HabilitationsService hsrv = new HabilitationsService();
		boolean deleted = hsrv.deleteUser(idUser);
		if(!deleted){
			FacesContext.getCurrentInstance().addMessage
			(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Impossible de suprimmer ce profil", 
					"Vérifiez s'il existe toujours des rôles attachés à ce profil"));
			return;
		} else {
			FacesContext.getCurrentInstance().addMessage
			(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Profil supprimé avec succès", 
					""));
		}
		
		CoreUser cp = new CoreUser();
		for(CoreUser p : users)
			if(p.getId() == idUser){
				cp = p;
				break;
			}
		users.remove(cp);
	}
	
	public void createUserInstance() {
		nucreatingUser.setPassword(nunewPassword);
		nucreatingUser.setEmail(nucreatingUser.getLogin());
		CoreRole role = null;
		for (CoreRole r : nuprofiles)
			if (r.getId() == Integer.parseInt(nuselectedProfil)) {
				role = r;
				break;
			}
		
		/*role.setProfil(new CoreProfil());
		for(CoreProfil p : profils){
			if(p.getId() == selectedProfilId){
				role.setProfil(p);
				break;
			}
		}*/
		
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");
		String appkey = ApplicationRepository.getInstance().getCache(skey)
				.getAppKey();
		nucreatingUser.setAppKey(appkey);
		nucreatingUser.setCoreRole(role);
		nucreatingUser.setBoundEntity(Integer.parseInt(nuselectedEntity));


		COrganization org = null;

		for (COrganization o : objOrgs) {
			if (o.getId() == new Integer(nuselectedOrg)) {
				org = o;
				break;
			}
		}

		nucreatingUser.setOrganization(new COrganization());
		if(nuselectedOrgInstance!=null && nuselectedOrgInstance.length()>0){
			nucreatingUser.setOrganization(org);
			nucreatingUser.setOrgInstance(new Integer(nuselectedOrgInstance));
		}
		UserDAOImpl dao = new UserDAOImpl();
		
		//	Je n'ai pas encore traité la modification

		int uid = dao.persist(nucreatingUser);

		nucreatingUser.setId(uid);
		
		//		RAZ habilitation
		HabilitationsService srv = new HabilitationsService();
		
		//	Update profiles
		if(selectedRolesTable != null){
			srv.majRoles(selectedRoles, nucreatingUser);
			selectedRolesTable = null;
		}
		nucreatingUser = new CoreUser();
		nunewPassword = "";
	}

	public void profileChanged() {
		int profid = Integer.parseInt(nuselectedProfil);

		CoreRole srole = null;
		for (CoreRole r : roles) {
			if (r.getId() == profid) {
				srole = r;
				break;
			}
		}

		int iden = srole.getBoundEntity();

		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass e = pde.getEntityById(iden);

		entities = pde.getDataKeys(e.getDataReference(), false, 0);
	}

	/*
	 * GED
	 */
	/*
	 * Filters
	 */
	public void entityChanged() {
		if (selectedEntity.equals(""))
			return;
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationLoader dal = new ApplicationLoader();
		beans = engine.getDataKeys(selectedEntity, false, 0);
		CBusinessClass ent = dal.getEntity(selectedEntity);
		idEntity = ent.getId();

		// Prepare semantic access filters
		searchControls = new ArrayList<UIFilterElement>();
		List<CAttribute> attributes = ent.getAttributes();
		searchControlsEnabled = (attributes != null && attributes.size() > 0);
		for (CAttribute attribute : attributes) {

			if (attribute.getDataReference().startsWith("pk_")
					|| attribute.isMultiple())
				continue;

			UIFilterElement element = new UIFilterElement();

			System.out.println("************ parsing "
					+ attribute.getAttribute());
			String type = attribute.getCAttributetype().getType();
			if (type == null)
				type = "";
			if (attribute.isReference()) {
				String referenceTable = attribute.getDataReference().substring(
						3);
				String referencedEntity = dal.getEntityFromDR(referenceTable);
				CBusinessClass e = dal.getEntity(referenceTable);
				ApplicationCache cache = ApplicationRepository.getInstance()
						.getCache(
								(String) FacesContext.getCurrentInstance()
										.getExternalContext().getSessionMap()
										.get("USER_KEY"));
				List<PairKVElement> list = engine.getDataKeys(referenceTable,
						(e.getUserRestrict() == 'Y'), cache.getUser().getId());
				element.setReferenceTable(referencedEntity);
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				if (!attribute.isMandatory())
					listElements.add(new PairKVElement("0", ""));
				for (PairKVElement kv : list) {
					listElements.add(kv);
				}
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute().replaceAll("ID ",
						""));
				element.setControlValue("");
				element.setListReference(listElements);
				element.setReference(true);
				searchControls.add(element);
				continue;
			}
			if (type.equals("ENTIER")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (type.toUpperCase().equals("HEURE")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (type.equals("TEXT") || type.equals("Texte")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (type.toUpperCase().equals("DATE")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
				element.setCtrlDate(true);
			} else if (type.toUpperCase().equals("DOUBLE")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (attribute.getCAttributetype().getId() == 7) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				ApplicationCache cache = ApplicationRepository.getInstance()
						.getCache(
								(String) FacesContext.getCurrentInstance()
										.getExternalContext().getSessionMap()
										.get("USER_KEY"));
				CoreUser u = cache.getUser();
				element.setControlValue(u.getFirstName() + " - "
						+ u.getLastName());
				element.setTrueValue(u.getId() + "");
				searchControls.add(element);
			} else if (attribute.getCAttributetype().getId() == 8) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (attribute.getCAttributetype().getId() == 9) {

				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			}
			System.out.println("************ parsed "
					+ attribute.getAttribute());
		}
		return;
	}

	public String suivant() {
		return "protogen-datahistorys2";
	}

	/*
	 * Research
	 */
	public String search() {
		OCRDataAccess dal = new OCRDataAccess();
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				skey);
		docs = dal.lookUp(selectedBean, selectedDriver, idEntity,
				searchControls);

		for (OCRHistory d : docs) {
			for (OCRDriverBean db : drivers) {
				if (d.getDriver().getId() == db.getId()) {
					d.setDriver(db);
					break;
				}
			}
			for (CoreUser u : cache.getUsers()) {
				if (u.getId() == d.getUser().getId()) {
					d.setUser(u);
					break;
				}
			}
		}
		return "protogen-datahistoryres";
	}

	public String downloadSelectedDoc() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		ec.responseReset();
		ec.setResponseContentType("image/png");
		ec.setResponseHeader("Content-Disposition",
				"attachment; filename=\"document.png\"");

		OutputStream output;

		try {
			output = ec.getResponseOutputStream();
			InputStream is = new FileInputStream(selectedDoc.getFileKey());

			byte[] content = IOUtils.toByteArray(is);

			is.close();
			output.write(content);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	/*
	 * GOrganization management
	 */
	public void parameterPkgChange() {
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		int ip = Integer.parseInt(selectedPkgId);
		pkinstances = new ArrayList<PairKVElement>();
		OrganizationDAL odal = new OrganizationDAL();

		String table = "";
		for (GParametersPackage i : cache.getModels()) {
			if (i.getId() == ip) {
				table = i.getEntity().getDataReference();
				break;
			}

		}

		pkinstances = odal.loadPackageInstances(ip, table);
	}

	public void iparameterPkgChange() {

		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		int ip = Integer.parseInt(selectedPkgId);
		pkinstances = new ArrayList<PairKVElement>();
		OrganizationDAL odal = new OrganizationDAL();

		String table = "";
		for (GParametersPackage i : cache.getModels()) {
			if (i.getId() == ip) {
				table = i.getEntity().getDataReference();
				break;
			}

		}

		pkinstances = odal.loadPackageInstances(ip, table);
		ProtogenDataEngine pde = new ProtogenDataEngine();
		pkgRows = pde.getDataKeys(table, false, 0);

	}

	public void saveParameters() {
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		OrganizationDAL odal = new OrganizationDAL();
		List<Integer> ins = new ArrayList<Integer>();
		for (String si : selectedInstances) {
			ins.add(new Integer(si));
		}
		int idorg = cache.getOrganization().getId();

		odal.saveInstanceMapping(idorg, ins);
	}

	public void saveInstanceParameters() {
		List<Integer> ins = new ArrayList<Integer>();

		for (String si : selectedRows)
			ins.add(new Integer(si));

		OrganizationDAL odal = new OrganizationDAL();
		int pkgId = Integer.parseInt(iselectedPkgId);
		odal.addInstances(pkgId, ins);

	}

	/*
	 * PHOTO DE PROFIL
	 */
	public void upload() {
        if(file != null) {
        	ApplicationCache cache = ApplicationRepository.getInstance().getCache(
    				(String) FacesContext.getCurrentInstance().getExternalContext()
    						.getSessionMap().get("USER_KEY"));
        	String ext = file.getFileName().substring(file.getFileName().lastIndexOf('.'));
        	String sfile = cache.getUser().getId()+ext;
        	String webpath = sfile;
        	sfile = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+"/photos/"+sfile;
        	File dir  = new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+"/photos/");
        	if(!dir.exists() || !dir.isDirectory())
        		dir.mkdir();
        	
        	try {
        		File f = new File(sfile);
            	if(f.exists())
            		f.delete();
            	OutputStream os = new FileOutputStream(f);
				IOUtils.write(file.getContents(), os);
				os.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	UserDAOImpl dao = new UserDAOImpl();
        	dao.updateProfilePicture(cache.getUser(), webpath);
        }
    }
	/*
	 * Role secondaire
	 */
	public String switchRole(){
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				skey);
		ApplicationLoader dal = new ApplicationLoader();
		CoreRole coreRole = dal.getRoleByID(Integer.parseInt(selectedRoleSecId));
		cache.getUser().setCoreRole(coreRole);
		cache.updateCache();
		FrontController frtCtrl = (FrontController) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("FRONT_CTRL");
		frtCtrl.intialize();
		return "protogen";	
	}
	
	public void updateMailConfig(){
		ApplicationLoader dal = new ApplicationLoader();
		if(mailCfg.getId() == 0)
			dal.saveMailConfig(selectedUser, mailCfg);
		else
			dal.updateMailConfig(selectedUser, mailCfg);
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Paramétres Email mis à jour avc succès"));
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public List<SResource> getResources() {
		return resources;
	}

	public void setResources(List<SResource> resources) {
		this.resources = resources;
	}

	public SResource getSelectedResource() {
		return selectedResource;
	}

	public void setSelectedResource(SResource selectedResource) {
		this.selectedResource = selectedResource;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public CoreUser getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(CoreUser selectedUser) {
		this.selectedUser = selectedUser;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public List<CoreUser> getUsers() {
		return users;
	}

	public void setUsers(List<CoreUser> users) {
		this.users = users;
	}

	public List<CoreRole> getRoles() {
		return roles;
	}

	public void setRoles(List<CoreRole> roles) {
		this.roles = roles;
	}

	public List<ScheduleEntry> getUserSchedule() {
		return userSchedule;
	}

	public void setUserSchedule(List<ScheduleEntry> userSchedule) {
		this.userSchedule = userSchedule;
	}

	public ScheduleEntry getHandledEntry() {
		return handledEntry;
	}

	public void setHandledEntry(ScheduleEntry handledEntry) {
		this.handledEntry = handledEntry;
	}

	public ScheduleModel getEventModel() {
		return eventModel;
	}

	public void setEventModel(ScheduleModel eventModel) {
		this.eventModel = eventModel;
	}

	public boolean isInsertMode() {
		return insertMode;
	}

	public void setInsertMode(boolean insertMode) {
		this.insertMode = insertMode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Date getEndAt() {
		return endAt;
	}

	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}

	public boolean isRappel() {
		return rappel;
	}

	public void setRappel(boolean rappel) {
		this.rappel = rappel;
	}

	public Date getRappelAt() {
		return rappelAt;
	}

	public void setRappelAt(Date rappelAt) {
		this.rappelAt = rappelAt;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getSelectedTheme() {
		return selectedTheme;
	}

	public void setSelectedTheme(String selectedTheme) {
		this.selectedTheme = selectedTheme;
	}

	public String getImportType() {
		return importType;
	}

	public void setImportType(String importType) {
		this.importType = importType;
	}

	public String getTempFile() {
		return tempFile;
	}

	public void setTempFile(String tempFile) {
		this.tempFile = tempFile;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public List<String> getSuperMenus() {
		return superMenus;
	}

	public void setSuperMenus(List<String> superMenus) {
		this.superMenus = superMenus;
	}

	public List<String> getMenus() {
		return menus;
	}

	public void setMenus(List<String> menus) {
		this.menus = menus;
	}

	public int getSelectedDriverId() {
		return selectedDriverId;
	}

	public void setSelectedDriverId(int selectedDriverId) {
		this.selectedDriverId = selectedDriverId;
	}

	public List<OCRDriverBean> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<OCRDriverBean> drivers) {
		this.drivers = drivers;
	}

	public List<String> getOcrFiles() {
		return ocrFiles;
	}

	public void setOcrFiles(List<String> ocrFiles) {
		this.ocrFiles = ocrFiles;
	}

	public boolean isSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(boolean superAdmin) {
		this.superAdmin = superAdmin;
	}

	public String getSelectedOrg() {
		return selectedOrg;
	}

	public void setSelectedOrg(String selectedOrg) {
		this.selectedOrg = selectedOrg;
	}

	public List<PairKVElement> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<PairKVElement> organizations) {
		this.organizations = organizations;
	}

	public String getSelectedInstance() {
		return selectedInstance;
	}

	public void setSelectedInstance(String selectedInstance) {
		this.selectedInstance = selectedInstance;
	}

	public List<PairKVElement> getInstances() {
		return instances;
	}

	public void setInstances(List<PairKVElement> instances) {
		this.instances = instances;
	}

	public List<COrganization> getObjOrgs() {
		return objOrgs;
	}

	public void setObjOrgs(List<COrganization> objOrgs) {
		this.objOrgs = objOrgs;
	}

	public CoreUser getNucreatingUser() {
		return nucreatingUser;
	}

	public void setNucreatingUser(CoreUser nucreatingUser) {
		this.nucreatingUser = nucreatingUser;
	}

	public String getNuselectedTheme() {
		return nuselectedTheme;
	}

	public void setNuselectedTheme(String nuselectedTheme) {
		this.nuselectedTheme = nuselectedTheme;
	}

	public String getNuselectedProfil() {
		return nuselectedProfil;
	}

	public void setNuselectedProfil(String nuselectedProfil) {
		this.nuselectedProfil = nuselectedProfil;
	}

	public List<CoreRole> getNuprofiles() {
		return nuprofiles;
	}

	public void setNuprofiles(List<CoreRole> nuprofiles) {
		this.nuprofiles = nuprofiles;
	}

	public String getNuselectedOrg() {
		return nuselectedOrg;
	}

	public void setNuselectedOrg(String nuselectedOrg) {
		this.nuselectedOrg = nuselectedOrg;
	}

	public String getNuselectedOrgInstance() {
		return nuselectedOrgInstance;
	}

	public void setNuselectedOrgInstance(String nuselectedOrgInstance) {
		this.nuselectedOrgInstance = nuselectedOrgInstance;
	}

	public String getNunewPassword() {
		return nunewPassword;
	}

	public void setNunewPassword(String nunewPassword) {
		this.nunewPassword = nunewPassword;
	}

	public String getNuconfirmPassword() {
		return nuconfirmPassword;
	}

	public void setNuconfirmPassword(String nuconfirmPassword) {
		this.nuconfirmPassword = nuconfirmPassword;
	}

	public String getNuselectedEntity() {
		return nuselectedEntity;
	}

	public void setNuselectedEntity(String nuselectedEntity) {
		this.nuselectedEntity = nuselectedEntity;
	}

	public List<PairKVElement> getEntities() {
		return entities;
	}

	public void setEntities(List<PairKVElement> entities) {
		this.entities = entities;
	}

	public String getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(String selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public int getIdEntity() {
		return idEntity;
	}

	public void setIdEntity(int idEntity) {
		this.idEntity = idEntity;
	}

	public String getSelectedBean() {
		return selectedBean;
	}

	public void setSelectedBean(String selectedBean) {
		this.selectedBean = selectedBean;
	}

	public List<PairKVElement> getBeans() {
		return beans;
	}

	public void setBeans(List<PairKVElement> beans) {
		this.beans = beans;
	}

	public String getSelectedDriver() {
		return selectedDriver;
	}

	public void setSelectedDriver(String selectedDriver) {
		this.selectedDriver = selectedDriver;
	}

	public List<OCRHistory> getDocs() {
		return docs;
	}

	public void setDocs(List<OCRHistory> docs) {
		this.docs = docs;
	}

	public OCRHistory getSelectedDoc() {
		return selectedDoc;
	}

	public void setSelectedDoc(OCRHistory selectedDoc) {
		this.selectedDoc = selectedDoc;
	}

	public List<UIFilterElement> getSearchControls() {
		return searchControls;
	}

	public void setSearchControls(List<UIFilterElement> searchControls) {
		this.searchControls = searchControls;
	}

	public boolean isSearchControlsEnabled() {
		return searchControlsEnabled;
	}

	public void setSearchControlsEnabled(boolean searchControlsEnabled) {
		this.searchControlsEnabled = searchControlsEnabled;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getSelectedPkgId() {
		return selectedPkgId;
	}

	public void setSelectedPkgId(String selectedPkgId) {
		this.selectedPkgId = selectedPkgId;
	}

	public List<PairKVElement> getPkgs() {
		return pkgs;
	}

	public void setPkgs(List<PairKVElement> pkgs) {
		this.pkgs = pkgs;
	}

	public List<String> getSelectedInstances() {
		return selectedInstances;
	}

	public void setSelectedInstances(List<String> selectedInstances) {
		this.selectedInstances = selectedInstances;
	}

	public List<PairKVElement> getPkinstances() {
		return pkinstances;
	}

	public void setPkinstances(List<PairKVElement> pkinstances) {
		this.pkinstances = pkinstances;
	}

	public String getIselectedPkgId() {
		return iselectedPkgId;
	}

	public void setIselectedPkgId(String iselectedPkgId) {
		this.iselectedPkgId = iselectedPkgId;
	}

	public List<String> getSelectedRows() {
		return selectedRows;
	}

	public void setSelectedRows(List<String> selectedRows) {
		this.selectedRows = selectedRows;
	}

	public List<PairKVElement> getPkgRows() {
		return pkgRows;
	}

	public void setPkgRows(List<PairKVElement> pkgRows) {
		this.pkgRows = pkgRows;
	}

	public List<UITheme> getThemes() {
		return themes;
	}

	public void setThemes(List<UITheme> themes) {
		this.themes = themes;
	}

	public String getColortheme() {
		return colortheme;
	}

	public void setColortheme(String colortheme) {
		this.colortheme = colortheme;
	}

	public String getStyletheme() {
		return styletheme;
	}

	public void setStyletheme(String styletheme) {
		this.styletheme = styletheme;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getSelectedRoleSecId() {
		return selectedRoleSecId;
	}

	public void setSelectedRoleSecId(String selectedRoleSecId) {
		this.selectedRoleSecId = selectedRoleSecId;
	}

	public List<PairKVElement> getRolesSec() {
		return rolesSec;
	}

	public void setRolesSec(List<PairKVElement> rolesSec) {
		this.rolesSec = rolesSec;
	}

	public boolean isAdminMode() {
		return adminMode;
	}

	public void setAdminMode(boolean adminMode) {
		this.adminMode = adminMode;
	}

	public int getSelectedOrgId() {
		return selectedOrgId;
	}

	public void setSelectedOrgId(int selectedOrgId) {
		this.selectedOrgId = selectedOrgId;
	}

	public List<GOrganization> getAllOrganizations() {
		return allOrganizations;
	}

	public void setAllOrganizations(List<GOrganization> allOrganizations) {
		this.allOrganizations = allOrganizations;
	}

	public List<CoreProfil> getProfils() {
		return profils;
	}

	public void setProfils(List<CoreProfil> profils) {
		this.profils = profils;
	}

	public int getSelectedProfilId() {
		return selectedProfilId;
	}

	public void setSelectedProfilId(int selectedProfilId) {
		this.selectedProfilId = selectedProfilId;
	}

	public List<CoreUser> getAllUsers() {
		return allUsers;
	}

	public void setAllUsers(List<CoreUser> allUsers) {
		this.allUsers = allUsers;
	}

	public List<CoreUser> getFilteredUsers() {
		return filteredUsers;
	}

	public void setFilteredUsers(List<CoreUser> filteredUsers) {
		this.filteredUsers = filteredUsers;
	}

	public DualListModel<String> getPicklistProfils() {
		return picklistProfils;
	}

	public void setPicklistProfils(DualListModel<String> picklistProfils) {
		this.picklistProfils = picklistProfils;
	}

	public DualListModel<String> getPicklistRoles() {
		return picklistRoles;
	}

	public void setPicklistRoles(DualListModel<String> picklistRoles) {
		this.picklistRoles = picklistRoles;
	}

	public List<CoreProfil> getListeProfils() {
		return listeProfils;
	}

	public void setListeProfils(List<CoreProfil> listeProfils) {
		this.listeProfils = listeProfils;
	}

	public List<CoreRole> getListeRoles() {
		return listeRoles;
	}

	public void setListeRoles(List<CoreRole> listeRoles) {
		this.listeRoles = listeRoles;
	}

	public boolean isUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(boolean updateUser) {
		this.updateUser = updateUser;
	}

	public List<CoreProfil> getTprofils() {
		return tprofils;
	}

	public void setTprofils(List<CoreProfil> tprofils) {
		this.tprofils = tprofils;
	}

	public CUserMailConfig getMailCfg() {
		return mailCfg;
	}

	public void setMailCfg(CUserMailConfig mailCfg) {
		this.mailCfg = mailCfg;
	}

	public List<CoreProfil> getSelectedProfils() {
		return selectedProfils;
	}

	public void setSelectedProfils(List<CoreProfil> selectedProfils) {
		this.selectedProfils = selectedProfils;
	}

	public List<CoreProfil> getAllProfils() {
		return allProfils;
	}

	public void setAllProfils(List<CoreProfil> allProfils) {
		this.allProfils = allProfils;
	}

	public List<CoreRole> getSelectedRoles() {
		return selectedRoles;
	}

	public void setSelectedRoles(List<CoreRole> selectedRoles) {
		this.selectedRoles = selectedRoles;
	}

	public List<CoreRole> getProfileRoles() {
		return profileRoles;
	}

	public void setProfileRoles(List<CoreRole> profileRoles) {
		this.profileRoles = profileRoles;
	}

	public List<Integer> getSelectedProfilsId() {
		return selectedProfilsId;
	}

	public void setSelectedProfilsId(List<Integer> selectedProfilsId) {
		this.selectedProfilsId = selectedProfilsId;
	}

	public List<Integer> getSelectedRolesId() {
		return selectedRolesId;
	}

	public void setSelectedRolesId(List<Integer> selectedRolesId) {
		this.selectedRolesId = selectedRolesId;
	}

	public CoreRole[] getSelectedRolesTable() {
		return selectedRolesTable;
	}

	public void setSelectedRolesTable(CoreRole[] selectedRolesTable) {
		this.selectedRolesTable = selectedRolesTable;
	}

	public int getSelectedRID() {
		return selectedRID;
	}

	public void setSelectedRID(int selectedRID) {
		this.selectedRID = selectedRID;
	}

	public int getSelectedPID() {
		return selectedPID;
	}

	public void setSelectedPID(int selectedPID) {
		this.selectedPID = selectedPID;
	}

	

}
