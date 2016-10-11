package fr.protogen.engine.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

public class ResourceManager {
	private String messagesFilePath = "/lang/messages.properties";
	Properties prop = new Properties(); 
	
	private static ResourceManager instance = null;
	
	public static synchronized ResourceManager getInstance(){
		if(instance==null)
			instance = new ResourceManager();
		
		return instance;
	}
	
	private ResourceManager(){
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
		    ExternalContext ec = fc.getExternalContext();
		    messagesFilePath = ec.getRealPath(".")+messagesFilePath;
			prop.load(new FileInputStream(messagesFilePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getMessage(String identifier){
		
		return prop.getProperty(identifier);
		
	}
}
