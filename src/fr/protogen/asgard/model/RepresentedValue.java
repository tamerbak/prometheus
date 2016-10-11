package fr.protogen.asgard.model;

import java.io.Serializable;

import fr.protogen.masterdata.model.CAttribute;

@SuppressWarnings("serial")
public class RepresentedValue implements Serializable {
	private CAttribute attribute;
	private AgregationFunction preformatFunction;
	private String preformatCode;
	
	public CAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
	public AgregationFunction getPreformatFunction() {
		return preformatFunction;
	}
	public void setPreformatFunction(AgregationFunction preformatFunction) {
		this.preformatFunction = preformatFunction;
	}
	public String getPreformatCode() {
		return preformatCode;
	}
	public void setPreformatCode(String preformatCode) {
		this.preformatCode = preformatCode;
	}
}
