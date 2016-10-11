package fr.protogen.ocr.pojo;

import java.io.Serializable;

public class Header implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String nom;
	private String format;
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Header() {
		super();
		this.nom="";
	}

	public Header(String nom) {
		super();
		this.nom = nom;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	
	
}
