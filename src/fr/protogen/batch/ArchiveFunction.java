package fr.protogen.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dbutils.DBUtils;

public class ArchiveFunction {

	public static void main(String[] args) {
		List<String> tables = new ArrayList<String>();
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    String sql = "select data_reference from c_businessclass";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    tables = new ArrayList<String>();
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

			    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    
			    String sql = "alter table "+t+" add column dirty character (1) not null default 'N'";
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.execute();
			    ps.close();
			    cnx.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
	}

}
