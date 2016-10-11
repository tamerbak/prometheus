package fr.protogen.connector.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.protogen.masterdata.dbutils.ProtogenConnection;

@Path("/gde")
public class GDEService {
	@POST
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response insertPraticien(String sdata){
		String result = "{\"status\" : \"ERREUR\", \"id\": \"0\"}";
		
		String[] T = sdata.split(";");
		String sciv = T[0];
		String nom = T[1];
		String prenom = T[2];
		String sf = T[3];
		
		
		Connection cnx=ProtogenConnection.getInstance().getConnection();
		int idSF = 0;
		String sql = "select pk_user_specialite from user_specialite where libelle=?";
		try{
			
			PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, sf);
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	idSF = rs.getInt(1);
		    rs.close();
		    ps.close();
		    if(idSF == 0){
		    	sql = "insert into user_specialite (libelle) values (?) returning pk_user_specialite";
		    	ps = cnx.prepareStatement(sql);
		    	ps.setString(1, sf);
		    	rs = ps.executeQuery();
		    	if(rs.next())
		    		idSF = rs.getInt(1);
		    	rs.close();
		    	ps.close();
		    	
		    }
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		int idSav = 0;
		sql = "select pk_user_savoir_faire from user_savoir_faire where libelle=?";
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, sf);
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	idSav = rs.getInt(1);
		    rs.close();
		    ps.close();
		    if(idSav == 0){
		    	sql = "insert into user_savoir_faire (libelle) values (?) returning pk_user_savoir_faire";
		    	ps = cnx.prepareStatement(sql);
		    	ps.setString(1, sf);
		    	rs = ps.executeQuery();
		    	if(rs.next())
		    		idSav = rs.getInt(1);
		    	rs.close();
		    	ps.close();
		    	
		    }
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		int civ = 0;
		sql = "select pk_user_civilite_exercice from user_civilite_exercice where libelle=?";
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, sciv);
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	civ = rs.getInt(1);
		    rs.close();
		    ps.close();
		    if(civ == 0){
		    	sql = "insert into user_civilite_exercice (libelle) values (?) returning pk_user_civilite_exercice";
		    	ps = cnx.prepareStatement(sql);
		    	ps.setString(1, sciv);
		    	rs = ps.executeQuery();
		    	if(rs.next())
		    		civ = rs.getInt(1);
		    	rs.close();
		    	ps.close();
		    	
		    }
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		sql = "INSERT INTO user_praticien (nom, prenom, fk_user_savoir_faire, fk_user_civilite_exercice, fk_user_specialite) "
				+ "VALUES (?, ?, ?, ?, ?) returning pk_user_praticien";
		
		
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, nom);
		    ps.setString(2, prenom);
		    ps.setInt(3, idSav);
		    ps.setInt(4, civ);	//	Civilité
		    ps.setInt(5, idSF); //	Spécialité
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	result = "{\"status\": \"SUCCES\", \"id\": \""+rs.getInt(1)+"\"}";
		    ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		return Response.status(200).entity(result).build();
	}
}
