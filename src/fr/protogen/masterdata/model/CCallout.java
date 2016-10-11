package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CCallout implements Serializable {

	private int id;
	private String nom;
	private byte[] file;
	private List<CCalloutArguments> args = new ArrayList<CCalloutArguments>();
	private String jsonArguments;
	private static final long serialVersionUID = 1L;
	
	/*
	 * GETTERS AND SETTERS
	 */
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
	public byte[] getFile() {
		return file;
	}
	public void setFile(byte[] file) {
		this.file = file;
	}
	public List<CCalloutArguments> getArgs() {
		return args;
	}
	public void setArgs(List<CCalloutArguments> args) {
		this.args = args;
	}
	public String getJsonArguments() {
		return jsonArguments;
	}
	public void setJsonArguments(String jsonArguments) {
		this.jsonArguments = jsonArguments;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
