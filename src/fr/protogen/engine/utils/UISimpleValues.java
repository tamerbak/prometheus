package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class UISimpleValues implements Serializable {

	private List<String> value = new ArrayList<String>();

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> values) {
		this.value = values;
	}
}
