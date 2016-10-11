package fr.protogen.event.geb.communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.commons.io.IOUtils;

import fr.protogen.engine.control.CommunicationControl;
import fr.protogen.engine.utils.FileManipulation;
import fr.protogen.event.geb.EventModel.GEventInstance;
import fr.protogen.event.geb.EventModel.PEAMail;
import fr.protogen.event.geb.EventModel.PEASms;
import fr.protogen.masterdata.model.CoreUser;


public class ClientMail {

	private static ClientMail instance = null;
	public synchronized static ClientMail getInstance(){
		if(instance == null)
			instance = new ClientMail();
		return instance;
	}
	
	public void sendMail(String u, PEAMail p, GEventInstance evt){
		String htmlFile ="notifymail.html";
		String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("");
		htmlFile = path+"/lang/"+htmlFile;
		String fcontent = "";
		try{
			InputStream is = new FileInputStream(new File(htmlFile)); 
			fcontent = IOUtils.toString(is);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		sendMail(u,p,evt,fcontent);
		/*String to = u;
		String subject = p.getSubject();
		String text = p.getMessage();
		text = text.replaceAll("<<message>>", evt.getContent());
		
		sendMail(to, subject, text);*/
	}
	
	public void sendMail(String adress, PEAMail p, GEventInstance evt, String mailTemplate){
		CommunicationControl ctrl = new CommunicationControl();
		ctrl.setSilent(true);
		
		ctrl.setEmailTo(adress);
		ctrl.setEmailSubject(p.getSubject());
		
		//String htmlmessage = loadMessageChangePwd(newPassword);
		String text = p.getMessage();
		text = text.replaceAll("<<message>>", evt.getContent());
		String htmlMessage = load(p.getSubject(), text, mailTemplate); 
		if(htmlMessage == null || htmlMessage.length() == 0)
			htmlMessage = text;
		ctrl.setEmailMessage(htmlMessage);
		
		ctrl.sendMail();
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Message envoyé","Des événements attachés à cet enregistrement ont été retrouvés et les destinataires ont été notifiés"));
		
	}
	
	private String load(String subject, String message, String mailTemplate) {
		String temp = mailTemplate.replaceAll("<<message>>", message);
		temp = temp.replaceAll("<<subject>>", subject);
		return temp;
	}

	private void sendMail(String to, String subject, String messagetosend){
		  
	      // Sender's email ID needs to be mentioned
	      final String from = "ginsure@phoenix-access.com";
	      final String pwd = "AjmeST0553";
	      // Assuming you are sending email from localhost
	      String host = "smtp.phoenix-access.com";

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);
	      properties.put("mail.smtp.auth", "true");
	      properties.put("mail.smtp.port", "587");
	      
	      
	      // Get the default Session object.
	      Session session = Session.getInstance(properties,
	    		  new javax.mail.Authenticator() {
	    			protected PasswordAuthentication getPasswordAuthentication() {
	    				return new PasswordAuthentication(from, pwd);
	    			}
	    		  });

	      try{
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         // Set Subject: header field
	         message.setSubject(subject);

	         // Now set the actual message
	         message.setText(messagetosend);

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	}
}
