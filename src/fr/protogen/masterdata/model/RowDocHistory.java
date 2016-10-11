package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class RowDocHistory implements Serializable{
	private int id;
	private int version;
	private Date creation = new Date();
	private String storageIdentifier;
	private CCallout callout = new CCallout();
	private CoreUser user = new CoreUser();
	private RowDocument document = new RowDocument();
	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public Date getCreation() {
		return creation;
	}
	public void setCreation(Date creation) {
		this.creation = creation;
	}
	public String getStorageIdentifier() {
		return storageIdentifier;
	}
	public void setStorageIdentifier(String storageIdentifier) {
		this.storageIdentifier = storageIdentifier;
	}
	public CCallout getCallout() {
		return callout;
	}
	public void setCallout(CCallout callout) {
		this.callout = callout;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
	public RowDocument getDocument() {
		return document;
	}
	public void setDocument(RowDocument document) {
		this.document = document;
	}
}
