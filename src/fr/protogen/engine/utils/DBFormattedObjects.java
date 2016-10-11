package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DBFormattedObjects implements Serializable {
	private Map<String, String> mainEntity;
	private Map<String, Map<String,String>> otmEntities;
	
	public DBFormattedObjects(){
		mainEntity = new HashMap<String, String>();
		otmEntities = new HashMap<String, Map<String,String>>();
	}
	
	public Map<String, String> getMainEntity() {
		return mainEntity;
	}
	public void setMainEntity(Map<String, String> mainEntity) {
		this.mainEntity = mainEntity;
	}
	public Map<String, Map<String, String>> getOtmEntities() {
		return otmEntities;
	}
	public void setOtmEntities(Map<String, Map<String, String>> otmEntities) {
		this.otmEntities = otmEntities;
	}
	
}
