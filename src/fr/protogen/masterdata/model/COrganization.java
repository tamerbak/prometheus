package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.engine.utils.PairKVElement;

@SuppressWarnings("serial")
public class COrganization implements Serializable {
	private int id;
	private String label;
	private CBusinessClass representative;
	private List<PairKVElement> instances=new ArrayList<PairKVElement>();
	
	/*
	 * 	GETTERS AND SETTERS
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public CBusinessClass getRepresentative() {
		return representative;
	}
	public void setRepresentative(CBusinessClass representative) {
		this.representative = representative;
	}
	public List<PairKVElement> getInstances() {
		return instances;
	}
	public void setInstances(List<PairKVElement> instances) {
		this.instances = instances;
	}
}
