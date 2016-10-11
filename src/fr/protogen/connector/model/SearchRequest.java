package fr.protogen.connector.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class SearchRequest implements Serializable {
	private String query;
	private AmanToken token;
	private List<String> tables;
	private List<DataModel> results;
	private String status;
	private String ignoreList = "";
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public List<String> getTables() {
		return tables;
	}
	public void setTables(List<String> tables) {
		this.tables = tables;
	}
	public AmanToken getToken() {
		return token;
	}
	public void setToken(AmanToken token) {
		this.token = token;
	}
	public List<DataModel> getResults() {
		return results;
	}
	public void setResults(List<DataModel> results) {
		this.results = results;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getIgnoreList() {
		return ignoreList;
	}
	public void setIgnoreList(String ignoreList) {
		this.ignoreList = ignoreList;
	}
	
	
}
