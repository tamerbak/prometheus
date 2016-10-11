package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CBatch implements Serializable {
	private int id;
	private String nom;
	private String code;
	private List<CBatchUnit> units = new ArrayList<CBatchUnit>();
	private List<CBatchArgument> arguments = new ArrayList<CBatchArgument>();
	
	public CBatch(int id, String nom, String code, List<CBatchUnit> units, List<CBatchArgument> arguments) {
		super();
		this.id = id;
		this.nom = nom;
		this.code = code;
		this.units = units;
		this.arguments = arguments;
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
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public List<CBatchUnit> getUnits() {
		return units;
	}
	public void setUnits(List<CBatchUnit> units) {
		this.units = units;
	}
	public List<CBatchArgument> getArguments() {
		return arguments;
	}
	public void setArguments(List<CBatchArgument> arguments) {
		this.arguments = arguments;
	}
}
