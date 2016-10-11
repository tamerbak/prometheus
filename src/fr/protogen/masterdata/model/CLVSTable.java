package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CLVSTable implements Serializable {
	private CBusinessClass table;
	private String label;
	private List<CLVSField> fields = new ArrayList<CLVSField>();
	private List<CLVSTable> tables = new ArrayList<CLVSTable>();
	
	public CBusinessClass getTable() {
		return table;
	}
	public void setTable(CBusinessClass table) {
		this.table = table;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public List<CLVSField> getFields() {
		return fields;
	}
	public void setFields(List<CLVSField> fields) {
		this.fields = fields;
	}
	public List<CLVSTable> getTables() {
		return tables;
	}
	public void setTables(List<CLVSTable> tables) {
		this.tables = tables;
	}
}
