package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class CScheduleEvent implements Serializable {
	private int dbID;
	private String label;
	private Date dateEvent;
	private Date endEvent;
	private CBusinessClass entity;
	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getDbID() {
		return dbID;
	}
	public void setDbID(int dbID) {
		this.dbID = dbID;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Date getDateEvent() {
		return dateEvent;
	}
	public void setDateEvent(Date dateEvent) {
		this.dateEvent = dateEvent;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public Date getEndEvent() {
		return endEvent;
	}
	public void setEndEvent(Date endEvent) {
		this.endEvent = endEvent;
	}
}
