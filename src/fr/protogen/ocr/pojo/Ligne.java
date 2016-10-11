package fr.protogen.ocr.pojo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ligne implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private List<Colonne> colonnes;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Colonne> getColonnes() {
		return colonnes;
	}
	public void setColonnes(List<Colonne> colonnes) {
		this.colonnes = colonnes;
	}
	public Ligne() {
		super();
		this.colonnes = new ArrayList<Colonne>();
	}
	public Ligne(String id, List<Colonne> colonnes) {
		super();
		this.id = id;
		this.colonnes = colonnes;
	}
	
	
}
