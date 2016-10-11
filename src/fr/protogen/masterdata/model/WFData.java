package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WFData implements Serializable {
	private List<WFAnswerData> answers = new ArrayList<WFAnswerData>();
	private List<WFDecisionData> decisions = new ArrayList<WFDecisionData>();
	private int subjectId;
	private String entity;
	private CWindow window;
	
	public List<WFAnswerData> getAnswers() {
		return answers;
	}
	public void setAnswers(List<WFAnswerData> answers) {
		this.answers = answers;
	}
	public List<WFDecisionData> getDecisions() {
		return decisions;
	}
	public void setDecisions(List<WFDecisionData> decisions) {
		this.decisions = decisions;
	}
	public int getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
}
