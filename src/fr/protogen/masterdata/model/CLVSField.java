package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CLVSField implements Serializable {
	private String libelle;
	private CAttribute attribute;
	
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public CAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
}
