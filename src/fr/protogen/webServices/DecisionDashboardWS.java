/**
 * 
 */
package fr.protogen.webServices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.primefaces.json.JSONException;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.security.Md5;

/**
 * @author developer
 *
 */
@Path("/decision_dashboard")
public class DecisionDashboardWS {
	
	private static Map<String, String> OPERATORS = new HashMap<String, String>();
	static {
		OPERATORS.put("e", "=");
		OPERATORS.put("ne", "<>");
		OPERATORS.put("l", "<");
		OPERATORS.put("le", "<=");
		OPERATORS.put("g", ">");
		OPERATORS.put("ge", ">=");
		OPERATORS.put("in", "in");
	}
	@SuppressWarnings("unchecked")
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public String loadDashboard(@QueryParam("appKey") String appKey,@QueryParam("role") String role,  @QueryParam("token") String hashToken, @QueryParam("filters") String filters) {
		
		
		if(!Md5.encode(AuthenticationWS.TOKEN_STRING).equals(hashToken)) {
			return "{\"error\":\"authentiticaiton failed\"}";
		}
		String query = "SELECT ft.* FROM c_fact_table ft, c_role__fact_table rft "
				+ "WHERE ft.id=rft.fact_table_id AND rft.role_id="+role+"  AND ft.appKey='"+appKey+"'";
		
		try {
			//getting DB Connection
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    Statement ps = cnx.createStatement();
			ResultSet apprs = ps.executeQuery(query);
			JSONObject dashboard = new JSONObject();
			//fetching results
			while(apprs.next()){
				JSONObject oneDash = new JSONObject();
				oneDash.put("query", apprs.getString("query"));
				oneDash.put("table_id", apprs.getInt("id"));
				oneDash.put("view_name", apprs.getString("view_name"));
				dashboard.put(apprs.getString("tablename"), oneDash);
				
			}
			ps.close();
			apprs.close();
			for(Iterator dashIt = dashboard.keySet().iterator(); dashIt.hasNext(); ){
				String dashName = (String) dashIt.next();
				JSONObject header = new JSONObject();
				String attrQuery = "select * from c_fact_table_attribute where fact_table_id="+((JSONObject)dashboard.get(dashName)).get("table_id").toString();
				
				String dataQuery = ((JSONObject)dashboard.get(dashName)).get("query").toString();
				String viewName = ((JSONObject)dashboard.get(dashName)).get("view_name").toString();
				try {
					dataQuery = generateViewQuery(dashName, viewName, filters);
				}catch(Exception e) {
					e.printStackTrace();
				}
				
				Statement ps1 = cnx.createStatement();
				ResultSet attrs = ps1.executeQuery(attrQuery);
				int comptXDim = 1;
				int comptYDim = 1;
				int comptDim = 1;
				List<String> labelX = new ArrayList<String>();
				List<String> labelY = new ArrayList<String>();
				JSONObject values = new JSONObject();
				while(attrs.next()) {
					if(attrs.getString("isxdimension").equals("Y")) {
					header.put(comptXDim++, attrs.getString("labelattribute"));
						labelX.add(attrs.getString("labelattribute"));
					}else if(attrs.getString("isydimension").equals("Y")) {
						header.put(comptXDim+comptYDim++, attrs.getString("labelattribute"));
						labelY.add(attrs.getString("labelattribute"));
					}else {
						header.put(comptXDim+comptYDim+comptDim++, attrs.getString("labelattribute"));
					}
					values.put(attrs.getString("labelattribute"), new JSONArray());
				}
				Statement ps2 = cnx.createStatement();
				System.out.println("requete sql :\n"+dataQuery);
				ResultSet rsData = ps2.executeQuery(dataQuery);
				JSONArray jData = new JSONArray();
				while(rsData.next()) {
					//   store data by header key {labelattribute:data}
					JSONObject line = new JSONObject();
					for(Iterator<Object> it = header.values().iterator(); it.hasNext();) {
						String prop = (String) it.next();
						if(labelY.contains(prop)) {
							try {
								line.put(prop, Double.parseDouble(rsData.getString(prop)));
							}catch(Exception e) {
								line.put(prop, 0);
							}
						}else if(labelX.contains(prop)){
							line.put(prop, rsData.getString(prop));
						}
						if(labelY.contains(prop)) {
							try {
								((JSONArray)values.get(prop)).add(Double.parseDouble(rsData.getString(prop)));
							}catch(Exception e) {
								((JSONArray)values.get(prop)).add(0);
							}
						}else if(labelX.contains(prop)){
							((JSONArray)values.get(prop)).add(rsData.getString(prop));
						}
						
					}
					jData.add(line);
				}
				 ((JSONObject)dashboard.get(dashName)).put("labelX", new org.primefaces.json.JSONArray(labelX));
				 ((JSONObject)dashboard.get(dashName)).put("labelY", new org.primefaces.json.JSONArray(labelY));
				 ((JSONObject)dashboard.get(dashName)).put("values", values);
				 ((JSONObject)dashboard.get(dashName)).put("header", header);
				 ((JSONObject)dashboard.get(dashName)).put("data", jData);
			}
			
			return dashboard.toJSONString();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new JSONObject().toJSONString();
	}
	
	private String generateViewQuery(String tableName, String viewName, String filters) throws JSONException {
		String query  = "SELECT * FROM "+viewName+"";
		if(filters != null ) {
			org.primefaces.json.JSONObject jFilters = new org.primefaces.json.JSONObject(filters);
			if( jFilters.length() != 0 && jFilters.getJSONArray(tableName)!= null && jFilters.getJSONArray(tableName).length() != 0) {
				query += " WHERE ";
				for(int i = 0; i < jFilters.getJSONArray(tableName).length(); i++) {
				
					org.primefaces.json.JSONObject  filt = jFilters.getJSONArray(tableName).getJSONObject(i);
					String propOp = "\""+filt.getString("prop")+"\"";
					String opera = OPERATORS.get(filt.getString("op"));
					String val = filt.getString("val");
					if("0".equals(val)) {
						String isNil =" is null ";
						if(opera.equals("ne")) {
							isNil = " is not null ";
						}
						query += propOp +isNil +" OR " +propOp+" "+opera + " '" + val + "'";
					}else {
						query += propOp + " " + opera + " '" + val + "'";
					}
					
					if((i+1) < jFilters.getJSONArray(tableName).length()) {
						query += " AND ";
					}
				}
			}
		}
		return query;
	}
}
