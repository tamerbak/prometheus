package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.*;

public class WorkflowDAO {

	public WorkflowNode loadFirstNode(WorkflowDefinition definition){
		WorkflowNode node = new WorkflowNode();
		
		try{
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		
		    String sql="SELECT s_wf_node.id, s_wf_node.label, s_wf_node.description, s_wf_node.type, s_wf_node.responsible " +
		    			"FROM public.s_wf_definition, public.s_wf_node " +
		    			"WHERE s_wf_node.definition = s_wf_definition.id AND s_wf_definition.id=? order by s_wf_node.id asc limit 1";
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, definition.getId());
		    
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	node.setId(rs.getInt(1));
		    	node.setLabel(rs.getString(2));
		    	node.setDescription(rs.getString(3));
		    	NodeType t = new NodeType();
		    	t.setId(rs.getInt(4));
		    	node.setType(t);
		    	CoreRole r = new CoreRole();
		    	r.setId(rs.getInt(5));
		    	node.setResponsible(r);
		    }
		    
		    rs.close();
		    ps.close();
		    
		    if(node.getType().getId()==20)
		    	return loadDecisionNode(node);
		    if(node.getType().getId()==21)
		    	return loadAnswerNode(node);
		    if(node.getType().getId()==22)
		    	return loadScreenNode(node);
		    return node;
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		  
		
		return node;
	}

	private WorkflowNode loadScreenNode(WorkflowNode node) {
		WorkflowScreenNode n = new WorkflowScreenNode();
		n.setDescription(node.getDescription());
		n.setId(node.getId());
		n.setLabel(node.getLabel());
		n.setResponsible(node.getResponsible());
		n.setType(node.getType());
		try{
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select \"window\" from s_wf_screen where node=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, n.getId());
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	CWindow w = new CWindow();
		    	w.setId(rs.getInt(1));
		    	n.setWindow(w);
		    }
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return n;
	}

	private WorkflowNode loadAnswerNode(WorkflowNode node) {
		WorkflowAnswer n = new WorkflowAnswer();
		n.setDescription(node.getDescription());
		n.setId(node.getId());
		n.setLabel(node.getLabel());
		n.setResponsible(node.getResponsible());
		n.setType(node.getType());
		try{
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select request from s_wf_answer where node=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, n.getId());
		    
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	WorkflowNode nd = new WorkflowNode();
		    	nd.setId(rs.getInt(1));
		    	n.setDecisionNode((WorkflowDecision) loadDecisionNode(nd));
		    }
		    
		    rs.close();
		    ps.close();
		    	
		}catch(Exception e){
			e.printStackTrace();
		}
		return n;
	}

	private WorkflowNode loadDecisionNode(WorkflowNode node) {
		WorkflowDecision n = new WorkflowDecision();
		n.setDescription(node.getDescription());
		n.setId(node.getId());
		n.setLabel(node.getLabel());
		n.setResponsible(node.getResponsible());
		n.setType(node.getType());
		
		try{
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select yes_label, no_label, yes_node, no_node from s_wf_request where node=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, n.getId());
		    
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	n.setYesLabel(rs.getString(1));
		    	n.setNoLabel(rs.getString(2));
		    	n.setYesNode(new WorkflowNode());n.getYesNode().setId(rs.getInt(3));
		    	n.setNoNode(new WorkflowNode());n.getNoNode().setId(rs.getInt(4));
		    }
		    
		    rs.close();
		    ps.close();
		    	
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return n;
	}

	public void createWFExecution(WorkflowDefinition definition,CoreUser user, String xml){
		
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "insert into s_wf_execution (definition, \"user\", current_node, persisted_parameters) values " +
						"(?,?,?,?)";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, definition.getId());
			ps.setInt(2, user.getId());
			ps.setInt(3, definition.getNodes().get(0).getId());
			ps.setString(4, xml);
			
			ps.execute();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public List<WorkflowExecution> loadPendingWorkflows(CoreRole role) {
		List<WorkflowExecution> wfes = new ArrayList<WorkflowExecution>();
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "SELECT   s_wf_execution.id AS id_execution,   s_wf_execution.definition,   s_wf_execution.\"user\",   " +
									"s_wf_execution.current_node,   s_wf_execution.persisted_parameters,   s_wf_node.responsible,   " +
									"s_wf_node.id,   s_wf_node.label,   s_wf_node.description,   s_wf_node.type,   s_wf_node.definition " +
						 " FROM   public.s_wf_execution,   public.s_wf_node " +
						 " WHERE   s_wf_execution.current_node = s_wf_node.id and responsible=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, role.getId());
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				WorkflowExecution wfe = new WorkflowExecution();
				WorkflowDefinition wfd = new WorkflowDefinition();
				WorkflowNode wfn = new WorkflowNode();
				CoreUser u = new CoreUser();
				NodeType nt = new NodeType();
				
				wfe.setId(rs.getInt(1));
				wfd.setId(rs.getInt(2));
				wfd = loadWorkflowbyId(wfd.getId());
				wfe.setDefinition(wfd);
				u.setId(rs.getInt(3));wfe.setUser(u);
				wfn.setId(rs.getInt(4));wfe.setCurrentNode(wfn);
				wfe.setParameters(rs.getString(5));
				wfn.setResponsible(role);
				wfn.setLabel(rs.getString(8));
				wfn.setDescription(rs.getString(9));
				nt.setId(rs.getInt(10));wfn.setType(nt);
				if(wfn.getType().getId()==20)
			    	wfn= loadDecisionNode(wfn);
			    if(wfn.getType().getId()==21)
			    	wfn=loadAnswerNode(wfn);
			    if(wfn.getType().getId()==22)
			    	wfn=loadScreenNode(wfn);
			    wfn.setDefinition(wfd);
			    wfe.setCurrentNode(wfn);
			    wfes.add(wfe);
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return wfes;
	}

	private WorkflowDefinition loadWorkflowbyId(int id) {
		WorkflowDefinition wfd = new WorkflowDefinition();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "select title, description from s_wf_definition where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				wfd.setTitle(rs.getString(1));
				wfd.setDescription(rs.getString(2));
			}
			
			rs.close();
			ps.close();
			
		} catch(Exception e){
			e.printStackTrace();
		}
		return wfd;
	}

	public WorkflowNode nextNode(WorkflowNode currentNode) {

		WorkflowNode current = new WorkflowNode();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "SELECT s_wf_transition.\"to\" FROM  public.s_wf_transition WHERE  s_wf_transition.\"from\" = ?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, currentNode.getId());
			
			
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				current.setId(rs.getInt(1));
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return current;
	}

	public void updateWorkflowExecution(WorkflowExecution exec) {
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			if(exec.getCurrentNode().getId()>0){
				String sql ="update s_wf_execution set current_node=?, persisted_parameters=? where id=?";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, exec.getCurrentNode().getId());
				ps.setString(2, exec.getParameters());
				ps.setInt(3, exec.getId());
				
				ps.execute();
				
				ps.close();
			} else {
				String sql ="delete from s_wf_execution where id=?";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, exec.getId());
				
				ps.execute();
				
				ps.close();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
