package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.List;

import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CWindow;

@SuppressWarnings("serial")
public class UIWindowACL implements Serializable {
	private CWindow window;
	private String title="";
	private int id;
	private boolean modification;
	private boolean suppression;
	private List<CAttribute> attributes;
	private List<CAttribute> selectedAttributes;
	
	public UIWindowACL(CWindow window){
		modification = true;
		suppression = true;
		attributes = window.getCAttributes();
		id=window.getId();
		title=window.getTitle();
		selectedAttributes = attributes;
	}

	/*
	 * GETTERS AND SETTERS
	 */
	public CWindow getWindow() {
		return window;
	}

	public void setWindow(CWindow window) {
		this.window = window;
	}

	public boolean isModification() {
		return modification;
	}

	public void setModification(boolean modification) {
		this.modification = modification;
	}

	public boolean isSuppression() {
		return suppression;
	}

	public void setSuppression(boolean suppression) {
		this.suppression = suppression;
	}

	public List<CAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}

	public List<CAttribute> getSelectedAttributes() {
		return selectedAttributes;
	}

	public void setSelectedAttributes(List<CAttribute> selectedAttributes) {
		this.selectedAttributes = selectedAttributes;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
