package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class MailCache implements Serializable {
	private int id;
	private Date derniereMAJ;
	private int taille;
	private List<MUserMail> contenu = new ArrayList<MUserMail>();
	private CoreUser utilisateur;
	
	/*
	 * GETTERS AND SETTERS 
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getDerniereMAJ() {
		return derniereMAJ;
	}
	public void setDerniereMAJ(Date derniereMAJ) {
		this.derniereMAJ = derniereMAJ;
	}
	public int getTaille() {
		return taille;
	}
	public void setTaille(int taille) {
		this.taille = taille;
	}
	public List<MUserMail> getContenu() {
		return contenu;
	}
	public void setContenu(List<MUserMail> contenu) {
		this.contenu = contenu;
	}
	public CoreUser getUtilisateur() {
		return utilisateur;
	}
	public void setUtilisateur(CoreUser utilisateur) {
		this.utilisateur = utilisateur;
	}
}
