package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class CDataHistory implements Serializable {
	
	private int id;
	private CBusinessClass entity;
	private CAttribute reference;
	private Map<Integer, CInstanceHistory> courant = new HashMap<Integer, CInstanceHistory>();
	
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
	public CAttribute getReference() {
		return reference;
	}
	public void setReference(CAttribute reference) {
		this.reference = reference;
	}
	public Map<Integer, CInstanceHistory> getCourant() {
		return courant;
	}
	public void setCourant(Map<Integer, CInstanceHistory> courant) {
		this.courant = courant;
	}
	
	
	
}
