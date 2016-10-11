package fr.protogen.engine.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.DataCouple;
import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.DataRow;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dataload.QueryBuilder;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBatchArgument;
import fr.protogen.masterdata.model.CBatchUnit;
import fr.protogen.masterdata.model.CBusinessClass;

public class CBSelectAlg implements CBatchAlgorithm {

	@Override
	public void executeBatchUnit(CBatchUnit unit) {
		InstructionsUnit u = new InstructionsUnit(unit.getInstructionsModel());
		List<CBatchArgument> collectionArgs = new ArrayList<CBatchArgument>();
		for(CBatchArgument a : unit.getBatch().getArguments()){
			String arg = a.getCode();
			if(!u.getInputVars().containsKey(arg))
				continue;
			
			String var = u.getInputVars().get(arg);
			if(a.getValue().split(",").length>1){
				collectionArgs.add(a);
				continue;
			}
			String sdataModel = u.getSdataModel();
			sdataModel = sdataModel.replaceAll(var, a.getValue());
			u.setSdataModel(sdataModel);
		}
		
		String sourceModel = u.getSdataModel();
		List<String> requests = new ArrayList<String>();
		if(collectionArgs.size() == 0)
			requests.add(sourceModel);
		for(CBatchArgument a : collectionArgs){
			String[] listValues = a.getValue().split(",");
			for(String lv : listValues){
				String request = sourceModel.replaceAll(a.getCode(), lv);
				requests.add(request);
			}
				
		}
		
		u.parse();
		DataModel inputModel = u.getDataModel();
		
		for(String r : requests){
			DataModel dm = executeRequest(r);
			inputModel.getRows().addAll(dm.getRows());
		}
		
		
		String streamedResponse = (new XStream()).toXML(inputModel);
		String outv = u.processInstructions(streamedResponse);
		for(CBatchArgument a : unit.getBatch().getArguments()){
			String arg = a.getCode();
			for(String k : u.getOutputVars().keySet())
				if(k.split("=")[0].equals(arg.trim()))
					a.setValue(outv);
		}
	}

	private DataModel executeRequest(String req) {
		DataModel inputModel = (DataModel)(new XStream()).fromXML(req);
		String constraint = "";
		
		if(inputModel.getClauses() != null && inputModel.getClauses().size()>0){
			QueryBuilder builder = new QueryBuilder();
			constraint = builder.CreateWSConstraints(inputModel.getClauses());
		}
		
		int page = inputModel.getPage();
		int pages = inputModel.getPages();
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass e = pde.getReferencedTable(inputModel.getEntity());
		List<Map<CAttribute, Object>> vals;
		if(constraint!= null && constraint.length()>0)
			vals = pde.getDataByConstraint(e, constraint);
		else
			vals = pde.getAllData(e);
		ApplicationLoader dal = new ApplicationLoader();
		inputModel.setDataMap(new ArrayList<DataEntry>());
		inputModel.setRows(new ArrayList<DataRow>());
		
		for(Map<CAttribute, Object> row : vals){
			DataRow r = new DataRow();
			r.setDataRow(new ArrayList<DataEntry>());
			int id=0;
			for(CAttribute a : row.keySet()){
				DataEntry de = new DataEntry();
				Object datum = row.get(a);
				de.setValue("<![CDATA["+(datum==null?"":datum.toString())+"]]>");
				if(a.getDataReference().startsWith("pk_"))
					id = Integer.parseInt(datum.toString());
				de.setLabel("<![CDATA["+a.getAttribute()+"]]>");
				de.setType(dal.parseType(a));
				de.setAttributeReference(a.getDataReference());
				if(de.getType().startsWith("fk_") && !a.isMultiple()){
					de.setValue((datum==null?"":datum.toString()));
					List<PairKVElement> elts = new ArrayList<PairKVElement>();
					if(datum!=null && datum.toString().length()>0){
						int idDatum = Integer.parseInt(datum.toString());
						PairKVElement pkve = pde.getDataKeyByID(de.getType().substring(3), idDatum);
						pkve.setDbID(idDatum);
						elts.add(pkve);
					}
					
					de.setList(new ArrayList<DataCouple>());
					if(elts !=null)
						for(PairKVElement p : elts){
							DataCouple dc = new DataCouple();
							dc.setId(p.getDbID());
							dc.setLabel(p.getValue());
							de.getList().add(dc);
						}
				}
				
				
				r.getDataRow().add(de);
				
			}
			Map<CBusinessClass, Integer> mtms = pde.getMtms(e,id);
			for(CBusinessClass cbc : mtms.keySet()){
				DataEntry de = new DataEntry();
				int i = mtms.get(cbc);
				de.setLabel("<![CDATA["+cbc.getName()+"]]>");
				de.setType("fk_"+cbc.getDataReference());
				de.setAttributeReference("fk_"+cbc.getDataReference()+"__"+e.getDataReference());
				de.setValue(i+"");
				r.getDataRow().add(de);
			}
			
			inputModel.getRows().add(r);
		}
		
		/*
		 * Dealing with pages
		 */
		if(pages>0){
			int dataSize = inputModel.getRows().size();
			int nbpages = dataSize/pages;
			inputModel.setNbpages(nbpages);
			if(dataSize>pages) {
				int start = (page-1)*pages;
				int end = start+pages;
				if(end >= inputModel.getRows().size())
					end = inputModel.getRows().size()-1;
				
				if(start>0){
					List<DataRow> rtd = new ArrayList<DataRow>();
					for(int i = 0 ; i < start ; i++){
						rtd.add(inputModel.getRows().get(i));
					}
					inputModel.getRows().removeAll(rtd);
				}
				List<DataRow> rowtd = new ArrayList<DataRow>();
				for(int i = end ; i < inputModel.getRows().size() ; i++){
					rowtd.add(inputModel.getRows().get(i));
				}
				inputModel.getRows().removeAll(rowtd);
			}
		}
		return inputModel;
	}

}
