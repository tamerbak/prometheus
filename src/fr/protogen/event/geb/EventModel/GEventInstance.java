package fr.protogen.event.geb.EventModel;

import java.util.Date;

public class GEventInstance {
	private int id;
	private GEvent event;
	private String content;
	private Date creation;
	private Date consultation;
	private boolean state;
	private int rowId;
	private Date nextExecution;
	private int relancesRestantes;
	
	/*	
	 * GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public GEvent getEvent() {
		return event;
	}
	public void setEvent(GEvent event) {
		this.event = event;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getCreation() {
		return creation;
	}
	public void setCreation(Date creation) {
		this.creation = creation;
	}
	public Date getConsultation() {
		return consultation;
	}
	public void setConsultation(Date consultation) {
		this.consultation = consultation;
	}
	public boolean isState() {
		return state;
	}
	public void setState(boolean state) {
		this.state = state;
	}
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public Date getNextExecution() {
		return nextExecution;
	}
	public void setNextExecution(Date nextExecution) {
		this.nextExecution = nextExecution;
	}
	public int getRelancesRestantes() {
		return relancesRestantes;
	}
	public void setRelancesRestantes(int relancesRestantes) {
		this.relancesRestantes = relancesRestantes;
	}
	
	
}
