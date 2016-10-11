package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.List;

import fr.protogen.masterdata.model.CBusinessClass;

@SuppressWarnings("serial")
public class GDataTable implements Serializable {
	private CBusinessClass entity;
	private List<GDataRow> rows;
	
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public List<GDataRow> getRows() {
		return rows;
	}
	public void setRows(List<GDataRow> rows) {
		this.rows = rows;
	}
}
