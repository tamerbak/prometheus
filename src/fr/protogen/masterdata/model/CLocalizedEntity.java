package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CLocalizedEntity implements Serializable {
	private int id;
	private CBusinessClass entity;
	private GOrganization organization;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public GOrganization getOrganization() {
		return organization;
	}
	public void setOrganization(GOrganization organization) {
		this.organization = organization;
	}
}
