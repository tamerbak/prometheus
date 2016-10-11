package fr.protogen.connector.listener;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.w3c.tools.codec.Base64FormatException;

import com.thoughtworks.xstream.XStream;

import fr.protogen.communication.client.SmsClient;
import fr.protogen.connector.model.SmsModel;
import fr.protogen.connector.model.StreamedFile;
import fr.protogen.engine.filestore.FileStoreManager;

@Path("/fss")
public class FileStoreService {
	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public String operationFile(String stream){
		
		XStream xe = new XStream();
		StreamedFile model = (StreamedFile)xe.fromXML(stream);
		
		if(model.getOperation().equals("PUT"))
			putFile(model);
		else
			getFile(model);
		
		stream = xe.toXML(model);
		return stream;
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
