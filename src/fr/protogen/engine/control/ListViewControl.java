package fr.protogen.engine.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.itextpdf.text.pdf.codec.Base64;
import com.thoughtworks.xstream.XStream;

import fr.protogen.callout.service.CalloutEngine;
import fr.protogen.callout.service.RemoteCalloutService;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.control.process.ProcessScreenListener;
import fr.protogen.engine.control.ui.MailDTO;
import fr.protogen.engine.control.ui.MtmBlock;
import fr.protogen.engine.control.ui.MtmLine;
import fr.protogen.engine.control.ui.RowFileSystemElement;
import fr.protogen.engine.control.ui.RowFolder;
import fr.protogen.engine.control.ui.SynthesisTab;
import fr.protogen.engine.filestore.CMSService;
import fr.protogen.engine.gexpression.ExpressionParserEngine;
import fr.protogen.engine.reporting.ExcelExportEngine;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.CalendarUtils;
import fr.protogen.engine.utils.EntityDTO;
import fr.protogen.engine.utils.HeaderExecutionResult;
import fr.protogen.engine.utils.ListKV;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.engine.utils.ResourceManager;
import fr.protogen.engine.utils.ScreenDataHistory;
import fr.protogen.engine.utils.SimpleDataTable;
import fr.protogen.engine.utils.StringFormat;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.engine.utils.UIFilterElement;
import fr.protogen.masterdata.DAO.AlertDataAccess;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.LocalizationEngine;
import fr.protogen.masterdata.model.ActionBatch;
import fr.protogen.masterdata.model.AlertInstance;
import fr.protogen.masterdata.model.ButtonParameter;
import fr.protogen.masterdata.model.CActionbutton;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CCalloutArguments;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CFolder;
import fr.protogen.masterdata.model.CLVSField;
import fr.protogen.masterdata.model.CLVSTable;
import fr.protogen.masterdata.model.CListViewSynthesis;
import fr.protogen.masterdata.model.CParameterMetamodel;
import fr.protogen.masterdata.model.CView;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CWindowCallout;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.MAction;
import fr.protogen.masterdata.model.MPostAction;
import fr.protogen.masterdata.model.OCRDriverBean;
import fr.protogen.masterdata.model.OCRHistory;
import fr.protogen.masterdata.model.RowDocument;
import fr.protogen.masterdata.model.SAlert;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.StoredFile;
import fr.protogen.masterdata.model.StoredFileType;
import fr.protogen.masterdata.model.WFData;
import fr.protogen.masterdata.model.WorkflowDefinition;
import fr.protogen.masterdata.services.FileStoreService;
import fr.protogen.masterdata.services.HabilitationsService;
import fr.protogen.masterdata.services.MTMService;
import fr.protogen.masterdata.services.PostValidationEngine;
import fr.protogen.masterdata.services.UserServices;
import fr.protogen.masterdata.services.ViewsService;
import fr.protogen.masterdata.services.WorkflowEngine;
import fr.protogen.ocr.pojo.Document;

@ManagedBean
@ViewScoped
public class ListViewControl implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8446726964395058048L;
	//	Static layout parameters
	private String windowTitle;
	private String windowDescription;
	private String windowPercentage;
	private String windowHelp;
	private String processMessage="";
	private String messageClass="goodMsg";
	private boolean selectAll;
	private boolean requireParameter;
	private boolean flag=false;
	
	//	Main entity
	private CBusinessClass windowEntity = new CBusinessClass();
	
	//	Data table parameters
	private List<PairKVElement> titles = new ArrayList<PairKVElement>();
	private List<ListKV> values = new ArrayList<ListKV>();
	private List<Integer> orderIndex = new ArrayList<Integer>();
	private ListKV[] selectedRow;
	private List<Integer> mainIDS;
	private List<Map<String,String>> foreign;
	private List<String> mtmTables;
	private List<Integer> references;
	private int userFieldIndex=-1;
	
	
	//	Buttons and actions
	private List<CActionbutton> buttons;
	private int idAction=0;
	
	//	Filters
	private List<UIFilterElement> filtersControls = new ArrayList<UIFilterElement>();
	private String selectedFiltreId;
	
	//	Request parameters
	@ManagedProperty(value="#{frontController.window}")
	private CWindow window;
	
	//	Process
	private boolean inProcess;

	//	Binary content attributes
	private List<CAttribute> fcas = new ArrayList<CAttribute>();
	private CAttribute selectedFca;
	private boolean fcasAvailable=false;
	
	//
	private List<Map<CAttribute, String>> keyMapList = new ArrayList<Map<CAttribute,String>>();
	
	//	Bound documents IDs
	private Map<CActionbutton, List<Integer>> executionTrace = new HashMap<CActionbutton, List<Integer>>();
	
	private List<CAttribute> validationAttributes = new ArrayList<CAttribute>();
	private List<Integer> validationIndexes = new ArrayList<Integer>();
	private boolean showLockToolbar = false;
	
	private String formview;
	
	//	Help
	private boolean docAvailable;
	private boolean actAvailable;
	private boolean bndAvailable;
	
	//	Parameters
	private List<ButtonParameter> parsToShow = new ArrayList<ButtonParameter>();
	private int buttonID;
	
	//	Batch
	private List<ActionBatch> batches = new ArrayList<ActionBatch>();
	private String batchID;
	private boolean silentMode;
	
	//	Alert
	private List<SAlert> alerts;
	
	//	Workflows
	private List<WorkflowDefinition> workflows = new ArrayList<WorkflowDefinition>();
	private boolean wfAvailable=false;
	
	//	No filters
	private boolean filtersEnabled;
	
	//	Parameters
	private CParameterMetamodel paramodel;
	private boolean updateBtn;
	private boolean deleteBtn;
	
	//	Sub view
	private int subviewId;
	private List<List<String>> subviewValues;
	private boolean subviewButton;
	private boolean detailsShown=false;
	
	// Historique
	private ScreenDataHistory history = new ScreenDataHistory();
	
	//	Details
	private List<PairKVElement> selValue = new ArrayList<PairKVElement>();
	
	private OCRDriverBean driver = new OCRDriverBean();
	private boolean ocrised = false;
	
	
	//	Showing OTM data
	private List<MtmBlock> mtmBlocks = new ArrayList<MtmBlock>();
	private List<MtmBlock> otoBlocks = new ArrayList<MtmBlock>();
	
	//	Alpha column mode
	private boolean alphaMode = false;
	private String alphaEntity="";
	private String alphaDataReference="";
	private List<Integer> autorizedAlphas = new ArrayList<Integer>();
	
	//	Data pagination
	private int pagesCount=1;
	private int currentPage=1;
	private List<Integer> allPages = new ArrayList<Integer>();
	
	//	File store
	private boolean storable = false;
	private List<String> files = new ArrayList<String>(); 
	private List<StoredFile> storedFiles = new ArrayList<StoredFile>();
	private List<StoredFileType> storedFileTypes = new ArrayList<StoredFileType>();
	private StoredFile storedFile = new StoredFile();
	private int selectedFileType;
	
	
	//	Columns styles
	private List<String> colStyles = new ArrayList<String>();
	private String alphaStyle="width:150px";
	private String fixedCols = "3";
	private String styleAffichage="full-size, no-size";
	private boolean voidData = false;
	
	//	Single column
	private boolean singleColumnMode = false;
	
	
	//	Mail
	private boolean mailWindow;
	private List<MailDTO> mails = new ArrayList<MailDTO>();
	private int selectedMail;
	private MailDTO tableMail;
	private MailDTO mail = new MailDTO();
	private List<MailDTO> filteredMails = new ArrayList<MailDTO>();
	
	//	TABS
	private TabView tabview=null;
	private int activeIndex;
	
	//	VIEWS
	private boolean anyViews;
	private List<CView> views;
	
	private UIFilterElement lastFilter = null;
	
	//	Internationalization
	private LocalizationEngine translator = new LocalizationEngine();
	
	//	Callouts
	private List<CWindowCallout> callouts = new ArrayList<CWindowCallout>();
	
	private boolean showDirections=false;
		
	//	Synthesis
	private boolean synthView = false;
	private CListViewSynthesis synthesisModel;
	private String synthSQL;
	private SynthesisTab synthesisTab;
	
	//	CMS Mode
	private boolean cmsMode = false;
	private CFolder rootFolder = new CFolder();
	private RowFolder rootRowFolder = null;
	private TreeNode rootNode;
	private TreeNode selectedNode;
	private Map<TreeNode, RowFolder> nodesFoldersMap =  new HashMap<TreeNode, RowFolder>();
	private Map<TreeNode, RowDocument> nodesDocsMap =  new HashMap<TreeNode, RowDocument>();
	private RowDocument selectedDocument = new RowDocument();
	private boolean seldoc = false;
	private String htmlCMS;
	private String rawCMSContent;
	private String cmsFileName="";
	private String cmsCreationDate="";
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void postLoad(){
		
		boolean notinsession=((window==null) || !FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
								|| FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")==null); 
		
		if(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
				&& FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")!=null && window==null)
			return;
		
		if(notinsession){
			try {
				
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return;
		}
		
		formview = "protogen-formline";//FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FORM_MODE").toString();

		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		
		// Static layout parameters
		windowTitle = translator.windowTranslate(window.getTitle(), window.getId(), u.getLanguage());
		windowDescription  = window.getStepDescription();
		windowPercentage = window.getPercentage()+" %";
		windowHelp = window.getHelpVideo();
		
		Map<String,Object> params =FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		
		
		
		if(params.containsKey("inProcess") && params.get("inProcess").equals("true"))
			inProcess = true;
		else
			inProcess = false;
		
		List<UIFilterElement> procedureFilters = new ArrayList<UIFilterElement>();
		if(inProcess){
			SAtom atom = (SAtom)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("adesc");
			windowDescription = atom.getDescription().replaceAll("MANDATORY", "");
			
			procedureFilters = (List<UIFilterElement>)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.PROCEDURE_FILTERS);
		}
		
		//	Data loading
		ApplicationLoader loader = new ApplicationLoader();
		window = loader.loadFullWindow(window);
		window = loader.loadWindowWithActions(window);
		HabilitationsService hsrv = new HabilitationsService();
		window.setCDocumentbuttons(hsrv.filtrerDocuments(cache,window.getCDocumentbuttons()));
		window.setCActionbuttons(hsrv.filtrerActions(cache,window.getCActionbuttons()));
		window.setWorkflows(hsrv.filtrerWorkflows(cache,window.getWorkflows()));
		if(!cache.isSuperAdmin())
			window = loader.filterWindow(window, cache.getRoles()); 
		AlertDataAccess ada = new AlertDataAccess();
		alerts = ada.getAlertByScreen(window);
		
		updateBtn = window.isUpdateBtn();
		deleteBtn = window.isDeleteBtn();
		
		workflows = window.getWorkflows();
		wfAvailable = workflows.size()>0;
		
		
		buttons = window.getCActionbuttons();
		callouts = loader.loadCallouts(window);
		
		//	Check if user is allowed to access creation form
		if(window.getFormId()>0){
			if(!hsrv.checkWindow(u, window.getFormId()))
				window.setFormId(0);
		}
		
		//	Validation attributes construction
		validationAttributes = new ArrayList<CAttribute>();
		
		for(CAttribute a : window.getCAttributes()){
			if(!a.getEntity().getDataReference().equals(window.getMainEntity()) || a.getCAttributetype().getId()!=9)
				continue;
			
			validationAttributes.add(a);
			validationIndexes.add(new Integer(window.getCAttributes().indexOf(a)));
			//showLockToolbar = true;
		}
		
		//	filters construction
		if(filtersControls.size()==0){
			for(CAttribute a : window.getCAttributes()){
				if(a.isFilterEnabled()){
					UIFilterElement f = new UIFilterElement();
					f = loadFilter(a);
					filtersControls.add(f);
				}
			}
			
			
		}

		if(filtersControls.size()>0){
			selectedFiltreId = filtersControls.get(0).getControlID();
		}
		
		fcas = new ArrayList<CAttribute>();
		for(CAttribute a : window.getCAttributes())
			if(a.getCAttributetype().getId()==6)
				fcas.add(a);
		fcasAvailable = fcas.size()>0;
		actAvailable = (buttons!=null && buttons.size()>0);
		for(CActionbutton btn : buttons){
			String sparams = btn.getParameters();
			if(sparams == null || sparams.length()==0)
				continue;
			String[] docp = sparams.split(";");
			if(btn.getListParam() == null)
				btn.setListParam(new ArrayList<ButtonParameter>());
			for(String key : docp){
				String k = key.split(":")[0];
				String type = key.split(":")[1];
				String label = key.split(":")[2];
				ButtonParameter p = new ButtonParameter();
				p.setParameter(k);
				p.setTitle(label);
				p.setType(type);
				if(type.equals("D"))
					p.setCtrlDate(true);
				else
					p.setCtrlDate(false);
				
				if(type.length()>1){
					//reference
					List<PairKVElement> elements = new ArrayList<PairKVElement>();
					ProtogenDataEngine e = new ProtogenDataEngine();
					elements = e.getDataKeys(type, false, 0);
					
					p.setReference(true);
					p.setElements(elements);
				}
				
				btn.getListParam().add(p);
			}
		}
		purgeWindowButtons(buttons);
		
		docAvailable = (window.getCDocumentbuttons()!=null && window.getCDocumentbuttons().size()>0);
		bndAvailable = (fcas!=null && fcas.size()>0);
		for(CDocumentbutton btn : window.getCDocumentbuttons()){
			String sparams = btn.getParameters();
			if(sparams == null || sparams.length()==0)
				continue;
			String[] docp = sparams.split(";");
			for(String key : docp){
					String k = key.split(":")[0];
					String type = key.split(":")[1];
					String label = key.split(":")[2];
					ButtonParameter p = new ButtonParameter();
					p.setParameter(k);
					p.setTitle(label);
					p.setType(type);
					if(type.equals("D"))
						p.setCtrlDate(true);
					else
						p.setCtrlDate(false);
					
					btn.getListParam().add(p);
			}
		}
		
		List<OCRDriverBean> drvs = loader.loadDrivers(cache.getAppKey());
		for(OCRDriverBean d : drvs){
			Document doc = (Document) (new XStream()).fromXML(d.getStringContent());
			if(doc.getMainEntity().equals(window.getMainEntity())){
				driver = d;
				ocrised = true;
				break;
			}
		}
		
		prepareSynthesis();
		prepareCMSMode();
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		
		if(window == null || window.getCWindowtype() == null || window.getCWindowtype().getId()!=1)
			return;
		double dataSizePaged = (1.0*engine.countData(window,new ArrayList<UIFilterElement>(),u))/10.0;
		pagesCount=(int) Math.ceil(dataSizePaged);
		allPages = new ArrayList<Integer>();
		for(int i=1 ; i <= pagesCount;i++)
			allPages.add(i);
		currentPage = 1;
		List<List<String>> data = engine.executeSelectWindowQuery(window, new ArrayList<UIFilterElement>(),u, currentPage, pagesCount);
		
		alphaDataReference = loader.checkForAlphaTable(window.getMainEntity());
		alphaMode=(alphaDataReference!=null && alphaDataReference.length()>0);
		if(alphaMode) {
			alphaEntity = loader.getEntityFromDR(alphaDataReference);
			autorizedAlphas = engine.loadAuthorizedAlphas(alphaDataReference,cache);
		}
		
		//	Add alpha entity filter
		if(alphaMode){
			UIFilterElement f = new UIFilterElement();
			f = loadMultipleFilter();
			filtersControls.add(f);
		}
		filtersEnabled = filtersControls.size()>0;
		
		mainIDS = engine.getLastIDS();
		foreign = engine.getForeignKeys();
		mtmTables = new ArrayList<String>();
		
		ApplicationLoader dal = new ApplicationLoader();
		mtmTables = dal.getDependentEntities(window.getMainEntity());
		
		MTMService service = new MTMService();
		List<MtmDTO> dtos = service.getMtm(window,engine.getReferencedTable(window.getMainEntity())); 
		mtmBlocks = populate(dtos);
		otoBlocks = populateOto(window);
		
		Map<String,Integer> referencesIndex = new HashMap<String,Integer>();
		Map<String,Integer> hourIndex = new HashMap<String,Integer>();
		Map<String,Integer> autoIndex = new HashMap<String,Integer>();
		references = new ArrayList<Integer>();
		orderIndex = new ArrayList<Integer>();
		int userIndex = -1;
		
		if(window.getCWindowtype().getId()!= 1)
			return;
		if(data==null){
			return ;
		}
		
		//	Load file store
		windowEntity = loader.getEntity(window.getMainEntity());  
		storable = windowEntity.isStorable();
		FileStoreService fservice = new FileStoreService();
		storedFileTypes = fservice.loadTypes(cache.getAppKey());
		if(storedFileTypes != null && storedFileTypes.size()>0)
			selectedFileType = storedFileTypes.get(0).getId();
		
		for(int i = 0 ; i < data.get(0).size() ; i++){
			
			PairKVElement element;
			if(data.get(0).get(i).startsWith("#REF#")){
				// Reference management
				String tref = data.get(0).get(i).replaceAll("#REF#pk_", "");
				tref = tref.replaceAll("#REF#fk_", "");
				referencesIndex.put(tref, new Integer(i));
				String tableName = "";
				dal = new ApplicationLoader();
				references.add(new Integer(i));
				for(CAttribute a : window.getCAttributes())
					if(a.getDataReference().equals("fk_"+tref)){
						tableName = a.getAttribute().replaceAll("ID ", "");
						break;
					}
				
				
				element = new PairKVElement(""+i, tableName);
			} else if (data.get(0).get(i).startsWith("#CURRENT_USER#")){
				userIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#CURRENT_USER#", ""));
			}else if (data.get(0).get(i).startsWith("#AUTO#")){
				String suffix = data.get(0).get(i).substring(1).split("#")[1];
				
				//userIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#AUTO#"+suffix+"#", ""),true,suffix);
				autoIndex.put(data.get(0).get(i).replace("#AUTO#"+suffix+"#", ""), new Integer(i));
			}
			else if (data.get(0).get(i).startsWith("#HOUR#")){
				String tref = data.get(0).get(i).replaceAll("#HOUR#", "");
				tref = tref.replaceAll("#HOUR#", "");
				hourIndex.put(tref, new Integer(i));
				element = new PairKVElement(""+i, tref);
			} else if (data.get(0).get(i).startsWith("#IN_U#")){
				userFieldIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#IN_U#", ""));
			} else if (data.get(0).get(i).startsWith("#IN_R#")){
				references.add(new Integer(i));
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#IN_R#", ""));
			} else
				element = new PairKVElement(""+i, data.get(0).get(i));
			
			for(CAttribute a : window.getCAttributes())
				if(a.getAttribute().equals(element.getValue())){
					element.setValue(translator.attributeTranslate(element.getValue(), a.getId(), u.getLanguage()));
					break;
				}
			titles.add(element);
		}
		
		
		
		for(int i = 0 ; i < data.get(1).size();i++)
			orderIndex.add(new Integer(data.get(1).get(i)));
		
		subviewButton = false;
		for(PairKVElement t : titles){
			int oi = orderIndex.get(titles.indexOf(t));
			if(window.getCAttributes().size()>=oi)
				break;
			t.setVisible(window.getCAttributes().get(oi).isSubvisible()); 
			subviewButton= subviewButton || !t.isVisible();
		}
		
		boolean submited = false;
		if(inProcess){
			submited = true;
			for(UIFilterElement e : this.filtersControls){
				for(UIFilterElement pe : procedureFilters){
					if(e.isReference() && e.getControlID().equals(pe.getLabel())){
						e.setControlValue(pe.getControlValue());
						e.setReadOnly(true);
						break;
					}
				}
			}
			
			doFilter();
			
		} 
		FrontController front = (FrontController)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FRONT_CTRL");
		if(inProcess && windowDescription!= null && windowDescription.length()>0){

			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(false);
			FacesContext.getCurrentInstance().getExternalContext().getFlash().clear();
			if(front.isKeepAlive())
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,windowTitle,windowDescription));
		}
		front.setKeepAlive(!front.isKeepAlive());
		if(window.isForcedFilter() && !submited)	{			//	If there is a mandatory lock don't show any data
			for(int i = 2 ; i < data.size() ; i++){
				
				ListKV list = new ListKV(""+i, data.get(i), mainIDS.get(i-2), referencesIndex,hourIndex,userIndex, 
						autoIndex,validationIndexes, titles, alphaMode,alphaDataReference,window.getMainEntity());
				if(alphaMode){
					boolean found = false;
					for(Integer oa : autorizedAlphas){
						if(list.getAlphaId() == oa.intValue()){
							found = true;
							break;
						}
					}
					if(!found)
						continue;
				}
				list.setTheme(engine.getRowsStyle().get(i-2));
				//Cette partie est pour ajouter un double zéro après la virgule aux champs nombre et monétaire
				//Je ne suis pas sur que ça marche
				for (int j = 0; j < list.getValue().size();  j++) {
					try{
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				        Date d = sdf.parse(list.getValue().get(j));
				        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
				        //double intVal = Double.parseDouble(list.getValue().get(j));
						list.getValue().set(j, dateFormatter.format(d));
						list.getRoundValue().set(j, dateFormatter.format(d));
					}catch(Exception ex){
					}
				} 
				values.add(list);
				
			}
			
			//	Manage screen history for procedures
			
			Boolean hm = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.HISTORY_MODE);
			if(hm == null)
				hm = new Boolean(false);
			hm = hm && inProcess && window.isSynthesis();
			if(hm.booleanValue()){
				String procentity = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.PROCEDURE_ENTITY);
				boolean flg = false;
				
				if(window.getMainEntity().equals(procentity)){
					history.setIdIndex(0);
					flg = true;
				}else {
					for(CAttribute a : window.getCAttributes())
						if(a.isReference() && a.getDataReference().equals("fk_"+procentity)){
							history.setIdIndex(window.getCAttributes().indexOf(a));
							flg = true;
							break;
						}
				}
				
				if(flg){
					history.setData(values);
					populateInnerValues(values);
					history.setTitles(titles);
					history.setWindowTitle(windowTitle);
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.LAST_HISTORY, history);
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.HISTORY, new Boolean(true));
				}
				FrontController fc = (FrontController)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FRONT_CTRL");
				fc.saveHistory();
			}
		}
	
		layoutDatatable();
	}

	
	private void prepareCMSMode() {
		this.cmsMode = CMSService.getInstance().checkFileStore(window);
		if(!this.cmsMode)
			return;
		
		rootFolder = CMSService.getInstance().loadRootFolder(window);
		if(rootFolder == null){
			this.cmsMode = false;
			return;
		}
		rootRowFolder = new RowFolder(rootFolder);
		prepareTree();
	}

	private void prepareTree() {
		rootNode = new DefaultTreeNode(new RowFileSystemElement(rootRowFolder), null);
		nodesFoldersMap.put(rootNode, rootRowFolder);
		for(RowFolder f : rootRowFolder.getSubFolders()){
			recursiveTreeConstruct(rootNode, f);
		}
	}


	private void recursiveTreeConstruct(TreeNode node, RowFolder f) {
		TreeNode tn = new DefaultTreeNode(new RowFileSystemElement(f), node);
		nodesFoldersMap.put(tn, f);
		for(RowFolder sf : f.getSubFolders()){
			recursiveTreeConstruct(rootNode, sf);
		}
	}

	public void updateCMSPanel(){
		int dbID = selectedRow[0].getDbID();
		CMSService.getInstance().populateFolders(rootRowFolder, dbID, window);
		updateTree();
	}
	
	private void updateTree() {
		rootNode = new DefaultTreeNode(new RowFileSystemElement(rootRowFolder), null);
		nodesFoldersMap.put(rootNode, rootRowFolder);
		for(RowFolder f : rootRowFolder.getSubFolders()){
			recursiveTreeUpdate(rootNode, f);
		}
		if(rootRowFolder.getDocuments() != null  && !rootRowFolder.getDocuments().isEmpty()){
			for(RowDocument d : rootRowFolder.getDocuments()){
				TreeNode tn = new DefaultTreeNode("file", new RowFileSystemElement(d), rootNode);
				nodesDocsMap.put(tn, d);
			}
		}
	}

	private void recursiveTreeUpdate(TreeNode node, RowFolder f) {
		TreeNode tn = new DefaultTreeNode(new RowFileSystemElement(f), node);
		nodesFoldersMap.put(tn, f);
		for(RowFolder sf : f.getSubFolders()){
			recursiveTreeConstruct(rootNode, sf);
		}
		if(f.getDocuments() != null  && !f.getDocuments().isEmpty()){
			for(RowDocument d : f.getDocuments()){
				TreeNode fn = new DefaultTreeNode("file", new RowFileSystemElement(d), tn);
				nodesDocsMap.put(fn, d);
			}
		}
	}
	
	public void onNodeSelect(NodeSelectEvent event){
		if(selectedRow == null || selectedRow.length == 0){
			selectedDocument = null;
			seldoc = false;
			htmlCMS="";
			cmsFileName="";
			cmsCreationDate="";
			return;
		}
		
		TreeNode n = event.getTreeNode();
		if(nodesFoldersMap.containsKey(n)){
			selectedDocument = null;
			seldoc = false;
			htmlCMS="";
			cmsFileName="";
			cmsCreationDate="";
			return;
		}
			
		
		selectedDocument = nodesDocsMap.get(n);
		seldoc = true; 
		
		cmsFileName = selectedDocument.getName();
		cmsCreationDate = CalendarUtils.formatDate(selectedDocument.getCreation());
		
		String file = CMSService.getInstance().downloadRowDoc(selectedDocument, selectedRow[0]);
		rawCMSContent = file;
		
		switch(selectedDocument.getType().getId()){
			case 1:htmlCMS = "<object data=\""+file+"\" type=\"application/pdf\" width=\"100%\" style=\"min-height:300px\" />";break;
			case 2:htmlCMS = "<img alt=\"\" src=\""+file+"\" />";break;
		}
	}
	
	public String downloadCMSDocument(){
		if(selectedDocument == null){
			return null;
		}
		if(selectedDocument.getType().getId() == 1){
			String tempPDF = FacesContext.getCurrentInstance().getExternalContext().getRealPath(".")+"/"+rawCMSContent;
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			try{
			    ec.responseReset();
			    ec.setResponseContentType("application/pdf");
			    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"document.pdf\"");
			    
				OutputStream output;
				output = ec.getResponseOutputStream();
				
				InputStream input = new FileInputStream(tempPDF);
				IOUtils.copy(input, output);
			
				File file = new File(tempPDF);
				if(file.exists())
					file.delete();
				
				input.close();
				output.close();
				output.flush();
				fc.responseComplete();
				
				return null;
				
				
			} catch(Exception exc){
				exc.printStackTrace();
			}
			return null;
		} else {
			String mime = rawCMSContent.split(";")[0];
			mime = mime.replaceAll("data:", "");
			String content = rawCMSContent.split(",")[1];
			byte[] decoded = com.sun.jersey.core.util.Base64.decode(content);
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			try{
			    ec.responseReset();
			    ec.setResponseContentType(mime);
			    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"document."+selectedDocument.getExtension()+"\"");
			    
				OutputStream output;
				output = ec.getResponseOutputStream();
				
				output.write(decoded);
				
				output.close();
				output.flush();
				fc.responseComplete();
				
				return null;
				
				
			} catch(Exception exc){
				exc.printStackTrace();
			}
			return null;
		}
		
	}
	
	
	public String calloutExecution(){
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String t=params.get("COUTID");
		int cid = Integer.parseInt(t);
		
		CWindowCallout callout = null;
		for(CWindowCallout c : callouts){
			if(c.getId() == cid){
				callout = c;
				break;
			}
		}
		
		if(callout == null)
			return null;
		
		//	Let us check if the callout needs any selected rows
		if(callout.getJsonArg().indexOf("<<DBID>>")>0){
			if(selectedRow == null || selectedRow.length == 0){
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_WARN,"Veuillez sélectionner un enregistrement",""));
				return null;
			}
			
			String args  = callout.getJsonArg().replaceAll("<<DBID>>", selectedRow[0].getDbID()+"");
			String enc = new String(com.sun.jersey.core.util.Base64.encode(args));
			String res="";
			try{
				res = RemoteCalloutService.getInstance().remoteCallout(enc, callout.getCallout().getId());
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			
			if(res.isEmpty())
				return null;
			if(callout.getType() == 1) //	PDF PRINT CALLOUT
				return printCallout(res);
		}
		
		
			
		return null;
	}
	
	private String printCallout(String res) {
		List<String> pdfs = new ArrayList<String>();
		String[] files = res.split(",");
		for(String e : files){
			try {
				String pdf = UUID.randomUUID().toString()+".pdf";
				byte[] data = Base64.decode(e);
				OutputStream output;
				output = new FileOutputStream(pdf);
				IOUtils.write(data, output);
				output.close();
				pdfs.add(pdf);
			} catch (FileNotFoundException exc) {
				exc.printStackTrace();
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
		String tempPDF = UUID.randomUUID().toString()+".pdf";
		try{
			PDFMergerUtility pdfMU = new PDFMergerUtility();
			for(String pdf : pdfs){
				pdfMU.addSource(new File(pdf));
			}
			pdfMU.setDestinationFileName(tempPDF);
			pdfMU.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
		}catch(Exception exc){
			exc.printStackTrace();
		}
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		try{
		    ec.responseReset();
		    ec.setResponseContentType("text/plain");
		    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"document.pdf\"");
		    
			OutputStream output;
			output = ec.getResponseOutputStream();
			
			InputStream input = new FileInputStream(tempPDF);
			IOUtils.copy(input, output);
			
			for(String p : pdfs){
				File f = new File(p);
				if(f.exists())
					f.delete();
			}
			
			File file = new File(tempPDF);
			if(file.exists())
				file.delete();
			
			input.close();
			output.close();
			output.flush();
			fc.responseComplete();
			
			return null;
			
			
		} catch(Exception exc){
			exc.printStackTrace();
		}
		
		return null;
	}

	private void prepareSynthesis() {
		ApplicationLoader dal = new ApplicationLoader();
		synthesisModel = dal.loadSLVModel(this.window);
		synthView = (synthesisModel.getId()>0);
		synthesisTab = new SynthesisTab();
		synthesisTab.setClvs(synthesisModel);
		synthesisTab.setLibelle(synthesisModel.getLibelle());
		synthSQL = synthesisModel.getExpression();
		prepareHtml();
	}

	private void prepareHtml() {
		String html = "<div style=\"width:100%; background-color:#fff\">";
		for(CLVSTable t : synthesisTab.getClvs().getTables()){
			html = html+cvsHtmlTable(t);
		}
		html = html + "</div>";
		synthesisTab.setHtmlContent(html);
	}

	private String cvsHtmlTable(CLVSTable t) {
		String html = "<div style=\"width:95%; margin-left: 5%; background-color:#ddd\">";
		html = html + "<h3 style=\"margin: 5px;\">"+t.getLabel()+"</h3>";
		html = html + "<table style=\"width:100%;border: 1px solid black;\">";
		html = html + "<tr>";
		double fwidth = 100.0/(t.getFields().size()*1.0);
		for(CLVSField f : t.getFields()){
			html = html+"<th style=\"border: 1px solid black;width : "+fwidth+"%\">";
			html = html+f.getLibelle();
			html = html+"</th>";
		}
		html = html + "</tr>";
		html = html + "</table>";
		for(CLVSTable subt : t.getTables()){
			html = html + cvsHtmlTable(subt);
		}
		html = html + "</div>";
		return html;
	}

	private void updateSynthPanel(int id){
		int deep = 0;
		ProtogenDataEngine pde = new ProtogenDataEngine();
		List<Map<String, String>> sdata = pde.executeSelectSynthesis(synthSQL, id);
		//prepareHtml();
		//String html = synthesisTab.getHtmlContent();
		String html = "<div style=\"width:100%; background-color:#fff\">";
		for(CLVSTable t : synthesisTab.getClvs().getTables()){
			String fkey = "";
			for(CAttribute a : t.getTable().getAttributes())
				if(a.getDataReference().indexOf(this.window.getMainEntity())>=0){
					fkey = window.getMainEntity()+"_pk_"+window.getMainEntity();
					break;
				}
			html = html+cvsHtmlTableFilled(t, sdata, fkey, id, deep);
		}
		html = html + "</div>";
		synthesisTab.setHtmlContent(html);
	}
	private String cvsHtmlTableFilled(CLVSTable t, List<Map<String, String>> sdata, String fkey, int parentId, int deep) {
		int rgb = deep+200;
		String html = "<div style=\"width:95%; margin-left: 5%; background-color:rgb("+rgb+","+rgb+","+rgb+");border-style: groove;border-width: 1px; margin-top: 5px;	margin-bottom: 5px;\">";
		html = html + "<h4 style=\"margin: 2px;\">"+t.getLabel()+"</h4>";
		html = html + "<table style=\"width:100%;border: 1px solid black;\">";
		html = html + "<tr>";
		double fwidth = 100.0/(t.getFields().size()*1.0);
		for(CLVSField f : t.getFields()){
			html = html+"<th style=\"border: 1px solid black;width : "+fwidth+"%\">";
			html = html+f.getLibelle();
			html = html+"</th>";
		}
		html = html + "</tr>";
		List<Integer> alreadyInData = new ArrayList<Integer>();
		for(Map<String, String> row : sdata){
			int rpId = Integer.parseInt(row.get(fkey));
			if(rpId!=parentId)
				continue;
			
			boolean alreadyIn=false;
			String pkField = t.getTable().getDataReference()+"_pk_"+t.getTable().getDataReference();
			int id = 0;
			try{
				id = Integer.parseInt(row.get(pkField));
			}catch(Exception e){
				
			}
			if(id == 0)
				return html+"<tr><td style=\"background-color:#fff\" colspan=\""+t.getFields().size()+"\" ><span>&nbsp;</span></td></tr></table></div>";
			for(Integer i : alreadyInData)
				if(i.intValue() == id){
					alreadyIn = true;
					break;
				}
			
			if(alreadyIn)
				continue;
			
			alreadyInData.add(id);
			
			html = html + "<tr style=\"background-color:#fff\">";
			for(CLVSField f : t.getFields()){
				html = html+"<td style=\"border: 1px solid black;width:"+fwidth+"%; padding : 3px\">";
				
				String key = t.getTable().getDataReference()+"_"+f.getAttribute().getDataReference();
				String sval = row.get(key);
				String trueVal = ExpressionParserEngine.getInstance().formatValue(f, sval);
				html = html+trueVal;
				html = html+"</td>";
			}
			html = html + "</tr>";
			
			for(CLVSTable subt : t.getTables()){
				String sfk = "";
				for(CAttribute a : subt.getTable().getAttributes())
					if(a.getDataReference().indexOf(t.getTable().getDataReference())>=0){
						sfk = t.getTable().getDataReference()+"_pk_"+t.getTable().getDataReference();
						break;
					}
				html = html+"<tr>";
				html = html+"<td colspan=\""+t.getFields().size()+"\">";
				html = html + cvsHtmlTableFilled(subt, sdata, sfk, id, deep+15);
				html = html+"</td>";
				html = html+"</tr>";
			}
			
		}
		html = html + "</table>";
		html = html + "</div>";
		return html;
	}
	public void activeIndexChanged(TabChangeEvent evt){
				
		String sid = evt.getTab().getId();
		
		for(MtmBlock b : otoBlocks){
			String id = "tabid_"+b.getEntityID();
			if(id.equals(sid)){
				activeIndex = mtmBlocks.indexOf(b);
				break;
			}
		}
		
		for(MtmBlock b : mtmBlocks){
			String id = "tabid_"+b.getEntityID();
			if(id.equals(sid)){
				activeIndex = mtmBlocks.indexOf(b)+otoBlocks.size();
				break;
			}
		}
		
		if(sid.equals("store_tab")){
			activeIndex = otoBlocks.size()+mtmBlocks.size()-1;
		}
		
		if(sid.equals("mail_store")){
			activeIndex = otoBlocks.size()+mtmBlocks.size();
		}
	}
	
	private void layoutDatatable(){
		colStyles = ListKV.formatStyles(values, titles, alphaMode);
		voidData = (values == null) || (values.size() == 0);
		if(alphaMode)
			alphaStyle = ListKV.formatAlpha(alphaEntity, values);
		else
			alphaStyle = "width:1px";
		
		if(alphaMode || titles.size()<=2)
			fixedCols = "2";
		else
			fixedCols = "3";
		
		if(titles.size()==1){
			styleAffichage = "quarter-size, three-quarters-size";
			
		} else
			styleAffichage = titles.size()>2?"full-size, no-size":"middle-size, middle-size";
		showDirections = titles.size()<3;
		
		List<Integer> ids = new ArrayList<Integer>();
		for(ListKV l : values)
			ids.add(l.getDbID());
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.DATA_IDS, ids);
		
		ViewsService srv = new ViewsService();
		views = srv.loadWindowViews(window.getId());
		anyViews = views!=null && views.size()>0;
		
		prepareMails();
	}
	
	private void prepareMails() {
		ApplicationLoader dal = new ApplicationLoader();
		
		mailWindow = dal.isMailWindow(window);

		
		
	}

	public String loadView(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String sid = params.get("SEL_VIEW");
		
		int id = Integer.parseInt(sid);
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.SELECTED_VIEW, ""+window.getId());
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.SELECTED_VIEW_ID, ""+id);
		
		return "protogen-sqlview";
	}
	
	public String versionMaterielle(){
		if(selectedRow == null || selectedRow.length == 0){
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_WARN,"Veuillez sélectionner un enregistrement",""));
			return null;
		}
		
		ApplicationLoader dal = new ApplicationLoader();
		OCRHistory h = dal.loadHistoryRow(driver,selectedRow[0].getDbID());
		
		if(h == null || h.getFileKey() == null || h.getFileKey().length()==0){
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_WARN,"Cet enregistrement ne dispose pas de document matériel",
							"Il est probable que cet enregistrement a été créé au niveau de l'application et non à travers la numérisation"));
			return "";
		}
		
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		ec.responseReset();
		ec.setResponseContentType("image/png");
		ec.setResponseHeader("Content-Disposition",
				"attachment; filename=\"document.png\"");

		OutputStream output;

		try {
			output = ec.getResponseOutputStream();
			InputStream is = new FileInputStream(h.getFileKey());

			byte[] content = IOUtils.toByteArray(is);

			is.close();
			output.write(content);
			output.close();
			output.flush();
			FacesContext.getCurrentInstance().responseComplete();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private void populateInnerValues(List<ListKV> outerValues) {
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass cb = pde.getReferencedTable(window.getMainEntity());
		cb = pde.populateEntity(cb);
		
		List<CAttribute> mtm = new ArrayList<CAttribute>();
		for(CAttribute a : cb.getAttributes()){
			if(a.isMultiple())
				mtm.add(a);
		}
		
	
		
		
		for(ListKV datum : outerValues){
			datum.setInnerData(new ArrayList<SimpleDataTable>());
			for(CAttribute a : mtm){
				//	Get the required mtm entity
				String mtmTableReference = a.getDataReference().split("__")[0].substring(3);
				CBusinessClass mtmEntity = pde.getReferencedTable(mtmTableReference);
				mtmEntity = pde.populateEntity(mtmEntity);
				
				List<String> titles = new ArrayList<String>();
				for(CAttribute me : mtmEntity.getAttributes()){
					if(me.getDataReference().startsWith("pk_") || me.getDataReference().equals(a.getDataReference()))
						continue;
					
					titles.add(me.getDataReference()+"::"+me.getAttribute());
				}
				
				List<List<String>> data =  pde.loadSimpleData(a,titles,datum.getDbID(),a.getDataReference());
				
				//	clean titles
				for(int ij=0; ij<titles.size();ij++){
					String title = titles.get(ij);
					title = title.split("::")[1];
					titles.set(ij, title);
				}
				
				SimpleDataTable sdt = new SimpleDataTable();
				sdt.setData(data);
				sdt.setHeader(a.getAttribute());
				sdt.setTitles(titles);
				sdt.setTable(sdt.format());
				datum.getInnerData().add(sdt);
				datum.table();
			}
		}
		
		if(!subviewButton)
			return;
		//	Subviews
		/*
		for(ListKV datum : outerValues){
			List<String> stitles = new ArrayList<String>();
			List<List<String>> data = new ArrayList<List<String>>();
			data.add(datum.getRoundValue());
			
			SimpleDataTable sdt = new SimpleDataTable();
			
			for(PairKVElement e : titles)
				stitles.add(e.getValue());
			
			sdt.setHeader(windowTitle);
			sdt.setData(data);
			sdt.setTitles(stitles);
			sdt.getTable();
			if(datum.getInnerData() == null){
				datum.setInnerData(new ArrayList<SimpleDataTable>());
			}
			datum.getInnerData().add(sdt);
			datum.table();
		}
		*/
	}

	public void subViewShow(){
		detailsShown = true;
		subviewValues = new ArrayList<List<String>>();
		for(ListKV l : selectedRow){
			
			subviewValues.add(l.getRoundValue());
			subviewId = l.getDbID();
		}
	}
	
	public void subViewClose(){
		detailsShown = false;
	}
	
	private void purgeWindowButtons(List<CActionbutton> acts) {
		for(CActionbutton a : acts){
			if(a.getTitle().contains("<<Etape=")){
				a.setVisible(false);
				if(!a.getTitle().endsWith("1>>"))
					continue;
				
				ActionBatch batch = new ActionBatch();
				batch.setTitle(a.getTitle().replaceAll("<<Etape=1>>", "").trim());
				batch.setDescription(a.getDescription());
				batch.setButtons(new ArrayList<CActionbutton>());
				batch.getButtons().add(a);
				batch.setListParam(a.getListParam());
				if(batch.getListParam()!=null && batch.getListParam().size()>0){
					batch.setParametered(true);
				}
				int nb=0;
				for(CActionbutton b : acts){
					if(b.getTitle().startsWith(batch.getTitle()))
						nb++;
				}
				
				for(int i=2;i<=nb;i++){
					String fragment = i+">>";
					for(CActionbutton b : acts)
						if(b.getTitle().startsWith(batch.getTitle()) && b.getTitle().endsWith(fragment)){
							batch.getButtons().add(b);
							break;
						}
				}
				
				batches.add(batch);
			}
		}
		
	}

	private UIFilterElement loadMultipleFilter(){
		UIFilterElement f = new UIFilterElement();
		
		f.setMultiple(true);
		f.setAttribute(new CAttribute());
		String att = "fk_"+window.getMainEntity()+"__"+alphaDataReference;
		f.getAttribute().setDataReference(att);
		f.setControlID(alphaEntity);
		f.setControlValue("");
		f.setLabel(alphaEntity);
		ProtogenDataEngine pde = new ProtogenDataEngine();
		List<PairKVElement> list = pde.getDataKeys(alphaDataReference, false, 0);
		f.setListReference(list);
		
		return f;
	}
	private UIFilterElement loadFilter(CAttribute a) {
		
		
		UIFilterElement f = new UIFilterElement();
		
		f.setAttribute(a);
		f.setForced(a.isFilterMandatory());
		f.setControlID(a.getAttribute());
		f.setEntity(a.getEntity());
		
		if(a.getDataReference().startsWith("fk_") && !a.isMultiple()){
			
			f.setReference(true);
			ProtogenDataEngine engine = new ProtogenDataEngine();
			ApplicationLoader dal = new ApplicationLoader();
			
			String referenceTable = a.getDataReference().substring(3);
			
			String referencedEntity = dal.getEntityFromDR(referenceTable);
			
			CBusinessClass ce = dal.getEntity(referenceTable);
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			List<PairKVElement> list = engine.getDataKeys(referenceTable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());
			
			f.setReferenceTable(referencedEntity);
			f.setListReference(list);
		}
		if(a.getDataReference().startsWith("fk_") && a.isMultiple()){
			f.setMultiple(true);
			ProtogenDataEngine engine = new ProtogenDataEngine();
			ApplicationLoader dal = new ApplicationLoader();
			
			String referenceTable = a.getDataReference().substring(3).split("__")[0];
			
			String referencedEntity = dal.getEntityFromDR(referenceTable);
			
			CBusinessClass ce = dal.getEntity(referenceTable);
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			List<PairKVElement> list = engine.getDataKeysMultiLVL(referenceTable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());
			
			f.setReferenceTable(referencedEntity);
			f.setListReference(list);
		}
		return f;
	}

	
	public void dataListChanged(){
		for(UIFilterElement f : filtersControls){
			if(!f.isReference() && !f.isMultiple())
				continue;
			for(PairKVElement p : f.getListReference())
				if(p.getKey().equals(f.getControlValue())){
					f.setTrueValue(p.getValue());
				}
		}
	}
	
	public void autocompleteFilter(){
		UIFilterElement filter = null;
		int id = Integer.parseInt(
					FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ACT_FILTER")
				);
		for(UIFilterElement f : filtersControls){
			if(f.getAttribute().getId() == id){
				filter = f;
				lastFilter=f;
				break;
			}
		}
		
		String keyWords = filter.getTrueValue().replaceAll(",", " ");;
		keyWords = keyWords.replaceAll(";"," ");
		List<String> keys = new ArrayList<String>();
		String[] keysTable = keyWords.split(" ");
		for(String  k : keysTable)
			keys.add(k);
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		Map<Integer, String> vals = pde.getDataKeys(filter.getAttribute().getDataReference().substring(3), 
				new ArrayList<String>(), keys);
		
		filter.setListReference(new ArrayList<PairKVElement>());
		for(Integer i : vals.keySet()){
			PairKVElement pkv = new PairKVElement(""+i.intValue(),vals.get(i));
			filter.getListReference().add(pkv);
		}
	}
	
	public void selectedFilterChanged(){
		String dummy = "";
		
		dummy+=selectedFiltreId;
	}
	
	public void addFilterToGrid(){
		UIFilterElement filter = null;
		
		for (UIFilterElement f : filtersControls) {
			if(f.getControlID().equals(selectedFiltreId)){
				filter = f;
				break;
			}
		}
		
		if(filter == null)
			return;
		
		filter.setActivated(true);
	}
	
	public String doFilter(){
		boolean noData = false;
		List<UIFilterElement> filters = new ArrayList<UIFilterElement>();
		for(UIFilterElement f : filtersControls){
			if(f.isMultiple()){
				if(f.getControlValue()!=null && f.getControlValue().length()>0){
					filters.add(f);
				}
				continue;
			}
			CAttribute a = f.getAttribute();
			
			if(a.getDataReference().startsWith("fk_")){
				if(f.getControlValue()!=null && f.getControlValue().length()>0)
					filters.add(f);
				else if(!window.isForcedFilter() && f.isForced())
					noData = true;
			}
			else{
				if((f.getLthan() != null && f.getLthan().length()>0))
					filters.add(f);
				else if(!window.isForcedFilter() && f.isForced())
					noData = true;
			}
		}
		
		
		
		//	Load data
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		double dataSizePaged = (1.0*engine.countData(window,filters,u))/10.0;
		pagesCount=(int) Math.ceil(dataSizePaged);
		allPages = new ArrayList<Integer>();
		for(int i=1 ; i <= pagesCount;i++)
			allPages.add(i);
		currentPage = 1;
		List<List<String>> data = engine.executeSelectWindowQuery(window,filters,u, currentPage, pagesCount);
		mainIDS = engine.getLastIDS();
		foreign = engine.getForeignKeys();
		mtmTables = new ArrayList<String>();
		
		ApplicationLoader dal = new ApplicationLoader();
		mtmTables = dal.getDependentEntities(window.getMainEntity());
		titles = new ArrayList<PairKVElement>();
		values = new ArrayList<ListKV>();
		
		Map<String,Integer> referencesIndex = new HashMap<String,Integer>();
		Map<String,Integer> autoIndex = new HashMap<String,Integer>();

		Map<String,Integer> hourIndex = new HashMap<String,Integer>();
		references = new ArrayList<Integer>();
		int userIndex = -1;
		for(int i = 0 ; i < data.get(0).size() ; i++){
			
			PairKVElement element;
			if(data.get(0).get(i).startsWith("#REF#")){
				// Reference management
				String tref = data.get(0).get(i).replaceAll("#REF#pk_", "");
				tref = tref.replaceAll("#REF#fk_", "");
				referencesIndex.put(tref, new Integer(i));
				String tableName = "";
				dal = new ApplicationLoader();
				references.add(new Integer(i));
				for(CAttribute a : window.getCAttributes())
					if(a.getDataReference().equals("fk_"+tref)){
						tableName = a.getAttribute().replaceAll("ID ", "");
						break;
					}
				
				
				element = new PairKVElement(""+i, tableName);
			}  else if (data.get(0).get(i).startsWith("#CURRENT_USER#")){
				userIndex = i;
				userFieldIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#CURRENT_USER#", ""));
			}else if (data.get(0).get(i).startsWith("#AUTO#")){
				String suffix = data.get(0).get(i).substring(1).split("#")[1];
				
				userIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#AUTO#"+suffix+"#", ""),true,suffix);
				autoIndex.put(data.get(0).get(i).replace("#AUTO#"+suffix+"#", ""), new Integer(i));
			} 
			else if (data.get(0).get(i).startsWith("#HOUR#")){
				String tref = data.get(0).get(i).replaceAll("#HOUR#", "");
				tref = tref.replaceAll("#HOUR#", "");
				hourIndex.put(tref, new Integer(i));
				element = new PairKVElement(""+i, tref);
			} else if (data.get(0).get(i).startsWith("#IN_U#")){
				userFieldIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#IN_U#", ""));
			} else if (data.get(0).get(i).startsWith("#IN_R#")){
				references.add(new Integer(i));
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#IN_R#", ""));
			}else
				element = new PairKVElement(""+i, data.get(0).get(i));
			
			titles.add(element);
		}
		
		subviewButton = false;
		for(PairKVElement t : titles){
			int oi = orderIndex.get(titles.indexOf(t));
			t.setVisible(window.getCAttributes().get(oi).isSubvisible());
			subviewButton= subviewButton || !t.isVisible();
		}
		
		if(!noData){
			for(int i = 2 ; i < data.size() ; i++){
				
				ListKV list = new ListKV(""+i, data.get(i), mainIDS.get(i-2), referencesIndex,hourIndex,userIndex, 
						autoIndex,validationIndexes, titles, alphaMode,alphaDataReference,window.getMainEntity());
				if(alphaMode){
					boolean found = false;
					for(Integer oa : autorizedAlphas){
						if(list.getAlphaId() == oa.intValue()){
							found = true;
							break;
						}
					}
					if(!found)
						continue;
				}
				list.setTheme(engine.getRowsStyle().get(i-2));
				values.add(list);
				
			}
			Boolean hm = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.HISTORY_MODE);
			if(hm == null)
				hm = new Boolean(false);
			hm = hm && inProcess && window.isSynthesis();
			if(hm.booleanValue()){
				String procentity = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.PROCEDURE_ENTITY);
				boolean flg = false;
				
				if(window.getMainEntity().equals(procentity)){
					history.setIdIndex(0);
					flg = true;
				}else {
					for(CAttribute a : window.getCAttributes())
						if(a.isReference() && a.getDataReference().equals("fk_"+procentity)){
							history.setIdIndex(window.getCAttributes().indexOf(a));
							flg = true;
							break;
						}
				}
				Boolean htr = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.HISTO_TO_RETRIEVE);
				if(flg && htr.booleanValue()){
					history.setData(values);
					populateInnerValues(values);
					history.setTitles(titles);
					history.setWindowTitle(windowTitle);
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.LAST_HISTORY, history);
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.HISTORY, new Boolean(true));
				}
				FrontController fc = (FrontController)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FRONT_CTRL");
				fc.saveHistory();
			}
		}
		layoutDatatable();
		if(values.size() == 1){
			if(updateBtn)
				singleColumnMode = true;
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("action", "update");
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("rowID", ""+values.get(0).getDbID());
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAMODE", new Boolean(alphaMode));
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAENTITY", alphaEntity);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAREFERENCE", values.get(0).getAlphaReference());
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("SINGLEMODE", singleColumnMode);
			return formview;
		}
		return "";
	}
	
	public void gotoPage(){
		boolean noData = false;
		List<UIFilterElement> filters = new ArrayList<UIFilterElement>();
		for(UIFilterElement f : filtersControls){
			if(f.isMultiple()){
				if(f.getControlValue()!=null && f.getControlValue().length()>0){
					filters.add(f);
				}
				continue;
			}
			CAttribute a = f.getAttribute();
			
			if(a.getDataReference().startsWith("fk_")){
				if(f.getControlValue()!=null && f.getControlValue().length()>0)
					filters.add(f);
				else if(!window.isForcedFilter() && f.isForced())
					noData = true;
			}
			else{
				if((f.getLthan() != null && f.getLthan().length()>0) || (f.getGthan() != null && f.getGthan().length()>0))
					filters.add(f);
				else if(!window.isForcedFilter() && f.isForced())
					noData = true;
			}
		}
		
		
		
		//	Load data
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		double dataSizePaged = (1.0*engine.countData(window,filters,u))/10.0;
		List<List<String>> data = engine.executeSelectWindowQuery(window,filters,u, currentPage, pagesCount);
		mainIDS = engine.getLastIDS();
		foreign = engine.getForeignKeys();
		mtmTables = new ArrayList<String>();
		
		ApplicationLoader dal = new ApplicationLoader();
		mtmTables = dal.getDependentEntities(window.getMainEntity());
		titles = new ArrayList<PairKVElement>();
		values = new ArrayList<ListKV>();
		
		Map<String,Integer> referencesIndex = new HashMap<String,Integer>();
		Map<String,Integer> autoIndex = new HashMap<String,Integer>();

		Map<String,Integer> hourIndex = new HashMap<String,Integer>();
		references = new ArrayList<Integer>();
		int userIndex = -1;
		for(int i = 0 ; i < data.get(0).size() ; i++){
			
			PairKVElement element;
			if(data.get(0).get(i).startsWith("#REF#")){
				// Reference management
				String tref = data.get(0).get(i).replaceAll("#REF#pk_", "");
				tref = tref.replaceAll("#REF#fk_", "");
				referencesIndex.put(tref, new Integer(i));
				String tableName = "";
				dal = new ApplicationLoader();
				references.add(new Integer(i));
				for(CAttribute a : window.getCAttributes())
					if(a.getDataReference().equals("fk_"+tref)){
						tableName = a.getAttribute().replaceAll("ID ", "");
						break;
					}
				
				
				element = new PairKVElement(""+i, tableName);
			}  else if (data.get(0).get(i).startsWith("#CURRENT_USER#")){
				userIndex = i;
				userFieldIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#CURRENT_USER#", ""));
			}else if (data.get(0).get(i).startsWith("#AUTO#")){
				String suffix = data.get(0).get(i).substring(1).split("#")[1];
				
				userIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#AUTO#"+suffix+"#", ""),true,suffix);
				autoIndex.put(data.get(0).get(i).replace("#AUTO#"+suffix+"#", ""), new Integer(i));
			} 
			else if (data.get(0).get(i).startsWith("#HOUR#")){
				String tref = data.get(0).get(i).replaceAll("#HOUR#", "");
				tref = tref.replaceAll("#HOUR#", "");
				hourIndex.put(tref, new Integer(i));
				element = new PairKVElement(""+i, tref);
			} else if (data.get(0).get(i).startsWith("#IN_U#")){
				userFieldIndex = i;
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#IN_U#", ""));
			} else if (data.get(0).get(i).startsWith("#IN_R#")){
				references.add(new Integer(i));
				element = new PairKVElement(""+i, data.get(0).get(i).replace("#IN_R#", ""));
			}else
				element = new PairKVElement(""+i, data.get(0).get(i));
			
			titles.add(element);
		}
		
		subviewButton = false;
		for(PairKVElement t : titles){
			int oi = orderIndex.get(titles.indexOf(t));
			t.setVisible(window.getCAttributes().get(oi).isSubvisible());
			subviewButton= subviewButton || !t.isVisible();
		}
		
		if(!noData){
			for(int i = 2 ; i < data.size() ; i++){
				
				ListKV list = new ListKV(""+i, data.get(i), mainIDS.get(i-2), referencesIndex,hourIndex,userIndex, 
						autoIndex,validationIndexes, titles, alphaMode,alphaDataReference,window.getMainEntity());
				if(alphaMode){
					boolean found = false;
					for(Integer oa : autorizedAlphas){
						if(list.getAlphaId() == oa.intValue()){
							found = true;
							break;
						}
					}
					if(!found)
						continue;
				}
				list.setTheme(engine.getRowsStyle().get(i-2));
				values.add(list);
				
			}
			Boolean hm = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.HISTORY_MODE);
			if(hm == null)
				hm = new Boolean(false);
			hm = hm && inProcess && window.isSynthesis();
			if(hm.booleanValue()){
				String procentity = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.PROCEDURE_ENTITY);
				boolean flg = false;
				
				if(window.getMainEntity().equals(procentity)){
					history.setIdIndex(0);
					flg = true;
				}else {
					for(CAttribute a : window.getCAttributes())
						if(a.isReference() && a.getDataReference().equals("fk_"+procentity)){
							history.setIdIndex(window.getCAttributes().indexOf(a));
							flg = true;
							break;
						}
				}
				Boolean htr = (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ProtogenConstants.HISTO_TO_RETRIEVE);
				if(flg && htr.booleanValue()){
					history.setData(values);
					populateInnerValues(values);
					history.setTitles(titles);
					history.setWindowTitle(windowTitle);
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.LAST_HISTORY, history);
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.HISTORY, new Boolean(true));
				}
				FrontController fc = (FrontController)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FRONT_CTRL");
				fc.saveHistory();
			}
		}
		layoutDatatable();
	}
	
	public String doUpdate(){
		if(selectedRow[0].isLocked()){
			FacesContext context = FacesContext.getCurrentInstance();  
			
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Impossible de modifier cet enregistrement", "Cet enregistrement a été validé et verouillé, veuillez l'invalider avant de le modifier"));
			return "";
		}
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		//String action = params.get("action");
		String rowID = params.get("rowID");
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("action", "update");
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("rowID", rowID);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAMODE", new Boolean(alphaMode));
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAENTITY", alphaEntity);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAREFERENCE", selectedRow[0].getAlphaReference());
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("SINGLEMODE", false);
		return formview;
	}
	
	public String doPrimeUpdate(){
		
		if(selectedRow == null || selectedRow.length==0){
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN,"Modification","Veuillez sélectionner un enregistrement à modifier"));
			return "";
		}
		
		String rowID = selectedRow[0].getDbID()+"";
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("action", "update");
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("rowID", rowID);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAMODE", new Boolean(alphaMode));
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAENTITY", alphaEntity);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAREFERENCE", selectedRow[0].getAlphaReference());
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("SINGLEMODE", false);
		
		return formview;
	}
	
	public void onRowDblClckSelect() {
		
		if(!updateBtn)
			return;
		
		String rowID = selectedRow[0].getDbID()+"";
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("action", "update");
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("rowID", rowID);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAMODE", new Boolean(alphaMode));
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAENTITY", alphaEntity);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ALPHAREFERENCE", selectedRow[0].getAlphaReference());
		
		
		String page = formview+".xhtml";
		
		
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(page);
		} catch (IOException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
	}
	
	public String doDelete(){
		if(selectedRow[0].isLocked()){
			FacesContext context = FacesContext.getCurrentInstance();  
			
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Impossible de supprimer cet enregistrement", "Cet enregistrement a été validé et verouillé, veuillez l'invalider avant de le supprimer"));
			return "";
		}
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String rowID = params.get("rowID");		
		
		Integer ID = new Integer(rowID);
		int index = mainIDS.indexOf(ID);
		ProtogenDataEngine engine = new ProtogenDataEngine();
		Boolean flag = engine.deleteRow(window, rowID, foreign.get(index), mtmTables);
		if(flag){
			processMessage = ResourceManager.getInstance().getMessage("delete_ok");
			messageClass = "goodMsg";
			ListKV dirty = null;
			for(ListKV list : values)
				if(list.getDbID() == ID)
					dirty = list;
			values.remove(dirty);
		} else {
			processMessage = ResourceManager.getInstance().getMessage("delete_ko");
			messageClass = "badMsg";
		}
		
		return "";
	}
	
	public String doPrimeDelete(){
		if(selectedRow == null || selectedRow.length==0){
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN,"Suppression","Veuillez sélectionner un enregistrement à supprimer"));
			return "";
		}
		if(selectedRow[0].isLocked()){
			FacesContext context = FacesContext.getCurrentInstance();  
			
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Impossible de supprimer cet enregistrement", "Cet enregistrement a été validé et verouillé, veuillez l'invalider avant de le supprimer"));
			return "";
		}
		Boolean flag=true;
		for(int i = 0 ; i < selectedRow.length ; i++){
			boolean localFlag=false;
			String rowID = selectedRow[i].getDbID()+"";		
			
			Integer ID = new Integer(rowID);
			int index = mainIDS.indexOf(ID);
			ProtogenDataEngine engine = new ProtogenDataEngine();
			localFlag =  engine.deleteRow(window, rowID, foreign.get(index), mtmTables);
			if(localFlag){
				ListKV dirty = null;
				for(ListKV list : values)
					if(list.getDbID() == ID)
						dirty = list;
				values.remove(dirty);
			}
			flag = flag && localFlag;
		}
		if(flag){
			FacesContext context = FacesContext.getCurrentInstance();  
			
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Enregsitrements supprimés", "Les enregistrements ont été supprimés avec succès"));
			
		} else {
			FacesContext context = FacesContext.getCurrentInstance();  
			
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Enregsitrements non supprimés", "Certains enregistrements n'ont pas été supprimés car ils sont toujours référencés dans l'application"));
			
		}
		
		return "";
		
	}
	
	public String exportFile(){
		
		try {
		String csvExport = "";
		for(PairKVElement title : titles){
			csvExport = csvExport+title.getValue()+",";
		}
		
		csvExport = csvExport+"\r";
		
		for(ListKV row : values){
			for(String val : row.getValue()){
				csvExport=csvExport+val+",";
			}
			csvExport = csvExport+"\r";
		}
		
		
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
	    ec.responseReset();
	    ec.setResponseContentType("text/plain");
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"protogen-export.csv\"");
	    
		OutputStream output = ec.getResponseOutputStream();
		
		OutputStreamWriter writer = new OutputStreamWriter(output) ;
		writer.write(csvExport);
		writer.close();
		
		fc.responseComplete();
		
		processMessage = ResourceManager.getInstance().getMessage("export_ok");
		messageClass = "goodMsg";
		} catch (IOException e) {

			e.printStackTrace();
		}
	    
		processMessage = ResourceManager.getInstance().getMessage("export_ko");
		messageClass = "badMsg";
		return "";
		
	}
	
	public String downloadContentAttribute(){
		
		if(selectedRow == null || selectedRow.length == 0){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Aucun enregistrement séléctionné",
					"Prière de séléctionner un enregistrement et de réessayer"));
			return "";
		}
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int ID = Integer.parseInt(params.get("fcaID"));
		for(CAttribute a : fcas){
			if(a.getId() == ID){
				selectedFca = a;
				break;
			}
		}
		
		Integer theID = new Integer(selectedRow[0].getDbID());
		ProtogenDataEngine engine = new ProtogenDataEngine();
		InputStream is = engine.getStream(selectedFca,window,theID.intValue());
		if(is == null){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Aucune pièce jointe n'est disponible pour cet enregistrement",
					"Prière de séléctionner un enregistrement et de réessayer"));
			return "";
		}
		String fn = selectedFca.getFileName()+"."+selectedFca.getFileExtension();
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
		
	 	ec.responseReset();
	    ec.setResponseContentType("application/octet-stream");
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\""+fn+"\"");
	    try{		
		    OutputStream output = ec.getResponseOutputStream();
			
			byte[] data = org.apache.commons.io.IOUtils.toByteArray(is);
			is.close();
			
			
			output.write(data);
			output.flush();
			output.close();
	
			fc.responseComplete();
	    } catch(Exception exc){
	    	exc.printStackTrace();
	    }
		return "";
		
	}
	public String docPrintParam() {  
		 Map<String, Object> docParameters = new HashMap<String, Object>(); 
			Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			int ID = Integer.parseInt(params.get("docID"));
			CDocumentbutton btn = null;
			for(CDocumentbutton b : window.getCDocumentbuttons()){
				if(b.getId()==ID){
					btn = b;
					break;
				}
			}
			
			FacesContext fc = FacesContext.getCurrentInstance();
		    ExternalContext ec = fc.getExternalContext();
			
			String jasperFile = "/reports/"+btn.getMDocument().getStream();
			String root= ec.getRealPath("");
			
			for(ButtonParameter bp : btn.getListParam()){
				String k = bp.getParameter();
				String type = bp.getType();
				String v = bp.getValue();
				Object value=null;
				if(type.equals("I"))
					value = new Integer(Integer.parseInt(v));
				if(type.equals("T"))
					value = v;
				
				
				
				docParameters.put(k, value);
			}
			
			switch(btn.getMDocument().getParameterMode()){
			case 'M':
				List<Integer> ids = new ArrayList<Integer>();
				if(!executionTrace.containsKey(btn)){
					if(selectedRow.length == 0){
						ids.add(new Integer(0));
					}
					
					for(ListKV list : selectedRow){
						ids.add(new Integer(list.getDbID()));
					}
				} else {
					ids = executionTrace.get(btn);
				}
				docParameters.put("selbeans", ids);
				break;
			case 'S':
				if(selectedRow.length == 0){
					Integer theID = new Integer(0);
					docParameters.put("selbean", theID);
				} else {
					Integer theID = new Integer(selectedRow[0].getDbID());
					docParameters.put("selbean", theID);
				}
				break;
			}
				
			/*DocumentEngine engine = new DocumentEngine();
			OutputStream output;
			String filepath =  engine.compile(root, jasperFile, docParameters,btn);
			try{
				ec.responseReset();
			    ec.setResponseContentType("text/plain");
			    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"protoprint.pdf\"");
			    
				output = ec.getResponseOutputStream();
				
				
				
				InputStream fileStr = new FileInputStream(filepath);
				
				byte[] data = org.apache.commons.io.IOUtils.toByteArray(fileStr);
				fileStr.close();
				
				File f = new File(filepath);
				if(f.exists())
					f.delete();
				
				output.write(data);
				output.flush();
				output.close();
				output = null;
				fc.responseComplete();
				fc.renderResponse();
				return null;
			} catch(Exception exc){
				
			}
			return  null;
			*/
			
			
			try{
			    ec.responseReset();
			    ec.setResponseContentType("text/plain");
			    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"protoprint.pdf\"");
			    
			    DocumentEngine engine = new DocumentEngine();
				OutputStream output;
				
				
				output = ec.getResponseOutputStream();
				
				engine.compile(root, jasperFile, docParameters, btn, output);
				fc.responseComplete();
				
				return null;
				
				
			} catch(Exception exc){
				String dummy = exc.getMessage();
				dummy=dummy+"";
			}
			
			return null;
	   }  
	
	public String docPrint(){
		Map<String, Object> docParameters = new HashMap<String, Object>(); 
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int ID = Integer.parseInt(params.get("docID"));
		CDocumentbutton btn = null;
		boolean boundFlag=false;;
		for(CDocumentbutton b : window.getCDocumentbuttons()){
			if(b.getId()==ID){
				btn = b;
				break;
			}
		}
		
		if(btn==null){
			for(CDocumentbutton b : window.getBoundDocuments()){
				if(b.getId()==ID){
					btn = b;
					boundFlag = true;
					break;
				}
			}
		}
		
		if(btn==null)
			return null;
		
		
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
		
		String jasperFile = "/reports/"+btn.getMDocument().getStream();
		String root= ec.getRealPath("");
		String sparams = btn.getParameters();
		String[] docp = sparams.split(";");
		if(inProcess){
			for(String key : docp){
				if(key.split("\\:") == null || key.split("\\:").length <2)
					continue;
				String k = key.split(":")[0];
				String type = key.split(":")[1];
				String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
				String v = ProcessScreenListener.getInstance().getParameters(ApplicationRepository.getInstance().getCache(skey).getUser()).get(k);
				Object value=null;
				if(type.equals("I"))
					value = new Integer(Integer.parseInt(v));
				if(type.equals("T"))
					value = v;
				
				
				docParameters.put(k, value);
			}
			
		} 
			
		switch(btn.getMDocument().getParameterMode()){
		case 'M':
			
			List<Integer> ids = new ArrayList<Integer>();
			if(!boundFlag){
				if(selectedRow.length == 0){
					ids.add(new Integer(0));
				}
				
				for(ListKV list : selectedRow){
					ids.add(new Integer(list.getDbID()));
				}
			} else {
				for(CActionbutton a : window.getCActionbuttons())
					if(a.isBound() && a.getBoundDocument().getId()==btn.getId()){
						ids = executionTrace.get(a);
						break;
					}
			}
			docParameters.put("selbeans", ids);
			break;
		case 'S':
			if(selectedRow.length == 0){
				Integer theID = new Integer(0);
				docParameters.put("selbean", theID);
			} else {
				Integer theID = new Integer(selectedRow[0].getDbID());
				docParameters.put("selbean", theID);
			}
			break;
		}
		
		String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
		CoreUser u = ApplicationRepository.getInstance().getCache(skey).getUser();
		docParameters.put("puser", new Integer(u.getId()));
		
	
		
		try{
			ec.responseReset();
		    ec.setResponseContentType("text/plain");
		    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"protoprint.pdf\"");
		    
		    DocumentEngine engine = new DocumentEngine();
			OutputStream output;
			
			
			output = ec.getResponseOutputStream();
			
			engine.compile(root, jasperFile, docParameters, btn, output);
			fc.responseComplete();
			
			
			return null;
		} catch(Exception exc){
			
		}
		return  null;
	}
	
	public void prepareParameters(){
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int ID = Integer.parseInt(params.get("actionParID"));
		CActionbutton btn = null;
		for(CActionbutton cbtn : buttons){
			if(cbtn.getId() == ID)
				btn = cbtn;
		}
		buttonID = ID;
		parsToShow = btn.getListParam();
	}
	
	public void prepareBatchParameters(){
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String batchtitle = params.get("batchParID");
		ActionBatch btn = null;
		for(ActionBatch cbtn : batches){
			if(cbtn.getTitle().equals(batchtitle))
				btn = cbtn;
		}
		batchID = batchtitle;
		parsToShow = btn.getListParam();
	}
	
	
	public void doBatchCalculus(){
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String t=params.get("batchID");
		
		if(t!=null && t.length()>0)
			batchID = t;
		
		
		silentMode = true;
		ActionBatch b=new ActionBatch();
		for(ActionBatch a : batches)
			if(a.getTitle().equals(batchID)){
				b=a;
				break;
			}
		
		if(b.isParametered()){
			for(CActionbutton btn : b.getButtons())
				btn.setListParam(b.getListParam());
		}
		
		for(CActionbutton btn : b.getButtons()){
			buttonID = btn.getId();
			doCalculus();
		}
		FacesContext context = FacesContext.getCurrentInstance();  
        
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Traitement executé avec succès", ""));
		
		//	Alert
		String reference = b.getTitle();
		for(SAlert a : alerts){
			AlertInstance i = new AlertInstance();
			i.setAlert(a);
			i.setMessage(a.getDescription().replaceAll("<<Référence>>", reference));
			
			AlertDataAccess ada = new AlertDataAccess();
			ada.insertAlert(i);
		}
		
		silentMode=false;
	}
	
	public void doCalculus(){

		List<Integer> selectedIds = new ArrayList<Integer>();
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int ID;
		if(params.containsKey("actionID") && params.get("actionID")!=null && params.get("actionID").length()>0 && !params.get("actionID").equals("null"))
			ID = Integer.parseInt(params.get("actionID"));
		else
			ID = buttonID;
		CActionbutton btn = null;
		for(CActionbutton cbtn : buttons){
			if(cbtn.getId() == ID)
				btn = cbtn;
		}
		
		MAction act = btn.getMAction();
		
		if(act.getCode().contains("## MULTI LIGNES")){
			doMultiLigneCalculus(act,btn);
			return;
		}
		
		ApplicationLoader loader = new ApplicationLoader();
		List<String> tags = loader.loadArguments(act);
		List<Map<String, Double>> tags_values = new ArrayList<Map<String,Double>>();
		ProtogenDataEngine pde = new ProtogenDataEngine();
		int i=0;
		for(ListKV row : selectedRow){

			i = values.indexOf(row);
			selectedIds.add(mainIDS.get(i));
			Map<String, Double> tag_v = new HashMap<String, Double>();
			for(String key : tags){
				int index = 0;
				boolean foundFlag = false;
				
				
				//	Traiter le cas d'une somme sur les colonnes Clé/Valeur
				if(key.startsWith("SOMME_") && key.split(":").length==1){			//	Cas d'une somme inconditionnelle
					
					CAttribute aref = new CAttribute();
					key = key.replaceAll("SOMME_", "");
					for(CAttribute a :  window.getCAttributes()){
						if(a.getAttribute().equals(key)){
							aref = a;
							break;
						}
					}
					double somme = 0;
					
					//	Detect the headers attribute
					String sourceTable = "";
					for(CAttribute a : window.getCAttributes()){
						if(a.getEntity().getDataReference().equals(aref.getEntity().getDataReference()) && a.getDataReference().startsWith("fk_")){
							sourceTable = a.getDataReference().substring(3);
							break;
						}
					}
					
					if(sourceTable.equals(""))			// Most likely a header reference
						continue;
					
					List<PairKVElement> refkeys = pde.getDataKeys(sourceTable, false, 0);
					for(PairKVElement t : titles){
						boolean flag=false;
						for(PairKVElement e : refkeys)
							if(t.getValue().equals(e.getValue())){
								flag = true;
								break;
							}
						
						if(!flag)
							continue;
						
						String sval = row.getValue().get(titles.indexOf(t));
						if(sval.equals("-")){
							sval="0.0";
						}
						somme = somme+Double.parseDouble(sval);
					}
					tag_v.put("SOMME_"+key, somme);
					continue;
				} else if (key.startsWith("SOMME_") && key.split(":").length>1){		//	Cas d'une somme conditionnelle
					CAttribute aref = new CAttribute();
					String constraint = key.split(":")[1];
					key = key.split(":")[0];
					key = key.replaceAll("SOMME_", "");
					for(CAttribute a :  window.getCAttributes()){
						if(a.getAttribute().equals(key)){
							aref = a;
							break;
						}
					}
					double somme = 0;
					
					//	Detect the headers attribute
					String sourceTable = "";
					for(CAttribute a : window.getCAttributes()){
						if(a.getEntity().getDataReference().equals(aref.getEntity().getDataReference()) && a.getDataReference().startsWith("fk_")){
							sourceTable = a.getDataReference().substring(3);
							break;
						}
					}
					
					List<String> constraintDecomposited = StringFormat.getInstance().decomposeConstraint(constraint);
					if(constraintDecomposited.size()==0)
						continue;
					
					// Let's detect the condition attribute
					CBusinessClass sourceEntity = pde.getReferencedTable(sourceTable);
					sourceEntity = pde.populateEntity(sourceEntity);
					String attributeDR = "";
					for(CAttribute a : sourceEntity.getAttributes()){
						if(a.getAttribute().trim().equals(constraintDecomposited.get(0).trim())){
							attributeDR = a.getDataReference();
							break;
						}
					}
					constraintDecomposited.set(0, attributeDR);
					List<PairKVElement> refkeys = pde.getDataKeys(sourceTable, false, 0, constraintDecomposited);
					for(PairKVElement t : titles){
						boolean flag=false;
						for(PairKVElement e : refkeys)
							if(t.getValue().equals(e.getValue())){
								flag = true;
							}
						
						if(!flag)
							continue;
						
						String sval = row.getValue().get(titles.indexOf(t));
						if(sval.equals("-"))
							sval="0";
						somme = somme+Double.parseDouble(sval);
					}
					tag_v.put("SOMME_"+key+":"+constraint, somme);
					continue;
				}
				
				for(PairKVElement title : titles){
					if(title.getValue().equals(key)){
						index = titles.indexOf(title);
						foundFlag = true;
						break;
					}
				}
				if(!foundFlag){
					double v = 0;
					if(key.equals("#UTILISATEUR#")){
						v = 1.0*ApplicationRepository.getInstance().getCache(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY").toString()).getUser().getId();
					}
					else if(key.equals("#IDENTIFIANT#")){
						v = 1.0*mainIDS.get(i).intValue();
					}
					else if(key.startsWith("valeur=")){		//	Valeur constante
						String k = key.split("=")[1].replaceAll(">>", "");
						v = Double.parseDouble(k);
					}else if(key.startsWith("VALEUR_")){
						String formula = key.split("_")[1];
						String table = formula.substring(0,formula.indexOf("("));
						String lk = formula.substring(formula.indexOf('(')+1,formula.indexOf(')'));
						String attrlabel = formula.split("\\.")[1];
						ProtogenDataEngine engine = new ProtogenDataEngine();
						ApplicationLoader al = new ApplicationLoader();
						CBusinessClass entity = al.getClassByName(table);
						entity = engine.populateEntity(entity);
						int I = 0;
						ApplicationLoader dal = new ApplicationLoader();
						String rrtable=entity.getDataReference();
						CBusinessClass cee = dal.getEntity(rrtable);
						cee = engine.populateEntity(cee);
						ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
						List<PairKVElement> entityKeys = engine.getDataKeys(rrtable,(cee.getUserRestrict()=='Y'),cache.getUser().getId());
						
						for(PairKVElement e : entityKeys)
							if(e.getValue().equals(lk)){
								I = Integer.parseInt(e.getKey());
								break;
							}
						
						CAttribute vat = new CAttribute();
						for(CAttribute a : entity.getAttributes())
							if(a.getAttribute().equals(attrlabel)){
								vat = a;
								break;
							}
						
						v = engine.getInjectedValue(entity,vat,I);
					}
					
					tag_v.put(key, v);
				} else {
					Integer I = new Integer(index);
					if(index == userFieldIndex){
						if(row.getValue().get(index).length()<=1) {
							tag_v.put(key, new Double(0));
							continue;
						}
						//	User field
						String prenom = row.getValue().get(index).split(" ")[0];
						String nom = row.getValue().get(index).split(" ")[1];
						
						UserServices dal = new UserServices();
						CoreUser u = dal.getUserByName(prenom,nom);
						Double d = new Double(u.getId()*1.0);
						tag_v.put(key, d);
					} else if(references.contains(I)){
						String k = row.getValue().get(index);
						ProtogenDataEngine engine = new ProtogenDataEngine();
						Double d = new Double(0);
						String dr="";
						for(CAttribute a : window.getCAttributes()){
							if(a.getAttribute().equals(key)){
								dr=a.getDataReference();
								break;
							}
						}
						ApplicationLoader dal= new ApplicationLoader();
						CBusinessClass ce = dal.getEntity(dr.substring(3));
						ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
						List<PairKVElement> listElements = engine.getDataKeys(dr.substring(3),(ce.getUserRestrict()=='Y'),cache.getUser().getId());

						for(PairKVElement e : listElements){
							if(e.getValue().equals(k)){
								d = new Double(e.getKey());
								break;
							}
						}
						tag_v.put(key, d);
					} else{
						Double d;
						if(row.getValue().get(index).split("-").length>1){
							int year = Integer.parseInt(row.getValue().get(index).split("-")[0]);
							int month = Integer.parseInt(row.getValue().get(index).split("-")[1]);
							int day = Integer.parseInt(row.getValue().get(index).split("-")[2]);
							Calendar c = Calendar.getInstance();
							c.set(Calendar.YEAR, year);
							c.set(Calendar.MONTH, month-1);
							c.set(Calendar.DAY_OF_MONTH, day);
							
							d = new Double(c.getTimeInMillis());
						} else if (row.getValue().get(index).trim().equals("OUI"))
							d = 1.0;
						else if (row.getValue().get(index).trim().equals("NON"))
							d = 0.0;
						else
							d = new Double(StringFormat.getInstance().tryParse(row.getValue().get(index)));
						tag_v.put(key, d);
					}
				}
			}
			tags_values.add(tag_v);
			
		}
		
		List<Map<String,String>> postActionParameters = new ArrayList<Map<String, String>>();
		MPostAction postAction = loader.getPostAction(act);
		i=0;
		for(ListKV row : selectedRow){
			i = values.indexOf(row);
			Map<String, String> tag_v = new HashMap<String, String>();
			for(String key : postAction.getParametersValues()){
				if(key == null || key.length()==0)
					continue;
				
				
				
				key = key.replaceAll("<<", "");
				key = key.replaceAll(">>", "");
				int index = 0;
				boolean foundFlag = false;
				for(PairKVElement title : titles){
					if(key.toLowerCase().equals(StringFormat.getInstance().attributeDataReferenceFormat(title.getValue()).toLowerCase())){
						index = titles.indexOf(title);
						foundFlag = true;
						break;
					}
				}
				if(!foundFlag){
					String v ="";
					if(key.toLowerCase().equals(StringFormat.getInstance().attributeDataReferenceFormat("#UTILISATEUR#"))){
						v = ""+ApplicationRepository.getInstance().getCache(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY").toString()).getUser().getId();
					}
					else if(key.toLowerCase().equals(StringFormat.getInstance().attributeDataReferenceFormat("#IDENTIFIANT#"))){
						v = ""+mainIDS.get(i).intValue();
					}else if(key.startsWith("valeur=")){		//	Valeur constante
						v = key.split("=")[1].replaceAll(">>", "");
					}else if(key.startsWith("dvaleur=")){		//	Valeur constante
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						v = df.format(new Date())+"+00";
					}else if(key.startsWith("seq:")){
						String suf = key.split(":")[1];
						String sequence = key.split(":")[2];
						int in = Integer.parseInt(key.split(":")[3]);
						
						ProtogenDataEngine engine = new ProtogenDataEngine();
						int seq = engine.nextVal(sequence, in>0);
						
						v = suf+seq;
					}
					
					if(v==""){
						//	look for it in parameters
						for(ButtonParameter p : parsToShow)
							if(StringFormat.getInstance().attributeDataReferenceFormat(p.getTitle()).equals(key))
								v=p.getValue()+"";
					}
					
					tag_v.put(key, v);
				} else {
					Integer I = new Integer(index);
					if(index == userFieldIndex){
						if(row.getValue().get(index).length()<=1) {
							tag_v.put(key, "0");
							continue;
						}
						//	User field
						String prenom = row.getValue().get(index).split(" ")[0];
						String nom = row.getValue().get(index).split(" ")[1];
						
						UserServices dal = new UserServices();
						CoreUser u = dal.getUserByName(prenom,nom);
						Double d = new Double(u.getId()*1.0);
						tag_v.put(key, d+"");
					} else if(references.contains(I)){
						String k = row.getValue().get(index);
						ProtogenDataEngine engine = new ProtogenDataEngine();
						Integer d = new Integer(0);
						String title="";
						for(PairKVElement t : titles){
							if(t.getKey().equals(index+"")){
								title = t.getValue();
								break;
							}
						}
						String rtable="";
						for(CAttribute a : window.getCAttributes()){
							if(a.getAttribute().equals(title)){
								rtable = a.getDataReference().substring(3);
								break;
							}
						}
						ApplicationLoader dal= new ApplicationLoader();
						CBusinessClass ce = dal.getEntity(rtable);
						ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
						List<PairKVElement> listElements = engine.getDataKeys(rtable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());

						for(PairKVElement e : listElements){
							if(e.getValue().trim().equals(k.trim())){
								d = new Integer(e.getKey());
								break;
							}
						}
						tag_v.put(key, d+"");
					} else{
						//	error -1111111
						String val=row.getValue().get(index);
						if (val.trim().equals("OUI"))
							val = "1.0";
						if (val.trim().equals("NON"))
							val = "0.0";
						String converted = StringFormat.getInstance().tryParse(val);
						if(converted.equals("-1111111"))
							tag_v.put(key, row.getValue().get(index));
						else if(converted.split("-").length==3){
							int year = Integer.parseInt(row.getValue().get(index).split("-")[0]);
							int month = Integer.parseInt(row.getValue().get(index).split("-")[1]);
							int day = Integer.parseInt(row.getValue().get(index).split("-")[2]);
							Calendar c = Calendar.getInstance();
							c.set(Calendar.YEAR, year);
							c.set(Calendar.MONTH, month-1);
							c.set(Calendar.DAY_OF_MONTH, day);
							
							Double d = new Double(c.getTimeInMillis());
							
							tag_v.put(key, d+"");
						} else {
							Double d = new Double(converted);
							tag_v.put(key, d+"");
						}
						
					}
				}
			}
			postActionParameters.add(tag_v);
			
		}
		
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
		
		CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
		Map<String, String> processParameters = new HashMap<String, String>();
		if(inProcess){
			String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
			processParameters = ProcessScreenListener.getInstance().getParameters(ApplicationRepository.getInstance().getCache(skey).getUser());
		} else if (btn.getListParam() != null) {
			for(ButtonParameter bp : btn.getListParam()){
				processParameters.put(bp.getParameter(), bp.getValue());
			}
			for(ButtonParameter bp : this.parsToShow){
				processParameters.put(bp.getParameter(), bp.getValue());
			}
			
			for(String k : processParameters.keySet()){
				for(Map<String, Double> vs : tags_values)
					vs.put(k, new Double(processParameters.get(k)));
			}
		} 
		
		//		Traiter la présence du Header
		keyMapList = new ArrayList<Map<CAttribute,String>>();
		List<HeaderExecutionResult> headers = compileValueHeaders(tags_values, tags, act, processParameters);
		List<HeaderExecutionResult> tempheaders =  compileHeaders(tags_values, tags, act, processParameters,headers);
		for(HeaderExecutionResult t : tempheaders){
			headers.add(t);
		}
		
		//		Remplacer les éléments concernés
		for(Map<String, Double> tagmap : tags_values){
			int index = tags_values.indexOf(tagmap);
			for(HeaderExecutionResult her : headers){
				
				for(String key : tags){
					
					if(key.split("_").length<=1)
						continue;
					if(!key.split("_")[1].equals(her.getVariable()))
						continue;
					if(key.startsWith("SOMME_")){
						tagmap.put(key, her.somme().get(index));
					} else if (key.startsWith("PRODUIT_")){
						tagmap.put(key, her.produit().get(index));
					}
				}
			}
		}
		int id =0;
		try {
			
			
			List<List<Double>> results = engine.returnListExecution(tags_values, tags, act, processParameters);
			ProtogenDataEngine dataEngine = new ProtogenDataEngine();
			boolean partial=false;
			boolean noneexecuted=true;
			executionTrace.put(btn, new ArrayList<Integer>());
			for(i = 0 ; i < results.size() ; i++){
				boolean subpartial=false;
					for(int j=0;j<results.get(i).size();j++)
						if(results.get(i).get(j)<=-999.999){
							partial=true;
							subpartial=true;
							break;
						}
				
				
				
				if(subpartial)
					continue;
				
				
				//	Put the values in postaction parameters
				for(int j = 0 ; j < results.get(i).size() ; j++){
					double d = results.get(i).get(j);
					postActionParameters.get(i).put("resultat_"+(j+1),d+"");
				}
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				Map<CAttribute, String> map = new HashMap<CAttribute, String>();
				if(keyMapList.size()>0)
					map=keyMapList.get(i);
				id = dataEngine.executePost(postAction, window.getMainEntity(), selectedIds.get(i), postActionParameters.get(i), processParameters, headers, i, map,cache.getUser());
					
				if(btn.isBound() && id>0){
					CDocumentbutton bound = btn.getBoundDocument();
					if(!window.getBoundDocuments().contains(bound))
						window.getBoundDocuments().add(bound);
					executionTrace.get(btn).add(new Integer(id));
					
					FrontController ctrl = (FrontController)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FRONT_CTRL");
					ctrl.updateBoundButtons(window.getBoundDocuments());
					docAvailable = docAvailable ||(ctrl.getBoundButtons()!=null && ctrl.getBoundButtons().size()>0);
				}
				
				noneexecuted=false;
			}
			if(!silentMode && id >0){
				if(noneexecuted){
					processMessage = "L'action a échoué, aucun enregistrement n'a été traité";
					messageClass = "goodMsg";
					FacesContext context = FacesContext.getCurrentInstance();  
					
					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Echec", processMessage)); 
				}
				else if(!partial){
					processMessage = ResourceManager.getInstance().getMessage("calculus_ok");
					messageClass = "goodMsg";
					FacesContext context = FacesContext.getCurrentInstance();  
			          
			        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Succés", processMessage)); 
				}
				else {
					processMessage = "Certains enregistrement n'ont pas été traités";
					messageClass = "goodMsg";
					FacesContext context = FacesContext.getCurrentInstance();  
			          
			        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Exécution partielle", processMessage)); 
				}
				
				//	Alert
				int dbid = selectedRow[0].getDbID();
				String table = window.getMainEntity();
				PairKVElement pkv = pde.getDataKeyByID(table, dbid);
				String reference = pkv.getValue();
				for(SAlert a : alerts){
					AlertInstance in = new AlertInstance();
					in.setAlert(a);
					in.setMessage(a.getDescription().replaceAll("<<Référence>>", reference));
					
					AlertDataAccess ada = new AlertDataAccess();
					ada.insertAlert(in);
				}
				
			}
			
			
		} catch (Exception e) {

			e.printStackTrace();
			
			processMessage = ResourceManager.getInstance().getMessage("calculus_ko");
			messageClass = "badMsg";
			FacesContext context = FacesContext.getCurrentInstance();  
	          
	        context.addMessage(null, new FacesMessage("Echec", processMessage));
		}
		
		selectAll = false;
		for(i=0 ; i < values.size();i++){
			values.get(i).setSelected(false);
		}
		
	}

	
	
	
	private void doMultiLigneCalculus(MAction act,CActionbutton btn) {
		//	Clean header
		String sourceCode = act.getCode().replace("## MULTI LIGNES", "");
		List<Integer> selectedIds = new ArrayList<Integer>();
		Map<String,String> params = new HashMap<String, String>();
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
		
		CalculusEngine cengine = new CalculusEngine(ec.getRealPath(""));
		int i=0;
		for(ListKV row : selectedRow){
			ApplicationLoader loader = new ApplicationLoader();
			List<String> tags = loader.loadArguments(act);
			i = values.indexOf(row);
			selectedIds.add(mainIDS.get(i));
			sourceCode = act.getCode().replace("## MULTI LIGNES", "");
			//replace non variant values
			for(String tag : tags){
				if(tag.contains("::"))
					continue;
				
				//	Current user
				if(tag.toLowerCase().equals(("#UTILISATEUR#").toLowerCase())){
					String v = ApplicationRepository.getInstance().getCache(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY").toString()).getUser().getId()+".0";
					sourceCode = sourceCode.replaceAll(tag, v);
					continue;
				}
				//	Row ID
				if(tag.toLowerCase().equals(("#IDENTIFIANT#").toLowerCase())){
					String v = mainIDS.get(i).intValue()+".0";
					sourceCode = sourceCode.replaceAll(tag, v);
					continue;
				}
				
				//	Other values
				int index = 0;
				for(PairKVElement title : titles){
					if(title.getValue().equals(tag)){
						index = titles.indexOf(title);
						break;
					}
				}
				
				Integer I = new Integer(index);
				if(index == userFieldIndex){
					if(row.getValue().get(index).length()<=1) {
						sourceCode = sourceCode.replaceAll(tag, "0.0");
						continue;
					}
					//	User field
					String prenom = row.getValue().get(index).split(" ")[0];
					String nom = row.getValue().get(index).split(" ")[1];
					
					UserServices dal = new UserServices();
					CoreUser u = dal.getUserByName(prenom,nom);
					Double d = new Double(u.getId()*1.0);
					sourceCode = sourceCode.replaceAll(tag, ""+d);
				} else if(references.contains(I)){
					String k = row.getValue().get(index);
					ProtogenDataEngine engine = new ProtogenDataEngine();
					Double d = new Double(0);
					
					String rtable=window.getCAttributes().get(index).getDataReference().substring(3);
					ApplicationLoader dal= new ApplicationLoader();
					CBusinessClass ce = dal.getEntity(rtable);
					ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
					List<PairKVElement> listElements = engine.getDataKeys(rtable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());

					for(PairKVElement e : listElements){
						if(e.getValue().equals(k)){
							d = new Double(e.getKey());
							break;
						}
					}
					sourceCode = sourceCode.replaceAll(tag, ""+d);
				} else{
				
					Double d = new Double(StringFormat.getInstance().tryParse(row.getValue().get(index)));
					sourceCode = sourceCode.replaceAll("<<"+tag+">>", ""+d);
				}
				
			}
			
			MPostAction postAction = loader.getPostAction(act);
			for(String key : postAction.getParametersValues()){
				if(key == null || key.length()==0 || key.contains("::"))
					continue;
				key = key.replaceAll("<<", "");
				key = key.replaceAll(">>", "");
				int index = 0;
				boolean foundFlag = false;
				for(PairKVElement title : titles){
					if(key.equals(StringFormat.getInstance().attributeDataReferenceFormat(title.getValue()))){
						index = titles.indexOf(title);
						foundFlag = true;
						break;
					}
				}
				if(!foundFlag){
					int v = 0;
					if(key.toLowerCase().equals(StringFormat.getInstance().attributeDataReferenceFormat("#UTILISATEUR#"))){
						v = ApplicationRepository.getInstance().getCache(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY").toString()).getUser().getId();
					}
					else if(key.toLowerCase().equals(StringFormat.getInstance().attributeDataReferenceFormat("#IDENTIFIANT#"))){
						v = mainIDS.get(i).intValue();
					}
					params.put(key, v+"");
				} else {
					Integer I = new Integer(index);
					if(index == userFieldIndex){
						if(row.getValue().get(index).length()<=1) {
							params.put(key, "0");
							continue;
						}
						//	User field
						String prenom = row.getValue().get(index).split(" ")[0];
						String nom = row.getValue().get(index).split(" ")[1];
						
						UserServices dal = new UserServices();
						CoreUser u = dal.getUserByName(prenom,nom);
						Double d = new Double(u.getId()*1.0);
						params.put(key, d+"");
					} else if(references.contains(I)){
						String k = row.getValue().get(index);
						ProtogenDataEngine engine = new ProtogenDataEngine();
						Integer d = new Integer(0);
						String title="";
						for(PairKVElement t : titles){
							if(t.getKey().equals(index+"")){
								title = t.getValue();
								break;
							}
						}
						String rtable="";
						for(CAttribute a : window.getCAttributes()){
							if(a.getAttribute().equals(title)){
								rtable = a.getDataReference().substring(3);
								break;
							}
						}
						ApplicationLoader dal= new ApplicationLoader();
						CBusinessClass ce = dal.getEntity(rtable);
						ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
						List<PairKVElement> listElements = engine.getDataKeys(rtable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());

						for(PairKVElement e : listElements){
							if(e.getValue().equals(k)){
								d = new Integer(e.getKey());
								break;
							}
						}
						params.put(key, d+"");
					} else{
					
						Double d = new Double(StringFormat.getInstance().tryParse(row.getValue().get(index)));
						params.put(key, d+"");
					}
				}
			}
			
			
		
			
			//	Now it's time to detect our trouble maker
			String mtmReference="";
			for(String tag : tags)
				if(tag.contains("::")){
					
					mtmReference = tag.split("::")[0];
					break;
				}
			
			//	Now let's look for the attribute index
			int attributeIndex = 0;
			String tabletoquery = "";
			for(CAttribute a : window.getCAttributes()){
				if(a.getAttribute().equals(mtmReference)){
					//	Bingo
					attributeIndex = window.getCAttributes().indexOf(a);
					tabletoquery = a.getDataReference().substring(3);
					break;
				}
			}
			
			// Now foreach column let us construct a program, compile it and store the result
			ProtogenDataEngine engine = new ProtogenDataEngine();
			String rtable=tabletoquery;
			ApplicationLoader dal= new ApplicationLoader();
			CBusinessClass ce = dal.getEntity(rtable);
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			List<PairKVElement> tablekeys = engine.getDataKeys(rtable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());

			for(PairKVElement key : tablekeys){
				for(PairKVElement title : titles){
					if(title.getValue().replaceAll(" ", "").equals(key.getValue().replaceAll(" ", ""))){
						int tindex = titles.indexOf(title);
						String v = values.get(i).getValue().get(tindex);
						String k = key.getKey();
						
						String finalCode = sourceCode.replaceAll("<<"+window.getCAttributes().get(attributeIndex).getAttribute()+"::clé>>", k);
						finalCode = sourceCode.replaceAll("<<"+window.getCAttributes().get(attributeIndex).getAttribute()+"::valeur>>", v.equals("-")?"0":v);
						params.put(StringFormat.getInstance().attributeDataReferenceFormat(window.getCAttributes().get(attributeIndex).getAttribute()+"::valeur"), v.equals("-")?"0":v);
						params.put(StringFormat.getInstance().attributeDataReferenceFormat(window.getCAttributes().get(attributeIndex).getAttribute()+"::clé"), k);
						List<Double> results = new ArrayList<Double>(); 
						try{
							results = cengine.executePlainTextCode(finalCode);
						}catch(Exception e){
							e.printStackTrace();
						}
						for(Double r : results){
							params.put("resultat_"+(results.indexOf(r)+1), r+"");
						}
						
						boolean partial=false;
						boolean noneexecuted=true;
						executionTrace.put(btn, new ArrayList<Integer>());
						for(int j = 0 ; j < results.size() ; j++){
							boolean subpartial=false;
								
									if(results.get(j)<=-999.999){
										partial=true;
										subpartial=true;
										break;
									}
							
							
							
							if(subpartial)
								continue;
							
							
							int id = executePost(postAction, window.getMainEntity(), selectedIds.get(i), params, new HashMap<String, String>(), new ArrayList<HeaderExecutionResult>(), i, new HashMap<CAttribute, String>());
							
											
							if(btn.isBound()){
								CDocumentbutton bound = btn.getBoundDocument();
								if(!window.getBoundDocuments().contains(bound))
									window.getBoundDocuments().add(bound);
								executionTrace.get(btn).add(new Integer(id));
								
								FrontController ctrl = (FrontController)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FRONT_CTRL");
								ctrl.updateBoundButtons(window.getBoundDocuments());
							}
							
							noneexecuted=false;
						}
						
						if(noneexecuted){
							processMessage = "L'action a échoué, aucun enregistrement n'a été traité";
							messageClass = "goodMsg";
							FacesContext context = FacesContext.getCurrentInstance();  
							
							context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Echec", processMessage)); 
						}
						else if(!partial){
							processMessage = ResourceManager.getInstance().getMessage("calculus_ok");
							messageClass = "goodMsg";
							FacesContext context = FacesContext.getCurrentInstance();  
					          
					        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Succés", processMessage)); 
						}
						else {
							processMessage = "Certains enregistrement n'ont pas été traités";
							messageClass = "goodMsg";
							FacesContext context = FacesContext.getCurrentInstance();  
					          
					        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Exécution partielle", processMessage)); 
						}
					}
				}
			}
		}
	}

	private int executePost(MPostAction postAction, String mainEntity,
			Integer selectedId, Map<String, String> params,
			HashMap<String, String> hashMap,
			ArrayList<HeaderExecutionResult> arrayList, int i,
			HashMap<CAttribute, String> hashMap2) {
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		return engine.executePost(postAction, window.getMainEntity(), selectedId, params, new HashMap<String, String>(), new ArrayList<HeaderExecutionResult>(), i, new HashMap<CAttribute, String>(),cache.getUser());
	}

	private List<HeaderExecutionResult> compileHeaders(
			List<Map<String, Double>> tags_values, List<String> tags,
			MAction act, Map<String, String> processParameters, List<HeaderExecutionResult> vheaders) {
		// TODO Auto-generated method stub
		
		List<HeaderExecutionResult> results = new ArrayList<HeaderExecutionResult>();
		
		
		String headerSource=act.getCode();
		if(headerSource.indexOf('"')<0)
			return results;
		headerSource = headerSource.substring(headerSource.indexOf("\"")+1, headerSource.lastIndexOf("\""));
		
		String[] headers = headerSource.split(";");
		for(String header : headers){
			boolean valueFlag=false;
			if(header.toLowerCase().contains("valeur:")){ 
				continue;
			}
			String reference = header.substring(header.indexOf(" ")+1);
			String variable = header = header.substring(header.lastIndexOf(" ")+1);
			reference = reference.substring(0,reference.lastIndexOf(" "));
			reference = reference.substring(0,reference.lastIndexOf(" "));
			
			reference = reference.replaceAll("<<", "");
			reference = reference.replaceAll(">>", "");
			variable = variable.replaceAll("<<", "");
			variable = variable.replaceAll(">>", "");
			
			HeaderExecutionResult her = new HeaderExecutionResult();
			
			her.setVariable(variable);
			
			//	Detecte attribute
			String attribute = reference.split("\\.")[0];
			String formulaAttribute = reference.split("\\.")[1];
			attribute = attribute.replaceAll("calculer ", "").trim();
			CAttribute referenceAttribute = null;
			for(CAttribute a : window.getCAttributes())
				if(a.getAttribute().equals(attribute)){
					referenceAttribute = a;
					break;				
				}
			
			if(referenceAttribute == null)	//	No columns for this one
				continue;
			
			her.setReferenceAttribute(referenceAttribute);
			String referenceTable = referenceAttribute.getDataReference().substring(3);
			ProtogenDataEngine engine = new ProtogenDataEngine();
			
			String rtable=referenceTable;
			ApplicationLoader dal= new ApplicationLoader();
			CBusinessClass ce = dal.getEntity(rtable);
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			List<PairKVElement> listElements = engine.getDataKeys(rtable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());
			
			her.setValues(new ArrayList<Map<Integer,Double>>());
			for(int I = 0; I < selectedRow.length ; I++){
				ListKV row = selectedRow[I];
				Map <Integer, Double> values = new HashMap<Integer, Double>();
				for(PairKVElement t : titles){
					int i = 0;
					int calcRowID = 0;
					boolean flag=false;
					for(PairKVElement k : listElements)
						if(k.getValue().equals(t.getValue())){
							calcRowID = Integer.parseInt(k.getKey());
							i=titles.indexOf(t);
							break;
						}
					if(calcRowID>0){
						String v = row.getValue().get(i);
						if(v.equals("Oui")){
							flag = true;
						}
					}
					if(flag){
						EntityDTO dto = engine.getDatumByID(referenceTable, calcRowID);
						
						if(valueFlag){
							
							for(CAttribute a : dto.getEntity().getAttributes())
								if(a.getAttribute().equals(formulaAttribute)){
									values.put(new Integer(calcRowID), new Double(dto.getValues().get(a)));
									break;
								}
							continue;
						}
						
						//	Get source code
						String formulaSource = "";
						for(CAttribute a : dto.getEntity().getAttributes())
							if(a.getAttribute().equals(formulaAttribute)){
								formulaSource = dto.getValues().get(a);
								
								break;
							}
						
						System.out.println("----------------------Avant Compilation----------------");
						System.out.println(formulaSource);
						System.out.println("----------------------------------------------------------");
						
						//	replace  existing arguments
						Map<String, Double> arguments = tags_values.get(I);
						for(String arg : arguments.keySet()){
							boolean f = true;
							for(HeaderExecutionResult h: results)
							{
								if(arg.equals(h.getVariable())){
									f = false;
									break;
								}
							}
							for(HeaderExecutionResult h: vheaders)
							{
								if(arg.equals("SOMME_"+h.getVariable()) || arg.equals("PRODUIT_"+h.getVariable())){
									f = false;
									break;
								}
							}
							if(f)
								formulaSource = formulaSource.replaceAll("<<"+arg+">>", arguments.get(arg)+"");
						}
						
						//	replace attributes from original table
						for(CAttribute a : dto.getEntity().getAttributes()){
							formulaSource = formulaSource.replaceAll("<<"+a.getAttribute()+">>", dto.getValues().get(a)+"");
						}
						
						//	replace header values
						int cindex=her.getValues().size();
						for(HeaderExecutionResult h : results){
							if(h.getValues().get(cindex)!=null && h.getValues().get(cindex).containsKey(new Integer(calcRowID)))
								formulaSource = formulaSource.replaceAll("<<"+h.getVariable()+">>", h.getValues().get(cindex).get(new Integer(calcRowID))+"");
						}
						
						//	replace referenced header values
						for(HeaderExecutionResult h : results){
							if(formulaSource.contains("<<#REF#"+h.getVariable()+">>")){
								int referencedCalcID;
								
								ProtogenDataEngine pde = new ProtogenDataEngine();
								referencedCalcID = pde.getForeignKeyValue(referenceTable,calcRowID,h.getReferenceAttribute().getDataReference());
								
								//	No reference
								if(h.getValues().get(cindex)==null || !h.getValues().get(cindex).containsKey(new Integer(referencedCalcID))){
									formulaSource = formulaSource.replaceAll("<<#REF#"+h.getVariable()+">>", "-999.999");
									continue;
								}
								formulaSource = formulaSource.replaceAll("<<#REF#"+h.getVariable()+">>", h.getValues().get(cindex).get(new Integer(referencedCalcID))+"");
								
							}
						}
						
						//	Replace agregated value headers
						for(HeaderExecutionResult h : vheaders){
							if(formulaSource.contains("<<SOMME_"+h.getVariable()+">>") || formulaSource.contains("<<PRODUIT_"+h.getVariable()+">>")){
								if(formulaSource.contains("<<SOMME_"+h.getVariable()+">>")){
									double somme = 0;
									for(Integer in : h.getValues().get(I).keySet()){
										somme= somme+h.getValues().get(I).get(in).doubleValue();
									}
									
									formulaSource=formulaSource.replaceAll("<<SOMME_"+h.getVariable()+">>", ""+somme);
								}
								if(formulaSource.contains("<<PRODUIT_"+h.getVariable()+">>")){
									double produit = 1;
									for(Integer in : h.getValues().get(I).keySet()){
										produit = produit*h.getValues().get(I).get(in).doubleValue();
									}
									
									formulaSource=formulaSource.replaceAll("<<PRODUIT_"+h.getVariable()+">>", ""+produit);
								}
							}
						}
						
						//	Replace injected values
						List<String> injectedValues = StringFormat.getInstance().getInjectedValues(formulaSource);
						for(String iv : injectedValues){
							String formula = iv.split("_")[1];
							String table = formula.substring(0,formula.indexOf("("));
							String key = formula.substring(formula.indexOf('(')+1,formula.indexOf(')'));
							String attrlabel = formula.split("\\.")[1];
							
							ApplicationLoader al = new ApplicationLoader();
							CBusinessClass entity = al.getClassByName(table);
							entity = engine.populateEntity(entity);
							int ID = 0;
							
							String rrtable=entity.getDataReference();
							CBusinessClass cee = dal.getEntity(rrtable);
							cee = engine.populateEntity(cee);
							cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
							List<PairKVElement> entityKeys = engine.getDataKeys(rrtable,(cee.getUserRestrict()=='Y'),cache.getUser().getId());
							
							for(PairKVElement e : entityKeys)
								if(e.getValue().equals(key)){
									ID = Integer.parseInt(e.getKey());
									break;
								}
							
							CAttribute vat = new CAttribute();
							for(CAttribute a : entity.getAttributes())
								if(a.getAttribute().equals(attrlabel)){
									vat = a;
									break;
								}
							
							double v = engine.getInjectedValue(entity,vat,ID);
							
							String surrogate = "<<"+iv+">>";
							if(formulaSource.contains(surrogate)){
								formulaSource = formulaSource.replace(surrogate, ""+v);
								System.out.println(formulaSource);
							}
						}
						
						FacesContext fc = FacesContext.getCurrentInstance();
					    ExternalContext ec = fc.getExternalContext();
						
						CalculusEngine ceng = new CalculusEngine(ec.getRealPath(""));
						double d = 0;
						
						for(Integer IK : values.keySet()){
							formulaSource = formulaSource.replaceAll("#H"+IK.intValue()+"H#", values.get(IK).doubleValue()+"");
						}
						
						
						formulaSource = formulaSource.replaceAll("#VARID#", ""+calcRowID);
						
						if(formulaSource.contains("soit")){
							try{
								d = ceng.executeSimpleScript(formulaSource);
							}catch(Exception exc){
								exc.printStackTrace();
							}
						} else {
							try{
								d = Double.parseDouble(formulaSource);
							}catch(Exception exc){
								exc.printStackTrace();
							}
						}
						values.put(new Integer(calcRowID), new Double(d));
					}
					
					
				}
				her.getValues().add(values);
				
			}
			results.add(her);
		}
		
		return results;
	}
	
	private List<HeaderExecutionResult> compileValueHeaders(
			List<Map<String, Double>> tags_values, List<String> tags,
			MAction act, Map<String, String> processParameters) {
		// TODO Auto-generated method stub
		
		List<HeaderExecutionResult> results = new ArrayList<HeaderExecutionResult>();
		
		//"calculer <<fkdanslagrille.formuletableorigine>> dans <<VARIABLE>>;"
		String headerSource=act.getCode();
		if(headerSource.indexOf('"')<0)
			return results;
		headerSource = headerSource.substring(headerSource.indexOf("\"")+1, headerSource.lastIndexOf("\""));
		
		String[] headers = headerSource.split(";");
		for(String header : headers){
			if(!header.toLowerCase().contains("valeur:")){
				continue;
			}
			header = header.replaceAll("Valeur:", "");
			String reference = header.substring(header.indexOf(" ")+1);
			String variable = header = header.substring(header.lastIndexOf(" ")+1);
			reference = reference.substring(0,reference.lastIndexOf(" "));
			reference = reference.substring(0,reference.lastIndexOf(" "));
			
			reference = reference.replaceAll("<<", "");
			reference = reference.replaceAll(">>", "");
			variable = variable.replaceAll("<<", "");
			variable = variable.replaceAll(">>", "");
			
			HeaderExecutionResult her = new HeaderExecutionResult();
			
			her.setVariable(variable);
			
			//	Detecte attribute
			String table = reference.split("\\.")[0];
			table = table.replaceAll("calculer ", "").trim();
			String valueAttribute = reference.split("\\.")[1];
			
			CBusinessClass referenceEntity = new CBusinessClass(); 
			for(CAttribute a : window.getCAttributes())
				if(a.getEntity().getName().equals(table)){
					referenceEntity = a.getEntity();
					break;
				}
			
			
			
			List<CAttribute> clauses = new ArrayList<CAttribute>();
			List<Integer> clauseIndexes = new ArrayList<Integer>();
			CAttribute referenceAttribute = new CAttribute();
			for(CAttribute a : window.getCAttributes()){
				if(a.getEntity().getDataReference().equals(referenceEntity.getDataReference())){
					if(a.isIndirectMtmKey())
						referenceAttribute = a;
					
					if(a.isIndirectMtmValue())
						continue;
					clauses.add(a);
					clauseIndexes.add(new Integer(window.getCAttributes().indexOf(a)));
				}
			}
			
			her.setReferenceAttribute(referenceAttribute);
			String referenceTable = referenceAttribute.getDataReference().substring(3);
			ProtogenDataEngine engine = new ProtogenDataEngine();
			
			String rtable=referenceTable;
			ApplicationLoader dal= new ApplicationLoader();
			CBusinessClass ce = dal.getEntity(rtable);
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			List<PairKVElement> listElements = engine.getDataKeys(rtable,(ce.getUserRestrict()=='Y'),cache.getUser().getId());

			her.setValues(new ArrayList<Map<Integer,Double>>());
			for(int I = 0; I < selectedRow.length ; I++){
				Map <Integer, Double> values = new HashMap<Integer, Double>();
				
				int calcRowID = 0;
				int dbID = selectedRow[I].getDbID();
				
				Map<CAttribute, String> keyValues=new HashMap<CAttribute, String>();
				referenceEntity=engine.populateEntity(referenceEntity);
				for(CAttribute a : referenceEntity.getAttributes()){
					if(!a.isReference())
						continue;
					
					for(PairKVElement t : this.titles){
						if(t.getValue().equals(a.getAttribute())){
							String value = selectedRow[I].getValue().get(titles.indexOf(t));
							boolean flag=false;
							
							String rrtable=a.getDataReference().substring(3);
							CBusinessClass cee = dal.getEntity(rrtable);
							cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
							List<PairKVElement> elts = engine.getDataKeys(rrtable,(cee.getUserRestrict()=='Y'),cache.getUser().getId());

							for(PairKVElement e : elts)
								if(e.getValue().trim().equals(value.trim())){
									keyValues.put(a, e.getKey());
									flag = true;
									break;
								}
							
							if(flag)
								break;
						}
					}
					
				}
				
				keyMapList.add(keyValues);
				
				for(PairKVElement k : listElements){
					
						calcRowID = Integer.parseInt(k.getKey());
						double v = engine.getByPassedValue(StringFormat.getInstance().attributeDataReferenceFormat(valueAttribute), referenceEntity.getDataReference(), referenceAttribute.getDataReference(), window.getMainEntity(), calcRowID,dbID, keyValues);
						values.put(new Integer(calcRowID), new Double(v));
						
				}
					
				
				her.getValues().add(values);
				
			}
			results.add(her);
		}
		
		return results;
	}
	
	public void selectRowManual(){
		String sid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("SELID");
		int id = Integer.parseInt(sid);
		
		for(ListKV l  : values){
			if(l.getDbID() == id){
				selectedRow = new ListKV[1];
				selectedRow[0] = l;
				break;
			}
		}
		
		rowSelected(null);
	}
	
	public void rowSelected(SelectEvent event){
		if(cmsMode)
			updateCMSPanel();
		ListKV row = selectedRow[0];
		if(this.synthView)
			updateSynthPanel(row.getDbID());
		selValue = new ArrayList<PairKVElement>();
		for(int i = 0 ; i <row.getValue().size() ; i++){
			String t = titles.get(i).getValue();
			String v = row.getValue().get(i);
			selValue.add(new PairKVElement(t, v));
		}
		
		ApplicationLoader loader = new ApplicationLoader();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		if(mailWindow && selectedRow != null && selectedRow.length>0)
			mails = loader.loadMails(selectedRow[0], user.getId());
		
		mail = new MailDTO();
		
		for(CAttribute a : validationAttributes){
			for(PairKVElement e : titles)
				if(e.getValue().equals(a.getAttribute())){
					int i = titles.indexOf(e);
					String v = row.getValue().get(i);
					if(v.equals("Oui"))
						a.setLockState(true);
					else
						a.setLockState(false);
				}
			for(PairKVElement e : titles)
				if(e.getValue().equals(a.getAttribute())){
					int i = titles.indexOf(e);
					for(int j=0;j<values.size();j++){
						if(values.get(j).getDbID()==selectedRow[0].getDbID())
							values.get(j).setLocked(values.get(j).getValue().get(i).equals("Oui"));
					}
					
				}
			
		}
		showLockToolbar=validationAttributes.size()>0;
		
		for(MtmBlock b : mtmBlocks){
			b.setLines(new ArrayList<MtmLine>());
		}
		
		for(MtmBlock b : mtmBlocks){
			ProtogenDataEngine pde = new ProtogenDataEngine();
			CWindow win = new CWindow();
			win.setMainEntity(b.getEntity().getDataReference());
			win.setCAttributes(new ArrayList<CAttribute>());
			for(UIControlElement e : b.getControls())
				win.getCAttributes().add(e.getAttribute());
			
			List<Map<CAttribute,Object>> vals = pde.getDataByConstraint(b.getEntity(), 
					"fk_"+b.getEntity().getDataReference()+"__"+window.getMainEntity()+"="+selectedRow[0].getDbID());
			if(vals.size()==0)
				continue;
			b.setLines(new ArrayList<MtmLine>());
			for(Map<CAttribute, Object> vl : vals){
				MtmLine l = new MtmLine();
				l.setValues(new ArrayList<PairKVElement>());
				for(CAttribute att : vl.keySet()){
					if(att.getDataReference().startsWith("pk_"))
						continue;
					if(att.isReference()){
						if(vl.get(att) == null){
							l.getValues().add(new PairKVElement(att.getAttribute(), ""));
						} else {
							int id = Integer.parseInt(vl.get(att)+"");
							PairKVElement el = pde.getDataKeyByID(att.getDataReference().substring(3), id);
							l.getValues().add(new PairKVElement(att.getAttribute(), el.getValue()));
						}
					}else{
						if(att.getCAttributetype().getId() == 5){
							if(vl.get(att) == null || vl.get(att).toString().length()==0){
								l.getValues().add(new PairKVElement(att.getAttribute(), "00:00"));
							} else {
								String v = vl.get(att).toString();
								String svalue;
								int ival = Integer.parseInt(v);
								if(ival>=1000)
									svalue=v.charAt(0)+""+v.charAt(1)+":"+v.charAt(2)+""+v.charAt(3);
								else if(ival>=100)
									svalue="0"+v.charAt(0)+":"+v.charAt(1)+""+v.charAt(2);
								else if(ival>=10)
									svalue="00:"+v.charAt(0)+""+v.charAt(1);
								else
									svalue="00:0"+v.charAt(0);
								l.getValues().add(new PairKVElement(att.getAttribute(), svalue));
							}
							
							
						} else if(att.getCAttributetype().getId() == 3) {
							if(vl.get(att) == null || vl.get(att).toString().length()==0 || vl.get(att).toString().equals("null")){
								l.getValues().add(new PairKVElement(att.getAttribute(), ""));
							} else {
								String date = vl.get(att).toString().split(" ")[0];
								String vd = date.split("-")[2]+"/"+date.split("-")[1]+"/"+date.split("-")[0];
								l.getValues().add(new PairKVElement(att.getAttribute(), vd));
							}
								
						} else {
							if(vl.get(att) != null && !vl.get(att).toString().equals("null"))
								l.getValues().add(new PairKVElement(att.getAttribute(), vl.get(att)+""));
							else
								l.getValues().add(new PairKVElement(att.getAttribute(), ""));
						}
					}
					
				}
				b.getLines().add(l);
			}
			Map<CAttribute,Object> r = vals.get(0);
			for(UIControlElement e : b.getControls())
				for(CAttribute att : r.keySet()){
					if(att.getId() == e.getAttribute().getId()){
						if(att.isReference()){
							if(r.get(att)!=null && r.get(att).toString().length()>0){
								int id = Integer.parseInt(r.get(att)+"");
								PairKVElement el = pde.getDataKeyByID(att.getDataReference().substring(3), id);
								e.setControlValue(el.getValue());
							} else
								e.setControlValue("");
						}else{
							e.setControlValue(r.get(att)+"");
						}
					}
				}
		}
		
		/*
		 * Stored files
		 */
		files = new ArrayList<String>();
		storedFiles = new ArrayList<StoredFile>();
		if(storable){
			FileStoreService store = new FileStoreService();
			int id = row.getDbID();
			storedFiles = store.loadStoredFiles(windowEntity, id,storedFileTypes);
		}

		
	}
	
	public String lockRow(){
		int id = selectedRow[0].getDbID();
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String k = params.get("rtlock");
		int aid = Integer.parseInt(k);		
		CAttribute attribute = new CAttribute();
		for(CAttribute a : validationAttributes){
			if(a.getId()==aid){
				attribute = a;
				
				for(PairKVElement e : titles)
					if(e.getValue().equals(a.getAttribute())){
						int i = titles.indexOf(e);
						for(int j=0;j<values.size();j++){
							if(values.get(j).getDbID()==selectedRow[0].getDbID())
								values.get(j).getValue().set(i, "Oui");
						}
						
					}
				
				break;
			}
		}
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		
		engine.lock(attribute,id);
		
		PostValidationEngine pve =new PostValidationEngine();
		pve.executePostValidation(attribute, id, selectedRow[0], titles);
		
		attribute.setLockState(true);
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Traitement effectué", "")); 
		
		return "";
	}

	public String unlockRow(){
		int id = selectedRow[0].getDbID();
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String k = params.get("rtunlock");
		int aid = Integer.parseInt(k);
		CAttribute attribute = new CAttribute();
		for(CAttribute a : validationAttributes){
			if(a.getId()==aid){
				attribute = a;
				
				for(PairKVElement e : titles)
					if(e.getValue().equals(a.getAttribute())){
						int i = titles.indexOf(e);
						for(int j=0;j<values.size();j++){
							if(values.get(j).getDbID()==selectedRow[0].getDbID())
								values.get(j).getValue().set(i, "Non");
						}
						
					}
				
				break;
			}
		}
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		
		engine.unlock(attribute,id);
		attribute.setLockState(false);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Traitement effectué", ""));
		return "";
	}
	
	/*
	 * 	WORKFLOWS
	 */
	public String launchWorkflow(){
		
		Map<String, String> parameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		
		int id = Integer.parseInt(parameters.get("wfID"));
		
		WorkflowDefinition definition = new WorkflowDefinition();
		for(WorkflowDefinition d : workflows){
			if(d.getId() == id){
				definition=d;
				break;
			}
		}
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		
		WFData datum = new WFData();
		ApplicationLoader dal = new ApplicationLoader();
		CBusinessClass e = dal.getEntity(window.getMainEntity());
		
		if(selectedRow != null && selectedRow.length>0){
			datum.setEntity(e.getName());
			datum.setSubjectId(selectedRow[0].getDbID());
			datum.setWindow(window);
		}
		
		WorkflowEngine engine=new WorkflowEngine(); 
		engine.launchWorkflow(definition, user, datum); 		
		
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Nouvel Workflow initié",""));
		return "";
	}
	
	/*
	 * 	Gérer les relations multiples
	 */
	private List<MtmBlock> populate(List<MtmDTO> dtos) {
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		LocalizationEngine translator = new LocalizationEngine();
		
		List<MtmBlock> results = new ArrayList<MtmBlock>();
		for(MtmDTO dto : dtos){
			MtmBlock block = new MtmBlock();
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
			
			//	constructing form
			block.setControls(new ArrayList<UIControlElement>());
			ProtogenDataEngine engine = new ProtogenDataEngine();
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
						e.setLabel(translator.attributeTranslate(a.getAttribute(), a.getId(), u.getLanguage()));
						e.setCtrlDate(true);
						e.setControlValue("");
						block.getControls().add(e);
					} else if (a.getCAttributetype().getId() == 12) {	//Boolean
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(translator.attributeTranslate(a.getAttribute(), a.getId(), u.getLanguage()));
						e.setBooleanValue(false);
						e.setControlValue("Non");
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
	
	private List<MtmBlock> populateOto(CWindow window) {
		ProtogenDataEngine pde = new ProtogenDataEngine();
		List<MtmBlock> results = new ArrayList<MtmBlock>();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		LocalizationEngine translator = new LocalizationEngine();		
		
		List<String> blockTables = new ArrayList<String>();
		for(CAttribute a : window.getCAttributes()){
			if(a.getEntity().getDataReference().equals(window.getMainEntity()) || a.isRappel())
				continue;
			String t = a.getEntity().getDataReference();
			boolean flag = false;
			for(String table : blockTables){
				if(t.equals(table)){
					flag = true;
					break;
				}
			}
			for(MtmBlock m : mtmBlocks){
				if(m.getEntity().getDataReference().equals(t)){
					flag = true;
					break;
				}
			}
			if(!flag)
				blockTables.add(t);
		}
		
		for(String t : blockTables){
			MtmBlock mb = new MtmBlock();
			CBusinessClass entity = pde.getReferencedTable(t);
			mb.setEntity(entity);
			mb.getEntity().setName(translator.entityTranslate(mb.getEntity().getName(), mb.getEntity().getId(), u.getLanguage()));
			mb.setControls(new ArrayList<UIControlElement>());
			mb.setEntityID(entity.getId());
			for(CAttribute a : entity.getAttributes()){
				if(a.getDataReference().equals("pk_"+t) || a.isMultiple())
					continue;
				UIControlElement c = new UIControlElement();
				c.setAttribute(a);
				if(a.getDataReference().startsWith("fk_")){
					c.setReference(true);
				}
				c.setControlValue("");
				mb.getControls().add(c);
			}
			results.add(mb);
		}
		
		return results;
	}
	
	public String exportExcel(){
		if (titles == null || titles.size() == 0)
			return "";
		
		ExcelExportEngine eee = new ExcelExportEngine();
		List<String> ts = new ArrayList<String>();
		for(PairKVElement e : titles)
			ts.add(e.getValue());
		
		
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		ec.responseReset();
		ec.setResponseContentType("application/vnd.ms-excel");
		ec.setResponseHeader("Content-Disposition",
				"attachment; filename=\"export.xls\"");

		OutputStream output;

		try {
			
			String file = eee.generateExcel(values, window.getTitle(), ts);
			
			output = ec.getResponseOutputStream();
			InputStream is = new FileInputStream(file);

			byte[] content = IOUtils.toByteArray(is);

			is.close();
			output.write(content);
			output.close();
			output.flush();
			FacesContext.getCurrentInstance().responseComplete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	/*
	 * FILE STORAGE
	 */
	public void handleFileUpload(FileUploadEvent event){
		if(selectedRow == null || selectedRow.length == 0){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Veuillez choisir un enregistrement",""));
			storedFile = new StoredFile();
			if(storedFileTypes!=null && storedFileTypes.size()>0)
				selectedFileType = storedFileTypes.get(0).getId();
			return;
		}
		try {
			InputStream is = event.getFile().getInputstream();
			
			for(StoredFileType t : storedFileTypes)
				if(t.getId() == selectedFileType){
					storedFile.setType(t);
					break;
				}
			
			int id = selectedRow[0].getDbID();
			FileStoreService service = new FileStoreService();
			boolean flag = service.uploadFile(windowEntity, id, event.getFile().getFileName(), is, storedFile);
			if(!flag){
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Erreur de téléchargement",
						"Le fichier "+event.getFile().getFileName()+" ne peut être téléchargé, veuillez vérifier que le fichier"
						+ " existe et que vous disposez des droits nécessaires pour le lire"));
				storedFile = new StoredFile();
				if(storedFileTypes!=null && storedFileTypes.size()>0)
					selectedFileType = storedFileTypes.get(0).getId();
				return;
			}
			storedFiles.add(storedFile);
			storedFile = new StoredFile();
			if(storedFileTypes!=null && storedFileTypes.size()>0)
				selectedFileType = storedFileTypes.get(0).getId();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteStoredFile(){
		String stringFileId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("FILE_ID");
		if(stringFileId == null || stringFileId.length() == 0 ){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur de suppression","Le document choisit est invalide, veuillez recharger l'écran"));
			return;
		}
		int id = Integer.parseInt(stringFileId);
		StoredFile sf = new StoredFile();
		for(StoredFile f : storedFiles)
			if(f.getId() == id){
				sf = f;
				break;
			}
		
		
		FileStoreService service = new FileStoreService();
		service.deleteStoredFile(sf, windowEntity, selectedRow[0].getDbID());
		storedFiles.remove(sf);
			
	}
	
	public String previewFile(){
		
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		
		map.put(ProtogenConstants.SELECTED_ROW, new Integer(selectedRow[0].getDbID()));
		map.put(ProtogenConstants.WINDOW_ENTITY, windowEntity);
		map.put(ProtogenConstants.SELECTED_FILE_PREVIEW, 
				FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("FILE_NAME")
				);
		
		return "pdf-preview";
	}
	
	public void mailSelectListener(){
		mail = tableMail;
	}
	
	/*
	 * 	GETTERS AND SETTERS
	 */

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

	public List<PairKVElement> getTitles() {
		return titles;
	}

	public void setTitles(List<PairKVElement> titles) {
		this.titles = titles;
	}

	public List<ListKV> getValues() {
		return values;
	}

	public void setValues(List<ListKV> values) {
		this.values = values;
	}

	public CWindow getWindow() {
		return window;
	}

	public void setWindow(CWindow window) {
		this.window = window;
	}




	public List<CActionbutton> getButtons() {
		return buttons;
	}




	public void setButtons(List<CActionbutton> buttons) {
		this.buttons = buttons;
	}


	public int getIdAction() {
		return idAction;
	}


	public void setIdAction(int idAction) {
		this.idAction = idAction;
	}

	public String getProcessMessage() {
		return processMessage;
	}

	public void setProcessMessage(String processMessage) {
		this.processMessage = processMessage;
	}

	public List<Integer> getMainIDS() {
		return mainIDS;
	}

	public void setMainIDS(List<Integer> mainIDS) {
		this.mainIDS = mainIDS;
	}

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}

	public String getMessageClass() {
		return messageClass;
	}

	public void setMessageClass(String messageClass) {
		this.messageClass = messageClass;
	}

	public List<Map<String,String>> getForeign() {
		return foreign;
	}

	public void setForeign(List<Map<String,String>> foreign) {
		this.foreign = foreign;
	}

	public boolean isInProcess() {
		return inProcess;
	}

	public void setInProcess(boolean inProcess) {
		this.inProcess = inProcess;
	}

	public boolean isRequireParameter() {
		return requireParameter;
	}

	public void setRequireParameter(boolean requireParameter) {
		this.requireParameter = requireParameter;
	}

	public ListKV[] getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(ListKV[] selectedRow) {
		this.selectedRow = selectedRow;
	}

	public List<CAttribute> getFcas() {
		return fcas;
	}

	public void setFcas(List<CAttribute> fcas) {
		this.fcas = fcas;
	}

	public CAttribute getSelectedFca() {
		return selectedFca;
	}

	public void setSelectedFca(CAttribute selectedFca) {
		this.selectedFca = selectedFca;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public List<String> getMtmTables() {
		return mtmTables;
	}

	public void setMtmTables(List<String> mtmTables) {
		this.mtmTables = mtmTables;
	}

	public List<UIFilterElement> getFiltersControls() {
		return filtersControls;
	}

	public void setFiltersControls(List<UIFilterElement> filtersControls) {
		this.filtersControls = filtersControls;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<Integer> getReferences() {
		return references;
	}

	public void setReferences(List<Integer> references) {
		this.references = references;
	}

	public int getUserFieldIndex() {
		return userFieldIndex;
	}

	public void setUserFieldIndex(int userFieldIndex) {
		this.userFieldIndex = userFieldIndex;
	}

	public List<Integer> getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(List<Integer> orderIndex) {
		this.orderIndex = orderIndex;
	}

	public Map<CActionbutton, List<Integer>> getExecutionTrace() {
		return executionTrace;
	}

	public void setExecutionTrace(Map<CActionbutton, List<Integer>> executionTrace) {
		this.executionTrace = executionTrace;
	}

	public List<CAttribute> getValidationAttributes() {
		return validationAttributes;
	}

	public List<Map<CAttribute, String>> getKeyMapList() {
		return keyMapList;
	}

	public void setKeyMapList(List<Map<CAttribute, String>> keyMapList) {
		this.keyMapList = keyMapList;
	}

	public List<Integer> getValidationIndexes() {
		return validationIndexes;
	}

	public void setValidationIndexes(List<Integer> validationIndexes) {
		this.validationIndexes = validationIndexes;
	}

	public void setValidationAttributes(List<CAttribute> validationAttributes) {
		this.validationAttributes = validationAttributes;
	}

	public boolean isShowLockToolbar() {
		return showLockToolbar;
	}

	public void setShowLockToolbar(boolean showLockToolbar) {
		this.showLockToolbar = showLockToolbar;
	}

	public String getFormview() {
		return formview;
	}

	public void setFormview(String formview) {
		this.formview = formview;
	}

	public boolean isDocAvailable() {
		return docAvailable;
	}

	public void setDocAvailable(boolean docAvailable) {
		this.docAvailable = docAvailable;
	}

	public boolean isActAvailable() {
		return actAvailable;
	}

	public void setActAvailable(boolean actAvailable) {
		this.actAvailable = actAvailable;
	}

	public boolean isBndAvailable() {
		return bndAvailable;
	}

	public void setBndAvailable(boolean bndAvailable) {
		this.bndAvailable = bndAvailable;
	}

	public List<ButtonParameter> getParsToShow() {
		return parsToShow;
	}

	public void setParsToShow(List<ButtonParameter> parsToShow) {
		this.parsToShow = parsToShow;
	}

	public int getButtonID() {
		return buttonID;
	}

	public void setButtonID(int buttonID) {
		this.buttonID = buttonID;
	}

	public List<ActionBatch> getBatches() {
		return batches;
	}

	public void setBatches(List<ActionBatch> batches) {
		this.batches = batches;
	}

	public List<SAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<SAlert> alerts) {
		this.alerts = alerts;
	}

	public List<WorkflowDefinition> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<WorkflowDefinition> workflows) {
		this.workflows = workflows;
	}

	public boolean isWfAvailable() {
		return wfAvailable;
	}

	public void setWfAvailable(boolean wfAvailable) {
		this.wfAvailable = wfAvailable;
	}

	public boolean isFiltersEnabled() {
		return filtersEnabled;
	}

	public void setFiltersEnabled(boolean filtersEnabled) {
		this.filtersEnabled = filtersEnabled;
	}

	public boolean isFcasAvailable() {
		return fcasAvailable;
	}

	public void setFcasAvailable(boolean fcasAvailable) {
		this.fcasAvailable = fcasAvailable;
	}

	public String getBatchID() {
		return batchID;
	}

	public void setBatchID(String batchID) {
		this.batchID = batchID;
	}

	public boolean isSilentMode() {
		return silentMode;
	}

	public void setSilentMode(boolean silentMode) {
		this.silentMode = silentMode;
	}

	public CParameterMetamodel getParamodel() {
		return paramodel;
	}

	public void setParamodel(CParameterMetamodel paramodel) {
		this.paramodel = paramodel;
	}

	public boolean isUpdateBtn() {
		return updateBtn;
	}

	public void setUpdateBtn(boolean updateBtn) {
		this.updateBtn = updateBtn;
	}

	public boolean isDeleteBtn() {
		return deleteBtn;
	}

	public void setDeleteBtn(boolean deleteBtn) {
		this.deleteBtn = deleteBtn;
	}

	public int getSubviewId() {
		return subviewId;
	}

	public void setSubviewId(int subviewId) {
		this.subviewId = subviewId;
	}

	public List<List<String>> getSubviewValues() {
		return subviewValues;
	}

	public void setSubviewValues(List<List<String>> subviewValues) {
		this.subviewValues = subviewValues;
	}

	public boolean isSubviewButton() {
		return subviewButton;
	}

	public void setSubviewButton(boolean subviewButton) {
		this.subviewButton = subviewButton;
	}

	public boolean isDetailsShown() {
		return detailsShown;
	}

	public void setDetailsShown(boolean detailsShown) {
		this.detailsShown = detailsShown;
	}

	public ScreenDataHistory getHistory() {
		return history;
	}

	public void setHistory(ScreenDataHistory history) {
		this.history = history;
	}

	public List<PairKVElement> getSelValue() {
		return selValue;
	}

	public void setSelValue(List<PairKVElement> selValue) {
		this.selValue = selValue;
	}

	public OCRDriverBean getDriver() {
		return driver;
	}

	public void setDriver(OCRDriverBean driver) {
		this.driver = driver;
	}

	public boolean isOcrised() {
		return ocrised;
	}

	public void setOcrised(boolean ocrised) {
		this.ocrised = ocrised;
	}

	public List<MtmBlock> getMtmBlocks() {
		return mtmBlocks;
	}

	public void setMtmBlocks(List<MtmBlock> mtmBlocks) {
		this.mtmBlocks = mtmBlocks;
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

	public String getAlphaDataReference() {
		return alphaDataReference;
	}

	public void setAlphaDataReference(String alphaDataReference) {
		this.alphaDataReference = alphaDataReference;
	}

	public List<Integer> getAutorizedAlphas() {
		return autorizedAlphas;
	}

	public void setAutorizedAlphas(List<Integer> autorizedAlphas) {
		this.autorizedAlphas = autorizedAlphas;
	}

	public int getPagesCount() {
		return pagesCount;
	}

	public void setPagesCount(int pagesCount) {
		this.pagesCount = pagesCount;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public List<Integer> getAllPages() {
		return allPages;
	}

	public void setAllPages(List<Integer> allPages) {
		this.allPages = allPages;
	}

	public List<MtmBlock> getOtoBlocks() {
		return otoBlocks;
	}

	public void setOtoBlocks(List<MtmBlock> otoBlocks) {
		this.otoBlocks = otoBlocks;
	}

	public boolean isStorable() {
		return storable;
	}

	public void setStorable(boolean storable) {
		this.storable = storable;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public CBusinessClass getWindowEntity() {
		return windowEntity;
	}

	public void setWindowEntity(CBusinessClass windowEntity) {
		this.windowEntity = windowEntity;
	}

	public List<StoredFile> getStoredFiles() {
		return storedFiles;
	}

	public void setStoredFiles(List<StoredFile> storedFiles) {
		this.storedFiles = storedFiles;
	}

	public List<StoredFileType> getStoredFileTypes() {
		return storedFileTypes;
	}

	public void setStoredFileTypes(List<StoredFileType> storedFileTypes) {
		this.storedFileTypes = storedFileTypes;
	}

	public StoredFile getStoredFile() {
		return storedFile;
	}

	public void setStoredFile(StoredFile storedFile) {
		this.storedFile = storedFile;
	}

	public int getSelectedFileType() {
		return selectedFileType;
	}

	public void setSelectedFileType(int selectedFileType) {
		this.selectedFileType = selectedFileType;
	}

	public List<String> getColStyles() {
		return colStyles;
	}

	public void setColStyles(List<String> colStyles) {
		this.colStyles = colStyles;
	}

	public String getAlphaStyle() {
		return alphaStyle;
	}

	public void setAlphaStyle(String alphaStyle) {
		this.alphaStyle = alphaStyle;
	}

	public String getFixedCols() {
		return fixedCols;
	}

	public void setFixedCols(String fixedCols) {
		this.fixedCols = fixedCols;
	}

	public String getStyleAffichage() {
		return styleAffichage;
	}

	public void setStyleAffichage(String styleAffichage) {
		this.styleAffichage = styleAffichage;
	}

	public boolean isVoidData() {
		return voidData;
	}

	public void setVoidData(boolean voidData) {
		this.voidData = voidData;
	}

	public boolean isShowDirections() {
		return showDirections;
	}

	public void setShowDirections(boolean showDirections) {
		this.showDirections = showDirections;
	}

	public boolean isSingleColumnMode() {
		return singleColumnMode;
	}

	public void setSingleColumnMode(boolean singleColumnMode) {
		this.singleColumnMode = singleColumnMode;
	}

	public boolean isMailWindow() {
		return mailWindow;
	}

	public void setMailWindow(boolean mailWindow) {
		this.mailWindow = mailWindow;
	}

	public List<MailDTO> getMails() {
		return mails;
	}

	public void setMails(List<MailDTO> mails) {
		this.mails = mails;
	}

	
	public int getSelectedMail() {
		return selectedMail;
	}

	public void setSelectedMail(int selectedMail) {
		this.selectedMail = selectedMail;
	}

	public MailDTO getMail() {
		return mail;
	}

	public void setMail(MailDTO mail) {
		this.mail = mail;
	}

	public List<MailDTO> getFilteredMails() {
		return filteredMails;
	}

	public void setFilteredMails(List<MailDTO> filteredMails) {
		this.filteredMails = filteredMails;
	}

	public MailDTO getTableMail() {
		return tableMail;
	}

	public void setTableMail(MailDTO tableMail) {
		this.tableMail = tableMail;
	}

	public TabView getTabview() {
		return tabview;
	}

	public void setTabview(TabView tabview) {
		this.tabview = tabview;
	}

	public int getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(int activeIndex) {
		this.activeIndex = activeIndex;
	}

	public boolean isAnyViews() {
		return anyViews;
	}

	public void setAnyViews(boolean anyViews) {
		this.anyViews = anyViews;
	}

	public List<CView> getViews() {
		return views;
	}

	public void setViews(List<CView> views) {
		this.views = views;
	}

	public UIFilterElement getLastFilter() {
		return lastFilter;
	}

	public void setLastFilter(UIFilterElement lastFilter) {
		this.lastFilter = lastFilter;
	}

	public String getSelectedFiltreId() {
		return selectedFiltreId;
	}

	public void setSelectedFiltreId(String selectedFiltreId) {
		this.selectedFiltreId = selectedFiltreId;
	}

	public List<CWindowCallout> getCallouts() {
		return callouts;
	}

	public void setCallouts(List<CWindowCallout> callouts) {
		this.callouts = callouts;
	}

	public LocalizationEngine getTranslator() {
		return translator;
	}

	public void setTranslator(LocalizationEngine translator) {
		this.translator = translator;
	}

	public boolean isSynthView() {
		return synthView;
	}

	public void setSynthView(boolean synthView) {
		this.synthView = synthView;
	}

	public CListViewSynthesis getSynthesisModel() {
		return synthesisModel;
	}

	public void setSynthesisModel(CListViewSynthesis synthesisModel) {
		this.synthesisModel = synthesisModel;
	}

	public String getSynthSQL() {
		return synthSQL;
	}

	public void setSynthSQL(String synthSQL) {
		this.synthSQL = synthSQL;
	}

	public SynthesisTab getSynthesisTab() {
		return synthesisTab;
	}

	public void setSynthesisTab(SynthesisTab synthesisTab) {
		this.synthesisTab = synthesisTab;
	}


	public boolean isCmsMode() {
		return cmsMode;
	}


	public void setCmsMode(boolean cmsMode) {
		this.cmsMode = cmsMode;
	}


	public CFolder getRootFolder() {
		return rootFolder;
	}


	public void setRootFolder(CFolder rootFolder) {
		this.rootFolder = rootFolder;
	}


	public RowFolder getRootRowFolder() {
		return rootRowFolder;
	}


	public void setRootRowFolder(RowFolder rootRowFolder) {
		this.rootRowFolder = rootRowFolder;
	}


	public TreeNode getRootNode() {
		return rootNode;
	}


	public void setRootNode(TreeNode rootNode) {
		this.rootNode = rootNode;
	}


	public TreeNode getSelectedNode() {
		return selectedNode;
	}


	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}


	public Map<TreeNode, RowFolder> getNodesMap() {
		return nodesFoldersMap;
	}


	public void setNodesMap(Map<TreeNode, RowFolder> nodesMap) {
		this.nodesFoldersMap = nodesMap;
	}


	public Map<TreeNode, RowDocument> getNodesDocsMap() {
		return nodesDocsMap;
	}


	public void setNodesDocsMap(Map<TreeNode, RowDocument> nodesDocsMap) {
		this.nodesDocsMap = nodesDocsMap;
	}


	public RowDocument getSelectedDocument() {
		return selectedDocument;
	}


	public void setSelectedDocument(RowDocument selectedDocument) {
		this.selectedDocument = selectedDocument;
	}


	public boolean isSeldoc() {
		return seldoc;
	}


	public void setSeldoc(boolean seldoc) {
		this.seldoc = seldoc;
	}


	public String getHtmlCMS() {
		return htmlCMS;
	}


	public void setHtmlCMS(String htmlCMS) {
		this.htmlCMS = htmlCMS;
	}


	public Map<TreeNode, RowFolder> getNodesFoldersMap() {
		return nodesFoldersMap;
	}


	public void setNodesFoldersMap(Map<TreeNode, RowFolder> nodesFoldersMap) {
		this.nodesFoldersMap = nodesFoldersMap;
	}


	public String getCmsFileName() {
		return cmsFileName;
	}


	public void setCmsFileName(String cmsFileName) {
		this.cmsFileName = cmsFileName;
	}


	public String getCmsCreationDate() {
		return cmsCreationDate;
	}


	public void setCmsCreationDate(String cmsCreationDate) {
		this.cmsCreationDate = cmsCreationDate;
	}

	

	
	
}
