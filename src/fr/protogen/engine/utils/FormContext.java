package fr.protogen.engine.utils;

import fr.protogen.masterdata.model.CWindow;

public class FormContext {
	private CWindow form;
	private UIControlsLine controls;
	public CWindow getForm() {
		return form;
	}
	public void setForm(CWindow form) {
		this.form = form;
	}
	public UIControlsLine getControls() {
		return controls;
	}
	public void setControls(UIControlsLine controls) {
		this.controls = controls;
	}
}
