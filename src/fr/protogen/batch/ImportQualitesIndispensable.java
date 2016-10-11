package fr.protogen.batch;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class ImportQualitesIndispensable {

	public static void main(String[] args) {
		String fileName = "D:\\tmp\\indispensables.csv";
		Connection cnx = null;
		PreparedStatement ps = null;
		
		List<String> lines = new ArrayList<String>();
		try{
			lines = IOUtils.readLines(new FileInputStream(fileName));
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		System.out.println("INSEE LINES : "+lines.size());
		String sql = "insert into user_indispensable (libelle, type) values (?, ?)";
		int index = 1;
		for(String l : lines){
			String[] cols = l.split(";");
			try{
				ps = cnx.prepareStatement(sql);
				ps.setString(1, cols[0].trim());
				ps.setString(2, cols[1].trim());
				ps.execute();
				System.out.println(index+"/"+lines.size());
				index++;
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(ps != null){try { ps.close();} catch (SQLException e) {e.printStackTrace();}}
			}
		}
		
		try{
			cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}

	}

}
