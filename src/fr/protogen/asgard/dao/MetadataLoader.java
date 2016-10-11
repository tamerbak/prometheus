package fr.protogen.asgard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import fr.protogen.asgard.metamodel.BPLigneTemplate;
import fr.protogen.asgard.metamodel.BPPage;
import fr.protogen.asgard.metamodel.BPTab;
import fr.protogen.asgard.metamodel.BPVariable;
import fr.protogen.asgard.metamodel.BusinessPlanTemplate;
import fr.protogen.asgard.metamodel.OptionAvancee;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class MetadataLoader {
	
	
	public List<OptionAvancee> loadAllOptions(){
		
		List<OptionAvancee> results  = new ArrayList<OptionAvancee>();
		
		try{
		    Connection cnx=((ProtogenConnection)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("PROTOGEN_CONNECTION")).getConnection();
		    String sql = "select id, label from bp_optionsavancees";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next()){
		    	OptionAvancee oa = new OptionAvancee();
		    	oa.setId(rs.getInt(1));
		    	oa.setLabel(rs.getString(2));
		    	
		    	results.add(oa);
		    }
		}catch(Exception e){
			e.printStackTrace();
		}    
		return results;
	}

	public BusinessPlanTemplate getBusinessPlanById(int id) {
		BusinessPlanTemplate bp = new BusinessPlanTemplate();
		try{
		    Connection cnx=((ProtogenConnection)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("PROTOGEN_CONNECTION")).getConnection();
		    String sql = "select start_year_query from bp_template where id=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, id);
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	bp.setCurrentYearQuery(rs.getString(1));
		    
		    rs.close();
		    ps.close();
		    
		    sql = "select id,title from bp_page where template=?";
		    ps = cnx.prepareStatement(sql);
		    ps.setInt(1, id);
		    rs = ps.executeQuery();
		    bp.setPages(new ArrayList<BPPage>());
		    int index=1;
		    while(rs.next()){
		    	BPPage tab = new BPPage(); 
		    	tab.setId(rs.getInt(1));
		    	tab.setTitle(rs.getString(2));
		    	tab.setOrderIndex(index);
		    	index++;
		    	bp.getPages().add(tab);
		    }
		    rs.close();
		    ps.close();
		    
		    
		    for(BPPage p : bp.getPages()){
		    	sql = "select id,title from bp_tab where page=?";
			    ps = cnx.prepareStatement(sql);
			    ps.setInt(1, p.getId());
			    rs = ps.executeQuery();
			    p.setTabs(new ArrayList<BPTab>());
			    while(rs.next()){
			    	BPTab tab = new BPTab();
			    	tab.setId(rs.getInt(1));
			    	tab.setTitle(rs.getString(2));
			    	p.getTabs().add(tab);
			    }
			    rs.close();
			    ps.close();
			    
			    
			    for(BPTab t : p.getTabs()){
			    	sql = "select id, compte,libelle,parametered_query, final_level, id_parent from bp_line where tab=?";
			    	ps = cnx.prepareStatement(sql);
			    	ps.setInt(1, t.getId());
			    	rs = ps.executeQuery();
			    	t.setLignes(new ArrayList<BPLigneTemplate>());
			    	while(rs.next()){
			    		BPLigneTemplate l = new BPLigneTemplate();
			    		l.setId(rs.getInt(1));
			    		l.setCompte(rs.getString(2));
			    		l.setLabel(rs.getString(3));
			    		l.setParameteredQuery(rs.getString(4));
			    		l.setFinalLevel(rs.getString(5).equals(("Y")));
			    		if(l.isFinalLevel()){
			    			BPLigneTemplate parent = new BPLigneTemplate();
			    			parent.setId(rs.getInt(6));
			    			l.setParent(parent);
			    		}
			    		t.getLignes().add(l);
			    	}
			    	
			    	rs.close();
			    	ps.close();
			    	for(BPLigneTemplate l : t.getLignes()){
			    		if(l.isFinalLevel())
			    			continue;
			    		
			    		l.setChildren(new ArrayList<BPLigneTemplate>());
			    		for(BPLigneTemplate c : t.getLignes()){
			    			if(c.isFinalLevel() && c.getParent().getId() == l.getId())
			    				l.getChildren().add(c);
			    		}
			    	}
			    }
			    
			    sql = "select id, formula, title from bp_page_variable where page=?";
			    ps = cnx.prepareStatement(sql);
			    ps.setInt(1, p.getId());
			    
			    rs = ps.executeQuery();
			    p.setVariables(new ArrayList<BPVariable>());
			    while(rs.next()){
			    	BPVariable v = new BPVariable();
			    	v.setId(rs.getInt(1));
			    	v.setFormula(rs.getString(2));
			    	v.setTitle(rs.getString(3));
			    	p.getVariables().add(v);
			    }
		    }
		   
		    
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		} 
		return bp;
	}
	
}
