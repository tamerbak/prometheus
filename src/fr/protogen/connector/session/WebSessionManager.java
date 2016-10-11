package fr.protogen.connector.session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import fr.protogen.masterdata.dbutils.DBUtils;

public class WebSessionManager {
	private static WebSessionManager instance=null;
	private List<WebSession> openSessions = new ArrayList<WebSession>();
	private Connection cnx=null;
	
	public synchronized static WebSessionManager getInstance(){
		if(instance == null){
			instance = new WebSessionManager();
			try {
				instance.cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//DBUtils.ds.getConnection();
		}
		return instance;
	}
	
	private WebSessionManager(){}
	
	
	public synchronized String authenticate(String appID){
		if(!appID.equals(ClientApplications.CRM) && !appID.equals(ClientApplications.GED))
			return "";
		
		WebSession ws = new WebSession();
		ws.setAppID(appID);
		ws.setSessionID(UUID.randomUUID().toString());
		ws.setStartTime(new Date());
		
		if(openSessions == null)
			openSessions = new ArrayList<WebSession>();
		
		openSessions.add(ws);
		
		return ws.getSessionID();
	}
	
	public synchronized String ttlCheck(String sessionID){
		
		WebSession os = null;
		for(WebSession ws : openSessions)
			if(ws.getSessionID().equals(sessionID)){
				os = ws;
				break;
			}
		
		if(os == null)
			return "";
		
		Date od = os.getStartTime();
		DateTime old = new DateTime(od.getTime());
		DateTime now = new DateTime();
		Interval interval = new Interval(old,now);
		if(interval.toDuration().getStandardDays()>0){
			openSessions.remove(os);
			return "";
		}
		
		return sessionID;
	}
	
	/*
	 * 	GETTERS AND SETTERS
	 */
	public List<WebSession> getOpenSessions() {
		return openSessions;
	}

	public void setOpenSessions(List<WebSession> openSessions) {
		this.openSessions = openSessions;
	}

	public synchronized Connection getCnx() {
		try{
			if (cnx.isClosed()){
				instance.cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			}
		}catch(Exception exc){
			
		}
		return cnx;
	}

	public void setCnx(Connection cnx) {
		this.cnx = cnx;
	}

	
}
