package fr.protogen.connector.listener;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.protogen.connector.session.WebSessionManager;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;


@Path("/sql")
public class QueryService {
	@SuppressWarnings("resource")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response authentifier(String sql){
		String res = "{}";
		Connection cnx = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			ps = cnx.prepareStatement(sql);
		    
		    if(sql.trim().toLowerCase().startsWith("select") || sql.trim().toLowerCase().contains("returning")){
			    rs = ps.executeQuery();
			    ResultSetMetaData rsmd = rs.getMetaData();
			    res = "{\"status\":\"success\", \"data\":[";
			    boolean nonEmpty = false;
			    while(rs.next()){
			    	nonEmpty = true;
			    	res = res+"{";
			    	String obj = "";
			    	for(int i = 1 ; i<=rsmd.getColumnCount();i++){
			    		obj = obj+"\""+rsmd.getColumnLabel(i)+"\":\""+rs.getObject(i)+"\",";
			    	}
			    	if(obj.length()>0)
			    		obj = obj.substring(0, obj.length()-1);
			    	res = res+obj+"},";
			    }
			    if(nonEmpty)
			    	res = res.substring(0,res.length()-1);
			    res = res + "]}";
			    rs.close();
			    ps.close();
		    } else {
		    	ps.execute();
		    	res = "{\"status\":\"success\"}";
		    	ps.close();
		    }
		}catch(Exception exc){
			exc.printStackTrace();
			String message = exc.getMessage().replaceAll("\"", "").replace("\n", "");
			res = "{\"status\":\"failure\", \"error\":\""+message+"\"}";
		}finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
            if (ps != null) try { ps.close(); } catch (SQLException e) {e.printStackTrace();}
            if (cnx != null) try { cnx.close(); } catch (SQLException e) {e.printStackTrace();}
        }
		
		return Response.status(200).entity(res).build();
	}
	
}
