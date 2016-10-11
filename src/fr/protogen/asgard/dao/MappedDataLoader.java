package fr.protogen.asgard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.faces.context.FacesContext;

import fr.protogen.asgard.model.masterdata.AsgardMappingModel;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class MappedDataLoader {
	
	
	public AsgardMappingModel loadAsgardMap(String appkey){
		AsgardMappingModel model = new AsgardMappingModel();
		
		try{
		    Connection cnx=((ProtogenConnection)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("PROTOGEN_CONNECTION")).getConnection();
		    String sql ="select stream from bp_asgard_mapping where appkey=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, appkey);
		    ResultSet rs = ps.executeQuery();
		    String smod = "";
		    if(rs.next())
		    	smod = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    model = XmlEngine.getInstance().deserialize(smod);
		}catch(Exception e){
			e.printStackTrace();
		} 
		return model;
	}
}
