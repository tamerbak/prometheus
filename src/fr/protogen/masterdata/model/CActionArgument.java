package fr.protogen.masterdata.model;

public class CActionArgument implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5922874124774752437L;
	private CAttribute attribute;
	private CActionbutton actionButton;
	private String argumentTag="";
	
	public CActionArgument(){
		
	}
	
	public CActionArgument(CAttribute attribute, CActionbutton actionButton, String argumentTag){
		this.attribute = attribute;
		this.actionButton = actionButton;
		this.argumentTag = argumentTag;
	}

	public CAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}

	public CActionbutton getActionButton() {
		return actionButton;
	}

	public void setActionButton(CActionbutton actionButton) {
		this.actionButton = actionButton;
	}

	public String getArgumentTag() {
		return argumentTag;
	}

	public void setArgumentTag(String argumentTag) {
		this.argumentTag = argumentTag;
	}
	
	
}
