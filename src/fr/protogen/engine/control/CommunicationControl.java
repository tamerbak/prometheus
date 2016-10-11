package fr.protogen.engine.control;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import fr.protogen.communication.client.ComServiceEmail;
import fr.protogen.communication.client.ComServiceFax;
import fr.protogen.communication.client.ComServiceSms;
import fr.protogen.communication.client.StringArray;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.engine.utils.ProtogenParameters;
import fr.protogen.masterdata.DAO.ScheduleDAO;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.SScheduledCom;
import fr.protogen.masterdata.model.ScheduleEntry;

@ManagedBean
@ViewScoped
public class CommunicationControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 840945014213413463L;

	private SScheduledCom comm;
	
	//	EMail
	private String emailTo;
	private List<String> tos;
	private String emailCC;
	private List<String> ccs;
	private String emailSubject;
	private String emailMessage;
	private String file;
	private String filedir;
	private boolean attached;
	private String emailCCI;
	
	//	SMS
	private String smsNumber;
	private String smsMessage;
	
	//fax
	private String  faxNumber;
	private Date datefaxdiffere;
	private Locale locale;
	private String fileFax;
	private String filedirFax;
	private boolean attachedFax;
	
	private boolean silent=false;
	
	/*
	 * Schedule
	 */
	private List<ScheduleEntry> userSchedule = new ArrayList<ScheduleEntry>();
	private ScheduleEntry handledEntry = new ScheduleEntry();
	private ScheduleModel eventModel;
	private boolean insertMode=false;
	private String title;
	private String description;
	private Date startAt;
	private Date endAt;
	private boolean rappel;
	private Date rappelAt;
	private int priority;
	private ScheduleEvent handledEvent;

	@PostConstruct
	public void initialize(){
		
		boolean notinsession=(!FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
				|| FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")==null); 

		if(notinsession){
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		Map<String,Object> params =FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		comm=(SScheduledCom)params.get(ProtogenConstants.COMM_ATOM);
		if(comm == null){
			comm = new SScheduledCom();
			comm.setTitle("Communication");
			comm.setDescription("Plateforme de communication (Courrier Eléctronique, SMS, Appel téléphonique, Visio-conférnece, Fax)");
		}
		attached = false;
		
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		
		eventModel = new DefaultScheduleModel(); 
		
		ScheduleDAO dao = new ScheduleDAO();
		userSchedule = dao.loadSchedule(user);
		
		for(ScheduleEntry e : userSchedule){
			eventModel.addEvent(new DefaultScheduleEvent(e.getTitle(), e.getStartAt(), e.getEndAt(), e));
		}
		
	}
	
	public List<String> completeMails(String key){
		
		List<String> contacts = new ArrayList<String>();
		
		String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(skey);
		List<CoreUser> usr = cache.getUsers();
		
		for(CoreUser u : usr)
			if(u.getFirstName().toLowerCase().startsWith(key.toLowerCase())
					|| u.getFirstName().toLowerCase().endsWith(key.toLowerCase())
					|| u.getLastName().toLowerCase().startsWith(key.toLowerCase())
					|| u.getLastName().toLowerCase().endsWith(key.toLowerCase())){
				contacts.add(u.getLogin());
			}
		
		return contacts;
	}
	
	public String sendSMS(){
		try {
			// access au service web
			String WS_URL=ProtogenParameters.APPLICATION+"/apiservice/comServiceSms?wsdl";
			URL url = new URL(WS_URL);
			Authenticator.setDefault(new Authenticator() {
				 
				@Override
			    protected PasswordAuthentication getPasswordAuthentication() {     
					return new PasswordAuthentication("mkyong", "123456".toCharArray());
			    }
			});
			QName qname = new QName("http://serviceweb.apiCom.web.phoenix.fr/",
					"ComServiceSmsService");
			// création de fabrique pour le ws
			Service service = Service.create(url, qname);
			// recupération proxy pr accéder au methode
			ComServiceSms servicewebSms = service.getPort(ComServiceSms.class);
			 /*******************UserName & Password ******************************/
			//add username and password for container authentication
	        BindingProvider bp = (BindingProvider) servicewebSms;
	        bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "mkyong");
	        bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "123456");
			   /**********************************************************************/	
			servicewebSms.envoyerSMS(smsNumber, smsMessage);
			
			smsNumber="";
			smsMessage="";
			FacesContext context = FacesContext.getCurrentInstance();  
	        if(!silent)
	        	context.addMessage(null, new FacesMessage("Message envoyé avec succès", ""));  
			
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();  
			if(!silent)
				context.addMessage(null, new FacesMessage("Erreur d'envoi du message veuillez essayer ultérieurement", ""));  
			e.printStackTrace();
		}
		return "";
	}
	
	public String sendMail(){
		try {
			// access au service web
			String WS_URL= "http://vitonjobv1.datqvvgppi.us-west-2.elasticbeanstalk.com/apiservice/comServiceEmail?wsdl";
			URL url = new URL(WS_URL);
			Authenticator.setDefault(new Authenticator() {
				 
				@Override
			    protected PasswordAuthentication getPasswordAuthentication() {     
					return new PasswordAuthentication("mkyong", "123456".toCharArray());
			    }
			});
			QName qname = new QName("http://serviceweb.apiCom.web.phoenix.fr/",
					"ComServiceEmailService");
			// création de fabrique pour le ws
			Service service = Service.create(url, qname);
			// recupération proxy pr accéder au methode
			ComServiceEmail servicewebEmail = service.getPort(ComServiceEmail.class);
			 /*******************UserName & Password ******************************/
			//add username and password for container authentication
	        BindingProvider bp = (BindingProvider) servicewebEmail;
	        bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "mkyong");
	        bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "123456");
			   /**********************************************************************/	
	        StringArray mailsarray = new StringArray();
	    	StringArray mailsccarray = new StringArray();
	    	StringArray mailscciarray = new StringArray();
	    	
	    	if(tos == null){
	    		tos = new ArrayList<String>();
	    		tos.add(emailTo);
	    	}
	    	if(ccs == null){
	    		ccs = new ArrayList<String>();
	    	}
	    	String[]mailscci=null;
	    	if(emailCCI!=null && !emailCCI.equals("")) {
	    		mailscci=emailCCI.split(",");
	    		for (int i = 0; i < mailscci.length; i++) {
		    		mailscciarray.getItem().add(mailscci[i]);
		    	}
	    	}
	    	for(String cc : ccs)
	    		mailsccarray.getItem().add(cc);
	    	
	    	for(String to : tos){
	    		mailsarray.getItem().add(to);
	    	}
	    		 
 	    	String coprsHtm="<html>" +emailMessage+
	    			"<html>";
			if(!attached){
				servicewebEmail.envoyermailfilecciccHtml(mailsarray, mailsccarray, mailscciarray, emailSubject, coprsHtm, "");
			}
			else {
				servicewebEmail.envoyermailfilecciccHtml(mailsarray, mailsccarray, mailscciarray, emailSubject, coprsHtm, file);
				File f = new File(file);
				f.delete();
				f = new File(filedir);
				f.delete();
			}
			
			emailCC="";emailCCI="";
			emailTo="";
			emailMessage="";
			emailSubject="";
			
			attached=false;
			
			FacesContext context = FacesContext.getCurrentInstance();  
			if(!silent)
				context.addMessage(null, new FacesMessage("Message envoyé avec succès", ""));  
			
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();  
			if(!silent)
				context.addMessage(null, new FacesMessage("Erreur d'envoi du message. Veuillez essayer ultérieurement", ""));  
			e.printStackTrace();
			return "KO";
		}
		return "OK";
	}
public String sendFAX(){
		
		try {
			// access au service web
			String WS_URL=ProtogenParameters.APPLICATION+"/apiservice/comServiceFax?wsdl";
			URL url = new URL(WS_URL);
			Authenticator.setDefault(new Authenticator() {
				 
				@Override
			    protected PasswordAuthentication getPasswordAuthentication() {     
					return new PasswordAuthentication("mkyong", "123456".toCharArray());
			    }
			});
			QName qname = new QName("http://serviceweb.apiCom.web.phoenix.fr/",
					"ComServiceFaxService");
			// création de fabrique pour le ws
			Service service = Service.create(url, qname);
			// recupération proxy pr accéder au methode
			ComServiceFax servicewebFax = service.getPort(ComServiceFax.class);
			 /*******************UserName & Password ******************************/
			//add username and password for container authentication
	        BindingProvider bp = (BindingProvider) servicewebFax;
	        bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "mkyong");
	        bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "123456");
			   /**********************************************************************/	
			StringArray st=new StringArray(); 
			String[] tofaxs = faxNumber.split(",");
			for (int i = 0; i < tofaxs.length; i++) {
				st.getItem().add(tofaxs[i]);
			}
			XMLGregorianCalendar datefaxdifferexml = null;
			GregorianCalendar gregory = new GregorianCalendar();
			gregory.setTime(datefaxdiffere);
			datefaxdifferexml=DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
			if(datefaxdifferexml==null || datefaxdiffere.compareTo(new Date() ) <0 )
				servicewebFax.envoyerfaxer(st,fileFax);
			else{
				servicewebFax.envoyerfaxerdifferer(st,fileFax,datefaxdifferexml);
				File f = new File(fileFax);
				f.delete();
				f = new File(filedirFax);
				f.delete();
			}
			
			faxNumber="";
			datefaxdiffere=null;
			locale=null;
			attachedFax=false;
			
			FacesContext context = FacesContext.getCurrentInstance();  
	          
	        context.addMessage(null, new FacesMessage("Message envoyé avec succès.", ""));  
			
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();  
	          
	        context.addMessage(null, new FacesMessage("Erreur d'envoi du message. Veuillez essayer ultérieurement.", ""));  
			e.printStackTrace();
		}
		return "";
	}
	
	@SuppressWarnings("unused")
   	public void fileUpload(FileUploadEvent event) {
       	try {
       		FacesContext fc = FacesContext.getCurrentInstance();
       	    ExternalContext ec = fc.getExternalContext();
   			InputStream is = event.getFile().getInputstream();
   			int length = (int)event.getFile().getSize();
   			filedirFax = ec.getRealPath("/tmp/")+UUID.randomUUID().toString()+"/";
   			File dir = new File(filedirFax);
   			if(!dir.exists())
   				dir.mkdir();
   			
   			fileFax = filedirFax+event.getFile().getFileName();
   			File f = new File(fileFax);
   			if(!f.exists())
   				f.createNewFile();
   			
   			OutputStream writer = new FileOutputStream(fileFax);
   			byte[] bytes = new byte[1024];
   			int read = 0;
   			while((read = is.read(bytes))!=-1){
   				writer.write(bytes);
   			}
   			
   			is.close();
   			writer.flush();
   			writer.close();
   			
			attachedFax = true;
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
       }
    
	@SuppressWarnings("unused")
   	public void handleFileUpload(FileUploadEvent event) {
       	try {
       		FacesContext fc = FacesContext.getCurrentInstance();
       	    ExternalContext ec = fc.getExternalContext();
   			InputStream is = event.getFile().getInputstream();
   			int length = (int)event.getFile().getSize();
   			filedir = ec.getRealPath("/tmp/")+UUID.randomUUID().toString()+"/";
   			File dir = new File(filedir);
   			if(!dir.exists())
   				dir.mkdir();
   			
   			file = filedir+event.getFile().getFileName();
   			File f = new File(file);
   			if(!f.exists())
   				f.createNewFile();
   			
   			OutputStream writer = new FileOutputStream(file);
   			byte[] bytes = new byte[1024];
   			int read = 0;
   			while((read = is.read(bytes))!=-1){
   				writer.write(bytes);
   			}
   			
   			is.close();
   			writer.flush();
   			writer.close();
   			
			attached = true;
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
       }
    public void redirection(){
    	Desktop desktop = null; 
		java.net.URI url; 
		try { 
		url = new java.net.URI(ProtogenParameters.APPLICATION+"/prometheus/protogen-sendappel"); 
		if (Desktop.isDesktopSupported()) 
		{ 
		desktop = Desktop.getDesktop(); 
		desktop.browse(url); 
		} 
		} 
		catch (Exception ex) { 
		 ex.printStackTrace();
		System.out.println(ex.getMessage()); 
		}
    }
	
    /*
	 * 	SCHEDULE MANAGEMENT
	 */
	public void selectEventChange(){
		/*handledEntry = (ScheduleEntry)evt.getScheduleEvent().getData();
		handledEvent = evt.getScheduleEvent();
		title = handledEntry.getTitle();
		description = handledEntry.getDescription();
		startAt = handledEntry.getStartAt();
		endAt = handledEntry.getEndAt();
		rappel = handledEntry.isRappel();
		rappelAt = handledEntry.getRappelAt();
		priority = handledEntry.getPriority();
		insertMode=true;*/
		
	}
	
	public void onEventMove(ScheduleEntryMoveEvent evt) {  
		handledEntry = (ScheduleEntry)evt.getScheduleEvent().getData();
		handledEvent = evt.getScheduleEvent();
		
		handledEntry.setStartAt(handledEvent.getStartDate());
		handledEntry.setEndAt(handledEvent.getEndDate());
	      
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		handledEntry.setUser(user);
		ScheduleDAO dao = new ScheduleDAO();
		dao.updateEntry(handledEntry);
		int index = eventModel.getEvents().indexOf(handledEvent);
		eventModel.getEvents().remove(index);
		eventModel.addEvent(new DefaultScheduleEvent(handledEntry.getTitle(), handledEntry.getStartAt(), handledEntry.getEndAt(), handledEntry));
		
	}  
	  
	public void onEventResize(ScheduleEntryResizeEvent evt) {  
		handledEntry = (ScheduleEntry)evt.getScheduleEvent().getData();
		handledEvent = evt.getScheduleEvent();
		
		handledEntry.setStartAt(handledEvent.getStartDate());
		handledEntry.setEndAt(handledEvent.getEndDate());
	      
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		handledEntry.setUser(user);
		ScheduleDAO dao = new ScheduleDAO();
		dao.updateEntry(handledEntry);
		int index = eventModel.getEvents().indexOf(handledEvent);
		eventModel.getEvents().remove(index);
		eventModel.addEvent(new DefaultScheduleEvent(handledEntry.getTitle(), handledEntry.getStartAt(), handledEntry.getEndAt(), handledEntry));
	}
	
	
	
	public String insertEntry(){
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		handledEntry = new ScheduleEntry();
		handledEntry.setTitle(title);
		handledEntry.setDescription(description);
		handledEntry.setStartAt(startAt);
		handledEntry.setEndAt(endAt);
		handledEntry.setRappel(rappel);
		handledEntry.setRappelAt(rappelAt);
		handledEntry.setPriority(priority);
		handledEntry.setUser(user);
		ScheduleDAO dao = new ScheduleDAO();
		dao.insertEntry(user, handledEntry);
		
		eventModel.addEvent(new DefaultScheduleEvent(handledEntry.getTitle(), handledEntry.getStartAt(), handledEntry.getEndAt(), handledEntry));
		
		return "";
	}
	
	public String updateEntry(){
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser user = cache.getUser();
		handledEntry.setTitle(title);
		handledEntry.setDescription(description);
		handledEntry.setStartAt(startAt);
		handledEntry.setEndAt(endAt);
		handledEntry.setRappel(rappel);
		handledEntry.setRappelAt(rappelAt);
		handledEntry.setPriority(priority);
		handledEntry.setUser(user);
		ScheduleDAO dao = new ScheduleDAO();
		dao.updateEntry(handledEntry);
		int index = eventModel.getEvents().indexOf(handledEvent);
		eventModel.getEvents().remove(index);
		eventModel.addEvent(new DefaultScheduleEvent(handledEntry.getTitle(), handledEntry.getStartAt(), handledEntry.getEndAt(), handledEntry));
		insertMode=false;
		return "";
	}
    
	/*
	 * 	Getters and setters
	 */
	public SScheduledCom getComm() {
		return comm;
	}

	public void setComm(SScheduledCom comm) {
		this.comm = comm;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public String getEmailCC() {
		return emailCC;
	}

	public void setEmailCC(String emailCC) {
		this.emailCC = emailCC;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailMessage() {
		return emailMessage;
	}

	public void setEmailMessage(String emailMessage) {
		this.emailMessage = emailMessage;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getSmsNumber() {
		return smsNumber;
	}

	public void setSmsNumber(String smsNumber) {
		this.smsNumber = smsNumber;
	}

	public String getSmsMessage() {
		return smsMessage;
	}

	public void setSmsMessage(String smsMessage) {
		this.smsMessage = smsMessage;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFiledir() {
		return filedir;
	}

	public void setFiledir(String filedir) {
		this.filedir = filedir;
	}

	public boolean isAttached() {
		return attached;
	}

	public void setAttached(boolean attached) {
		this.attached = attached;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public Date getDatefaxdiffere() {
		return datefaxdiffere;
	}

	public void setDatefaxdiffere(Date datefaxdiffere) {
		this.datefaxdiffere = datefaxdiffere;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getFileFax() {
		return fileFax;
	}

	public void setFileFax(String fileFax) {
		this.fileFax = fileFax;
	}

	public String getFiledirFax() {
		return filedirFax;
	}

	public void setFiledirFax(String filedirFax) {
		this.filedirFax = filedirFax;
	}

	public boolean isAttachedFax() {
		return attachedFax;
	}

	public void setAttachedFax(boolean attachedFax) {
		this.attachedFax = attachedFax;
	}

	public String getEmailCCI() {
		return emailCCI;
	}

	public void setEmailCCI(String emailCCI) {
		this.emailCCI = emailCCI;
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public List<String> getTos() {
		return tos;
	}

	public void setTos(List<String> tos) {
		this.tos = tos;
	}

	public List<String> getCcs() {
		return ccs;
	}

	public void setCcs(List<String> ccs) {
		this.ccs = ccs;
	}

	public List<ScheduleEntry> getUserSchedule() {
		return userSchedule;
	}

	public void setUserSchedule(List<ScheduleEntry> userSchedule) {
		this.userSchedule = userSchedule;
	}

	public ScheduleEntry getHandledEntry() {
		return handledEntry;
	}

	public void setHandledEntry(ScheduleEntry handledEntry) {
		this.handledEntry = handledEntry;
	}

	public ScheduleModel getEventModel() {
		return eventModel;
	}

	public void setEventModel(ScheduleModel eventModel) {
		this.eventModel = eventModel;
	}

	public boolean isInsertMode() {
		return insertMode;
	}

	public void setInsertMode(boolean insertMode) {
		this.insertMode = insertMode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Date getEndAt() {
		return endAt;
	}

	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}

	public boolean isRappel() {
		return rappel;
	}

	public void setRappel(boolean rappel) {
		this.rappel = rappel;
	}

	public Date getRappelAt() {
		return rappelAt;
	}

	public void setRappelAt(Date rappelAt) {
		this.rappelAt = rappelAt;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public ScheduleEvent getHandledEvent() {
		return handledEvent;
	}

	public void setHandledEvent(ScheduleEvent handledEvent) {
		this.handledEvent = handledEvent;
	}
	
}
