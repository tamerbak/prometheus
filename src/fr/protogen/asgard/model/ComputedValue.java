package fr.protogen.asgard.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ComputedValue implements Serializable {
	private VisitingDimension dimension;
	private AgregationFunction function;
	private String code;
	
	public VisitingDimension getDimension() {
		return dimension;
	}
	public void setDimension(VisitingDimension dimension) {
		this.dimension = dimension;
	}
	public AgregationFunction getFunction() {
		return function;
	}
	public void setFunction(AgregationFunction function) {
		this.function = function;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
