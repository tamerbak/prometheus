package fr.protogen.connector.listener;


import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class CORSFilter implements ContainerResponseFilter  {

	@Override
	public ContainerResponse filter(ContainerRequest req, ContainerResponse cres) {
		/*cres.getHeaders().add("Access-Control-Allow-Origin", "*");
	    cres.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
	    cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
	    cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
	    cres.getHeaders().add("Access-Control-Max-Age", "1209600");*/
		ResponseBuilder corsResponseBuilder = Response.fromResponse(cres.getResponse());
		
        corsResponseBuilder.header("Access-Control-Allow-Origin", "*")
        
        .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
        
        .header("Access-Control-Max-Age", "1209600")
        
        .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
		
        cres.setResponse(corsResponseBuilder.build());
        
		return cres;
	}
	
   
}
