package fr.protogen.masterdata.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.poi.util.IOUtils;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.StoredFile;
import fr.protogen.masterdata.model.StoredFileType;

public class FileStoreService {
	private String root="";
	
	public FileStoreService(){
		root = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+"/file_store/";
	}
	
	public List<String> loadFiles(CBusinessClass entity, int beanId){
		List<String> files = new ArrayList<String>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "select file_name from c_entity_store where class_id=? and bean_id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, entity.getId());
			ps.setInt(2, beanId);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				files.add(rs.getString(1));
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return files;
	}
	
	public String fullPathFile(CBusinessClass entity, int beanID, String file){
		return "file_store/"+entity.getId()+"/"+beanID+"/"+file;
	}
	
	public boolean uploadFile(CBusinessClass entity, int beanID, String file, InputStream is, StoredFile storedFile) throws IOException{
		boolean flag = false;
		
		String rep = root;
		File dir = new File(rep);
		if(!dir.isDirectory() || !dir.exists()){
			dir.mkdir();
		}
		
		rep = root+entity.getId();
		dir = new File(rep);
		if(!dir.isDirectory() || !dir.exists()){
			dir.mkdir();
		}
		
		rep = root+entity.getId()+"/"+beanID;
		dir = new File(rep);
		if(!dir.isDirectory() || !dir.exists()){
			dir.mkdir();
		}
		
		String fullName = root+entity.getId()+"/"+beanID+"/"+file;
		File toCheck = new File(fullName);
		if(toCheck.exists() && toCheck.isFile()){
			int index = fullName.lastIndexOf('.');
			fullName = fullName.substring(0,index-1)+"(1)"+fullName.substring(index);
			index = file.lastIndexOf('.');
			file = file.substring(0,index-1)+"(1)"+file.substring(index);
		}
		toCheck = null;
		File toCreate = new File(fullName);
		
		toCreate.createNewFile();
		
		OutputStream os = new FileOutputStream(toCreate);
		IOUtils.copy(is, os);
		
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "insert into c_entity_store (class_id,bean_id,file_name,type_id,libelle,description,is_private) values (?,?,?,?,?,?,?)";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, entity.getId());
			ps.setInt(2, beanID);
			ps.setString(3,file);
			ps.setInt(4, storedFile.getType().getId());
			ps.setString(5, storedFile.getLibelle());
			ps.setString(6, storedFile.getDescription());
			ps.setString(7, storedFile.isPrivateFile()?"Y":"N");
			ps.execute();
			ps.close();
			flag = true;
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return flag;
	}

	public List<StoredFileType> loadTypes(String appKey) {
		List<StoredFileType> results = new ArrayList<StoredFileType>();
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "select id, libelle from c_file_type where appkey=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				StoredFileType t = new StoredFileType();
				t.setId(rs.getInt(1));
				t.setLibelle(rs.getString(2));
				results.add(t);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}

	public List<StoredFile> loadStoredFiles(CBusinessClass entity, int beanId, List<StoredFileType> types) {
		List<StoredFile> results = new ArrayList<StoredFile>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "select id, file_name, libelle, description, type_id, is_private from c_entity_store where class_id=? and bean_id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, entity.getId());
			ps.setInt(2, beanId);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				StoredFile f = new StoredFile();
				f.setId(rs.getInt(1));
				f.setFileName(rs.getString(2));
				f.setLibelle(rs.getString(3));
				f.setDescription(rs.getString(4));
				for(StoredFileType t : types)
					if(t.getId() == rs.getInt(5)){
						f.setType(t);
						break;
					}
				f.setPrivateFile(rs.getString(6).equals("Y"));
				results.add(f);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}

	public void deleteStoredFile(StoredFile sf,CBusinessClass entity, int beanId) {
		
		String file = root+entity.getId()+"/"+beanId+"/"+sf.getFileName();
		File f = new File(file);
		if(f.exists() && f.isFile())
			f.delete();
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement("delete from c_entity_store where id=?");
			ps.setInt(1, sf.getId());
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
}
