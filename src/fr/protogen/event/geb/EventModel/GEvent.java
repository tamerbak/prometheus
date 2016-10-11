package fr.protogen.event.geb.EventModel;

import java.io.Serializable;
import java.util.Date;

import fr.protogen.masterdata.model.CoreUser;

@SuppressWarnings("serial")
public class GEvent implements Serializable {
	private int id;
	private String title;
	private EventType type;
	private String contenu;
	private CoreUser destinataire;
	private int beanId;
	private boolean autoEvent=false;
	
	private boolean differe;
	private Date dateLancement;
	private int periode;
	private int nbRelances;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}
	public String getContenu() {
		return contenu;
	}
	public void setContenu(String contenu) {
		this.contenu = contenu;
	}
	public CoreUser getDestinataire() {
		return destinataire;
	}
	public void setDestinataire(CoreUser destinataire) {
		this.destinataire = destinataire;
	}
	public int getBeanId() {
		return beanId;
	}
	public void setBeanId(int beanId) {
		this.beanId = beanId;
	}
	public boolean isAutoEvent() {
		return autoEvent;
	}
	public void setAutoEvent(boolean autoEvent) {
		this.autoEvent = autoEvent;
	}
	public boolean isDiffere() {
		return differe;
	}
	public void setDiffere(boolean differe) {
		this.differe = differe;
	}
	public Date getDateLancement() {
		return dateLancement;
	}
	public void setDateLancement(Date dateLancement) {
		this.dateLancement = dateLancement;
	}
	public int getPeriode() {
		return periode;
	}
	public void setPeriode(int periode) {
		this.periode = periode;
	}
	public int getNbRelances() {
		return nbRelances;
	}
	public void setNbRelances(int nbRelances) {
		this.nbRelances = nbRelances;
	}
}
