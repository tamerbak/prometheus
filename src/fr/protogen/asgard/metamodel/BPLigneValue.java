package fr.protogen.asgard.metamodel;

import java.io.Serializable;

public class BPLigneValue implements Serializable {
	private BPYear year;
	private double value;
	public BPLigneValue(BPYear y, double v) {
		this.year=y;
		this.value=v;
	}
	public BPYear getYear() {
		return year;
	}
	public void setYear(BPYear year) {
		this.year = year;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
}
