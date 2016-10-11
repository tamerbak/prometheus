package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.model.CFolder;
import fr.protogen.masterdata.model.RowDocument;

@SuppressWarnings("serial")
public class RowFolder implements Serializable {
	private CFolder folder;
	private List<RowFolder> subFolders = new ArrayList<RowFolder>();
	private List<RowDocument> documents = new ArrayList<RowDocument>();
	
	public RowFolder(CFolder folder){
		this.folder = folder;
		
		for(CFolder f : folder.getSubFolders()){
			RowFolder sf = new RowFolder(f);
			this.subFolders.add(sf);
		}
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public CFolder getFolder() {
		return folder;
	}
	public void setFolder(CFolder folder) {
		this.folder = folder;
	}
	public List<RowFolder> getSubFolders() {
		return subFolders;
	}
	public void setSubFolders(List<RowFolder> subFolders) {
		this.subFolders = subFolders;
	}
	public List<RowDocument> getDocuments() {
		return documents;
	}
	public void setDocuments(List<RowDocument> documents) {
		this.documents = documents;
	}
}
