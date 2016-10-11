package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GStructureTemplate implements Serializable {
	private int id;
	private String nom;
	private GStructureElement root;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public GStructureElement getRoot() {
		return root;
	}
	public void setRoot(GStructureElement root) {
		this.root = root;
	}
	
}
