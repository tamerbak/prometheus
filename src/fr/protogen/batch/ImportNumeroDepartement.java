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

public class ImportNumeroDepartement {

	public static void main(String[] args) {
		String fileName = "D:\\tmp\\departements.csv";
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
		int index = 1;
		for(String l : lines){
			String[] c = l.split(",");
			String num = c[0];
			String nom = c[1];
			String sql = "update user_departement set numero=? where lower_unaccent(nom)=lower_unaccent(?)";
			try{
				ps = cnx.prepareStatement(sql);
				ps.setString(1, num);
				ps.setString(2, nom);
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
