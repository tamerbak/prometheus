package fr.protogen.masterdata.model;

import java.io.Serializable;

public class WFDecisionData implements Serializable {
	private int decisionId;
	private boolean confirmed;
	private String commentaire;
	
	public int getDecisionId() {
		return decisionId;
	}
	public void setDecisionId(int decisionId) {
		this.decisionId = decisionId;
	}
	public boolean isConfirmed() {
		return confirmed;
	}
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}
	public String getCommentaire() {
		return commentaire;
	}
	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}
}
