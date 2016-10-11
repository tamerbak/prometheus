package fr.protogen.connector.listener;


import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.SearchRequest;
import fr.protogen.masterdata.DAO.GeneriumLinkDAL;

@Path("/search")
public class SearchListener {
	
	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public String search(String stoken){
		
		SearchRequest inputModel = new SearchRequest();
		XStream helper = new XStream();
		inputModel = (SearchRequest) helper.fromXML(stoken);
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new SearchRequest();
			inputModel.setStatus("FAILURE");
			return helper.toXML(inputModel);
		}
		
		GeneriumLinkDAL dal = new GeneriumLinkDAL();
		boolean ignoreList = (inputModel.getIgnoreList()!=null&&inputModel.getIgnoreList().toLowerCase().equals("true"))?true:false;
		List<DataModel> models = dal.lookUp(inputModel.getTables(), inputModel.getQuery(), ignoreList);
		inputModel.setResults(models);
		stoken= helper.toXML(inputModel);
		return stoken;
	}
}
