package fr.protogen.asgard.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;

import javax.faces.context.FacesContext;

import fr.protogen.asgard.metamodel.BPLigneTemplate;
import fr.protogen.asgard.metamodel.BPYear;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class ParameteredDataEngine {
	public int getYear(String parameteredYearQuery){
		int year=0;
		
		try{
		    Connection cnx=((ProtogenConnection)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("PROTOGEN_CONNECTION")).getConnection();
		    String sql = parameteredYearQuery;
		    PreparedStatement ps =cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    String d = ""; 
		    if(rs.next())
		    	d=rs.getObject(1).toString();
		    
		    year=Integer.parseInt(d.split("-")[0]);
		    //year = d.getYear();
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return year;
	}

	public double getValue(BPYear y, BPLigneTemplate l) {
		double result=0;
		try{
		    Connection cnx=((ProtogenConnection)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("PROTOGEN_CONNECTION")).getConnection();
		    String sql = l.getParameteredQuery().replaceAll("<<year>>", y.getYear()+"").replaceAll("<<compte>>", l.getCompte());
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	result = rs.getDouble(1); 
		    
		    result = Math.round(result*100.0)/100.0;
		    DecimalFormat df = new DecimalFormat("###.##");
		    String dummyNbr = df.format(result);
		    result = Double.parseDouble(dummyNbr.replaceAll(",","." ));
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return result;
	}
}
