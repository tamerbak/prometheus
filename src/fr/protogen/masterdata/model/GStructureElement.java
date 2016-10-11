package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class GStructureElement implements Serializable {
	private int id;
	private String nom;
	private GOrganizationRole role;
	private GStructureElement parent;
	private List<GStructureElement> children;
	
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
	public GOrganizationRole getRole() {
		return role;
	}
	public void setRole(GOrganizationRole role) {
		this.role = role;
	}
	public GStructureElement getParent() {
		return parent;
	}
	public void setParent(GStructureElement parent) {
		this.parent = parent;
	}
	public List<GStructureElement> getChildren() {
		return children;
	}
	public void setChildren(List<GStructureElement> children) {
		this.children = children;
	}
}
