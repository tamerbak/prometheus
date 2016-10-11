package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CWindowCallout implements Serializable {

	private static final long serialVersionUID = -497512849494005338L;
	private int id;
	private CWindow window;
	private CCallout callout;
	private int type;
	private String jsonArg;
	private List<CWindowCalloutArgument> arguments = new ArrayList<CWindowCalloutArgument>();
	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public CCallout getCallout() {
		return callout;
	}
	public void setCallout(CCallout callout) {
		this.callout = callout;
	}
	public List<CWindowCalloutArgument> getArguments() {
		return arguments;
	}
	public void setArguments(List<CWindowCalloutArgument> arguments) {
		this.arguments = arguments;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getJsonArg() {
		return jsonArg;
	}
	public void setJsonArg(String jsonArg) {
		this.jsonArg = jsonArg;
	}
}
