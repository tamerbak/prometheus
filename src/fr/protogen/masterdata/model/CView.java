package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CView implements Serializable {
	private int id;
	private String title;
	private char type;
	private String mime;
	private List<CViewPart> parts = new ArrayList<CViewPart>();
	
	public CView(){
		parts = new ArrayList<CViewPart>();
	}
	
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
	public char getType() {
		return type;
	}
	public void setType(char type) {
		this.type = type;
	}
	public String getMime() {
		return mime;
	}
	public void setMime(String mime) {
		this.mime = mime;
	}
	public List<CViewPart> getParts() {
		return parts;
	}
	public void setParts(List<CViewPart> parts) {
		this.parts = parts;
	}
}
