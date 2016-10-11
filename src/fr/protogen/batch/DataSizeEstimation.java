package fr.protogen.batch;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class DataSizeEstimation {

	public static void main(String[] args) {
		List<String> tables = new ArrayList<String>(); 
		String sql = "select data_reference from c_businessclass";
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next())
		    	tables.add(rs.getString(1));
		    
		    rs.close();ps.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
	    double avg=0;
	    double max=0;
	    double min=0;
	    List<Double> vals = new ArrayList<Double>();
	    for(String t:tables){
	    	sql = "SELECT avg(octet_length(t.*::text)) FROM "+t+" AS t;";
	    	try {
				Class.forName("org.postgresql.Driver");

			    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			    
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    ResultSet rs = ps.executeQuery();
			    if(rs.next())
			    	vals.add(new Double(rs.getDouble(1)));
			    
			    rs.close();
			    ps.close();
			    
	    	}catch(Exception exc){
	    		exc.printStackTrace();
	    	}
	    }
		
	    // Evaluation
	    if(vals.size()==0)
	    	return;
	    max = vals.get(0);
	    min = vals.get(0);
	    
	    for(Double d : vals){
	    	if(d>max)
	    		max=d;
	    	if(d<min)
	    		min=d;
	    	avg = avg+d;
	    }
	    
	    String dataFile = "d:\\datasize.txt";
	    String si="";
	    String sv="";
	    for(int i=0;i<vals.size();i++){
	    	si = si+i+",";
	    	sv = sv+vals.get(i)+",";
	    }
	    si = si.substring(0,si.length()-1);
	    sv = sv.substring(0,sv.length()-1);
	    
	    String data = si+"\n"+sv;
	    try {
			IOUtils.write(data.getBytes(), new FileOutputStream(dataFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    avg = avg/vals.size();
		    
		System.out.println("-------------------------");
		System.out.println("|\tMIN\tMAX\tMOY\t|");
		System.out.println("-------------------------");
		System.out.println("|\t"+min+"\t"+max+"\t"+avg+"\t|");
		System.out.println("-------------------------");
	}

}
