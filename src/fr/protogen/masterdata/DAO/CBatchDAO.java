package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CBatch;
import fr.protogen.masterdata.model.CBatchArgument;
import fr.protogen.masterdata.model.CBatchUnit;
import fr.protogen.masterdata.model.CBatchUnitType;

public class CBatchDAO {
	public CBatch loadBatch(int id){
		String sql = "select nom, code, appkey from c_batch where id=?";
		CBatch result = null;
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				result = new CBatch(id, rs.getString(1), rs.getString(2), new ArrayList<CBatchUnit>(), new ArrayList<CBatchArgument>());
			}
			rs.close();
			ps.close();
			if(result == null)
				return null;
			
			sql = "select id, libelle, code, value from c_batch_argument where batch_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while(rs.next()){
				CBatchArgument a = new CBatchArgument(rs.getInt(1), rs.getString(2), rs.getString(4), rs.getString(3));
				result.getArguments().add(a);
			}
			rs.close();
			ps.close();
			
			sql = "select id, nom, instructionsmodel, type_id from c_batch_unit where batch_id=? order by unit_order asc";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while(rs.next()){
				CBatchUnit u = new CBatchUnit(rs.getInt(1), rs.getString(2), rs.getString(3), new CBatchUnitType(rs.getInt(4), ""));
				u.setBatch(result);
				result.getUnits().add(u);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return result;
	}
}
