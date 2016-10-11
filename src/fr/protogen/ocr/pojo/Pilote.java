package fr.protogen.ocr.pojo;

import java.io.Serializable;

public class Pilote implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String nom;

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Pilote() {
		super();
		this.nom="";
	}

	public Pilote(String nom) {
		super();
		this.nom = nom;
	}
	
	
	
}
