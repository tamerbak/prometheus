package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CParametersWindow;
import fr.protogen.masterdata.model.CUIParameter;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SProcess;
import fr.protogen.masterdata.model.SResource;
import fr.protogen.masterdata.model.SScheduledCom;
import fr.protogen.masterdata.model.SStep;
import fr.protogen.masterdata.model.StepType;

public class SProcessDataAccess {

	public List<SProcess> getAllProccesses(String appKey){
		String query = "select * from s_process where appkey='"+appKey+"'";
		
		try {
		    Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			List<SProcess> processes = new ArrayList<SProcess>();
			
			while(rs.next()){
				SProcess process = new SProcess();
				process.setId(rs.getInt("id"));
				process.setStream(rs.getString("stream"));
				process.setTitle(rs.getString("title"));
				process.setDescription(rs.getString("description"));
				processes.add(process);
			}
			
			rs.close();
			st.close();
					
			
			for(SProcess p : processes){
				String pquery = "select id_c_window, is_parameter, parameters from s_process_window where id_s_process="+p.getId()+" order by s_process_window.\"order\"";
				cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
				st = cnx.createStatement();
				rs = st.executeQuery(pquery);
				List<CWindow> windows = new ArrayList<CWindow>();
				while(rs.next()){
					if(rs.getInt("is_parameter") == 0){
						CWindow window = new CWindow();
						window.setId(rs.getInt("id_c_window"));
						window.setParameters(rs.getString("parameters"));
						windows.add(window);
						
					} else {
						CParametersWindow window = new CParametersWindow();
						window.setId(rs.getInt("id_c_window"));
						windows.add(window);
					}
				}
				
				rs.close();
				st.close();
				
				p.setWindows(windows);
				for(CWindow w : windows){
					ApplicationLoader loader = new ApplicationLoader();
					if(w instanceof CParametersWindow){
						w = loader.loadFullParametersScreen(w.getId());
					}else{
						
						w = loader.loadFullWindow(w);
					}
				}
			}
			
			
			return processes;
			
		}catch(Exception exc){
			
		}
		
		return null;
	}

	public List<SProcedure> getProcedures(String appKey, GOrganization organization, List<CoreRole> roles) {
		
		String where =" where app_key='"+appKey+"' ";
		if(organization != null && organization.getId() > 0){
			String sroles = "";
			for(CoreRole r : roles)
				sroles = sroles+","+r.getId();
			if(sroles.length()>0){
				String sprocedures="";
				sroles = "("+sroles.substring(1)+")";
				String sql = "select procedure_id from core_acl_procedure where role_id in "+sroles;
				try{
					Connection cnx = ProtogenConnection.getInstance().getConnection();
					PreparedStatement ps = cnx.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					while(rs.next())
						sprocedures = sprocedures+","+rs.getInt(1);
					rs.close();
					ps.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
				if(sprocedures.length()>0){
					where = where+" and ("+sprocedures.substring(1)+") ";
				}
			}
		}
		List<SProcedure> procedures = new ArrayList<SProcedure>();
		
		try {
		    Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    Statement st = cnx.createStatement();
		    
			//	Select procedures		    
		    String sql = "select * from s_procedure "+where; 
		    ResultSet rs = st.executeQuery(sql);
		    
		    ProtogenDataEngine en = new ProtogenDataEngine();
		    while (rs.next()){
		    	SProcedure p = new SProcedure();
		    	p.setId(rs.getInt("id"));
		    	p.setTitle(rs.getString("title"));
		    	p.setDescription(rs.getString("description"));
		    	p.setAppKey(appKey);
		    	String[] ts = rs.getString("key_words").split(",");
		    	p.setKeyWords(new ArrayList<String>());
		    	int ide = rs.getInt("main_entity");
		    	p.setMainEntityPresent(ide > 0);
		    	if(p.isMainEntityPresent()){
		    		p.setMainEntity(en.getEntityById(ide));
		    		p.setMainEntity(en.populateEntity(p.getMainEntity()));
		    		p.setListInstances(en.getDataKeys(p.getMainEntity().getDataReference(), false, 0));
		    	}
		    	for(String k : ts)
		    		p.getKeyWords().add(k);
		    	procedures.add(p);
		    }
		    rs.close();
		    
		    for(SProcedure p : procedures){
		    	//	get filters
		    	p.setFilters(new ArrayList<CBusinessClass>());
		    	sql = "select id_bclass from s_procedure_filters where id_procedure="+p.getId();
		    	rs = st.executeQuery(sql);
		    	ProtogenDataEngine pde = new ProtogenDataEngine();
		    	while(rs.next()){
		    		CBusinessClass c = new CBusinessClass();
		    		c = pde.getEntityById(rs.getInt(1));
		    		c = pde.populateEntity(c);
		    		p.getFilters().add(c);
		    	}
		    	rs.close();
		    	
		    	//	Now get steps
		    	sql = "select id, title, description from s_procedure_step where procedure_id="+p.getId()+" order by id asc";
		    	rs = st.executeQuery(sql);
		    	p.setEtapes(new ArrayList<SStep>());
		    	while(rs.next()){
		    		SStep s = new SStep();
		    		s.setId(rs.getInt(1));
		    		s.setTitle(rs.getString(2));
		    		s.setDescription(rs.getString(3));
		    		p.getEtapes().add(s);
		    	}
		    	rs.close();
		    	
		    	for(SStep s : p.getEtapes()){
		    		sql = "select id, title, description, id_type, id_window, id_scheduled_com, id_resource, synthesis  from s_atom where id_step="+s.getId()+" order by id asc";
		    		rs = st.executeQuery(sql);
		    		s.setActions(new ArrayList<SAtom>());
		    		while (rs.next()){
		    			SAtom a = new SAtom();
		    			a.setId(rs.getInt(1));
		    			a.setTitle(rs.getString(2));
		    			a.setDescription(rs.getString(3));
		    			a.setMandatory(a.getDescription()!=null && a.getDescription().startsWith("MANDATORY"));
		    			a.setSynthesis(rs.getString("synthesis").equals("Y"));
		    			a.setType(new StepType(rs.getInt(4), ""));
		    			switch(a.getType().getId()){
		    			case 1:
		    				a.setWindow(loadWindowAtom(rs.getInt(5),cnx));
		    				a.getWindow().setAppKey(appKey);
		    				break;
		    			case 2:
		    				a.setParameters(loadParametersAtom(a.getId(),cnx));
		    				break;
		    			case 3:case 4:
		    				a.setResource(loadResourceAtom(rs.getInt(7),cnx));
		    				break;
		    			case 5:case 6:case 7:case 8:
		    				a.setCommunication(loadComAtom(rs.getInt(6),cnx));
		    				break;
		    				
		    			}
		    			s.getActions().add(a);
		    		}
		    	}
		    }
		    
		    
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return procedures;
	}

	private List<CUIParameter> loadParametersAtom(int id, Connection cnx) throws Exception {
		List<CUIParameter> parameters = new ArrayList<CUIParameter>();
		
		String query = "select p.id, p.parameter_key, p.parameter_type, p.parameter_label from c_parameter_ctrl p, s_procedure_s_parameter j where p.id=j.parameter_id and j.procedure_id="+id;
		Statement st = cnx.createStatement();
		ResultSet rs = st.executeQuery(query);
		
		while(rs.next()){
			CUIParameter p = new CUIParameter();
			p.setId(rs.getInt(1));
			p.setParameterKey(rs.getString(2));
			p.setParameterType(rs.getString(3).charAt(0));
			p.setParameterLabel(rs.getString(4));
			parameters.add(p);
		}
		rs.close();
		
		return parameters;
	}

	private SResource loadResourceAtom(int resId, Connection cnx) throws Exception {
		SResource r = new SResource();
		
		String query = "select id, title, description, filename from s_resource where id="+resId;
		Statement st = cnx.createStatement();
		ResultSet rs = st.executeQuery(query);
		if(rs.next()) {
			r.setId(rs.getInt(1));
			r.setTitle(rs.getString(2));
			r.setDescription(rs.getString(3));
			r.setFileName(rs.getString(4));
		}
		rs.close();
		
		return r;
	}

	private SScheduledCom loadComAtom(int comId, Connection cnx) throws Exception {
		SScheduledCom c = new SScheduledCom();
		
		String query = "select id, title, description, attachement_id from s_scheduled_com where id="+comId;
		
		Statement st = cnx.createStatement();
		ResultSet rs = st.executeQuery(query);
		if(rs.next()) {
			c.setId(rs.getInt(1));
			c.setTitle(rs.getString(2));
			c.setDescription(rs.getString(3));
			c.setAttachement(new SResource());
			c.getAttachement().setId(rs.getInt(4));
		}
		
		return c;
	}

	private CWindow loadWindowAtom(int id, Connection cnx) throws Exception {
		CWindow w = new CWindow();
		w.setId(id);
		ApplicationLoader dal = new ApplicationLoader();
		
		return dal.loadFullWindow(w);
	}
}
