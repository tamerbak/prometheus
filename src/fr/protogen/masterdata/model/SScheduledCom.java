package fr.protogen.masterdata.model;

import java.io.Serializable;

public class SScheduledCom implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4474988864765265522L;
	private int id;
	private String title;
	private String description;
	private SResource attachement;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public SResource getAttachement() {
		return attachement;
	}
	public void setAttachement(SResource attachement) {
		this.attachement = new SResource();
		this.attachement.setDescription(attachement.getDescription());
		this.attachement.setFileName(attachement.getFileName());
		this.attachement.setId(attachement.getId());
		this.attachement.setTitle(attachement.getTitle());
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
