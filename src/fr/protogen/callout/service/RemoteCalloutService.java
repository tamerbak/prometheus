package fr.protogen.callout.service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.itextpdf.text.pdf.codec.Base64;

public class RemoteCalloutService {
	private static RemoteCalloutService instance = null;
	public static synchronized RemoteCalloutService getInstance(){
		if(instance == null)
			instance = new RemoteCalloutService();
		return instance;
	}
	private RemoteCalloutService(){}
	
	public String remoteCallout(String arg, int idc) throws IOException, JSONException{
		String url="http://vitonjobv1.datqvvgppi.us-west-2.elasticbeanstalk.com/api/business";
		String result = "";
		
		URL object=new URL(url);

		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");
		
		JSONObject parent = new JSONObject();
		parent.put("class","fr.protogen.masterdata.model.CCallout");
		parent.put("id", idc);
		JSONArray ja = new JSONArray();
		JSONObject child = new JSONObject();
		child.put("class", "fr.protogen.masterdata.model.CCalloutArguments");
		child.put("label", "Libelle");
		child.put("value", arg);
		ja.put(child);
		parent.put("args", ja);
		
		OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
		wr.write(parent.toString());
		wr.flush();
		
		StringBuilder sb = new StringBuilder();
		int HttpResult = con.getResponseCode(); 
		if(HttpResult == HttpURLConnection.HTTP_OK){
			
		    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));  
		    String line = null;  
		    while ((line = br.readLine()) != null) {  
		        sb.append(line + "\n");  
		    }  

		    br.close();  
		    result = sb.toString();
		    System.out.println("RESULT : "+result);
		    System.out.println("CALLOUT EXECTURED");
		    
		}else{
		    System.out.println(con.getResponseMessage());  
		} 
		
		return result;
	}
	
	public String remoteCallout(Map<String,String> args, int idc) throws IOException, JSONException{
		String url="http://vitonjobv1.datqvvgppi.us-west-2.elasticbeanstalk.com/api/business";
		String result = "";
		
		URL object=new URL(url);

		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");
		
		JSONObject parent = new JSONObject();
		parent.put("class","fr.protogen.masterdata.model.CCallout");
		parent.put("id", idc);
		JSONArray ja = new JSONArray();
		for(String k : args.keySet()){
			String lib = k;
			String arg = args.get(k);
			JSONObject child = new JSONObject();
			child.put("class", "fr.protogen.masterdata.model.CCalloutArguments");
			child.put("libelle", lib);
			child.put("value", arg);
			ja.put(child);
		}
		parent.put("args", ja);
		
		OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
		wr.write(parent.toString());
		wr.flush();
		
		StringBuilder sb = new StringBuilder();
		int HttpResult = con.getResponseCode(); 
		if(HttpResult == HttpURLConnection.HTTP_OK){
			
		    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));  
		    String line = null;  
		    while ((line = br.readLine()) != null) {  
		        sb.append(line + "\n");  
		    }  

		    br.close();  
		    result = sb.toString();
		    System.out.println("RESULT : "+result);
		    System.out.println("CALLOUT EXECTURED");
		    
		}else{
		    System.out.println(con.getResponseMessage());  
		} 
		
		return result;
	}
}
