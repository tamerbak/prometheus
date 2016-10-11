package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

public class ScheduleEntry implements Serializable  {
	
	private int id;
	private String title;
	private String description;
	private Date startAt;
	private Date endAt;
	private boolean rappel;
	private Date rappelAt;
	private CoreUser user;
	private int priority;
	
	
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getStartAt() {
		return startAt;
	}
	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}
	public Date getEndAt() {
		return endAt;
	}
	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}
	public boolean isRappel() {
		return rappel;
	}
	public void setRappel(boolean rappel) {
		this.rappel = rappel;
	}
	public Date getRappelAt() {
		return rappelAt;
	}
	public void setRappelAt(Date rappelAt) {
		this.rappelAt = rappelAt;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
}
