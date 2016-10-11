package fr.protogen.connector.listener;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.GeneriumStructure;
import fr.protogen.masterdata.DAO.ApplicationLoader;

@Path("/generium")
public class GeneriumListener {
	
	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public String generiumAction(String token){
		
		GeneriumStructure structure = (GeneriumStructure) (new XStream()).fromXML(token);
		
		if(structure.getOperation().equals("reference-keys"))
			token = referenceKeys(structure);
		
		return token;
	}

	private String referenceKeys(GeneriumStructure structure) {
		ApplicationLoader dal = new ApplicationLoader();
		structure.setDataKeys(dal.getKeyAttributesReference(structure.getTable()));
		return (new XStream()).toXML(structure);
	}
}
