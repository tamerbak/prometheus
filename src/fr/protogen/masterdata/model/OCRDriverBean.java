package fr.protogen.masterdata.model;

import java.io.Serializable;

import fr.protogen.ocr.pojo.Document;

@SuppressWarnings("serial")
public class OCRDriverBean implements Serializable {
	private int id;
	private String label;
	private String stringContent;
	private Document content;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getStringContent() {
		return stringContent;
	}
	public void setStringContent(String stringContent) {
		this.stringContent = stringContent;
	}
	public Document getContent() {
		return content;
	}
	public void setContent(Document content) {
		this.content = content;
	}
}
