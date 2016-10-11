package fr.protogen.engine.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.masterdata.DAO.ScheduleDAO;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.ScheduleEntry;

@javax.faces.bean.ManagedBean
@RequestScoped
public class ScheduleControl {

	
	private List<ScheduleEntry> userSchedule = new ArrayList<ScheduleEntry>();
	private ScheduleEntry handledEntry = new ScheduleEntry();
	private ScheduleModel eventModel;
	private boolean insertMode=false;
	
	public ScheduleControl(){
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
	}
	
	@PostConstruct
	public void initialize(){
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		
		eventModel = new DefaultScheduleModel(); 
		
		ScheduleDAO dao = new ScheduleDAO();
		userSchedule = dao.loadSchedule(user);
		
		for(ScheduleEntry e : userSchedule){
			eventModel.addEvent(new DefaultScheduleEvent(e.getTitle(), e.getStartAt(), e.getEndAt(), e));
		}
		
	}

	public void onEventSelect(SelectEvent selectEvent) {  
	   // event = (ScheduleEvent) selectEvent.getObject();  
	}  
	  
	public void onDateSelect(SelectEvent selectEvent) {  
	    //event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());  
		insertMode = true;
	}  
	  
	public void onEventMove(ScheduleEntryMoveEvent event) {  
	    //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());  
	      
	}  
	  
	public void onEventResize(ScheduleEntryResizeEvent event) {  
	   // FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());  
	      
	}
	
	public void validateEntry(){
		if(insertMode){
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			CoreUser user = cache.getUser();
			handledEntry.setUser(user);
			ScheduleDAO dao = new ScheduleDAO();
			dao.insertEntry(user, handledEntry);
		}
	}
	
	/*
	 * 	GETTERS AND SETTERS
	 */
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
	
}
