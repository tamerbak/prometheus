package fr.protogen.event.geb.EventModel;

import fr.protogen.dataload.DataDefinitionOperation;
import fr.protogen.masterdata.model.CBusinessClass;

@SuppressWarnings("serial")
public class GDataEvent extends GEvent {
	private CBusinessClass entity;
	private EventType type;
	private DataDefinitionOperation operation;
	
	public GDataEvent(){}
	
	public GDataEvent(GEvent e, CBusinessClass entity) {
		setBeanId(e.getBeanId());
		setContenu(e.getContenu());
		setTitle(e.getTitle());
		setType(e.getType());
		setDestinataire(e.getDestinataire());
		setDateLancement(e.getDateLancement());
		setDiffere(e.isDiffere());
		setNbRelances(e.getNbRelances());
		setPeriode(e.getPeriode());
		
		this.entity = entity;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}
	public DataDefinitionOperation getOperation() {
		return operation;
	}
	public void setOperation(DataDefinitionOperation operation) {
		this.operation = operation;
	}
}
