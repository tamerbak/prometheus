package fr.protogen.help.backoffice.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.help.backoffice.model.HelpQuestion;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class HelpQuestionDAO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2151827797136256019L;
	public boolean save(HelpQuestion question){
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "insert into help_question " +
					"(question_label)" +
					" values " +
					"(?)";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setString(1, question.getLabel());
			
			
			ps.execute();
			
			ps.close();
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}

	public HelpQuestion getQuestionById(long questionId){
		HelpQuestion question=null;
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select question_label from help_question where question_id="+questionId);
			
			
			if(rs.next()){
				question=new HelpQuestion();
				question.setQuestionId(questionId);
				question.setLabel(rs.getString("question_label"));
			}
			
			rs.close();
			st.close();
			
			return question;
		}catch(Exception e){
			e.printStackTrace();
			return question;
		}
	}
	public HelpQuestion getQuestionByLabel(String questionLabel){
		HelpQuestion question=null;
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String query="select question_id from help_question where question_label=?";
			PreparedStatement st = cnx.prepareStatement(query);
			st.setString(1, questionLabel);
			ResultSet rs = st.executeQuery();
			
			
			if(rs.next()){
				question=new HelpQuestion();
				question.setQuestionId(rs.getLong("question_id"));
				question.setLabel(questionLabel);
				return question;
			}
			
			rs.close();
			st.close();
			
			return question;
		}catch(Exception e){
			e.printStackTrace();
			return question;
		}
	}
	
	public List<HelpQuestion> getAllQuestionsByArticleID(long articleId) {
		List<HelpQuestion> questions=new ArrayList<HelpQuestion>();
		HelpQuestion question;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select question_id from help_article_question where article_id="+articleId);
			while(rs.next()){
				question=getQuestionById(rs.getLong("question_id"));
				questions.add(question);
			}
			
			rs.close();
			st.close();
			
			return questions;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return questions;
	}

	public List<HelpQuestion> saveArticleQuestions(String[] questionsLabel) {
		List<HelpQuestion> questions=new ArrayList<HelpQuestion>();
		HelpQuestion question=null;
		for (String label : questionsLabel) {
			if(label.length()>0){
				question=getQuestionByLabel(label.trim()+"?");
				//question doesn't exist
				if(question==null){
					question=new HelpQuestion();
					question.setLabel(label.trim()+"?");
					save(question);
					//getting full question object (with inserted id)
					question=getQuestionByLabel(label.trim()+"?");
				}
				
				questions.add(question);
			}
			
		}
		return questions;
		
	}

	
	public void updateQuestionsQueries(List<String> queriedQuestions) {
		String question=queriedQuestions.get(0);
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String query="update help_question set question_queries=question_queries + 1 where question_label in (?)";
			PreparedStatement st = cnx.prepareStatement(query);
			st.setString(1, question);
			st.execute();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<String> getSuggestions(String questionPart) {
		List<String> suggestions=new ArrayList<String>();
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String query="select question_label from help_question where LOWER(question_label) like ?";
			PreparedStatement st = cnx.prepareStatement(query);
			
			st.setString(1, "%"+questionPart.toLowerCase()+"%");
			ResultSet rs = st.executeQuery();
			
			
			while(rs.next()){
				
				suggestions.add(rs.getString("question_label"));
				
			}
			
			rs.close();
			st.close();
			
			return suggestions;
		}catch(Exception e){
			e.printStackTrace();
			return suggestions;
		}
	}

	public void removeUnlinked() {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			//delete all links associated with article
			String sql = "delete from help_question " +
					"where question_id not in (select question_id from help_article_question)";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			
			ps.execute();
			
			ps.close();
			
		
			return ;
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}


}
