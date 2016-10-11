package fr.protogen.connector.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataEntry {
	private String label;
	private String attributeReference;
	private String type;
	private List<DataCouple> list;
	private String value;
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<DataCouple> getList() {
		return list;
	}
	public void setList(List<DataCouple> list) {
		this.list = list;
	}
	public String getAttributeReference() {
		return attributeReference;
	}
	public void setAttributeReference(String attributeReference) {
		this.attributeReference = attributeReference;
	}
}
