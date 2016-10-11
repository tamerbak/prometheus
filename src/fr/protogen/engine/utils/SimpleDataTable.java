package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class SimpleDataTable implements Serializable {
	
	private String header;
	private List<String> titles;
	private List<List<String>> data;
	private String table=""; 
	
	public String format(){
		table = "<table role=\"grid\"><thead><tr>";
		
		for(String t : titles){
			table = table+"<th class=\"ui-datatable-header ui-widget-header\">"+t+"</th>";
		}
		table = table+"</tr></thead>";
		table = table+"<tbody>";
		for(List<String> datum : data){
			table=table+"<tr>";
			for(String v : datum){
				table = table+"<td><center>"+v+"</center></td>";
			}
			table = table+"</tr>";
		}
		table = table+"</tbody></table>";
		return table;
	}
	
	public List<String> getTitles() {
		return titles;
	}
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	public List<List<String>> getData() {
		return data;
	}
	public void setData(List<List<String>> data) {
		this.data = data;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}
}
