package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ExportButton implements Serializable {
	private int id;
	private String title;
	private char mode;
	private ExportMap map;
	private CWindow window;
	private String description;
	
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
	public char getMode() {
		return mode;
	}
	public void setMode(char mode) {
		this.mode = mode;
	}
	public ExportMap getMap() {
		return map;
	}
	public void setMap(ExportMap map) {
		this.map = map;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
