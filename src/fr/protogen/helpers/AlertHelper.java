package fr.protogen.helpers;

import java.util.UUID;

public class AlertHelper {

	public String formatAlert(String title, String description){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><alert><title><![CDATA[\""+title+"\"]]></title><description><![CDATA[\""+description+"\"]]></description></alert>";
	}

	
}
