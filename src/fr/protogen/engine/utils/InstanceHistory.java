package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class InstanceHistory implements Serializable {
	private String title;
	private int dbID;
	private List<ScreenDataHistory> history;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getDbID() {
		return dbID;
	}
	public void setDbID(int dbID) {
		this.dbID = dbID;
	}
	public List<ScreenDataHistory> getHistory() {
		return history;
	}
	public void setHistory(List<ScreenDataHistory> history) {
		this.history = history;
	}

}
