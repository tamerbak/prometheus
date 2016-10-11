package fr.protogen.connector.model;

import javax.xml.bind.JAXBElement;

public interface DTOModel {
	String identifyDriver();
	void parseContent(JAXBElement<DataModel> dto);
}
