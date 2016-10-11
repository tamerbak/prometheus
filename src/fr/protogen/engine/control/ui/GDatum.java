package fr.protogen.engine.control.ui;

import java.io.Serializable;

import fr.protogen.masterdata.model.CAttribute;

@SuppressWarnings("serial")
public class GDatum implements Serializable {
	private CAttribute attribute;
	private String value;
	private String valueLabel;
	
	public CAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValueLabel() {
		return valueLabel;
	}
	public void setValueLabel(String valueLabel) {
		this.valueLabel = valueLabel;
	}
}
