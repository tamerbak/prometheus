package fr.protogen.help.backoffice.model;

import java.io.Serializable;

public class HelpQuestion implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5558557161426691640L;
	private long questionId;
	private String label;
	//number of search queries this question is used in
	private long queries;
	
	
	public HelpQuestion() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public HelpQuestion(long questionId, String label) {
		this();
		this.questionId = questionId;
		this.label = label;
	}


	public long getQuestionId() {
		return questionId;
	}
	public void setQuestionId(long questionId) {
		this.questionId = questionId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public long getQueries() {
		return queries;
	}


	public void setQueries(long queries) {
		this.queries = queries;
	}


	public static String[] splitQuestions(String questions) {
		return questions.replaceAll("[!\"#\\$%&()*+,-./:;<=>@\\[\\]\\^_`|~\\{\\}]", "").toLowerCase().split("\\?");
	}
	
	
	
}
