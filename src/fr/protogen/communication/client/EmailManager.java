package fr.protogen.communication.client;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import fr.protogen.engine.control.ui.MailDTO;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.MUserMail;
import fr.protogen.masterdata.model.MailCache;

@SuppressWarnings("serial")
public class EmailManager implements Serializable {

	public List<MailDTO> loadEmails(String hostIn, String pop, String login,
			String password) {
		List<MailDTO> mails = new ArrayList<MailDTO>();
		
		try{
		
			Properties properties = new Properties();
			
			properties.put("mail.store.protocol", "imap");
			Session emailSession = Session.getDefaultInstance(properties);
			
			Store store = emailSession.getStore("imaps");

		    store.connect(hostIn, login, password);
			
		    Folder emailFolder = store.getFolder("INBOX");
		    emailFolder.open(Folder.READ_ONLY);
		    Message[] messages = emailFolder.getMessages();
		    int id = 1;
		    int limit = 5;
		    for(Message m : messages){
		    	if(limit == 0)
		    		break;
		    	limit--;
		    	MailDTO d = new MailDTO();
		    	d.setId(id);
		    	d.setTitle(m.getSubject());
		    	if(m.isMimeType("text/plain")){
		    		d.setContent(m.getContent().toString());
		    	} else if (m.getContent() instanceof Multipart){
					Multipart multipart = (Multipart) m.getContent();
					String content = "";
					for (int j = 0; j < multipart.getCount(); j++) {
					
						 BodyPart bodyPart = multipart.getBodyPart(j);
						
						 String disposition = bodyPart.getDisposition();
						
						 content = content + bodyPart.getContent().toString();
						 
						 
						 if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) { 
					 
					
							 	DataHandler handler = bodyPart.getDataHandler();
							 	System.out.println("file name : " + handler.getName());                                 
					 }
		             
		          }
				d.setContent(content);
		    	} 
		    	if(m.getAllRecipients() != null && m.getAllRecipients().length > 0)
		    		d.setCorrespondant(m.getAllRecipients()[0].toString());
		    	d.setSentOn(m.getSentDate());
		    	d.setInBox(true);
		    	mails.add(d);
		    	id++;
		    }
		    String sent = "SENT";
		    Folder[] folders = store.getFolder("").list();
		    for(Folder f : folders)
		    	if(f.getFullName().toLowerCase().contains("sent")){
		    		sent = f.getFullName();
		    	}
		    emailFolder = store.getFolder(sent);
		    emailFolder.open(Folder.READ_ONLY);
		    messages = emailFolder.getMessages();
		    limit =5;
		    for(Message m : messages){
		    	if(limit == 0)
		    		break;
		    	limit--;
		    	MailDTO d = new MailDTO();
		    	d.setId(id);
		    	d.setTitle(m.getSubject());
		    	if(m.isMimeType("text/plain")){
		    		d.setContent(m.getContent().toString());
		    	} else if (m.getContent() instanceof Multipart){
					Multipart multipart = (Multipart) m.getContent();
					String content = "";
					for (int j = 0; j < multipart.getCount(); j++) {
					
						 BodyPart bodyPart = multipart.getBodyPart(j);
						
						 String disposition = bodyPart.getDisposition();
						
						 content = content + bodyPart.getContent().toString();
						 
						 
						 if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) { 
					 
					
							 	DataHandler handler = bodyPart.getDataHandler();
							 	System.out.println("file name : " + handler.getName());                                 
					 }
		             
		          }
				d.setContent(content);
		    	} 
		    	if(m.getFrom() != null && m.getFrom().length > 0)
		    		d.setCorrespondant(m.getFrom()[0].toString());
		    	d.setSentOn(m.getSentDate());
		    	d.setInBox(true);
		    	mails.add(d);
		    	id++;
		    }
		    
		} catch (java.lang.Exception e) {
	         e.printStackTrace();
	    }
		
		return mails;
	}

	public void updateCache(MailCache mails) {
		/*
		 * Paramètres du compte
		 */
		String hostIn = "";
		String login = "";
		String password = "";
		boolean found = false;
		try{
			String sql = "select pop, smtp, login, pass, host from c_user_mailconfig where user_id=?";
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, mails.getUtilisateur().getId());
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				found = true;
				login = rs.getString(3);
				password = rs.getString(4);
				hostIn = rs.getString(5);
			}
			rs.close();
			ps.close();
			
			if(!found)
				return;
			
			/*
			 * Collecte des messages
			 */
			
			Properties properties = new Properties();
			
			properties.put("mail.store.protocol", "imap");
			properties.setProperty("mail.imap.auth.plain.disable", "true");
			Session emailSession = Session.getDefaultInstance(properties);
			
			emailSession.setDebug(true);
			
			
			Store store = emailSession.getStore("imaps");

		    store.connect(hostIn, login, password);
			
		    Folder emailFolder = store.getFolder("INBOX");
		    emailFolder.open(Folder.READ_ONLY);
		    int end = emailFolder.getMessageCount();
            int start = end - mails.getTaille()/2 + 1;
		    Message messages[] = emailFolder.getMessages(start, end);
		    Message messageReverse[] = reverseMessageOrder(messages);
		    List<MUserMail> msgs = new ArrayList<MUserMail>();
		    for(Message mes : messageReverse){
		    	MUserMail m = new MUserMail();
		    	int difference = mes.getReceivedDate().compareTo(mails.getDerniereMAJ());
		    	if(mes.getReceivedDate()!= null && difference<0)
		    		continue;
		    	
		    	m.setCorrespondant(mes.getFrom()[0].toString());
		    	m.setDateMessage(mes.getReceivedDate());
		    	m.setEntrant(true);
		    	m.setSujet(mes.getSubject());
		    	m.setContenu(mes.getContent().toString());
		    	
		    	if (mes.getContent() instanceof Multipart){
		    		Multipart multipart = (Multipart) mes.getContent();
					String content = "";
					for (int j = 0; j < multipart.getCount(); j++) {
						 BodyPart bodyPart = multipart.getBodyPart(j);
						 content = content + bodyPart.getContent().toString();
					}
					m.setContenu(content);
					m.setUtilisateur(mails.getUtilisateur());
		    	}
		    	
		    	mails.getContenu().add(m);
		    	msgs.add(m);
		    }
		    
		    try{
			    String sent = "SENT";
			    Folder[] folders = store.getFolder("").list();
			    for(Folder f : folders)
			    	if(f.getFullName().toLowerCase().contains("sent")){
			    		sent = f.getFullName();
			    	}
			    emailFolder = store.getFolder(sent);
			    emailFolder.open(Folder.READ_ONLY);
			    end = emailFolder.getMessageCount();
	            start = end - mails.getTaille()/2 + 1;
			    messages = emailFolder.getMessages(start, end);
			    messageReverse = reverseMessageOrder(messages);
			    for(Message mes : messageReverse){
			    	MUserMail m = new MUserMail();
			    	if(mes.getSentDate()!= null && mes.getSentDate().compareTo(mails.getDerniereMAJ())<0)
			    		continue;
			    	
			    	m.setCorrespondant(mes.getRecipients(RecipientType.TO)[0].toString());
			    	m.setDateMessage(mes.getSentDate());
			    	m.setEntrant(false);
			    	m.setSujet(mes.getSubject());
			    	m.setContenu(mes.getContent().toString());
			    	
			    	if (mes.getContent() instanceof Multipart){
			    		Multipart multipart = (Multipart) mes.getContent();
						String content = "";
						for (int j = 0; j < multipart.getCount(); j++) {
							 BodyPart bodyPart = multipart.getBodyPart(j);
							 content = content + bodyPart.getContent().toString();
						}
						m.setContenu(content);
						m.setUtilisateur(mails.getUtilisateur());
			    	}
			    	
			    	mails.getContenu().add(m);
			    	msgs.add(m);
		    }
		    } catch(java.lang.Exception e){
		    	e.printStackTrace();
		    }
		    
		    mails.setDerniereMAJ(new Date());
		    
		    if(mails.getTaille()<mails.getContenu().size()){
		    	int toRem = mails.getContenu().size() - mails.getTaille()+1;
		    	for(int i = 0 ; i < toRem ; i++)
		    		mails.getContenu().remove(i);
		    }
		    
		    /*
		     * Persister
		     */
		    for(MUserMail m : msgs){
		    	sql = "insert into m_user_mail (sujet, correspondant, dateMail, content, sense, cache_id, user_id) values "
		    			+ "(?,?,?,?,?,?,?)";
		    	ps = cnx.prepareStatement(sql);
		    	ps.setString(1, m.getSujet());
		    	ps.setString(2, m.getCorrespondant());
		    	ps.setDate(3, new java.sql.Date(m.getDateMessage().getTime()));
		    	ps.setString(4, m.getContenu());
		    	ps.setString(5, m.isEntrant()?"I":"O");
		    	ps.setInt(6, mails.getId());
		    	ps.setInt(7, mails.getUtilisateur().getId());
		    	ps.execute();
		    	ps.close();
		    }
		    
		    sql = "update m_mail_cache set dateMaj=? where id=?";
		    ps = cnx.prepareStatement(sql);
		    ps.setTimestamp(1, new Timestamp(mails.getDerniereMAJ().getTime()));
		    ps.setInt(2, mails.getId());
		    ps.execute();
		    ps.close();
		}catch(java.lang.Exception exc){
			exc.printStackTrace();
		}
		
		
		
		
	}

	private Message[] reverseMessageOrder(Message[] messages) {
        Message revMessages[]= new Message[messages.length];
        int i=messages.length-1;
        for (int j=0;j<messages.length;j++,i--) {
             revMessages[j] = messages[i];

        }

        return revMessages;

   }
}
