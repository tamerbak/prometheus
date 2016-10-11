package fr.protogen.importData;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBUtils {
	
	public static final String url="jdbc:postgresql://localhost:5432/Test";//"jdbc:postgresql://ns389914.ovh.net:5432/protogenmaster";
	public static final String driver="org.postgresql.Driver";
	public static final String username="postgres";
	public static final String password="admin";
	public static final DataSource ds = getDataSource();
	
	
	public static DataSource getDataSource(){
		Context ctx;
		try {
			ctx = new InitialContext();
		
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/postgres");
		
			return ds;
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return null;
	}
}
