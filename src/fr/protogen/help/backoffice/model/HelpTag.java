package fr.protogen.help.backoffice.model;

import java.io.Serializable;

public class HelpTag implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5558557161426691640L;
	private long tagId;
	private String label;
	//number of search queries this tag is used in
	private long queries;
	
	
	public HelpTag() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public HelpTag(long tagId, String label) {
		this();
		this.tagId = tagId;
		this.label = label;
	}


	public long getTagId() {
		return tagId;
	}
	public void setTagId(long tagId) {
		this.tagId = tagId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}


	public static String[] splitTags(String tags) {
		return tags.replaceAll("\\p{Punct}", " ").toLowerCase().split("\\s+");
	}
	
	public static String formatTags(String tags) {
		return tags.replaceAll("\\p{Punct}", " ").toLowerCase();
	}

	public long getQueries() {
		return queries;
	}


	public void setQueries(long queries) {
		this.queries = queries;
	}
	
	
	
}
