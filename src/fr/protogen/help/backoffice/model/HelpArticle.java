package fr.protogen.help.backoffice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HelpArticle implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -207101018691612552L;
	private long articleId;
	private String title,content,video;

	private List<HelpArticle> links;
	private List<HelpTag> tags;
	private List<HelpQuestion> questions;
	private String formattedTags;
	

	public HelpArticle() {
		super();
		links=new ArrayList<HelpArticle>();
		tags=new ArrayList<HelpTag>();
		questions=new ArrayList<HelpQuestion>();
	}

	public HelpArticle(int articleId, String title, String content,
			String video) {
		this();
		this.articleId = articleId;
		this.title = title;
		this.content = content;
		this.video = video;
	}
	
	//Handling Lists
	//links
	public void addLink(HelpArticle article){
		links.add(article);
	}
	public void removeLink(HelpArticle article){
		links.remove(article);
	}
	public void clearLinks(){
		links.clear();
	}
	
	//tags
	public void addTag(HelpTag tag){
		tags.add(tag);
	}
	public void removeTag(HelpTag tag){
		tags.remove(tag);
	}
	public void clearTags(){
		tags.clear();
	}
	
	//question
	public void addQuestion(HelpQuestion question){
		questions.add(question);
	}
	public void removeQuestion(HelpQuestion question){
		questions.remove(question);
	}
	public void clearQuestions(){
		questions.clear();
	}
	
	public long getArticleId() {
		return articleId;
	}
	public void setArticleId(long articleId) {
		this.articleId = articleId;
	}
	public String getTitle() {
		if(title!=null)
			return title.substring(0,1).toUpperCase()+title.substring(1);
		
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getVideo() {
		return video;
	}
	public void setVideo(String video) {
		this.video = video;
	}

	public List<HelpArticle> getLinks() {
		return links;
	}

	public void setLinks(List<HelpArticle> links) {
		this.links = links;
	}

	public List<HelpTag> getTags() {
		return tags;
	}

	public void setTags(List<HelpTag> tags) {
		this.tags = tags;
		formattedTags=formateTags();
	}
	
	
	public List<HelpQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<HelpQuestion> questions) {
		this.questions = questions;
	}

	public String getFormattedTags() {
		return formattedTags;
	}

	public void setFormattedTags(String formattedTags) {
		this.formattedTags = formattedTags;
	}

	public String formateTags(){
		String allTags="";
		for (HelpTag tag:tags) {
			allTags+=(allTags.isEmpty())?tag.getLabel():", "+tag.getLabel();
		}
		return allTags;
	}
	
	public String getShortContent(){
		if(content !=null)
			return content.substring(0,(content.length()>100?100:content.length()));
		return content;
	}

	public String formatQuestions() {
		String formattedQuestions="";
		if(questions!=null){
			for(HelpQuestion question:questions){
				formattedQuestions+=(formattedQuestions.isEmpty()?"":", ")+question.getLabel();
			}
		}
		return formattedQuestions;
	}
	
	
	
	
}
