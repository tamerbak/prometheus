package fr.protogen.masterdata.services;

public class VPAFactory {
	private static VPAFactory instance = null;
	public static synchronized VPAFactory getInstance(){
		if(instance == null)
			instance = new VPAFactory();
		return instance;
	}
	private VPAFactory(){
		
	}
	
	public ValidationPostAction create(String type){
		if(type.equals("SMS"))
			return new VPASms();
		return new VPAGenkey();
	}
}
