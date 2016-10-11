package fr.protogen.masterdata.model;

import java.io.Serializable;

public class WorkflowNode implements Serializable {
	private int id;
	private String label;
	private String description;
	private NodeType type;
	private WorkflowDefinition definition;
	private CoreRole responsible;
	
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public NodeType getType() {
		return type;
	}
	public void setType(NodeType type) {
		this.type = type;
	}
	public CoreRole getResponsible() {
		return responsible;
	}
	public void setResponsible(CoreRole responsible) {
		this.responsible = responsible;
	}
	public WorkflowDefinition getDefinition() {
		return definition;
	}
	public void setDefinition(WorkflowDefinition definition) {
		this.definition = definition;
	}
}
