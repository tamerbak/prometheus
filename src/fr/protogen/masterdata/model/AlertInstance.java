package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class AlertInstance implements Serializable {
	private int id;
	private SAlert alert;
	private String message;
	private Date created;
	private boolean seen;
	private Date seenOn;
	private CoreUser seenBy;
	private boolean closed;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public SAlert getAlert() {
		return alert;
	}
	public void setAlert(SAlert alert) {
		this.alert = alert;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public boolean isSeen() {
		return seen;
	}
	public void setSeen(boolean seen) {
		this.seen = seen;
	}
	public Date getSeenOn() {
		return seenOn;
	}
	public void setSeenOn(Date seenOn) {
		this.seenOn = seenOn;
	}
	public CoreUser getSeenBy() {
		return seenBy;
	}
	public void setSeenBy(CoreUser seenBy) {
		this.seenBy = seenBy;
	}
	public boolean isClosed() {
		return closed;
	}
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
}
