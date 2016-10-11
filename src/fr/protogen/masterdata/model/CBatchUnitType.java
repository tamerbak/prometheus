package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CBatchUnitType implements Serializable {
	private int id;
	private String libelle;
	
	public CBatchUnitType(){
		
	}
	public CBatchUnitType(int id, String libelle) {
		super();
		this.id = id;
		this.libelle = libelle;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
}
