package fr.protogen.engine.control;

import java.awt.Desktop;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import fr.protogen.engine.utils.ProtogenParameters;

@ManagedBean
@SessionScoped
public class SendAppelControl {

	private String dummy="-";
	
	@PostConstruct
	public void postload(){
		Desktop desktop = null; 
		java.net.URI url; 
		try { 
		url = new java.net.URI(ProtogenParameters.APPLICATION+"/prometheus/protogen-sendappel"); 
		if (Desktop.isDesktopSupported()) 
		{ 
		desktop = Desktop.getDesktop(); 
		desktop.browse(url); 
		} 
		} 
		catch (Exception ex) { 
		 ex.printStackTrace();
		System.out.println(ex.getMessage()); 
		}
	}

	public String getDummy() {
		return dummy;
	}

	public void setDummy(String dummy) {
		this.dummy = dummy;
	}
}
