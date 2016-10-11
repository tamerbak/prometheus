package fr.protogen.connector.model;

import java.util.List;

public class QWSQuery {
	private String table;
	private String type;
	private AmanToken token;
	private List<QWSField> fields;
	private List<QWSConstraint> constraints;
	
	/*
	 * GETTERS AND SETTERS
	 */
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public AmanToken getToken() {
		return token;
	}
	public void setToken(AmanToken token) {
		this.token = token;
	}
	public List<QWSField> getFields() {
		return fields;
	}
	public void setFields(List<QWSField> fields) {
		this.fields = fields;
	}
	public List<QWSConstraint> getConstraints() {
		return constraints;
	}
	public void setConstraints(List<QWSConstraint> constraints) {
		this.constraints = constraints;
	}
	
}
