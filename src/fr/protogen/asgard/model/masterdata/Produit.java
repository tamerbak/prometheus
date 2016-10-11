package fr.protogen.asgard.model.masterdata;


@SuppressWarnings("serial")
public class Produit extends DbEntity {
	private MappedColumn reference;
	private MappedColumn prixAchat;
	
	public MappedColumn getReference() {
		return reference;
	}
	public void setReference(MappedColumn reference) {
		this.reference = reference;
	}
	public MappedColumn getPrixAchat() {
		return prixAchat;
	}
	public void setPrixAchat(MappedColumn prixAchat) {
		this.prixAchat = prixAchat;
	}
}
