package fr.protogen.engine.control.ui;

import java.io.Serializable;

public class SelectedItem implements Serializable{
	private int index;

	public SelectedItem(int i) {
		// TODO Auto-generated constructor stub
		index = i;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
