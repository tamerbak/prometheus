package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.List;

import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.model.GParametersPackage;

@SuppressWarnings("serial")
public class GParametersComponent implements Serializable {
	private String modelName;
	private GParametersPackage pkg;
	private List<PairKVElement> modelInstances;
	
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public List<PairKVElement> getModelInstances() {
		return modelInstances;
	}
	public void setModelInstances(List<PairKVElement> modelInstances) {
		this.modelInstances = modelInstances;
	}
	public GParametersPackage getPkg() {
		return pkg;
	}
	public void setPkg(GParametersPackage pkg) {
		this.pkg = pkg;
	}
}
