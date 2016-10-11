package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StoredFile implements Serializable {
	private int id;
	private String fileName;
	private StoredFileType type=new StoredFileType();
	private String libelle;
	private String description;
	private boolean privateFile=true;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public StoredFileType getType() {
		return type;
	}
	public void setType(StoredFileType type) {
		this.type = type;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isPrivateFile() {
		return privateFile;
	}
	public void setPrivateFile(boolean privateFile) {
		this.privateFile = privateFile;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
