package fr.protogen.connector.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import flexjson.JSONDeserializer;
import fr.protogen.callout.service.CalloutEngine;
import fr.protogen.masterdata.model.CCallout;

@Path("/business")
public class BusinessListener {
	
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
		String res = "";
		try {
			res = CalloutEngine.getInstance().executeInterpretCallout(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Response.status(200).entity(res).build();
	}
	
	@GET
	public String raz(){
		String trace= "RAZ Started";
		String baseURI="calloutlibs";
		
		File index = new File(baseURI);
		if(!index.exists() || !index.isDirectory())
			index.mkdir();
		
		String[] entries = index.list();
		for(String s: entries){
		    File currentFile = new File(index.getPath(),s);
		    currentFile.delete();
		    trace=trace+"\n\tDeleted "+s;
		}
		index.delete();
		trace=trace+"\n\tDirectory Deleted ";
		return trace;
	}
}
