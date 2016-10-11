package fr.protogen.connector.listener;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.thoughtworks.xstream.XStream;

import fr.protogen.communication.client.SmsClient;
import fr.protogen.connector.model.SmsModel;

@Path("/envoisms")
public class SmsService {
	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public String sendMail(String stream){
		
		XStream xe = new XStream();
		SmsModel mod = (SmsModel)xe.fromXML(stream); 
		
		try {
			SmsClient.getInstance().sendSMS(mod.getText(), mod.getTelephone());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		stream = xe.toXML(mod);
		return stream;
	}
	
}
