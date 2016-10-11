package fr.protogen.engine.control;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
 
import com.twilio.sdk.client.TwilioCapability;
import com.twilio.sdk.client.TwilioCapability.DomainException;
public class TwilioServlet extends HttpServlet {
	 
	public static final String ACCOUNT_SID = "ACb10c2d472d258d2cbdeb73e9a3678404";
    public static final String AUTH_TOKEN = "d16d2f8ab0e2f5930543c99d79808c43";
   
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 
        // This is a special Quickstart application sid - or configure your own
        // at twilio.com/user/account/apps
        String applicationSid = "AP9bd35d9d21b91c3ee31d2409ea570859";
 
        TwilioCapability capability = new TwilioCapability(ACCOUNT_SID, AUTH_TOKEN);
        capability.allowClientOutgoing(applicationSid);
 
        String token = null;
        try {
            token = capability.generateToken();
        } catch (DomainException e) {
            e.printStackTrace();
        }
        // Forward the token information to a JSP view
        response.setContentType("text/html");
        request.setAttribute("token", token);
        RequestDispatcher view = request.getRequestDispatcher("protogen-sendappel.xhtml");
        view.forward(request, response);
    }
}