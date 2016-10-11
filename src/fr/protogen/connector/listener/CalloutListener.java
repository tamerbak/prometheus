package fr.protogen.connector.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import fr.protogen.callout.GCalloutData;
import fr.protogen.callout.service.CalloutEngine;
import fr.protogen.masterdata.model.CCallout;

@Path("/callout")
public class CalloutListener {
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response anima(InputStream inStream){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		CCallout c = new JSONDeserializer<CCallout>().deserialize(jsonText);
		Object results = null;
		try {
			results = CalloutEngine.getInstance().executeCallout(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(results == null)
			results="";
		
		String res = new JSONSerializer().serialize(results);
		return Response.status(200).entity(res).build();
	}
	
}
