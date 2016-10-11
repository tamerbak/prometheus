package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class CSchedulableEntity implements Serializable {
	private int id;
	private CBusinessClass entity;
	private CAttribute fromAttribute;
	private CAttribute toAttribute;
	private CWindow window;
	private String color;
	private List<CScheduleEvent> events;
	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public CAttribute getFromAttribute() {
		return fromAttribute;
	}
	public void setFromAttribute(CAttribute attribute) {
		this.fromAttribute = attribute;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public List<CScheduleEvent> getEvents() {
		return events;
	}
	public void setEvents(List<CScheduleEvent> events) {
		this.events = events;
	}
	public CAttribute getToAttribute() {
		return toAttribute;
	}
	public void setToAttribute(CAttribute toAttribute) {
		this.toAttribute = toAttribute;
	}
}
