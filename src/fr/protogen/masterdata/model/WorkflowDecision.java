package fr.protogen.masterdata.model;

import java.io.Serializable;


public class WorkflowDecision extends WorkflowNode implements Serializable {
	private String yesLabel;
	private String noLabel;
	private WorkflowNode yesNode;
	private WorkflowNode noNode;
	
	public String getYesLabel() {
		return yesLabel;
	}
	public void setYesLabel(String yesLabel) {
		this.yesLabel = yesLabel;
	}
	public String getNoLabel() {
		return noLabel;
	}
	public void setNoLabel(String noLabel) {
		this.noLabel = noLabel;
	}
	public WorkflowNode getYesNode() {
		return yesNode;
	}
	public void setYesNode(WorkflowNode yesNode) {
		this.yesNode = yesNode;
	}
	public WorkflowNode getNoNode() {
		return noNode;
	}
	public void setNoNode(WorkflowNode noNode) {
		this.noNode = noNode;
	}
}
