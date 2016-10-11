package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GOrganizationStructure implements Serializable {
	private int id;
	private String nom;
	private GStructureTemplate template;
	private GOrganization root;
	
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
	public GStructureTemplate getTemplate() {
		return template;
	}
	public void setTemplate(GStructureTemplate template) {
		this.template = template;
	}
	public GOrganization getRoot() {
		return root;
	}
	public void setRoot(GOrganization root) {
		this.root = root;
	}
}
