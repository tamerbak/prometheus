package fr.protogen.asgard.metamodel;

import java.io.Serializable;
import java.util.List;

public class BPYear implements Serializable  {
	private int year;
	private boolean future;
	private List<BPYearValue> values;
	
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public boolean isFuture() {
		return future;
	}
	public void setFuture(boolean future) {
		this.future = future;
	}
	public List<BPYearValue> getValues() {
		return values;
	}
	public void setValues(List<BPYearValue> values) {
		this.values = values;
	}
}
