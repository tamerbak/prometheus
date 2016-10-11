package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class GParametersPackage implements Serializable {
	private int id;
	private String nom;
	private CBusinessClass entity;
	private List<CBusinessClass> implicatedEntities;
	
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
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public List<CBusinessClass> getImplicatedEntities() {
		return implicatedEntities;
	}
	public void setImplicatedEntities(List<CBusinessClass> implicatedEntities) {
		this.implicatedEntities = implicatedEntities;
	}
	
}
