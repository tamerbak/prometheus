package fr.protogen.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dbutils.DBUtils;

public class SequenceCorrector {

	public static void main(String[] args) {
		updateToTwoMillions();

	}
	
	public static void updateToTwoMillions(){
		List<String> tables = new ArrayList<String>();
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(
		    		"jdbc:postgresql://localhost:5432/avocat"
		    		,"jakj", "ENUmaELI5H");
		    String sql = "select data_reference from c_businessclass";
		      
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next())
		    	tables.add(rs.getString(1));
		    rs.close();
		    ps.close();
		    cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(String t : tables){
			try {
				Class.forName("org.postgresql.Driver");

				Connection cnx = DriverManager.getConnection(
			    		"jdbc:postgresql://localhost:5432/avocat"
			    		,"jakj", "ENUmaELI5H");
			    String sql = "select setval('"+t+"_seq',200000000)";
			     
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    
			    ResultSet rs = ps.executeQuery();
			    rs.next();
			    System.out.println("Traitement de : "+t+"_seq ("+rs.getInt(1)+")");
			    
			    rs.close();
			    ps.close();
			    cnx.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
	}

}
