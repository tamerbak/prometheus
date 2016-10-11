package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StoredFileType implements Serializable {
	private int id;
	private String libelle;
	
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
