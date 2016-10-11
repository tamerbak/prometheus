package fr.protogen.export.model.xml;

import java.io.Serializable;
import java.util.List;

public class XDocument implements Serializable {

	private List<XElement> elements;
	private String rootTag;
	private String enoding;
	private String xmlns;
	
	
	public List<XElement> getElements() {
		return elements;
	}
	public void setElements(List<XElement> elements) {
		this.elements = elements;
	}
	public String getRootTag() {
		return rootTag;
	}
	public void setRootTag(String rootTag) {
		this.rootTag = rootTag;
	}
	public String getEnoding() {
		return enoding;
	}
	public void setEnoding(String enoding) {
		this.enoding = enoding;
	}
	public String getXmlns() {
		return xmlns;
	}
	public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}
	
	
}
