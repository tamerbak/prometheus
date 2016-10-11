package fr.protogen.masterdata.model;

public class ResearchableResource {
	private int id;
	private ResearchableType type;
	private String label;
	private boolean form=false;
	
	public ResearchableResource(){
		
	}
	
	public ResearchableResource(int id, String label,
			ResearchableType type) {
		this.id = id;
		this.label = label;
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ResearchableType getType() {
		return type;
	}
	public void setType(ResearchableType type) {
		this.type = type;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isForm() {
		return form;
	}

	public void setForm(boolean form) {
		this.form = form;
	}
}
