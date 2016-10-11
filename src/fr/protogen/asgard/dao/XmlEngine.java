package fr.protogen.asgard.dao;

import com.thoughtworks.xstream.XStream;

import fr.protogen.asgard.model.masterdata.AsgardMappingModel;

public class XmlEngine {
	private static XmlEngine instance=null;
	public static synchronized XmlEngine getInstance(){
		if(instance == null)
			instance = new XmlEngine();
		
		return instance;
	}
	
	private XmlEngine(){}
	
	public String serialize(AsgardMappingModel model){
		
		XStream encoder = new XStream();
		String xml = encoder.toXML(model);
		
		return xml;
	}
	
	public AsgardMappingModel deserialize(String xml){
		XStream encoder = new XStream();
		AsgardMappingModel model = (AsgardMappingModel) encoder.fromXML(xml);
		return model;
	}
}
