package fr.protogen.masterdata.model;

import java.io.InputStream;
import java.io.Serializable;

public class SResource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1927104041576227897L;

	private int id;
	private String title;
	private String description;
	private String fileName;
	private InputStream content;
	
	
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
	public InputStream getContent() {
		return content;
	}
	public void setContent(InputStream content) {
		this.content = content;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
}
