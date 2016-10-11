package fr.protogen.masterdata.model;

import java.io.Serializable;

public class StepType implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1149439702894459556L;
	private int id;
	private String type;
	
	public StepType(int id, String type){
		this.id=id;
		this.type = type;
	}
	
	public StepType() {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
