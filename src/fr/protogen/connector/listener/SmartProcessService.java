package fr.protogen.connector.listener;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.AmanToken;
import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.SmartProcessModel;
import fr.protogen.connector.session.WebSessionManager;
import fr.protogen.engine.process.CBatchAlgorithm;
import fr.protogen.engine.process.CBatchAlgorithmFactory;
import fr.protogen.masterdata.DAO.CBatchDAO;
import fr.protogen.masterdata.model.CBatch;
import fr.protogen.masterdata.model.CBatchArgument;
import fr.protogen.masterdata.model.CBatchUnit;

@Path("/sps")
public class SmartProcessService {
	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public String trigger(String sinputModel){
		SmartProcessModel inputModel = (SmartProcessModel)(new XStream()).fromXML(sinputModel);
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			DataModel dm = new DataModel();
			dm.setStatus("FAILURE");
			dm.setExpired("TRUE");
			return (new XStream()).toXML(dm);
		}
		CBatchDAO dal = new CBatchDAO();
		CBatch batch = dal.loadBatch(inputModel.getPid());
		for(String iv : inputModel.getInitVars()){
			String key = iv.split("=")[0];
			String value = iv.split("=")[1];
			
			for(CBatchArgument a : batch.getArguments())
				if(a.getLibelle().equals(key.trim())){
					a.setValue(value);
					break;
				}
		}
		
		
		for(CBatchUnit u : batch.getUnits()){
			CBatchAlgorithm algo = CBatchAlgorithmFactory.getInstance().create(u.getType());
			algo.executeBatchUnit(u);
		}
		
		String returnStream="";
		for(CBatchArgument a : batch.getArguments()){
			if(a.getLibelle().equals(inputModel.getOutvar().trim())){
				returnStream = a.getValue();
			}
		}
		
		return returnStream;
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String test(){
		AmanToken token = new AmanToken();
		token.setAppId("FRZ48GAR4561FGD456T4E");
		token.setSessionId("");
		
		SmartProcessModel model = new SmartProcessModel();
		model.setToken(token);
		model.setInitVars(new ArrayList<String>());
		model.getInitVars().add("__SESSIONID__:fe3319d1-1836-40e9-a413-aac0ce60dd9f");
		model.getInitVars().add("__IDCOMPTE__:75");
		model.setOutvar("__RESULT__");
		model.setPid(1);
		return (new XStream()).toXML(model);
	}
}
