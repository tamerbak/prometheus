package fr.protogen.engine.utils;

import java.util.LinkedHashMap;

import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class EntityDTO {
	private CBusinessClass entity;
	private LinkedHashMap<CAttribute, String> values;
	
	
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public LinkedHashMap<CAttribute, String> getValues() {
		return values;
	}
	public void setValues(LinkedHashMap<CAttribute, String> values) {
		this.values = values;
	}
	
}
