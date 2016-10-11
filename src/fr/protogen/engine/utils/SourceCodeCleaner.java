package fr.protogen.engine.utils;

import java.util.ArrayList;
import java.util.List;

public class SourceCodeCleaner {

	public List<String> getHeaders(String sourceCode){
		List<String> headers = new ArrayList<String>();
		String code = sourceCode;
		if(!sourceCode.toLowerCase().startsWith("\"calculer"))
			return headers;
		
		code = code.substring(1,code.lastIndexOf('"')-1);
		String[] th = code.split(";");
		
		for(String h : th)
			headers.add(h);
		
		return headers;
	}
	
	public String getHeaderValueSegment(String header){
		String val="";
		
		val = header.split(" dans ")[0];
		val = val.substring(8); //	Remove Calculer
		val = val.trim();
		
		return val;
	}
	
	public String getHeaderVariableSegment(String header){
		String val="";
		
		val = header.split(" à partir de ")[0];
		val = val.split(" dans ")[1];
		val = val.trim().replaceAll("<<", "").replaceAll(">>", "");
		
		return val;
	}
	
	public String getHeaderReferenceKey(String header){
		String val="";
				
		val = header.split(" avec ")[0];
		val = val.split(" à partir de ")[1];
		val = val.trim();
		
		return val;
	}
	
	public List<String> getHeaderConstraints(String header){
		List<String> constraints = new ArrayList<String>();
		
		if(header.split(" avec ").length==1)
			return constraints;
		
		String h = header;
		h = h.split(" avec ")[1];
		h = h.replaceAll(";", "");
		
		String[] cs = h.split(" et ");
		
		for(String c : cs){
			c.trim();
			c = SpecialValuesEngine.getInstance().parseSpecialValues(c);
			constraints.add(c);
		}
		
		return constraints;
	}
	
	public String getHeaderConstraintTable(String c){
		String t="";
		
		t = c.split(">>")[0];
		t = (t.split("<<").length>1)?t.split("<<")[1]:t.split("<<")[0];
		
		return t.split("\\.")[0];
	}
	public String getHeaderConstraintColumn(String c){
		String t="";
		
		t = c.split(">>")[0];
		t = (t.split("<<").length>1)?t.split("<<")[1]:t.split("<<")[0];
		
		return t.split("\\.")[1];
	}
}
