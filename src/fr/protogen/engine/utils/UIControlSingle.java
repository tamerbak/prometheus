package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class UIControlSingle implements Serializable {
	private List<UIControlElement> controls;
	
	public UIControlSingle(){
		controls = new ArrayList<UIControlElement>();
	}

	public List<UIControlElement> getControls() {
		return controls;
	}

	public void setControls(List<UIControlElement> controls) {
		this.controls = controls;
	}
	
}