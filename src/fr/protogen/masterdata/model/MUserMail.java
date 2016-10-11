package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class MUserMail implements Serializable {
	private int id;
	private String sujet;
	private String correspondant;
	private Date dateMessage;
	private boolean entrant;
	private CoreUser utilisateur;
	private String contenu;
	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSujet() {
		return sujet;
	}
	public void setSujet(String sujet) {
		this.sujet = sujet;
	}
	public String getCorrespondant() {
		return correspondant;
	}
	public void setCorrespondant(String correspondant) {
		this.correspondant = correspondant;
	}
	public Date getDateMessage() {
		return dateMessage;
	}
	public void setDateMessage(Date dateMessage) {
		this.dateMessage = dateMessage;
	}
	public boolean isEntrant() {
		return entrant;
	}
	public void setEntrant(boolean entrant) {
		this.entrant = entrant;
	}
	public CoreUser getUtilisateur() {
		return utilisateur;
	}
	public void setUtilisateur(CoreUser utilisateur) {
		this.utilisateur = utilisateur;
	}
	public String getContenu() {
		return contenu;
	}
	public void setContenu(String contenu) {
		this.contenu = contenu;
	}
}
