package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SAsgardDrive implements Serializable {
	private int id;
	private String model;
	private String query;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
}
