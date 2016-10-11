package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.List;

import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.UIControlElement;

@SuppressWarnings("serial")
public class MtmLine implements Serializable{
	private int id;
	private String key;
	private boolean calculated;
	private boolean dirty=false;
	private List<PairKVElement> values;
	private List<UIControlElement> controls;
	private boolean temporary;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public List<PairKVElement> getValues() {
		return values;
	}
	public void setValues(List<PairKVElement> values) {
		this.values = values;
	}
	public boolean isCalculated() {
		return calculated;
	}
	public void setCalculated(boolean calculated) {
		this.calculated = calculated;
	}
	public boolean isDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	public List<UIControlElement> getControls() {
		return controls;
	}
	public void setControls(List<UIControlElement> controls) {
		this.controls = controls;
	}
	public boolean isTemporary() {
		return temporary;
	}
	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}
	
	
}
