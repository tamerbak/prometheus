package fr.protogen.masterdata.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class EntrepriseService {

	public int insertEntreprise(String sigle, String raisonSociale, double capital, int typeEntreprise, String referenceDeclarant, String identifiantFiscal, int idForm, String userFullName, String siretEntreprise){
		int id=0;
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    String sql = "insert into user_entreprise (sigle, raison_sociale, capital_social, fk_user_type_entreprise, reference_declarant, identifiant_entreprise, fk_user_forme_entite, siret) "
		    		+ " values (?, ?, ?, ?, ?, ?, ?,?)";
		    PreparedStatement ps=cnx.prepareStatement(sql);
		    
		    ps.setString(1, sigle);
		    ps.setString(2, raisonSociale);
		    ps.setDouble(3, capital);
		    ps.setInt(4, typeEntreprise);
		    ps.setString(5, referenceDeclarant);
		    ps.setString(6, identifiantFiscal);
		    ps.setInt(7, idForm);
		    ps.setString(8, siretEntreprise);
		    
		    ps.execute();
		    ps.close();
		    sql = "select nextval('user_entreprise_seq')";
		    ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	id=rs.getInt(1)-1;
		    rs.close();
		    ps.close();
		    
		    sql = "insert into c_org_instance (organization,instance) values (?,?)";
		    
		    ps = cnx.prepareStatement(sql);
		    ps.setInt(1, 1);
		    ps.setInt(2, id);
		    ps.execute();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}

	public void persistConventions(int ident, List<String> selectedConvention) {
		try{
			Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    for(String sc : selectedConvention){
			    String sql = "insert into user_affectation_convention_collective (fk_user_entreprise, fk_user_convention_collective) values (?,?)";
			    PreparedStatement ps=cnx.prepareStatement(sql);
			    ps.setInt(1, ident);
			    ps.setInt(2, Integer.parseInt(sc));
			    ps.execute();
			    ps.close();
		    }
		    
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
