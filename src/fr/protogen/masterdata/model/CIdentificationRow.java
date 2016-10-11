package fr.protogen.masterdata.model;

public class CIdentificationRow {
	private int id;
	private CBusinessClass reference;
	private CBusinessClass source;
	
	/*
	 * GETTERS AND SETTERS
	 */
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public CBusinessClass getReference() {
		return reference;
	}
	public void setReference(CBusinessClass reference) {
		this.reference = reference;
	}
	public CBusinessClass getSource() {
		return source;
	}
	public void setSource(CBusinessClass source) {
		this.source = source;
	}
}
