package fr.protogen.connector.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import fr.protogen.connector.model.AmanToken;
import fr.protogen.connector.session.ClientApplications;
import fr.protogen.connector.session.WebSessionManager;
import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.security.Md5;

@Path("/amanjs")
public class AmanJSService {
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authentifier(InputStream incomingData){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		AmanToken amanToken = new JSONDeserializer<AmanToken>().deserialize(jsonText);
		//	Check AppID
		if(!amanToken.getAppId().equals(ClientApplications.CRM) && !amanToken.getAppId().equals(ClientApplications.GED)){
			amanToken.setStatus("APP INCONUE");
			String res = new JSONSerializer().serialize(amanToken);
			return Response.status(200).entity(res).build();
		}
		
		//	Authenticate User
		UserDAOImpl service = new UserDAOImpl(); 
		CoreUser u = service.getUser(amanToken.getUsername(), Md5.encode(amanToken.getPassword()));
		
		if(u==null){
			amanToken.setStatus("ECHEC");
			String res = new JSONSerializer().serialize(amanToken);
			return Response.status(200).entity(res).build();
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
		
		// return HTTP response 200 in case of success
		String res = new JSONSerializer().serialize(amanToken);
		return Response.status(200).entity(res).build();
	}
	
	@GET
	@Path("/verify")
	@Produces(MediaType.TEXT_PLAIN)
	public Response verifyRESTService(InputStream incomingData) {
		String result = "Test WS Successfully started..";
 
		// return HTTP response 200 in case of success
		return Response.status(200).entity(result).build();
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
