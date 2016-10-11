package fr.protogen.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class ImportRPPS {

	public static void main(String[] args) {
		importRPPS();
		//importAdresses();
	}
	private static void importAdresses() {
		String dir = "C:\\Users\\jakjoud\\Documents\\projects support files\\TOLK\\bano-data-master\\";
		File directory = new File(dir);
		String[] files = directory.list();
		System.out.println("[TOLK][BANO]\tNombre de fichiers : "+files.length);
		for(String f : files){
			System.out.println("[TOLK][RPPS]\tFichier en cours : "+f);
			importAdresseFile(dir+f);
		}
	}
	private static void importAdresseFile(String fileName) {
		List<String> lines = new ArrayList<String>();
		Connection cnx = null;
		try{
			lines = IOUtils.readLines(new FileInputStream(fileName));
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		System.out.println("[TOLK][RPPS]\tFichier contenant "+lines.size()+" Adresses");
		int index = 0;
		for(String l : lines){
			index++;
			System.out.println("[TOLK][RPPS]\tAdresse "+index+"/"+lines.size());
			String[] T = l.split(",");
			String id = T[0];
			String num = T[1];
			String add = T[2];
			String cp = T[3];
			String dist = T[4];
			
			String sql = "INSERT INTO user_adresse (adresse, num, cp, district, code) VALUES "
					+ "(?, ?, ?, ?, ?)";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, add);
			    ps.setString(2, num);
			    ps.setString(3, cp);
			    ps.setString(4, dist);
			    ps.setString(5, id);
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
	private static void importRPPS(){
		String fileName = "C:\\Users\\jakjoud\\Documents\\projects support files\\TOLK\\rpps1.csv";
		List<String> lines = new ArrayList<String>();
		Connection cnx = null;
		BufferedWriter bw = null;
		try{
			lines = IOUtils.readLines(new FileInputStream(fileName));
			if(lines.size()>0)
				lines.remove(0);
			Class.forName("org.postgresql.Driver");
			cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			File file = new File("C:\\Users\\jakjoud\\Documents\\projects support files\\TOLK\\rpps1.csv.log");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write("[VIT1JOB]Nombre des Professionnels\t"+lines.size());
			bw.newLine();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		System.out.println("[TOLK][RPPS]\tInsertion de "+lines.size()+" professionels");
		
		int index = 0;
		for(String l : lines){
			index++;
			String[] T = l.split(";");
			String id = T[0];
			String sciv = T[1];
			String nom = T[3];
			String prenom = T[4];
			String cat = T[7];
			String csf = "";	//	A mettre dans la spécialité
			String sf = "";	//	A mettre dans la spécialité
			if(T.length>8){
				csf = T[8];
				sf = T [9];
			}
			System.out.println("[TOLK][RPPS]\tPROFESSIONNEL "+id+" "+index+"/"+lines.size());
			System.out.println("[TOLK][RPPS]\tVérification de la présence préalable du professionnel");
			String sql = "select pk_user_praticien from user_praticien where identifiant_rpps=?";
			boolean found=false;
			try{
				bw.write("[TOLK][RPPS]\tPROFESSIONNEL "+id+" "+index+"/"+lines.size());
				bw.newLine();
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, id);
			    ResultSet rs = ps.executeQuery();
			    found = rs.next();
			    rs.close();
			    ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			if(found)
				continue;
			
			System.out.println("[TOLK][RPPS]\tVérification de la spécialité");
			int idSF = 0;
			sql = "select pk_user_specialite from user_specialite where code_specialite=?";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, csf);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	idSF = rs.getInt(1);
			    rs.close();
			    ps.close();
			    if(idSF == 0){
			    	sql = "insert into user_specialite (code_specialite, libelle) values (?,?) returning pk_user_specialite";
			    	ps = cnx.prepareStatement(sql);
			    	ps.setString(1, csf);
			    	ps.setString(2, sf);
			    	rs = ps.executeQuery();
			    	if(rs.next())
			    		idSF = rs.getInt(1);
			    	rs.close();
			    	ps.close();
			    	
			    }
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			System.out.println("[TOLK][RPPS]\tVérification du savoir faire");
			int idSav = 0;
			sql = "select pk_user_savoir_faire from user_savoir_faire where code=?";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, csf);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	idSav = rs.getInt(1);
			    rs.close();
			    ps.close();
			    if(idSav == 0){
			    	sql = "insert into user_savoir_faire (code, libelle) values (?,?) returning pk_user_savoir_faire";
			    	ps = cnx.prepareStatement(sql);
			    	ps.setString(1, csf);
			    	ps.setString(2, sf);
			    	rs = ps.executeQuery();
			    	if(rs.next())
			    		idSav = rs.getInt(1);
			    	rs.close();
			    	ps.close();
			    	
			    }
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			System.out.println("[TOLK][RPPS]\tVérification de la catégorie professionnelle");
			int idCat = 0;
			sql = "select pk_user_categorie_professionnelle from user_categorie_professionnelle where libelle=?";
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, cat);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	idCat = rs.getInt(1);
			    rs.close();
			    ps.close();
			    if(idCat == 0){
			    	sql = "insert into user_categorie_professionnelle (libelle) values (?) returning pk_user_categorie_professionnelle";
			    	ps = cnx.prepareStatement(sql);
			    	ps.setString(1, cat);
			    	rs = ps.executeQuery();
			    	if(rs.next())
			    		idCat = rs.getInt(1);
			    	rs.close();
			    	ps.close();
			    	
			    }
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			System.out.println("[TOLK][RPPS]\tVérification de la civilité");
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
			
			System.out.println("[TOLK][RPPS]\tInsertion");
			sql = "INSERT INTO user_praticien (identifiant_rpps, nom, prenom, fk_user_savoir_faire, fk_user_categorie_professionnelle, fk_user_civilite_exercice, fk_user_specialite) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
			
			
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
			    ps.setString(1, id);
			    ps.setString(2, nom);
			    ps.setString(3, prenom);
			    ps.setInt(4, idSav);
			    ps.setInt(5, idCat);
			    ps.setInt(6, civ);	//	Civilité
			    ps.setInt(7, idSF); //	Spécialité
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
}
