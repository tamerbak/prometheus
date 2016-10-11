package fr.protogen.masterdata.model;

import java.util.List;

public class SAtom implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3811364364657591024L;
	private int id;
	private String title;
	private String description;
	private StepType type;
	private CWindow window;
	private List<CUIParameter> parameters;
	private SResource resource;
	private SScheduledCom communication;
	private SStep step;
	private boolean mandatory;
	private boolean done;
	private boolean current;
	private boolean synthesis;
	
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public StepType getType() {
		return type;
	}
	public void setType(StepType type) {
		this.type = type;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public List<CUIParameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<CUIParameter> parameters) {
		this.parameters = parameters;
	}
	public SResource getResource() {
		return resource;
	}
	public void setResource(SResource resource) {
		this.resource = new SResource();
		this.resource.setDescription(resource.getDescription());
		this.resource.setFileName(resource.getFileName());
		this.resource.setId(resource.getId());
		this.resource.setTitle(resource.getTitle());
	}
	public SScheduledCom getCommunication() {
		return communication;
	}
	public void setCommunication(SScheduledCom communication) {
		this.communication = communication;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public SStep getStep() {
		return step;
	}
	public void setStep(SStep step) {
		this.step = step;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	public boolean isCurrent() {
		return current;
	}
	public void setCurrent(boolean current) {
		this.current = current;
	}
	public boolean isSynthesis() {
		return synthesis;
	}
	public void setSynthesis(boolean synthesis) {
		this.synthesis = synthesis;
	}
	
	

}
