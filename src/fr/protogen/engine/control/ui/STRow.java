package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class STRow implements Serializable {
	private int id;
	private List<STField> fields;
	private List<String> formattedValue;
	
	public List<STField> getFields() {
		return fields;
	}
	public void setFields(List<STField> fields) {
		this.fields = fields;
	}
	public List<String> getFormattedValue() {
		return formattedValue;
	}
	public void setFormattedValue(List<String> formattedValue) {
		this.formattedValue = formattedValue;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
