package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class ExportMap implements Serializable {
	private int id;
	private ExportDriver driver;
	private CBusinessClass entity;
	private List<MapEntry> entries;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ExportDriver getDriver() {
		return driver;
	}
	public void setDriver(ExportDriver driver) {
		this.driver = driver;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public List<MapEntry> getEntries() {
		return entries;
	}
	public void setEntries(List<MapEntry> entries) {
		this.entries = entries;
	}
}
