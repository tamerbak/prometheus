package fr.protogen.masterdata.model;

@SuppressWarnings("serial")
public class SAlert implements java.io.Serializable{
	private int id;
	private String titre;
	private String description;
	private boolean insert=true;
	private CoreRole role;
	private CWindow window;
	private String appKey;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isInsert() {
		return insert;
	}
	public void setInsert(boolean insert) {
		this.insert = insert;
	}
	public CoreRole getRole() {
		return role;
	}
	public void setRole(CoreRole role) {
		this.role = role;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
}
