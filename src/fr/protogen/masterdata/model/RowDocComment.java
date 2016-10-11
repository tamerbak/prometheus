package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class RowDocComment implements Serializable {
	private int id;
	private String title;
	private String content;
	private Date dateComment = new Date();
	private CoreUser user = new CoreUser();
	
	/*
	 * GETTERS AND SETTERS
	 */
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getDateComment() {
		return dateComment;
	}
	public void setDateComment(Date dateComment) {
		this.dateComment = dateComment;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
}
