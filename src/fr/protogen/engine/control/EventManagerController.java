package fr.protogen.engine.control;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import fr.protogen.dataload.DataDefinitionOperation;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.event.geb.EventDataAccess;
import fr.protogen.event.geb.PEAType;
import fr.protogen.event.geb.EventModel.EventType;
import fr.protogen.event.geb.EventModel.GDataEvent;
import fr.protogen.event.geb.EventModel.GEvent;
import fr.protogen.event.geb.EventModel.PEAMail;
import fr.protogen.event.geb.EventModel.PEASms;
import fr.protogen.event.geb.EventModel.PEAWindow;
import fr.protogen.event.geb.EventModel.PostEventAction;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CWindow;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class EventManagerController implements Serializable {
	private GEvent evenement = new GDataEvent();
	private int selectedEventType;
	private int selectedDataOperation;
	private List<CBusinessClass> entities = new ArrayList<CBusinessClass>();;
	private int selectedEntity;
	private List<PairKVElement> rows = new ArrayList<PairKVElement>();
	private String selectedRow;
	
	/*
	 * POST EVENT
	 */
	private int selectedPEASType=1;
	private int selectedWindow;
	private List<CWindow> windows = new ArrayList<CWindow>();
	private boolean details;
	private String message;
	private String mailSubject;
	private String mailBody;
	private int selectedDestEntity;
	private List<CBusinessClass> destEntities = new ArrayList<CBusinessClass>();
	private String selectedDestinataire;
	private List<PairKVElement> destRows = new ArrayList<PairKVElement>();
	
	public EventManagerController(){
		//	Populate business entities
		ApplicationLoader dal = new ApplicationLoader();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		entities = dal.loadAllEntities(cache.getAppKey());
		windows = dal.loadWindows(cache.getAppKey());
	}
	
	@PostConstruct
	public void energize(){
		boolean notinsession=(!FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
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
	}
	
	public void updateRows(){
		if (selectedEntity == 0)
			return;
		CBusinessClass entity = null;
		for(CBusinessClass e : entities)
			if(e.getId() == selectedEntity){
				entity = e;
				break;
			}
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		List<PairKVElement> elts = pde.getDataKeys(entity.getDataReference(), false, 0);
		if(elts!=null)
			rows = elts;
		
		//	Select all dependent tables
		destEntities = new ArrayList<CBusinessClass>();
		destEntities.add(entity);
		for(CAttribute a : entity.getAttributes()){
			if(a.getDataReference().startsWith(("fk_")) && a.isReference()){
				CBusinessClass dep = pde.getReferencedTable(a.getDataReference().substring(3));
				destEntities.add(dep);
			}
		}
		
	}
	
	public void saveEvent(){
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				
		EventType t = typeLoad(selectedEventType);
		DataDefinitionOperation o = loadOperation(selectedDataOperation); 
		CBusinessClass entity = null;
		for(CBusinessClass e : entities)
			if(e.getId() == selectedEntity){
				entity = e;
				break;
			}
		int beanId=0;
		for(PairKVElement e : rows)
			if(selectedRow.equals(e.getKey())){
				beanId = Integer.parseInt(e.getKey());
				break;
			}
		
		evenement.setBeanId(beanId);
		evenement.setDestinataire(cache.getUser());
		evenement.setType(t);
		GDataEvent gde = new GDataEvent(evenement, entity);
		gde.setEntity(entity);
		gde.setOperation(o);
		EventDataAccess eda = new EventDataAccess();
		evenement = eda.saveEvent(gde);
		
		/*
		 * SAVE POST EVENT ACTION
		 */
		PEAType type = PEAType.SCREEN ;
		if(selectedPEASType == 1)
			type = PEAType.SCREEN;
		if(selectedPEASType == 2)
			type = PEAType.SMS;
		if(selectedPEASType == 3)
			type = PEAType.MAIL;
		
		PostEventAction pea = new PostEventAction();
		pea.setType(type);
		if(type == PEAType.SCREEN){
			PEAWindow a = new PEAWindow();
			a.setType(type);
			CWindow win = null;
			for(CWindow w : windows){
				if(w.getId() == selectedWindow){
					win = w;
					break;
				}
			}
			a.setEvent(evenement);
			a.setModeDetails(details);
			a.setWindow(win);
			eda.persistScreenPEA(a);
		}
		if(selectedPEASType == 2){
			PEASms sms = new PEASms();
			sms.setType(type);
			sms.setEvent(evenement);
			sms.setSubject("");
			sms.setText(message);
			eda.persistSMSPEA(sms);
		}
		if(selectedPEASType == 3){
			PEAMail m = new PEAMail();
			m.setType(type);
			m.setEvent(evenement);
			m.setSubject(mailSubject);
			m.setMessage(mailBody);
			eda.persistMailPEA(m);
		}
		
		/*
		 * SAVE DESTINAT
		 */
		CBusinessClass chosen = new CBusinessClass();
		for(CBusinessClass e : destEntities)
			if(e.getId() == selectedDestEntity){
				chosen = e;
				break;
			}
		eda.persistDestinat(chosen, evenement);
	}
	
	private DataDefinitionOperation loadOperation(int o) {
		switch(o){
		case 1 : return DataDefinitionOperation.INSERT;
		case 2 : return DataDefinitionOperation.UPDATE;
		default : return DataDefinitionOperation.DELETE;
		}
		
	}

	private EventType typeLoad(int t) {
		switch(t){
			case 1: return EventType.DATA_ACCESS;
			case 2: return EventType.CALENDAR;
			case 3: return EventType.EXTERN;
			default: return EventType.WEB_SERVICE;
		}
	}

	/*
	 * GETTERS AND SETTERS
	 */
	public GEvent getEvenement() {
		return evenement;
	}

	public void setEvenement(GEvent evenement) {
		this.evenement = evenement;
	}

	public int getSelectedEventType() {
		return selectedEventType;
	}

	public void setSelectedEventType(int selectedEventType) {
		this.selectedEventType = selectedEventType;
	}

	public int getSelectedDataOperation() {
		return selectedDataOperation;
	}

	public void setSelectedDataOperation(int selectedDataOperation) {
		this.selectedDataOperation = selectedDataOperation;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public int getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(int selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public List<PairKVElement> getRows() {
		return rows;
	}

	public void setRows(List<PairKVElement> rows) {
		this.rows = rows;
	}

	public String getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(String selectedRow) {
		this.selectedRow = selectedRow;
	}

	public int getSelectedPEASType() {
		return selectedPEASType;
	}

	public void setSelectedPEASType(int selectedPEASType) {
		this.selectedPEASType = selectedPEASType;
	}

	public int getSelectedWindow() {
		return selectedWindow;
	}

	public void setSelectedWindow(int selectedWindow) {
		this.selectedWindow = selectedWindow;
	}

	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}

	public boolean isDetails() {
		return details;
	}

	public void setDetails(boolean details) {
		this.details = details;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public String getMailBody() {
		return mailBody;
	}

	public void setMailBody(String mailBody) {
		this.mailBody = mailBody;
	}

	public int getSelectedDestEntity() {
		return selectedDestEntity;
	}

	public void setSelectedDestEntity(int selectedDestEntity) {
		this.selectedDestEntity = selectedDestEntity;
	}

	public List<CBusinessClass> getDestEntities() {
		return destEntities;
	}

	public void setDestEntities(List<CBusinessClass> destEntities) {
		this.destEntities = destEntities;
	}

	public String getSelectedDestinataire() {
		return selectedDestinataire;
	}

	public void setSelectedDestinataire(String selectedDestinataire) {
		this.selectedDestinataire = selectedDestinataire;
	}

	public List<PairKVElement> getDestRows() {
		return destRows;
	}

	public void setDestRows(List<PairKVElement> destRows) {
		this.destRows = destRows;
	}
}
