package fr.protogen.engine.process;

import fr.protogen.masterdata.model.CBatchUnitType;

public class CBatchAlgorithmFactory {
	private static CBatchAlgorithmFactory instance = null;
	public static CBatchAlgorithmFactory getInstance(){
		if(instance==null)
			instance=new CBatchAlgorithmFactory();
		return instance;
	}
	private CBatchAlgorithmFactory(){}
	
	public CBatchAlgorithm create(CBatchUnitType type){
		switch(type.getId()){
			case 1 : return new CBInsertAlg();
			case 2 : return new CBEditAlg();
			case 3 : return new CBDeleteAlg();
			case 4 : return new CBSelectAlg();
			case 5 : return new CBCalcAlg();
			default : return new CBGenAlg();
		}
	}
}
