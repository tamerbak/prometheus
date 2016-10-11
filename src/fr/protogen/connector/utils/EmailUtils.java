package fr.protogen.connector.utils;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailUtils {

	private String userN;
	private String passW;
	
	public void sendMail(Properties props, BodyPart part, String from, String to, String subject) throws Exception {
		Authenticator auth = new SMTPAuthenticator();
		Session mailSession = Session.getDefaultInstance(props, auth);
		Transport transport = mailSession.getTransport();

		MimeMessage message = new MimeMessage(mailSession);
		Multipart multipart = new MimeMultipart("alternative");
		multipart.addBodyPart(part);
		
		message.setContent(multipart);
		message.setFrom(new InternetAddress(from));
		message.setSubject(subject);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		
		transport.connect();
		transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
		transport.close();
	}

	public String getUserN() {
		return userN;
	}

	public void setUserN(String userN) {
		this.userN = userN;
	}

	public String getPassW() {
		return passW;
	}

	public void setPassW(String passW) {
		this.passW = passW;
	}

	private class SMTPAuthenticator extends javax.mail.Authenticator {
		public PasswordAuthentication getPasswordAuthentication() {
			String username = userN;
			String password = passW;
			return new PasswordAuthentication(username, password);
		}
	}

	
}
