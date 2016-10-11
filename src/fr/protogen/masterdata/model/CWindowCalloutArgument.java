package fr.protogen.masterdata.model;

import java.io.Serializable;

public class CWindowCalloutArgument implements Serializable {

	private static final long serialVersionUID = 1499530246632720638L;
	private int id;
	private CCalloutArguments argument;
	private CAttribute attribute;
	private boolean prompt;
	private boolean selection;
	private boolean created;
	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public CCalloutArguments getArgument() {
		return argument;
	}
	public void setArgument(CCalloutArguments argument) {
		this.argument = argument;
	}
	public CAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
	public boolean isPrompt() {
		return prompt;
	}
	public void setPrompt(boolean prompt) {
		this.prompt = prompt;
	}
	public boolean isSelection() {
		return selection;
	}
	public void setSelection(boolean selection) {
		this.selection = selection;
	}
	public boolean isCreated() {
		return created;
	}
	public void setCreated(boolean created) {
		this.created = created;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
