package fr.protogen.event.geb.EventModel;

import java.util.Date;

import fr.protogen.event.geb.PEAType;

public class PostEventAction {
	private int id;
	private GEvent event;
	private PEAType type;
	
	
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
	public PEAType getType() {
		return type;
	}
	public void setType(PEAType type) {
		this.type = type;
	}
	
}
