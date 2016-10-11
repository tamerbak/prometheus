package fr.protogen.connector.listener;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.AmanToken;
import fr.protogen.connector.session.ClientApplications;
import fr.protogen.connector.session.WebSessionManager;
import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.security.Md5;

@Path("/aman")
public class AmanService {
	
	
	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public String authentifier(String token){
		
		XStream parser = new XStream();
		AmanToken amanToken = (AmanToken) parser.fromXML(token);
		
		//	Check AppID
		if(!amanToken.getAppId().equals(ClientApplications.CRM) && !amanToken.getAppId().equals(ClientApplications.GED)){
			amanToken.setStatus("APP INCONUE");
			return parser.toXML(amanToken);
		}
		
		//	Authenticate User
		UserDAOImpl service = new UserDAOImpl(); 
		CoreUser u = service.getUser(amanToken.getUsername(), Md5.encode(amanToken.getPassword()));
		
		if(u==null){
			amanToken.setStatus("ECHEC");
			return parser.toXML(amanToken);
		}
		
		amanToken.setBeanId(u.getBoundEntity());
		
		//	Create Session Token
		String sessionID = WebSessionManager.getInstance().authenticate(amanToken.getAppId());
		
		//	Return user Token
		amanToken.setSessionId(sessionID);
		amanToken.setId(u.getId());
		amanToken.setStatus("SUCCES");
		amanToken.setNom(u.getFirstName()+" "+u.getLastName());
		amanToken.setUsername("");
		amanToken.setPassword("");
		
		return parser.toXML(amanToken);
	}
	
//	@POST
//	@Produces(MediaType.TEXT_XML)
//	public String test(){
//		
//		XStream parser = new XStream();
//		AmanToken amanToken = new AmanToken();
//		amanToken.setAppId(ClientApplications.CRM);
//		amanToken.setUsername("jakjoud@gmail.com");
//		amanToken.setPassword("aaaa");
//		String token=parser.toXML(amanToken);
//		//	Check AppID
//		if(!amanToken.getAppId().equals(ClientApplications.CRM) && !amanToken.getAppId().equals(ClientApplications.GED))
//			return token;
//		
//		//	Authenticate User
//		UserDAOImpl service = new UserDAOImpl(); 
//		CoreUser u = service.getUser(amanToken.getUsername(), amanToken.getPassword());
//		
//		if(u==null)
//			return token;
//		
//		//	Create Session Token
//		String sessionID = WebSessionManager.getInstance().authenticate(amanToken.getAppId());
//		
//		//	Return user Token
//		amanToken.setSessionId(sessionID);
//		
//		return parser.toXML(amanToken);
//	}
}
