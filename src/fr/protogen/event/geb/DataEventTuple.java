package fr.protogen.event.geb;

import fr.protogen.dataload.DataDefinitionOperation;
import fr.protogen.event.geb.EventModel.GEvent;
import fr.protogen.masterdata.model.CBusinessClass;

public class DataEventTuple {
	private CBusinessClass entity;
	private DataDefinitionOperation operation;
	private GEvent eventDefinition;
	
	
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public DataDefinitionOperation getOperation() {
		return operation;
	}
	public void setOperation(DataDefinitionOperation operation) {
		this.operation = operation;
	}
	public GEvent getEventDefinition() {
		return eventDefinition;
	}
	public void setEventDefinition(GEvent eventDefinition) {
		this.eventDefinition = eventDefinition;
	}
}
