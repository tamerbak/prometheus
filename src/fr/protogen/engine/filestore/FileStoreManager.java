package fr.protogen.engine.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.w3c.tools.codec.Base64FormatException;
import com.itextpdf.text.pdf.codec.Base64;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.masterdata.DAO.FileStoreDAO;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.FFileStore;

public class FileStoreManager {
	private static String rootPath = "";
	private static FileStoreManager instance = null;
	public static synchronized FileStoreManager getInstance(){
		if(instance == null)
			instance = new FileStoreManager();
		return instance;
	}
	private FileStoreManager(){
		File dir = new File(rootPath);
		if(!dir.exists()|| dir.isFile())
			dir.mkdirs();
	}
	
	public FFileStore store(String fileName, String stream, String entity, int identifiant) throws IOException, Base64FormatException{
		FFileStore res = new FFileStore();
		String path = rootPath+fileName+UUID.randomUUID().toString();
		ProtogenDataEngine pde = new ProtogenDataEngine();
		res.setEntity(pde.getReferencedTable(entity));
		res.setFileName(fileName);
		res.setFullPath(path);
		res.setIdentifiant(identifiant);
		
		byte[] content = stream.getBytes();
		OutputStream os = new FileOutputStream(res.getFullPath());
		IOUtils.write(content, os);
		os.close();
		
		FileStoreDAO dao = new FileStoreDAO();
		dao.insertFile(res);
		
		return res;
	}
	
	public String load(int identifiant, String table) throws IOException{
		String stream = "";
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass c = pde.getReferencedTable(table);
		
		FileStoreDAO dao = new FileStoreDAO();
		FFileStore f = dao.selectFile(identifiant, c);
		
		InputStream is = new FileInputStream(f.getFullPath());
		byte[] content = IOUtils.toByteArray(is);
		is.close();
		
		stream = new String(content);
		
		return stream;
	}
}
