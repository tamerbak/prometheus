package fr.protogen.engine.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FileManipulation {
	private static FileManipulation instance=null;
	private String serverPath="";
	
	private FileManipulation(String serverPath) {
		this.serverPath = serverPath;
	}

	public static synchronized FileManipulation getInstance(String serverPath){
		if(instance==null)
			instance = new FileManipulation(serverPath);
		return instance;
	}
	
	public synchronized String saveTempFile(String fileExt, InputStream is, String fileUser) throws Exception{
		String fileName = serverPath+fileUser+fileExt;
		
		File f = new File(fileName);
	    if(!f.exists())
	    	f.createNewFile();
	    
	    byte[] data = org.apache.commons.io.IOUtils.toByteArray(is);
		FileOutputStream w = new FileOutputStream(f);
		w.write(data);
	    
	    w.close();
	    is.close();
		
		return fileName;
		
	}
	
	public synchronized void deleteTempFile(String file){
		try{
			File f = new File(file);
			f.delete();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void saveFile(String fn, InputStream is) throws IOException {
		
		String fileName = fn;
		
		File f = new File(fileName);
	    if(!f.exists())
	    	f.createNewFile();
	    
	    byte[] data = org.apache.commons.io.IOUtils.toByteArray(is);
		FileOutputStream w = new FileOutputStream(f);
		w.write(data);
	    
	    w.close();
	    is.close();
		
		
		
	}

	public void copyFiles(String ssource, String sdest) throws Exception {
			
		File source = new File(ssource);
		File dest = new File(sdest);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
	    
		
	}
}
