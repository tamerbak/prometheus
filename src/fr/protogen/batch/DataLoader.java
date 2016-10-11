package fr.protogen.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class DataLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*loadNafs();
		loadCC();
		loadAM();
		conventionsParMetier();
		conventionsParNaf();
		metierParNaf();
		
		loadNewNafs();
		loadNewMetiers();
		loadNewCC();
		coupleActivitiesToNAF();
		coupleCCToMetier();
		coupleCCToNAF();*/
		insertRubriques();
	}
	
	

	private static void insertRubriques() {
		/*
		 * insert into user_rubrique 
(fk_user_organisme, libelle, base_de_calcul_salariale,base_de_calcul_patronale,taux__salarial,taux_patronal, montant_plafond,formule_calcul_salarie, formule_calcul_employeur)
VALUES
(?, ?, ?,?,?,?, ?,?, ?)
		 */
		
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\contisationssociales.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			String sql ="insert into user_rubrique "+
					"(fk_user_organisme, libelle, base_de_calcul_salariale,base_de_calcul_patronale,"
					+ "taux__salarial,taux_patronal, montant_plafond,formule_calcul_salarie, formule_calcul_employeur)"+
					"VALUES(?, ?, ?,?,?,?, ?,?, ?)";
			for(String l : content){
				//int org = Integer.parseInt(s)
			}
		}catch(Exception exc){
			
		}
		
	}



	@SuppressWarnings("unused")
	private static void loadGrille(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\ansi2utf8\\ANSI2UTF8\\grille.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			
			for(String l : content){
				String[] cols = l.split(";");
				String idgrille=cols[0].replaceAll("\"", "");
				String libelle=cols[1].replaceAll("\"", "");
				String classe=cols[2].replaceAll("\"", "");
				String echelon=cols[3].replaceAll("\"", "");
				String seuil=cols[4].replaceAll("\"", "").replace(",", ".");
				String plafond=cols[5].replaceAll("\"", "").replace(",", ".");
				
				String sql = "insert into user_classification_grille (classe,echelon) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, classe);
				ps.setString(2, echelon);
				
				ps.execute();
				
				ps.close();
				
				int id = 0;
				sql="select nextval('user_classification_grille_seq')";
				ps = cnx.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				if(rs.next())
					id=rs.getInt(1)-1;
				
				rs.close();
				ps.close();
				
				sql = "insert into user_grille_salaire (identifiant, libelle, fk_user_classification_grille, plancher, plafond) values (?, ?, ?, ?, ?)";
				ps = cnx.prepareStatement(sql);
				ps.setString(1, idgrille);
				ps.setString(2, libelle);
				ps.setInt(3, id);
				ps.setDouble(4, Double.parseDouble(seuil));
				ps.setDouble(5, Double.parseDouble(plafond));
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void loadFonctions(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\Alimentation_fonction.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(";");
				String ID=cols[0].replaceAll("\"", "");
				String libelle=cols[1].replaceAll("\"", "");
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_type_fonction (code_type_fonction,libelle) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ID);
				ps.setString(2, libelle);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadMetiers(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\metiers.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(";");
				String ID=cols[0].replaceAll("\"", "");
				String libelle=cols[1].replaceAll("\"", "");
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_type_metier (code_type_metier ,libelle) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ID);
				ps.setString(2, libelle);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadPrimes(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\primes.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(";");
				String ID=cols[0].replaceAll("\"", "");
				String libelle=cols[1].replaceAll("\"", "");
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_type_prime (code_type_prime ,libelle) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ID);
				ps.setString(2, libelle);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadClausesSpec(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\clausesspec.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(";");
				String ID=cols[0].replaceAll("\"", "");
				String libelle=cols[1].replaceAll("\"", "");
				String libellelong=cols[2].replaceAll("\"", "");
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_clauses_contrat (code_clause ,fk_user_type_clause,libelle_court,libelle_long) values (?,41,?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ID);
				ps.setString(2, libelle);
				ps.setString(3, libellelong);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadTypeContrat(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\typecontrat.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(";");
				String ID=cols[0].replaceAll("\"", "");
				String libelle=cols[1].replaceAll("\"", "");
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_type_contrat (code_contrat ,libelle,nombre_heures_mensuelle,forfait_jours,jours_rtt) values (?,?,151.67,226,8)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ID);
				ps.setString(2, libelle);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private static void loadJustifs(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\justifs.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(";");
				String ID=cols[0].replaceAll("\"", "");
				String libelle=cols[1].replaceAll("\"", "");
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_type__de_justificatif (code_numerique,libelle) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setDouble(1, Double.parseDouble(ID));
				ps.setString(2, libelle);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadBalance(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\balance.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			
			for(String l : content){
				String[] cols = l.split(";");
				String compte=cols[0].replaceAll("\"", "");
				String libelle=cols[1].replaceAll("\"", "");
				String sdeb = cols[3].replaceAll(" ", "").replaceAll("\"", "");
				String scred = cols[4].replaceAll(" ", "").replaceAll("\"", "");
				double mdebit = Double.parseDouble(sdeb);
				double mcredit = Double.parseDouble(scred);
				
				
				String sql = "select pk_user_compte_comptable from user_compte_comptable where code = ?";
				String likeCompte = compte;
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, likeCompte);
				ResultSet rs = ps.executeQuery();
				int id = 0;
				if(rs.next())
					id= rs.getInt(1);
				rs.close();
				ps.close();
				if(id == 0)
					id = createCompte(compte,libelle);
				
				sql = "insert into user_ecriture_comptable (numero,intitule,fk_user_compte_comptable,fk_user_journal_comptable,montant_credit,montant_debit,fk_user_periode_fiscale,date_d_ecriture) "
						+ "values (?,?,?,43,?,?,40,'2013-01-01 00:00:00+00')";
				ps = cnx.prepareStatement(sql);
				ps.setString(1, "2013 - 1");
				ps.setString(2, "Balance d'ouverture 2013");
				ps.setInt(3, id);
				ps.setDouble(4, mdebit);
				ps.setDouble(5, mcredit);
				
				
				
				ps.execute();
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int createCompte(String compte, String libelle) {
		int id=0;
		try{
			String idclass = compte.charAt(0)+"";
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select pk_user_classe_comptable from user_classe_comptable where code=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, idclass);
		    ResultSet rs = ps.executeQuery();
		    int classe = 0;
		    if(rs.next())
		    	classe=rs.getInt(1);
		    rs.close();
		    ps.close();
		    sql = "insert into user_compte_comptable (code, libelle, fk_user_classe_comptable) values (?,?,?)";
		    ps = cnx.prepareStatement(sql);
		    ps.setString(1, compte);
		    ps.setString(2, libelle);
		    ps.setInt(3, classe);
		    
		    ps.execute();
			ps.close();
		    
		    sql = "select nextval('user_compte_comptable_seq')";
		    ps = cnx.prepareStatement(sql);
		    rs = ps.executeQuery();
		    if(rs.next())
		    	id=rs.getInt(1)-1;
		    rs.close();
		    ps.close();
		    
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}
	private static void loadActivites(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\activites.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(",");
				String ID=cols[0];
				String libelle=cols[1].replaceAll("\"", "");
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_activite (code_d_activite,libelle) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ID);
				ps.setString(2, libelle);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadNafs(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\nafs.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(";");
				String ID=cols[0];
				String libelle=cols[1].replaceAll("\"", "");
				libelle = libelle.trim();
				ID = ID.trim();
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_codification_naf (code_naf,libelle) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ID);
				ps.setString(2, libelle);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadNewNafs() {
		try {
			Class.forName("org.postgresql.Driver");

			System.out.println("-----------------------------NAFS");
			
		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\listecc.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			for(String l : content){
				String[] cols = l.split(";");
				String[] libelles=cols[4].replaceAll("\"", "").split(",");
				
				for(String libelle : libelles){
				
					libelle = libelle.trim().replace(" ", "");
					
					boolean found=false;
					String sql = "select pk_user_codification_naf from user_codification_naf where code_naf=?";
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.setString(1, libelle);
					ResultSet rs = ps.executeQuery();
					found = rs.next();
					rs.close();ps.close();
					
					if(found)
						continue;
					System.out.println(libelle);
					sql = "insert into user_codification_naf (code_naf) values (?)";
					ps = cnx.prepareStatement(sql);
					ps.setString(1, libelle);
					
					ps.execute();
					
					ps.close();
				}
			}

				
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void loadNewMetiers() {
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    System.out.println("-------------------------Metier");
			String path="D:\\csvs\\listecc.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			for(String l : content){
				String[] cols = l.split(";");
				String[] libelles=cols[3].replaceAll("\"", "").split(",");
				
				for(String libelle : libelles){
				
					libelle = libelle.trim();
					
					boolean found=false;
					String sql = "select * from user_activite_metier where libelle=?";
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.setString(1, libelle);
					ResultSet rs = ps.executeQuery();
					found = rs.next();
					rs.close();ps.close();
					
					if(found)
						continue;
					System.out.println(libelle);
					sql = "insert into user_activite_metier (libelle) values (?)";
					ps = cnx.prepareStatement(sql);
					ps.setString(1, libelle);
					
					ps.execute();
					
					ps.close();
				}
			}

				
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadNewCC() {
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    System.out.println("------------------------Conventions collectives");
			String path="D:\\csvs\\listecc.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			for(String l : content){
				String[] cols = l.split(";");
				String id = cols[0].replaceAll("\"", "").replace(" ", "").trim();
				String libelle = cols[2].replaceAll("\"", "").trim();
				
				libelle = libelle.trim();
					
				boolean found=false;
				String sql = "select * from user_convention_collective where code=?";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, id);
				ResultSet rs = ps.executeQuery();
				found = rs.next();
				rs.close();ps.close();
				
				if(found)
					continue;
				System.out.println(id);
				sql = "insert into user_convention_collective (code,libelle) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setString(1, id);
				ps.setString(2, libelle);
				ps.execute();
				
				ps.close();
				
			}

				
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void coupleCCToNAF(){
		System.out.println("---------------------------CC 2 NAF");
		Map<String, Integer> nafs = new HashMap<String, Integer>();
		Map<String, Integer> ccs = new HashMap<String, Integer>();
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select pk_user_codification_naf, code_naf from user_codification_naf";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	nafs.put(rs.getString(2), new Integer(rs.getInt(1)));
		    }
		    
		    rs.close();
		    ps.close();


		    sql = "select pk_user_convention_collective, code from user_convention_collective";
		    ps = cnx.prepareStatement(sql);
		    
		    rs = ps.executeQuery();
		    while(rs.next()){
		    	ccs.put(rs.getString(2), new Integer(rs.getInt(1)));
		    }
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		String path="D:\\csvs\\listecc.csv";
		File f = new File(path);
		List<String> content = new ArrayList<String>();

		try {
			content = IOUtils.readLines(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String l : content){
			String scc = l.split(";")[0].trim();
			int idcc = 0;
			for(String cc : ccs.keySet()){
				if(cc.equals(scc)){
					idcc = ccs.get(cc).intValue();
					break;
				}
			}
			
			if(idcc == 0)
				continue;
			
			String[] nfs = l.split(";")[4].split(",");
			for(String n : nfs){
				n = n.trim().replaceAll(" ", "");
				if(!nafs.containsKey(n))
					continue;
				
				int idnaf = nafs.get(n).intValue();
				try {
					Class.forName("org.postgresql.Driver");

					
				    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
				    
				    String sql = "select * from user_conventions_par_naf where fk_user_convention_collective=? and fk_user_codification_naf=?";
				    PreparedStatement ps = cnx.prepareStatement(sql);
				    ps.setInt(1, idcc);
				    ps.setInt(2, idnaf);
				    Boolean found=false;
				    ResultSet rs = ps.executeQuery();
				    found = rs.next();
				    rs.close();
				    ps.close();
				    
				    if(found)
				    	continue;
				    
				    System.out.println(n+" - "+idcc);
				    sql = "insert into user_conventions_par_naf (fk_user_convention_collective, fk_user_codification_naf) "
				    		+ " values (?,?)";
				    ps = cnx.prepareStatement(sql);
				    ps.setInt(1, idcc);
				    ps.setInt(2, idnaf);
				    
				    ps.execute();
				    
				    ps.close();
				    
				    
				}catch(Exception exc){
					exc.printStackTrace();
				}
				
			}
		}
	}
	
	
	private static void coupleCCToMetier(){
		System.out.println("--------------------------  CC2Metier");
		Map<String, Integer> ams = new HashMap<String, Integer>();
		Map<String, Integer> ccs = new HashMap<String, Integer>();
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select pk_user_activite_metier, libelle from user_activite_metier";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	ams.put(rs.getString(2), new Integer(rs.getInt(1)));
		    }
		    
		    rs.close();
		    ps.close();

		    
		    sql = "select pk_user_convention_collective, code from user_convention_collective";
		    ps = cnx.prepareStatement(sql);
		    
		    rs = ps.executeQuery();
		    while(rs.next()){
		    	ccs.put(rs.getString(2), new Integer(rs.getInt(1)));
		    }
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		String path="D:\\csvs\\listecc.csv";
		File f = new File(path);
		List<String> content = new ArrayList<String>();

		try {
			content = IOUtils.readLines(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String l : content){
			String scc = l.split(";")[0].trim();
			int idcc = 0;
			for(String cc : ccs.keySet()){
				if(cc.equals(scc)){
					idcc = ccs.get(cc).intValue();
					break;
				}
			}
			
			if(idcc == 0)
				continue;
			
			String[] nfs = l.split(";")[3].split(",");
			for(String n : nfs){
				n = n.trim().replaceAll(" ", "");
				if(!ams.containsKey(n))
					continue;
				
				int idnaf = ams.get(n).intValue();
				try {
					Class.forName("org.postgresql.Driver");

				    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
				    
				    String sql = "select * from user_convention_par_metier where fk_user_convention_collective=? and fk_user_activite_metier=?";
				    PreparedStatement ps = cnx.prepareStatement(sql);
				    ps.setInt(1, idcc);
				    ps.setInt(2, idnaf);
				    Boolean found=false;
				    ResultSet rs = ps.executeQuery();
				    found = rs.next();
				    rs.close();
				    ps.close();
				    
				    if(found)
				    	continue;
				    
				    sql = "insert into user_convention_par_metier (fk_user_convention_collective, fk_user_activite_metier) "
				    		+ " values (?,?)";
				    System.out.println(n+" - "+idcc);
				    ps = cnx.prepareStatement(sql);
				    ps.setInt(1, idcc);
				    ps.setInt(2, idnaf);
				    
				    ps.execute();
				    
				    ps.close();
				    
				    
				}catch(Exception exc){
					exc.printStackTrace();
				}
				
			}
		}
	}
	
	
	
	
	private static void coupleActivitiesToNAF(){
		System.out.println("------------------------------ Act2CC");
		Map<String, Integer> nafs = new HashMap<String, Integer>();
		Map<String, Integer> ams = new HashMap<String, Integer>();
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    String sql = "select pk_user_codification_naf, code_naf from user_codification_naf";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	nafs.put(rs.getString(2), new Integer(rs.getInt(1)));
		    }
		    
		    rs.close();
		    ps.close();
		    
		    sql = "select pk_user_activite_metier, libelle from user_activite_metier";
		    ps = cnx.prepareStatement(sql);
		    
		    rs = ps.executeQuery();
		    while(rs.next()){
		    	ams.put(rs.getString(2), new Integer(rs.getInt(1)));
		    }
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		String path="D:\\csvs\\listecc.csv";
		File f = new File(path);
		List<String> content = new ArrayList<String>();

		try {
			content = IOUtils.readLines(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String l : content){
			String[] snafs = l.split(";")[4].split("\\,");
			String[] sams = l.split(";")[3].split("\\,");
			for(String naf : nafs.keySet()){
				boolean nafExists = false;
				int selNaf=0;
				for(String n : snafs){
					n = n.trim().replaceAll(" ", "");
					if(naf.equals(n)){
						nafExists = true;
						selNaf = nafs.get(naf);
						break;
					}
				}
				
				if(!nafExists)
					continue;
				
				for(String a : sams){
					a = a.trim();
					for(String ak : ams.keySet()){
						if(ak.equals(a)){
							try {
								Class.forName("org.postgresql.Driver");

							    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
							    
							    String sql = "select * from user_metier_par_naf where fk_user_activite_metier=? and fk_user_codification_naf=?";
							    PreparedStatement ps = cnx.prepareStatement(sql);
							    ps.setInt(1, ams.get(ak).intValue());
							    ps.setInt(2, selNaf);
							    Boolean found=false;
							    ResultSet rs = ps.executeQuery();
							    found = rs.next();
							    rs.close();
							    ps.close();
							    
							    if(found)
							    	continue;
							    
							    sql = "insert into user_metier_par_naf (fk_user_activite_metier, fk_user_codification_naf) "
							    		+ " values (?,?)";
							    System.out.println(sql+" "+ak+" "+selNaf);
							    ps = cnx.prepareStatement(sql);
							    ps.setInt(1, ams.get(ak).intValue());
							    ps.setInt(2, selNaf);
							   
							    
							    ps.execute();
							    
							    ps.close();
							    
							    
							}catch(Exception exc){
								exc.printStackTrace();
							}
							
						}
					}
				}
			}
		}
	}
	
	private static void loadCC(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\cc.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				String[] cols = l.split(";");
				String ID=cols[0];
				String libelle=cols[1].replaceAll("\"", "");
				libelle = libelle.trim();
				ID = ID.trim().replaceAll(" ", "");;
				
				if(ids.contains(ID))
					continue;
				
				String sql = "insert into user_convention_collective (code,libelle) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, ID);
				ps.setString(2, libelle);
				
				ps.execute();
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void loadAM(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\csvs\\am.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				
				l = l.trim();
				
				boolean flag=false;
				for(String i : ids)
					if(i.trim().equals(l.trim())){
						flag = true;
						break;
					}
				if(flag)
					continue;
				
				String sql = "insert into user_activite_metier (code_activite,libelle) values (?,?)";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, l);
				ps.setString(2, l);
				
				ps.execute();
				
				ids.add(l);
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void conventionsParMetier(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			Map<String, Integer> conventions = new HashMap<String, Integer>();
			Map<String, Integer> metiers = new HashMap<String, Integer>();
		    
		    String sql = "select pk_user_convention_collective, code from user_convention_collective";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next())
		    	conventions.put(rs.getString(2), new Integer(rs.getInt(1)));
		    
		    rs.close();
		    ps.close();
		    
		    sql = "select pk_user_activite_metier, code_activite from user_activite_metier";
		    ps = cnx.prepareStatement(sql);
		    rs = ps.executeQuery();
		    while(rs.next())
		    	metiers.put(rs.getString(2), new Integer(rs.getInt(1)));
		    
		    rs.close();
		    ps.close();
		    
		    
		    String path="D:\\csvs\\ccmetier.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				if(l.split(";").length<2)
					continue;
				String cc = l.split(";")[0].trim();
				String am = l.split(";")[1].trim();
				
				
				boolean flag=false;
				for(String i : ids)
					if(i.trim().equals(l.trim())){
						flag = true;
						break;
					}
				if(flag)
					continue;
				
				int idcc=0;
				int idam=0;
				for(String cck : conventions.keySet()){
					if(cck.trim().equals(cc.trim())){
						idcc=conventions.get(cck.trim());
						break;
					}
				}
				for(String amk : metiers.keySet()){
					if(amk.trim().startsWith(am.trim())){
						if(amk.trim().endsWith(am.trim())){
							idam=metiers.get(amk);
							break;
						}
					}
				}
				
				sql = "insert into user_convention_par_metier (fk_user_convention_collective,fk_user_activite_metier) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, idcc);
				ps.setInt(2,idam);
				
				ps.execute();
				
				ids.add(l);
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void conventionsParNaf(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			Map<String, Integer> conventions = new HashMap<String, Integer>();
			Map<String, Integer> metiers = new HashMap<String, Integer>();
		    
		    String sql = "select pk_user_convention_collective, code from user_convention_collective";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next())
		    	conventions.put(rs.getString(2), new Integer(rs.getInt(1)));
		    
		    rs.close();
		    ps.close();
		    
		    sql = "select pk_user_codification_naf, code_naf from user_codification_naf";
		    ps = cnx.prepareStatement(sql);
		    rs = ps.executeQuery();
		    while(rs.next())
		    	metiers.put(rs.getString(2), new Integer(rs.getInt(1)));
		    
		    rs.close();
		    ps.close();
		    
		    
		    String path="D:\\csvs\\ccnaf.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				if(l.split(";").length<2)
					continue;
				String cc = l.split(";")[0].trim();
				String am = l.split(";")[1].trim();
				
				
				boolean flag=false;
				for(String i : ids)
					if(i.trim().equals(l.trim())){
						flag = true;
						break;
					}
				if(flag)
					continue;
				
				int idcc=0;
				int idam=0;
				for(String cck : conventions.keySet()){
					if(cck.trim().equals(cc.trim())){
						idcc=conventions.get(cck.trim());
						break;
					}
				}
				for(String amk : metiers.keySet()){
					if(amk.trim().startsWith(am.trim())){
						if(amk.trim().endsWith(am.trim())){
							idam=metiers.get(amk);
							break;
						}
					}
				}
				
				sql = "insert into user_conventions_par_naf (fk_user_convention_collective,fk_user_codification_naf) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, idcc);
				ps.setInt(2,idam);
				
				ps.execute();
				
				ids.add(l);
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void metierParNaf(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			Map<String, Integer> conventions = new HashMap<String, Integer>();
			Map<String, Integer> metiers = new HashMap<String, Integer>();
		    
		    String sql = "select pk_user_codification_naf, code_naf from user_codification_naf";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next())
		    	conventions.put(rs.getString(2), new Integer(rs.getInt(1)));
		    
		    rs.close();
		    ps.close();
		    
		    sql = "select pk_user_activite_metier, code_activite from user_activite_metier";
		    ps = cnx.prepareStatement(sql);
		    rs = ps.executeQuery();
		    while(rs.next())
		    	metiers.put(rs.getString(2), new Integer(rs.getInt(1)));
		    
		    rs.close();
		    ps.close();
		    
		    
		    String path="D:\\csvs\\metiernaf.csv";
			File f = new File(path);
			List<String> content;

			content = IOUtils.readLines(new FileInputStream(f));
			List<String> ids = new ArrayList<String>();
			for(String l : content){
				if(l.split(";").length<2)
					continue;
				String cc = l.split(";")[0].trim();
				String am = l.split(";")[1].trim();
				
				
				boolean flag=false;
				for(String i : ids)
					if(i.trim().equals(l.trim())){
						flag = true;
						break;
					}
				if(flag)
					continue;
				
				int idcc=0;
				int idam=0;
				for(String cck : conventions.keySet()){
					
					if(cck.toString().trim().equals(cc.toString().trim())){
						idcc=conventions.get(cck.trim());
						break;
					}
				}
				
				if(idcc == 0){
					cc = cc.substring(1);
					for(String cck : conventions.keySet()){
						
						if(cck.toString().trim().equals(cc.toString().trim())){
							idcc=conventions.get(cck.trim());
							break;
						}
					}
				}
				
				for(String amk : metiers.keySet()){
					if(amk.trim().startsWith(am.trim())){
						if(amk.trim().endsWith(am.trim())){
							idam=metiers.get(amk);
							break;
						}
					}
				}
				
				if(idam==0){
					am = am.substring(1);
					for(String amk : metiers.keySet()){
						if(amk.trim().startsWith(am.trim())){
							if(amk.trim().endsWith(am.trim())){
								idam=metiers.get(amk);
								break;
							}
						}
					}
				}
				
				sql = "insert into user_metier_par_naf (fk_user_activite_metier,fk_user_codification_naf) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, idam);
				ps.setInt(2,idcc);
				
				ps.execute();
				
				ids.add(l);
				
				ps.close();
				

				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
