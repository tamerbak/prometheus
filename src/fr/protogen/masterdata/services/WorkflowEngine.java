package fr.protogen.masterdata.services;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import fr.protogen.masterdata.DAO.WorkflowDAO;
import fr.protogen.masterdata.model.*;

public class WorkflowEngine {
	
	
	public WorkflowNode launchWorkflow(WorkflowDefinition definition, CoreUser user, WFData datum){
		WorkflowNode node= new WorkflowNode();
		
		//	Get the first node
		WorkflowDAO dao = new WorkflowDAO();
		node = dao.loadFirstNode(definition);
		
		definition.setNodes(new ArrayList<WorkflowNode>());
		definition.getNodes().add(node);
		
		String xml="";
		XStream encoder = new XStream();
		xml=encoder.toXML(datum);
		
		
		//	Create Workflow Execution
		dao.createWFExecution(definition, user,xml);
		return node;
	}

	public List<WorkflowExecution> getPendingInstances(CoreRole role) {
		List<WorkflowExecution> wfs = new ArrayList<WorkflowExecution>();
		
		WorkflowDAO dao = new WorkflowDAO();
		wfs = dao.loadPendingWorkflows(role);
		
		XStream encoder = new XStream();
		
		for(WorkflowExecution wfe : wfs){
			if(wfe.getParameters()!=null)
			wfe.setDataParameters((WFData)encoder.fromXML(wfe.getParameters()));
		}
		
		return wfs;
	}

	public void updateWorkflowExecution(WorkflowExecution exec) {
		WorkflowNode n = new WorkflowNode(); 
				
		WorkflowDAO dao = new WorkflowDAO();
		n = dao.nextNode(exec.getCurrentNode());
		exec.setCurrentNode(n);
		String xml="";
		XStream encoder = new XStream();
		xml=encoder.toXML(exec.getDataParameters());
		exec.setParameters(xml);
		dao.updateWorkflowExecution(exec);
		
	}

	public void updateWorkflowExecution(WorkflowExecution exec,
			WorkflowNode n) {
		WorkflowDAO dao = new WorkflowDAO();
		exec.setCurrentNode(n);
		String xml="";
		XStream encoder = new XStream();
		xml=encoder.toXML(exec.getDataParameters());
		exec.setParameters(xml);
		dao.updateWorkflowExecution(exec);
		
	}
}
