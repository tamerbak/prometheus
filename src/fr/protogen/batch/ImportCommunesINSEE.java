package fr.protogen.batch;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class ImportCommunesINSEE {
	public static void main(String[] args) {
		correct();		
	}
	
	private static void importNew(){
		String fileName = "D:\\tmp\\insee.csv";
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
		String sql = "insert into user_commune (nom, code_insee) values (?, ?)";
		int index = 1;
		for(String l : lines){
			String[] cols = l.split(";");
			try{
				ps = cnx.prepareStatement(sql);
				ps.setString(1, cols[0].trim());
				ps.setString(2, formatINSEE(cols[3].trim()));
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
	
	private static void correct(){
		String fileName = "D:\\tmp\\insee.csv";
		Connection cnx = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
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
			String[] cols = l.split(";");
			String comm = cols[0];
			String cp = cols[1];
			if(cp.length()<5)
				cp = "0"+cp;
			String dept = cols[2];
			String insee = cols[3];
			if(insee.length()<5)
				insee = "0"+insee;
			
			int idcp=0;
			int iddept = 0;
			
			String cpSql = "select pk_user_code_postal from user_code_postal where code=?";
			try{
				ps = cnx.prepareStatement(cpSql);
				ps.setString(1, cp);
				rs = ps.executeQuery();
				if(rs.next())
					idcp = rs.getInt(1);
			}catch(Exception exc){
				exc.printStackTrace();
			}finally{
				if(ps != null){try { ps.close();} catch (SQLException e) {e.printStackTrace();}}
				if(rs != null){try { rs.close();} catch (SQLException e) {e.printStackTrace();}}
			}
			if(idcp == 0){
				cpSql = "insert into user_code_postal (code) values (?) returning pk_user_code_postal ";
				try{
					ps = cnx.prepareStatement(cpSql);
					ps.setString(1, cp);
					rs = ps.executeQuery();
					if(rs.next())
						idcp = rs.getInt(1);
				}catch(Exception exc){
					exc.printStackTrace();
				}finally{
					if(ps != null){try { ps.close();} catch (SQLException e) {e.printStackTrace();}}
					if(rs != null){try { rs.close();} catch (SQLException e) {e.printStackTrace();}}
				}
			}
			
			String depSql = "select pk_user_departement from user_departement where nom=?";
			try{
				ps = cnx.prepareStatement(depSql);
				ps.setString(1, dept);
				rs = ps.executeQuery();
				if(rs.next())
					iddept = rs.getInt(1);
			}catch(Exception exc){
				exc.printStackTrace();
			}finally{
				if(ps != null){try { ps.close();} catch (SQLException e) {e.printStackTrace();}}
				if(rs != null){try { rs.close();} catch (SQLException e) {e.printStackTrace();}}
			}
			if(iddept== 0){
				depSql = "insert into user_departement (nom) values (?) returning pk_user_departement ";
				try{
					ps = cnx.prepareStatement(depSql);
					ps.setString(1, dept);
					rs = ps.executeQuery();
					if(rs.next())
						iddept = rs.getInt(1);
				}catch(Exception exc){
					exc.printStackTrace();
				}finally{
					if(ps != null){try { ps.close();} catch (SQLException e) {e.printStackTrace();}}
					if(rs != null){try { rs.close();} catch (SQLException e) {e.printStackTrace();}}
				}
			}
			
			String sql = "update user_commune set fk_user_departement=?, fk_user_code_postal=? where nom=? and code_insee=?";
			
			try{
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, iddept);
				ps.setInt(2, idcp);
				ps.setString(3, comm);
				ps.setString(4, insee);
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
	
	private static String formatINSEE(String insee) {
		int l = insee.length();
		if(l == 4)
			insee = "0"+insee;
		return insee;
	}
}
