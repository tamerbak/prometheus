package fr.protogen.asgard.model.masterdata;

@SuppressWarnings("serial")
public class LignesFactureAchat extends DbEntity {
	private MappedColumn quantite;
	private MappedColumn total;
	
	public MappedColumn getQuantite() {
		return quantite;
	}
	public void setQuantite(MappedColumn quantite) {
		this.quantite = quantite;
	}
	public MappedColumn getTotal() {
		return total;
	}
	public void setTotal(MappedColumn total) {
		this.total = total;
	}
}
