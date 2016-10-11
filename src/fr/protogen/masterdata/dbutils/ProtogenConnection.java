package fr.protogen.masterdata.dbutils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ProtogenConnection {

	private static ProtogenConnection instance = null;
	public static final String url="jdbc:postgresql://localhost:5432/protogenmaster";//"jdbc:postgresql://ns389914.ovh.net:5432/protogenmaster";
	public static final String driver="org.postgresql.Driver";
	public static final String username="jakj";
	public static final String password="ENUmaELI5H";
	private Connection cnx ;
	
	public static synchronized ProtogenConnection getInstance(){
		if(instance == null)
		{
			try {
				instance = new 	ProtogenConnection();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return instance;
	}
	
	private ProtogenConnection() throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");

		cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
	}
	
	
	public synchronized Connection getConnection() {
		if(cnx==null){
			try{
				Class.forName("org.postgresql.Driver");

				cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			}catch(Exception e){
				
			}
		}
			
		return cnx;
	}
}
