package fr.protogen.engine.control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpSession;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import fr.protogen.communication.client.EmailManager;
import fr.protogen.dataload.ContextPersistence;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.control.process.ProcessEvoltionListener;
import fr.protogen.engine.control.process.ProcessScreenListener;
import fr.protogen.engine.control.ui.LocalizationParameters;
import fr.protogen.engine.control.ui.UIMenu;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.DTOProcessSession;
import fr.protogen.engine.utils.FormContext;
import fr.protogen.engine.utils.InstanceHistory;
import fr.protogen.engine.utils.ListKV;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.engine.utils.ScreenDataHistory;
import fr.protogen.engine.utils.UIFilterElement;
import fr.protogen.event.geb.EventDataAccess;
import fr.protogen.event.geb.GeneriumEventBus;
import fr.protogen.event.geb.EventModel.GEventInstance;
import fr.protogen.event.geb.EventModel.PEAMail;
import fr.protogen.event.geb.EventModel.PEASms;
import fr.protogen.event.geb.EventModel.PEAWindow;
import fr.protogen.event.geb.EventModel.PostEventAction;
import fr.protogen.event.geb.communication.ClientMail;
import fr.protogen.event.geb.communication.SmsClient;
import fr.protogen.masterdata.DAO.AlertDataAccess;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.LocalizationEngine;
import fr.protogen.masterdata.model.AlertInstance;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CSchedulableEntity;
import fr.protogen.masterdata.model.CScheduleEvent;
import fr.protogen.masterdata.model.CUIParameter;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.ResearchableResource;
import fr.protogen.masterdata.model.ResearchableType;
import fr.protogen.masterdata.model.SApplication;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.SMenuitem;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SProcess;
import fr.protogen.masterdata.model.SRubrique;
import fr.protogen.masterdata.model.SStep;
import fr.protogen.masterdata.model.WFData;
import fr.protogen.masterdata.model.WFDecisionData;
import fr.protogen.masterdata.model.WorkflowAnswer;
import fr.protogen.masterdata.model.WorkflowDecision;
import fr.protogen.masterdata.model.WorkflowExecution;
import fr.protogen.masterdata.model.WorkflowNode;
import fr.protogen.masterdata.model.WorkflowScreenNode;
import fr.protogen.masterdata.services.WorkflowEngine;

/**
 * La fonction principale du FrontController est l'initialisation des paramètres de l'application et de l'utilisateur
 * Il est aussi indispensable pour la centralisation de certaines fonctions telles que les procédures et les Workflows
 * @author 
 *
 */
@ManagedBean
@SessionScoped
public class FrontController implements ProcessEvoltionListener {

	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Paramètres d'application
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private String userLastName;
	private String userRole;
	private CoreUser currentUser = new CoreUser();
	private LocalizationParameters localization;
	private List<SMenuitem> metamenu;
	private String mainWindowTitle;
	private String logoPath = "";
	private Map<String, List<String>> menu;
	private List<UIMenu> supermenu;
	private ApplicationCache cache;
	private int selectedSuperMenu;
	private List<PairKVElement> currentMenu;
	private int supermenuSize;
	private int submenuSize;
	private CWindow window;
	private List<SProcess> processes;
	private List<SProcedure> procedures = new ArrayList<SProcedure>();
	private SProcedure procedure = new SProcedure();
	private SProcess process;
	private List<String> procLabels;
	private String selectedProcess;
	private String selectedResearchable;
	private String processTitle;
	private String processDescription;
	private SAtom atom;
	private List<SAtom> currentAtoms = new ArrayList<SAtom>();
	private List<SAtom> nextAtoms = new ArrayList<SAtom>();
	private List<SAtom> previousAtoms = new ArrayList<SAtom>();
	private boolean inProcess = false;
	private String filteredMenuItem;
	private SApplication papp;

	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Libellés de navigation rapide entre les écrans
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private String previousStepLabel = "";
	private boolean previousFound = false;
	private String currentStepLabel = "";
	private boolean currentFound = false;
	private String nextStepLabel = "";
	private boolean nextFound = false;

	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Gestion des sessions interrompues
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private boolean interruptedProcess = false;
	private String inprocLabel = "";
	private String inprocAtom = "";
	private DTOProcessSession ps = new DTOProcessSession();
	private boolean pause = true;
	private FormContext formContext = new FormContext();
	private boolean formContextAvailable;
	private CWindow lastScreen = new CWindow();
	

	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Menu d'application
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private boolean showWholeMenu;
	private List<CAttribute> fcas;
	private String navigableClass = "";
	private int idMenu;
	private boolean linksAvailable;
	private CWindow firstLink;
	private List<CDocumentbutton> boundButtons = new ArrayList<CDocumentbutton>();
	private String formview;
	private String accIndex = "null";
	private List<SRubrique> angramainyu;
	private SRubrique currentRubrique = new SRubrique();
	private CWindow lastWindow = null;
	private boolean lastWindowAvailable = false;
	private boolean singleLink;

	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Thèmes
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private String themeLib = "cupertino";
	private String masterPage = "/protogen-green.xhtml";
	private String activeRubrique = "0";
	private String themeVar = "";
	private String uniformPageBodyStyle = "background-image:url(img/textures/fabric.png);background-repeat:no-repeat;background-attachment:fixed;background-color:white;";
	private String majorColor = "#2E6E9E";
	private String logoPercent = "100%";
	private String redemptionWidth = "66%";
	private boolean showRightPanel = true;
	private String colortheme;
	private String styletheme;

	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Gestion des alertes
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private boolean alertsAvailable = false;
	private List<AlertInstance> userAlerts = new ArrayList<AlertInstance>();
	private AlertInstance currentAlert;

	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Workflows
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private List<WorkflowExecution> workflowInstances = new ArrayList<WorkflowExecution>();
	private List<WorkflowNode> userNodes = new ArrayList<WorkflowNode>();
	private WorkflowNode currentNode;
	private WorkflowDecision decision;
	private boolean decisionVisible = false;
	private WorkflowAnswer answer;
	private boolean answerVisible;
	private String commentaire;
	private String answerValue;
	private String wfLinkLabel;
	private boolean wfLinkVisible;
	
	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Recherche avancée
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private List<ResearchableResource> sresources;

	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Procédures
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private String procedureTitle = "";
	private String procedureDescription = "";
	private List<UIFilterElement> filters = new ArrayList<UIFilterElement>();
	private List<ScreenDataHistory> historique;
	private List<Integer> historiqueId;
	private List<InstanceHistory> histoShow;
	private List<PairKVElement> titles;
	private List<String> subviewValues;
	private boolean showSubs = false;
	private boolean keepAlive = true;

	
	/*$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*
	 * 	Logo et photo
	 *$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	private String logo;
	private String profilPhoto;
	private String organizationName;
	
	//	Evénements
	private List<GEventInstance> events = new ArrayList<GEventInstance>();
	
	//	Mode superadmin
	private boolean adminMode;
	
	public FrontController() {

	}

	@PostConstruct
	public void intialize() {

		/*
		 * Initialisation et vérification de l'authentification
		 */
		boolean notinsession = (!FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().containsKey("USER_KEY") || FacesContext
				.getCurrentInstance().getExternalContext().getSessionMap()
				.get("USER_KEY") == null);

		if (notinsession) {

			return;
		}

		/*
		 * Initialisation des paramètres de l'utilisateur et de l'application
		 */
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");

		cache = ApplicationRepository.getInstance().getCache(skey);
		userLastName = cache.getUser().getLastName();
		currentUser = cache.getUser();
		colortheme = cache.getUser().getThemeColor();
		styletheme = cache.getUser().getThemeStyle();
		userRole = currentUser.getCoreRole().getRole();
		papp = cache.getWindow().getSApplication();

		adminMode = cache.isSuperAdmin();
		localization = new LocalizationParameters(currentUser);
		if(cache.getOrganization()!= null && cache.getOrganization()!=null && cache.getOrganization().getName()!=null)
			organizationName = cache.getOrganization().getName();
		
		/*
		 * Vérification des alertes
		 */
		AlertDataAccess ada = new AlertDataAccess();
		alertsAvailable = ada.checkAlerts(currentUser.getCoreRole().getId());
		if (alertsAvailable) {
			userAlerts = ada.getMyAlerts(currentUser.getCoreRole().getId());
			FacesContext.getCurrentInstance().addMessage(
					"",
					new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Vous avez des alertes en attente de consultation",
							""));
		}

		/*
		 * Menu
		 */
		formview = "protogen-formline";	//	Cette instruction concerne l'utilisation de deux modes de formulaires, actuellement ce n'est plus utilisé
		menu = new HashMap<String, List<String>>();

		// Initialize window
		mainWindowTitle = cache.getWindow().getSApplication().getProjectName()
				+ " (" + cache.getWindow().getSApplication().getVersion()
				+ ") - " + cache.getWindow().getApplicationTitle();

		String theme = cache.getUser().getUserTheme();
		String colorTheme = cache.getUser().getThemeColor();
		if (theme.equals("THEME:BLEU")) {
			themeLib = "cupertino";
			if(colorTheme.equals("css/admin.css"))
				themeLib = "delta";
			if(colorTheme.equals("css/admin_green.css"))
				themeLib = "delta";
			if(colorTheme.equals("css/admin_red.css"))
				themeLib = "delta";
				
			masterPage = "/protogen-green.xhtml";
			themeVar = "";
			uniformPageBodyStyle = "background-image:url(img/bgeffect.png);background-repeat:no-repeat;background-attachment:fixed;background-color:white;";
			majorColor = "#2E6E9E";
			logoPercent = "100%";
		} 
		if (theme.equals("THEME:ROUGE")) {
			themeLib = "cupertino";
			if(colorTheme.equals("css/layout.css"))
				themeLib = "delta";
			if(colorTheme.equals("css/layout_green.css"))
				themeLib = "delta";
			if(colorTheme.equals("css/layout_red.css"))
				themeLib = "delta";
			
			masterPage = "/redemption_rouge.xhtml";
			themeVar = "-red";
			uniformPageBodyStyle = "background:url(images/bg.gif) repeat-x left top #d4d3d3; font-family:\"Trebuchet MS\", Arial, Helvetica, sans-serif; font-size: 13px; color: #333;";
			majorColor = "#FFFFFF";
			logoPercent = "22%";
		}
		if (theme.equals("THEME:DEVELOPR")) {
			themeLib = "cupertino";
			masterPage = "/redemption.xhtml";
			themeVar = "-red";
			uniformPageBodyStyle = "background:url(images/bg.gif) repeat-x left top #d4d3d3; font-family:\"Trebuchet MS\", Arial, Helvetica, sans-serif; font-size: 13px; color: #333;";
			majorColor = "#FFFFFF";
			logoPercent = "22%";
		}
	
		// Récupérer les procédures
		procedures = cache.getProcedures();
		procLabels = new ArrayList<String>();
		for (SProcedure p : procedures) {
			procLabels.add(p.getTitle());
		}

		// Mettre en place le menu
		angramainyu = cache.getMenu();
		currentRubrique = angramainyu.get(0);

		sresources = new ArrayList<ResearchableResource>();
		sresources.add(new ResearchableResource(1, "Alertes",
				ResearchableType.FONCTION_SYSTEME));
		sresources.add(new ResearchableResource(2, "Agenda",
				ResearchableType.FONCTION_SYSTEME));
		sresources.add(new ResearchableResource(3, "Administration",
				ResearchableType.FONCTION_SYSTEME));
		sresources.add(new ResearchableResource(4, "Chargement des données",
				ResearchableType.FONCTION_SYSTEME));
		sresources.add(new ResearchableResource(5, "Archive numérique",
				ResearchableType.FONCTION_SYSTEME));
		sresources.add(new ResearchableResource(6, "Profils et rôles",
				ResearchableType.FONCTION_SYSTEME));
		sresources.add(new ResearchableResource(7, "Utilisateurs",
				ResearchableType.FONCTION_SYSTEME));
		
		//	Gestion de l'internationalisation du menu
		LocalizationEngine translator = new LocalizationEngine();
		for (SRubrique r : angramainyu) {
			r.setTitre(translator.rubriqueTranslate(r.getTitre(), r.getId(), currentUser.getLanguage()));
			for (SMenuitem m : r.getItems()) {
				m.setTitle(translator.menuTranslate(m.getTitle(), m.getId(), currentUser.getLanguage()));
				if(m.getSubs() != null && m.getSubs().size()>0){
					for(SMenuitem i : m.getSubs()){
						i.setTitle(translator.menuTranslate(i.getTitle(), i.getId(), currentUser.getLanguage()));
					}
				}
			}
		}
		
		
		for (SRubrique r : angramainyu) {
			
			if (r.isOneColumne()) {
				for (SMenuitem m : r.getItems()) {
					ResearchableResource ress = new ResearchableResource();
					ress.setId(m.getId());
					ress.setLabel(m.getTitle());
					ress.setType(ResearchableType.ECRAN);
					sresources.add(ress);
				}
			} else {
				for (SMenuitem men : r.getItems()) {
					if(!men.isParent() || men.getSubs() == null){
						ResearchableResource ress = new ResearchableResource();
						ress.setId(men.getId());
						ress.setLabel(men.getTitle());
						ress.setType(ResearchableType.ECRAN);
						sresources.add(ress);
						continue;
					}
					for (SMenuitem m : men.getSubs()) {
						ResearchableResource ress = new ResearchableResource();
						ress.setId(m.getId());
						ress.setLabel(m.getTitle());
						ress.setType(ResearchableType.ECRAN);
						sresources.add(ress);
					}
				}
			}
		}

		// Alimentation de la recherche
		ApplicationLoader adal = new ApplicationLoader();
		List<CWindow> forms = adal.loadFormWindows(cache.getAppKey());
		for (CWindow w : forms) {
			ResearchableResource ress = new ResearchableResource();
			ress.setId(w.getId());
			ress.setForm(true);
			ress.setLabel(w.getTitle());
			ress.setType(ResearchableType.FORM);
			sresources.add(ress);
		}
		
		metamenu = new ArrayList<SMenuitem>();
		supermenu = new ArrayList<UIMenu>();

		for (SMenuitem item : metamenu) {
			if (item.isParent()) {
				UIMenu smenu = new UIMenu();
				smenu.setItemId(item.getId());
				smenu.setSupermenu(item.getTitle());
				smenu.setSubmenus(new ArrayList<PairKVElement>());
				List<String> submenu = new ArrayList<String>();
				for (SMenuitem subitem : metamenu) {
					if (!subitem.isParent()
							&& subitem.getIdParent() == item.getId()) {
						submenu.add(subitem.getTitle());
						smenu.getSubmenus().add(
								new PairKVElement("" + subitem.getId(), subitem
										.getTitle()));
					}
				}
				menu.put(item.getTitle(), submenu);
				supermenu.add(smenu);
			}
		}

		/*
		 * Purger les menus vides et les rubriques vides
		 */
		List<UIMenu> toRemove = new ArrayList<UIMenu>();
		for (UIMenu m : supermenu) {
			if (m.getSubmenus() == null || m.getSubmenus().size() == 0)
				toRemove.add(m);
		}

		supermenu.removeAll(toRemove);

		supermenuSize = 700 / (supermenu.size() + 1);
		
		// Logo
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		String ext = cache.getUser().getCoreRole().getFileName();
		if(ext != null && ext.length() > 0){
			ext = ext.substring(ext.lastIndexOf('.'));
			logoPath = ec.getRealPath("") + "/logo/"+cache.getUser().getCoreRole().getLogoResKey()+ext;
			File logo = new File(logoPath);
	
			if (!logo.exists())
				logoPath = "/logo/logo.png";
			else
				logoPath = "/logo/"+cache.getUser().getCoreRole().getLogoResKey()+ext;
		}else
			logoPath = "/logo/logo.png";
		
		// Restauration d'une session interompue
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ps = engine.getProcessSession(cache.getUser());
		interruptedProcess = !ps.isVoidFlag();
		if (interruptedProcess) {
			for (SProcedure p : procedures)
				if (p.getId() == ps.getProcess().getId()) {
					ps.setProcess(p);
					break;
				}

			for (SStep stp : ps.getProcess().getEtapes()) {
				for (SAtom a : stp.getActions())
					if (a.getId() == ps.getAtom().getId()) {
						ps.setAtom(a);
						break;
					}
			}

			inprocLabel = ps.getProcess().getTitle();
			inprocAtom = ps.getAtom().getTitle();
		}

		showWholeMenu = true;
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("FRONT_CTRL", this);

		// Avatar de l'utilisateur
		String pathname = FacesContext.getCurrentInstance().getExternalContext().
				getRealPath("")+"/photos/"+cache.getUser().getPhoto();
		File file = new File(pathname);
		if(!file.exists()){
			profilPhoto = "/img/user.png";
		} else {
			profilPhoto = "/photos/"+cache.getUser().getPhoto();
		}
		
		// Gestion des workflows
		CoreRole role = cache.getUser().getCoreRole();
		WorkflowEngine wfe = new WorkflowEngine();
		workflowInstances = wfe.getPendingInstances(role);
		userNodes = new ArrayList<WorkflowNode>();
		for (WorkflowExecution e : workflowInstances) {
			alertsAvailable = true;
			userNodes.add(e.getCurrentNode());
			String title = "Le processus " + e.getDefinition().getTitle()
					+ " requiert votre participation";
			String description = "Vous êtes invités à assurer l'activité suivante : "
					+ e.getCurrentNode().getLabel();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, title,
							description));
		}
		
		//	Bus d'événements
		GeneriumEventBus bus = GeneriumEventBus.getInstance();
		bus.energize(currentUser);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.EVENT_BUS, bus);
		events = bus.fetch(currentUser);
		if(events != null && events.size()>0){
			FacesContext.getCurrentInstance().
				addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Evénements", "Vous avez des événements en attente de consultation"));
		}
		
		//	Initiliser le calendrier
		prepareSchedule();
		
		//	Vérifier si un formulaire a été quitté en plein saisie
		ContextPersistence pers = new ContextPersistence();
		formContext = pers.loadControls(cache.getUser());
		formContextAvailable = formContext != null;
		if(!formContextAvailable)
			return;
		ApplicationLoader dal = new ApplicationLoader();
		formContext.getForm().setAppKey(cache.getAppKey());
		setLastScreen(dal.loadWindowFromLink(formContext.getForm()));
		
	}
	
	/**
	 * Changer le thème de l'application
	 */
	public void alterTheme() {
		String theme = cache.getUser().getUserTheme();
		if (theme.equals("THEME:BLEU")) {
			themeLib = "redmond";
			masterPage = "/protogen-green.xhtml";
			themeVar = "";
			uniformPageBodyStyle = "background-image:url(img/bgeffect.png);background-repeat:no-repeat;background-attachment:fixed;background-color:white;";
			majorColor = "#2E6E9E";
			logoPercent = "100%";
		} 
		if (theme.equals("THEME:ROUGE")) {
			themeLib = "cupertino";
			masterPage = "/redemption_rouge.xhtml";
			themeVar = "-red";
			uniformPageBodyStyle = "background:url(images/bg.gif) repeat-x left top #d4d3d3; font-family:\"Trebuchet MS\", Arial, Helvetica, sans-serif; font-size: 13px; color: #333;";
			majorColor = "#FFFFFF";
			logoPercent = "22%";
		}
		if (theme.equals("THEME:DEVELOPR")) {
			themeLib = "cupertino";
			masterPage = "/redemption.xhtml";
			themeVar = "-red";
			uniformPageBodyStyle = "background:url(images/bg.gif) repeat-x left top #d4d3d3; font-family:\"Trebuchet MS\", Arial, Helvetica, sans-serif; font-size: 13px; color: #333;";
			majorColor = "#FFFFFF";
			logoPercent = "22%";
		}
	}

	/*
	 * Clôturer une alerte
	 */
	public void closeAlert() {
		
		int id = 0;
		String sid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ALRTID");
		
		id = Integer.parseInt(sid);
		
		for(AlertInstance a : userAlerts){
			if(a.getId() == id){
				currentAlert = a;
			}
		}
		
		if(currentAlert == null)
			return;
		
		AlertDataAccess ada = new AlertDataAccess();
		ada.closeAlert(currentAlert);

		userAlerts.remove(currentAlert);
		if (userAlerts.size() == 0)
			alertsAvailable = false;

	}

	/**
	 * Sélection de la rubrique courante
	 * @return rubrique en cours
	 */
	public String updateRubrique() {
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		int id = Integer.parseInt(params.get("crubrique"));
		currentRubrique = new SRubrique();
		for (SRubrique r : angramainyu)
			if (r.getId() == id) {
				setActiveRubrique(angramainyu.indexOf(r) + "");
				currentRubrique = r;
				break;
			}
		return "";
	}

	/**
	 * Vérifier la présence d'une session utilisateur
	 * @param event
	 */
	public void checkForSession(ComponentSystemEvent event) {

		boolean notinsession = (!FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().containsKey("USER_KEY") || FacesContext
				.getCurrentInstance().getExternalContext().getSessionMap()
				.get("USER_KEY") == null);

		if (notinsession) {
			HttpSession session = (HttpSession) FacesContext
					.getCurrentInstance().getExternalContext().getSession(true);
			session.invalidate();

			ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) FacesContext
					.getCurrentInstance().getApplication()
					.getNavigationHandler();

			nav.performNavigation("login");
		}

	}

	/**
	 * Initialisation d'un menu
	 */
	private void initMenu() {
			for (UIMenu item : supermenu) {
			if (selectedSuperMenu == item.getItemId()) {
				currentMenu = item.getSubmenus();
				submenuSize = 700 / currentMenu.size();
			}
		}
	}

	/**
	 * Restaurer le menu en cours
	 * @return
	 */
	public String restoreMenu() {
		accIndex = "null";
		showWholeMenu = true;
		return "";
	}

	/**
	 * Changer le menu sélectionné
	 * @return
	 */
	public String menuChange() {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String id = params.get("selected");
		selectedSuperMenu = Integer.parseInt(id);

		initMenu();
		return "";
	}

	/*
	 * Renvoyer les clés de recherche
	 */
	public List<String> autocomplete(String prefix) {
		return procLabels;
	}

	/**
	 * Activation d'un écran à partir d'un menu ou un lien
	 * @return Nouvel écran
	 */
	public String updateScreenThroughLink() {

		CWindow surrogate = window;

		/*
		 * Les paramètres permettent de détecter le nouvel écran sollicité et la séquence d'écrans qui interpelle celui ci
		 */
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String id = params.get("screenseq");
		String stn = params.get("tonew");
		if(id == null || id.equals("null"))
			return "protogen";
		int idMenu = Integer.parseInt(id);
		boolean toNew = stn != null && stn.equals("1");

		ApplicationLoader dao = new ApplicationLoader();

		CWindow next = new CWindow();
		if (window.getLinks() != null && window.getLinks().size() > 0) {
			for (CWindow w : window.getLinks())
				if (w.getId() == idMenu) {
					next = w;
					break;
				}

		}

		if (next.getId() == 0) {
			next.setId(idMenu);
			next.setAppKey(window.getAppKey());
		}

		/*
		 * Chargement des liens de l'écran
		 */
		boundButtons = new ArrayList<CDocumentbutton>();
		window = dao.loadWindowFromLink(next);
		window = dao.loadFullWindow(window);
		window = dao.loadWindowWithActions(window);
		linksAvailable = window.getLinks() != null
				&& window.getLinks().size() > 0;
		if (linksAvailable) {
			firstLink = window.getLinks().get(0);
			lastWindow = window.getLinks().get(window.getLinks().size() - 1);
			lastWindowAvailable = false;
		} else if (surrogate != null && surrogate.getId() > 0) {
			window.setLinks(new ArrayList<CWindow>());
			window.getLinks().add(surrogate);
			lastWindow = surrogate;
			linksAvailable = true;
			lastWindowAvailable = true;
		}

		if (linksAvailable) {
			if (lastWindowAvailable && lastWindow.getId() == window.getId()) {
				window.getLinks().remove(lastWindow);
				if (window.getLinks().size() > 0) {
					lastWindow = window.getLinks().get(
							window.getLinks().size() - 1);
					lastWindowAvailable = true;
				} else {
					linksAvailable = false;
					lastWindowAvailable = false;
				}
			}

		}

		if (toNew) {
			if (window.getLinks() != null
					&& !window.getLinks().contains(surrogate))
				window.getLinks().add(surrogate);

			lastWindow = surrogate;
			lastWindowAvailable = true;
		}

		singleLink = (window.getLinks() != null && window.getLinks().size() == 1);

		fcas = new ArrayList<CAttribute>();
		for (CAttribute a : window.getCAttributes())
			if (a.getCAttributetype().getId() == 6)
				fcas.add(a);

		showWholeMenu = false;

		if (window.getCWindowtype().getId() == 1)
			return "protogen-listview";
		else if (window.getCWindowtype().getId() == 2) {
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("action", "insert");
			return formview;
		}

		return "";
	}

	/**
	 * Methode de navigation entre écrans
	 * @return Nouvel écran
	 */
	public String updateScreen() {

		CWindow surrogate = window;
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String id = params.get("screenseq");
		idMenu = Integer.parseInt(id);
		
		navigableClass = "current navigable-current";

		ApplicationLoader dao = new ApplicationLoader();

		SMenuitem item = new SMenuitem();
		item.setId(idMenu);
		item.setAppKey(cache.getAppKey());
		
		boundButtons = new ArrayList<CDocumentbutton>();
		window = dao.loadWindowFromMenu(item);
		if(window.getId()==0)
			return "";
		window = dao.loadFullWindow(window);
		window = dao.loadWindowWithActions(window);
		linksAvailable = window.getLinks() != null
				&& window.getLinks().size() > 0;
		if (linksAvailable) {
			firstLink = window.getLinks().get(0);
			lastWindow = window.getLinks().get(window.getLinks().size() - 1);
			lastWindowAvailable = false;
		} else if (surrogate != null && surrogate.getId() > 0) {
			window.setLinks(new ArrayList<CWindow>());
			window.getLinks().add(surrogate);
			lastWindow = surrogate;
			lastWindowAvailable = true;
			linksAvailable = true;
		}

		if (linksAvailable) {
			if (lastWindowAvailable && lastWindow.getId() == window.getId()) {
				window.getLinks().remove(lastWindow);
				if (window.getLinks().size() > 0) {
					lastWindow = window.getLinks().get(
							window.getLinks().size() - 1);
					lastWindowAvailable = true;
				} else {
					linksAvailable = false;
					lastWindowAvailable = false;
				}
			}
		}
		singleLink = (window.getLinks() != null && window.getLinks().size() == 1);

		fcas = new ArrayList<CAttribute>();
		for (CAttribute a : window.getCAttributes())
			if (a.getCAttributetype().getId() == 6)
				fcas.add(a);

		showWholeMenu = false;

		if (window.getCWindowtype().getId() == 1)
			return "protogen-listview";
		else if (window.getCWindowtype().getId() == 2) {
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("action", "insert");
			return formview;
		}

		return "";
	}

	/**
	 * Methode de navigation entre écrans
	 * @param Identifiant du menu
	 * @return nouvel écran
	 */
	public String updateScreen(int theId) {

		CWindow surrogate = window;

		int idMenu = theId;

		ApplicationLoader dao = new ApplicationLoader();

		SMenuitem item = new SMenuitem();
		item.setId(idMenu);
		item.setAppKey(cache.getAppKey());

		boundButtons = new ArrayList<CDocumentbutton>();
		window = dao.loadWindowFromMenu(item);
		window = dao.loadFullWindow(window);
		window = dao.loadWindowWithActions(window);
		linksAvailable = window.getLinks() != null
				&& window.getLinks().size() > 0;
		if (linksAvailable) {
			firstLink = window.getLinks().get(0);
			lastWindow = window.getLinks().get(window.getLinks().size() - 1);
			lastWindowAvailable = false;
		} else if (surrogate != null && surrogate.getId() > 0) {
			window.setLinks(new ArrayList<CWindow>());
			window.getLinks().add(surrogate);
			lastWindow = surrogate;
			lastWindowAvailable = true;
			linksAvailable = true;
		}

		if (linksAvailable) {
			if (lastWindowAvailable && lastWindow.getId() == window.getId()) {
				window.getLinks().remove(lastWindow);
				if (window.getLinks().size() > 0) {
					lastWindow = window.getLinks().get(
							window.getLinks().size() - 1);
					lastWindowAvailable = true;
				} else {
					lastWindowAvailable = false;
					linksAvailable = false;
				}
			}
		}

		singleLink = (window.getLinks() != null && window.getLinks().size() == 1);
		fcas = new ArrayList<CAttribute>();
		for (CAttribute a : window.getCAttributes())
			if (a.getCAttributetype().getId() == 6)
				fcas.add(a);

		// FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("inProcess",
		// "false");
		showWholeMenu = false;

		if (window.getCWindowtype().getId() == 1)
			return "protogen-listview";
		else if (window.getCWindowtype().getId() == 2) {
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("action", "insert");
			return formview;
		}

		return "";
	}

	/**
	 * Charger la structure d'une fenêtre
	 * Cette méthode utilise l'object Window partagé
	 * @return le nouvel écran
	 */
	public String loadWindow(){
		ApplicationLoader dao = new ApplicationLoader();
		window = dao.loadFullWindow(window);
		window = dao.loadWindowWithActions(window);
		linksAvailable = window.getLinks() != null
				&& window.getLinks().size() > 0;
		if (linksAvailable) {
			firstLink = window.getLinks().get(0);
			lastWindow = window.getLinks().get(window.getLinks().size() - 1);
			lastWindowAvailable = false;
		} 

		
		singleLink = (window.getLinks() != null && window.getLinks().size() == 1);

		fcas = new ArrayList<CAttribute>();
		for (CAttribute a : window.getCAttributes())
			if (a.getCAttributetype().getId() == 6)
				fcas.add(a);

		showWholeMenu = true;

		if (window.getCWindowtype().getId() == 1)
			return "protogen-listview";
		else if (window.getCWindowtype().getId() == 2) {
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("action", "insert");
			return formview;
		}

		return "";
	}
	
	/**
	 * Cette méthode est utilisée seulement dans le cas des thèmes utilisant un menu accordéon avec des recherches par rubrique
	 */
	public void gotoMenu() {
		int idMenu = Integer.parseInt(filteredMenuItem);
		String page = "";

		ApplicationLoader dao = new ApplicationLoader();

		SMenuitem item = new SMenuitem();
		item.setId(idMenu);
		item.setAppKey(cache.getAppKey());
		showWholeMenu = false;
		window = dao.loadWindowFromMenu(item);
		boundButtons = new ArrayList<CDocumentbutton>();
		window = dao.loadWindowFromMenu(item);
		window = dao.loadFullWindow(window);
		window = dao.loadWindowWithActions(window);
		linksAvailable = window.getLinks() != null
				&& window.getLinks().size() > 0;
		if (linksAvailable)
			firstLink = window.getLinks().get(0);

		singleLink = (window.getLinks() != null && window.getLinks().size() == 1);
		fcas = new ArrayList<CAttribute>();
		for (CAttribute a : window.getCAttributes())
			if (a.getCAttributetype().getId() == 6)
				fcas.add(a);

		if (window.getCWindowtype().getId() == 1)
			page = "protogen-listview.xhtml";
		else if (window.getCWindowtype().getId() == 2) {
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("action", "insert");
			page = formview + ".xhtml";
		}

		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(page);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Détruire la session et quitter l'application
	 * @return page de login
	 */
	public String logout() {

		boolean notinsession = (!FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().containsKey("USER_KEY") || FacesContext
				.getCurrentInstance().getExternalContext().getSessionMap()
				.get("USER_KEY") == null);

		if (notinsession) {
			HttpSession session = (HttpSession) FacesContext
					.getCurrentInstance().getExternalContext().getSession(true);
			session.invalidate();

			return "login.xhtml";
		}

		if (inProcess) {
			//	Sauvegarder l'état de la procédure
			ps = new DTOProcessSession();
			ps.setProcess(procedure);
			ps.setAtom(atom);
			ps.setUser(cache.getUser());

			ProtogenDataEngine engine = new ProtogenDataEngine();
			engine.saveProcessSession(ps);
		}

		try {
			ProcessScreenListener.getInstance().kill(cache.getUser());

		} catch (Exception exc) {
			exc.printStackTrace();
		}

		try {
			cache.logout(cache.getUser());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			String skey = (String) FacesContext.getCurrentInstance()
					.getExternalContext().getSessionMap().get("USER_KEY");
			ApplicationRepository.getInstance().terminate(skey);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		previousStepLabel = "";
		previousFound = false;
		currentStepLabel = "";
		currentFound = false;
		nextStepLabel = "";
		nextFound = false;

		cache = null;
		menu = null;

		// Réinitialisation
		mainWindowTitle = "";
		procedures = null;
		procLabels = null;
		metamenu = null;
		supermenu = null;
		supermenuSize = 0;
		selectedSuperMenu = 0;
		logoPath = "";

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(true);
		session.invalidate();

		return "login.xhtml";
	}

	
	/**
	 * Sélection de l'écran suivant de la procédure
	 */
	@SuppressWarnings("unchecked")
	public String next() {

		// Vérifier les paramètres de la procédure
		String gotoPage = "protogen";
		if (ProcessScreenListener.getInstance()
				.getCurrentScreen(cache.getUser()).getType().getId() == 2) {
			List<CUIParameter> parameters = (List<CUIParameter>) FacesContext
					.getCurrentInstance().getExternalContext().getSessionMap()
					.get(ProtogenConstants.PARAMETERS);
			gotoPage = ProcessScreenListener.getInstance().getNextScreen(
					cache.getUser(), parameters, null);
		} else {
			gotoPage = ProcessScreenListener.getInstance().getNextScreen(
					cache.getUser(), null, null);
		}

		//	Atom référence l'action en cours
		showWholeMenu = false;
		atom.setCurrent(false);
		atom.setDone(true);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("inProcess", "true");

		atom = ProcessScreenListener.getInstance().getCurrentScreen(
				cache.getUser());
		atom.setCurrent(true);
		currentAtoms = atom.getStep().getActions();
		nextAtoms = ProcessScreenListener.getInstance().getNextAtoms(
				cache.getUser());
		previousAtoms = ProcessScreenListener.getInstance().getPreviousAtoms(
				cache.getUser());
		boolean endOfStep = false;
		if (!currentStepLabel.equals(atom.getStep().getTitle())) {
			previousStepLabel = currentStepLabel;
			currentStepLabel = atom.getStep().getTitle();
			endOfStep = true;
		}

		if (previousStepLabel.length() > 0)
			previousFound = true;
		else
			previousFound = false;

		boundButtons = new ArrayList<CDocumentbutton>();
		currentFound = true;
		nextStepLabel = ProcessScreenListener.getInstance().getNextScreenLabel(
				cache.getUser());
		if (nextStepLabel.length() > 0)
			nextFound = true;
		else
			nextFound = false;
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("adesc", atom);
		
		// Gestion des écrans de synthèse
		if (gotoPage.equals("protogen-synthesis")) {
			nextFound = false;
			previousFound = false;
			currentFound = true;
			showWholeMenu = true;

			nextStepLabel = "";
			previousStepLabel = "";
			currentStepLabel = "Fin de la procédure";

			Map<String, Object> sm = FacesContext.getCurrentInstance()
					.getExternalContext().getSessionMap();
			sm.put(ProtogenConstants.GOTO_PAGE, gotoPage);
			sm.put(ProtogenConstants.HISTO_TO_RETRIEVE, new Boolean(false));

			histoShow = new ArrayList<InstanceHistory>();
			if (historiqueId == null || historique == null)
				return "protogen-step-synthesis";

			for (Integer I : historiqueId) {
				boolean continueFlag = false;
				for (InstanceHistory inh : histoShow) {
					if (inh.getDbID() == I.intValue()) {
						continueFlag = true;
						break;
					}

				}

				if (continueFlag)
					continue;

				InstanceHistory ih = new InstanceHistory();
				ih.setDbID(I.intValue());

				for (PairKVElement e : procedure.getListInstances()) {
					if (Integer.parseInt(e.getKey()) == I.intValue()) {
						ih.setTitle(e.getValue());
						break;
					}
				}
				ih.setHistory(new ArrayList<ScreenDataHistory>());
				for (ScreenDataHistory history : historique) {
					ScreenDataHistory h = new ScreenDataHistory();
					h.setIdIndex(history.getIdIndex());
					h.setTitles(history.getTitles());
					h.setWindowTitle(history.getWindowTitle());
					h.setData(new ArrayList<ListKV>());
					for (ListKV datum : history.getData()) {
						int idIndex = h.getIdIndex();
						int idDB = 0;
						if (idIndex == 0)
							idDB = datum.getDbID();
						else {

							for (PairKVElement e : procedure.getListInstances()) {
								String compareValue = datum.getValue().get(
										idIndex - 1);
								if (e.getValue().startsWith(compareValue)) {
									idDB = Integer.parseInt(e.getKey());
									break;
								}
							}
						}

						if (idDB == ih.getDbID()) {
							h.getData().add(datum);
							boolean found = false;
							for (ScreenDataHistory sdh : ih.getHistory())
								if (sdh.getWindowTitle().equals(
										h.getWindowTitle())) {
									found = true;
									break;
								}
							if (!found)
								ih.getHistory().add(h);
						}
					}

				}
				histoShow.add(ih);
			}

		}

		//	Sélection de l'écran à charger en fonction de l'action en cours
		if (atom.getType().getId() == 1) {
			window = atom.getWindow();
			showWholeMenu = false;
			boundButtons = new ArrayList<CDocumentbutton>();
			ApplicationLoader dao = new ApplicationLoader();

			window = dao.loadFullWindow(window);
			window = dao.loadWindowWithActions(window);
			window.setSynthesis(atom.isSynthesis());
			linksAvailable = window.getLinks() != null
					&& window.getLinks().size() > 0;
			if (linksAvailable)
				firstLink = window.getLinks().get(0);

			fcas = new ArrayList<CAttribute>();
			for (CAttribute a : window.getCAttributes())
				if (a.getCAttributetype().getId() == 6)
					fcas.add(a);
			if (gotoPage.equals(formview)) {
				FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().put("action", "insert");
				return formview;
			}
		}

		//	Gestion de la fin d'une étape
		if (endOfStep) {
			Map<String, Object> sm = FacesContext.getCurrentInstance()
					.getExternalContext().getSessionMap();
			sm.put(ProtogenConstants.GOTO_PAGE, gotoPage);
			sm.put(ProtogenConstants.HISTO_TO_RETRIEVE, new Boolean(false));

			histoShow = new ArrayList<InstanceHistory>();
			if (historiqueId == null || historique == null)
				return "protogen-step-synthesis";

			for (Integer I : historiqueId) {
				boolean continueFlag = false;
				for (InstanceHistory inh : histoShow) {
					if (inh.getDbID() == I.intValue()) {
						continueFlag = true;
						break;
					}

				}

				if (continueFlag)
					continue;

				InstanceHistory ih = new InstanceHistory();
				ih.setDbID(I.intValue());

				for (PairKVElement e : procedure.getListInstances()) {
					if (Integer.parseInt(e.getKey()) == I.intValue()) {
						ih.setTitle(e.getValue());
						break;
					}
				}
				ih.setHistory(new ArrayList<ScreenDataHistory>());
				for (ScreenDataHistory history : historique) {
					ScreenDataHistory h = new ScreenDataHistory();
					h.setIdIndex(history.getIdIndex());
					h.setTitles(history.getTitles());
					h.setWindowTitle(history.getWindowTitle());
					h.setData(new ArrayList<ListKV>());
					for (ListKV datum : history.getData()) {
						int idIndex = h.getIdIndex();
						int idDB = 0;
						if (idIndex == 0)
							idDB = datum.getDbID();
						else {

							for (PairKVElement e : procedure.getListInstances()) {
								String compareValue = datum.getValue().get(
										idIndex - 1);
								if (e.getValue().startsWith(compareValue)) {
									idDB = Integer.parseInt(e.getKey());
									break;
								}
							}
						}

						if (idDB == ih.getDbID()) {
							h.getData().add(datum);
							boolean found = false;
							for (ScreenDataHistory sdh : ih.getHistory())
								if (sdh.getWindowTitle().equals(
										h.getWindowTitle())) {
									found = true;
									break;
								}
							if (!found)
								ih.getHistory().add(h);
						}
					}

				}
				histoShow.add(ih);
			}

			return "protogen-step-synthesis";

		} else

			return gotoPage;
	}

	public void saveHistory() {

		if (procedure.isMainEntityPresent()) {

			Boolean histo = (Boolean) FacesContext.getCurrentInstance()
					.getExternalContext().getSessionMap()
					.get(ProtogenConstants.HISTORY);
			if (histo != null && histo.booleanValue()) {
				ScreenDataHistory sdh = (ScreenDataHistory) FacesContext
						.getCurrentInstance().getExternalContext()
						.getSessionMap().get(ProtogenConstants.LAST_HISTORY);
				if (historique == null)
					historique = new ArrayList<ScreenDataHistory>();
				if (historiqueId == null)
					historiqueId = new ArrayList<Integer>();

				boolean hflag = false;
				ScreenDataHistory todel = null;
				for (ScreenDataHistory d : historique)
					if (d.getWindowTitle().equals(sdh.getWindowTitle())) {
						hflag = true;
						todel = d;
						break;
					}
				if (hflag)
					historique.remove(todel);

				historique.add(sdh);

				for (ListKV l : sdh.getData()) {
					int id = 0;
					if (sdh.getIdIndex() == 0)
						id = l.getDbID();
					else {
						String k = l.getValue().get(sdh.getIdIndex() - 1);

						for (PairKVElement e : procedure.getListInstances()) {
							if (e.getValue().startsWith(k)) {
								id = Integer.parseInt(e.getKey());
								break;
							}
						}
					}
					boolean fflag = false;
					for (Integer I : historiqueId) {
						if (I.intValue() == id) {
							fflag = true;
							break;
						}
					}
					if (!fflag)
						historiqueId.add(new Integer(id));
				}

			}
		}
	}

	public String continueProcedure() {
		Map<String, Object> sm = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();
		String page = (String) sm.get(ProtogenConstants.GOTO_PAGE);
		sm.put(ProtogenConstants.HISTO_TO_RETRIEVE, new Boolean(true));

		return page;
	}

	public void resChanged(AjaxBehaviorEvent e) {
		int id = 0;
		ResearchableResource re = new ResearchableResource();
		for (ResearchableResource r : sresources)
			if (r.getLabel().equals(selectedResearchable)) {
				re = r;
				break;
			}
		id = re.getId();
		//select menu after a research
		idMenu=id;
		String page = "";
		if (re.getType() == ResearchableType.ECRAN) {
			page = updateScreen(id) + ".xhtml";

		}

		if (re.getType() == ResearchableType.FORM) {
			ApplicationLoader dal = new ApplicationLoader();
			window = new CWindow();
			window.setId(re.getId());
			window.setAppKey(cache.getAppKey());
			window = dal.loadFullWindow(window);
			page = formview + ".xhtml";
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("action", "insert");
		}

		if (re.getType() == ResearchableType.FONCTION_SYSTEME) {
			switch (re.getId()) {
			case 1:
				page = "protogen-alerts.xhtml";
				break;
			case 2:
				page = "protogen-schedule.xhtml";
				break;
			case 3:
				page = "protogen-administration.xhtml";
				break;
			case 4:
				page = "protogen-dataload.xhtml";
				break;
			case 5:
				page = "protogen-datahistory.xhtml";
				break;
			case 6:
				page = "parametrage-profils.xhtml";
				break;
			case 7:
				page = "parametrage-utilisateurs.xhtml";
				break;
			}
		}

		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(page);
		} catch (IOException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}

		return;
	}

	public void selChanged(AjaxBehaviorEvent e) {

		for (SProcedure p : procedures) {
			if (p.getTitle().equals(selectedProcess)) {
				procedure = p;
				break;
			}
		}

		filters = new ArrayList<UIFilterElement>();
		ProtogenDataEngine pde = new ProtogenDataEngine();
		for (CBusinessClass c : procedure.getFilters()) {
			UIFilterElement fe = new UIFilterElement();
			List<PairKVElement> le = pde.getDataKeys(c.getDataReference(),
					false, 0);
			fe.setReference(true);
			fe.setListReference(le);
			fe.setLabel(c.getName());
			filters.add(fe);
		}

		procedureTitle = procedure.getTitle();
		procedureDescription = procedure.getDescription();

		String page = "protogen-welcome-procedure.xhtml";

		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(page);
		} catch (IOException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}

		return;
	}

	public String launchProcedure() {

		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put(ProtogenConstants.PROCEDURE_FILTERS, filters);

		String page = startProcess();
		return page;
	}

	public void detailedRow() {
		Map<String, String> pms = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String sindex, swindow;
		sindex = pms.get("subvindex");
		swindow = pms.get("subvhistorywindow");

		int index = Integer.parseInt(sindex);

		showSubs = true;

		// Look for the window and fill up the titles
		for (InstanceHistory ih : histoShow) {
			boolean found = false;
			for (ScreenDataHistory h : ih.getHistory()) {
				if (h.getWindowTitle().equals(swindow)) {
					titles = h.getTitles();
					subviewValues = new ArrayList<String>();
					for (ListKV l : h.getData())
						if (l.getDbID() == index) {
							subviewValues = l.getRoundValue();
						}

					found = true;
					break;
				}
			}
			if (found)
				break;
		}
	}

	public void hideSubs() {
		showSubs = false;
	}

	public String startProcess() {
		String page = "";

		Map<String, Object> sm = FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap();
		sm.put(ProtogenConstants.HISTO_TO_RETRIEVE, new Boolean(false));

		for (SProcedure p : procedures) {
			if (p.getTitle().equals(selectedProcess)) {
				procedure = p;
				break;
			}
		}
		if (procedure.isMainEntityPresent()) {
			sm.put(ProtogenConstants.HISTO_TO_RETRIEVE, new Boolean(true));
			sm.put(ProtogenConstants.PROCEDURE_ENTITY, procedure
					.getMainEntity().getDataReference());
		}

		Boolean historyMode = new Boolean(procedure.isMainEntityPresent());
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put(ProtogenConstants.HISTORY_MODE, historyMode);

		showWholeMenu = false;

		inProcess = true;
		processTitle = procedure.getTitle();
		processDescription = "Vous venez de terminer la procédure : "
				+ procedure.getTitle() + " : \n" + procedure.getDescription();

		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("inProcess", "true");

		page = ProcessScreenListener.getInstance().createNewProcess(
				cache.getUser(), procedure, this);
		atom = ProcessScreenListener.getInstance().getCurrentScreen(
				cache.getUser());

		currentStepLabel = atom.getStep().getTitle();
		currentFound = true;
		nextStepLabel = ProcessScreenListener.getInstance().getNextScreenLabel(
				cache.getUser());
		if (nextStepLabel.length() > 0)
			nextFound = true;
		else
			nextFound = false;

		currentAtoms = atom.getStep().getActions();
		atom.setCurrent(true);
		nextAtoms = ProcessScreenListener.getInstance().getNextAtoms(
				cache.getUser());

		boundButtons = new ArrayList<CDocumentbutton>();

		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("adesc", atom);

		if (atom.getType().getId() == 1) {
			window = atom.getWindow();
			boundButtons = new ArrayList<CDocumentbutton>();
			ApplicationLoader dao = new ApplicationLoader();

			window = dao.loadFullWindow(window);
			window = dao.loadWindowWithActions(window);
			window.setSynthesis(atom.isSynthesis());
			linksAvailable = window.getLinks() != null
					&& window.getLinks().size() > 0;
			if (linksAvailable)
				firstLink = window.getLinks().get(0);

			fcas = new ArrayList<CAttribute>();
			for (CAttribute a : window.getCAttributes())
				if (a.getCAttributetype().getId() == 6)
					fcas.add(a);
			if (page.equals(formview)) {
				FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().put("action", "insert");
				return formview;
			}
		}

		return page;
	}

	public String loadContext(){
		window = lastScreen;
		formContextAvailable = false;
		Map<String, Object> sesmap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		sesmap.put(ProtogenConstants.CONTEXT_FOUND, new Boolean(true));
		sesmap.put(ProtogenConstants.CONTEXT_FORM, formContext);
		loadWindow();
		return formview;
	}
	
	public String deleteContext(){
		formContextAvailable = false;
		ContextPersistence pers = new ContextPersistence();
		pers.mark(cache.getUser());
		return "";
	}
	
	
	public String loadInterruptedProcess() {
		
		if(formContextAvailable && !interruptedProcess)
			return loadContext();
		selectedProcess = ps.getProcess().getTitle();
		startProcess();
		return gotoAction();

	}

	
	
	public String deleteInterruptedProcess() {
		if(formContextAvailable && !interruptedProcess)
			return deleteContext();
		ProtogenDataEngine engine = new ProtogenDataEngine();

		engine.clearProcessSession(ps);
		interruptedProcess = false;
		return "";
	}

	public String gotoAction() {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String id = "";
		showWholeMenu = false;
		if (params.containsKey("toatom"))
			id = params.get("toatom");
		else if (interruptedProcess) {
			id = ps.getAtom().getId() + "";
		} else {
			return "";
		}

		interruptedProcess = false;
		String gotoPage = ProcessScreenListener.getInstance()
				.getSpecificScreen(cache.getUser(), Integer.parseInt(id));

		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("inProcess", "true");

		if (atom != null) {
			atom.setCurrent(false);
		}
		if (atom.isMandatory()) {
			FacesContext
					.getCurrentInstance()
					.addMessage(
							"",
							new FacesMessage(
									FacesMessage.SEVERITY_ERROR,
									"Cette action est obligatoire",
									"Vous ne pouvez pas naviguer vers une autre action avant d'accomplir l'action en cours"));
			return "";
		}

		atom = ProcessScreenListener.getInstance().getCurrentScreen(
				cache.getUser());
		atom.setCurrent(true);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("adesc", atom);
		currentAtoms = atom.getStep().getActions();
		nextAtoms = ProcessScreenListener.getInstance().getNextAtoms(
				cache.getUser());
		previousAtoms = ProcessScreenListener.getInstance().getPreviousAtoms(
				cache.getUser());
		if (!currentStepLabel.equals(atom.getStep().getTitle())) {
			previousStepLabel = currentStepLabel;
			currentStepLabel = atom.getStep().getTitle();
		}

		if (previousStepLabel.length() > 0)
			previousFound = true;
		else
			previousFound = false;

		currentFound = true;
		nextStepLabel = ProcessScreenListener.getInstance().getNextScreenLabel(
				cache.getUser());
		if (nextStepLabel.length() > 0)
			nextFound = true;
		else
			nextFound = false;

		if (gotoPage.equals("protogen-synthesis")) {
			nextFound = false;
			previousFound = false;
			currentFound = true;

			nextStepLabel = "";
			previousStepLabel = "";
			currentStepLabel = "Fin de la procédure";
		}
		boundButtons = new ArrayList<CDocumentbutton>();
		if (atom.getType().getId() == 1) {
			window = atom.getWindow();
			showWholeMenu = false;
			boundButtons = new ArrayList<CDocumentbutton>();
			ApplicationLoader dao = new ApplicationLoader();

			window = dao.loadFullWindow(window);
			window = dao.loadWindowWithActions(window);
			window.setSynthesis(atom.isSynthesis());
			linksAvailable = window.getLinks() != null
					&& window.getLinks().size() > 0;
			if (linksAvailable)
				firstLink = window.getLinks().get(0);

			fcas = new ArrayList<CAttribute>();
			for (CAttribute a : window.getCAttributes())
				if (a.getCAttributetype().getId() == 6)
					fcas.add(a);
			if (gotoPage.equals(formview)) {
				FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().put("action", "insert");
				return formview;
			}
		}

		ProtogenDataEngine engine = new ProtogenDataEngine();

		engine.clearProcessSession(ps);

		return gotoPage;

	}

	public String endProcess() {
		ProcessScreenListener.getInstance().destroyCurrentScreen(
				cache.getUser());
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("inProcess", "false");
		historique = new ArrayList<ScreenDataHistory>();

		nextAtoms = new ArrayList<SAtom>();
		previousAtoms = new ArrayList<SAtom>();
		currentAtoms = new ArrayList<SAtom>();
		showWholeMenu = true;

		currentStepLabel = "";
		currentFound = false;
		nextStepLabel = "";
		nextFound = false;
		previousStepLabel = "";
		previousFound = false;
		inProcess = false;
		boundButtons = new ArrayList<CDocumentbutton>();
		return "protogen";
	}

	public List<String> completeProcedure(String key) {
		List<String> labels = new ArrayList<String>();

		if (key.startsWith("*")) {
			for (SProcedure p : procedures)
				labels.add(p.getTitle());
			return labels;
		}

		for (SProcedure p : procedures) {
			for (String k : p.getKeyWords()) {
				if (k.toLowerCase().startsWith(key.toLowerCase())) {
					labels.add(p.getTitle());
					break;
				}
			}
		}

		return labels;
	}

	public List<String> completeResearchProcedure(String key) {
		List<String> labels = new ArrayList<String>();

		if (key.startsWith("*")) {
			for (ResearchableResource r : sresources)
				labels.add(r.getLabel());
			return labels;
		}

		for (ResearchableResource r : sresources) {
			if (r.getLabel().toLowerCase().startsWith(key.toLowerCase()))
				labels.add(r.getLabel());
		}

		return labels;
	}

	public void updateBoundButtons(List<CDocumentbutton> btns) {
		boundButtons = btns;

	}

	/*
	 * REDEMPTION SPECIFIC
	 */

	public void handleProcedureClose() {
		if (showRightPanel)
			redemptionWidth = "96%";
		else
			redemptionWidth = "66%";
		showRightPanel = !showRightPanel;
	}

	// Dashboard play/pause
	public void playSwitch() {
		pause = !pause;
	}

	public void pollTurn() {
		System.out.println("State : " + pause);
	}

	public void pollEvents(){
		GeneriumEventBus bus = GeneriumEventBus.getInstance();
		bus.energize(currentUser);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.EVENT_BUS, bus);
		events = bus.fetch(currentUser);
		if(events != null && events.size()>0){
			FacesContext.getCurrentInstance().
				addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Evénements", "Vous avez des événements en attente de consultation"));
		}
	}
	
	public void pollMails(){
		EmailManager manager = new EmailManager();
		manager.updateCache(cache.getMails());
	}
	
	/*
	 * WORKFLOW MANAGEMENT
	 */

	public String initWF() {
		// Load workflow id

		// Create WF execution instance and persist it

		// If the first step is mine

		return "";
	}

	public String executeWorkflowNode() {

		WorkflowExecution exec = new WorkflowExecution();
		for (WorkflowExecution we : workflowInstances)
			if (we.getCurrentNode() == currentNode) {
				exec = we;
				break;
			}

		wfLinkVisible = exec.getDataParameters().getEntity() != null
				&& exec.getDataParameters().getEntity().length() > 0;
		if (wfLinkVisible)
			wfLinkLabel = exec.getDataParameters().getEntity();

		if (currentNode instanceof WorkflowDecision) {
			decision = (WorkflowDecision) currentNode;
			decisionVisible = true;
		} else if (currentNode instanceof WorkflowAnswer) {
			answer = (WorkflowAnswer) currentNode;
			answerVisible = true;

			for (WFDecisionData d : exec.getDataParameters().getDecisions()) {
				if (d.getDecisionId() == answer.getDecisionNode().getId()) {
					answerValue = d.isConfirmed() ? answer.getDecisionNode()
							.getYesLabel() : answer.getDecisionNode()
							.getNoLabel();
					commentaire = d.getCommentaire();
				}

			}
		} else if (currentNode instanceof WorkflowScreenNode) {
			WorkflowScreenNode w = (WorkflowScreenNode) currentNode;
			return gotoScreenNode(w.getWindow());
		}

		return "";
	}

	public String gotoScreenNode(CWindow next) {
		ApplicationLoader dao = new ApplicationLoader();
		next.setAppKey(cache.getAppKey());
		window = dao.loadWindowFromLink(next);
		window = dao.loadFullWindow(window);
		window = dao.loadWindowWithActions(window);
		linksAvailable = window.getLinks() != null
				&& window.getLinks().size() > 0;
		if (linksAvailable)
			firstLink = window.getLinks().get(0);

		fcas = new ArrayList<CAttribute>();
		for (CAttribute a : window.getCAttributes())
			if (a.getCAttributetype().getId() == 6)
				fcas.add(a);

		showWholeMenu = true;

		WorkflowEngine e = new WorkflowEngine();

		WorkflowExecution exec = new WorkflowExecution();
		for (WorkflowExecution we : workflowInstances)
			if (we.getCurrentNode() == currentNode) {
				exec = we;
				break;
			}

		e.updateWorkflowExecution(exec);
		workflowInstances.remove(exec);

		if (window.getCWindowtype().getId() == 1)
			return "protogen-listview";
		else if (window.getCWindowtype().getId() == 2) {
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("action", "insert");
			return formview;
		}

		return "";
	}

	public void doWfYes() {

		WorkflowEngine e = new WorkflowEngine();

		WorkflowExecution exec = new WorkflowExecution();
		for (WorkflowExecution we : workflowInstances)
			if (we.getCurrentNode() == currentNode) {
				exec = we;
				break;
			}
		WFDecisionData d = new WFDecisionData();
		d.setConfirmed(true);
		d.setDecisionId(decision.getId());
		d.setCommentaire(commentaire);
		if (exec.getDataParameters().getDecisions() == null)
			exec.getDataParameters().setDecisions(
					new ArrayList<WFDecisionData>());

		exec.getDataParameters().getDecisions().add(d);

		e.updateWorkflowExecution(exec, decision.getYesNode());
		workflowInstances.remove(exec);
		decisionVisible = false;
	}

	public void doWfNo() {
		WorkflowEngine e = new WorkflowEngine();

		WorkflowExecution exec = new WorkflowExecution();
		for (WorkflowExecution we : workflowInstances)
			if (we.getCurrentNode() == currentNode) {
				exec = we;
				break;
			}
		WFDecisionData d = new WFDecisionData();
		d.setConfirmed(false);
		d.setCommentaire(commentaire);
		d.setDecisionId(decision.getId());
		if (exec.getDataParameters().getDecisions() == null)
			exec.getDataParameters().setDecisions(
					new ArrayList<WFDecisionData>());

		exec.getDataParameters().getDecisions().add(d);

		e.updateWorkflowExecution(exec, decision.getNoNode());
		workflowInstances.remove(exec);
		decisionVisible = false;
	}

	public void workflowAnswerNext() {
		WorkflowEngine e = new WorkflowEngine();

		WorkflowExecution exec = new WorkflowExecution();
		for (WorkflowExecution we : workflowInstances)
			if (we.getCurrentNode() == currentNode) {
				exec = we;
				break;
			}

		e.updateWorkflowExecution(exec);
		workflowInstances.remove(exec);
		answerVisible = false;
	}

	public String wfFollowLink() {

		WorkflowExecution exec = new WorkflowExecution();
		for (WorkflowExecution we : workflowInstances)
			if (we.getCurrentNode() == currentNode) {
				exec = we;
				break;
			}

		WFData datum = exec.getDataParameters();

		window = datum.getWindow();
		String rowID = datum.getSubjectId() + ""; // selectedRow[0].getDbID()+"";

		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("action", "update");
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.put("rowID", rowID);
		return formview;

	}

	public void pollWorkflows() {
		// Get workflow executions
		CoreRole role = cache.getUser().getCoreRole();
		WorkflowEngine wfe = new WorkflowEngine();
		workflowInstances = wfe.getPendingInstances(role);
		userNodes = new ArrayList<WorkflowNode>();
		for (WorkflowExecution e : workflowInstances) {
			alertsAvailable = true;
			userNodes.add(e.getCurrentNode());
			String title = "Le processus " + e.getDefinition().getTitle()
					+ " requiert votre participation";
			String description = "Vous êtes invités à assurer l'activité suivante : "
					+ e.getCurrentNode().getLabel();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, title,
							description));
		}
	}

	
	/*
	 * EVENTS MANAGEMENT
	 */
	public String markEvent(){
		String eid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("EVTIN");
		if(eid == null || eid.length()==0)
			return "";
		
		int id = Integer.parseInt(eid);
		GEventInstance ei = null;
		for(GEventInstance i : events)
			if(i.getId() == id){
				ei = i;
				break;
			}
		
		GeneriumEventBus bus = (GeneriumEventBus)FacesContext.getCurrentInstance().
				getExternalContext().getSessionMap().get(ProtogenConstants.EVENT_BUS);
		
		PostEventAction action = bus.markEvent(ei);
		if(action instanceof PEAWindow){
			CWindow w = ((PEAWindow)action).getWindow();
			return loadEventWindow(w,((PEAWindow)action),ei);
		}
		
		EventDataAccess eda = new EventDataAccess();
		String tel = eda.getDestinataireTel(ei);
		String email = eda.getDestinataireEmail(ei);
		
		if(action instanceof PEASms){
			SmsClient.getInstance().sendSMS(tel, (PEASms)action, ei);
		}
		if(action instanceof PEAMail){
			ClientMail.getInstance().sendMail(email, (PEAMail)action, ei);
		}
		
		return "";
	}
	
	private String loadEventWindow(CWindow next, PEAWindow evt, GEventInstance ei){
		ApplicationLoader dao = new ApplicationLoader();
		next.setAppKey(cache.getAppKey());
		window = dao.loadWindowFromLink(next);
		window = dao.loadFullWindow(window);
		window = dao.loadWindowWithActions(window);
		linksAvailable = window.getLinks() != null
				&& window.getLinks().size() > 0;
		if (linksAvailable)
			firstLink = window.getLinks().get(0);

		fcas = new ArrayList<CAttribute>();
		for (CAttribute a : window.getCAttributes())
			if (a.getCAttributetype().getId() == 6)
				fcas.add(a);

		showWholeMenu = true;

		

		if (window.getCWindowtype().getId() == 1){
			if(!evt.isModeDetails())
				return "protogen-listview";
			
			FacesContext.getCurrentInstance().getExternalContext()
			.getSessionMap().put("rowID", ""+ei.getRowId());
			
			FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap().put("action", "update");
			return formview;
			
		}
		else if (window.getCWindowtype().getId() == 2) {
			
			FacesContext.getCurrentInstance().getExternalContext()
					.getSessionMap().put("action", "insert");
			return formview;
		}

		return "";
	}
	
	/*
	 * Scheduling entities
	 */
	private List<CSchedulableEntity> schedules = new ArrayList<CSchedulableEntity>();
	private List<PairKVElement> scheduleDetails = new ArrayList<PairKVElement>();
	private String detailedEntity="";
	
	private ScheduleModel scheduleModel = new DefaultScheduleModel();
	private void prepareSchedule(){
		ApplicationLoader dal = new ApplicationLoader();
		ProtogenDataEngine pde = new ProtogenDataEngine();
		scheduleModel = new DefaultScheduleModel();
		
		String appkey = cache.getAppKey();
		schedules = dal.loadScheduledEntities(appkey);
		for(CSchedulableEntity ce : schedules){
			ce = pde.loadEvents(ce, cache);
			for(CScheduleEvent e : ce.getEvents()){
				if(e.getDateEvent() == null)
					continue;
				
				DefaultScheduleEvent ev = new DefaultScheduleEvent(e.getLabel(), e.getDateEvent(), e.getEndEvent(),e);
				ev.setAllDay(true);
				scheduleModel.addEvent(ev);
			}
		}
		
	}
	
	public void onEventSelect(SelectEvent selectEvent) {
		ProtogenDataEngine pde = new ProtogenDataEngine();
		
		ScheduleEvent event = (ScheduleEvent) selectEvent.getObject();
		CScheduleEvent e = (CScheduleEvent)event.getData();
		setDetailedEntity(e.getEntity().getName());
		
		List<Map<CAttribute, Object>> data = pde.getDataByConstraint(e.getEntity(), "pk_"+e.getEntity().getDataReference()+"="+e.getDbID());
		if(data == null || data.size() == 0)
			return;
		
		Map<CAttribute,Object> datum = data.get(0);
		scheduleDetails = new ArrayList<PairKVElement>();
		for(CAttribute a : datum.keySet()){
			String key = a.getAttribute();
			String value = "";
			if(a.getDataReference().startsWith("pk_")){
				continue;
			}
			if(a.getDataReference().startsWith("fk_")){
				if(datum.get(a)!=null && datum.get(a).toString().length()>0){
					int id = Integer.parseInt(datum.get(a).toString().trim());
					String table = a.getDataReference().substring(3);
					PairKVElement pkv = pde.getDataKeyByID(table, id);
					value=pkv.getValue();
				}else{
					value="";
				}
			} else {
				if(a.getCAttributetype().getId()==3){
					String sdate = datum.get(a).toString().trim().split(" ")[0];
					if(sdate == null  || sdate.length()==0)
						value="";
					else {
						value = sdate.split("-")[2]+"/"+sdate.split("-")[1]+"/"+sdate.split("-")[0];
					}
						
				} else{
					if(datum.get(a)==null)
						value="";
					else
						value=datum.get(a).toString().trim();
				}
			}
			
			PairKVElement pkv = new PairKVElement(key,value);
			scheduleDetails.add(pkv);
		}
		
		return;
    }
	
	/*
	 * GETTERS AND SETTERS
	 */

	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public String getMainWindowTitle() {
		return mainWindowTitle;
	}

	public void setMainWindowTitle(String mainWindowTitle) {
		this.mainWindowTitle = mainWindowTitle;
	}

	public Map<String, List<String>> getMenu() {
		return menu;
	}

	public void setMenu(Map<String, List<String>> menu) {
		this.menu = menu;
	}

	public ApplicationCache getCache() {
		return cache;
	}

	public void setCache(ApplicationCache cache) {
		this.cache = cache;
	}

	public List<UIMenu> getSupermenu() {
		return supermenu;
	}

	public void setSupermenu(List<UIMenu> supermenu) {
		this.supermenu = supermenu;
	}

	public int getSelectedSuperMenu() {
		return selectedSuperMenu;
	}

	public void setSelectedSuperMenu(int selectedSuperMenu) {
		this.selectedSuperMenu = selectedSuperMenu;
	}

	public List<PairKVElement> getCurrentMenu() {
		return currentMenu;
	}

	public void setCurrentMenu(List<PairKVElement> currentMenu) {
		this.currentMenu = currentMenu;
	}

	public CWindow getWindow() {
		return window;
	}

	public void setWindow(CWindow window) {
		this.window = window;
	}

	public int getSubmenuSize() {
		return submenuSize;
	}

	public void setSubmenuSize(int submenuSize) {
		this.submenuSize = submenuSize;
	}

	public int getSupermenuSize() {
		return supermenuSize;
	}

	public void setSupermenuSize(int supermenuSize) {
		this.supermenuSize = supermenuSize;
	}

	public List<SProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<SProcess> processes) {
		this.processes = processes;
	}

	public String getSelectedProcess() {
		return selectedProcess;
	}

	public void setSelectedProcess(String selectedProcess) {
		this.selectedProcess = selectedProcess;
	}

	public List<String> getProcLabels() {
		return procLabels;
	}

	public void setProcLabels(List<String> procLabels) {
		this.procLabels = procLabels;
	}

	public SProcess getProcess() {
		return process;
	}

	public void setProcess(SProcess process) {
		this.process = process;
	}

	public String getProcessTitle() {
		return processTitle;
	}

	public void setProcessTitle(String processTitle) {
		this.processTitle = processTitle;
	}

	public String getProcessDescription() {
		return processDescription;
	}

	public void setProcessDescription(String processDescription) {
		this.processDescription = processDescription;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}

	public List<SMenuitem> getMetamenu() {
		return metamenu;
	}

	public void setMetamenu(List<SMenuitem> metamenu) {
		this.metamenu = metamenu;
	}

	public String getPreviousStepLabel() {
		return previousStepLabel;
	}

	public void setPreviousStepLabel(String previousStepLabel) {
		this.previousStepLabel = previousStepLabel;
	}

	public String getCurrentStepLabel() {
		return currentStepLabel;
	}

	public void setCurrentStepLabel(String currentStepLabel) {
		this.currentStepLabel = currentStepLabel;
	}

	public String getNextStepLabel() {
		return nextStepLabel;
	}

	public void setNextStepLabel(String nextStepLabel) {
		this.nextStepLabel = nextStepLabel;
	}

	public boolean isPreviousFound() {
		return previousFound;
	}

	public void setPreviousFound(boolean previousFound) {
		this.previousFound = previousFound;
	}

	public boolean isCurrentFound() {
		return currentFound;
	}

	public void setCurrentFound(boolean currentFound) {
		this.currentFound = currentFound;
	}

	public boolean isNextFound() {
		return nextFound;
	}

	public void setNextFound(boolean nextFound) {
		this.nextFound = nextFound;
	}

	public List<SProcedure> getProcedures() {
		return procedures;
	}

	public void setProcedures(List<SProcedure> procedures) {
		this.procedures = procedures;
	}

	public SProcedure getProcedure() {
		return procedure;
	}

	public void setProcedure(SProcedure procedure) {
		this.procedure = procedure;
	}

	public SAtom getAtom() {
		return atom;
	}

	public void setAtom(SAtom atom) {
		this.atom = atom;
	}

	public boolean isInProcess() {
		return inProcess;
	}

	public void setInProcess(boolean inProcess) {
		this.inProcess = inProcess;
	}

	public List<SAtom> getCurrentAtoms() {
		return currentAtoms;
	}

	public void setCurrentAtoms(List<SAtom> currentAtoms) {
		this.currentAtoms = currentAtoms;
	}

	public List<SAtom> getNextAtoms() {
		return nextAtoms;
	}

	public void setNextAtoms(List<SAtom> nextAtoms) {
		this.nextAtoms = nextAtoms;
	}

	public List<SAtom> getPreviousAtoms() {
		return previousAtoms;
	}

	public void setPreviousAtoms(List<SAtom> previousAtoms) {
		this.previousAtoms = previousAtoms;
	}

	public String getUserLastName() {
		return userLastName;
	}

	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}

	public String getFilteredMenuItem() {
		return filteredMenuItem;
	}

	public void setFilteredMenuItem(String filteredMenuItem) {
		this.filteredMenuItem = filteredMenuItem;
	}

	public boolean isInterruptedProcess() {
		return interruptedProcess;
	}

	public void setInterruptedProcess(boolean interruptedProcess) {
		this.interruptedProcess = interruptedProcess;
	}

	public String getInprocLabel() {
		return inprocLabel;
	}

	public void setInprocLabel(String inprocLabel) {
		this.inprocLabel = inprocLabel;
	}

	public String getInprocAtom() {
		return inprocAtom;
	}

	public void setInprocAtom(String inprocAtom) {
		this.inprocAtom = inprocAtom;
	}

	public DTOProcessSession getPs() {
		return ps;
	}

	public void setPs(DTOProcessSession ps) {
		this.ps = ps;
	}

	public boolean isShowWholeMenu() {
		return showWholeMenu;
	}

	public void setShowWholeMenu(boolean showWholeMenu) {
		this.showWholeMenu = showWholeMenu;
	}

	public List<CAttribute> getFcas() {
		return fcas;
	}

	public void setFcas(List<CAttribute> fcas) {
		this.fcas = fcas;
	}

	public List<CDocumentbutton> getBoundButtons() {
		return boundButtons;
	}

	public void setBoundButtons(List<CDocumentbutton> boundButtons) {
		this.boundButtons = boundButtons;
	}

	public CoreUser getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(CoreUser currentUser) {
		this.currentUser = currentUser;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public boolean isLinksAvailable() {
		return linksAvailable;
	}

	public void setLinksAvailable(boolean linksAvailable) {
		this.linksAvailable = linksAvailable;
	}

	public CWindow getFirstLink() {
		return firstLink;
	}

	public void setFirstLink(CWindow firstLink) {
		this.firstLink = firstLink;
	}

	public String getAccIndex() {
		return accIndex;
	}

	public void setAccIndex(String accIndex) {
		this.accIndex = accIndex;
	}

	public List<SRubrique> getAngramainyu() {
		return angramainyu;
	}

	public void setAngramainyu(List<SRubrique> angramainyu) {
		this.angramainyu = angramainyu;
	}

	public SRubrique getCurrentRubrique() {
		return currentRubrique;
	}

	public void setCurrentRubrique(SRubrique currentRubrique) {
		this.currentRubrique = currentRubrique;
	}

	public String getThemeLib() {
		return themeLib;
	}

	public void setThemeLib(String themeLib) {
		this.themeLib = themeLib;
	}

	public String getActiveRubrique() {
		return activeRubrique;
	}

	public void setActiveRubrique(String activeRubrique) {
		this.activeRubrique = activeRubrique;
	}

	public String getMasterPage() {
		return masterPage;
	}

	public void setMasterPage(String masterPage) {
		this.masterPage = masterPage;
	}

	public String getThemeVar() {
		return themeVar;
	}

	public void setThemeVar(String themeVar) {
		this.themeVar = themeVar;
	}

	public String getUniformPageBodyStyle() {
		return uniformPageBodyStyle;
	}

	public void setUniformPageBodyStyle(String uniformPageBodyStyle) {
		this.uniformPageBodyStyle = uniformPageBodyStyle;
	}

	public String getMajorColor() {
		return majorColor;
	}

	public void setMajorColor(String majorColor) {
		this.majorColor = majorColor;
	}

	public String getLogoPercent() {
		return logoPercent;
	}

	public void setLogoPercent(String logoPercent) {
		this.logoPercent = logoPercent;
	}

	public boolean isAlertsAvailable() {
		return alertsAvailable;
	}

	public void setAlertsAvailable(boolean alertsAvailable) {
		this.alertsAvailable = alertsAvailable;
	}

	public List<AlertInstance> getUserAlerts() {
		return userAlerts;
	}

	public void setUserAlerts(List<AlertInstance> userAlerts) {
		this.userAlerts = userAlerts;
	}

	public AlertInstance getCurrentAlert() {
		return currentAlert;
	}

	public void setCurrentAlert(AlertInstance currentAlert) {
		this.currentAlert = currentAlert;
	}

	public String getRedemptionWidth() {
		return redemptionWidth;
	}

	public void setRedemptionWidth(String redemptionWidth) {
		this.redemptionWidth = redemptionWidth;
	}

	public String getFormview() {
		return formview;
	}

	public void setFormview(String formview) {
		this.formview = formview;
	}

	public boolean isShowRightPanel() {
		return showRightPanel;
	}

	public void setShowRightPanel(boolean showRightPanel) {
		this.showRightPanel = showRightPanel;
	}

	public SApplication getPapp() {
		return papp;
	}

	public void setPapp(SApplication papp) {
		this.papp = papp;
	}

	public List<WorkflowExecution> getWorkflowInstances() {
		return workflowInstances;
	}

	public void setWorkflowInstances(List<WorkflowExecution> workflowInstances) {
		this.workflowInstances = workflowInstances;
	}

	public List<WorkflowNode> getUserNodes() {
		return userNodes;
	}

	public void setUserNodes(List<WorkflowNode> userNodes) {
		this.userNodes = userNodes;
	}

	public WorkflowNode getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(WorkflowNode currentNode) {
		this.currentNode = currentNode;
	}

	public WorkflowDecision getDecision() {
		return decision;
	}

	public void setDecision(WorkflowDecision decision) {
		this.decision = decision;
	}

	public boolean isDecisionVisible() {
		return decisionVisible;
	}

	public void setDecisionVisible(boolean decisionVisible) {
		this.decisionVisible = decisionVisible;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public WorkflowAnswer getAnswer() {
		return answer;
	}

	public void setAnswer(WorkflowAnswer answer) {
		this.answer = answer;
	}

	public boolean isAnswerVisible() {
		return answerVisible;
	}

	public void setAnswerVisible(boolean answerVisible) {
		this.answerVisible = answerVisible;
	}

	public String getAnswerValue() {
		return answerValue;
	}

	public void setAnswerValue(String answerValue) {
		this.answerValue = answerValue;
	}

	public String getWfLinkLabel() {
		return wfLinkLabel;
	}

	public void setWfLinkLabel(String wfLinkLabel) {
		this.wfLinkLabel = wfLinkLabel;
	}

	public boolean isWfLinkVisible() {
		return wfLinkVisible;
	}

	public void setWfLinkVisible(boolean wfLinkVisible) {
		this.wfLinkVisible = wfLinkVisible;
	}

	public CWindow getLastWindow() {
		return lastWindow;
	}

	public void setLastWindow(CWindow lastWindow) {
		this.lastWindow = lastWindow;
	}

	public boolean isLastWindowAvailable() {
		return lastWindowAvailable;
	}

	public void setLastWindowAvailable(boolean lastWindowAvailable) {
		this.lastWindowAvailable = lastWindowAvailable;
	}

	public List<ResearchableResource> getSresources() {
		return sresources;
	}

	public void setSresources(List<ResearchableResource> sresources) {
		this.sresources = sresources;
	}

	public String getSelectedResearchable() {
		return selectedResearchable;
	}

	public void setSelectedResearchable(String selectedResearchable) {
		this.selectedResearchable = selectedResearchable;
	}

	public boolean isSingleLink() {
		return singleLink;
	}

	public void setSingleLink(boolean singleLink) {
		this.singleLink = singleLink;
	}

	public String getProcedureTitle() {
		return procedureTitle;
	}

	public void setProcedureTitle(String procedureTitle) {
		this.procedureTitle = procedureTitle;
	}

	public String getProcedureDescription() {
		return procedureDescription;
	}

	public void setProcedureDescription(String procedureDescription) {
		this.procedureDescription = procedureDescription;
	}

	public List<UIFilterElement> getFilters() {
		return filters;
	}

	public void setFilters(List<UIFilterElement> filters) {
		this.filters = filters;
	}

	public List<ScreenDataHistory> getHistorique() {
		return historique;
	}

	public void setHistorique(List<ScreenDataHistory> historique) {
		this.historique = historique;
	}

	public List<Integer> getHistoriqueId() {
		return historiqueId;
	}

	public void setHistoriqueId(List<Integer> historiqueId) {
		this.historiqueId = historiqueId;
	}

	public List<InstanceHistory> getHistoShow() {
		return histoShow;
	}

	public void setHistoShow(List<InstanceHistory> histoShow) {
		this.histoShow = histoShow;
	}

	public List<PairKVElement> getTitles() {
		return titles;
	}

	public void setTitles(List<PairKVElement> titles) {
		this.titles = titles;
	}

	public List<String> getSubviewValues() {
		return subviewValues;
	}

	public void setSubviewValues(List<String> subviewValues) {
		this.subviewValues = subviewValues;
	}

	public boolean isShowSubs() {
		return showSubs;
	}

	public void setShowSubs(boolean showSubs) {
		this.showSubs = showSubs;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public String getNavigableClass() {
		return navigableClass;
	}

	public void setNavigableClass(String navigableClass) {
		this.navigableClass = navigableClass;
	}

	public int getIdMenu() {
		return idMenu;
	}

	public void setIdMenu(int idMenu) {
		this.idMenu = idMenu;
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

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getProfilPhoto() {
		return profilPhoto;
	}

	public void setProfilPhoto(String profilPhoto) {
		this.profilPhoto = profilPhoto;
	}

	public FormContext getFormContext() {
		return formContext;
	}

	public void setFormContext(FormContext formContext) {
		this.formContext = formContext;
	}

	public boolean isFormContextAvailable() {
		return formContextAvailable;
	}

	public void setFormContextAvailable(boolean formContextAvailable) {
		this.formContextAvailable = formContextAvailable;
	}

	public CWindow getLastScreen() {
		return lastScreen;
	}

	public void setLastScreen(CWindow lastScreen) {
		this.lastScreen = lastScreen;
	}

	public List<GEventInstance> getEvents(){
		return events;
	}
	
	public void setEvents(List<GEventInstance> events){
		this.events = events;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public List<CSchedulableEntity> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<CSchedulableEntity> schedules) {
		this.schedules = schedules;
	}

	public ScheduleModel getScheduleModel() {
		return scheduleModel;
	}

	public void setScheduleModel(ScheduleModel scheduleModel) {
		this.scheduleModel = scheduleModel;
	}

	public boolean isAdminMode() {
		return adminMode;
	}

	public void setAdminMode(boolean adminMode) {
		this.adminMode = adminMode;
	}

	public List<PairKVElement> getScheduleDetails() {
		return scheduleDetails;
	}

	public void setScheduleDetails(List<PairKVElement> scheduleDetails) {
		this.scheduleDetails = scheduleDetails;
	}

	public String getDetailedEntity() {
		return detailedEntity;
	}

	public void setDetailedEntity(String detailedEntity) {
		this.detailedEntity = detailedEntity;
	}

	public LocalizationParameters getLocalization() {
		return localization;
	}

	public void setLocalization(LocalizationParameters localization) {
		this.localization = localization;
	}
}
