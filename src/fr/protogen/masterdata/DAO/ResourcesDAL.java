package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.GResource;

public class ResourcesDAL {
	private static ResourcesDAL instance = null;
	public static synchronized ResourcesDAL getInstance(){
		if(instance == null)
			instance = new ResourcesDAL();
		return instance;
	}
	private ResourcesDAL(){}
	
	public GResource loadResourceByKey(String key){
		GResource r = new GResource();
		String sql = "select id, res_name, res_type, fichier from g_resource where res_key=?";
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, key);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				r.setKey(key);
				r.setId(rs.getInt(1));
				r.setName(rs.getString(2));
				r.setType(rs.getString(3));
				r.setFile(rs.getBytes(4));
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return r;
	}
	
	public GResource loadResourceById(int id){
		GResource r = new GResource();
		String sql = "select res_key, res_name, res_type, fichier from g_resource where id=?";
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				r.setKey(rs.getString(1));
				r.setId(id);
				r.setName(rs.getString(2));
				r.setType(rs.getString(3));
				r.setFile(rs.getBytes(4));
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return r;
	}
}
