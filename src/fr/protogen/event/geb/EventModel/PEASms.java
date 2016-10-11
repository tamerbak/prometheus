package fr.protogen.event.geb.EventModel;

public class PEASms extends PostEventAction {
	
	private String subject;
	private String text;
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
