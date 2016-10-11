package fr.protogen.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.protogen.masterdata.dbutils.DBUtils;

public class CorrectSequences {

	public static void main(String[] args) {
		List<String> tables = new ArrayList<String>();
		String tablesSQL = "select data_reference from c_businessclass";
		Connection cnx = null;
		try{
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(tablesSQL);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	tables.add(rs.getString(1));
		    }
		    rs.close();
		    ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		Map<String, Integer> ids = new HashMap<String, Integer>();
		for(String t : tables){
			String sql = "select max(pk_"+t+") from "+t;
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	ids.put(t, new Integer(rs.getInt(1)+1));
			    rs.close();
			    ps.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		for(String t : ids.keySet()){
			String sql = "ALTER SEQUENCE "+t+"_seq RESTART WITH "+ids.get(t)+";";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.execute();
			    ps.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
