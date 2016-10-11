package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MapEntry implements Serializable {
	private CAttribute attribute;
	private String key;
	private String defaultValue;
	
	public CAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}
