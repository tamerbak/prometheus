package fr.protogen.dataload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.thoughtworks.xstream.XStream;

import fr.protogen.engine.utils.FormContext;
import fr.protogen.engine.utils.UIControlsLine;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreUser;

public class ContextPersistence {
	public void persist(CoreUser user, CWindow form, UIControlsLine controls)
	{
		XStream engine = new XStream();
		String data = engine.toXML(controls);
		
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "delete from c_temporary_form_data where id_user=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ps.execute();
			ps.close();
			
			sql = "insert into c_temporary_form_data (id_user,id_form,form_data) values (?,?,?)";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ps.setInt(2, form.getId());
			ps.setString(3, data);
			
			ps.execute();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public FormContext loadControls(CoreUser user)
	{
		XStream engine = new XStream();
		FormContext result = new FormContext();
		String sql = "select id_form,form_data from c_temporary_form_data where id_user=?";
		boolean found=false;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				found = true;
				result.setForm(new CWindow());
				result.getForm().setId(rs.getInt(1));
				String data = rs.getString(2);
				UIControlsLine lines = (UIControlsLine)engine.fromXML(data);
				result.setControls(lines);
			}
			
			rs.close();
			ps.close();
			
			sql = "delete from c_temporary_form_data where id_user=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ps.execute();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(found)
			return result;
		else
			return null;
	}
	
	public void mark(CoreUser user){
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "delete from c_temporary_form_data where id_user=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ps.execute();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
