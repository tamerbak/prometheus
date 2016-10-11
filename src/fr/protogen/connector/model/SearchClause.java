package fr.protogen.connector.model;

import java.io.Serializable;

public class SearchClause implements Serializable {
	private String field;
	private String clause;
	private String gt;
	private String lt;
	private String type;
	
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getClause() {
		return clause;
	}
	public void setClause(String clause) {
		this.clause = clause;
	}
	public String getGt() {
		return gt;
	}
	public void setGt(String gt) {
		this.gt = gt;
	}
	public String getLt() {
		return lt;
	}
	public void setLt(String lt) {
		this.lt = lt;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
