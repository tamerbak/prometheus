package fr.protogen.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.security.Md5;

public class SecurityPatch {
	public static void main(String[] args) {
		applymd5();
	}
	
	private static void applymd5(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select id, password from core_user";
		    Map<Integer, String> usrs = new HashMap<Integer, String>();
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next())
		    	usrs.put(new Integer(rs.getInt(1)), rs.getString(2));
		    rs.close();
		    ps.close();
		    for(Integer ID : usrs.keySet()){
		    	int id = ID.intValue();
		    	String pwd = Md5.encode(usrs.get(ID));
		    	
		    	sql = "update core_user set password=? where id=?";
		    	ps=cnx.prepareStatement(sql);
		    	ps.setString(1, pwd);
		    	ps.setInt(2, id);
		    	
		    	ps.execute();
		    	ps.close();
		    }
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
}
