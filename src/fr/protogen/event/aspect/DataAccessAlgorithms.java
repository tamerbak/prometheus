package fr.protogen.event.aspect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import fr.protogen.dataload.DataDefinitionOperation;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.event.geb.EventDataAccess;
import fr.protogen.event.geb.GeneriumEventBus;
import fr.protogen.event.geb.EventModel.GDataEvent;
import fr.protogen.event.geb.EventModel.GEventInstance;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CWindow;

public class DataAccessAlgorithms {

	public void notifyTableDependant(CWindow window, CBusinessClass entity, int dbID, 
			ProtogenDataEngine engine, DataDefinitionOperation operation){
		//	Construire la liste des données
		Map<CAttribute, Object> values;
		if(operation == DataDefinitionOperation.DELETE)
			values = engine.getDataToDelete();
		else
			 values = engine.getDataByConstraint(entity, "pk_"+entity.getDataReference()+"="+dbID).get(0);
				
		//	Récupérer tous les événements
		EventDataAccess eda = new EventDataAccess();
		List<GDataEvent> evts = eda.loadTableDataEvents(entity, operation);
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		GeneriumEventBus bus = (GeneriumEventBus)map.get(ProtogenConstants.EVENT_BUS);
		
		
		//	Formuler les instances
		for(GDataEvent e : evts){
			GEventInstance i = new GEventInstance();
			i.setRowId(dbID);
			i.setEvent(e);
			i.setCreation(new Date());
			
			String contenu = e.getContenu();
			for(CAttribute a : values.keySet()){
				if(values.get(a) == null)
					continue;
				String v = values.get(a).toString();
				v.replaceAll(a.getAttribute(), v);
			}
			
			i.setContent(contenu);
			i.setState(false);
			bus.notifyDataEvent(i);
		}
		
	}

	public void notifyRowDependant(CWindow window, CBusinessClass entity,
			int dbID, ProtogenDataEngine engine,
			DataDefinitionOperation operation) {
		//	Construire la liste des données
		Map<CAttribute, Object> values;
		if(operation == DataDefinitionOperation.DELETE)
			values = engine.getDataToDelete();
		else
			 values = engine.getDataByConstraint(entity, "pk_"+entity.getDataReference()+"="+dbID).get(0);
		
		//	Récupérer tous les événements
		EventDataAccess eda = new EventDataAccess();
		List<GDataEvent> rowEvts = eda.loadRowDataEvents(operation);
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		GeneriumEventBus bus = (GeneriumEventBus)map.get(ProtogenConstants.EVENT_BUS);
		
		//	Enlever les événements qui ne référencent pas un champs de la table Entity
		List<GDataEvent> evts = new ArrayList<GDataEvent>();
		for(GDataEvent e : rowEvts){
			CBusinessClass cl = e.getEntity();
			cl = engine.getEntityById(cl.getId());
			e.setEntity(cl);
			if(cl.getDataReference().equals(window.getMainEntity())){
				evts.add(e);
				continue;
			}
			for(CAttribute a : values.keySet())
				if(a.getDataReference().equals("fk_"+cl.getDataReference())){
					evts.add(e);
					break;
				}
		}
				
		//	Pour chaque événement
		for(GDataEvent e : evts){
			//	Sélectionner l'entité de l'événement
			CBusinessClass eventEntity = e.getEntity();
			
			//	Extraire son ID
			int id = 0;
			if(eventEntity.getDataReference().equals(window.getMainEntity())){
				id = e.getBeanId();
			}
			for(CAttribute a : values.keySet()){
				if(a.getDataReference().equals("fk_"+eventEntity.getDataReference())){
					id = Integer.parseInt(values.get(a).toString());
					break;
				}
			}
			
			//	Si le ID est différent de celui de l'événement alors passer
			if(id!=e.getBeanId())
				continue;
			
			
			//	Formuler une instance
			GEventInstance i = new GEventInstance();
			i.setEvent(e);
			i.setCreation(new Date());
			i.setRowId(dbID);
			String contenu = e.getContenu();
			for(CAttribute a : values.keySet()){
				String v="";
				if(values.get(a)!=null)
					v = values.get(a).toString();
				
				v.replaceAll(a.getAttribute(), v);
			}
			
			i.setContent(contenu);
			i.setState(false);
			bus.notifyDataEvent(i);
		}
		//	Fin
	}
}
