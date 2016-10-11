package fr.protogen.masterdata.model;

import java.io.Serializable;

public class WFAnswerData implements Serializable {
	private int nodeId;
	private String comment;
	
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
