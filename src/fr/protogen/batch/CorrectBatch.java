package fr.protogen.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.dbutils.DBUtils;

public class CorrectBatch {

	public static void main(String[] args) {
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    List<RubriqueBean> rubriques = new ArrayList<RubriqueBean>();
		    String sql = "select pk_user_rubrique, base_de_calcul_salariale, base_de_calcul_patronale from user_rubrique where protogen_user_id=77";
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	RubriqueBean b = new RubriqueBean();
		    	b.setId(rs.getInt(1));
		    	b.setBs(rs.getString(2));
		    	b.setBp(rs.getString(3));
		    	rubriques.add(b);
		    }
		    
		    rs.close();
		    ps.close();
		    for(RubriqueBean b : rubriques){
		    	sql = "update user_rubrique set base_de_calcul_salariale=?, base_de_calcul_patronale=? where pk_user_rubrique=?";
		    	ps = cnx.prepareStatement(sql);
		    	
		    	ps.setString(1, b.getBs().replaceAll("<<Salaire brut>>", "<<Montant brut>>"));
		    	ps.setString(2, b.getBp().replaceAll("<<Salaire brut>>", "<<Montant brut>>"));
		    	ps.setInt(3, b.getId());
		    	ps.execute();
		    	
		    	ps.close();
		    }
		    
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}

	}

}
