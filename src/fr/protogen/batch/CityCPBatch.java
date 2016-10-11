package fr.protogen.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class CityCPBatch {

	public static void main(String[] args) {
		//exportData();
		importData();

	}

	private static void exportData(){
		
		try {
			FileOutputStream cps = new FileOutputStream(new File("cps"));
			String scps="";
			String svilles="";
			FileOutputStream villes = new FileOutputStream(new File("villes"));
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select pk_user_code_postal, code_postale, fk_user_pays from user_code_postal";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	scps = scps+"\n"+rs.getInt(1)+":"+rs.getString(2)+":"+rs.getInt(3);
		    }
		    if(scps.length()>0)
		    	scps = scps.substring(1);
		    rs.close();
		    ps.close();
		    IOUtils.write(scps.getBytes(), cps);
		    cps.close();
		    
		    sql = "select pk_user_ville, nom_ville, fk_user_code_postal from user_ville";
		    ps = cnx.prepareStatement(sql);
		    rs = ps.executeQuery();
		    while(rs.next()){
		    	svilles = svilles+"\n"+rs.getInt(1)+":"+rs.getString(2)+":"+rs.getInt(3);
		    }
		    if(svilles.length()>0)
		    	svilles = svilles.substring(1);
		    rs.close();
		    ps.close();
		    IOUtils.write(svilles.getBytes(), villes);
		    villes.close();
		    
		    cnx.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	private static void importData(){
		try {
			FileInputStream cps = new FileInputStream(new File("cps"));
			List<String> scps=IOUtils.readLines(cps);
			FileInputStream villes = new FileInputStream(new File("villes"));
			List<String> svilles=IOUtils.readLines(villes);
			
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "insert into core_code_postal (id,code_postal,pays) values (?,?,?)";
		    for(String c : scps){
		    	String id = c.split(":")[0];
		    	String cp = c.split(":")[1];
		    	String p = c.split(":")[2];
		    	
		    	PreparedStatement ps = cnx.prepareStatement(sql);
		    	ps.setInt(1, Integer.parseInt(id));
		    	ps.setString(2, cp);
		    	ps.setInt(3, Integer.parseInt(p));
		    	
		    	ps.execute();
		    	ps.close();
		    }
		    
		    sql = "insert into core_ville (id,ville,code_postal) values (?,?,?)";
		    for(String c : svilles){
		    	String id = c.split(":")[0];
		    	String cp = c.split(":")[1];
		    	String p = c.split(":")[2];
		    	
		    	PreparedStatement ps = cnx.prepareStatement(sql);
		    	ps.setInt(1, Integer.parseInt(id));
		    	ps.setString(2, cp);
		    	ps.setInt(3, Integer.parseInt(p));
		    	
		    	ps.execute();
		    	ps.close();
		    }
		    
		    cps.close();
		    villes.close();
		    cnx.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
}
