package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class CViewPart implements Serializable {
	private int id;
	private String[] titles;
	private String query;
	private List<List<String>> dataRows;
	
	public String[] getTitles() {
		return titles;
	}
	public void setTitles(String[] titles) {
		this.titles = titles;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public List<List<String>> getDataRows() {
		return dataRows;
	}
	public void setDataRows(List<List<String>> dataRows) {
		this.dataRows = dataRows;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
