package fr.protogen.asgard.model.masterdata;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MappedColumn implements Serializable {
	private String dbCol;

	public String getDbCol() {
		return dbCol;
	}

	public void setDbCol(String dbCol) {
		this.dbCol = dbCol;
	}
}
