package fr.protogen.masterdata.model;

import java.io.Serializable;

public class WorkflowTransition implements Serializable {
	private int id;
	private WorkflowNode from;
	private WorkflowNode to;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public WorkflowNode getFrom() {
		return from;
	}
	public void setFrom(WorkflowNode from) {
		this.from = from;
	}
	public WorkflowNode getTo() {
		return to;
	}
	public void setTo(WorkflowNode to) {
		this.to = to;
	}
}
