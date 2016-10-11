package fr.protogen.webServices.refactoredClasses;

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
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.control.CalculusEngine;
import fr.protogen.engine.control.ui.GParametersComponent;
import fr.protogen.engine.control.ui.MtmBlock;
import fr.protogen.engine.control.ui.MtmLine;
import fr.protogen.engine.control.ui.SelectedItem;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.DBFormattedObjects;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.engine.utils.UIControlsLine;
import fr.protogen.engine.utils.UISimpleValues;
import fr.protogen.masterdata.DAO.AlertDataAccess;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.OrganizationDAL;
import fr.protogen.masterdata.DAO.TriggersEngine;
import fr.protogen.masterdata.model.AlertInstance;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CGlobalValue;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.CParameterMetamodel;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GParametersInstance;
import fr.protogen.masterdata.model.GParametersPackage;
import fr.protogen.masterdata.model.SAlert;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.Trigger;
import fr.protogen.masterdata.services.MTMService;

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
	private List<MtmBlock> mtmBlocks;
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
	
	//	Methods
	@PostConstruct
	public void postLoad(){
		
		boolean notinsession=(window==null || !FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
				|| FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")==null); 

		if(notinsession){
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
			} catch (IOException e) {
			// TODO Auto-generated catch block
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
		
		windowTitle = window.getTitle();
		windowDescription  = window.getStepDescription();
		windowPercentage = window.getPercentage()+" %";
		windowHelp = window.getHelpVideo();
		
		selectedMtmBlock = new SelectedItem(0);
		
		
		
		//	Data loading
		ApplicationLoader loader = new ApplicationLoader(); 
		window = loader.loadFullWindow(window);
		window = loader.loadWindowWithActions(window);
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
		//	Call Back Problem !!!!
		if(params.containsKey("action") && params.get("action").equals("update")){
			action = "update";
			insert=false;
			
			dbID = Integer.parseInt((String)params.get("rowID")); 
			ProtogenDataEngine engine = new ProtogenDataEngine();
			initialData = engine.getDataByID(dbID, window);
			
			List<MtmDTO> dtos = service.getMtmFull(entity, dbID); 
			mtmBlocks = populate(dtos);
			
			loadComponents();
			

		} else if(params.containsKey("action") && params.get("action").equals("insert")) {
			action = "insert";
			insert=true;
			List<MtmDTO> dtos = service.getMtm(window,entity); 
			mtmBlocks = populate(dtos);
			
			loadVoidComponents();
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
		
		if(inProcess && windowDescription!= null && windowDescription.length()>0){

			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(false);
			FacesContext.getCurrentInstance().getExternalContext().getFlash().clear();
			
		}
		
		
		checkingChangeListener();
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
	
	public void uploadFile(FileUploadEvent event) {
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
   			// TODO Auto-generated catch block
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
	
	private List<MtmBlock> populate(List<MtmDTO> dtos) {
		// TODO Auto-generated method stub
		
		List<MtmBlock> results = new ArrayList<MtmBlock>();
		for(MtmDTO dto : dtos){
			MtmBlock block = new MtmBlock();
			block.setEntity(dto.getMtmEntity());
			block.setEntityID(dto.getMtmEntity().getId());
//			block.setLines(new ArrayList<MtmLine>());
			block.setTitles(new ArrayList<String>());
			
			for(CAttribute a : dto.getMtmEntity().getAttributes()){
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				if(a.isAutoValue())
					continue;
				block.getTitles().add(a.getAttribute());
			}
			
			if(action.equals("update") )
				for(Map<CAttribute, Object> dataLine : dto.getMtmData()){
					MtmLine line = new MtmLine();
					line.setValues(new ArrayList<PairKVElement>());
					for(CAttribute a : dataLine.keySet()){
						if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
							continue;
						if(a.isAutoValue())
							continue;
						String val = dataLine.get(a).toString();
						PairKVElement pkv=new PairKVElement(a.getDataReference(), val);
						
						if(a.getCAttributetype().getId() == 3){
							pkv.setDate(true);
							String dval = val.split(" ")[0];
							pkv.setFormattedDateValue(dval.split("-")[2]+"/"+dval.split("-")[1]+"/"+dval.split("-")[0]);
						}
						line.getValues().add(pkv);
					}
					block.getLines().add(line);
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
						e.setLabel(a.getAttribute());
						e.setCtrlDate(true);
						e.setControlValue("");
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
					ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
					List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),cache.getUser().getId());
					
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

	public void checkingChangeListener(){
		
		//	Condtional layout
		List<MtmDTO> dtos;
		if(action == null || action.equals("update")){
			dtos = getFullDTOS();
		}
		else{
			dtos = getDTOS();
		}
		for(UIControlElement e : controls){
//			if(!e.isReference()){
				if(e.getAttribute().isConditionalLayout()){
					double value=0;
					//	Calculus Vudu
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine engine = new CalculusEngine(ec.getRealPath(""));
					try {
						value=engine.evaluateControlLayoutFormula(controls,e,dtos, window.getAppKey());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
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
		
		for(UIControlElement e : controls){
			if(!e.isReference() || e.getAttribute()==null || e.getAttribute().getId()==0)
				continue;
			if(e.getAttribute().getDataReference().substring(3).equals(window.getMainEntity()))		//	In case of reflexive reference keep all
				continue;
			if(selectedAttribute.getId() == e.getAttribute().getId())
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
							se.setControlValue(se.getListReference().get(0).getKey());
							se.setTrueValue(se.getListReference().get(0).getValue());
						}
						if(!se.getControlValue().equals("0"))
							wheres.add(drf+"="+se.getControlValue());
						
						
						break;
					}
				}
			}
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			CBusinessClass cbc = dal.getEntity(e.getAttribute().getDataReference().substring(3));
			ProtogenDataEngine engine = new ProtogenDataEngine();
			Map<Integer, String> list = engine.getDataKeys(e.getAttribute().getDataReference().substring(3),wheres);
			List<PairKVElement> listElements = new ArrayList<PairKVElement>();
			if(!e.getAttribute().isMandatory())
				listElements.add(new PairKVElement("0", ""));
			if(list!=null && list.size()>0)
				for(Integer i : list.keySet()){
					listElements.add(new PairKVElement(i.intValue()+"", list.get(i)));
				}
			e.getListReference().clear();
			if(listElements.size()>0){
				e.setListReference(listElements);
			} else {
				e.getListReference().add(new PairKVElement("0", ""));
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
					int id = Integer.parseInt(k);
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
				roe.setControlValue(k);
			}
			
		}
		
		if(window.getRappelReference() != null && window.getRappelReference().getId()>0){
			rappelActivated = true;
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
					
					if(a.isReference()){
						int id=0;
						if(value != null && value.length()>0)
							id = Integer.parseInt(value);
						String table = a.getDataReference().substring(3);
						engine = new ProtogenDataEngine();
						ArrayList<String> wheres = new ArrayList<String>();
						wheres.add("pk_"+table+"="+id);
						Map<Integer, String> list = engine.getDataKeys(table,wheres);
						for(Integer i : list.keySet()){
							value=list.get(i);
							break;
						}
					}
					
					UIControlElement rctrl = new UIControlElement();
					rctrl.setControlID(a.getAttribute());
					rctrl.setControlValue(value);
					rappelHistory.add(rctrl);
					
				}
			}
		}
	}
	
	public void loadVoidComponents() {
		// TODO Auto-generated method stub
		controls = new ArrayList<UIControlElement>();
		controlLines = new UIControlsLine();
		references = new ArrayList<String>();
		updatedFields="";
		List<String> entities = new ArrayList<String>();
		ApplicationLoader dal = new ApplicationLoader();
		
		

		for(CAttribute attribute : window.getCAttributes()){
			
			if(!attribute.isVisible() || attribute.getDataReference().startsWith("pk_") || attribute.isMultiple() /*|| checkAttribute(attribute,mtmBlocks)*/ )
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
				
				
				ProtogenDataEngine engine = new ProtogenDataEngine();
				
				String referenceTable = attribute.getDataReference().substring(3);
				
				//	Add all search references
				addSearchReferences(referenceTable);
				
				references.add(referenceTable);
				String referencedEntity = dal.getEntityFromDR(referenceTable);
				
				CBusinessClass e = dal.getEntity(referenceTable);
		
				
				
				List<PairKVElement> list = engine.getDataKeys(referenceTable,false,0);
				
				element.setReferenceTable(referencedEntity);
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				if(!attribute.isMandatory())
					listElements.add(new PairKVElement("0", ""));
				for(PairKVElement kv : list){
					listElements.add(kv);
				}
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
				/*
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				CoreUser u = cache.getUser();
				element.setControlValue(u.getFirstName()+" - "+u.getLastName());
				element.setTrueValue(u.getId()+"");
				*/
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
			}
		} 
		
		if(updatedFields.length()>0)
			updatedFields=updatedFields.substring(0,updatedFields.length()-1);
		return;
	}

	
	
	private void addSearchReferences(String theReferenceTable) {
		// TODO Auto-generated method stub
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
			//ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			List<PairKVElement> list = engine.getDataKeys(referenceTable,false,0);
			
			element.setReferenceTable(referencedEntity);
			List<PairKVElement> listElements = new ArrayList<PairKVElement>();
			if(!a.isMandatory())
				listElements.add(new PairKVElement("0", ""));
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		references = new ArrayList<String>();
		controls = new ArrayList<UIControlElement>();
		controlLines = new UIControlsLine();
		List<String> entities = new ArrayList<String>();
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
			if(!attribute.isVisible() || (attribute.getDataReference().startsWith("fk_") && !attribute.isReference()) || attribute.getDataReference().startsWith("pk_"))
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
				if(!attribute.isMandatory())
					listElements.add(new PairKVElement("0", ""));
				for(PairKVElement kv : list){
					listElements.add(kv);
				}
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setListReference(listElements);
				element.setFiltrable(listElements.size()>10);
				element.setReference(true);
				
				//	Value
				String value = initialData.getMainEntity().get(attribute.getAttribute());
				for(PairKVElement e : listElements){
					if(e.getKey().equals(value))
						element.setTrueValue(e.getValue());
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
			}else if(type.equals("HEURE")){
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				if(attribute.isConditionalLayout())
					updatedFields = updatedFields+":protogen_main:idmask_"+attribute.getId()+",";
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					String v = initialData.getMainEntity().get(attribute.getAttribute());
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
			}
		}
		
		if(updatedFields.length()>0)
			updatedFields=updatedFields.substring(0,updatedFields.length()-1);
	}
	
	private Date getDate(String sdate) {
		// TODO Auto-generated method stub
		String days = sdate.split(" ")[0];
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return formatter.parse(days);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Calendar.getInstance().getTime();
	}





	//	Active behavior

	public String doSave(){
		
		List<MtmDTO> dtos;
		if(action == null || action.equals("update")){
			dtos = getFullDTOS();
		}
		else{
			dtos = getDTOS();
		}
		
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
						// TODO Auto-generated catch block
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
						// TODO Auto-generated catch block
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
		
		//	MAJ COntenu binaire
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
		
		if(action == null || action.equals("update")){
			flag= engine.executeSaveAction(window, controls, dbID, foreignKeys, dtos);
			engine.updateGlobalValues(globalControls);
			
		}
		else{
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			CoreUser u = cache.getUser(); 
			flag= engine.executeInsertAction(window, controls,dtos,u);
			
			dbID=engine.getLastRowId(window.getMainEntity());
			
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
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO,"Enregistrement sauvegardé avec succès",""));
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.FORM_CONTROLS, controls);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.FORM_WINDOW, windowTitle);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.FORM_DTOS, mtmBlocks);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.FORM_AUTOVALUES, engine.getAutovalues());
			if(inlineForm){
				if(insert){
					UISimpleValues v = new UISimpleValues();
					for(UIControlElement c : controls){
						String va =c.getControlValue();
						if(c.isReference() || c.getAttribute().getCAttributetype().getId()==9)
							va=c.getTrueValue();
						if(c.isCtrlDate()){
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
					saved=true;
				}
				return "";
			}else
				return "protogen-synthform";
			
		} 
		
		return "";
	}

	private List<MtmDTO> getFullDTOS() {
		// TODO Auto-generated method stub
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
					for(CAttribute a : block.getEntity().getAttributes())
						if(a.getDataReference().equals(dataref)){
							if(a.isAutoValue())
								continue;
							if(!a.isReference())
								data.put(a, e.getValue());
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
				
				dto.getMtmData().add(data);
			}
			results.add(dto);
		}
		
		
		return results;
	}
	
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
							if(!a.isReference())
								data.put(a, e.getValue());
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
		checkingChangeListener();
		return;
	}
	public void saveTextStatus(){
		checkingChangeListener();
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
	public String doDelete(){
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int lineId = Integer.parseInt(params.get("todel"));
		int blockId = Integer.parseInt(params.get("todelBlock"));
		MtmLine toRem = null;
		for(MtmBlock block : mtmBlocks){
			if(block.getEntityID() == blockId){
				for(MtmLine line : block.getLines()){
					if(line.getId() == lineId){
						toRem = line;
						break;
					}
						
				}
				block.getLines().remove(toRem);
				break;
			}
		}
		
		
		return "";
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
						// TODO Auto-generated catch block
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
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					if(value==0){
						FacesContext context = FacesContext.getCurrentInstance();  
				          
				        context.addMessage(null, new FacesMessage("Erreur", "La valeur de "+e.getControlID()+" est incorrecte"));  
				        return;
					}
				}
				PairKVElement pkv = new PairKVElement(e.getControlID(), e.getControlValue());
				
				if(e.getAttribute().getCAttributetype().getId()==3){
					pkv.setDate(true);
					String dval = e.getControlValue();
					Date d = e.getDateValue();
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					dval = c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+"/"+c.get(Calendar.YEAR);
					pkv.setFormattedDateValue(dval);
				
				}
				line.getValues().add(pkv);
			}
			else {
				line.getValues().add(new PairKVElement(e.getControlID(), e.getTrueValue()));
			}
		}
		
		block.getLines().add(line);
		
	
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
		String inlineAttId = params.get("inlineAttId");
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationLoader dal = new ApplicationLoader();
		toCreateEntity = dal.getEntity(datareference.substring(3));
		List<CAttribute> entityAttributes = toCreateEntity.getAttributes();				
		
		
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
				if(!attribute.isMandatory() || list==null || list.size()==0)
					listElements.add(new PairKVElement("0", ""));
				for(PairKVElement kv : list){
					listElements.add(kv);
				}
				element.setControlValue(listElements.get(0).getKey());
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
			}
		} 
		inlineReferenceChange();
	}
	
	public void inlineReferenceChange(){
		String dummy="";
		dummy=dummy+"";
	}
	
	public void saveInline(){
		ProtogenDataEngine engine = new ProtogenDataEngine();
		int id = engine.insertNewReference(toCreateEntity,inlineCreation);
		checkingChangeListener();
		
		if(id>0){
			for(UIControlElement e : controls){
				if(!e.isReference() || e.getAttribute().getId()==0)
					continue;
				
				if(e.getAttribute().getDataReference().equals("fk_"+toCreateEntity.getDataReference())){
					for(PairKVElement pkv : e.getListReference())
						if(pkv.getKey().equals(""+id)){
							e.setTrueValue(pkv.getValue());
							e.setControlValue(""+id);
						}
				}
			}
		}
		
		checkingChangeListener();
		for(UIControlElement c : inlineCreation){
			if(c.isReference())
				c.setControlValue(c.getListReference().get(0).getKey());
		}
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


	
}
