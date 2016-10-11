package fr.protogen.connector.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import flexjson.JSONDeserializer;
import fr.protogen.connector.model.JSONReportQuery;
import fr.protogen.engine.reporting.JSONPDFWriter;
import fr.protogen.engine.reporting.PDFWriter;
import fr.protogen.engine.reporting.SQLPDFWriter;

@Path("/reportjs")
public class ReportingJS {
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getData(InputStream sin){
		String res = "{\"status\" : \"failure\", \"pdf\" : \"\"}";
		
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sin));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		JSONReportQuery query = new JSONDeserializer<JSONReportQuery>().deserialize(jsonText);
		PDFWriter w = new JSONPDFWriter();
		if(query.getType()!= null && query.getType().equals("SQL")){
			w = new SQLPDFWriter();
		}
		
		String encodedPDF = w.doPrint(query.getJrxmlFilePath(), query.getEncodedJson(), true, true);
		res = "{\"status\" : \"success\", \"pdf\" : \""+encodedPDF+"\"}";
		
		return Response.status(200).entity(res).build();
	}
}
