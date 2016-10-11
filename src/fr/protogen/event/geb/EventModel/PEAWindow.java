package fr.protogen.event.geb.EventModel;

import fr.protogen.masterdata.model.CWindow;

public class PEAWindow extends PostEventAction {
	private CWindow window;
	private boolean modeDetails;
	
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public boolean isModeDetails() {
		return modeDetails;
	}
	public void setModeDetails(boolean modeDetails) {
		this.modeDetails = modeDetails;
	}
}
