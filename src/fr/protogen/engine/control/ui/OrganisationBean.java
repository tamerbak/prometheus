package fr.protogen.engine.control.ui;

import java.io.Serializable;

import fr.protogen.masterdata.model.GOrganization;

@SuppressWarnings("serial")
public class OrganisationBean implements Serializable {
	private GOrganization objet;
	private String nom;
	private String type;
	
	public OrganisationBean(GOrganization objet){
		this.objet = objet;
		nom = objet.getName();
		type = "Organisation";
		if(objet.getRole() != null)
			type = objet.getRole().getLibelle();
	}

	/*
	 * GETTERS AND SETTERS
	 */
	public GOrganization getObjet() {
		return objet;
	}

	public void setObjet(GOrganization objet) {
		this.objet = objet;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
