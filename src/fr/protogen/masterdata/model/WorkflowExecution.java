package fr.protogen.masterdata.model;

import java.io.Serializable;

public class WorkflowExecution implements Serializable {
	private int id;
	private WorkflowDefinition definition;
	private CoreUser user;
	private WorkflowNode currentNode;
	private String parameters;
	private WFData dataParameters;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public WorkflowDefinition getDefinition() {
		return definition;
	}
	public void setDefinition(WorkflowDefinition definition) {
		this.definition = definition;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
	public WorkflowNode getCurrentNode() {
		return currentNode;
	}
	public void setCurrentNode(WorkflowNode currentNode) {
		this.currentNode = currentNode;
	}
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	public WFData getDataParameters() {
		return dataParameters;
	}
	public void setDataParameters(WFData dataParameters) {
		this.dataParameters = dataParameters;
	}
}
