package fr.protogen.importData;
import java.util.List;


public class DataStructureTable {

	private List<DataStructureTable> referencedTable;
	private List<String> headers,types,protogenColumns;
	private String protogenTable;
	private List<List<Object>> data;
	private List<String> keys;
	
	
	public List<DataStructureTable> getReferencedTable() {
		return referencedTable;
	}
	public void setReferencedTable(List<DataStructureTable> referencedTable) {
		this.referencedTable = referencedTable;
	}
	public List<String> getHeaders() {
		return headers;
	}
	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
	public List<String> getTypes() {
		return types;
	}
	public void setTypes(List<String> types) {
		this.types = types;
	}
	public List<String> getProtogenColumns() {
		return protogenColumns;
	}
	public void setProtogenColumns(List<String> protogenColumns) {
		this.protogenColumns = protogenColumns;
	}
	
	public String getProtogenTable() {
		return protogenTable;
	}
	public void setProtogenTable(String protogenTable) {
		this.protogenTable = protogenTable;
	}
	public List<List<Object>> getData() {
		return data;
	}
	public void setData(List<List<Object>> data) {
		this.data = data;
	}
	public List<String> getKeys() {
		return keys;
	}
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}
	
	
	
	
}
