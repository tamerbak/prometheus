package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.control.CalculusEngine;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.Trigger;

public class TriggersEngine {
	private static TriggersEngine instance= null;
	public static synchronized TriggersEngine getInstance(){
		if(instance==null)
			instance = new TriggersEngine();
		return instance;
	}
	private TriggersEngine(){}
	
	public void trigger(Trigger trigger, List<UIControlElement> controls, List<MtmDTO> dtos ){
		//	we start with direct references
		for(UIControlElement c : controls){
			if(c.getAttribute().getDataReference().substring(3).equals(trigger.getReference().getDataReference())){
				//	We are in
				String sourceCode = trigger.getFormula();
				ProtogenDataEngine engine = new ProtogenDataEngine();
				CBusinessClass entity =   engine.getEntityByName(trigger.getReference().getName());
				String sql;
				if(trigger.isDirect())
					sql = "select * from "+trigger.getReference().getDataReference()+" where pk_"+trigger.getReference().getDataReference()+"="+c.getControlValue();
				else
					sql = "select * from "+trigger.getReference().getDataReference()+" where fk_"+trigger.getReference().getDataReference()+"="+c.getControlValue();
				Map<CAttribute, String> values = new HashMap<CAttribute, String>();
				try {
				    Class.forName("org.postgresql.Driver");

				    Connection cnx = ProtogenConnection.getInstance().getConnection();
				    
				    PreparedStatement ps = cnx.prepareStatement(sql);
				    ResultSet rs = ps.executeQuery();
				    
				    if(rs.next()){
				    	for(CAttribute a : entity.getAttributes()){
				    		values.put(a, rs.getObject(a.getDataReference()).toString());
				    	}
				    }
				    
				    rs.close();
				    ps.close();
				    
				}catch(Exception exc){
					exc.printStackTrace();
				}
				
				//	Eliminate attributes from original table
				for(CAttribute a : values.keySet()){
					sourceCode = sourceCode.replaceAll("<<"+trigger.getReference().getName()+"."+a.getAttribute()+">>", values.get(a));
				}
				
				//	Eliminate attributes from view
				for(UIControlElement e : controls){
					sourceCode = sourceCode.replaceAll("<<"+e.getAttribute().getAttribute()+">>", e.getControlValue());
				}
				
				double result = 0;
				
				try{
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine calculus = new CalculusEngine(ec.getRealPath(""));
					
					result = calculus.executePlainTextCode(sourceCode).get(0).doubleValue();
					
				}catch(Exception exc){
					exc.printStackTrace();
				}
				
				sql = "update "+trigger.getReference().getDataReference()+" set "+trigger.getTarget().getDataReference()+"="+result+" where pk_"+trigger.getReference().getDataReference()+"="+c.getControlValue();
				try {
				    Class.forName("org.postgresql.Driver");

				    Connection cnx = ProtogenConnection.getInstance().getConnection();
				    
				    PreparedStatement ps = cnx.prepareStatement(sql);
				    ps.execute();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			}
		}
		
		//	Now we atack mtm
		for(MtmDTO dto : dtos){
			boolean flag = false;
			CAttribute referenceAttribute=new CAttribute();
			for(CAttribute a : dto.getMtmEntity().getAttributes())
				if(a.getDataReference().substring(3).equals(trigger.getReference().getDataReference())){
					flag=true;
					referenceAttribute = a;
					break;
				}
			if(!flag)
				continue;
			
			ProtogenDataEngine engine = new ProtogenDataEngine();
			CBusinessClass entity =   engine.getEntityByName(trigger.getReference().getName());
			for(Map<CAttribute, Object> dvals : dto.getMtmData()){
				String sql = "select * from "+trigger.getReference().getDataReference()+" where pk_"+trigger.getReference().getDataReference()+"="+dvals.get(referenceAttribute).toString();
				Map<CAttribute, String> values = new HashMap<CAttribute, String>();
				try {
				    Class.forName("org.postgresql.Driver");
	
				    Connection cnx = ProtogenConnection.getInstance().getConnection();
				    
				    PreparedStatement ps = cnx.prepareStatement(sql);
				    ResultSet rs = ps.executeQuery();
				    
				    if(rs.next()){
				    	for(CAttribute a : entity.getAttributes()){
				    		if(checkForColumn(rs, a))
				    			values.put(a, rs.getObject(a.getDataReference())==null?"":rs.getObject(a.getDataReference()).toString());
				    	}
				    }
				    
				    rs.close();
				    ps.close();
				    
				}catch(Exception exc){
					exc.printStackTrace();
				}
				
				String sourceCode  = trigger.getFormula();
				
				//	Eliminate attributes from original table
				for(CAttribute a : values.keySet()){
					sourceCode = sourceCode.replaceAll("<<"+trigger.getReference().getName()+"."+a.getAttribute()+">>", values.get(a));
				}
				
				//	Eliminate attributes from view
				for(UIControlElement e : controls){
					sourceCode = sourceCode.replaceAll("<<"+e.getAttribute().getAttribute()+">>", e.getControlValue());
				}		
				
				//	Eliminate attributes of mtm
				for(CAttribute a : dvals.keySet()){
					sourceCode = sourceCode.replaceAll("<<"+trigger.getReference().getName()+"."+a.getAttribute()+">>", dvals.get(a).toString());
				}
				
				
				double result = 0;
				
				try{
					FacesContext fc = FacesContext.getCurrentInstance();
				    ExternalContext ec = fc.getExternalContext();
					
					CalculusEngine calculus = new CalculusEngine(ec.getRealPath(""));
					
					result = calculus.executePlainTextCode(sourceCode).get(0).doubleValue();
					
				}catch(Exception exc){
					exc.printStackTrace();
				}
				
				sql = "update "+trigger.getReference().getDataReference()+" set "+trigger.getTarget().getDataReference()+"="+result+" where pk_"+trigger.getReference().getDataReference()+"="+dvals.get(referenceAttribute).toString();
				try {
				    Class.forName("org.postgresql.Driver");

				    Connection cnx = ProtogenConnection.getInstance().getConnection();
				    
				    PreparedStatement ps = cnx.prepareStatement(sql);
				    ps.execute();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			}
		}
		
	}
	private boolean checkForColumn(ResultSet rs, CAttribute a) throws SQLException {
		// TODO Auto-generated method stub
		
		ResultSetMetaData rsm = rs.getMetaData();
		
		for(int i=1; i<=rsm.getColumnCount();i++ ){
			if(rsm.getColumnLabel(i).equals(a.getDataReference()))
				return true;
		}
		
		return false;
	}
}
