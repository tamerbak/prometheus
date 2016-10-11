package fr.protogen.masterdata.model;

import java.sql.Date;

public class SUIAlert implements java.io.Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4157695847569468325L;
	private int ID;
	private Date created;
	private CWindow window;
	private String constraint;
	private String outputData;
	private boolean newInstance;
	private String identifier;
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public String getConstraint() {
		return constraint;
	}
	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}
	public String getOutputData() {
		return outputData;
	}
	public void setOutputData(String outputData) {
		this.outputData = outputData;
	}
	public boolean isNewInstance() {
		return newInstance;
	}
	public void setNewInstance(boolean newInstance) {
		this.newInstance = newInstance;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	
}
