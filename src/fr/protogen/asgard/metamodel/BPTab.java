package fr.protogen.asgard.metamodel;

import java.io.Serializable;
import java.util.List;

public class BPTab implements Serializable {
	private int id;
	private String title;
	private List<String> titles;
	private List<BPLigneTemplate> lignes;
	private List<BPLigneTemplate> parentLignes;
	private List<Double> total;
	private String footer;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<BPLigneTemplate> getLignes() {
		return lignes;
	}
	public void setLignes(List<BPLigneTemplate> lignes) {
		this.lignes = lignes;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<String> getTitles() {
		return titles;
	}
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	public List<BPLigneTemplate> getParentLignes() {
		return parentLignes;
	}
	public void setParentLignes(List<BPLigneTemplate> parentLignes) {
		this.parentLignes = parentLignes;
	}
	public List<Double> getTotal() {
		return total;
	}
	public void setTotal(List<Double> total) {
		this.total = total;
	}
	public String getFooter() {
		return footer;
	}
	public void setFooter(String footer) {
		this.footer = footer;
	}
}
