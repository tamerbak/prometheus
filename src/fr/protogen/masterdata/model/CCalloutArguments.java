package fr.protogen.masterdata.model;

import java.io.Serializable;

public class CCalloutArguments implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String libelle;
	private String value;
	
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
