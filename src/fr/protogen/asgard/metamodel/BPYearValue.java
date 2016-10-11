package fr.protogen.asgard.metamodel;

import java.io.Serializable;

public class BPYearValue implements Serializable  {
	private BPYear year;
	private BPLigneTemplate ligne;
	private double value;
	
	public BPYear getYear() {
		return year;
	}
	public void setYear(BPYear year) {
		this.year = year;
	}
	public BPLigneTemplate getLigne() {
		return ligne;
	}
	public void setLigne(BPLigneTemplate ligne) {
		this.ligne = ligne;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
}
