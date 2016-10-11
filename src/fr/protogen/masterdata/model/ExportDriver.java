package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ExportDriver implements Serializable {
	private int id;
	private String title;
	private String key;
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
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
