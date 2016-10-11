package fr.protogen.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dbutils.DBUtils;

public class StaticIndexing {
	public static void main(String[] args) {
		log("Indexer les salariés");
		//indexeSalaries();
		log("Indexer les employeurs");
		indexEmployeurs();
	}

	private static void indexEmployeurs() {
		List<Integer> salaries = new ArrayList<Integer>();
		String sql = "select pk_user_employeur from user_employeur where pk_user_employeur in (select fk_user_employeur from user_offre where pk_user_offre in(select fk_user_offre from user_competence_offre where fk_user_competence=42))";
		Connection cnx = null;
		try{
			Class.forName("org.postgresql.Driver");
		    cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				salaries.add(new Integer(rs.getInt(1)));
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		log("Indexation de "+salaries.size()+" employeurs");
		
		for(Integer id : salaries) {
			log("Employeur "+salaries.indexOf(id)+" / "+salaries.size());
//			Indexer par ville et pays
			log("Indexer les employeurs par villes et pays");
			sql = "SELECT   user_ville.fk_user_pays,   user_ville.pk_user_ville FROM   public.user_employeur,   public.user_ville WHERE   user_ville.pk_user_ville = user_employeur.fk_user_ville and pk_user_employeur=?";
			String queries = "";
			
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					queries = "insert into index_user_employeur (index_table, id_index, pk_user_employeur) VALUES "
							+ "('user_ville',"+rs.getInt(1)+","+id+");"
							+ "insert into index_user_employeur (index_table, id_index, pk_user_employeur) VALUES "
							+ "('user_pays',"+rs.getInt(2)+","+id+");";
				}
				rs.close();
				ps.close();
				
				ps = cnx.prepareStatement(queries);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
//			Indexer par competence
			sql = "SELECT   user_competence_offre.fk_user_competence FROM   public.user_employeur,   public.user_offre,   public.user_competence_offre WHERE   user_offre.fk_user_employeur = user_employeur.pk_user_employeur AND  user_competence_offre.fk_user_offre = user_offre.pk_user_offre and pk_user_employeur=?";
			queries = "";
			log("Indexer les employeurs par compétence");
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					queries = "insert into index_user_employeur (index_table, id_index, pk_user_employeur) VALUES "
							+ "('user_competence',"+rs.getInt(1)+","+id+");";
				}
				rs.close();
				ps.close();
				
				ps = cnx.prepareStatement(queries);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
//			Indexer par langues
			sql = "SELECT   user_maitrise_langue_offre.fk_user_langue FROM   public.user_employeur,   public.user_offre,   public.user_maitrise_langue_offre WHERE   user_offre.fk_user_employeur = user_employeur.pk_user_employeur AND  user_maitrise_langue_offre.fk_user_offre = user_offre.pk_user_offre and pk_user_employeur=?";
			queries = "";
			log("Indexer les employeurs par langue");
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					queries = "insert into index_user_employeur (index_table, id_index, pk_user_employeur) VALUES "
							+ "('user_langue',"+rs.getInt(1)+","+id+");";
				}
				rs.close();
				ps.close();
				
				ps = cnx.prepareStatement(queries);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
	
		try{
			cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	private static void indexeSalaries() {
		List<Integer> salaries = new ArrayList<Integer>();
		String sql = "select pk_user_salarie from user_salarie";
		Connection cnx = null;
		try{
			Class.forName("org.postgresql.Driver");
		    cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				salaries.add(new Integer(rs.getInt(1)));
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		log("Indexation de "+salaries.size()+" salariés");
		
		
		for(Integer id : salaries) {
			log("Salarié "+salaries.indexOf(id)+" / "+salaries.size());
//			Indexer par ville et pays
			log("Indexer les salariés par villes et pays");
			sql = "SELECT    user_ville.pk_user_ville,    user_ville.fk_user_pays FROM    public.user_salarie,    public.user_ville WHERE    user_salarie.fk_user_ville = user_ville.pk_user_ville and pk_user_salarie=?";
			String queries = "";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				
				while(rs.next()){
					queries = "insert into index_user_salarie (index_table, id_index, pk_user_salarie) VALUES "
							+ "('user_ville',"+rs.getInt(1)+","+id+");"
							+ "insert into index_user_salarie (index_table, id_index, pk_user_salarie) VALUES "
							+ "('user_pays',"+rs.getInt(2)+","+id+");";
				}
				rs.close();
				ps.close();
				
				ps = cnx.prepareStatement(queries);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
//			Indexer par competence
			log("Indexer les salariés par compétence");
			sql = "SELECT   user_competence_salarie.fk_user_competence FROM   public.user_salarie,   public.user_competence_salarie WHERE   user_competence_salarie.fk_user_salarie = user_salarie.pk_user_salarie and pk_user_salarie=?";
			queries = "";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					queries = "insert into index_user_salarie (index_table, id_index, pk_user_salarie) VALUES "
							+ "('user_competence',"+rs.getInt(1)+","+id+");";
				}
				rs.close();
				ps.close();
				
				ps = cnx.prepareStatement(queries);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
//			Indexer par langues
			log("Indexer les salariés par langues");
			sql = "SELECT   user_maitrise_langue_salarie.fk_user_langue FROM   public.user_salarie,   public.user_maitrise_langue_salarie WHERE   user_maitrise_langue_salarie.fk_user_salarie = user_salarie.pk_user_salarie and pk_user_salarie=?";
			queries = "";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					queries = "insert into index_user_salarie (index_table, id_index, pk_user_salarie) VALUES "
							+ "('user_langue',"+rs.getInt(1)+","+id+");";
				}
				rs.close();
				ps.close();
				
				ps = cnx.prepareStatement(queries);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
		
		try{
			cnx.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
	
	private static void log(String message){
		System.out.println("[VIT1JOB][INDEATION]"+message);
	}
}
