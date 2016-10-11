package fr.protogen.communication.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;


public class SmsClient {
	private static SmsClient instance = null;
	public static synchronized SmsClient getInstance(){
		if(instance == null)
			instance=new SmsClient();
		return instance;
	}
	
	private SmsClient(){}
	public void sendSMS(String m, String number) throws IOException{
		number = number.substring(2);
		String data = "";
		data += "username=" + URLEncoder.encode("vitonjob", "ISO-8859-1");
        data += "&password=" + URLEncoder.encode("Polytechnique**@2016", "ISO-8859-1");
        data += "&message=" + URLEncoder.encode(m, "ISO-8859-1");
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
			
		}
	

}
