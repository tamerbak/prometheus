package fr.protogen.masterdata.model;

import java.io.Serializable;

public class WorkflowAnswer extends WorkflowNode implements Serializable {
	private WorkflowDecision decisionNode;

	public WorkflowDecision getDecisionNode() {
		return decisionNode;
	}

	public void setDecisionNode(WorkflowDecision decisionNode) {
		this.decisionNode = decisionNode;
	}
}
