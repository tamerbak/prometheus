package fr.protogen.connector.sage;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import fr.protogen.connector.model.DTOModel;
import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;

public class SageDTOModel implements DTOModel {

	private List<DataEntry> entries;
	private Map<DataEntry, String> fieldsMapping;
	
	@Override
	public String identifyDriver() {
		return "SAGE";
	}

	@Override
	public void parseContent(JAXBElement<DataModel> dto) {
		

	}

	/*
	 * 	GETTERS AND SETTERS
	 */
	public List<DataEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<DataEntry> entries) {
		this.entries = entries;
	}

	public Map<DataEntry, String> getFieldsMapping() {
		return fieldsMapping;
	}

	public void setFieldsMapping(Map<DataEntry, String> fieldsMapping) {
		this.fieldsMapping = fieldsMapping;
	}

}
