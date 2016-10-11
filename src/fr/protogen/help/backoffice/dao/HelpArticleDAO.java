package fr.protogen.help.backoffice.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.help.backoffice.model.HelpArticle;
import fr.protogen.help.backoffice.model.HelpTag;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class HelpArticleDAO  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5581562089707463548L;
	private HelpTagDAO tagDAO;
	String alreadyFoundArticle="";
	private HelpQuestionDAO questionDAO;
	
	public HelpArticleDAO() {
		super();
		tagDAO=new HelpTagDAO();
		questionDAO=new HelpQuestionDAO();
	}

	public HelpArticle save(HelpArticle article){
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "insert into help_article " +
					"(title,video,article_content)" +
					" values " +
					"(?,?,?) RETURNING article_id";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, article.getTitle());
			ps.setString(2,article.getVideo());
			ps.setString(3, article.getContent());
			/*
			ps.setInt(1, instance.getAlert().getId());
			ps.setString(2,instance.getMessage());
			*/
			ResultSet rs=ps.executeQuery();
			if(rs.next()){
				article.setArticleId(rs.getLong("article_id"));
			}
			ps.close();
			return article;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public HelpArticle getArticleByID(long articleID) {
		// TODO Auto-generated method stub
		HelpArticle article = null;
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select * from help_article ha where ha.article_id="+articleID+"");
			if(rs.next()){
				article=new HelpArticle();
				article.setArticleId(articleID);
				article.setTitle(rs.getString("title"));
				article.setVideo(rs.getString("video"));
				article.setContent(rs.getString("article_content"));
				article.setTags(tagDAO.getAllTagsByArticleID(article.getArticleId()));
				article.setLinks(getAllLinksByArticleID(article.getArticleId()));
				article.setQuestions(questionDAO.getAllQuestionsByArticleID(article.getArticleId()));
				
			}
			
			rs.close();
			st.close();
			
			return article;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return article;
	}
	
	public List<HelpArticle> getArticlesByTag(String tag) {
		// TODO Auto-generated method stub
		HelpArticle article = null;
		List<HelpArticle> articles=new ArrayList<HelpArticle>();
		boolean alreadyFound=false;
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery("select * from help_article ha,help_tag ht,help_article_tag hat where ha.article_id=hat.article_id and ht.tag_id=hat.tag_id and LOWER(ht.tag_label) like '%"+tag.toLowerCase()+"%'"+(alreadyFoundArticle.isEmpty()?"":" and ha.article_id not in("+alreadyFoundArticle+")"));
			while(rs.next()){
				alreadyFound=false;
				article=new HelpArticle();
				article.setArticleId(rs.getLong("article_id"));
				article.setTitle(rs.getString("title"));
				article.setVideo(rs.getString("video"));
				article.setContent(rs.getString("article_content"));
				article.setTags(tagDAO.getAllTagsByArticleID(article.getArticleId()));
				article.setLinks(getAllLinksByArticleID(article.getArticleId()));
				
				alreadyFoundArticle+=(alreadyFoundArticle.isEmpty()?article.getArticleId():","+article.getArticleId());
				for(HelpArticle a:articles){
					if(a.getArticleId()==article.getArticleId()){
						alreadyFound=true;
					}
				}
				if(!alreadyFound){
					articles.add(article);
				}
				
			}
			
			rs.close();
			st.close();
			return articles;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return articles;
	}
	
	private List<HelpArticle> getAllLinksByArticleID(long articleId) {
		// TODO Auto-generated method stub
		List<HelpArticle> links=new ArrayList<HelpArticle>();
		HelpArticle link;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select dest_article_id from help_article_link where src_article_id="+articleId+"");
			while(rs.next()){
				link=getArticleByID(rs.getLong("dest_article_id"));
				links.add(link);
			}
			
			rs.close();
			st.close();
			
			return links;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return links;
	}

	public List<HelpArticle> getArticlesByTags(String searchTags) {
		List<String> queriedTags=new ArrayList<String>();
		List<HelpArticle> articles=new ArrayList<HelpArticle>();
		List<String> lookupedTags;
		alreadyFoundArticle="";
		
		String[] tags=HelpTag.splitTags(searchTags);
		if(tags.length>0){
			lookupedTags=new ArrayList<String>();
			for(String tag:tags){
				List<HelpArticle> tagArticles;
				if(!alreadyLookedfor(lookupedTags,tag)){
					tagArticles=getArticlesByTag(tag.trim());
					
				
					articles.addAll(tagArticles);
					queriedTags.add(tag);
				
					lookupedTags.add(tag);
				}
					
			}
		if(queriedTags.size()>0){
			tagDAO.updateTagsQueries(queriedTags);
		}
		
		}
		return articles;

	}

	private boolean alreadyLookedfor(List<String> lookupedTags, String tag) {
		if(lookupedTags.size()==0){
			return false;
		}
		else{
			for(String lookedTag:lookupedTags){
				if(lookedTag.equalsIgnoreCase(tag)){
					return true;
				}
			}
		}
		return false;
		
	}

	public void modify(HelpArticle article) throws Exception {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "update help_article set title=?,video=?,article_content=? " +
					" where article_id=?";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, article.getTitle());
			ps.setString(2,article.getVideo());
			ps.setString(3, article.getContent());
			ps.setLong(4, article.getArticleId());

			ps.execute();
			ps.close();
			return ;
		}catch(Exception e){
			throw e;
		}
		
	}

	public List<HelpArticle> getArticlesByQuestions(String question) {
		List<HelpArticle> articles=null;
		
	    articles=getArticlesByQuestion(question.trim());
		
	    List<String> questions=new ArrayList<String>();
	    questions.add(question);
		questionDAO.updateQuestionsQueries(questions);
		
		return articles;

	}

	private List<HelpArticle> getArticlesByQuestion(String question) {
		HelpArticle article = null;
		List<HelpArticle> articles=new ArrayList<HelpArticle>();
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String query="select DISTINCT ha.article_id,ha.title,ha.video,ha.article_content from help_article ha,help_question hq,help_article_question haq where ha.article_id=haq.article_id and hq.question_id=haq.question_id and hq.question_label=?";
			PreparedStatement st = cnx.prepareStatement(query);
			st.setString(1,question);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				article=new HelpArticle();
				article.setArticleId(rs.getLong("article_id"));
				article.setTitle(rs.getString("title"));
				article.setVideo(rs.getString("video"));
				article.setContent(rs.getString("article_content"));
				article.setTags(tagDAO.getAllTagsByArticleID(article.getArticleId()));
				article.setLinks(getAllLinksByArticleID(article.getArticleId()));
				article.setQuestions(questionDAO.getAllQuestionsByArticleID(article.getArticleId()));
				
				articles.add(article);
			}
			
			rs.close();
			st.close();
			return articles;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return articles;
	}

	
	
}
