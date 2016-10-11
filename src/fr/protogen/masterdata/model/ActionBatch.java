package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class ActionBatch implements Serializable {

	private String title;
	private String description;
	private List<CActionbutton> buttons;
	private List<ButtonParameter> listParam;
	private boolean parametered;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<CActionbutton> getButtons() {
		return buttons;
	}
	public void setButtons(List<CActionbutton> buttons) {
		this.buttons = buttons;
	}
	public List<ButtonParameter> getListParam() {
		return listParam;
	}
	public void setListParam(List<ButtonParameter> listParam) {
		this.listParam = listParam;
	}
	public boolean isParametered() {
		return parametered;
	}
	public void setParametered(boolean parametered) {
		this.parametered = parametered;
	}
	
	
}
