package fr.protogen.masterdata.DAO;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.Country;

public class CountryDAO  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8072630923231664003L;

	/**
	 * 
	 */

	
	public List<Country> getAllCountries() {
		// TODO Auto-generated method stub
		List<Country> countries=new ArrayList<Country>();
		Country country;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select * from \"countriesInfo\"");
			while(rs.next()){
				country=new Country();
				country.setId(rs.getInt("id"));
				country.setName(rs.getString("cName"));
				country.setIso(rs.getString("iso"));
				country.setIso3(rs.getString("iso3"));
				country.setNiceName(rs.getString("nicename"));
				country.setNumCode(rs.getInt("numcode"));
				country.setPhoneCode(rs.getInt("phonecode"));
				
				countries.add(country);
			}
			
			rs.close();
			st.close();
			
			return countries;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return countries;
	}



	
	
}
