package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

public class WorkflowDefinition implements Serializable {
	private int id;
	private String title;
	private String description;
	private List<WorkflowNode> nodes;
	private List<WorkflowTransition> transitions;
	
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<WorkflowNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<WorkflowNode> nodes) {
		this.nodes = nodes;
	}
	public List<WorkflowTransition> getTransitions() {
		return transitions;
	}
	public void setTransitions(List<WorkflowTransition> transitions) {
		this.transitions = transitions;
	}
}
