package fr.protogen.ocr.pojo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Document implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String mainEntity;
	private List<Singledata> singledatas;
	private List<Tableau> tableaux;
	private List<Relativite> relativites;
	private String iddriver;
	private String iddocument;
	
	public String getId() {
		return id;
	}
	
	public List<Relativite> getRelativites() {
		return relativites;
	}

	public void setRelativites(List<Relativite> relativites) {
		this.relativites = relativites;
	}

	public void setId(String id) {
		this.id = id;
	}
	public List<Singledata> getSingledatas() {
		return singledatas;
	}
	public void setSingledatas(List<Singledata> singledatas) {
		this.singledatas = singledatas;
	}
	public List<Tableau> getTableaux() {
		return tableaux;
	}
	public void setTableaux(List<Tableau> tableaux) {
		this.tableaux = tableaux;
	}
	public Document() {
		super();
		this.id="";
		this.singledatas=new ArrayList<Singledata>();
		this.tableaux=new ArrayList<Tableau>();
		this.relativites=new ArrayList<Relativite>();
	}
	public Document(String id) {
		super();
		this.id = id;
	}

	public String getMainEntity() {
		return mainEntity;
	}

	public void setMainEntity(String mainEntity) {
		this.mainEntity = mainEntity;
	}

	public String getIddriver() {
		return iddriver;
	}

	public void setIddriver(String iddriver) {
		this.iddriver = iddriver;
	}

	public String getIddocument() {
		return iddocument;
	}

	public void setIddocument(String iddocument) {
		this.iddocument = iddocument;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	
	
	
}
