package fr.protogen.engine.control.process;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CUIParameter;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SProcess;

public class ProcessSession {
	private UUID processIdentifier;
	private SAtom currentWindow;
	private CoreUser currentUser;
	private CBusinessClass entity;
	private List<Map<String, String>> data;
	private Map<String, String> parameters;
	private List<CUIParameter> procedureParameters;
	private SProcess process;
	private SProcedure procedure;
	private ProcessEvoltionListener listener;
	
	public List<Map<String, String>> getData() {
		return data;
	}
	public void setData(List<Map<String, String>> data) {
		this.data = data;
	}
	
	
	public SAtom getCurrentWindow() {
		return currentWindow;
	}
	public void setCurrentWindow(SAtom currentWindow) {
		this.currentWindow = currentWindow;
	}
	public CoreUser getCurrentUser() {
		return currentUser;
	}
	public void setCurrentUser(CoreUser currentUser) {
		this.currentUser = currentUser;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	public UUID getProcessIdentifier() {
		return processIdentifier;
	}
	public void setProcessIdentifier(UUID processIdentifier) {
		this.processIdentifier = processIdentifier;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public SProcess getProcess() {
		return process;
	}
	public void setProcess(SProcess process) {
		this.process = process;
	}
	public ProcessEvoltionListener getListener() {
		return listener;
	}
	public void setListener(ProcessEvoltionListener listener) {
		this.listener = listener;
	}
	public List<CUIParameter> getProcedureParameters() {
		return procedureParameters;
	}
	public void setProcedureParameters(List<CUIParameter> procedureParameters) {
		this.procedureParameters = procedureParameters;
	}
	public SProcedure getProcedure() {
		return procedure;
	}
	public void setProcedure(SProcedure procedure) {
		this.procedure = procedure;
	}
	
}
