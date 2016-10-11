package fr.protogen.event.geb;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.protogen.dataload.DataDefinitionOperation;
import fr.protogen.event.geb.EventModel.GDataEvent;
import fr.protogen.event.geb.EventModel.GEvent;
import fr.protogen.event.geb.EventModel.GEventInstance;
import fr.protogen.event.geb.EventModel.PEAMail;
import fr.protogen.event.geb.EventModel.PEASms;
import fr.protogen.event.geb.EventModel.PEAWindow;
import fr.protogen.event.geb.EventModel.PostEventAction;
import fr.protogen.event.geb.communication.ClientMail;
import fr.protogen.event.geb.communication.SmsClient;
import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreUser;

public class GeneriumEventBus {
	
	private Map<CoreUser,List<GEvent>> events;
		
	private static GeneriumEventBus instance = null;
	public synchronized static GeneriumEventBus getInstance(){
		if(instance == null)
			instance = new GeneriumEventBus();
		
		return instance;
	}
	private GeneriumEventBus(){
		events = new HashMap<CoreUser, List<GEvent>>();
	}
	
	public List<GEventInstance> energize(CoreUser utilisateur){
		EventDataAccess eda = new EventDataAccess();
		List<GEvent> evts = eda.loadDataEvents(utilisateur);
		events.put(utilisateur, evts);
		
		return fetch(utilisateur);
	}
	
	public void notifyDataEvent(GDataEvent evt){
		EventDataAccess eda = new EventDataAccess();
		GEventInstance i = new GEventInstance();
		i.setEvent(evt);
		i.setContent(evt.getContenu());
		i.setCreation(new Date());
		i.setState(false);
		eda.persist(i);
	}
	
	public void notifyDataEvent(GEventInstance i) {
		
		
		EventDataAccess eda = new EventDataAccess();
		i =eda.persist(i);
		
		
		if(!i.getEvent().isAutoEvent())
			return;
		PostEventAction action = markEvent(i);
		
		if(action instanceof PEAWindow){
			//CWindow w = ((PEAWindow)action).getWindow();
			//return loadEventWindow(w,((PEAWindow)action),i);
			eda.unmark(i);
		}
		
		String tel = eda.getDestinataireTel(i);
		String email = eda.getDestinataireEmail(i);
		
		
		if(action instanceof PEASms){
			SmsClient.getInstance().sendSMS(tel, (PEASms)action, i);
		}
		if(action instanceof PEAMail){
			ClientMail.getInstance().sendMail(email, (PEAMail)action, i);
		}
	}
	
	public PostEventAction markEvent(GEventInstance evt){
		PostEventAction res = new PostEventAction();
		
		EventDataAccess eda = new EventDataAccess();
		res = eda.mark(evt);
		
		return res;
	}
	
	public List<GEventInstance> fetch(CoreUser utilisateur){
		List<GEventInstance> results = new ArrayList<GEventInstance>();
		
		if(!events.containsKey(utilisateur))
			return results;
		EventDataAccess eda = new EventDataAccess();
		for(GEvent e : events.get(utilisateur)){
			results.addAll(eda.fetch(e));
		}
		
		return results;
	}
	
	public List<GEvent> loadDataEvent(CBusinessClass entity, int dbID, DataDefinitionOperation operation){
		List<GEvent> lev = new ArrayList<GEvent>();
		for(CoreUser u : events.keySet())
			for(GEvent e : events.get(u)){
				if(!(e instanceof GDataEvent))
					continue;
				
				GDataEvent dev = (GDataEvent)e;
				if(dev.getBeanId() == dbID && dev.getEntity().getId() == entity.getId()
						&& dev.getOperation() == operation)
					lev.add(dev);
			}
		
		return lev;
	}
	
	
}
