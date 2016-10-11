package fr.protogen.masterdata.model;

import java.util.List;

import fr.protogen.engine.control.ui.UIMenu;

public class SRubrique {
	private int id;
	private String titre;
	private String description;
	private boolean pilotage;
	private List<SMenuitem> items;
	private List<UIMenu> innermenues;
	private boolean oneColumne;
	private boolean technical;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isPilotage() {
		return pilotage;
	}
	public void setPilotage(boolean pilotage) {
		this.pilotage = pilotage;
	}
	public List<SMenuitem> getItems() {
		return items;
	}
	public void setItems(List<SMenuitem> items) {
		this.items = items;
	}
	public List<UIMenu> getInnermenues() {
		return innermenues;
	}
	public void setInnermenues(List<UIMenu> innermenues) {
		this.innermenues = innermenues;
	}
	public boolean isOneColumne() {
		return oneColumne;
	}
	public void setOneColumne(boolean oneColumne) {
		this.oneColumne = oneColumne;
	}
	public boolean isTechnical() {
		return technical;
	}
	public void setTechnical(boolean technical) {
		this.technical = technical;
	}
	
}
