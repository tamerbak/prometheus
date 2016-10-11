package fr.protogen.asgard.model.masterdata;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DbEntity implements Serializable {
	protected String dbTable;

	public String getDbTable() {
		return dbTable;
	}

	public void setDbTable(String dbTable) {
		this.dbTable = dbTable;
	}
}
