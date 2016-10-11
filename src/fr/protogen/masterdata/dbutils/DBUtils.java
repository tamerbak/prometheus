package fr.protogen.masterdata.dbutils;

public class DBUtils {
	
	//public static String url="jdbc:postgresql://localhost:5432/"+FacesContext.getCurrentInstance().getExternalContext().getInitParameter("database"); //"jdbc:postgresql://ns389914.ovh.net:5432/protogenmaster";
	//public static String url="jdbc:postgresql://localhost:5432/vitonjobdb";
	//public static String url="jdbc:postgresql://ns389914.ovh.net:9990/vitonjobdb";
	/*public static String url="jdbc:postgresql://vitonjob.cjyvilyeiwcj.us-west-2.rds.amazonaws.com:5432/vitonjobdb";
	public static final String driver="org.postgresql.Driver";
	public static final String username="jakj";
	public static final String password="ENUmaELI5H";
	*/
	public static final String server = System.getProperty("RDS_HOSTNAME");
	public static final String db = System.getProperty("RDS_DB_NAME");
	public static final String username="jakj";
	public static final String password="ENUmaELI5H";
	public static final String url="jdbc:postgresql://"+server+":5432/"+db;
}
