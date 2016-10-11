package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CBatchArgument implements Serializable {
	private int id;
	private String libelle;
	private String value;
	private String code;
	
	public CBatchArgument(int id, String libelle, String value, String code) {
		super();
		this.id = id;
		this.libelle = libelle;
		this.value = value;
		this.code = code;
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
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
