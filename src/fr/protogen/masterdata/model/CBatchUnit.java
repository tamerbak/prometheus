package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CBatchUnit implements Serializable {
	private int id;
	private String nom;
	private String instructionsModel;
	private int order;
	private CBatchUnitType type;
	private CBatch batch;
	
	public CBatchUnit(int id, String nom, String instructionsModel, CBatchUnitType type) {
		super();
		this.id = id;
		this.nom = nom;
		this.instructionsModel = instructionsModel;
		this.type = type;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getInstructionsModel() {
		return instructionsModel;
	}
	public void setInstructionsModel(String instructionsModel) {
		this.instructionsModel = instructionsModel;
	}
	public CBatchUnitType getType() {
		return type;
	}
	public void setType(CBatchUnitType type) {
		this.type = type;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public CBatch getBatch() {
		return batch;
	}

	public void setBatch(CBatch batch) {
		this.batch = batch;
	}
}
