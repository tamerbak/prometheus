package fr.protogen.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import fr.protogen.masterdata.dbutils.DBUtils;

public class CorrectContract {

	public static void main(String[] args) {
		Connection cnx = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
		String sql = "select c.pk_user_contrat, o.titre from user_contrat c, user_offre_entreprise o where c.fk_user_offre_entreprise=o.pk_user_offre_entreprise";
		Map<Integer, String> contractOffers = new HashMap<Integer, String>();
		try{
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    ps = cnx.prepareStatement(sql);
		    rs = ps.executeQuery();
		    while(rs.next()){
		    	contractOffers.put(rs.getInt(1), rs.getString(2));
		    }
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{rs.close();}catch(Exception exc){}
			try{ps.close();}catch(Exception exc){}
			try{cnx.close();}catch(Exception exc){}
		}
		
		
		sql = "update user_contrat set titre=? where pk_user_contrat=? ";
		for(Integer id : contractOffers.keySet()){
			try{
				cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    ps = cnx.prepareStatement(sql);
			    ps.setString(1, contractOffers.get(id));
			    ps.setInt(2, id.intValue());
			    ps.execute();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{ps.close();}catch(Exception exc){}
				try{cnx.close();}catch(Exception exc){}
			}
		}
	}

}
