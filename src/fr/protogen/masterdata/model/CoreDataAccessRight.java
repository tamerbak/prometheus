package fr.protogen.masterdata.model;

import java.io.Serializable;

public class CoreDataAccessRight implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CBusinessClass entity;
	private int value;
	
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
}
