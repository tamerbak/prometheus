package fr.protogen.asgard.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class ResultTable implements Serializable {
	private List<ComputedValue> computedValues;
	private RepresentedValue representedValue;
	private List<VisitingDimension> dimensions;
	
	private String query; 
	
	
	public List<ComputedValue> getComputedValues() {
		return computedValues;
	}
	public void setComputedValues(List<ComputedValue> computedValues) {
		this.computedValues = computedValues;
	}
	public RepresentedValue getRepresentedValue() {
		return representedValue;
	}
	public void setRepresentedValue(RepresentedValue representedValue) {
		this.representedValue = representedValue;
	}
	public List<VisitingDimension> getDimensions() {
		return dimensions;
	}
	public void setDimensions(List<VisitingDimension> dimensions) {
		this.dimensions = dimensions;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	
}
