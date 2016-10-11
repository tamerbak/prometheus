package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CDocumentType implements Serializable {
	private int id;
	private String label;
	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
