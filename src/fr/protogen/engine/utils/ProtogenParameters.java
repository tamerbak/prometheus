package fr.protogen.engine.utils;

import javax.faces.context.FacesContext;

public class ProtogenParameters {
	public static final String APPLICATION="http://ns389914.ovh.net:8080";
	public static final String SERVER_PATH=FacesContext.getCurrentInstance().getExternalContext().getRealPath("");
	
}