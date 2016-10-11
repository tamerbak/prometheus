package fr.protogen.engine.process;

import fr.protogen.connector.model.DataRow;
import fr.protogen.masterdata.DAO.GeneriumLinkDAL;
import fr.protogen.masterdata.model.CBatchArgument;
import fr.protogen.masterdata.model.CBatchUnit;

public class CBInsertAlg implements CBatchAlgorithm {

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
		
		u.parse();
		
		GeneriumLinkDAL dal = new GeneriumLinkDAL();
		String outv = "";
		for(DataRow r : u.getDataModel().getRows()){
			String sid = dal.insertData(r.getDataRow(), u.getDataModel());
			outv = outv+sid+",";
		}
		if(outv.length()>1)
			outv.substring(0, outv.length()-1);
		
		outv = u.processInstructions(outv);
		
		for(CBatchArgument a : unit.getBatch().getArguments()){
			String arg = a.getCode();
			if(!u.getOutputVars().containsKey(arg))
				continue;
			
			a.setValue(outv);
		}
	}

}
