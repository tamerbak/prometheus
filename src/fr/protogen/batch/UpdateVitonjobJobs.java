package fr.protogen.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class UpdateVitonjobJobs {

	public static void main(String[] args) {
		String csvSource = "/home/jakjoud/Projects/VitOnJob/jobs.csv";
		List<String> fileLines = new ArrayList<String>();
		List<String> errorStack = new ArrayList<String>();
		int index, size;
		index = 0;
		size = 0;
		try {
			InputStream is = new FileInputStream(csvSource);
			fileLines = IOUtils.readLines(is);
			index = 1;
			size = fileLines.size();
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Connection cnx = null;
		try{
			Class.forName("org.postgresql.Driver");
		    cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(String l : fileLines){
			System.out.println("JOB : "+index+"/"+size);
			index++;
			String[] cols = l.split(";");
			int id = Integer.parseInt(cols[0]);
			String metier = cols[1];
			String job = cols[2];
			String code = cols[3].trim();
			
			try{
				
			    String sql = "update user_metier set libelle=? where pk_user_metier in (select fk_user_metier from user_job where pk_user_job=?)";
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, metier);
			    ps.setInt(2, id);
			    ps.execute();
			    ps.close();
			    
			    sql = "update user_job set libelle = ?, code_insee=? where pk_user_job=?";
			    ps = cnx.prepareStatement(sql);
			    ps.setString(1, job);
			    ps.setString(2, code);
			    ps.setInt(3, id);
			    ps.execute();
			    ps.close();
			    
			}catch(Exception e){
				errorStack.add(id+" - "+e.getMessage());
			}
		}
		
		try{
			cnx.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(errorStack.size() == 0)
			System.out.println("No ERRORS");
		else
			System.out.println("ERRORS");
		
		for(String e : errorStack){
			System.out.println("\t"+e);
		}
	}

}
