package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;


@SuppressWarnings("serial")
public class CParametersWindow extends CWindow implements Serializable {
	private int id_test;
	private String title_test;
	private String description_test;
	private List<CUIParameter> uiParameters;
	
	public int getId() {
		return id_test;
	}
	public void setId(int id) {
		this.id_test = id;
	}
	public String getTitle() {
		return title_test;
	}
	public void setTitle(String title) {
		this.title_test = title;
	}
	public String getDescription() {
		return description_test;
	}
	public void setDescription(String description) {
		this.description_test = description;
	}
	public List<CUIParameter> getUiParameters() {
		return uiParameters;
	}
	public void setUiParameters(List<CUIParameter> uiParameters) {
		this.uiParameters = uiParameters;
	}
	
}
