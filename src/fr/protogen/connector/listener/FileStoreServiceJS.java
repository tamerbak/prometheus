package fr.protogen.connector.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.w3c.tools.codec.Base64FormatException;


import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import fr.protogen.connector.model.StreamedFile;
import fr.protogen.engine.filestore.FileStoreManager;

@Path("/fssjs")
public class FileStoreServiceJS {
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response operationFile(InputStream sin){
		
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
		System.out.println(jsonText);
		StreamedFile model = new JSONDeserializer<StreamedFile>().deserialize(jsonText);
		
		if(model.getOperation().equals("PUT"))
			putFile(model);
		else
			getFile(model);
		
		String stream = new JSONSerializer().deepSerialize(model);
		return Response.status(200).entity(stream).build();
	}

	private void getFile(StreamedFile model) {
		try {
			String content = FileStoreManager.getInstance().load(model.getIdentifiant(), model.getTable());
			model.setStream(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void putFile(StreamedFile model) {
		try {
			FileStoreManager.getInstance().store(model.getFileName(), model.getStream(), model.getTable(), model.getIdentifiant());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Base64FormatException e) {
			e.printStackTrace();
		}
	}
	
}
