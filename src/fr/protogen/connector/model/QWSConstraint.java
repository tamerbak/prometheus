package fr.protogen.connector.model;

import java.util.ArrayList;
import java.util.List;

public class QWSConstraint {
	private String field;
	private String constraint;
	private String value;
	private List<QWSConstraint> and = new ArrayList<QWSConstraint>();
	private List<QWSConstraint> or = new ArrayList<QWSConstraint>();
	
	public String sqlfy(){
		return "";
	}
	
	public void andConstraint(QWSConstraint c){
		and.add(c);
	}
	
	public void orConstraint(QWSConstraint c){
		or.add(c);
	}

	/*
	 * GETTERS AND SETTERS
	 */
	public String getField() {
		return field;
	}

	public String getConstraint() {
		return constraint;
	}

	public String getValue() {
		return value;
	}

	public List<QWSConstraint> getAnd() {
		return and;
	}

	public List<QWSConstraint> getOr() {
		return or;
	}

	public void setField(String field) {
		this.field = field;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setAnd(List<QWSConstraint> and) {
		this.and = and;
	}

	public void setOr(List<QWSConstraint> or) {
		this.or = or;
	}
}
