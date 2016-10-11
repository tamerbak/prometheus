package fr.protogen.engine.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.protogen.masterdata.model.CAttribute;

public class HeaderExecutionResult {

	private String variable;
	private List<Map<Integer,Double>> values;
	private CAttribute referenceAttribute;
	
	public List<Double> somme(){
		List<Double> r = new ArrayList<Double>();
		for(Map<Integer,Double> m : values){
			double d=0;
			for(Double dd : m.values())
				d = d+dd.doubleValue();
			r.add(new Double(d));
		}
		return r;
	}
	
	public List<Double> produit(){
		List<Double> r = new ArrayList<Double>();
		for(Map<Integer,Double> m : values){
			double d=1;
			for(Double dd : m.values())
				d = d*dd.doubleValue();
			r.add(new Double(d));
		}
		return r;
	}
	
	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	public List<Map<Integer, Double>> getValues() {
		return values;
	}
	public void setValues(List<Map<Integer, Double>> values) {
		this.values = values;
	}

	public CAttribute getReferenceAttribute() {
		return referenceAttribute;
	}

	public void setReferenceAttribute(CAttribute referenceAttribute) {
		this.referenceAttribute = referenceAttribute;
	}
	
	

}
