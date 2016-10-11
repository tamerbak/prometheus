package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CCallout;

public class CalloutDAO {
	public CCallout getCallout(CCallout callout){
		String sql = "select fichier from c_callout where callout_key=? order by id desc limit 1";
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setInt(1, callout.getId());
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				callout.setFile(rs.getBytes(1));
			}
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return callout;
	}
}
