package fr.protogen.asgard.model.masterdata;

@SuppressWarnings("serial")
public class TVA extends DbEntity {
	private MappedColumn taux;

	public MappedColumn getTaux() {
		return taux;
	}

	public void setTaux(MappedColumn taux) {
		this.taux = taux;
	}
}
