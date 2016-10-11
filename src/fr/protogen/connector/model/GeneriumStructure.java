package fr.protogen.connector.model;

import java.io.Serializable;
import java.util.List;

public class GeneriumStructure implements Serializable {

	private String table;
	private List<String> dataKeys;
	private String operation;
	
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public List<String> getDataKeys() {
		return dataKeys;
	}
	public void setDataKeys(List<String> dataKeys) {
		this.dataKeys = dataKeys;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
}
