package fr.protogen.export.model.xml;

import java.io.Serializable;
import java.util.List;

public class XNode extends XElement implements Serializable {
	private List<XElement> innerElements;

	public List<XElement> getInnerElements() {
		return innerElements;
	}

	public void setInnerElements(List<XElement> innerElements) {
		this.innerElements = innerElements;
	}
}
