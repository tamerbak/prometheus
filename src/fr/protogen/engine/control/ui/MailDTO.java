package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class MailDTO implements Serializable {
	private int id;
	private String correspondant = "";
	private String title;
	private String content;
	private Date sentOn=new Date();
	private boolean inBox=true;
	
	public MailDTO(){
		
	}
	
	public MailDTO(int id, String correspondant, String title, String content, Date setOn, boolean inBox) {
		super();
		this.id = id;
		this.correspondant = correspondant;
		this.title = title;
		this.content = content;
		this.setSentOn(sentOn);
		this.setInBox(inBox);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCorrespondant() {
		return correspondant;
	}
	public void setCorrespondant(String correspondant) {
		this.correspondant = correspondant;
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

	public Date getSentOn() {
		return sentOn;
	}

	public void setSentOn(Date sentOn) {
		this.sentOn = sentOn;
	}

	public boolean isInBox() {
		return inBox;
	}

	public void setInBox(boolean inBox) {
		this.inBox = inBox;
	}
	
	
}
