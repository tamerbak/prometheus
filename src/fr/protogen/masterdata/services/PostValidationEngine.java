package fr.protogen.masterdata.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.engine.utils.ListKV;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class PostValidationEngine {
	public void executePostValidation(CAttribute a, int dbID, ListKV row, List<PairKVElement> titles){
		List<String> pas = new ArrayList<String>();
		
		String sql = "select id, type_action, arguments, formula from c_validation_action where attribute_id=? order by id asc";
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, a.getId());
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	String paLigne = rs.getString(2)+";;"+rs.getString(3)+";;"+rs.getString(4);
		    	pas.add(paLigne);
		    }
		    rs.close();
		    ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(String p : pas){
			String type = p.split(";;")[0];
			ValidationPostAction action = VPAFactory.getInstance().create(type);
			CBusinessClass e = a.getEntity();
			String args = p.split(";;")[1];
			String formula = p.split(";;")[2];
			action.executePostAction(dbID, a, e, formula, args, row, titles);
		}
		
	}
}
