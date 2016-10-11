package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

import fr.protogen.engine.utils.PairKVElement;

@SuppressWarnings("serial")
public class GParameterValues implements Serializable {
	private GParametersInstance packagedParameters;
	private CBusinessClass entity;
	private int rowDbId;
	private PairKVElement key;
	private List<PairKVElement> values;
	
	public GParametersInstance getPackagedParameters() {
		return packagedParameters;
	}
	public void setPackagedParameters(GParametersInstance packagedParameters) {
		this.packagedParameters = packagedParameters;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public int getRowDbId() {
		return rowDbId;
	}
	public void setRowDbId(int rowDbId) {
		this.rowDbId = rowDbId;
	}
	public List<PairKVElement> getValues() {
		return values;
	}
	public void setValues(List<PairKVElement> values) {
		this.values = values;
	}
	public PairKVElement getKey() {
		return key;
	}
	public void setKey(PairKVElement key) {
		this.key = key;
	}
}
