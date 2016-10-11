package fr.protogen.asgard.metamodel;

import java.io.Serializable;
import java.util.List;

public class BPVariable implements Serializable{
	private int id;
	private String title;
	private String formula;
	private List<Double> values;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}
	public List<Double> getValues() {
		return values;
	}
	public void setValues(List<Double> values) {
		this.values = values;
	}
	
}
