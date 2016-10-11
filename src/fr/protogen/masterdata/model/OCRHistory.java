package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

public class OCRHistory implements Serializable {
	private int id;
	private int beanId;
	private String bean;
	private Date ocrDate;
	private CBusinessClass entity;
	private OCRDriverBean driver;
	private CoreUser user;
	private String fileKey;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBeanId() {
		return beanId;
	}
	public void setBeanId(int beanId) {
		this.beanId = beanId;
	}
	public String getBean() {
		return bean;
	}
	public void setBean(String bean) {
		this.bean = bean;
	}
	public Date getOcrDate() {
		return ocrDate;
	}
	public void setOcrDate(Date ocrDate) {
		this.ocrDate = ocrDate;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public OCRDriverBean getDriver() {
		return driver;
	}
	public void setDriver(OCRDriverBean driver) {
		this.driver = driver;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
	public String getFileKey() {
		return fileKey;
	}
	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}
}
