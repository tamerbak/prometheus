package fr.protogen.masterdata.model;

import java.util.List;

public class MPostAction implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8492496071301424473L;
	private int id;
	private String postAction;
	private MPostactionType type;
	private List<CAttribute> attributes; 
	private List<String> defaultValues;
	private List<String> parametersValues;
	private List<String> prefixes;
	
	public List<CAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPostAction() {
		return postAction;
	}
	public void setPostAction(String postAction) {
		this.postAction = postAction;
	}
	public MPostactionType getType() {
		return type;
	}
	public void setType(MPostactionType type) {
		this.type = type;
	}
	public List<String> getDefaultValues() {
		return defaultValues;
	}
	public void setDefaultValues(List<String> defaultValues) {
		this.defaultValues = defaultValues;
	}
	public List<String> getParametersValues() {
		return parametersValues;
	}
	public void setParametersValues(List<String> parametersValues) {
		this.parametersValues = parametersValues;
	}
	public List<String> getPrefixes() {
		return prefixes;
	}
	public void setPrefixes(List<String> prefixes) {
		this.prefixes = prefixes;
	}
	
	
	
}
