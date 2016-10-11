package fr.protogen.connector.model;

import java.util.List;

public class JSONReportQuery {
	private List<String> jrxmlFilePath;
	private String encodedJson;
	private String type;
	
	public List<String> getJrxmlFilePath() {
		return jrxmlFilePath;
	}
	public void setJrxmlFilePath(List<String> jrxmlFilePath) {
		this.jrxmlFilePath = jrxmlFilePath;
	}
	public String getEncodedJson() {
		return encodedJson;
	}
	public void setEncodedJson(String encodedJson) {
		this.encodedJson = encodedJson;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
