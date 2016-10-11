package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.List;

public class StringListDTO implements Serializable {
	private List<String> list;

	public StringListDTO(List<String> list){
		this.list = list;
	}
	
	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}
}
