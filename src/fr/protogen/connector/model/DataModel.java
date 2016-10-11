package fr.protogen.connector.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dataModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataModel {
	private String entity;
	private String rootTag;
	private List<DataEntry> dataMap;
	private List<DataRow> rows;
	private List<DataModel> oneToMany;
	private AmanToken token;
	private String expired;
	private String unrecognized;
	private String status;
	private String operation;
	private List<SearchClause> clauses;
	private int page=0;
	private int pages=0;
	private int nbpages=0;
	
	private int iddriver;
	private String ignoreList="";
	
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public String getRootTag() {
		return rootTag;
	}
	public void setRootTag(String rootTag) {
		this.rootTag = rootTag;
	}
	public List<DataEntry> getDataMap() {
		return dataMap;
	}
	public void setDataMap(List<DataEntry> dataMap) {
		this.dataMap = dataMap;
	}
	public List<DataRow> getRows() {
		return rows;
	}
	public void setRows(List<DataRow> rows) {
		this.rows = rows;
	}
	public AmanToken getToken() {
		return token;
	}
	public void setToken(AmanToken token) {
		this.token = token;
	}
	public String getExpired() {
		return expired;
	}
	public void setExpired(String expired) {
		this.expired = expired;
	}
	public String getUnrecognized() {
		return unrecognized;
	}
	public void setUnrecognized(String unrecognized) {
		this.unrecognized = unrecognized;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public int getIddriver() {
		return iddriver;
	}
	public void setIddriver(int iddriver) {
		this.iddriver = iddriver;
	}
	public List<DataModel> getOneToMany() {
		return oneToMany;
	}
	public void setOneToMany(List<DataModel> oneToMany) {
		this.oneToMany = oneToMany;
	}
	public List<SearchClause> getClauses() {
		return clauses;
	}
	public void setClauses(List<SearchClause> clauses) {
		this.clauses = clauses;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getPages() {
		return pages;
	}
	public void setPages(int pages) {
		this.pages = pages;
	}
	public int getNbpages() {
		return nbpages;
	}
	public void setNbpages(int nbpages) {
		this.nbpages = nbpages;
	}
	public String getIgnoreList() {
		return ignoreList;
	}
	public void setIgnoreList(String ignoreList) {
		this.ignoreList = ignoreList;
	}
}
