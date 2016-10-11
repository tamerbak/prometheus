package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.model.CLVSField;
import fr.protogen.masterdata.model.CLVSTable;

@SuppressWarnings("serial")
public class STTable implements Serializable {
	private String libelle;
	private CLVSTable table;
	private List<STTable> subTables = new ArrayList<STTable>();
	private List<CLVSField> fields = new ArrayList<CLVSField>();
	private List<STRow> rows = new ArrayList<STRow>();
	
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public CLVSTable getTable() {
		return table;
	}
	public void setTable(CLVSTable table) {
		this.table = table;
	}
	public List<CLVSField> getFields() {
		return fields;
	}
	public void setFields(List<CLVSField> fields) {
		this.fields = fields;
	}
	public List<STRow> getRows() {
		return rows;
	}
	public void setRows(List<STRow> rows) {
		this.rows = rows;
	}
	public List<STTable> getSubTables() {
		return subTables;
	}
	public void setSubTables(List<STTable> subTables) {
		this.subTables = subTables;
	}
}
