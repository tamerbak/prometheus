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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class RubriqueCorrectBatch {

	public static void main(String[] args) {
		importAll();
	}
	
	private static void exportRubriques(){
		String pathB = "D:\\remboursementRubrique\\";


		try {

			(new File(pathB)).mkdir();

			

			Class.forName("org.postgresql.Driver");

			Connection cnx = DriverManager.getConnection(DBUtils.url,
					DBUtils.username, DBUtils.password);

			/*
			 * 
			 * TRANCHE B
			 */

			String sql = "select base_de_calcul_salariale, taux__salarial, taux_patronal, code_rubrique, libelle, "
					+ "pk_user_rubrique from user_rubrique where base_de_calcul_salariale like '%GQL%'";

			Statement st = cnx.createStatement();

			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				String bc = rs.getString(1);
				String cot = rs.getString(4) + "====" + rs.getString(5)
						+ "====" + rs.getDouble(2) + "====" + rs.getDouble(3) + "===="
						+ bc;
				

				File f = new File(pathB + rs.getInt(6)+".plc");

				IOUtils.write(cot, new FileOutputStream(f));
				
				f = new File(pathB + rs.getInt(6)+".plc");

				IOUtils.write(cot, new FileOutputStream(f));
			}

			rs.close();

			st.close();

			
		} catch (Exception exc) {

			exc.printStackTrace();

		}
	}
	private static void importAll(){
		String path = "D:\\remboursementRubrique\\";
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
			
			
		}
		
	}
}
