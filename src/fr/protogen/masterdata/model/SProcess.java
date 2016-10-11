package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

public class SProcess implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5401176172491753838L;
	private int id;
	private String title;
	private CBusinessClass entity;
	private CoreUser user;
	private List<CWindow> windows;
	private List<CParametersWindow> pwindows;
	private String description;
	
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
	public List<CWindow> getWindows() {
		return windows;
	}
	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	private String stream;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStream() {
		return stream;
	}
	public void setStream(String stream) {
		this.stream = stream;
	}
	public List<CParametersWindow> getPwindows() {
		return pwindows;
	}
	public void setPwindows(List<CParametersWindow> pwindows) {
		this.pwindows = pwindows;
	}
	
}
