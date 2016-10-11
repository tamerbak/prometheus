package fr.protogen.connector.model;

import java.util.List;

public class SmartProcessModel {
	private int pid;
	private List<String> initVars;
	private String outvar;
	private AmanToken token;
	
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public List<String> getInitVars() {
		return initVars;
	}
	public void setInitVars(List<String> initVars) {
		this.initVars = initVars;
	}
	public String getOutvar() {
		return outvar;
	}
	public void setOutvar(String outvar) {
		this.outvar = outvar;
	}
	public AmanToken getToken() {
		return token;
	}
	public void setToken(AmanToken token) {
		this.token = token;
	}
}
