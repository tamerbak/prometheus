package fr.protogen.batch;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.poi.util.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class JobExport {

	public static void main(String[] args) {
		String sql = "select j.pk_user_job, j.libelle, m.libelle "
				+ "from user_job j, user_metier m "
				+ "where j.fk_user_metier = m.pk_user_metier";
		String csvFile = "/home/jakjoud/Projects/VitOnJob/webcrawler/jobs.csv";
		String csv = "\"id\",\"Metier\",\"Job\"";
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	String line = rs.getInt(1)+",\""+rs.getString(3)+"\",\""+rs.getString(2)+"\"";
		    	System.out.println(line);
		    	csv = csv+"\n"+line;
		    }
		    rs.close();
		    ps.close();
		    cnx.close();
		    OutputStream os = new FileOutputStream(csvFile);
		    org.apache.commons.io.IOUtils.write(csv, os);
		    os.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

}
