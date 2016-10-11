package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class RowDocument implements Serializable {
	private int id;
	private String name;
	private String extension;
	private Date creation = new Date();
	private int version;
	private String signature;
	private String textContent;
	private int rowId;
	private String storageIdentifier;
	private CCallout storeCallout = new CCallout();
	private CDocumentType type = new CDocumentType();
	private CWindow window = new CWindow();
	private CFolder folder = new CFolder();
	private CoreUser user = new CoreUser();
	private List<RowDocComment> comments = new ArrayList<RowDocComment>();
	private List<RowDocHistory> olderVersions = new ArrayList<RowDocHistory>();
	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public Date getCreation() {
		return creation;
	}
	public void setCreation(Date creation) {
		this.creation = creation;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getTextContent() {
		return textContent;
	}
	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public CCallout getStoreCallout() {
		return storeCallout;
	}
	public void setStoreCallout(CCallout storeCallout) {
		this.storeCallout = storeCallout;
	}
	public CDocumentType getType() {
		return type;
	}
	public void setType(CDocumentType type) {
		this.type = type;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public CFolder getFolder() {
		return folder;
	}
	public void setFolder(CFolder folder) {
		this.folder = folder;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
	public List<RowDocComment> getComments() {
		return comments;
	}
	public void setComments(List<RowDocComment> comments) {
		this.comments = comments;
	}
	public List<RowDocHistory> getOlderVersions() {
		return olderVersions;
	}
	public void setOlderVersions(List<RowDocHistory> olderVersions) {
		this.olderVersions = olderVersions;
	}
	public String getStorageIdentifier() {
		return storageIdentifier;
	}
	public void setStorageIdentifier(String storageIdentifier) {
		this.storageIdentifier = storageIdentifier;
	}
	
}
