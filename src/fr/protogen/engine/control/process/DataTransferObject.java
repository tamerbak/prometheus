package fr.protogen.engine.control.process;

import java.util.List;

import fr.protogen.masterdata.model.CBusinessClass;

public class DataTransferObject {
	
	private List<CBusinessClass> entities;
	
	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	
}
