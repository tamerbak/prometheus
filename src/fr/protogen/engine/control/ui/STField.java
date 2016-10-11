package fr.protogen.engine.control.ui;

import java.io.Serializable;

import fr.protogen.masterdata.model.CLVSField;

@SuppressWarnings("serial")
public class STField implements Serializable {
	private CLVSField field;
	private String value;
	
	public CLVSField getField() {
		return field;
	}
	public void setField(CLVSField field) {
		this.field = field;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
