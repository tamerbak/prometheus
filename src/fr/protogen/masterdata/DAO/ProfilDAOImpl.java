package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import fr.protogen.engine.utils.ProtogenKeyGenerator;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CoreDataConstraint;
import fr.protogen.masterdata.model.CoreProfil;
import fr.protogen.masterdata.model.CoreUser;

public class ProfilDAOImpl {

	public int persist(CoreProfil p) {
		String uid="";
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "insert into core_profil (code,libelle,date_effet,date_fin,id_role)" +
		    		"		VALUES (?,?,?,?,?)";
		    
		    uid = ProtogenKeyGenerator.getInstance().generateKey(); 
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, p.getCode());
		    ps.setString(2, p.getLibelle());
		    ps.setDate(3, new Date(p.getDateEffet().getTime()));
		    ps.setDate(4, new Date(p.getDateFin().getTime()));
		    ps.setInt(5, p.getRole().getId());
		    
		    ps.execute();
		    ps.close();
		    
		    int id = getProfil(p);
		    return id;
		    
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}	
	}
	
	private int getProfil(CoreProfil p){
		try {
			int id = 0;
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			String sql = "select p.id from core_profil p where p.code ='"+p.getCode()+"' and p.libelle='"+p.getLibelle()+"' and p.date_effet = '"+ p.getDateEffet()+"' and p.date_fin = '"+ p.getDateFin()+"' and p.id_role = '"+ p.getRole().getId()+"'";
			ResultSet rs = st.executeQuery(sql);
			if(rs.next()){
				id = rs.getInt(1);
			} 
			
			rs.close();
			st.close();
			return id;
		}
		    catch(Exception ex){
		    	ex.printStackTrace();
		    	return 0;
		    }
		    
	}
	
	public void persist(CoreDataConstraint p) {
		String uid="";
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "insert into core_data_constraint (id_role,entity,bean_id)" +
		    		"		VALUES (?,?,?)";
		    
		    uid = ProtogenKeyGenerator.getInstance().generateKey(); 
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, p.getRoleId());
		    ps.setInt(2, p.getEntity().getId());
		    ps.setInt(3, p.getBeanId());
		    
		    ps.execute();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
}