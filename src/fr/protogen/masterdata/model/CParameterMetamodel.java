package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CParameterMetamodel implements Serializable {
	private int id;
	private String label;
	private String description;
	private COrganization organization;
	
	private List<CBusinessClass> mappedEntities = new ArrayList<CBusinessClass>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public COrganization getOrganization() {
		return organization;
	}
	public void setOrganization(COrganization organization) {
		this.organization = organization;
	}
	public List<CBusinessClass> getMappedEntities() {
		return mappedEntities;
	}
	public void setMappedEntities(List<CBusinessClass> mappedEntities) {
		this.mappedEntities = mappedEntities;
	}
}
