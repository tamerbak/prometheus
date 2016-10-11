package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
public class GQLDataResult implements Serializable {
	
	private Map<String, Double> values;
	private double singleValue;
	private String dicoPrefix;
	private boolean dictionnaryMode=false;
	
	public GQLDataResult(){
		singleValue = 0;
	}
	
	public Map<String, Double> getValues() {
		return values;
	}
	public void setValues(Map<String, Double> values) {
		this.values = values;
	}
	public double getSingleValue() {
		return singleValue;
	}
	public void setSingleValue(double singleValue) {
		this.singleValue = singleValue;
	}

	public boolean isDictionnaryMode() {
		return dictionnaryMode;
	}

	public void setDictionnaryMode(boolean dictionnaryMode) {
		this.dictionnaryMode = dictionnaryMode;
	}

	public String getDicoPrefix() {
		return dicoPrefix;
	}

	public void setDicoPrefix(String dicoPrefix) {
		this.dicoPrefix = dicoPrefix;
	}
}
