package fr.protogen.masterdata.model;

import java.io.Serializable;

public class CoreDataConstraint implements Serializable {

	private CBusinessClass entity;
	private int beanId;
	private int roleId;

	public CBusinessClass getEntity() {
		return entity;
	}

	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}

	public int getBeanId() {
		return beanId;
	}

	public void setBeanId(int beanId) {
		this.beanId = beanId;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

}
