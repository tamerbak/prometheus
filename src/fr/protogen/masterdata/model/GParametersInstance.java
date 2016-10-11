package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GParametersInstance implements Serializable {
	private int id;
	private GParametersPackage modelPackage;
	private int beanId;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public GParametersPackage getModelPackage() {
		return modelPackage;
	}
	public void setModelPackage(GParametersPackage modelPackage) {
		this.modelPackage = modelPackage;
	}
	public int getBeanId() {
		return beanId;
	}
	public void setBeanId(int beanId) {
		this.beanId = beanId;
	}
}
