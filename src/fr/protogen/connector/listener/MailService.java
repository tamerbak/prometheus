package fr.protogen.connector.listener;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.MailModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@Path("/envoimail")
public class MailService {
	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public String sendMail(String stream){
		
		XStream xe = new XStream();
		MailModel mod = (MailModel)xe.fromXML(stream); 
		
		/*CommunicationControl ctrl = new CommunicationControl();
		
		ctrl.setSilent(true);
		ctrl.setEmailTo(mod.getSendTo());
		ctrl.setEmailSubject(mod.getTitle());
		ctrl.setEmailMessage(mod.getContent());
		mod.setStatus(ctrl.sendMail());
		
		stream = xe.toXML(mod);
		return stream;*/
		try{
			Properties props = new Properties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", "ssl0.ovh.net");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.port", 465);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			
	
			Authenticator auth = new SMTPAuthenticator();
			Session mailSession = Session.getDefaultInstance(props, auth);
			// uncomment for debugging infos to stdout
			mailSession.setDebug(true);
			Transport transport = mailSession.getTransport();
	
			MimeMessage message = new MimeMessage(mailSession);
	
			Multipart multipart = new MimeMultipart("alternative");
	
			/*BodyPart part = new MimeBodyPart();
			part.setText(mod.getContent());
				
	
			multipart.addBodyPart(part);*/
				
			message.setText(mod.getContent());
			message.setFrom(new InternetAddress("support@vitonjob.com"));
			message.setSubject(mod.getTitle());
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mod.getSendTo()));
			
			transport.connect();
			transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
			transport.close();
			mod.setStatus("success");
		} catch(Exception e){
			e.printStackTrace();
			mod.setStatus("failure : "+e.getMessage());
		}
		stream = xe.toXML(mod);
		return stream;
	}
	
	private class SMTPAuthenticator extends javax.mail.Authenticator {
		public PasswordAuthentication getPasswordAuthentication() {
			String username = "support@vitonjob.com";
			String password = "AjmeST0553";
			return new PasswordAuthentication(username, password);
		}
	}
}
