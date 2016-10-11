package fr.protogen.masterdata.DAO;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import fr.protogen.masterdata.dbutils.ProtogenConnection;

@SuppressWarnings("serial")
public class LocalizationEngine implements Serializable {

	public Map<String, String> loadApplicationTerminology(String appkey, String code){
		Map<String, String> terminology = new HashMap<String, String>();
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "select app_key, val from application_translation where code_lang=? and appkey=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setString(2, appkey);
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	terminology.put(rs.getString(1), rs.getString(2));
		    }
		    rs.close();
		    ps.close();
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		
		return terminology;
	}
	
	public String rubriqueTranslate(String title, int id, String code){
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select val from c_rubrique_trans where code_lang=? and id_rubrique=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setInt(2, id);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	title = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return title;
	}

	public String menuTranslate(String title, int id, String code){
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select val from c_menu_trans where code_lang=? and id_menu=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setInt(2, id);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	title = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return title;
	}

	public String windowTranslate(String title, int id, String code){
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select val from c_window_trans where code_lang=? and id_window=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setInt(2, id);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	title = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return title;
	}

	public String attributeTranslate(String title, int id, String code){
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select val from c_attribute_trans where code_lang=? and id_attribute=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setInt(2, id);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	title = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return title;
	}

	public String entityTranslate(String title, int id, String code){
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select val from c_table_trans where code_lang=? and id_class=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setInt(2, id);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	title = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return title;
	}

	public String actionTranslate(String title, int id, String code){
		
		try{
			
	
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select val from c_action_trans where code_lang=? and id_action=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setInt(2, id);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	title = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return title;
	}

	public String documentTranslate(String title, int id, String code){
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select val from c_document_trans where code_lang=? and id_document=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setInt(2, id);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	title = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return title;
	}

	public String viewTranslate(String title, int id, String code){
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select val from c_view_trans where code_lang=? and id_view=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, code);
		    ps.setInt(2, id);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	title = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return title;
	}
}
