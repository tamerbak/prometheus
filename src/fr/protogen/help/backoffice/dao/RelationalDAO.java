package fr.protogen.help.backoffice.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;

import fr.protogen.help.backoffice.model.HelpArticle;
import fr.protogen.help.backoffice.model.HelpQuestion;
import fr.protogen.help.backoffice.model.HelpTag;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class RelationalDAO implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8179123735603216870L;

	public RelationalDAO() {
		super();
	}

	public boolean saveArticleLink(long srcArticleId,long destArticleId ){
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "insert into help_article_link " +
					"(src_article_id,dest_article_id)" +
					" values " +
					"(?,?)";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setLong(1,srcArticleId);
			ps.setLong(2,destArticleId);
			
			ps.execute();
			
			ps.close();
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean saveArticleTag(long articleId,long tagId){
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "insert into help_article_tag " +
					"(article_id,tag_id)" +
					" values " +
					"(?,?)";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setLong(1,articleId);
			ps.setLong(2,tagId);
			
			ps.execute();
			
			ps.close();
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}

	public void saveAllArticleTag(HelpArticle article) {
		for(HelpTag tag:article.getTags()){
			saveArticleTag(article.getArticleId(), tag.getTagId());
		}
		
	}

	public void saveAllArticleLink(HelpArticle article) {
		for(HelpArticle link:article.getLinks()){
			saveArticleLink(article.getArticleId(), link.getArticleId());
		}
		
	}

	public void modifyAllArticleTag(HelpArticle article) throws Exception {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			//delete all tags associated with article
			String sql = "delete from help_article_tag " +
					"where article_id=?" ;
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setLong(1,article.getArticleId());
			
			ps.execute();
			
			ps.close();
			
			//register article's new tags
			saveAllArticleTag(article);
			return ;
		}catch(Exception e){
			throw e;
		}
		
	}

	public void modifyAllArticleLink(HelpArticle article) throws Exception {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			//delete all links associated with article
			String sql = "delete from help_article_link " +
					"where src_article_id=?";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setLong(1,article.getArticleId());
			
			ps.execute();
			
			ps.close();
			
			//register article's new links
			saveAllArticleLink(article);
			return ;
		}catch(Exception e){
			throw e;
		}
		
		
	}

	public void saveAllArticleQuestion(HelpArticle article) {
		for(HelpQuestion question:article.getQuestions()){
			saveArticleQuestion(article.getArticleId(), question.getQuestionId());
		}
		
	}

	private boolean saveArticleQuestion(long articleId, long questionId) {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "insert into help_article_question " +
					"(article_id,question_id)" +
					" values " +
					"(?,?)";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setLong(1,articleId);
			ps.setLong(2,questionId);
			
			ps.execute();
			
			ps.close();
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}

	public void modifyAllArticleQuestion(HelpArticle article) throws Exception {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			//delete all tags associated with article
			String sql = "delete from help_article_question " +
					"where article_id=?;" ;
			
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setLong(1,article.getArticleId());
			
			ps.execute();
			
			ps.close();
			
			//register article's new tags
			saveAllArticleQuestion(article);
			return ;
		}catch(Exception e){
			throw e;
		}
	}
	

	
	

}
