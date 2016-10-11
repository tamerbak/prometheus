package fr.protogen.masterdata.services;

import fr.protogen.masterdata.model.ExportDriver;

public class ExportDriverFactory {
	private static ExportDriverFactory instance=null;
	public synchronized static ExportDriverFactory getInstance(){
		if(instance==null)
			instance=new ExportDriverFactory();
		return instance;
	}
	private ExportDriverFactory(){}
	
	public ExportDriverAlgorithm createDriver(ExportDriver bean){
		if(bean.getId()==1)
			return new SepaAlgorithm();
		
		return null;
	}
	
}
