package fr.protogen.connector.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RowIdentification implements Serializable {
	private String table;
	private int id;
	
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
