package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.FFileStore;

public class FileStoreDAO {
	public void insertFile(FFileStore f){
		String sql = "insert into f_filestore (file_title,"
				+ "full_file_path,"
				+ "table_data_reference, identifiant) values (?,?,?,?) "
				+ "returning id";
		
		String deleteQuery = "delete from f_filestore where identifiant=? and table_data_reference=?";
		
		try{
			Class.forName("org.postgresql.Driver");
			
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    PreparedStatement ps = cnx.prepareStatement(deleteQuery);
		    ps.setInt(1, f.getIdentifiant());
		    ps.setString(2, f.getEntity().getDataReference());
		    ps.execute();
		    ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}

		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, f.getFileName());
		    ps.setString(2, f.getFullPath());
		    ps.setString(3, f.getEntity().getDataReference());
		    ps.setInt(4, f.getIdentifiant());
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	f.setId(rs.getInt(1));
		    rs.close();
		    ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}

	}
	
	public FFileStore selectFile(int identifiant, CBusinessClass table){
		FFileStore f = new FFileStore();
		
		f.setEntity(table);
		f.setIdentifiant(identifiant);
		String sql = "select id, full_file_path, file_title from f_filestore "
				+ "where identifiant=? and table_data_reference=?";
		
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, f.getIdentifiant());
		    ps.setString(2, f.getEntity().getDataReference());
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	f.setId(rs.getInt(1));
		    	f.setFullPath(rs.getString(2));
		    	f.setFileName(rs.getString(3));
		    }
		    rs.close();
		    ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return f;
	}
}
