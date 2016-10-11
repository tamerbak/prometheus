package fr.protogen.event.geb.communication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import fr.protogen.event.geb.EventModel.GEventInstance;
import fr.protogen.event.geb.EventModel.PEASms;
import fr.protogen.masterdata.model.CoreUser;

public class SmsClient {
	private static SmsClient instance = null;
	public static synchronized SmsClient getInstance(){
		if(instance == null)
			instance=new SmsClient();
		return instance;
	}
	
	private SmsClient(){}
	
	public void sendSMS(String u, PEASms p, GEventInstance evt){
		String tel = u;
		String text = p.getText();
		text = text.replaceAll("<<message>>", evt.getContent());
		
		sendSMS(text, tel);
	}
	
	private void sendSMS(String text, String number){
		if(number == null || number.length()<3)
			return;
		number = number.substring(2);
		try{
			String data = "";
			data += "username=" + URLEncoder.encode("jakjoud", "ISO-8859-1");
	        data += "&password=" + URLEncoder.encode("AjmeST0553", "ISO-8859-1");
	        data += "&message=" + URLEncoder.encode(text, "ISO-8859-1");
	        data += "&want_report=1";
	        data += "&msisdn="+number;
	        
	        URL url = new URL("http://bulksms.vsms.net:5567/eapi/submission/send_sms/2/2.0");
	        URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
            wr.close();
            rd.close();
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
}

