package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class LocalDAO {

	public List< String> getCP(String pays){
		List< String> cps = new ArrayList< String>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
	    
			String sql = "select id, code_postal from core_code_postal where pays=? order by code_postal asc";
			int pid = pays.equals("+33")?40:42;
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, pid);
			ResultSet rs= ps.executeQuery();
			while(rs.next()){
				cps.add(rs.getString(2));
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return cps;
	}
	
	public List<String> getVilles(String cp){
		List<String> v = new ArrayList<String>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "select v.ville from core_ville v, core_code_postal cp where cp.id=v.code_postal and cp.code_postal=? order by ville asc";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, cp);
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next())
		    	v.add(rs.getString(1));
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return v;
	}

	public List<PairKVElement> getActivites() {
		List<PairKVElement> activites = new ArrayList<PairKVElement>();
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			String sql = "select pk_user_activite_metier, libelle from user_activite_metier order by pk_user_activite_metier asc";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next())
				activites.add(new PairKVElement(rs.getInt(1)+"", rs.getString(2)));
			
			rs.close();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return activites;
	}

	public List<PairKVElement> getConventionsCollectives(int aid) {
		List<PairKVElement> conventions = new ArrayList<PairKVElement>();
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql = "select pk_user_convention_collective, code, libelle from user_convention_collective, user_convention_par_metier "
					+ "	where pk_user_convention_collective=fk_user_convention_collective and fk_user_activite_metier=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, aid);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				conventions.add(new PairKVElement(rs.getInt(1)+"", rs.getString(2)+" - "+rs.getString(3)));
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return conventions;
	}

	public List<PairKVElement> getConventionsCollectivesByNaf(int idNaf) {
		List<PairKVElement> conventions = new ArrayList<PairKVElement>();
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql = "select pk_user_convention_collective, code, libelle from user_convention_collective, user_conventions_par_naf "
					+ "	where pk_user_convention_collective=fk_user_convention_collective and fk_user_codification_naf=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, idNaf);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				conventions.add(new PairKVElement(rs.getInt(1)+"", rs.getString(2)+" - "+rs.getString(3)));
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return conventions;
	}
	
	public List<PairKVElement> getNafs() {
		List<PairKVElement> conventions = new ArrayList<PairKVElement>();
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql = "select pk_user_codification_naf, code_naf, libelle from user_codification_naf";
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				conventions.add(new PairKVElement(rs.getInt(1)+"", rs.getString(2)+" - "+rs.getString(3)));
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return conventions;
	}
	
	public List<PairKVElement> getNafsByActivities(int selectedActivtiy) {
		List<PairKVElement> conventions = new ArrayList<PairKVElement>();
		List<PairKVElement> temp = new ArrayList<PairKVElement>();
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql = "select pk_user_codification_naf, code_naf, libelle from user_codification_naf, user_metier_par_naf where "
							+ " pk_user_codification_naf=fk_user_codification_naf and fk_user_activite_metier=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, selectedActivtiy);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				temp.add(new PairKVElement(rs.getInt(1)+"", rs.getString(2)+" - "+rs.getString(3)));
			}
			rs.close();
			ps.close();
			
			
			for(PairKVElement e : temp){
				boolean flag = false;
				for(PairKVElement c : conventions)
					if(e.getKey().equals(c.getKey())){
						flag = true;
						break;
					}
				
				if(!flag)
					conventions.add(e);
			}
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return conventions;
	}
	
	public List<PairKVElement> getActivitiesByNaf(int idNaf) {
		List<PairKVElement> conventions = new ArrayList<PairKVElement>();
		List<PairKVElement> temp = new ArrayList<PairKVElement>();
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql = "select pk_user_activite_metier, code_activite, libelle from user_activite_metier, user_metier_par_naf"
					+ "	where pk_user_activite_metier=fk_user_activite_metier and fk_user_codification_naf=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, idNaf);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				temp.add(new PairKVElement(rs.getInt(1)+"", (rs.getString(2)==null)?rs.getString(3):rs.getString(2)));
			}
			rs.close();
			ps.close();
			
			
			for(PairKVElement e : temp){
				boolean flag = false;
				for(PairKVElement c : conventions)
					if(e.getKey().equals(c.getKey())){
						flag = true;
						break;
					}
				
				if(!flag)
					conventions.add(e);
			}
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return conventions;
	}
	public void persistCity(String cityName, String selectedCityCP) {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql = "select pk_user_code_postal from user_code_postal where code_postale=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, selectedCityCP);
			int idcp = 40;
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				idcp = rs.getInt(1);
			rs.close();ps.close();
			
			sql = "insert into user_ville (nom_ville, fk_user_code_postal, fk_user_pays) values (?,?,?)";
			ps = cnx.prepareStatement(sql);
			ps.setString(1, cityName);
			ps.setInt(2, idcp);
			ps.setInt(3, 40);
			
			ps.execute();
			
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public boolean persistCP(String cpName, String selectedCPPays) {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			String sql = "select pk_user_code_postal from user_code_postal where code_postale=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, cpName);
			
			boolean flag = false;
			ResultSet rs = ps.executeQuery();
			flag = rs.next();
			rs.close();
			ps.close();
			if(flag){
				
				return false;
			}
			
			sql = "insert into user_code_postal (code_postale, fk_user_pays) values (?,?)";
			ps = cnx.prepareStatement(sql);
			ps.setString(1, cpName);
			ps.setInt(2, 40);
			
			ps.execute();
			
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return true;
	}

	

	

	
}
