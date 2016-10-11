package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CDataHistory;
import fr.protogen.masterdata.model.CInstanceHistory;

public class HistoryDataAccess {

	public CDataHistory checkForHistory(CBusinessClass entity){
		CDataHistory result = new CDataHistory();
		result.setEntity(entity);
		result.setId(0);
		
		String sql = "select id, attribute from c_data_history where entity=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, entity.getId());
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				result.setId(rs.getInt(1));
				result.setReference(new CAttribute());
				result.getReference().setId(rs.getInt(2));
			}
			
			rs.close();
			ps.close();
			
			if(result.getId() == 0)
				return result;
			
			sql = "select id, bean, date_debut, date_fin, attribute_id from c_instance_history where history=? and courant='Y'";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, result.getId());
			rs = ps.executeQuery();
			result.setCourant(new HashMap<Integer, CInstanceHistory>());
			
			while(rs.next()){
				CInstanceHistory i = new CInstanceHistory();
				
				i.setId(rs.getInt(1));
				i.setBean(rs.getInt(2));
				i.setDateDebut(rs.getDate(3));
				if(rs.getDate(4)!=null)
					i.setDateFin(rs.getDate(4));
				
				Integer I = new Integer(rs.getInt(5));
				
				result.getCourant().put(I, i);
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}

	/**
	 * We detected the presence of an old version onf this row 
	 * we are now going to declare it as obsolete
	 * @param courant
	 */
	public void renderObsolete(CInstanceHistory courant, java.util.Date newDate) {
		
		String sql = "update c_instance_history set courant='N', date_fin=? where id=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			java.util.Date d = newDate;
			ps.setDate(1, new Date(d.getTime()));
			ps.setInt(2, courant.getId());
			
			ps.execute();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	/**
	 * We created a new version of a row, we are going to mark it as the current one
	 * @param courant
	 * @param h
	 * @param dbID
	 * @param histoStart 
	 */
	public void historise(CInstanceHistory courant, CDataHistory h, int dbID, int idAttribute, java.util.Date histoStart) {
		String sql = "insert into c_instance_history (history, bean, date_debut, courant, attribute_id) values "
				+ "(?, ?, ?, ?, ?)";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			java.util.Date d = histoStart!=null?histoStart:new java.util.Date();
			ps.setInt(1,h.getId());
			ps.setInt(2, dbID);
			ps.setDate(3, new Date(d.getTime()));
			ps.setString(4,"Y");
			ps.setInt(5,idAttribute);
			ps.execute();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public CInstanceHistory getHistoryInstance(CDataHistory h, int id,
			int histoReference) {
		String sql = "select id, date_debut, date_fin, courant from c_instance_history "
				+ "	where history=? and bean=? and attribute_id=?";
		CInstanceHistory result = new CInstanceHistory();
		result.setId(0);
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, h.getId());
			ps.setInt(2, id);
			ps.setInt(3, histoReference);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				result.setId(rs.getInt(1));
				result.setDateDebut(rs.getDate(2));
				result.setDateFin(rs.getDate(3));
				result.setCourant(rs.getString(4).equals("Y"));
				result.setBean(id);
			}
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
}
