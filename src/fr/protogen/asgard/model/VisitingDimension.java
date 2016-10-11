package fr.protogen.asgard.model;

import java.io.Serializable;
import java.util.List;

import fr.protogen.masterdata.model.CAttribute;

@SuppressWarnings("serial")
public class VisitingDimension implements Serializable {
	private CAttribute attribute;
	private boolean endpoint;
	private VisitingDimension parent;
	private List<VisitingDimension> children;
	
	public CAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
	public boolean isEndpoint() {
		return endpoint;
	}
	public void setEndpoint(boolean endpoint) {
		this.endpoint = endpoint;
	}
	public VisitingDimension getParent() {
		return parent;
	}
	public void setParent(VisitingDimension parent) {
		this.parent = parent;
	}
	public List<VisitingDimension> getChildren() {
		return children;
	}
	public void setChildren(List<VisitingDimension> children) {
		this.children = children;
	}
}
