package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.faces.context.FacesContext;

import fr.protogen.dataload.QueryBuilder;
import fr.protogen.masterdata.model.SUIAlert;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.AlertInstance;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.SAlert;

public class AlertDataAccess {
	
	public void insertAlert(AlertInstance instance){
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "insert into s_alert_instance " +
					"(alerte,message,creation,seen,seen_on,closed)" +
					" values " +
					"(?,?,'"+(new Date()).toString()+"','N','"+(new Date()).toString()+"','N')";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, instance.getAlert().getId());
			ps.setString(2,instance.getMessage());
			
			ps.execute();
			
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public List<SAlert> getAlertByScreen(CWindow window){
		List<SAlert> results = new ArrayList<SAlert>();
		
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "select s_alert_id, titre, description from s_alert where s_window=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, window.getId());
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				SAlert a = new SAlert();
				a.setId(rs.getInt(1));
				a.setTitre(rs.getString(2));
				a.setDescription(rs.getString(3));
				
				results.add(a);
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return results;
	}
	
	public boolean checkAlerts(int userRole) {
		boolean flag=false;
		
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "SELECT s_alert_instance.id " +
					"FROM public.s_alert, public.s_alert_instance, public.core_role " +
					"WHERE s_alert.role = core_role.id AND s_alert_instance.alerte = s_alert.s_alert_id and closed='N' and core_role.id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, userRole);
			ResultSet rs = ps.executeQuery();
			flag=rs.next();
			
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
	}
	
	
	
	public List<AlertInstance> getMyAlerts(int id) {
		List<AlertInstance> alerts = new ArrayList<AlertInstance>();
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "SELECT s_alert_instance.id, s_alert_instance.message, s_alert_instance.creation, s_alert_instance.seen, s_alert_instance.seen_on, s_alert_instance.closed " +
					" FROM public.s_alert, public.s_alert_instance, public.core_role " +
					" WHERE s_alert.role = core_role.id AND s_alert_instance.alerte = s_alert.s_alert_id and closed='N' and core_role.id=? order by s_alert_instance.creation desc";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				AlertInstance a = new AlertInstance();
				a.setId(rs.getInt(1));
				a.setMessage(rs.getString(2));
				a.setCreated(rs.getDate(3));
				a.setSeen(rs.getString(4).equals("Y"));
				a.setSeenOn(rs.getDate(5));
				a.setClosed(rs.getString(6).equals("Y"));
				alerts.add(a);
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return alerts;
	}
	
	public void closeAlert(AlertInstance currentAlert) {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
		
			String sql = "update s_alert_instance set closed='Y' where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, currentAlert.getId());
			
			ps.execute();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}	
		
	}
	
	public boolean checkAlert(UUID identifier){
		
		try {
		    Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();
			Statement st = cnx.createStatement();
			
			QueryBuilder builder = new QueryBuilder();
			String query = builder.buildSelectAlert(identifier);
			
			ResultSet rs = st.executeQuery(query);
			
			if(rs.next())
				return true;
			return false;
			
			
		}catch(Exception exc){
			
		}
		
		return true;
	}

	public void insertUIAlert(SUIAlert alert) {
		// TODO Auto-generated method stub
		try {
		    Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();
			Statement st = cnx.createStatement();
			
			QueryBuilder builder = new QueryBuilder();
			String query = builder.buildCreateUIAlert(alert);
			
			st.execute(query);
			
			
		}catch(Exception exc){
			
		}
	}

	public SUIAlert getUIAlert(UUID identifier) {
		// TODO Auto-generated method stub
		
		try {
		    Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();
			Statement st = cnx.createStatement();
			
			QueryBuilder builder = new QueryBuilder();
			String query = builder.buildSelectUIAlert(identifier);
			
			ResultSet rs = st.executeQuery(query);
			
			if(rs.next()){
				SUIAlert alert = new SUIAlert();
				alert.setIdentifier(identifier.toString());
				alert.setOutputData(rs.getString("outputData"));
				return alert;
			}
			
			
		}catch(Exception exc){
			
		}
		return null;
	}

	public void deleteUIAlert(UUID identifier) {
		// TODO Auto-generated method stub
		try {
		    Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();
			Statement st = cnx.createStatement();
			
			QueryBuilder builder = new QueryBuilder();
			String query = builder.buildDeleteUIAlert(identifier.toString());
			
			st.execute(query);
			
			
		}catch(Exception exc){
			
		}
	}
	
	//	Methods to retrieve existing alerts
	public List<SAlert> getExistingAlerts(){
		/*QueryBuilder builder = new QueryBuilder();
		String query = builder.buildSelectAllAlerts();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			List<SAlert> alerts = new ArrayList<SAlert>();
			
			while(rs.next()){
				SAlert alert = new SAlert();
				alert.setSAlertID(rs.getInt("s_alert_id"));
				alert.setAlertAccessID(rs.getString("alert_access_id"));
				alert.setAlertStream(rs.getString("xml_stream"));
				alerts.add(alert);
			}
			
			rs.close();
			st.close();
			
			
			return alerts;
			
		}catch(Exception exc){
			
		}
		*/
		return null;
	}

	

	
	
}
