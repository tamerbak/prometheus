package fr.protogen.asgard.model.masterdata;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AsgardMappingModel implements Serializable {
	private Produit produit;
	private TVA tva;
	private LignesFactureAchat lignesFactureAchat;
	
	public Produit getProduit() {
		return produit;
	}
	public void setProduit(Produit produit) {
		this.produit = produit;
	}
	public TVA getTva() {
		return tva;
	}
	public void setTva(TVA tva) {
		this.tva = tva;
	}
	public LignesFactureAchat getLignesFactureAchat() {
		return lignesFactureAchat;
	}
	public void setLignesFactureAchat(LignesFactureAchat lignesFactureAchat) {
		this.lignesFactureAchat = lignesFactureAchat;
	}
}
