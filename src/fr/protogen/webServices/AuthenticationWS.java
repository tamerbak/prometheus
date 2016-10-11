/**
 * 
 */
package fr.protogen.webServices;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.security.Md5;

/**
 * @author developer
 *
 */
@Path("/authentication")
public class AuthenticationWS {
	
	public static final String TOKEN_STRING = "cZfWk4UrWUoMSjb";
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public String getUser(@QueryParam("login") String login, @QueryParam("password") String password, @QueryParam("token") String hashToken) {
		if(Md5.encode(TOKEN_STRING).equals(hashToken)) {
			UserDAOImpl dao = new UserDAOImpl();
			
			CoreUser user = dao.getUser(login, Md5.encode(password));
			Gson gson = new GsonBuilder().create();
			return gson.toJson(user);
		} 
		
		return "{\"error\":\"authentication failed\"}";
		
	}
}
