package fr.protogen.asgard.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class AsgardModel implements Serializable {
	private List<ResultTable> tables;

	public List<ResultTable> getTables() {
		return tables;
	}

	public void setTables(List<ResultTable> tables) {
		this.tables = tables;
	}
	
	
}
