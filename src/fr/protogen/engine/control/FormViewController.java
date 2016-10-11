package fr.protogen.engine.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;

import fr.protogen.dataload.ContextPersistence;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.control.ui.GParametersComponent;
import fr.protogen.engine.control.ui.MtmBlock;
import fr.protogen.engine.control.ui.MtmLine;
import fr.protogen.engine.control.ui.SelectedItem;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.CalendarUtils;
import fr.protogen.engine.utils.DBFormattedObjects;
import fr.protogen.engine.utils.FormContext;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.engine.utils.UIControlSingle;
import fr.protogen.engine.utils.UIControlsLine;
import fr.protogen.engine.utils.UISimpleValues;
import fr.protogen.masterdata.DAO.AlertDataAccess;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.CompositionDataAccess;
import fr.protogen.masterdata.DAO.HistoryDataAccess;
import fr.protogen.masterdata.DAO.LocalizationEngine;
import fr.protogen.masterdata.DAO.OrganizationDAL;
import fr.protogen.masterdata.DAO.TriggersEngine;
import fr.protogen.masterdata.model.AlertInstance;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CComposedBean;
import fr.protogen.masterdata.model.CComposingeBean;
import fr.protogen.masterdata.model.CComposition;
import fr.protogen.masterdata.model.CDataHistory;
import fr.protogen.masterdata.model.CGlobalValue;
import fr.protogen.masterdata.model.CIdentificationRow;
import fr.protogen.masterdata.model.CInstanceHistory;
import fr.protogen.masterdata.model.CLocalizedEntity;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.CParameterMetamodel;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreProfil;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.GParametersInstance;
import fr.protogen.masterdata.model.GParametersPackage;
import fr.protogen.masterdata.model.SAlert;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.Trigger;
import fr.protogen.masterdata.services.HabilitationsService;
import fr.protogen.masterdata.services.MTMService;

@ManagedBean
@ViewScoped
public class FormViewController implements Serializable {
	
	
	//	Attributes
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4456884863616119922L;
	
	/**
	 * 
	 */
	private String action;
	@ManagedProperty(value="#{frontController.window}")
	private CWindow window;
	private List<UIControlElement> controls;
	private String windowTitle;
	private String windowDescription;
	private String windowPercentage;
	private String windowHelp;
	private String processMessage="";
	private String messageClass="goodMsg";
	private DBFormattedObjects initialData;
	private int dbID;
	private Map<String, String> foreignKeys;
	private String moneyCode;
	
	
	//	mtm lines
	private List<MtmBlock> mtmBlocks = new ArrayList<MtmBlock>();
	private List<MtmBlock> refBlocks = new ArrayList<MtmBlock>();
	private SelectedItem selectedMtmBlock;
	private String selectedReference;
	
	//	process related
	private boolean inProcess=false;
	
	private CAttribute selectedAttribute = new CAttribute();
	
	//	Global values management
	private List<UIControlElement> globalControls = new ArrayList<UIControlElement>(); 
	
	
	//	Files and binary content
	private UIControlElement uploadTo;
	private boolean nonVoidContent;
	private boolean fileFieldInprogress=false;
	private String fileExtension;
	private StreamedContent file;
	private UploadedFile uploadFile;
	
	//	Reference attributes
	private List<String> references = new ArrayList<String>();
	
	//	Recall
	private boolean rappelActivated = false;
	private List<UIControlElement> rappelHistory = new ArrayList<UIControlElement>();
	
	private boolean insert;
	
	//	Saisie en lignes
	private UIControlsLine controlLines = new UIControlsLine();	
	
	private boolean inlineForm=false;
	
	private List<String> titles=new ArrayList<String>();
	private List<UISimpleValues> values = new ArrayList<UISimpleValues>();
	private boolean saved=false;
	
	
	//	inline reference creation
	private List<UIControlElement> inlineCreation=new ArrayList<UIControlElement>();
	private UIControlsLine icLines = new UIControlsLine();
	private CBusinessClass toCreateEntity = new CBusinessClass();
	private UIControlElement controlToCreate = new UIControlElement();
	
	//	Alerts
	private List<SAlert> alerts;
	
	//	Parameters model
	private CParameterMetamodel paramodel;
	private boolean parameteredEntity=false;
	private Map<GParametersPackage, List<GParametersInstance>> graphicalParameters;
	private List<GParametersComponent> parametersComponents;
	private GParametersComponent selectedComponent;
	private String selectedComponentName;
	private String selectedInstance;
	private GParametersPackage selectedPackage = new GParametersPackage();
	
	//	Updated fields
	private String updatedFields="";
	
	//	History
	private boolean histomode=false;
	private Date histoStart = new Date();
	
	//	Implementing unique row mode
	private boolean uniqueRowMode;
	private CAttribute uniqueTo;
	private String unicityReferenceTable;
	private CIdentificationRow identificationRow;
	private boolean defaultChecked;
	
	//	Composition
	private boolean compositionMode = false;
	private CComposedBean composedBean = new CComposedBean();
	private CComposition composition = new CComposition();
	private List<PairKVElement> composables = new ArrayList<PairKVElement>();
	private DualListModel<String> toCompose;
	private List<String> sourceComposition;
	private List<String> targetComposition;
	
	//	Mtm changes
	private MtmBlock changeMtmBlock = new MtmBlock();
	
	private int activeMtmBlockId;
	
	//	profiles and roles
	private boolean userRepresentative;
	private DualListModel<String> picklistProfils;
	private DualListModel<String> picklistRoles;
	private List<CoreRole> profils;
	private List<CoreProfil> roles;
	private String userName;
	private String password;
	private String confirmPassword;
	private List<CoreProfil> profilsList = new ArrayList<CoreProfil>();
	private List<CoreRole> rolesList = new ArrayList<CoreRole>();
	
	//	Localized entity
	private CLocalizedEntity localizedEntity;
	private boolean localizedMode;
	private List<PairKVElement> organizationLevels = new ArrayList<PairKVElement>();
	private String selectedLevelId;
	private List<GOrganization> organisations = new ArrayList<GOrganization>();
	private Map<GOrganization, TreeNode> orgMap = new HashMap<GOrganization, TreeNode>();
	private TreeNode root;
	private TreeNode selectedOrg;
	
	//	History
	private CDataHistory history = new CDataHistory();
	
	//	Alpha mode
	private boolean alphaMode=false;
	private String alphaEntity="";
	private String alphaReference="";
	
	private boolean navigation;
	
	private boolean singleMode = false;
	
	private int activeIndex=0;
	
	//	Internationalization
	private LocalizationEngine translator = new LocalizationEngine();
	
	private MtmBlock mtmBlock = new MtmBlock();
	private UIControlElement uictrl = new UIControlElement();
	private MtmLine mtmLine = new MtmLine();
	private boolean mtmEnabled = false;
	
	private UIControlElement currentControl;
	
	//	Methods
		
	@PostConstruct
	public void energize() {
		
		boolean notinsession=(window==null || !FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
				|| FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")==null); 

		if(notinsession){
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.HISTORY, new Boolean(false));
		
		/*********************************************************************************************************
		 *********************************** Parameters: START****************************************************
		 *********************************************************************************************************/
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		OrganizationDAL odal = new OrganizationDAL();
		CBusinessClass referenceBC = pde.getReferencedTable(window.getMainEntity());
		setParametersComponents(new ArrayList<GParametersComponent>());
		graphicalParameters = new HashMap<GParametersPackage, List<GParametersInstance>>();
		// Get applied models
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		List<GParametersInstance> models = cache.getParameterPackages();
		for(GParametersInstance m : models){
			List<CBusinessClass> modelEntities = odal.loadModelEntities(m);
			m.getModelPackage().setImplicatedEntities(modelEntities); 
		}
		
		//	Check if entity belongs to any model
		parametersComponents = new ArrayList<GParametersComponent>();
		
		for(GParametersInstance m : models){
			boolean foundFlag= false;
			for(CBusinessClass bc : m.getModelPackage().getImplicatedEntities()){
				if(bc.getId() == referenceBC.getId()){
					foundFlag = true;
				}
			}
			
			parameteredEntity = parameteredEntity || foundFlag;
			boolean ppflag = false;
			for(GParametersPackage gpp : graphicalParameters.keySet()){
				if(gpp.getId() == m.getModelPackage().getId()){
					ppflag=true;
					break;
				}
					
			}
			
			if(ppflag){
				for(GParametersPackage gpp : graphicalParameters.keySet()){
					if(gpp.getId() == m.getModelPackage().getId()){
						graphicalParameters.get(gpp).add(m);
						break;
					}
				}
				
			}else{
				graphicalParameters.put(m.getModelPackage(), new ArrayList<GParametersInstance>());
				graphicalParameters.get(m.getModelPackage()).add(m);
			}
			
			GParametersComponent comp = new GParametersComponent();
			if(!ppflag){
				comp.setModelName(m.getModelPackage().getNom());
				comp.setPkg(m.getModelPackage());
				if(comp.getPkg().getEntity().getId()==0){
					comp.getPkg().setEntity(pde.getReferencedTable(comp.getPkg().getEntity().getDataReference()));
				}
				comp.setModelInstances(new ArrayList<PairKVElement>());
			}
			
			for(GParametersComponent cc : parametersComponents){
				if(cc.getPkg().getId() == m.getModelPackage().getId()){
					comp = cc;
					break;
				}
			}
			
			List<GParametersInstance> instances = new ArrayList<GParametersInstance>();
			for(GParametersPackage gpp : graphicalParameters.keySet()){
				if(gpp.getId() == m.getModelPackage().getId()){
					instances = graphicalParameters.get(gpp);
				}
			}
			
			for(GParametersInstance i : instances){
				
				PairKVElement e = pde.getDataKeyByID(i.getModelPackage().getEntity().getDataReference(), i.getBeanId());
				boolean inflag = false;
				for(PairKVElement el : comp.getModelInstances()){
					if(el.getDbID() == e.getDbID()){
						inflag = true;
						break;
					}
				}
				if(!inflag)
					comp.getModelInstances().add(e);
			}
			if(!ppflag)
				parametersComponents.add(comp);
			
		}
		
		if(parametersComponents != null && parametersComponents.size()>0){
			selectedComponent = parametersComponents.get(0);
			selectedPackage =  selectedComponent.getPkg();
		}
		
		/*********************************************************************************************************
		 *********************************** Parameters: END******************************************************
		 *********************************************************************************************************/
		
		moneyCode = cache.getMoneyCode();
		
		inlineForm = (FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FORM_MODE").equals("protogen-formline"));
		
		// Static layout parameters
		CoreUser u = cache.getUser();
		
		windowTitle = translator.windowTranslate(window.getTitle(), window.getId(), u.getLanguage());
		windowDescription  = window.getStepDescription();
		windowPercentage = window.getPercentage()+" %";
		windowHelp = window.getHelpVideo();
		
		selectedMtmBlock = new SelectedItem(0);
		
		
		
		//	Data loading
		ApplicationLoader loader = new ApplicationLoader(); 
		window = loader.loadFullWindow(window);
		window = loader.loadWindowWithActions(window);
		if(!cache.isSuperAdmin())
			window = loader.filterWindow(window, cache.getRoles());
		AlertDataAccess ada = new AlertDataAccess();
		alerts = ada.getAlertByScreen(window);
		
		//	Get action type
		Map<String,Object> params =FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		
		if(params.containsKey("inProcess") && params.get("inProcess").equals("true"))
			inProcess = true;
		else
			inProcess = false;
		
		if(inProcess){
			SAtom atom = (SAtom)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("adesc");
			windowDescription = atom.getDescription();
			windowDescription = atom.getDescription().replaceAll("MANDATORY", "");
		}
		
		MTMService service = new MTMService();
		CBusinessClass entity = new CBusinessClass();
		entity.setDataReference(window.getMainEntity());
				
		entity = pde.getReferencedTable(window.getMainEntity());
		
		HistoryDataAccess hda = new HistoryDataAccess();
		setHistory(hda.checkForHistory(entity));
		//histomode = (h.getId()>0);
		
		//	check if this form is in a unique row mode
		//	Load identification row insatance
		identificationRow = loader.chechForIdentificationRwSystem(window.getMainEntity());
		uniqueRowMode = (identificationRow != null);
		Map<String, Object> sesmap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		//	Composition
		CompositionDataAccess cda = new CompositionDataAccess();
		composition = cda.loadComposition(referenceBC);
		if(composition.getId()>0){
			
			composition.setEntity(referenceBC);
			for(CAttribute a : window.getCAttributes())
				if(a.getId() == composition.getRuleAttribute().getId()){
					composition.setRuleAttribute(a);
					break;
				}
			
			composables = cda.loadComposableBeans(composition);
			compositionMode = true;
			sourceComposition = new ArrayList<String>();
			targetComposition = new ArrayList<String>();
			
			for(PairKVElement e : composables){
				sourceComposition.add(e.getValue());
			}
			
			toCompose = new DualListModel<String>(sourceComposition, targetComposition);
		}
			
		if(params.containsKey("action") && params.get("action").equals("update")){
			action = "update";
			insert=false;
			navigation = true;
			Boolean am = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ALPHAMODE");
			if(am){
				alphaMode = am.booleanValue();
				alphaEntity = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ALPHAENTITY");
				alphaReference = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ALPHAREFERENCE");
			}
			
			dbID = Integer.parseInt((String)params.get("rowID"));
			System.out.println("Loading data for DBID "+dbID);
			
			// clean window from filtering attributes
			List<CAttribute> toRemove = new ArrayList<CAttribute>();
			for(CAttribute a : window.getCAttributes()){
				if(loader.embeds(window.getMainEntity(), a.getEntity().getDataReference()))
					toRemove.add(a);
			}
			
			window.getCAttributes().removeAll(toRemove);
			
			ProtogenDataEngine engine = new ProtogenDataEngine();
			initialData = engine.getDataByID(dbID, window);
			
			singleMode = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("SINGLEMODE")!= null 
					&& (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("SINGLEMODE");
			
			List<MtmDTO> dtos = service.getMtmFull(entity, dbID); 
			mtmBlocks = populate(dtos);
			if(mtmBlocks!=null && mtmBlocks.size()>0){
				mtmEnabled = true;
				mtmBlock= mtmBlocks.get(0);
				populateFullBlock(mtmBlock);
			}
			
			Boolean cf = false;
			if(sesmap.containsKey(ProtogenConstants.CONTEXT_FOUND))
				cf = (Boolean)sesmap.get(ProtogenConstants.CONTEXT_FOUND);
			if(cf)
			{
				FormContext fc = (FormContext)sesmap.get(ProtogenConstants.CONTEXT_FORM);
				controlLines = fc.getControls();
				controls = new ArrayList<UIControlElement>();
				for(UIControlSingle s : controlLines.getCtlines())
					for(UIControlElement ec : s.getControls())
						controls.add(ec);
			}
			else
				loadComponents();
			List<CAttribute> attrsToHide = alimenterTabsRef();
			for(UIControlElement ctrl : controls) {
				ctrl.setVisible(true);
				for(CAttribute at : attrsToHide) {
					if(at.getEntity().getDataReference().equals(ctrl.getAttribute().getEntity().getDataReference())
							&& at.getDataReference().equals(ctrl.getAttribute().getDataReference())
					) {
						ctrl.setVisible(false);
						ctrl.setHide(true);
					}
					if(ctrl.getAttribute().getDataReference().startsWith("fk_") && !ctrl.getAttribute().isReference()) {
						ctrl.setVisible(false);
						ctrl.setHide(true);	
					}
				}
			}
			
			refBlocks = populateFromRefsForUpdate(mtmBlocks, window.getTabsReferences());
			if(compositionMode){
				//	Get all components
				composedBean = cda.loadCompnents(dbID, composition, composables);
				
				//	Update dual list
				for(CComposingeBean b : composedBean.getComposition()){
					for(String s : toCompose.getSource())
						if(s.equals(b.getLibelle())){
							toCompose.getTarget().add(s);
							break;
						}
					
				}
				
				toCompose.getSource().removeAll(toCompose.getTarget());
			}
		} else if(params.containsKey("action") && params.get("action").equals("insert")) {
			action = "insert";
			insert=true;
			navigation=false;
			List<MtmDTO> dtos = service.getMtm(window,entity); 
			mtmBlocks = populate(dtos);
			if(mtmBlocks!=null && mtmBlocks.size()>0){
				mtmBlock= mtmBlocks.get(0);
				populateBlock(mtmBlock);
			}
			
			alimenterTabsRef();
			refBlocks = populateFromRefs(mtmBlocks, window.getTabsReferences());
			Boolean cf = false;
			if(sesmap.containsKey(ProtogenConstants.CONTEXT_FOUND))
				cf = (Boolean)sesmap.get(ProtogenConstants.CONTEXT_FOUND);
			if(cf)
			{
				FormContext fc = (FormContext)sesmap.get(ProtogenConstants.CONTEXT_FORM);
				controlLines = fc.getControls();
				controls = new ArrayList<UIControlElement>();
				for(UIControlSingle s : controlLines.getCtlines())
					for(UIControlElement ec : s.getControls())
						controls.add(ec);
			}
			else
				loadVoidComponents();
			loadVoidRefComponenets();
		}
		
		if(window.getRappelReference() != null && window.getRappelReference().getId()>0){
			rappelHistory = new ArrayList<UIControlElement>();
			int idReference=0;
			String rappelReference = "";
			for(UIControlElement c : controls){
				if(c.getAttribute().equals(window.getRappelReference())){
					if(c.getControlValue() != null && c.getControlValue().length()>0){
						idReference = Integer.parseInt(c.getControlValue());
						rappelActivated = true;
					}
					rappelReference = c.getAttribute().getDataReference();
					break;
				}
			}
			
			if(idReference>0){
				String tableReference = window.getMainEntity();
				for(CAttribute a : window.getCAttributes()){
					if(!a.isRappel())
						continue;
					
					String adr = a.getDataReference();
					ProtogenDataEngine engine = new ProtogenDataEngine();
					String value = engine.getDatumByReferenceID(tableReference, adr, rappelReference,idReference);
					
					UIControlElement rctrl = new UIControlElement();
					rctrl.setControlID(a.getAttribute());
					if(value == null || value.length()==0 || value.equals("null"))
						value="";
					rctrl.setControlValue(value);
					rappelHistory.add(rctrl);
					
				}
			}
		}
		
		globalControls = new ArrayList<UIControlElement>();
		for(CGlobalValue v : window.getGlobalValues()){
			UIControlElement e = new UIControlElement();
			e.setControlID(v.getId()+"");
			e.setLabel(v.getLabel());
			e.setControlValue(v.getValue());
			globalControls.add(e);
			controlLines.addControl(e);
		}
		
		for(UIControlElement e : controls){
			titles.add(e.getControlID());
		}
		FrontController front = (FrontController)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FRONT_CTRL");
		if(inProcess && windowDescription!= null && windowDescription.length()>0){

			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(false);
			FacesContext.getCurrentInstance().getExternalContext().getFlash().clear();
			if(front.isKeepAlive())
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,windowTitle,windowDescription));
		}
		front.setKeepAlive(!front.isKeepAlive());
		
		prepareRolesProfiles(cache);
		
		//	Check for localization mode
		
		localizedEntity = odal.checkForLocalizationMode(cache.getOrganization(),window.getMainEntity());
		localizedMode = (localizedEntity.getId() > 0);
		if(localizedMode){
			organisations = odal.loadOrganizations(cache.getOrganization());
			prepareTree(cache.getOrganization());
		}
		
		checkingChangeListener();
	}
	
	public List<String> autocompleteMethod(String keyWords){
		List<String> results = new ArrayList<String>();
		FacesContext context = FacesContext.getCurrentInstance();
		String dref = (String)UIComponent.getCurrentComponent(context).getAttributes().get("CTRL");
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		List<String> keys = new ArrayList<String>();
		keyWords = keyWords.replaceAll(";"," ");
		String[] keysTable = keyWords.split(" ");
		for(String  k : keysTable)
			keys.add(k.toLowerCase());
		
		Map<Integer, String> vals = pde.getDataKeys(dref.substring(3), 
				new ArrayList<String>(), keys);
		for(String v : vals.values())
			results.add(v);
		
		return results;
	}
	
	public void activeIndexChanged(TabChangeEvent evt){
		String sid = evt.getTab().getId();
		
		for(MtmBlock b : mtmBlocks){
			String id = "tabid_"+b.getEntityID();
			if(id.equals(sid)){
				activeIndex = mtmBlocks.indexOf(b);
				break;
			}
		}
	}
	
	public void updateActiveBlock(){
		int id = Integer.parseInt(
					FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedMTM")
				);
		for(MtmBlock b : mtmBlocks)
			if(b.getEntityID() == id){
				mtmBlock = b;
				activeIndex = mtmBlocks.indexOf(b);
				break;
			}
		if(insert)
			populateBlock(mtmBlock);
		else
			populateFullBlock(mtmBlock);
	}
	
	public void updateDDList(){
		int id = Integer.parseInt(
					FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("CTRL_ATT_ID")
				);
		int lineIndex = Integer.parseInt(
				FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("LINE_INDEX")
			);
	
		mtmLine = mtmBlock.getLines().get(lineIndex);
		for(UIControlElement c : mtmLine.getControls())
			if(c.getAttribute().getId() == id){
				uictrl = c;
				break;
			}
			
	}
	
	@SuppressWarnings("unchecked")
	public String nextButton(){
		int nextId = dbID;
		
		List<Integer> ids = (List<Integer>) FacesContext.getCurrentInstance().
				getExternalContext().getSessionMap().get(ProtogenConstants.DATA_IDS);
		
		for(int i = 0 ; i < ids.size() ; i++)
			if(ids.get(i).intValue() == dbID && i < (ids.size()-1)){
				nextId = ids.get(i+1);
				break;
			}
		
		dbID = 0;
		dbID = nextId;
		
		System.out.println("Current DBID A : " + dbID);
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("rowID", ""+dbID);
		
		action = "update";
		insert=false;
		navigation = true;
		Boolean am = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ALPHAMODE");
		if(am){
			alphaMode = am.booleanValue();
			alphaEntity = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ALPHAENTITY");
			alphaReference = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ALPHAREFERENCE");
		}
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		initialData = new DBFormattedObjects();

		initialData = engine.getDataByID(dbID, window);
		MTMService service = new MTMService();
		CBusinessClass entity = new CBusinessClass();
		entity.setDataReference(window.getMainEntity());
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		entity = pde.getReferencedTable(window.getMainEntity());
		
		List<MtmDTO> dtos = service.getMtmFull(entity, dbID); 
		mtmBlocks = populate(dtos);
		loadComponents();
		List<CAttribute> attrsToHide = alimenterTabsRef();
		for(UIControlElement ctrl : controls) {
			ctrl.setVisible(true);
			for(CAttribute at : attrsToHide) {
				if(at.getEntity().getDataReference().equals(ctrl.getAttribute().getEntity().getDataReference())
						&& at.getDataReference().equals(ctrl.getAttribute().getDataReference())
				) {
					ctrl.setVisible(false);
					ctrl.setHide(true);
				}
				if(ctrl.getAttribute().getDataReference().startsWith("fk_") && !ctrl.getAttribute().isReference()) {
					ctrl.setVisible(false);
					ctrl.setHide(true);	
				}
			}
		}
		refBlocks = populateFromRefsForUpdate(mtmBlocks, window.getTabsReferences());
		CompositionDataAccess cda = new CompositionDataAccess();
		
		if(compositionMode){
			//	Get all components
			composedBean = cda.loadCompnents(dbID, composition, composables);
			
			//	Update dual list
			for(CComposingeBean b : composedBean.getComposition()){
				for(String s : toCompose.getSource())
					if(s.equals(b.getLibelle())){
						toCompose.getTarget().add(s);
						break;
					}
				
			}
			
			toCompose.getSource().removeAll(toCompose.getTarget());
		}
		
		System.out.println("Current DBID B : " + dbID);
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public String backButton(){
		
		int backId = dbID;
		List<Integer> ids = (List<Integer>) FacesContext.getCurrentInstance().
				getExternalContext().getSessionMap().get(ProtogenConstants.DATA_IDS);
		
		for(int i = 0 ; i < ids.size() ; i++)
			if(ids.get(i).intValue() == dbID && i >0){
				backId = ids.get(i-1);
				break;
			}
		dbID = 0;
		dbID = backId;
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("rowID", ""+dbID);
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		initialData = new DBFormattedObjects();
		initialData = engine.getDataByID(dbID, window);
		MTMService service = new MTMService();
		CBusinessClass entity = new CBusinessClass();
		entity.setDataReference(window.getMainEntity());
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		entity = pde.getReferencedTable(window.getMainEntity());
		
		List<MtmDTO> dtos = service.getMtmFull(entity, dbID); 
		mtmBlocks = populate(dtos);
		loadComponents();
		List<CAttribute> attrsToHide = alimenterTabsRef();
		for(UIControlElement ctrl : controls) {
			ctrl.setVisible(true);
			for(CAttribute at : attrsToHide) {
				if(at.getEntity().getDataReference().equals(ctrl.getAttribute().getEntity().getDataReference())
						&& at.getDataReference().equals(ctrl.getAttribute().getDataReference())
				) {
					ctrl.setVisible(false);
					ctrl.setHide(true);
				}
				if(ctrl.getAttribute().getDataReference().startsWith("fk_") && !ctrl.getAttribute().isReference()) {
					ctrl.setVisible(false);
					ctrl.setHide(true);	
				}
			}
		}
		refBlocks = populateFromRefsForUpdate(mtmBlocks, window.getTabsReferences());
		CompositionDataAccess cda = new CompositionDataAccess();
		
		if(compositionMode){
			//	Get all components
			composedBean = cda.loadCompnents(dbID, composition, composables);
			
			//	Update dual list
			for(CComposingeBean b : composedBean.getComposition()){
				for(String s : toCompose.getSource())
					if(s.equals(b.getLibelle())){
						toCompose.getTarget().add(s);
						break;
					}
				
			}
			
			toCompose.getSource().removeAll(toCompose.getTarget());
		}
		
		System.out.println("Current DBID : " + dbID);
		return "";
	}
	
	private void loadVoidRefComponenets() {
		for(UIControlElement ctrl : controls) {
			ctrl.setVisible(true);
			for(MtmBlock block : refBlocks) {
				for(UIControlElement at : block.getControls()) {
					
					if(at.getAttribute().getEntity().getDataReference().equals(ctrl.getAttribute().getEntity().getDataReference())
							&& at.getAttribute().getDataReference().equals(ctrl.getAttribute().getDataReference())
					) {
						ctrl.setVisible(false);
						ctrl.setHide(true);
						ctrl.getAttribute().setMandatory(false);
//						ctrl.setControlValue("");
//						at.setControlValue("");
					}
					if(ctrl.getAttribute().getDataReference().startsWith("fk_") && !ctrl.getAttribute().isReference()) {
						ctrl.setVisible(false);
						ctrl.setHide(true);
						synchronized (window.getCAttributes()) {
							window.getCAttributes().remove(ctrl.getAttribute());
						}
					}
				}
			}
		}
		
	}

	private List<MtmBlock> populateFromRefsForUpdate(List<MtmBlock> mtmBlocks2, Map<String, List<CAttribute>> refTabs) {
		List<MtmBlock> results = new ArrayList<MtmBlock>();
		
		for(String key : refTabs.keySet()){
			boolean existsWithinMtmBlocks = false;
			MtmBlock block = new MtmBlock();
			block.setLines(new ArrayList<MtmLine>());
			block.setTitles(new ArrayList<String>());
			
			for(MtmBlock mblk : mtmBlocks2) {
				if(key.equals(mblk.getEntity().getDataReference())){
					existsWithinMtmBlocks = true;
					break;
				}
			}
			
			if(existsWithinMtmBlocks) {
				continue;
			}
			
			block.setEntity(refTabs.get(key).get(0).getEntity());
			block.setEntityID(block.getEntity().getId());
			block.setControls(new ArrayList<UIControlElement>());
			ProtogenDataEngine engine = new ProtogenDataEngine();
			
			for(CAttribute a : refTabs.get(key)) {
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				if(a.isAutoValue())
					continue;
				if(!a.isReference()){
					if(a.getCAttributetype().getId() == 3){ //	Date
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(a.getAttribute());
						e.setCtrlDate(true);
						String dval = initialData.getOtmEntities().get(a.getEntity().getName()).get(a.getAttribute());
						String[] dateVals = dval.split("-");
						if(dateVals.length>1){
							e.setControlValue(dval.split("-")[2]+"/"+dval.split("-")[1]+"/"+dval.split("-")[0]);
						}else {
							e.setControlValue("");
						}
						block.getControls().add(e);
					} else {
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(a.getAttribute());
						e.setControlValue(initialData.getOtmEntities().get(a.getEntity().getName()).get(a.getAttribute()));
						block.getControls().add(e);
					}
				} else {
					UIControlElement element = new UIControlElement();
					
					ApplicationLoader dal = new ApplicationLoader();
					CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
					ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
					List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),cache.getUser().getId());
					UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
					element.setInlineControlLines(inlines);
					element.setAttribute(a);
					element.setControlID(a.getDataReference());
					element.setLabel(a.getAttribute().replaceAll("ID ",""));
					element.setControlValue("");
					element.setListReference(listElements);
					element.setReference(true);
					block.getControls().add(element);
				}
			}
			results.add(block);
		}
		return results;
	}

	private List<CAttribute> alimenterTabsRef() {
		List<CAttribute> indexes = new ArrayList<CAttribute>();
		for(CAttribute attribute : window.getCAttributes()) {
			String attributeEntityRef = attribute.getEntity().getDataReference();
			
			if(!attributeEntityRef.equals(window.getMainEntity()) && !attribute.isRappel()) {
				if(window.getTabsReferences().get(attributeEntityRef) == null) {
					window.getTabsReferences().put(attributeEntityRef, new ArrayList<CAttribute>());
				}
				window.getTabsReferences().get(attributeEntityRef).add(attribute);
				indexes.add(attribute);
				
			} 
		}
		return indexes;
	}
	
	
	public void dummyListener(){
		String dummy = "";
		dummy = dummy+"";
		
	}
	
	private void prepareTree(GOrganization rorg) {
		GOrganization rootor = new GOrganization();
		rootor.setName("Organisation");
		root = new DefaultTreeNode(rootor, null);
		TreeNode node = new DefaultTreeNode(rorg,root);
		orgMap.put(rorg, node);
		for(GOrganization o : organisations){
			GOrganization p = o.getParent();
			TreeNode parent = orgMap.get(p);
			TreeNode n = new DefaultTreeNode(o, parent);
			orgMap.put(o, n);
		}
		
		if(insert)
			return;
		
		OrganizationDAL odal = new OrganizationDAL();
		int idOrg = odal.loadLocalization(localizedEntity.getId(),dbID);
		GOrganization selorg = rorg;
		for(GOrganization o : organisations)
			if(o.getId() == idOrg){
				selorg = o;
				break;
			}
		selectedOrg = orgMap.get(selorg);
	}

	@SuppressWarnings("unused")
	public void onOrgSelect(){
		Object o = selectedOrg;
		
	}
	
	public void modelParametrageChanged(){
		for(GParametersComponent pc : parametersComponents){
			if(pc.getPkg().getNom().equals(selectedComponentName)){
				selectedPackage =  pc.getPkg();
				selectedComponent = pc;
				break;
			}
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
       	try {
       		for(UIControlElement e : controls)
       			if(e.isBinaryContent()){
       				uploadTo = e;
       				break;	
       			}
       				
       		InputStream is = event.getFile().getInputstream();
   			int length = (int)event.getFile().getSize();
			if(length>0)
				uploadTo.setNonVoidContent(true);
			
			FacesContext fc = FacesContext.getCurrentInstance();
		    ExternalContext ec = fc.getExternalContext();
		    
		    String tempfile = ec.getRealPath("")+"/tmp/"+UUID.randomUUID().toString()+"."+uploadTo.getAttribute().getFileExtension();
		    File f = new File(tempfile);
		    File directory = new File(ec.getRealPath("")+"/tmp");
		    if(!directory.exists()) {
		    	directory.mkdir();
		    }
		    if(!f.exists())
		    	f.createNewFile();
   			uploadTo.setContent(tempfile);
   			
   			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("TMP_FILE_"+uploadTo.getAttribute().getDataReference(), tempfile);
			
		    byte[] buffer = new byte[1024];
		    int len;
		    OutputStream os = new FileOutputStream(tempfile);
		    
		    while ((len = is.read(buffer)) > -1 ) {
		        os.write(buffer, 0, len);
		    }
		    os.close();
		    os.flush();

		    
   			String mime="";
   			if(uploadTo.getAttribute().getFileExtension().toLowerCase().equals("pdf"))
   				mime = "application/pdf";
   			if(uploadTo.getAttribute().getFileExtension().toLowerCase().equals("doc") )
   				mime = "application/msword";
   			if(uploadTo.getAttribute().getFileExtension().toLowerCase().equals("docx") )
   				mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			uploadTo.setFile(new DefaultStreamedContent(is,mime,uploadTo.getAttribute().getFileName()+"."+uploadTo.getAttribute().getFileExtension()));
			fileFieldInprogress = false;
   		} catch (Exception e) {
   			e.printStackTrace();
   		}
    }
	
	public void prepareBinaryContent(){
		
		fileFieldInprogress = true;
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int aid = Integer.parseInt(params.get("ngoID"));
		
		for(UIControlElement e : controls)
			if(e.getAttribute().getId() == aid){
				uploadTo = e;
			}
		
		String str = uploadTo.getControlID();
		str = str+"";
	}
	
	//	Graphic layout
	
	private void populateFullBlock(MtmBlock block){
		block.setVisited(true);
		MtmDTO dto = block.getDto();
		ProtogenDataEngine pde = new ProtogenDataEngine();
		if(block.getLines().size() != 0)
			return;
		
		for(Map<CAttribute, Object> dataLine : dto.getMtmData()){
			MtmLine line = new MtmLine();
			line.setKey(block.getEntityID()+"-");
			line.setValues(new ArrayList<PairKVElement>());
			for(CAttribute a : dataLine.keySet()){
				if(a.getDataReference().startsWith("pk_")){
					line.setKey(block.getEntityID()+"-"+dataLine.get(a).toString());
					continue;
				}
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				if(a.isAutoValue())
					continue;
				String val;
				if (dataLine.get(a) != null)
					val = dataLine.get(a).toString();
				else 
					val = "";
				PairKVElement pkv=new PairKVElement(a.getDataReference(), val);
				pkv.setAttribute(a);
				pkv.setReference(false);
				if(a.getDataReference().startsWith("fk_")){
					pkv.setReference(true);
					List<PairKVElement> liste = pde.getDataKeys(a.getDataReference().substring(3), false, 0);
					pkv.setListReferences(liste);
					
				}
				
				if(a.getCAttributetype().getId() == 3){
					pkv.setDate(true);
					if(val != null && val.length()>0){
						String dval = val.split(" ")[0];
						int day = Integer.parseInt(dval.split("-")[2]);
						int month= Integer.parseInt(dval.split("-")[1]);
						int year = Integer.parseInt(dval.split("-")[0]);
						Calendar c = Calendar.getInstance();
						c.set(Calendar.DAY_OF_MONTH, day);
						c.set(Calendar.MONTH, month-1);
						c.set(Calendar.YEAR, year);
						pkv.setDateValue(c.getTime());
						pkv.setFormattedDateValue(dval.split("-")[2]+"/"+dval.split("-")[1]+"/"+dval.split("-")[0]);
					} else {
						Calendar c = Calendar.getInstance();
						pkv.setDateValue(c.getTime());
						pkv.setFormattedDateValue(c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR));
					}
				}
				line.getValues().add(pkv);
			}
			line.setTemporary(false);
			line.setControls(new ArrayList<UIControlElement>());
			block.getLines().add(line);
		}
		
//		constructing form
				block.setControls(new ArrayList<UIControlElement>());
				ProtogenDataEngine engine = new ProtogenDataEngine();
				boolean update = action.equals("update");
				for(CAttribute a : dto.getMtmEntity().getAttributes()){
					if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
						continue;
					if(a.isAutoValue())
						continue;
					
					if(!a.isReference()){
						if(a.getCAttributetype().getId() == 3){ //	Date
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setCtrlDate(true);
							e.setControlValue("");
							block.getControls().add(e);
						} else if (a.getCAttributetype().getId() == 12) {	//Boolean
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setBooleanValue(false);
							e.setControlValue("Non");
							block.getControls().add(e);
						} else {
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setControlValue("");
							block.getControls().add(e);
						}
					} else {
						UIControlElement element = new UIControlElement();
						
						ApplicationLoader dal = new ApplicationLoader();
						CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
						
						List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),0);
						
						UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
						element.setInlineControlLines(inlines);
						element.setAttribute(a);
						element.setControlID(a.getDataReference());
						element.setLabel(a.getAttribute().replaceAll("ID ",""));
						element.setControlValue("");
						element.setListReference(listElements);
						element.setReference(true);
						block.getControls().add(element);
					}
					
				}
				
				for(MtmLine l : block.getLines()){
					for(CAttribute a : dto.getMtmEntity().getAttributes()){
						if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
							continue;
						if(a.isAutoValue())
							continue;
						if(!a.isReference()){
							if(a.getCAttributetype().getId() == 3){ //	Date
								UIControlElement e = new UIControlElement();
								e.setAttribute(a);
								e.setControlID(a.getDataReference());
								e.setLabel(a.getAttribute());
								e.setCtrlDate(true);
								e.setControlValue("");
								
								if(update){
									for(PairKVElement pkv : l.getValues()){
										if(pkv.getAttribute().getId() == a.getId()){
											e.setDateValue(pkv.getDateValue());
											break;
										}
									}
								}
								
								l.getControls().add(e);
							} else if (a.getCAttributetype().getId() == 12) {	//Boolean
								UIControlElement e = new UIControlElement();
								e.setAttribute(a);
								e.setControlID(a.getDataReference());
								e.setLabel(a.getAttribute());
								e.setBooleanValue(false);
								e.setControlValue("Non");
								
								if(update){
									for(PairKVElement pkv : l.getValues()){
										if(pkv.getAttribute().getId() == a.getId()){
											e.setBooleanValue(pkv.getValue().equals("Non"));
											e.setControlValue(pkv.getValue());
											break;
										}
									}
								}
								
								
								l.getControls().add(e);
							} else {
								UIControlElement e = new UIControlElement();
								e.setAttribute(a);
								e.setControlID(a.getDataReference());
								e.setLabel(a.getAttribute());
								e.setControlValue("");
								l.getControls().add(e);
								
								if(update){
									for(PairKVElement pkv : l.getValues()){
										if(pkv.getAttribute().getId() == a.getId()){
											e.setControlValue(pkv.getValue());
											break;
										}
									}
								}
							}
						} else {
							UIControlElement element = new UIControlElement();
							
							ApplicationLoader dal = new ApplicationLoader();
							CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
							
							List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),0);
							
							UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
							element.setInlineControlLines(inlines);
							element.setAttribute(a);
							element.setControlID(a.getDataReference());
							element.setLabel(a.getAttribute().replaceAll("ID ",""));
							element.setControlValue("");
							element.setListReference(listElements);
							element.setReference(true);
							String uid = block.getEntityID()+"_"+a.getId()+"_"+block.getLines().indexOf(l);
							element.setUniqueID(uid);
							
							if(update){
								for(PairKVElement pkv : l.getValues()){
									if(pkv.getAttribute().getId() == a.getId()){
										element.setTrueValue(pkv.getValue());
										break;
									}
								}
							}
							
							l.getControls().add(element);
						}
						
					}
				}
	}
	
	private void populateBlock(MtmBlock block){
		block.setVisited(true);
		MtmDTO dto = block.getDto();
		if(block.getLines().size() != 0)
			return;
		
		for (int i=0 ; i < 1 ; i++){
			MtmLine line = new MtmLine();
			line.setKey(block.getEntityID()+"-"+i);
			line.setValues(new ArrayList<PairKVElement>());
			line.setTemporary(true);
			line.setControls(new ArrayList<UIControlElement>());
			line.setId(0);
			for(CAttribute a : dto.getMtmEntity().getAttributes()){
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				PairKVElement pkv=new PairKVElement(a.getDataReference(), "");
				line.getValues().add(pkv);
			}
			block.getLines().add(line);
		}
		
//		constructing form
				block.setControls(new ArrayList<UIControlElement>());
				ProtogenDataEngine engine = new ProtogenDataEngine();
				boolean update = action.equals("update");
				for(CAttribute a : dto.getMtmEntity().getAttributes()){
					if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
						continue;
					if(a.isAutoValue())
						continue;
					
					if(!a.isReference()){
						if(a.getCAttributetype().getId() == 3){ //	Date
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setCtrlDate(true);
							e.setControlValue("");
							block.getControls().add(e);
						} else if (a.getCAttributetype().getId() == 12) {	//Boolean
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setBooleanValue(false);
							e.setControlValue("Non");
							block.getControls().add(e);
						} else {
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setControlValue("");
							block.getControls().add(e);
						}
					} else {
						UIControlElement element = new UIControlElement();
						
						ApplicationLoader dal = new ApplicationLoader();
						CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
						
						List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),0);
						
						UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
						element.setInlineControlLines(inlines);
						element.setAttribute(a);
						element.setControlID(a.getDataReference());
						element.setLabel(a.getAttribute().replaceAll("ID ",""));
						element.setControlValue("");
						element.setListReference(listElements);
						element.setReference(true);
						block.getControls().add(element);
					}
					
				}
				
				for(MtmLine l : block.getLines()){
					for(CAttribute a : dto.getMtmEntity().getAttributes()){
						if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
							continue;
						if(a.isAutoValue())
							continue;
						if(!a.isReference()){
							if(a.getCAttributetype().getId() == 3){ //	Date
								UIControlElement e = new UIControlElement();
								e.setAttribute(a);
								e.setControlID(a.getDataReference());
								e.setLabel(a.getAttribute());
								e.setCtrlDate(true);
								e.setControlValue("");
								
								if(update){
									for(PairKVElement pkv : l.getValues()){
										if(pkv.getAttribute().getId() == a.getId()){
											e.setDateValue(pkv.getDateValue());
											break;
										}
									}
								}
								
								l.getControls().add(e);
							} else if (a.getCAttributetype().getId() == 12) {	//Boolean
								UIControlElement e = new UIControlElement();
								e.setAttribute(a);
								e.setControlID(a.getDataReference());
								e.setLabel(a.getAttribute());
								e.setBooleanValue(false);
								e.setControlValue("Non");
								
								if(update){
									for(PairKVElement pkv : l.getValues()){
										if(pkv.getAttribute().getId() == a.getId()){
											e.setBooleanValue(pkv.getValue().equals("Non"));
											e.setControlValue(pkv.getValue());
											break;
										}
									}
								}
								
								
								l.getControls().add(e);
							} else {
								UIControlElement e = new UIControlElement();
								e.setAttribute(a);
								e.setControlID(a.getDataReference());
								e.setLabel(a.getAttribute());
								e.setControlValue("");
								l.getControls().add(e);
								
								if(update){
									for(PairKVElement pkv : l.getValues()){
										if(pkv.getAttribute().getId() == a.getId()){
											e.setControlValue(pkv.getValue());
											break;
										}
									}
								}
							}
						} else {
							UIControlElement element = new UIControlElement();
							
							ApplicationLoader dal = new ApplicationLoader();
							CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
							
							List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),0);
							
							UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
							element.setInlineControlLines(inlines);
							element.setAttribute(a);
							element.setControlID(a.getDataReference());
							element.setLabel(a.getAttribute().replaceAll("ID ",""));
							element.setControlValue("");
							element.setListReference(listElements);
							element.setReference(true);
							String uid = block.getEntityID()+"_"+a.getId()+"_"+block.getLines().indexOf(l);
							element.setUniqueID(uid);
							
							if(update){
								for(PairKVElement pkv : l.getValues()){
									if(pkv.getAttribute().getId() == a.getId()){
										element.setTrueValue(pkv.getValue());
										break;
									}
								}
							}
							
							l.getControls().add(element);
						}
						
					}
				}
	}
	
	private List<MtmBlock> populate(List<MtmDTO> dtos) {
		
		List<MtmBlock> results = new ArrayList<MtmBlock>();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		LocalizationEngine translator = new LocalizationEngine();
		
		
		for(MtmDTO dto : dtos){
			MtmBlock block = new MtmBlock();
			block.setDto(dto);
			block.setEntity(dto.getMtmEntity());
			block.getEntity().setName(translator.entityTranslate(block.getEntity().getName(), block.getEntity().getId(), u.getLanguage()));
			block.setEntityID(dto.getMtmEntity().getId());
			block.setLines(new ArrayList<MtmLine>());
			block.setTitles(new ArrayList<String>());
			
			for(CAttribute a : dto.getMtmEntity().getAttributes()){
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				if(a.isAutoValue())
					continue;
				block.getTitles().add(translator.attributeTranslate(a.getAttribute(), a.getId(), u.getLanguage()));
			}
			
			/*if(action.equals("update") )
				for(Map<CAttribute, Object> dataLine : dto.getMtmData()){
					MtmLine line = new MtmLine();
					line.setKey(block.getEntityID()+"-");
					line.setValues(new ArrayList<PairKVElement>());
					for(CAttribute a : dataLine.keySet()){
						if(a.getDataReference().startsWith("pk_")){
							line.setKey(block.getEntityID()+"-"+dataLine.get(a).toString());
							continue;
						}
						if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
							continue;
						if(a.isAutoValue())
							continue;
						String val;
						if (dataLine.get(a) != null)
							val = dataLine.get(a).toString();
						else 
							val = "";
						PairKVElement pkv=new PairKVElement(a.getDataReference(), val);
						pkv.setAttribute(a);
						pkv.setReference(false);
						if(a.getDataReference().startsWith("fk_")){
							pkv.setReference(true);
							List<PairKVElement> liste = pde.getDataKeys(a.getDataReference().substring(3), false, 0);
							pkv.setListReferences(liste);
							
						}
						
						if(a.getCAttributetype().getId() == 3){
							pkv.setDate(true);
							if(val != null && val.length()>0){
								String dval = val.split(" ")[0];
								int day = Integer.parseInt(dval.split("-")[2]);
								int month= Integer.parseInt(dval.split("-")[1]);
								int year = Integer.parseInt(dval.split("-")[0]);
								Calendar c = Calendar.getInstance();
								c.set(Calendar.DAY_OF_MONTH, day);
								c.set(Calendar.MONTH, month-1);
								c.set(Calendar.YEAR, year);
								pkv.setDateValue(c.getTime());
								pkv.setFormattedDateValue(dval.split("-")[2]+"/"+dval.split("-")[1]+"/"+dval.split("-")[0]);
							} else {
								Calendar c = Calendar.getInstance();
								pkv.setDateValue(c.getTime());
								pkv.setFormattedDateValue(c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR));
							}
						}
						line.getValues().add(pkv);
					}
					line.setTemporary(false);
					line.setControls(new ArrayList<UIControlElement>());
					block.getLines().add(line);
				}
			else {
				for (int i=0 ; i < 1 ; i++){
					MtmLine line = new MtmLine();
					line.setKey(block.getEntityID()+"-"+i);
					line.setValues(new ArrayList<PairKVElement>());
					line.setTemporary(true);
					line.setControls(new ArrayList<UIControlElement>());
					line.setId(0);
					for(CAttribute a : dto.getMtmEntity().getAttributes()){
						if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
							continue;
						PairKVElement pkv=new PairKVElement(a.getDataReference(), "");
						line.getValues().add(pkv);
					}
					block.getLines().add(line);
				}
			}
			//	constructing form
			block.setControls(new ArrayList<UIControlElement>());
			ProtogenDataEngine engine = new ProtogenDataEngine();
			boolean update = action.equals("update");
			for(CAttribute a : dto.getMtmEntity().getAttributes()){
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				if(a.isAutoValue())
					continue;
				
				if(!a.isReference()){
					if(a.getCAttributetype().getId() == 3){ //	Date
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(a.getAttribute());
						e.setCtrlDate(true);
						e.setControlValue("");
						block.getControls().add(e);
					} else if (a.getCAttributetype().getId() == 12) {	//Boolean
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(a.getAttribute());
						e.setBooleanValue(false);
						e.setControlValue("Non");
						block.getControls().add(e);
					} else {
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(a.getAttribute());
						e.setControlValue("");
						block.getControls().add(e);
					}
				} else {
					UIControlElement element = new UIControlElement();
					
					ApplicationLoader dal = new ApplicationLoader();
					CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
					
					List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),cache.getUser().getId());
					
					UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
					element.setInlineControlLines(inlines);
					element.setAttribute(a);
					element.setControlID(a.getDataReference());
					element.setLabel(a.getAttribute().replaceAll("ID ",""));
					element.setControlValue("");
					element.setListReference(listElements);
					element.setReference(true);
					block.getControls().add(element);
				}
				
			}
			
			for(MtmLine l : block.getLines()){
				for(CAttribute a : dto.getMtmEntity().getAttributes()){
					if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
						continue;
					if(a.isAutoValue())
						continue;
					if(!a.isReference()){
						if(a.getCAttributetype().getId() == 3){ //	Date
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setCtrlDate(true);
							e.setControlValue("");
							
							if(update){
								for(PairKVElement pkv : l.getValues()){
									if(pkv.getAttribute().getId() == a.getId()){
										e.setDateValue(pkv.getDateValue());
										break;
									}
								}
							}
							
							l.getControls().add(e);
						} else if (a.getCAttributetype().getId() == 12) {	//Boolean
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setBooleanValue(false);
							e.setControlValue("Non");
							
							if(update){
								for(PairKVElement pkv : l.getValues()){
									if(pkv.getAttribute().getId() == a.getId()){
										e.setBooleanValue(pkv.getValue().equals("Non"));
										e.setControlValue(pkv.getValue());
										break;
									}
								}
							}
							
							
							l.getControls().add(e);
						} else {
							UIControlElement e = new UIControlElement();
							e.setAttribute(a);
							e.setControlID(a.getDataReference());
							e.setLabel(a.getAttribute());
							e.setControlValue("");
							l.getControls().add(e);
							
							if(update){
								for(PairKVElement pkv : l.getValues()){
									if(pkv.getAttribute().getId() == a.getId()){
										e.setControlValue(pkv.getValue());
										break;
									}
								}
							}
						}
					} else {
						UIControlElement element = new UIControlElement();
						
						ApplicationLoader dal = new ApplicationLoader();
						CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
						
						List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),cache.getUser().getId());
						
						UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
						element.setInlineControlLines(inlines);
						element.setAttribute(a);
						element.setControlID(a.getDataReference());
						element.setLabel(a.getAttribute().replaceAll("ID ",""));
						element.setControlValue("");
						element.setListReference(listElements);
						element.setReference(true);
						String uid = block.getEntityID()+"_"+a.getId()+"_"+block.getLines().indexOf(l);
						element.setUniqueID(uid);
						
						if(update){
							for(PairKVElement pkv : l.getValues()){
								if(pkv.getAttribute().getId() == a.getId()){
									element.setTrueValue(pkv.getValue());
									break;
								}
							}
						}
						
						l.getControls().add(element);
					}
					
				}
			}
			*/
			results.add(block);
		}
		return results;
	}
	
	public void mtmLineEditionListener(RowEditEvent event){
		ProtogenDataEngine pde = new ProtogenDataEngine();
		MtmLine line = (MtmLine)event.getObject();
		int tableId = Integer.parseInt(line.getKey().split("-")[0]);
		int rowId = Integer.parseInt(line.getKey().split("-")[1]);
		MtmBlock block = null;
		for(MtmBlock b : mtmBlocks){
			if(b.getEntityID() == tableId){
				block = b;
				break;
			}
		}
		
		if(block == null)
			return;
		
		Map<CAttribute, String> values = new HashMap<CAttribute, String>();
		for(PairKVElement pkv : line.getValues()){
			if(pkv.isReference()){
				PairKVElement element = null;
				for(PairKVElement e : pkv.getListReferences()){
					if(e.getValue().equals(pkv.getValue())){
						element = e;
						break;
					}
				}
				
				if(element == null)
					continue;
				
				values.put(pkv.getAttribute(), element.getKey());
				continue;
			}
			if(pkv.isDate()){
				String formatedDate = "";
				Calendar c = Calendar.getInstance();
				c.setTime(pkv.getDateValue());
				formatedDate = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
				values.put(pkv.getAttribute(), formatedDate);
				pkv.setValue(formatedDate);
				pkv.setFormattedDateValue(c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR));
				continue;
			}
			if(pkv.getAttribute().getCAttributetype().getId() == 12){
				String v = pkv.isBooleanValue()?"Oui":"Non";
				values.put(pkv.getAttribute(), v);
				continue;
			}
			values.put(pkv.getAttribute(), pkv.getValue());
		}
		pde.executeUpdate(block.getEntity().getDataReference(), rowId, values);
		
	}
	
	private List<MtmBlock> populateFromRefs(List<MtmBlock> mtmBlocks2, Map<String, List<CAttribute>> refTabs) {
		List<MtmBlock> results = new ArrayList<MtmBlock>();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		LocalizationEngine translator = new LocalizationEngine();
		
		for(String key : refTabs.keySet()){
			boolean existsWithinMtmBlocks = false;
			MtmBlock block = new MtmBlock();
//			block.setEntity(dto.getMtmEntity());
//			block.setEntityID(dto.getMtmEntity().getId());
			block.setLines(new ArrayList<MtmLine>());
			block.setTitles(new ArrayList<String>());
			
			for(MtmBlock mblk : mtmBlocks2) {
				if(key.equals(mblk.getEntity().getDataReference())){
					existsWithinMtmBlocks = true;break;
				}
			}
			
			if(existsWithinMtmBlocks) {
				continue;
			}
			
			block.setEntity(refTabs.get(key).get(0).getEntity());
			block.getEntity().setName(translator.entityTranslate(block.getEntity().getName(), block.getEntity().getId(), u.getLanguage()));
			block.setEntityID(block.getEntity().getId());
			block.setControls(new ArrayList<UIControlElement>());
			ProtogenDataEngine engine = new ProtogenDataEngine();
			
			for(CAttribute a : refTabs.get(key)) {
				if(a.getDataReference().startsWith("fk_")) {
					a.setReference(true);
				}
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				if(a.isAutoValue())
					continue;
				if(!a.isReference()){
					if(a.getCAttributetype().getId() == 3){ //	Date
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(translator.attributeTranslate(a.getAttribute(), a.getId(), u.getLanguage()));
						e.setCtrlDate(true);
						e.setControlValue("");
						block.getControls().add(e);
					} else {
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(translator.attributeTranslate(a.getAttribute(), a.getId(), u.getLanguage()));
						e.setControlValue("");
						block.getControls().add(e);
					}
				} else {
					UIControlElement element = new UIControlElement();
					
					ApplicationLoader dal = new ApplicationLoader();
					CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
					List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),cache.getUser().getId());
					
					UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
					element.setInlineControlLines(inlines);
					
					element.setAttribute(a);
					element.setControlID(a.getDataReference());
					element.setLabel(translator.attributeTranslate(a.getAttribute().replaceAll("ID ",""), a.getId(), u.getLanguage()));
					element.setControlValue("");
					element.setListReference(listElements);
					element.setReference(true);
					block.getControls().add(element);
				}
			}
			results.add(block);
		}
		return results;
	}

	public void checkingMtmChangeListener(FacesEvent event){
		//	Collect all references
		String satt = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("mtmattribute1");
		String sblock = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("mtmblock1");
		satt = event.getComponent().getAttributes().get("mtmattribute").toString();
		sblock= event.getComponent().getAttributes().get("mtmblock").toString();
		
		if(satt == null || sblock == null)
			return;
		
		int aid = Integer.parseInt(satt);
		int bid = Integer.parseInt(sblock);
		
		MtmBlock changeMtmBlock = null;
		CAttribute selectedAttribute = null;
		
		for(MtmBlock b : mtmBlocks)
			if(b.getEntityID() == bid){
				changeMtmBlock = b;
				break;
			}
		
		if(changeMtmBlock == null)
			return;
		
		for(UIControlElement c : changeMtmBlock.getControls())
			if(c.getAttribute().getId() == aid){
				selectedAttribute = c.getAttribute();
				break;
			}
		
		if(selectedAttribute == null)
			return;
		
		List<UIControlElement> references = new ArrayList<UIControlElement>();
		for(UIControlElement c : changeMtmBlock.getControls()){
			if(c.isReference() && c.getAttribute().getId()!= selectedAttribute.getId())
				references.add(c);
		}
		UIControlElement ctrl = null;
		for(UIControlElement c : changeMtmBlock.getControls()){
			if(c.getAttribute().getId() == selectedAttribute.getId()){
				ctrl = c;
				break;
			}
		}
		if(ctrl == null)
			return;
		String value = null;
		for(PairKVElement e : ctrl.getListReference())
			if(e.getValue().equals(ctrl.getTrueValue())){
				value = e.getKey();
				break;
			}
		
		if(value == null)
			return ;
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		
		for(UIControlElement c : references){
			String tableReference = c.getAttribute().getDataReference().substring(3);
			CBusinessClass en = pde.getReferencedTable(tableReference);
			for(CAttribute a : en.getAttributes())
				if(a.getDataReference() == selectedAttribute.getDataReference()){
					List<String> cts = new ArrayList<String>();
					cts.add(a.getDataReference());
					cts.add("=");
					cts.add(value);
					List<PairKVElement> newElts = pde.getDataKeys(en.getDataReference(), false, 0, cts);
					c.setListReference(newElts);
					break;
				}
		}
	}
	
	public void checkingMtmChangeListener(String satt, String sblock){
		//	Collect all references
		if(satt == null || sblock == null)
			return;
		
		int aid = Integer.parseInt(satt);
		int bid = Integer.parseInt(sblock);
		
		MtmBlock changeMtmBlock = null;
		CAttribute selectedAttribute = null;
		
		for(MtmBlock b : mtmBlocks)
			if(b.getEntityID() == bid){
				changeMtmBlock = b;
				break;
			}
		
		if(changeMtmBlock == null)
			return;
		
		for(UIControlElement c : changeMtmBlock.getControls())
			if(c.getAttribute().getId() == aid){
				selectedAttribute = c.getAttribute();
				break;
			}
		
		if(selectedAttribute == null)
			return;
		
		List<UIControlElement> references = new ArrayList<UIControlElement>();
		for(UIControlElement c : changeMtmBlock.getControls()){
			if(c.isReference() && c.getAttribute().getId()!= selectedAttribute.getId())
				references.add(c);
		}
		UIControlElement ctrl = null;
		for(UIControlElement c : changeMtmBlock.getControls()){
			if(c.getAttribute().getId() == selectedAttribute.getId()){
				ctrl = c;
				break;
			}
		}
		if(ctrl == null)
			return;
		String value = null;
		for(PairKVElement e : ctrl.getListReference())
			if(e.getValue().equals(ctrl.getTrueValue())){
				value = e.getKey();
				break;
			}
		
		if(value == null)
			return ;
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		
		for(UIControlElement c : references){
			String tableReference = c.getAttribute().getDataReference().substring(3);
			CBusinessClass en = pde.getReferencedTable(tableReference);
			for(CAttribute a : en.getAttributes())
				if(a.getDataReference() == selectedAttribute.getDataReference()){
					List<String> cts = new ArrayList<String>();
					cts.add(a.getDataReference());
					cts.add("=");
					cts.add(value);
					List<PairKVElement> newElts = pde.getDataKeys(en.getDataReference(), false, 0, cts);
					c.setListReference(newElts);
					break;
				}
		}
	}
	
	public void checkingChangeListener(){
		
		//	Condtional layout
		List<MtmDTO> dtos = new ArrayList<MtmDTO>();

		for(UIControlElement e : controls){
				if(e.getAttribute().isConditionalLayout()){
					double value=0;
					//	Calculus Vudu
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
					try {
						value=engine.evaluateControlLayoutFormula(controls,e,dtos, window.getAppKey());
					} catch (Exception e1) {
						
						e1.printStackTrace();
					}
					
					if(value==0){
						e.setReadOnly(true);
						e.setHide(true);
						e.setControlValue("");
					}else{
						e.setReadOnly(false);
						e.setHide(false);
					}
//				}
			}
		}
		
		for(MtmBlock block : refBlocks){
			for(UIControlElement e : block.getControls()){
				if(!e.isReference() || e.getAttribute()==null || e.getAttribute().getId()==0)
					continue;
				ApplicationLoader dal = new ApplicationLoader();
				List<String> referencedAttributes = dal.getReferencedEntities(e.getAttribute().getDataReference().substring(3));
				List<String> wheres = new ArrayList<String>();
				for(UIControlElement se : controls){
					if(se.getAttribute().getId()==0)
						continue;
					for(String drf : referencedAttributes){
						
						if(drf.equals(e.getAttribute().getDataReference()))
							continue;
						if(se.getAttribute().getDataReference().equals(drf)){
							if(se.getControlValue()==null || se.getControlValue().length()==0){
								if(se.getListReference()!=null && se.getListReference().size()>0){
									se.setControlValue(se.getListReference().get(0).getKey());
									se.setTrueValue(se.getListReference().get(0).getValue());
								}
							}
							if((se.getControlValue() != null) && !se.getControlValue().equals("0") && !se.getControlValue().equals(""))
								wheres.add(drf+"="+se.getControlValue());
							
							
							break;
						}
					}
				}
				
				ProtogenDataEngine engine = new ProtogenDataEngine();
				Map<Integer, String> list = engine.getDataKeys(e.getAttribute().getDataReference().substring(3),wheres);
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				if(list!=null && list.size()>0)
					for(Integer i : list.keySet()){
						listElements.add(new PairKVElement(i.intValue()+"", list.get(i)));
					}
				e.getListReference().clear();
				if(listElements.size()>0){
					e.setListReference(listElements);
				}
				
			}
		}
		
		for(MtmBlock block : mtmBlocks){
			for(UIControlElement e : block.getControls()){
				if(!e.isReference() || e.getAttribute()==null || e.getAttribute().getId()==0)
					continue;
				ApplicationLoader dal = new ApplicationLoader();
				List<String> referencedAttributes = dal.getReferencedEntities(e.getAttribute().getDataReference().substring(3));
				List<String> wheres = new ArrayList<String>();
				for(UIControlElement se : controls){
					if(se.getAttribute().getId()==0)
						continue;
					for(String drf : referencedAttributes){
						
						if(drf.equals(e.getAttribute().getDataReference()))
							continue;
						if(se.getAttribute().getDataReference().equals(drf)){
							if(se.getControlValue()==null || se.getControlValue().length()==0){
								if(se.getListReference()!=null && se.getListReference().size()>0){
									se.setControlValue(se.getListReference().get(0).getKey());
									se.setTrueValue(se.getListReference().get(0).getValue());
								}
							}
							if((se.getControlValue() != null) && !se.getControlValue().equals("0") && !se.getControlValue().equals(""))
								wheres.add(drf+"="+se.getControlValue());
							
							
							break;
						}
					}
				}
				
				ProtogenDataEngine engine = new ProtogenDataEngine();
				Map<Integer, String> list = engine.getDataKeys(e.getAttribute().getDataReference().substring(3),wheres);
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				if(list!=null && list.size()>0)
					for(Integer i : list.keySet()){
						listElements.add(new PairKVElement(i.intValue()+"", list.get(i)));
					}
				e.getListReference().clear();
				if(listElements.size()>0){
					e.setListReference(listElements);
				}
				
			}
		}
		
		for(UIControlElement e : controls){
			if(!e.isReference() || e.getAttribute()==null || e.getAttribute().getId()==0)
				continue;
			if(e.getAttribute().getDataReference().substring(3).equals(window.getMainEntity()))		//	In case of reflexive reference keep all
				continue;
			if(selectedAttribute.getId() == e.getAttribute().getId())
				continue;
			if(e.getAttribute().isRappel()){
				//	Dealing with inner data
				for(UIControlElement se : controls)
					if(se.getAttribute().getId() == e.getAttribute().getSuperAttribute()){
						
						String val = se.getControlValue();
						int id=0;
						for(PairKVElement pkv : se.getListReference()){
							if(val == pkv.getValue()){
								id = Integer.parseInt(pkv.getKey());
								break;
							}
						}
						ProtogenDataEngine pde = new ProtogenDataEngine();
						String value = pde.getAssociatedValue(se.getAttribute(), e.getAttribute(), id);
						if(value!= null && value.length()>0 && !value.equals("null"))
							value="";
						e.setControlValue(value);
						break;
					}
				continue;
			}
			ApplicationLoader dal = new ApplicationLoader();
			List<String> referencedAttributes = dal.getReferencedEntities(e.getAttribute().getDataReference().substring(3));
			List<String> wheres = new ArrayList<String>();
			for(UIControlElement se : controls){
				if(se.getAttribute().getId()==0)
					continue;
				for(String drf : referencedAttributes){
					
					if(drf.equals(e.getAttribute().getDataReference()))
						continue;
					if(se.getAttribute().getDataReference().equals(drf)){
						if(se.getControlValue()==null || se.getControlValue().length()==0){
							if(se.getListReference()!=null && se.getListReference().size()>0){
								se.setControlValue(se.getListReference().get(0).getKey());
								se.setTrueValue(se.getListReference().get(0).getValue());
							}
						}
						if((se.getControlValue() != null) && !se.getControlValue().equals("0") && !se.getControlValue().equals(""))
							wheres.add(drf+"="+se.getControlValue());
						
						
						break;
					}
				}
			}
			ProtogenDataEngine engine = new ProtogenDataEngine();
			Map<Integer, String> list = engine.getDataKeys(e.getAttribute().getDataReference().substring(3),wheres);
			List<PairKVElement> listElements = new ArrayList<PairKVElement>();
			if(list!=null && list.size()>0)
				for(Integer i : list.keySet()){
					listElements.add(new PairKVElement(i.intValue()+"", list.get(i)));
				}
			e.getListReference().clear();
			if(listElements.size()>0){
				e.setListReference(listElements);
			}
			for(UIControlElement roe : controls){
				if(!roe.isReadOnly())
					continue;
				String table = e.getAttribute().getDataReference().substring(3);
				if(!roe.getAttribute().getEntity().getDataReference().equals(table))
					continue;
				String dbID = e.getControlValue();
				if(dbID.length()==0 && e.getListReference() != null && e.getListReference().size()>0){
					String k =e.getListReference().get(0).getKey();
					dbID = k;
				}
				String k = engine.getDataByID(table, roe.getAttribute().getDataReference(), dbID);
				if(roe.getAttribute().getDataReference().startsWith("fk_")){	//	Is a reference
					int id = 0;
					if(k!= null && k.length()>0 && !k.equals("null"))
						id = Integer.parseInt(k);
					String lookTable = roe.getAttribute().getDataReference().substring(3);
					engine = new ProtogenDataEngine();
					ArrayList<String> twheres = new ArrayList<String>();
					twheres.add("pk_"+lookTable+"="+id);
					Map<Integer, String> tlist = engine.getDataKeys(lookTable,twheres);
					for(Integer i : tlist.keySet()){
						k=tlist.get(i);
						break;
					}
				}
				if(k.split(" ").length == 2 && k.split("-").length==3)	//	Date
					k = k.split(" ")[0];
				if(k== null || k.length()==0 || k.equals("null"))
					k="";
				roe.setControlValue(k);
			}
			
		}
		
		//	update Table Lists
		updateTableLists();
	}
	
	public void updateMtmReferenceKeys(){
		Map<String, String> reqMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int idAttribute = Integer.parseInt(reqMap.get("CTRL_ID"));
		int idEntity = Integer.parseInt(reqMap.get("BLOCK_ID"));
		
		MtmBlock blc = null;
		for(MtmBlock b : mtmBlocks){
			if(b.getEntityID() == idEntity){
				blc = b;
				break;
			}
		}
		UIControlElement ctrl = null;
		for(UIControlElement c : blc.getControls()){
			if(c.getAttribute().getId() == idAttribute){
				ctrl = c;
				break;
			}
		}
		List<String> keys = new ArrayList<String>();
		String keyWords = ctrl.getSearchKeyWords().replaceAll(",", " ");
		keyWords = keyWords.replaceAll(";"," ");
		String[] keysTable = keyWords.split(" ");
		for(String  k : keysTable)
			keys.add(k);
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		Map<Integer, String> vals = pde.getDataKeys(ctrl.getAttribute().getDataReference().substring(3), 
				new ArrayList<String>(), keys);
		
		ctrl.setListReference(new ArrayList<PairKVElement>());
		for(Integer i : vals.keySet()){
			PairKVElement pkv = new PairKVElement(""+i.intValue(),vals.get(i));
			ctrl.getListReference().add(pkv);
		}
	}
	
	public void updateReferenceKeys(){
		UIControlElement ctrl = uictrl;
		
		List<String> keys = new ArrayList<String>();
		String keyWords = ctrl.getSearchKeyWords().replaceAll(",", " ");
		keyWords = keyWords.replaceAll(";"," ");
		String[] keysTable = keyWords.split(" ");
		for(String  k : keysTable)
			keys.add(k);
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		Map<Integer, String> vals = pde.getDataKeys(ctrl.getAttribute().getDataReference().substring(3), 
				new ArrayList<String>(), keys);
		
		ctrl.setListReference(new ArrayList<PairKVElement>());
		for(Integer i : vals.keySet()){
			PairKVElement pkv = new PairKVElement(""+i.intValue(),vals.get(i));
			ctrl.getListReference().add(pkv);
		}
			
	}
	
	public void updateReferenceKeysSingle(){
		Map<String, String> reqMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int id= Integer.parseInt(
					reqMap.get("CTRL_ID")
				);
		UIControlElement ctrl = null;
		for(UIControlElement c : controls){
			if(c.getAttribute().getId() == id){
				ctrl = c;
				break;
			}
		}
		
		List<String> keys = new ArrayList<String>();
		String keyWords = ctrl.getSearchKeyWords().replaceAll(",", " ");
		keyWords = keyWords.replaceAll(";"," ");
		String[] keysTable = keyWords.split(" ");
		for(String  k : keysTable)
			keys.add(k);
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		Map<Integer, String> vals = pde.getDataKeys(ctrl.getAttribute().getDataReference().substring(3), 
				new ArrayList<String>(), keys);
		
		ctrl.setListReference(new ArrayList<PairKVElement>());
		for(Integer i : vals.keySet()){
			PairKVElement pkv = new PairKVElement(""+i.intValue(),vals.get(i));
			ctrl.getListReference().add(pkv);
		}
			
	}
	
	public void autoCompleteMtmFieldSelect(){
		Map<String, String> reqMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String selectedVal = reqMap.get("CTRL_ATT_VAL");
		
		
		MtmBlock blc = mtmBlock;
		UIControlElement ctrl = uictrl;
		
		
		
		for(PairKVElement p : ctrl.getListReference()){
			if(p.getKey().equals(selectedVal)){
				ctrl.setControlValue(p.getKey());
				ctrl.setTrueValue(p.getValue());
				break;
			}
		}
		
		checkingMtmChangeListener(ctrl.getAttribute().getId()+"", blc.getEntityID()+"");
	}
	
	public void autoCompleteFieldSelect(){
		Map<String, String> reqMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int idAttribute = Integer.parseInt(reqMap.get("CTRL_ATT_ID"));
		String selectedVal = reqMap.get("CTRL_ATT_VAL");
		UIControlElement ctrl = null;
		for(UIControlElement c : controls){
			if(c.getAttribute().getId() == idAttribute){
				ctrl = c;
				break;
			}
		}
		
		for(PairKVElement p : ctrl.getListReference()){
			if(p.getKey().equals(selectedVal)){
				ctrl.setControlValue(p.getKey());
				ctrl.setTrueValue(p.getValue());
				break;
			}
		}
		
		checkingChangeListener();
	}
	
	private void updateTableLists() {
		for(UIControlElement e : controls){
			if(e.getAttribute().getCAttributetype().getId() != 11)
				continue;
			
			ApplicationLoader dal = new ApplicationLoader();
			ProtogenDataEngine pde = new ProtogenDataEngine();
			
			String attributeRef = "";//"fk_"+e.getAttribute().getMetatableReference().split("\\.")[0].replaceAll("<<", "");
			String tableName = e.getAttribute().getMetatableReference().split("\\.")[0].replaceAll("<<", "").trim();
			
			CBusinessClass refClass = dal.getClassByName(tableName);
			refClass = dal.getEntity(refClass.getDataReference());
			attributeRef = "fk_"+refClass.getDataReference();
					
			UIControlElement tableControl = null;
			for(UIControlElement c : controls){
				if(c.getAttribute().getDataReference().equals(attributeRef)){
					tableControl = c;
					break;
				}
			}
			if(tableControl == null || tableControl.getControlValue()==null || 
					tableControl.getControlValue().equals("0") || tableControl.getControlValue().equals(""))
				continue;
			
			//	Get the table
			int idTable = Integer.parseInt(tableControl.getControlValue());
			List<Map<CAttribute, Object>> datum = pde.getDataByConstraint(refClass, "pk_"+refClass.getDataReference()+"="+idTable);
			if(datum == null || datum.size() == 0)
				continue;
			int cbcID=0;
			String aname = e.getAttribute().getMetatableReference().split("\\.")[1].replaceAll(">>", "").trim();
			for(CAttribute a : datum.get(0).keySet()){
				if(a.getAttribute().equals(aname)){
					String scbc = datum.get(0).get(a).toString();
					if(scbc != null && scbc.length()>0)
						cbcID = Integer.parseInt(scbc); 
					break;
				}
			}
			
			if(cbcID == 0)
				continue;
			
			CBusinessClass entity = dal.getEntityById(cbcID);
			List<PairKVElement> elts = pde.getDataKeys(entity.getDataReference(), false, 0);
			if(elts == null)
				continue;
			e.setListReference(new ArrayList<PairKVElement>());
			
			e.getListReference().addAll(elts);
		}
	}

	private void loadVoidComponents() {
		controls = new ArrayList<UIControlElement>();
		controlLines = new UIControlsLine();
		references = new ArrayList<String>();
		updatedFields="";
		List<String> entities = new ArrayList<String>();
		ApplicationLoader dal = new ApplicationLoader();
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		
		for(CAttribute attribute : window.getCAttributes()){
			// if we are in a unique row form we should look for the source attribute
			if(uniqueRowMode){
				if(attribute.getDataReference().equals("fk_"+identificationRow.getSource().getDataReference()))
					uniqueTo = attribute;
			}
			
			if(!attribute.isVisible() || attribute.getDataReference().startsWith("pk_") || attribute.isMultiple() || checkAttribute(attribute,mtmBlocks) )
				continue;
			
			UIControlElement element = new UIControlElement();
			
			if(attribute.isRappel()){
				element.setAttribute(attribute);
				
				for(CAttribute a : window.getCAttributes())
					if(a.getDataReference().startsWith("fk_") && attribute.getEntity().getDataReference().equals(a.getDataReference().substring(3))){
						attribute.setSuperAttribute(a.getId());
						break;
					}
				element.setReadOnly(true);
				element.setControlID(attribute.getAttribute());
				element.setTitle(attribute.getAttribute());
				element.setControlValue("");
				controls.add(element);
				controlLines.addControl(element);
				continue;
			}
			
			if(attribute.isAutoValue()){
				String nextVal = engine.loadNextValue(attribute);
				attribute.setDefaultValue(nextVal);
				element.setControlValue(nextVal);
				element.setReadOnly(true);
			}
			if(entities.contains(attribute.getEntity().getDataReference())){
				element.setTitle("");
				element.setVisible(false);
			} else {
				element.setTitle(attribute.getEntity().getName());
				element.setVisible(true);
				entities.add(attribute.getEntity().getDataReference());
			}
			
			String type = attribute.getCAttributetype().getType();
			if(attribute.isReference()){
				
				String entity = attribute.getEntity().getDataReference();
				if(references.contains(entity))
					continue;
				
				
				String referenceTable = attribute.getDataReference().substring(3);
				
				//	Add all search references
				addSearchReferences(referenceTable);
				
				references.add(referenceTable);
				String referencedEntity = dal.getEntityFromDR(referenceTable);
				
				CBusinessClass e = dal.getEntity(referenceTable);
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				List<PairKVElement> list = engine.getDataKeys(referenceTable,(e.getUserRestrict()=='Y'),cache.getUser().getId());
				
				element.setReferenceTable(referencedEntity);
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				
				if(list==null){
					list = new ArrayList<PairKVElement>();
				}
				for(PairKVElement kv : list){
					listElements.add(kv);
				}
				
				// prepare inline components
				
				UIControlsLine inlines = this.prepareInlineComponents(attribute.getDataReference(), ""+attribute.getId());
				element.setInlineControlLines(inlines);
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute().replaceAll("ID ", ""));
				element.setControlValue("");
				element.setListReference(listElements);
				element.setReference(true);
				element.setFiltrable(listElements.size()>10);
				
				
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idselect_"+attribute.getId()+",";
				controls.add(element);
				controlLines.addControl(element);
				continue;
			}
			if(references.contains(attribute.getEntity().getDataReference())){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				element.setVisible(false);
				element.setReadOnly(true);
				controls.add(element);
				controlLines.addControl(element);
				continue;
			}
			if(type.equals("ENTIER")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idtext_"+attribute.getId()+",";
			} else if(type.equals("Booléen")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				element.setBooleanValue(false);
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idcheckbox_"+attribute.getId()+",";
			} else if(type.equals("HEURE")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idmask_"+attribute.getId()+",";
			} else if (type.equals("TEXT")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				if(attribute.getDefaultValue()!=null && attribute.getDefaultValue().length()>0)
					element.setControlValue(attribute.getDefaultValue());
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idtext_"+attribute.getId()+",";
			} else if (type.equals("DATE")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				controls.add(element);
				controlLines.addControl(element);
				element.setDateValue(new Date());
				element.setCtrlDate(true);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idcalendar_"+attribute.getId()+",";
			} else if (type.equals("DOUBLE")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.getDefaultValue()!=null && attribute.getDefaultValue().length()>0)
					element.setControlValue(attribute.getDefaultValue());
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idtext_"+attribute.getId()+",";
			} else if(type.equals("FICHIER")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setBinaryContent(true);
				element.setNonVoidContent(false);
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idfichier_"+attribute.getId()+",";
			} else if(attribute.getCAttributetype().getId()==7) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				CoreUser u = cache.getUser();
				element.setControlValue(u.getFirstName()+" - "+u.getLastName());
				element.setTrueValue(u.getId()+"");
				controls.add(element);
				controlLines.addControl(element);
			} else if (attribute.getCAttributetype().getId()==8){
				element.setMoney(true);
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.getDefaultValue()!=null && attribute.getDefaultValue().length()>0)
					element.setControlValue(attribute.getDefaultValue());
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idtext_"+attribute.getId()+",";
			} else if (attribute.getCAttributetype().getId()==9){
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idvalidate_"+attribute.getId()+",";
			} else if(attribute.getCAttributetype().getId()==10){ 
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				List<CBusinessClass> alent = dal.loadAllEntities(cache.getAppKey());
				element.setListReference(new ArrayList<PairKVElement>());
				
				for(CBusinessClass c : alent){
					element.getListReference().add(new PairKVElement(""+c.getId(), c.getName()));
				}
				controls.add(element);
				controlLines.addControl(element);
				
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idvalidate_"+attribute.getId()+",";
			} else if (attribute.getCAttributetype().getId() == 11){
				element.setListReference(new ArrayList<PairKVElement>());
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				controls.add(element);
				controlLines.addControl(element);
				
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idvalidate_"+attribute.getId()+",";
			}
		} 
		
		if(updatedFields.length()>0)
			updatedFields=updatedFields.substring(0,updatedFields.length()-1);
		return;
	}

	
	
	private void addSearchReferences(String theReferenceTable) {
		for(CAttribute a : window.getCAttributes()){
			if(!a.isReference() || !a.getEntity().getDataReference().equals(theReferenceTable))
				continue;
			
			ProtogenDataEngine engine = new ProtogenDataEngine();
			ApplicationLoader dal = new ApplicationLoader();
			
			String referenceTable = a.getDataReference().substring(3);
			
			UIControlElement element = new UIControlElement();
			
			references.add(referenceTable);
			String referencedEntity = dal.getEntityFromDR(referenceTable);

			CBusinessClass e = dal.getEntity(referenceTable);
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			List<PairKVElement> list = engine.getDataKeys(referenceTable,(e.getUserRestrict()=='Y'),cache.getUser().getId());
			
			element.setReferenceTable(referencedEntity);
			List<PairKVElement> listElements = new ArrayList<PairKVElement>();
			for(PairKVElement kv : list){
				listElements.add(kv);
			}
			element.setAttribute(a);
			element.setControlID(a.getAttribute().replaceAll("ID ", ""));
			element.setControlValue("");
			element.setListReference(listElements);
			element.setFiltrable(listElements.size()>10);
			element.setReference(true);
			controls.add(element);
			controlLines.addControl(element);
		}
	}


	private boolean checkAttribute(CAttribute attribute,
			List<MtmBlock> mtmBlocks2) {
		 
		if(attribute.getEntity().getDataReference().equals(window.getMainEntity()))
			return false;
		for(MtmBlock b : mtmBlocks2){
			if(attribute.getDataReference().contains(b.getEntity().getDataReference()))
				return true;
			for(CAttribute a : b.getEntity().getAttributes())
				if(a.getDataReference().equals(attribute.getDataReference()))
					return true;
			
		}
		return false;
	}





	private void loadComponents() {
		references = new ArrayList<String>();
		controls = new ArrayList<UIControlElement>();
		controlLines = new UIControlsLine();
		
		foreignKeys = new HashMap<String, String>();
		updatedFields="";
		
		ApplicationLoader dal = new ApplicationLoader();
		
		/*
		 * 	check if the main entity is part of parameters model
		 */
		boolean superadmin = ((Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("SUPER_ADMIN")).booleanValue();
		if(superadmin){
			
			ProtogenDataEngine pde = new ProtogenDataEngine();
			int id = pde.loadParameteredFor(window.getMainEntity(), dbID);
			
			CBusinessClass ment = dal.getEntity(window.getMainEntity());
			paramodel = dal.loadParametersMetamodel(ment);
			
			if(paramodel != null){
				COrganization org = paramodel.getOrganization();
				UIControlElement element = new UIControlElement();
				element.setControlID(org.getLabel());
				element.setTitle(org.getLabel());
				element.setReference(true);
				element.setListReference(org.getInstances());
				CAttribute a = new CAttribute();
				a.setId(0);
				element.setAttribute(a);
				element.setControlValue(id+"");
				controls.add(element);
				controlLines.addControl(element);
			}
		}
		
		
		for(CAttribute attribute : window.getCAttributes()){
			if(attribute.getDataReference().startsWith("fk_") && attribute.getEntity().getDataReference().equals(window.getMainEntity()) && !attribute.isReference() &&!attribute.isMultiple())
				foreignKeys.put(attribute.getDataReference(), initialData.getMainEntity().get(attribute.getAttribute()));
			if(!attribute.isVisible() || (attribute.getDataReference().startsWith("fk_") && !attribute.isReference() && !attribute.isRappel()) || attribute.getDataReference().startsWith("pk_"))
				continue;
			
			
			// if we are in a unique row form we should look for the source attribute
			if(uniqueRowMode){
				if(attribute.getDataReference().equals("fk_"+identificationRow.getSource().getDataReference()))
					uniqueTo = attribute;
			}
			
			UIControlElement element = new UIControlElement();
			
			/*if(entities.contains(attribute.getEntity().getDataReference())){
				element.setTitle("");
				element.setVisible(false);
			} else {
				element.setTitle(attribute.getEntity().getName());
				element.setVisible(true);
				entities.add(attribute.getEntity().getDataReference());
			}*/
			ProtogenDataEngine pde = new ProtogenDataEngine();
			if(attribute.isRappel()){
				element.setAttribute(attribute);
				CAttribute se = new CAttribute();
				for(CAttribute a : window.getCAttributes())
					if(a.getDataReference().startsWith("fk_") && attribute.getEntity().getDataReference().equals(a.getDataReference().substring(3))){
						attribute.setSuperAttribute(a.getId());
						se = a;
						break;
					}
				element.setReadOnly(true);
				element.setControlID(attribute.getAttribute());
				element.setTitle(attribute.getAttribute());
				String sid= initialData.getMainEntity().get(se);
				int id = 0;
				if(sid!=null && sid.length()>0 && !sid.equals("null"))
					id = Integer.parseInt(sid);
				String value = pde.getAssociatedValue(se, attribute, id);
				if(value == null || value.length()==0 || value.equals("null"))
					value="";
				element.setControlValue(value);
				controls.add(element);
				controlLines.addControl(element);
				continue;
			}
			if(attribute.isReference()){
				ProtogenDataEngine engine = new ProtogenDataEngine();
				String referenceTable = attribute.getDataReference().substring(3);
				references.add(referenceTable);
				String referencedEntity = dal.getEntityFromDR(referenceTable);
				element.setReferenceTable(referencedEntity);
				
				CBusinessClass ce = dal.getEntity(referenceTable);
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				List<PairKVElement> list = engine.getDataKeys(referenceTable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());
				
				
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				for(PairKVElement kv : list){
					listElements.add(kv);
				}
				
				UIControlsLine inlines = this.prepareInlineComponents(attribute.getDataReference(), ""+attribute.getId());
				element.setInlineControlLines(inlines);
				
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setListReference(listElements);
				element.setFiltrable(listElements.size()>10);
				element.setReference(true);
				
				//	Value
				String value = initialData.getMainEntity().get(attribute.getAttribute());
				boolean found = false;
				for(PairKVElement e : listElements){
					if(e.getKey().equals(value)){
						element.setTrueValue(e.getValue());
						found = true;
						break;
					}
				}
				
				if(!found && value!=null && value!="0" && value.length()>0){
					PairKVElement el = engine.getDataKeyByID(attribute.getDataReference().substring(3), Integer.parseInt(value));
					element.getListReference().add(el);
					element.setTrueValue(el.getValue());
					found = true;
				}
				
				if(!found && value!=null && value.length()>0 && value.length()>0){
					System.out.println(value);
					PairKVElement e = engine.getDataKeyByID(attribute.getDataReference().substring(3), Integer.parseInt(value));
					e.setAttribute(attribute);
					e.setKey(value);
					element.getListReference().add(e);
					element.setTrueValue(e.getValue());
					
				}

				if(uniqueRowMode){
					int id = engine.getDefault(identificationRow, value);
					if(id == dbID)
						defaultChecked = true;
				}
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idselect_"+attribute.getId()+",";
				continue;
			}
			String type = attribute.getCAttributetype().getType();

			if(references.contains(attribute.getEntity().getDataReference())){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setVisible(false);

				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					String k = initialData.getMainEntity().get(attribute.getAttribute());
					if(k.split(" ").length == 2 && k.split("-").length==3)	//	Date
						k = k.split(" ")[0];
					element.setControlValue(k);
				} else {
					String k = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					if(k.split(" ").length == 2 && k.split("-").length==3)	//	Date
						k = k.split(" ")[0];
					element.setControlValue(k);
				}
				element.setReadOnly(true);
				controls.add(element);
				controlLines.addControl(element);
				continue;
			}
			
			if(type.equals("ENTIER")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					element.setControlValue(initialData.getMainEntity().get(attribute.getAttribute()));
				} else {
					String value = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					element.setControlValue(value);
				}
				
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idtext_"+attribute.getId()+",";
			} else if(type.equals("Booléen")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					element.setControlValue(initialData.getMainEntity().get(attribute.getAttribute()));
				} else {
					String value = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					element.setControlValue(value);
				}
				element.setBooleanValue(element.getControlValue().equals("Oui"));
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idcheckbox_"+attribute.getId()+",";
			} else if(type.equals("HEURE")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idmask_"+attribute.getId()+",";
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					String v = initialData.getMainEntity().get(attribute.getAttribute());
					if(v == null || v.length()==0)
						v="0";
					int ival =  Integer.parseInt(v);
					String svalue="";
					if(ival>=1000)
						svalue=v.charAt(0)+""+v.charAt(1)+":"+v.charAt(2)+""+v.charAt(3);
					else if(ival>=100)
						svalue="0"+v.charAt(0)+":"+v.charAt(1)+""+v.charAt(2);
					else if(ival>=10)
						svalue="00:"+v.charAt(0)+""+v.charAt(1);
					else
						svalue="00:0"+v.charAt(0);
					element.setControlValue(svalue);
				} else {
					String v = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					int ival =  Integer.parseInt(v);
					String svalue="";
					if(ival>=1000)
						svalue=v.charAt(0)+""+v.charAt(1)+":"+v.charAt(2)+""+v.charAt(3);
					else if(ival>=100)
						svalue="0"+v.charAt(0)+":"+v.charAt(1)+""+v.charAt(2);
					else if(ival>=10)
						svalue="00:"+v.charAt(0)+""+v.charAt(1);
					else
						svalue="00:0"+v.charAt(0);
					element.setControlValue(svalue);
				}
				controls.add(element);
				controlLines.addControl(element);

			} else if (type.equals("TEXT")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					System.out.println(attribute.getAttribute());
					System.out.println(initialData.getMainEntity().toString());
					element.setControlValue(initialData.getMainEntity().get(attribute.getAttribute()));
				} else {
					String value = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					element.setControlValue(value);
				}
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idtext_"+attribute.getId()+",";
			} else if (type.equals("DATE")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setCtrlDate(true);
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					String sdate = initialData.getMainEntity().get(attribute.getAttribute());
					Date date = getDate(sdate);
					element.setDateValue(date);
					
				} else {
					String sdate = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					Date date = getDate(sdate);
					element.setDateValue(date);
					
				}
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idcalendar_"+attribute.getId()+",";
			} else if (type.equals("DOUBLE")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					element.setControlValue(initialData.getMainEntity().get(attribute.getAttribute()));
				} else {
					String value = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					element.setControlValue(value);
				}
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idtext_"+attribute.getId()+",";
			}  else if(type.equals("FICHIER")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setBinaryContent(true);
				element.setNonVoidContent(false);
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idfichier_"+attribute.getId()+",";
			} else if(attribute.getCAttributetype().getId()==7) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				CoreUser u = cache.getUser();
				element.setControlValue(u.getFirstName()+" - "+u.getLastName());
				element.setTrueValue(u.getId()+"");
				controls.add(element);
				controlLines.addControl(element);
			} else if (attribute.getCAttributetype().getId()==8){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					element.setControlValue(initialData.getMainEntity().get(attribute.getAttribute()));
				} else {
					String value = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					element.setControlValue(value);
				}
				element.setMoney(true);
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idtext_"+attribute.getId()+",";
			} else if (attribute.getCAttributetype().getId()==9){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					element.setControlValue(initialData.getMainEntity().get(attribute.getAttribute()));
				} else {
					String value = initialData.getOtmEntities().get(attribute.getEntity().getName()).get(attribute.getAttribute());
					element.setControlValue(value);
				}
				controls.add(element);
				controlLines.addControl(element);
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idvalidate_"+attribute.getId()+",";
			} else if(attribute.getCAttributetype().getId()==10){ 
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				List<CBusinessClass> alent = dal.loadAllEntities(cache.getAppKey());
				element.setListReference(new ArrayList<PairKVElement>());
				
				for(CBusinessClass c : alent){
					element.getListReference().add(new PairKVElement(""+c.getId(), c.getName()));
				}
				controls.add(element);
				controlLines.addControl(element);
				
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idvalidate_"+attribute.getId()+",";
			} else if (attribute.getCAttributetype().getId() == 11){
				element.setListReference(new ArrayList<PairKVElement>());
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				controls.add(element);
				controlLines.addControl(element);
				
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idvalidate_"+attribute.getId()+",";
			}
			
			if(attribute.isAutoValue()){
				element.setReadOnly(true);
				element.setControlValue(attribute.getDefaultValue() + " " + element.getControlValue());
			}
		}
		
		if(updatedFields.length()>0)
			updatedFields=updatedFields.substring(0,updatedFields.length()-1);
	}
	
	private Date getDate(String sdate) {
		String days = sdate.split(" ")[0];
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			System.out.println("Date to parse : "+days);
			return formatter.parse(days);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return Calendar.getInstance().getTime();
	}





	//	Active behavior

	public String doSave(){
		
		List<MtmDTO> dtos = getFullDTOS();
		
		
		//	Vérifier s'il y a des références obligatoires vides
		boolean tovalidate=false;
		for(UIControlElement e : controls){
			if(/*e.isReference() && */e.getAttribute().isMandatory() && 
					e.getAttribute().getEntity().getDataReference().equals(window.getMainEntity())){
				if(e.getControlValue() == null || e.getControlValue().length()==0){
					FacesContext context = FacesContext.getCurrentInstance();  
			        tovalidate=true; 
			        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Le champ "+e.getControlID()+" est obligatoire",""));  
			        return "";
				}
			}
		}
	
		if(tovalidate)
			return "";
		//	Vérifier s'il y a des entrées numériques qui sont nulles et les remplacer par des zeros
		for(UIControlElement e : controls){
			if(e.getAttribute().getCAttributetype() == null)
				continue;
			int type = e.getAttribute().getCAttributetype().getId();
			if(type==4 || type ==8){
				if(e.getControlValue() == null || e.getControlValue().length()==0)
					e.setControlValue("0");
			}
		}
		
		//	Vérification avant sauvegarde
		for(UIControlElement e : controls){
			if(!e.isReference()){
				if(e.getAttribute().isRequiresValidation() && !e.isReadOnly()){
					double value=0;
					//	Calculus Vudu
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
					try {
						value=engine.evaluateFormula(controls,e,dtos, window.getAppKey());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					if(value==0){
						FacesContext context = FacesContext.getCurrentInstance();  
				        
				        context.addMessage(null, new FacesMessage("Erreur", "La valeur de "+e.getControlID()+" est incorrecte"));  
				        return "";
					}
				}
			}
		}
		
		//	Champs calculés
		for(UIControlElement e : controls){
			if(!e.isReference() && !e.isReadOnly()){
				if(e.getAttribute().isCalculated()){
					if(e.getAttribute().getFormula().startsWith("DECLANCHEUR")){
						e.setControlValue(""+e.getAttribute().getFormula().split("::")[1]);
						continue;
					}
					double value=0;
					//	Calculus Vudu
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
					try {
						value=engine.evaluateMtm(controls,e,dtos,window.getAppKey());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					if(value==-999.999){
						FacesContext context = FacesContext.getCurrentInstance();  
				          
				        context.addMessage(null, new FacesMessage("Erreur", "La valeur de "+e.getControlID()+" est incorrecte"));  
				        return "";
					}
					
					e.setControlValue(""+value);
				}
			}
		}
		
		//	MAJ Contenu binaire
		for(UIControlElement e : controls){
			if(!e.isBinaryContent())
				continue;
			
			String key="TMP_FILE_"+e.getAttribute().getDataReference();
			if(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey(key))
				e.setContent((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key));
		}
		

		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		Boolean flag ;
		boolean voidflag=true;
		for(UIControlElement c : controls){
			if(c.getControlValue()!=null && c.getControlValue().length()>0){
				voidflag=false;
				break;
			}
		}
		
		if(voidflag){
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN,"Enregistrement non sauvegardé","Vous avez essayé de sauvegarder un enregistrement vide, prière de renseigner les différents champs et de réessayer"));
			return "";
		}
		
		// Populate foreign keys
		for(UIControlElement e : controls){
			if(!e.isReference())
				continue;
			String textValue=e.getTrueValue();
			if(textValue == null || textValue.length()==0)
				continue;
			
			textValue = textValue.replaceAll("-", "");
			String keyWords = textValue.replaceAll("  ", " ");
			String[] keysTable = keyWords.split(" ");
			List<String> keys = new ArrayList<String>();
			for(String  k : keysTable)
				keys.add(k);
			
			Map<Integer, String> vals = engine.getDataKeys(e.getAttribute().getDataReference().substring(3), 
					new ArrayList<String>(), keys);
			
			if(vals.size()>0){
				
				for(Integer id : vals.keySet()){
					String key = vals.get(id);
					if(textValue.equals(key)){
						e.setControlValue(id+"");
						break;
					}
				}
				
			}
		}
		
		if(action == null || action.equals("update")){
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				
			flag= engine.executeSaveAction(window, controls, dbID, foreignKeys, dtos,refBlocks);
			engine.updateGlobalValues(globalControls);
			if(compositionMode){
				CompositionDataAccess cda = new CompositionDataAccess();
				cda.saveComposition(composition, dbID, toCompose,composables);
			}
			
			if(localizedMode){
				OrganizationDAL odal = new OrganizationDAL();
				if(selectedOrg == null)
					selectedOrg = orgMap.get(cache.getOrganization());
				odal.updateLocalization(localizedEntity, dbID, (GOrganization)selectedOrg.getData());
			}
			
			
			//	Update default value
			if(uniqueRowMode && defaultChecked){
				int idsource = 0;
				for(UIControlElement c : controls){
					if(c.getAttribute() == uniqueTo){
						for(PairKVElement e : c.getListReference())
							if(e.getValue().equals(c.getTrueValue())){
								idsource = Integer.parseInt(e.getKey());
								break;
							}
						break;
					}
				}
				engine.updateDefaultRow(identificationRow, idsource, dbID);
			}
			
		}
		else{
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			CoreUser u = cache.getUser();
			if(u.getOrgInstance() == 0){
				u.setOrgInstance(u.getOriginalOrganization().getIdBean());
			}
			Map<String, String> referenceForeignKeys = addReferences(u);
			flag= engine.executeInsertAction(window, controls,dtos,u, referenceForeignKeys);
			
			dbID=engine.getLastRowId(window.getMainEntity());
			
			if(localizedMode){
				OrganizationDAL odal = new OrganizationDAL();
				if(selectedOrg == null)
					selectedOrg = orgMap.get(cache.getOrganization());
				odal.persistLocalization(localizedEntity, dbID, (GOrganization)selectedOrg.getData());
			}
			
			//	Composition
			if(compositionMode){
				CompositionDataAccess cda = new CompositionDataAccess();
				cda.saveComposition(composition, dbID, toCompose,composables);
			}
			
			//	Check for history mode
			ApplicationLoader al = new ApplicationLoader();
			CBusinessClass entity = al.getEntity(window.getMainEntity());
			HistoryDataAccess hda = new HistoryDataAccess();
			CDataHistory h = hda.checkForHistory(entity);
			
			if(h.getId()>0){
				for(CAttribute a : window.getCAttributes()){
					if(a.getId() == h.getReference().getId()){
						String svalue = "0";
						for(UIControlElement c : controls){
							if(c.getAttribute().getId() == a.getId()){
								for(PairKVElement e : c.getListReference())
									if(e.getValue().equals(c.getTrueValue())){
										svalue = e.getKey();
										break;
									}
								break;
							}
						}
						int i = Integer.parseInt(svalue);
						CInstanceHistory courant = new CInstanceHistory();
						courant.setId(0);
						
						for(Integer I : h.getCourant().keySet()){
							if(I.intValue() == i){
								courant = h.getCourant().get(I);
								break;
							}
						}
						
						if(courant.getId() > 0){
							hda.renderObsolete(courant, CalendarUtils.addDays(histoStart, -1));
						}
						
						hda.historise(courant, h, dbID,i, histoStart);
						
						break;
					}
				}
			}
			
			//	Update default value
			if(uniqueRowMode && defaultChecked){
				int idsource = 0;
				for(UIControlElement c : controls){
					if(c.getAttribute() == uniqueTo){
						for(PairKVElement e : c.getListReference())
							if(e.getValue().equals(c.getTrueValue())){
								idsource = Integer.parseInt(e.getKey());
								break;
							}
						break;
					}
				}
				engine.updateDefaultRow(identificationRow, idsource, dbID);
			}
			
			
			
			if(parameteredEntity){
				int idIn = 0;
				
				OrganizationDAL odal = new OrganizationDAL();
				idIn = odal.loadModelInstance(Integer.parseInt(selectedInstance),selectedPackage.getId());
				
				CBusinessClass e = engine.getReferencedTable(window.getMainEntity());
				
				odal.updateParametersMap(e, dbID, idIn,selectedPackage);
			}
			
			engine.updateGlobalValues(globalControls);
			for(Trigger t : window.getTriggers()){
				TriggersEngine.getInstance().trigger(t, controls, dtos);
			}
			
			//	Alerts
			
			String reference = "";
			for(UIControlElement e : controls){
				if(e.getAttribute().isKeyAttribute())
					reference = reference+e.getControlValue()+" - ";
			}
			if(reference.length()>3)
				reference = reference.substring(0,reference.length()-3);
			
			for(SAlert a : alerts){
				AlertInstance i = new AlertInstance();
				i.setAlert(a);
				i.setMessage(a.getDescription().replaceAll("<<Référence>>", reference));
				
				AlertDataAccess ada = new AlertDataAccess();
				ada.insertAlert(i);
				
			}
		}
		
		if(flag){
			
			if(picklistProfils.getTarget().size() >0 )
				updateRolesProfiles(dbID);
			
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO,"Enregistrement sauvegardé avec succès",""));
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.FORM_CONTROLS, controls);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.FORM_WINDOW, windowTitle);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.FORM_DTOS, mtmBlocks);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.FORM_AUTOVALUES, engine.getAutovalues());
			
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			CoreUser u = cache.getUser();
			
			ContextPersistence pers = new ContextPersistence();
			pers.mark(u);
			if(inlineForm){
				if(insert){
					UISimpleValues v = new UISimpleValues();
					for(UIControlElement c : controls){
						String va =c.getControlValue();
						if(c.isReference() || c.getAttribute().getCAttributetype().getId()==9)
							va=c.getTrueValue();
						if(c.getAttribute().getCAttributetype().getId() == 10 
								|| c.getAttribute().getCAttributetype().getId() == 11){
							for(PairKVElement e : c.getListReference())
								if(e.getKey().equals(c.getControlValue())){
									va = e.getValue();
									break;
								}
						}
						if(c.isCtrlDate() && c.getControlValue() != null && c.getControlValue().length()>0){
							DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
							va = df.format(c.getDateValue());
						}
						v.getValue().add(va);
					}
					values.add(v);
					MTMService service = new MTMService();
					CBusinessClass entity = new CBusinessClass();
					entity.setDataReference(window.getMainEntity());
					List<MtmDTO> dtosm = service.getMtm(window, entity);
					mtmBlocks = populate(dtosm);
					loadVoidComponents();
					loadVoidRefComponenets();
					saved=true;
				}
			}
			
			if(insert){
				loadVoidComponents();
			}
		} 
		
		return null;
	}

	

	private List<MtmDTO> getFullDTOS() {

		List<MtmDTO> results = new ArrayList<MtmDTO>();
		ProtogenDataEngine engine = new ProtogenDataEngine();

		for(MtmBlock block : mtmBlocks){
			//	Get the correspondant dto
			if(!block.isVisited())
				continue;
			MtmDTO dto = new MtmDTO();
			dto.setMtmEntity(block.getEntity());
			dto.setMtmData(new ArrayList<Map<CAttribute,Object>>());
			for(MtmLine line : block.getLines()){
				if(line.isTemporary())
					continue;
				Map<CAttribute, Object> data = new HashMap<CAttribute, Object>();
				for(PairKVElement e : line.getValues()){
					String dataref = e.getKey();
					for(CAttribute a : block.getEntity().getAttributes())
						if(a.getDataReference().equals(dataref)){
							if(a.isAutoValue())
								continue;
							if(!a.isReference()){
								if(a.getCAttributetype().getId() == 5){
									String h = "0000";
									if(e.getValue()!=null && e.getValue().length()>0 && e.getValue().split(":").length>1)
										h = e.getValue().split(":")[0]+e.getValue().split(":")[1];
									data.put(a, h);
								} else if(a.getCAttributetype().getId() == 3){
									String d = "0000-00-00 00:00:00+00";
									if(e.getValue()!=null && e.getValue().length()>0){
										d = e.getFormattedDateValue().split("/")[2]+"-"+e.getFormattedDateValue().split("/")[1]+
												"-"+e.getFormattedDateValue().split("/")[0]+" 00:00:00+00";
									}
									data.put(a, d);
								}
								else
									data.put(a, e.getValue());
								
							}
							else {
								ApplicationLoader dal = new ApplicationLoader();
								CBusinessClass ce = dal.getEntity(a.getDataReference().substring(3));
								ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
								List<PairKVElement> refvals = engine.getDataKeys(a.getDataReference().substring(3),(ce.getUserRestrict()=='Y'),cache.getUser().getId());

								
								for(PairKVElement kv : refvals)
								{
									String v1 = kv.getValue().trim().replaceAll(" ", "").replaceAll("\t", "");
									String v2 = e.getValue().trim().replaceAll(" ", "").replaceAll("\t", "");
									if(v1.equals(v2)){
										data.put(a, kv.getKey());
									}
								}
							}
						}
				}
				
				dto.getMtmData().add(data);
			}
			results.add(dto);
		}
		
		
		return results;
	}
	
	@SuppressWarnings("unused")
	private List<MtmDTO> getDTOS() {
		List<MtmDTO> results = new ArrayList<MtmDTO>();
		ProtogenDataEngine engine = new ProtogenDataEngine();

		for(MtmBlock block : mtmBlocks){
			//	Get the correspondant dto
			MtmDTO dto = new MtmDTO();
			dto.setMtmEntity(block.getEntity());
			dto.setMtmData(new ArrayList<Map<CAttribute,Object>>());
			for(MtmLine line : block.getLines()){
				Map<CAttribute, Object> data = new HashMap<CAttribute, Object>();
				for(PairKVElement e : line.getValues()){
					String dataref = e.getKey();
					for(CAttribute a : block.getEntity().getAttributes()){
						if(a.getDataReference().equals(dataref)){
							if(a.isAutoValue())
								continue;
							if(!a.isReference()){
								if(a.getCAttributetype().getId() == 5){
									String h = "0000";
									if(e.getValue()!=null && e.getValue().length()>0)
										h = e.getValue().split(":")[0]+e.getValue().split(":")[1];
									data.put(a, h);
								} else if(a.getCAttributetype().getId() == 3){
									String d = "0000-00-00 00:00:00+00";
									if(e.getValue()!=null && e.getValue().length()>0)
										d = e.getValue().split("\\")[2]+"-"+e.getValue().split("\\")[1]+"-"+e.getValue().split("\\")[0]+" 00:00:00+00";
									data.put(a, d);
								}
								else
									data.put(a, e.getValue());
							}
							else {
								ApplicationLoader dal = new ApplicationLoader();
								CBusinessClass ce = dal.getEntity(a.getDataReference().substring(3));
								ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
								List<PairKVElement> refvals = engine.getDataKeys(a.getDataReference().substring(3),(ce.getUserRestrict()=='Y'),cache.getUser().getId());

								for(PairKVElement kv : refvals)
								{
									if(kv.getValue().equals(e.getValue())){
										data.put(a, kv.getKey());
									}
								}
							}
						}
					}
				}
				
				//	Get FK1
				String reftable = "fk_"+block.getEntity().getDataReference()+"_"+this.window.getMainEntity();
				for(CAttribute a : block.getEntity().getAttributes())
					if(a.getDataReference().equals(reftable))
							data.put(a, line.getId()+"");
				dto.getMtmData().add(data);
			}
			results.add(dto);
		}
		
		
		return results;
	}


	public void handleDateSelect() {
		//checkingChangeListener();
		return;
	}
	public void saveTextStatus(AjaxBehaviorEvent e){
		/*checkingChangeListener();
		ContextPersistence pers = new ContextPersistence();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		pers.persist(u, window, controlLines);*/
		e.getComponent();
		return;
	}


	public String updateSelection(){
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		selectedMtmBlock.setIndex(Integer.parseInt(params.get("selected")));
		return "";
	}
	public void onTabChange(TabChangeEvent event){
		selectedMtmBlock.setIndex(Integer.parseInt(event.getTab().getId().split("_")[1]));	
	}

	public String voiddo(){
		String dummy="";
		return dummy;
	}
	
	public void newLine(){
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int blockId = Integer.parseInt(params.get("SEL_BLOCK"));
		MtmBlock block = null;
		for(MtmBlock b : mtmBlocks)
			if(b.getEntityID() == blockId){
				block = b;
				break;
			}
		
		if(block == null)
			return;
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		
		MtmLine l = new MtmLine();
		l.setKey(block.getEntityID()+"-"+block.getLines().size());
		l.setValues(new ArrayList<PairKVElement>());
		l.setTemporary(true);
		l.setControls(new ArrayList<UIControlElement>());
		for(CAttribute a : block.getEntity().getAttributes()){
			if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
				continue;
			PairKVElement pkv=new PairKVElement(a.getDataReference(), "");
			l.getValues().add(pkv);
			
			if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
				continue;
			if(a.isAutoValue())
				continue;
			if(!a.isReference()){
				if(a.getCAttributetype().getId() == 3){ //	Date
					UIControlElement e = new UIControlElement();
					e.setAttribute(a);
					e.setControlID(a.getDataReference());
					e.setLabel(a.getAttribute());
					e.setCtrlDate(true);
					e.setControlValue("");
					l.getControls().add(e);
				} else if (a.getCAttributetype().getId() == 12) {	//Boolean
					UIControlElement e = new UIControlElement();
					e.setAttribute(a);
					e.setControlID(a.getDataReference());
					e.setLabel(a.getAttribute());
					e.setBooleanValue(false);
					e.setControlValue("Non");
					l.getControls().add(e);
				} else {
					UIControlElement e = new UIControlElement();
					e.setAttribute(a);
					e.setControlID(a.getDataReference());
					e.setLabel(a.getAttribute());
					e.setControlValue("");
					l.getControls().add(e);
				}
			} else {
				UIControlElement element = new UIControlElement();
				
				ApplicationLoader dal = new ApplicationLoader();
				CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),cache.getUser().getId());
				
				UIControlsLine inlines = this.prepareInlineComponents(a.getDataReference(), ""+a.getId());
				element.setInlineControlLines(inlines);
				element.setAttribute(a);
				element.setControlID(a.getDataReference());
				element.setLabel(a.getAttribute().replaceAll("ID ",""));
				element.setControlValue("");
				element.setListReference(listElements);
				element.setReference(true);
				String uid = block.getEntityID()+"_"+a.getId()+"_"+block.getLines().indexOf(l);
				element.setUniqueID(uid);
				l.getControls().add(element);
			}
		}
		
		
		
		block.getLines().add(l);
	}
	
	public String doDelete(){
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int lineId = Integer.parseInt(params.get("todel"));
		mtmBlock.getLines().remove(lineId);
		
		return "";
	}
	
	public void doValidate(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int index = Integer.parseInt(
				params.get("LINE_INDEX")
				);
		
		MtmBlock block = mtmBlock;

		
		MtmLine line = block.getLines().get(index);
		line.setTemporary(false);
		for(UIControlElement e : line.getControls()){
			if(!e.isReference()){
				if(e.getAttribute().isCalculated()){
					double value=0;
					//	Calculus Vudu
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
					try {
						value=engine.evaluateMtm(line.getControls(),e);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					e.setControlValue(""+value);
				}
				if(e.getAttribute().isRequiresValidation()){
					double value=0;
					//	Calculus Vudu
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
					try {
						value=engine.evaluateFormula(line.getControls(),e,new ArrayList<MtmDTO>(), window.getAppKey());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					if(value==0){
						FacesContext context = FacesContext.getCurrentInstance();  
				          
				        context.addMessage(null, new FacesMessage("Erreur", "La valeur de "+e.getControlID()+" est incorrecte"));  
				        return;
					}
				}
				PairKVElement pkv = new PairKVElement(e.getControlID(), e.getControlValue());
				
				if(e.getControlValue()!=null && e.getControlValue().length()>0 && e.getAttribute().getCAttributetype().getId()==3){
					pkv.setDate(true);
					String dval = e.getControlValue();
					Date d = e.getDateValue();
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					dval = c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR);
					pkv.setFormattedDateValue(dval);
				
				}
				line.getValues().add(pkv);
			}
			else {
				line.getValues().add(new PairKVElement(e.getControlID(), e.getTrueValue()));
			}
		}
	}
	
	public void mtmAdd(){
		
		int blockID = selectedMtmBlock.getIndex();
		MtmBlock block = null;

		if(blockID==0){
			//	first one
			block = mtmBlocks.get(0);
		}
		for(MtmBlock b : mtmBlocks){
			if(b.getEntityID() == blockID){
				block = b;
				break;
			}			
		}
		
		
		MtmLine line = new MtmLine();
		
		line.setValues(new ArrayList<PairKVElement>());
		for(UIControlElement e : block.getControls()){
			if(!e.isReference()){
				if(e.getAttribute().isCalculated()){
					double value=0;
					//	Calculus Vudu
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
					try {
						value=engine.evaluateMtm(block.getControls(),e);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					e.setControlValue(""+value);
				}
				if(e.getAttribute().isRequiresValidation()){
					double value=0;
					//	Calculus Vudu
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
					try {
						value=engine.evaluateFormula(block.getControls(),e,new ArrayList<MtmDTO>(), window.getAppKey());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					if(value==0){
						FacesContext context = FacesContext.getCurrentInstance();  
				          
				        context.addMessage(null, new FacesMessage("Erreur", "La valeur de "+e.getControlID()+" est incorrecte"));  
				        return;
					}
				}
				PairKVElement pkv = new PairKVElement(e.getControlID(), e.getControlValue());
				
				if(e.getControlValue()!=null && e.getControlValue().length()>0 && e.getAttribute().getCAttributetype().getId()==3){
					pkv.setDate(true);
					String dval = e.getControlValue();
					Date d = e.getDateValue();
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					dval = c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR);
					pkv.setFormattedDateValue(dval);
				
				}
				line.getValues().add(pkv);
			}
			else {
				line.getValues().add(new PairKVElement(e.getControlID(), e.getTrueValue()));
			}
		}
		
		block.getLines().add(line);
		
		checkingChangeListener();
	}
	
private Map<String, String> addReferences(CoreUser u){
		
	Map<String, String> refForeignKeys = new  LinkedHashMap<String, String>();
		ProtogenDataEngine gen = new ProtogenDataEngine();
		for(MtmBlock block : refBlocks){
					
			MtmLine line = new MtmLine();
			
			line.setValues(new ArrayList<PairKVElement>());
			for(UIControlElement e : block.getControls()){
				if(!e.isReference()){
					if(e.getAttribute().isCalculated()){
						double value=0;
						//	Calculus Vudu
						FacesContext fc = FacesContext.getCurrentInstance();
						ExternalContext ec = fc.getExternalContext();
						
						CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
						try {
							value=engine.evaluateMtm(block.getControls(),e);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						e.setControlValue(""+value);
					}
					if(e.getAttribute().isRequiresValidation()){
						double value=0;
						//	Calculus Vudu
						FacesContext fc = FacesContext.getCurrentInstance();
						ExternalContext ec = fc.getExternalContext();
						
						CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
						try {
							value=engine.evaluateFormula(block.getControls(),e,new ArrayList<MtmDTO>(), window.getAppKey());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						
						if(value==0){
							FacesContext context = FacesContext.getCurrentInstance();  
							
							context.addMessage(null, new FacesMessage("Erreur", "La valeur de "+e.getControlID()+" est incorrecte"));  
							return null;
						}
					}
					PairKVElement pkv = new PairKVElement(e.getControlID(), e.getControlValue());
					
					if(e.getAttribute().getCAttributetype().getId()==3){
						pkv.setDate(true);
						String dval = e.getControlValue();
						if(dval!= null && !dval.equals("")){
							Date d = e.getDateValue();
							Calendar c = Calendar.getInstance();
							c.setTime(d);
							dval = c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+"/"+c.get(Calendar.YEAR);
							
						} else {
							dval = "";
						}
						pkv.setFormattedDateValue(dval);
						
					}
					line.getValues().add(pkv);
				}
				else {
					line.getValues().add(new PairKVElement(e.getControlID(), e.getTrueValue()));
				}
			}
			//block.getLines().add(line);
			Map<String,String> vals = gen.executeInsertReference(window, block, u);
			if(vals!=null & vals.size()>0)
				refForeignKeys.putAll(vals);
		}
		return refForeignKeys;
		
		
	}
	
	public void selChanged(AjaxBehaviorEvent  e){
		String ref = selectedReference;
		ref = ref+"";
		return;
	}

	/*
	 * INLINE REFERENCES CREATION
	 */
	public void loadInlineComponents() {
		//	Get Data
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String datareference = params.get("inlineDataReference");
		@SuppressWarnings("unused")
		String inlineAttId = params.get("inlineAttId");
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationLoader dal = new ApplicationLoader();
		toCreateEntity = dal.getEntity(datareference.substring(3));
		List<CAttribute> entityAttributes = toCreateEntity.getAttributes();				
		String activeTab = params.get("mtmblock");
		try{
			activeMtmBlockId = Integer.parseInt(activeTab);
		}catch(NumberFormatException nfe) {
			activeMtmBlockId = 0;
		}
		
		//	Construct controls
		inlineCreation = new ArrayList<UIControlElement>();
		icLines = new UIControlsLine();
		references = new ArrayList<String>();
		List<String> entities = new ArrayList<String>();
		for(CAttribute attribute : entityAttributes){
			if(attribute.getDataReference().startsWith("pk_") || attribute.isMultiple()  )
				continue;
			if(attribute.isAutoValue())
				continue;
			UIControlElement element = new UIControlElement();
			if(entities.contains(attribute.getEntity().getDataReference())){
				element.setTitle("");
				element.setVisible(false);
			} else {
				element.setTitle(attribute.getEntity().getName());
				element.setVisible(true);
				entities.add(attribute.getEntity().getDataReference());
			}
			
			String type = attribute.getCAttributetype().getType();
			if(attribute.isReference()){
				
				String entity = attribute.getEntity().getDataReference();
				if(references.contains(entity))
					continue;
				
				
				String referenceTable = attribute.getDataReference().substring(3);
				
				//	Add all search references
				addSearchReferences(referenceTable);
				
				references.add(referenceTable);
				String referencedEntity = dal.getEntityFromDR(referenceTable);
				
				CBusinessClass e = dal.getEntity(referenceTable);
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				List<PairKVElement> list = engine.getDataKeys(referenceTable,(e.getUserRestrict()=='Y'),cache.getUser().getId());
				
				element.setReferenceTable(referencedEntity);
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				for(PairKVElement kv : list){
					listElements.add(kv);
				}
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute().replaceAll("ID ", ""));
				element.setControlValue("");
				element.setListReference(listElements);
				element.setReference(true);
				element.setFiltrable(listElements.size()>10);
				inlineCreation.add(element);
				icLines.addControl(element);
				continue;
			}
			if(references.contains(attribute.getEntity().getDataReference())){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				element.setVisible(false);
				element.setReadOnly(true);
				inlineCreation.add(element);
				icLines.addControl(element);
				continue;
			}
			if(type.equals("Entier")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if(type.equals("Heure")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (type.equals("Texte")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (type.equals("Date")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
				element.setCtrlDate(true);
			} else if (type.equals("Double")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			}  else if(attribute.getCAttributetype().getId()==7) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				CoreUser u = cache.getUser();
				element.setControlValue(u.getFirstName()+" - "+u.getLastName());
				element.setTrueValue(u.getId()+"");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (attribute.getCAttributetype().getId()==8){
				element.setMoney(true);
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (attribute.getCAttributetype().getId()==9){
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (attribute.getCAttributetype().getId()==12){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("Non");
				inlineCreation.add(element);
				icLines.addControl(element);
			}
			
		} 
		inlineReferenceChange();
	}
	
	public UIControlsLine prepareInlineComponents(String datareference, String inlineAttId) {
		//	Get Data
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationLoader dal = new ApplicationLoader();
		toCreateEntity = dal.getEntity(datareference.substring(3));
		List<CAttribute> entityAttributes = toCreateEntity.getAttributes();				
				
		//	Construct controls
		List<UIControlElement> inlineCreation = new ArrayList<UIControlElement>();
		UIControlsLine icLines = new UIControlsLine();
		List<String> references = new ArrayList<String>();
		List<String> entities = new ArrayList<String>();
		for(CAttribute attribute : entityAttributes){
			if(attribute.getDataReference().startsWith("pk_") || attribute.isMultiple()  )
				continue;
			if(attribute.isAutoValue())
				continue;
			UIControlElement element = new UIControlElement();
			if(entities.contains(attribute.getEntity().getDataReference())){
				element.setTitle("");
				element.setVisible(false);
			} else {
				element.setTitle(attribute.getEntity().getName());
				element.setVisible(true);
				entities.add(attribute.getEntity().getDataReference());
			}
			
			String type = attribute.getCAttributetype().getType();
			if(attribute.isReference()){
				
				String entity = attribute.getEntity().getDataReference();
				if(references.contains(entity))
					continue;
				
				
				String referenceTable = attribute.getDataReference().substring(3);
				
				//	Add all search references
				addSearchReferences(referenceTable);
				
				references.add(referenceTable);
				String referencedEntity = dal.getEntityFromDR(referenceTable);
				
				CBusinessClass e = dal.getEntity(referenceTable);
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				List<PairKVElement> list = engine.getDataKeys(referenceTable,(e.getUserRestrict()=='Y'),cache.getUser().getId());
				
				element.setReferenceTable(referencedEntity);
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				for(PairKVElement kv : list){
					listElements.add(kv);
				}
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute().replaceAll("ID ", ""));
				element.setControlValue("");
				element.setListReference(listElements);
				element.setReference(true);
				element.setFiltrable(listElements.size()>10);
				inlineCreation.add(element);
				icLines.addControl(element);
				continue;
			}
			if(references.contains(attribute.getEntity().getDataReference())){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				element.setVisible(false);
				element.setReadOnly(true);
				inlineCreation.add(element);
				icLines.addControl(element);
				continue;
			}
			if(type.equals("Entier")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if(type.equals("Heure")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (type.equals("Texte")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (type.equals("Date")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
				element.setCtrlDate(true);
			} else if (type.equals("Double")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			}  else if(attribute.getCAttributetype().getId()==7) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				CoreUser u = cache.getUser();
				element.setControlValue(u.getFirstName()+" - "+u.getLastName());
				element.setTrueValue(u.getId()+"");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (attribute.getCAttributetype().getId()==8){
				element.setMoney(true);
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (attribute.getCAttributetype().getId()==9){
				
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				inlineCreation.add(element);
				icLines.addControl(element);
			} else if (attribute.getCAttributetype().getId()==12){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("Non");
				inlineCreation.add(element);
				icLines.addControl(element);
			}
			
		} 
		return icLines;
		
	}
	
	public void inlineReferenceChange(){
		String dummy="";
		dummy=dummy+"";
		
	}
	
	public void updateInlineForm(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String attributeDataReference = params.get("INSERTATT");
		String blockID = params.get("INSERTBLK"); 
		UIControlElement element = null;
		if(blockID.equals("-1")){
			//	MAIN FORM
			for(UIControlElement e : controls){
				if(e.getAttribute().getDataReference().equals(attributeDataReference)){
					element = e;
					break;
				}
			}
		} else {
			//	MTM
			int eid = Integer.parseInt(blockID);
			for(MtmBlock b : mtmBlocks){
				if(b.getEntityID() == eid){
					for(UIControlElement e : b.getControls()){
						if(e.getAttribute().getDataReference().equals(attributeDataReference)){
							element = e;
							break;
						}
					}
					break;
				}
			}
			for(MtmBlock b : refBlocks){
				if(b.getEntityID() == eid){
					for(UIControlElement e : b.getControls()){
						if(e.getAttribute().getDataReference().equals(attributeDataReference)){
							element = e;
							break;
						}
					}
					break;
				}
			}
			
		}
		
		if(element == null)
			return;
		
		controlToCreate = element;
	}
	
	public void saveInline(){
		UIControlElement element = controlToCreate;
		String attributeDataReference = element.getAttribute().getDataReference();
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		
		toCreateEntity = engine.getReferencedTable(attributeDataReference.substring(3));
		inlineCreation = new ArrayList<UIControlElement>();
		
		for(UIControlSingle ls : element.getInlineControlLines().getCtlines())
			inlineCreation.addAll(ls.getControls());
		
		int id = engine.insertNewReference(toCreateEntity,inlineCreation);
		PairKVElement pkv = engine.getDataKeyByID(toCreateEntity.getDataReference(), id);
		//checkingChangeListener();
		
		if(id>0){
			for(UIControlElement e : controls){
				if(!e.isReference() || e.getAttribute().getId()==0)
					continue;
				
				if(e.getAttribute().getDataReference().equals("fk_"+toCreateEntity.getDataReference())){
					e.getListReference().add(pkv);
					e.setTrueValue(pkv.getValue());
					e.setControlValue(""+id);
					
				}
			}
			for(MtmBlock block : mtmBlocks){
				for(UIControlElement e : block.getControls()){
					if(!e.isReference() || e.getAttribute().getId()==0)
						continue;
					
					if(e.getAttribute().getDataReference().equals("fk_"+toCreateEntity.getDataReference())){
						e.getListReference().add(pkv);
						e.setTrueValue(pkv.getValue());
						e.setControlValue(""+id);
						
					}
				}
				for(MtmLine l : block.getLines()){
					for(UIControlElement e : l.getControls()){
						if(!e.isReference() || e.getAttribute().getId()==0)
							continue;
						
						if(e.getAttribute().getDataReference().equals("fk_"+toCreateEntity.getDataReference())){
							e.getListReference().add(pkv);
							e.setTrueValue(pkv.getValue());
							e.setControlValue(""+id);
						}
					}
				}
			}
			for(MtmBlock block : refBlocks){
				for(UIControlElement e : block.getControls()){
					if(!e.isReference() || e.getAttribute().getId()==0)
						continue;
					
					if(e.getAttribute().getDataReference().equals("fk_"+toCreateEntity.getDataReference())){
						e.getListReference().add(pkv);
						e.setTrueValue(pkv.getValue());
						e.setControlValue(""+id);
					}
				}
			}
		}
		
		checkingChangeListener();
		for(UIControlElement c : inlineCreation){
			
			if(c.isReference() && c.getListReference()!= null && c.getListReference().size()>0)
				c.setControlValue(c.getListReference().get(0).getKey());
		}
		
		inlineCreation = new ArrayList<UIControlElement>();
		controlToCreate = null;
	}
	
	
	/*
	 *	Profiles and roles 
	 */
	private void prepareRolesProfiles(ApplicationCache cache) {
		HabilitationsService hsrv = new HabilitationsService();
		userRepresentative = hsrv.checkForUserBinding(window.getMainEntity(), cache);
		
		profilsList = hsrv.loadProfils(cache.getAppKey());
		rolesList = new ArrayList<CoreRole>();
		
		List<String> profilsStart = new ArrayList<String>();
		List<String> profilsEnd = new ArrayList<String>();
		List<String> rolesStart = new ArrayList<String>();
		List<String> rolesEnd = new ArrayList<String>();
		
		for(CoreProfil r : profilsList)
			profilsStart.add(r.getLibelle());
		
		picklistProfils = new DualListModel<String>(profilsStart, profilsEnd);
		picklistRoles = new DualListModel<String>(rolesStart, rolesEnd);
		
		//	In case of an update I need to load the username,roles and profiles
		if(insert)
			return;
		
		CoreUser u = hsrv.loadBoundUser(window.getMainEntity(), cache,dbID);
		userName = u.getLogin();
		
		List<Integer> idRoles = hsrv.loadUsersRoles(u.getId());
		List<Integer> idProfils = hsrv.loadUsersProfiles(u.getId());
		
		List<CoreRole> actualProfiles = new ArrayList<CoreRole>();
		for(Integer I : idRoles){
			if(profils == null)
				break;
			for(CoreRole p : profils){
				if(p.getId() == I.intValue()){
					actualProfiles.add(p);
					picklistRoles.getTarget().add(p.getRole());
					picklistRoles.getSource().remove(p.getRole());
					break;
				}
			}
		}
		
		List<CoreProfil> actualProfils = hsrv.loadRoles(actualProfiles);
		for(CoreProfil p : actualProfils){
			picklistProfils.getSource().add(p.getLibelle());
			for(Integer I : idProfils){
				if(I.intValue() == p.getId()){
					picklistProfils.getTarget().add(p.getLibelle());
					picklistProfils.getSource().remove(p.getLibelle());
					break;
				}
			}
		}
	}

	public void onTransfer(TransferEvent evt){
		List<CoreProfil> tprofils= new ArrayList<CoreProfil>();
		for(Object sp : evt.getItems())
			for(CoreProfil p : profilsList)
				if(p.getLibelle().equals(sp.toString())){
					tprofils.add(p);
					break;
				}
		HabilitationsService hsrv = new HabilitationsService();
		rolesList = hsrv.loadRolesByProfil(tprofils);
		
		List<String> rolesStart = new ArrayList<String>();
		List<String> rolesEnd = new ArrayList<String>();
		for(CoreRole p : rolesList)
			rolesStart.add(p.getRole());
		
		picklistRoles = new DualListModel<String>(rolesStart, rolesEnd);
	}
	
	public void onDummyTransfer(TransferEvent evt){
		String dummy = "";
		dummy=dummy+"";
	}
	
	private void updateRolesProfiles(int id) {
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		CoreUser us = new CoreUser();
		us.setEmail(userName);
		us.setLogin(userName);
		us.setPassword("1234");
		us.setBoundEntity(dbID);
		us.setAppKey(u.getAppKey());
		us.setOrganization(u.getOrganization());
		us.setOrgInstance(u.getOrgInstance());
		us.setThemeColor(u.getThemeColor());
		us.setThemeStyle(u.getThemeStyle());
		us.setUserTheme(u.getUserTheme());
		
		//	RAZ habilitation
		HabilitationsService srv = new HabilitationsService();
		srv.RAZ(window.getMainEntity(), dbID);
		
		//	Update profiles
		List<CoreRole> selectedRoles = new ArrayList<CoreRole>();
		for(String itm : picklistRoles.getTarget()){
			for(CoreRole r : rolesList)
				if(r.getRole().equals(itm)){
					selectedRoles.add(r);
					break;
				}
		}
		
		srv.majRoles(window.getMainEntity(), id, selectedRoles, insert, us);
		
		//	Update roles
		List<CoreProfil> selectedProfils = new ArrayList<CoreProfil>();
		for(String itm : picklistProfils.getTarget()){
			for(CoreProfil r : profilsList)
				if(r.getLibelle().equals(itm)){
					selectedProfils.add(r);
					break;
				}
		}
		
		srv.majProfils(window.getMainEntity(), id, selectedProfils, selectedRoles, insert);
	}

	public void ajaxValidate(){
		String dummy = "";
		dummy=dummy+"";
	}
	
	/*
	 * Historisation
	 */
	public void histoChange(){
		String hi = "";
		hi = hi+histoStart;
	}
	
	/*
	 * Single row mode
	 */
	public String doPrimeDelete(){
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		
		engine.deleteSimple(window, dbID);
		
		return "protogen-listview";
	}
	
	/*
	 * 	GETTERS AND SETTERS
	 */
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public List<UIControlElement> getControls() {
		return controls;
	}
	public void setControls(List<UIControlElement> controls) {
		this.controls = controls;
	}

	public String getWindowTitle() {
		return windowTitle;
	}

	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public String getWindowDescription() {
		return windowDescription;
	}

	public void setWindowDescription(String windowDescription) {
		this.windowDescription = windowDescription;
	}

	public String getWindowPercentage() {
		return windowPercentage;
	}

	public void setWindowPercentage(String windowPercentage) {
		this.windowPercentage = windowPercentage;
	}

	public String getWindowHelp() {
		return windowHelp;
	}

	public void setWindowHelp(String windowHelp) {
		this.windowHelp = windowHelp;
	}

	public String getProcessMessage() {
		return processMessage;
	}

	public void setProcessMessage(String processMessage) {
		this.processMessage = processMessage;
	}

	public String getMessageClass() {
		return messageClass;
	}

	public void setMessageClass(String messageClass) {
		this.messageClass = messageClass;
	}

	public DBFormattedObjects getInitialData() {
		return initialData;
	}

	public void setInitialData(DBFormattedObjects initialData) {
		this.initialData = initialData;
	}

	public int getDbID() {
		return dbID;
	}

	public void setDbID(int dbID) {
		this.dbID = dbID;
	}

	public List<MtmBlock> getMtmBlocks() {
		return mtmBlocks;
	}

	public void setMtmBlocks(List<MtmBlock> mtmBlocks) {
		this.mtmBlocks = mtmBlocks;
	}

	public SelectedItem getSelectedMtmBlock() {
		return selectedMtmBlock;
	}

	public void setSelectedMtmBlock(SelectedItem selectedMtmBlock) {
		this.selectedMtmBlock = selectedMtmBlock;
	}

	public String getSelectedReference() {
		return selectedReference;
	}

	public void setSelectedReference(String selectedReference) {
		this.selectedReference = selectedReference;
	}





	public boolean isInProcess() {
		return inProcess;
	}





	public void setInProcess(boolean inProcess) {
		this.inProcess = inProcess;
	}





	public CAttribute getSelectedAttribute() {
		return selectedAttribute;
	}





	public void setSelectedAttribute(CAttribute selectedAttribute) {
		this.selectedAttribute = selectedAttribute;
	}





	public List<UIControlElement> getGlobalControls() {
		return globalControls;
	}





	public void setGlobalControls(List<UIControlElement> globalControls) {
		this.globalControls = globalControls;
	}


	public UIControlElement getUploadTo() {
		return uploadTo;
	}


	public void setUploadTo(UIControlElement uploadTo) {
		this.uploadTo = uploadTo;
	}


	public Map<String, String> getForeignKeys() {
		return foreignKeys;
	}


	public void setForeignKeys(Map<String, String> foreignKeys) {
		this.foreignKeys = foreignKeys;
	}


	public boolean isNonVoidContent() {
		return nonVoidContent;
	}


	public void setNonVoidContent(boolean nonVoidContent) {
		this.nonVoidContent = nonVoidContent;
	}


	public String getFileExtension() {
		return fileExtension;
	}


	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}


	public StreamedContent getFile() {
		return file;
	}


	public void setFile(StreamedContent file) {
		this.file = file;
	}


	public boolean isFileFieldInprogress() {
		return fileFieldInprogress;
	}


	public void setFileFieldInprogress(boolean fileFIeldInprogress) {
		this.fileFieldInprogress = fileFIeldInprogress;
	}


	public UploadedFile getUploadFile() {
		return uploadFile;
	}


	public void setUploadFile(UploadedFile uploadFile) {
		this.uploadFile = uploadFile;
	}


	public List<String> getReferences() {
		return references;
	}


	public void setReferences(List<String> references) {
		this.references = references;
	}


	public boolean isRappelActivated() {
		return rappelActivated;
	}


	public void setRappelActivated(boolean rappelActivated) {
		this.rappelActivated = rappelActivated;
	}


	public List<UIControlElement> getRappelHistory() {
		return rappelHistory;
	}


	public void setRappelHistory(List<UIControlElement> rappelHistory) {
		this.rappelHistory = rappelHistory;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public String getMoneyCode() {
		return moneyCode;
	}


	public void setMoneyCode(String moneyCode) {
		this.moneyCode = moneyCode;
	}


	public boolean isInsert() {
		return insert;
	}


	public void setInsert(boolean insert) {
		this.insert = insert;
	}


	public UIControlsLine getControlLines() {
		return controlLines;
	}


	public void setControlLines(UIControlsLine controlLines) {
		this.controlLines = controlLines;
	}


	public boolean isInlineForm() {
		return inlineForm;
	}


	public void setInlineForm(boolean inlineForm) {
		this.inlineForm = inlineForm;
	}


	public List<String> getTitles() {
		return titles;
	}


	public void setTitles(List<String> titles) {
		this.titles = titles;
	}


	public List<UISimpleValues> getValues() {
		return values;
	}


	public void setValues(List<UISimpleValues> values) {
		this.values = values;
	}


	public boolean isSaved() {
		return saved;
	}


	public void setSaved(boolean saved) {
		this.saved = saved;
	}


	public List<UIControlElement> getInlineCreation() {
		return inlineCreation;
	}


	public void setInlineCreation(List<UIControlElement> inlineCreation) {
		this.inlineCreation = inlineCreation;
	}


	public UIControlsLine getIcLines() {
		return icLines;
	}


	public void setIcLines(UIControlsLine icLines) {
		this.icLines = icLines;
	}


	public CBusinessClass getToCreateEntity() {
		return toCreateEntity;
	}


	public void setToCreateEntity(CBusinessClass toCreateEntity) {
		this.toCreateEntity = toCreateEntity;
	}


	public List<SAlert> getAlerts() {
		return alerts;
	}


	public void setAlerts(List<SAlert> alerts) {
		this.alerts = alerts;
	}


	public String getUpdatedFields() {
		return updatedFields;
	}


	public void setUpdatedFields(String updatedFields) {
		this.updatedFields = updatedFields;
	}


	public boolean isParameteredEntity() {
		return parameteredEntity;
	}


	public void setParameteredEntity(boolean parameteredEntity) {
		this.parameteredEntity = parameteredEntity;
	}


	public CParameterMetamodel getParamodel() {
		return paramodel;
	}


	public void setParamodel(CParameterMetamodel paramodel) {
		this.paramodel = paramodel;
	}


	public Map<GParametersPackage, List<GParametersInstance>> getGraphicalParameters() {
		return graphicalParameters;
	}


	public void setGraphicalParameters(Map<GParametersPackage, List<GParametersInstance>> graphicalParameters) {
		this.graphicalParameters = graphicalParameters;
	}


	public List<GParametersComponent> getParametersComponents() {
		return parametersComponents;
	}


	public void setParametersComponents(List<GParametersComponent> parametersComponents) {
		this.parametersComponents = parametersComponents;
	}


	public GParametersComponent getSelectedComponent() {
		return selectedComponent;
	}


	public void setSelectedComponent(GParametersComponent selectedComponent) {
		this.selectedComponent = selectedComponent;
	}


	public String getSelectedInstance() {
		return selectedInstance;
	}


	public void setSelectedInstance(String selectedInstance) {
		this.selectedInstance = selectedInstance;
	}

	public String getSelectedComponentName() {
		return selectedComponentName;
	}

	public void setSelectedComponentName(String selectedComponentName) {
		this.selectedComponentName = selectedComponentName;
	}

	public GParametersPackage getSelectedPackage() {
		return selectedPackage;
	}

	public void setSelectedPackage(GParametersPackage selectedPackage) {
		this.selectedPackage = selectedPackage;
	}

	public boolean isUniqueRowMode() {
		return uniqueRowMode;
	}

	public void setUniqueRowMode(boolean uniqueRowMode) {
		this.uniqueRowMode = uniqueRowMode;
	}

	public CAttribute getUniqueTo() {
		return uniqueTo;
	}

	public void setUniqueTo(CAttribute uniqueTo) {
		this.uniqueTo = uniqueTo;
	}

	public String getUnicityReferenceTable() {
		return unicityReferenceTable;
	}

	public void setUnicityReferenceTable(String unicityReferenceTable) {
		this.unicityReferenceTable = unicityReferenceTable;
	}

	public CIdentificationRow getIdentificationRow() {
		return identificationRow;
	}

	public void setIdentificationRow(CIdentificationRow identificationRow) {
		this.identificationRow = identificationRow;
	}

	public boolean isDefaultChecked() {
		return defaultChecked;
	}

	public void setDefaultChecked(boolean defaultChecked) {
		this.defaultChecked = defaultChecked;
	}

	public boolean isCompositionMode() {
		return compositionMode;
	}

	public void setCompositionMode(boolean compositionMode) {
		this.compositionMode = compositionMode;
	}

	public CComposedBean getComposedBean() {
		return composedBean;
	}

	public void setComposedBean(CComposedBean composedBean) {
		this.composedBean = composedBean;
	}

	public CComposition getComposition() {
		return composition;
	}

	public void setComposition(CComposition composition) {
		this.composition = composition;
	}

	public List<PairKVElement> getComposables() {
		return composables;
	}

	public void setComposables(List<PairKVElement> composables) {
		this.composables = composables;
	}

	public DualListModel<String> getToCompose() {
		return toCompose;
	}

	public void setToCompose(DualListModel<String> toCompose) {
		this.toCompose = toCompose;
	}

	public List<String> getSourceComposition() {
		return sourceComposition;
	}

	public void setSourceComposition(List<String> sourceComposition) {
		this.sourceComposition = sourceComposition;
	}

	public List<String> getTargetComposition() {
		return targetComposition;
	}

	public void setTargetComposition(List<String> targetComposition) {
		this.targetComposition = targetComposition;
	}

	public MtmBlock getChangeMtmBlock() {
		return changeMtmBlock;
	}

	public void setChangeMtmBlock(MtmBlock changeMtmBlock) {
		this.changeMtmBlock = changeMtmBlock;
	}


	public boolean isUserRepresentative() {
		return userRepresentative;
	}


	public void setUserRepresentative(boolean userRepresentative) {
		this.userRepresentative = userRepresentative;
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

	public List<CoreRole> getProfils() {
		return profils;
	}

	public void setProfils(List<CoreRole> profils) {
		this.profils = profils;
	}

	public List<CoreProfil> getRoles() {
		return roles;
	}

	public void setRoles(List<CoreProfil> roles) {
		this.roles = roles;
	}

	public boolean isHistomode() {
		return histomode;
	}

	public void setHistomode(boolean histomode) {
		this.histomode = histomode;
	}

	public Date getHistoStart() {
		return histoStart;
	}

	public void setHistoStart(Date histoStart) {
		this.histoStart = histoStart;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public boolean isLocalizedMode() {
		return localizedMode;
	}

	public void setLocalizedMode(boolean localizedMode) {
		this.localizedMode = localizedMode;
	}

	public List<PairKVElement> getOrganizationLevels() {
		return organizationLevels;
	}

	public void setOrganizationLevels(List<PairKVElement> organizationLevels) {
		this.organizationLevels = organizationLevels;
	}

	public String getSelectedLevelId() {
		return selectedLevelId;
	}

	public void setSelectedLevelId(String selectedLevelId) {
		this.selectedLevelId = selectedLevelId;
	}

	public CLocalizedEntity getLocalizedEntity() {
		return localizedEntity;
	}

	public void setLocalizedEntity(CLocalizedEntity localizedEntity) {
		this.localizedEntity = localizedEntity;
	}

	public List<GOrganization> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<GOrganization> organisations) {
		this.organisations = organisations;
	}

	public Map<GOrganization, TreeNode> getOrgMap() {
		return orgMap;
	}

	public void setOrgMap(Map<GOrganization, TreeNode> orgMap) {
		this.orgMap = orgMap;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public TreeNode getSelectedOrg() {
		return selectedOrg;
	}

	public void setSelectedOrg(TreeNode selectedOrg) {
		this.selectedOrg = selectedOrg;
	}

	public List<CoreProfil> getProfilsList() {
		return profilsList;
	}

	public void setProfilsList(List<CoreProfil> profilsList) {
		this.profilsList = profilsList;
	}

	public List<CoreRole> getRolesList() {
		return rolesList;
	}

	public void setRolesList(List<CoreRole> rolesList) {
		this.rolesList = rolesList;
	}

	public CDataHistory getHistory() {
		return history;
	}

	public void setHistory(CDataHistory history) {
		this.history = history;
	}

	/**
	 * @return the refBlocks
	 */
	public List<MtmBlock> getRefBlocks() {
		return refBlocks;
	}

	/**
	 * @param refBlocks the refBlocks to set
	 */
	public void setRefBlocks(List<MtmBlock> refBlocks) {
		this.refBlocks = refBlocks;
	}

	/**
	 * @return the activeMtmBlockId
	 */
	public int getActiveMtmBlockId() {
		return activeMtmBlockId;
	}

	/**
	 * @param activeMtmBlockId the activeMtmBlockId to set
	 */
	public void setActiveMtmBlockId(int activeMtmBlockId) {
		this.activeMtmBlockId = activeMtmBlockId;
	}

	public boolean isAlphaMode() {
		return alphaMode;
	}

	public void setAlphaMode(boolean alphaMode) {
		this.alphaMode = alphaMode;
	}

	public String getAlphaEntity() {
		return alphaEntity;
	}

	public void setAlphaEntity(String alphaEntity) {
		this.alphaEntity = alphaEntity;
	}

	public String getAlphaReference() {
		return alphaReference;
	}

	public void setAlphaReference(String alphaReference) {
		this.alphaReference = alphaReference;
	}

	public boolean isNavigation() {
		return navigation;
	}

	public void setNavigation(boolean navigation) {
		this.navigation = navigation;
	}

	public boolean isSingleMode() {
		return singleMode;
	}

	public void setSingleMode(boolean singleMode) {
		this.singleMode = singleMode;
	}

	public int getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(int activeIndex) {
		this.activeIndex = activeIndex;
	}

	public UIControlElement getControlToCreate() {
		return controlToCreate;
	}

	public void setControlToCreate(UIControlElement controlToCreate) {
		this.controlToCreate = controlToCreate;
	}

	public MtmBlock getMtmBlock() {
		return mtmBlock;
	}

	public void setMtmBlock(MtmBlock mtmBlock) {
		this.mtmBlock = mtmBlock;
	}

	public UIControlElement getUictrl() {
		return uictrl;
	}

	public void setUictrl(UIControlElement uictrl) {
		this.uictrl = uictrl;
	}

	public MtmLine getMtmLine() {
		return mtmLine;
	}

	public void setMtmLine(MtmLine mtmLine) {
		this.mtmLine = mtmLine;
	}

	public boolean isMtmEnabled() {
		return mtmEnabled;
	}

	public void setMtmEnabled(boolean mtmEnabled) {
		this.mtmEnabled = mtmEnabled;
	}

	public boolean isTabsShown(){
		return mtmBlocks!=null && mtmBlocks.size()>0;
	}

	public UIControlElement getCurrentControl() {
		return currentControl;
	}

	public void setCurrentControl(UIControlElement currentControl) {
		this.currentControl = currentControl;
	}
	
}
