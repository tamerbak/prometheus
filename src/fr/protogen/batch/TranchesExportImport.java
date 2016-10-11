package fr.protogen.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class TranchesExportImport {

	public static void main(String[] args) {
		correctRemboursement();
	}
	
	private static void correctRemboursement(){
		try {
			String formule = IOUtils.toString((new FileInputStream("D:\\rubriquesB\\Remb.txt")));
			String sql = "update user_rubrique set base_de_calcul_salariale=?, base_de_calcul_patronale=? where libelle like '%(Rembours%'";
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, formule);
			ps.setString(2, formule);
			
			
			ps.execute();
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
	
	private static void importAll(){
		String path = "D:\\rubriquesB\\";
		List<Integer> rids = new ArrayList<Integer>();
		try {
		
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			
			String sql = "select pk_user_rubrique from user_rubrique where protogen_user_id=77";
			
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(sql);
			while(rs.next())
				rids.add(new Integer(rs.getInt(1)));
			
			rs.close();
			st.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(Integer I : rids){
			int id = I.intValue();
			File origine = new File(path+id+".plc");
			if(!origine.exists())
				continue;
			String content = "";
			try {
				content = IOUtils.toString(new FileInputStream(origine));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(content.length() == 0)
				continue;
			
			String[] champs = content.split("====");
			String sql = "update user_rubrique set base_de_calcul_salariale=?, base_de_calcul_patronale=? where pk_user_rubrique=?";
			try{
				Class.forName("org.postgresql.Driver");

				Connection cnx = DriverManager.getConnection(DBUtils.url,
						DBUtils.username, DBUtils.password);
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setString(1, champs[4]);
				ps.setString(2, champs[4]);
				ps.setInt(3, id);
				
				ps.execute();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			/*
			 * 	Remboursement
			 */
			executeInsert(id+"R.plc",champs, "(Remboursement)");
			executeInsert(id+"B.plc",champs, "(Tranche B)");
			executeInsert(id+"BR.plc",champs, "(Remboursement tranche B)");
			executeInsert(id+"C.plc",champs, "(Tranche C)");
			executeInsert(id+"CR.plc",champs, "(Remboursement tranche C)");
			
		}
		
	}

	private static void executeInsert(String id, String[] ofields,String suffixe) {
		String path = "D:\\rubriquesB\\";

		File origine = new File(path+id);
		if(!origine.exists())
			return;
		String content = "";
		try {
			content = IOUtils.toString(new FileInputStream(origine));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(content.length() == 0)
			return;
		
		String[] champs = content.split("====");
		String sql = "insert into user_rubrique (code_rubrique,libelle, taux__salarial, taux_patronal,base_de_calcul_salariale,base_de_calcul_patronale) "
				+ " values (?,?,?,?,?,?)";
		try{
			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setString(1, ofields[0]);
			ps.setString(2, ofields[1]+" "+suffixe);
			ps.setDouble(3, Double.parseDouble(ofields[2]));
			ps.setDouble(4, Double.parseDouble(ofields[3]));
			ps.setString(5, champs[4]);
			ps.setString(6, champs[4]);
			
			ps.execute();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}

	private static void exportAll() {

		String pathB = "D:\\rubriquesB\\";

		String pathC = "D:\\rubriquesC\\";

		try {

			/*(new File(pathB)).mkdir();

			(new File(pathC)).mkdir();*/

			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			/*
			 * 
			 * TRANCHE B
			 */

			String sql = "select base_de_calcul_salariale, taux__salarial, taux_patronal, code_rubrique, libelle,pk_user_rubrique from user_rubrique where base_de_calcul_salariale like '%ranche B%'";

			Statement st = cnx.createStatement();

			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				String bc = rs.getString(1).replace("<<VALEUR_Tranche(Tranche B).Montant plafond>>","");
				bc = bc.replace("<<VALEUR_Tranche(Tranche C).Montant plafond>>","");
				String cot = rs.getString(4) + "====" + rs.getString(5)
						+ "====" + rs.getDouble(2) + "====" + rs.getDouble(3) + "===="
						+ bc;
				

				File f = new File(pathB + rs.getInt(6)+".plc");

				IOUtils.write(cot, new FileOutputStream(f));

				
				bc = rs.getString(1).replace("<<VALEUR_Tranche(Tranche A).Montant plafond>>","");
				cot = rs.getString(4) + "====" + rs.getString(5)
						+ "====" + rs.getDouble(2) + "====" + rs.getDouble(3) + "===="
						+ bc;
				
				f = new File(pathB + rs.getInt(6)+"B.plc");

				IOUtils.write(cot, new FileOutputStream(f));
			}

			rs.close();

			st.close();

			/*
			 * 
			 * TRANCHE C
			 */

			sql = "select base_de_calcul_salariale, taux__salarial, taux_patronal, code_rubrique, libelle,pk_user_rubrique from user_rubrique where base_de_calcul_salariale like '%ranche C%'";

			st = cnx.createStatement();

			rs = st.executeQuery(sql);

			while (rs.next()) {

				String cot = rs.getString(4) + "====" + rs.getString(5)
						+ "====" + rs.getDouble(2) + "====" + rs.getDouble(3) + "===="
						+ rs.getString(1);

				File f = new File(pathC + rs.getInt(6)+".plc");

				IOUtils.write(cot, new FileOutputStream(f));

			}

			rs.close();

			st.close();

		} catch (Exception exc) {

			exc.printStackTrace();

		}

	}

}