package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CFolder implements Serializable {
	private int id;
	private String name;
	private String description;
	private boolean root;
	private List<CFolder> subFolders = new ArrayList<CFolder>();
	
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
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
	public List<CFolder> getSubFolders() {
		return subFolders;
	}
	public void setSubFolders(List<CFolder> subFolders) {
		this.subFolders = subFolders;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
