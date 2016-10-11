package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

public class SStep implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9188739303932625185L;
	private int id;
	private String title;
	private String description;
	private List<SAtom> actions;
	
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
	public List<SAtom> getActions() {
		return actions;
	}
	public void setActions(List<SAtom> actions) {
		this.actions = actions;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
