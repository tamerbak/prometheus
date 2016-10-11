package fr.protogen.engine.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;

public class StringFormat {
	private static StringFormat instance = null;
	
	public static synchronized StringFormat getInstance(){
		if(instance == null)
			instance = new StringFormat();
		return instance;
	}
	
	private StringFormat(){
		
	}
	
	public String format(String value){
		String nvalue=value.replace("'", "''");
		return nvalue;
	}
	
	public String tableDataReferenceFormat(String table){
		String dataReference="user_";
		for(int i = 0 ; i < table.length() ; i++){
			char c = table.charAt(i);
			switch(c){
				case 'é':case 'è':case 'ê': c='e';break;
				case 'à':case 'â':case 'ä':c='a';break;
				case 'ç':c='c';break;
				case 'ô':c='o';break;
				case 'ï':c='i';break;
				case 'û':case 'ù':c='u';break;
				case ' ':case '\t':case '\'':c='_';break;
				case '/':case'-':case':':case';':c='_';break;
			}
			dataReference = dataReference+c;
		}
		
		return dataReference.toLowerCase();
		
	}

	public String attributeDataReferenceFormat(String attributeTitle) {
		// TODO Auto-generated method stub
		String dataReference="";
		for(int i = 0 ; i < attributeTitle.length() ; i++){
			char c = attributeTitle.charAt(i);
			switch(c){
				case 'é':case 'è':case 'ê': c='e';break;
				case 'à':c='a';break;
				case 'ç':c='c';break;
				case 'ô':c='o';break;
				case ' ':case '\t':case '\'':c='_';break;
				case '/':case'-':case':':case';':c='_';break;
			}
			dataReference = dataReference+c;
		}
		
		return dataReference.toLowerCase();
	}

	public String tryParse(String value) {
		// TODO Auto-generated method stub
		String v = "-1111111";
		
		if(v.split("-").length==3)
			return value;
		
		try {
			double d = Double.parseDouble(value);
			v = d+"";
		}catch(Exception e){
			v="-1111111";
		}
		
		return v;
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
	    return bd.doubleValue();
	}

	public List<String> getInjectedValues(String formulaSource) {
		
		List<String> injectedValues = new ArrayList<String>();
		
		int index=0;
		boolean flag=true;
		while(flag){
			try{
				String iv = formulaSource.substring(formulaSource.indexOf("<<", index)+2, formulaSource.indexOf(">>", index));
				
				if(iv.startsWith("VALEUR_"))
					injectedValues.add(iv);
				
				index = formulaSource.indexOf(">>", index)+1;
			} catch(Exception e){
				flag=false;
			}
			
		}
		
		return injectedValues;
	}
	
	public String fileToString(String file) {
	    try{  
	    	String mail="";
	    	File fileDir = new File(file);
	    	 
			BufferedReader in = new BufferedReader(
			   new InputStreamReader(
	                      new FileInputStream(fileDir), "UTF8"));
	 
			String str;
	 
			while ((str = in.readLine()) != null) {
			    mail=mail+"\n"+str;
			}
	 
            in.close();
            return mail;
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}


    }

	public List<String> decomposeConstraint(String c) {
		List<String> dc = new ArrayList<String>(); 
		if(c.contains("=")){
			dc.add(c.split("=")[0]);
			dc.add("=");
			dc.add(c.split("=")[1]);
		}
		if(c.contains("<")){
			dc.add(c.split("<")[0]);
			dc.add("<");
			dc.add(c.split("<")[1]);
		}
		if(c.contains(">")){
			dc.add(c.split(">")[0]);
			dc.add(">");
			dc.add(c.split(">")[1]);
		}
		return dc;
	}

	public String formatPhone(String tel) {
		if(tel.startsWith("+"))
			return "00"+tel.substring(1);
		if(tel.startsWith("00"))
			return tel;
		if(tel.startsWith("0"))
			return "0033"+tel.substring(1);
		return null;
	}
}
