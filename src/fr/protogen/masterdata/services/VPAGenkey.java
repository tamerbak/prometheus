package fr.protogen.masterdata.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Random;

import fr.protogen.engine.utils.ListKV;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class VPAGenkey implements ValidationPostAction {

	@Override
	public void executePostAction(int dbID, CAttribute attribute, CBusinessClass entity, String formula,
			String arguments, ListKV row,List<PairKVElement> titles) {
		
		int nbDigits = Integer.parseInt(arguments.split(",")[0]);
		int col = Integer.parseInt(arguments.split(",")[1]);
		String code = "";
		Random rand = new Random(); 
		
		for(int i = 0 ; i < nbDigits ; i++){
			int r = rand.nextInt(9);
			code = code+r;
		}
		
		
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    PreparedStatement ps = cnx.prepareStatement(formula);
		    ps.setString(1, code);
		    ps.setInt(2, dbID);
		    ResultSet rs = ps.executeQuery();
		    Object res = null;
		    if(rs.next())
		    	res = rs.getObject(1);
		    rs.close();
		    ps.close();
		    
		    if(col>=0){
		    	row.getValue().set(col, res.toString());
		    }
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
