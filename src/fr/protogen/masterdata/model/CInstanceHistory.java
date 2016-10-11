package fr.protogen.masterdata.model;

import java.util.Date;

public class CInstanceHistory {
	private int id;
	private int bean;
	private Date dateDebut;
	private Date dateFin;
	private boolean courant;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBean() {
		return bean;
	}
	public void setBean(int bean) {
		this.bean = bean;
	}
	public Date getDateDebut() {
		return dateDebut;
	}
	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}
	public Date getDateFin() {
		return dateFin;
	}
	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}
	public boolean isCourant() {
		return courant;
	}
	public void setCourant(boolean courant) {
		this.courant = courant;
	}
}
