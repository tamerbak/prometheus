package fr.protogen.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dbutils.DBUtils;

public class NationalitePaysCopyBatch {

	public static void main(String[] args) {
		Connection cnx = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String sql = "update user_nationalite set dirty='Y'";
		try{
			ps = cnx.prepareStatement(sql);
			ps.execute();
		}catch(Exception exc){
			exc.printStackTrace();
		}finally{
			if(ps != null)
				try{
					ps.close();
				}catch(Exception e){
					e.printStackTrace();
				}
		}
		
		sql="select nom from user_pays";
		List<String> pays = new ArrayList<String>();
		try{
			ps = cnx.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				pays.add(rs.getString(1));
			}
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}finally{
			if(rs != null)
				try{
					rs.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			if(ps != null)
				try{
					ps.close();
				}catch(Exception e){
					e.printStackTrace();
				}
		}
		
		sql = "insert into user_nationalite (libelle) values (?)";
		for(String p : pays){
			try{
				ps = cnx.prepareStatement(sql);
				ps.setString(1, p);
				ps.execute();
			}catch(Exception exc){
				exc.printStackTrace();
			}finally{
				if(ps != null)
					try{
						ps.close();
					}catch(Exception e){
						e.printStackTrace();
					}
			}
		}
		
		try{
			cnx.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
