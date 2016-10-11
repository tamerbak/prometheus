package fr.protogen.engine.process;

import java.util.Random;

import fr.protogen.masterdata.model.CBatchArgument;
import fr.protogen.masterdata.model.CBatchUnit;

public class CBGenAlg implements CBatchAlgorithm {

	@Override
	public void executeBatchUnit(CBatchUnit unit) {
		InstructionsUnit u = new InstructionsUnit(unit.getInstructionsModel());
		
		for(CBatchArgument a : unit.getBatch().getArguments()){
			String arg = a.getCode();
			if(!u.getInputVars().containsKey(arg))
				continue;
			
			String var = u.getInputVars().get(arg);
			String sdataModel = u.getSdataModel();
			sdataModel = sdataModel.replaceAll(var, a.getValue());
			u.setSdataModel(sdataModel);
		}
		String arguments = "";
		for(String k : u.getInputVars().keySet()){
			arguments = u.getInputVars().get(k);	//	Only one input argument is applied
			break;
		}
		if(arguments.length()==0)
			return;
		
		int nbDigits = Integer.parseInt(arguments);
		String code = "";
		Random rand = new Random(); 
		
		for(int i = 0 ; i < nbDigits ; i++){
			int r = rand.nextInt(9);
			code = code+r;
		}
		
		for(CBatchArgument a : unit.getBatch().getArguments()){
			String arg = a.getCode();
			if(!u.getOutputVars().containsKey(arg))
				continue;
			
			a.setValue(code);
		}
					
	}

}
