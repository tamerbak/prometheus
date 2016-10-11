package fr.protogen.connector.model;

public class QWSField {
	private boolean loadReferenceData=false;
	private String field;
	
	/*
	 * GETTERS AND SETTERS
	 */
	public boolean isLoadReferenceData() {
		return loadReferenceData;
	}
	public void setLoadReferenceData(boolean loadReferenceData) {
		this.loadReferenceData = loadReferenceData;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	
}
