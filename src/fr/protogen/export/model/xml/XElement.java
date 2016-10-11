package fr.protogen.export.model.xml;

import java.io.Serializable;
import java.util.List;

public class XElement implements Serializable {
	private String textContent;
	private List<String> attributes;
	public String getTextContent() {
		return textContent;
	}
	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}
	public List<String> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	
}
