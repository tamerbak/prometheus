package fr.protogen.help.backoffice.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.protogen.help.backoffice.model.HelpTag;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class HelpTagDAO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2151827797136256019L;
	public boolean save(HelpTag tag){
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "insert into help_tag " +
					"(tag_label)" +
					" values " +
					"(?)";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setString(1, tag.getLabel());
			
			
			ps.execute();
			
			ps.close();
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}

	public HelpTag getTagById(long tagId){
		HelpTag tag=null;
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select tag_label from help_tag where tag_id="+tagId);
			
			
			if(rs.next()){
				tag=new HelpTag();
				tag.setTagId(tagId);
				tag.setLabel(rs.getString("tag_label"));
			}
			
			rs.close();
			st.close();
			
			return tag;
		}catch(Exception e){
			e.printStackTrace();
			return tag;
		}
	}
	public HelpTag getTagByLabel(String tagLabel){
		HelpTag tag=null;
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select tag_id from help_tag where tag_label='"+tagLabel+"'");
			
			
			if(rs.next()){
				tag=new HelpTag();
				tag.setTagId(rs.getLong("tag_id"));
				tag.setLabel(tagLabel);
				return tag;
			}
			
			rs.close();
			st.close();
			
			return tag;
		}catch(Exception e){
			e.printStackTrace();
			return tag;
		}
	}
	
	public List<HelpTag> getAllTagsByArticleID(long articleId) {
		List<HelpTag> tags=new ArrayList<HelpTag>();
		HelpTag tag;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select tag_id from help_article_tag where article_id="+articleId);
			while(rs.next()){
				tag=getTagById(rs.getLong("tag_id"));
				tags.add(tag);
			}
			
			rs.close();
			st.close();
			
			return tags;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tags;
	}

	public List<HelpTag> saveArticleTags(String[] tagsLabel) {
		List<HelpTag> tags=new ArrayList<HelpTag>();
		List<String> attachedTags=new ArrayList<String>();
		HelpTag tag=null;
		for (String label : tagsLabel) {
			if(attachedTags.contains(label)){
				continue;
			}
			attachedTags.add(label);
			tag=getTagByLabel(label);
			//tag doesn't exist
			if(tag==null){
				tag=new HelpTag();
				tag.setLabel(label);
				save(tag);
				//getting full tag object (with inserted id)
				tag=getTagByLabel(label);
			}
			
			tags.add(tag);
		}
		return tags;
		
	}

	public List<HelpTag> getTagsForCloud() {
		List<HelpTag> tags=new ArrayList<HelpTag>();
		HelpTag tag;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select * from help_tag");
			while(rs.next()){
				tag=new HelpTag();
				tag.setTagId(rs.getLong("tag_id"));
				tag.setQueries(rs.getLong("tag_queries"));
				tag.setLabel(rs.getString("tag_label"));
				
				tags.add(tag);
			}
			
			rs.close();
			st.close();
			
			return tags;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tags;
	}

	public void updateTagsQueries(List<String> queriedTags) {
		String formattedTags="";
		for(String tag:queriedTags){
			formattedTags+=(formattedTags.isEmpty()?"":",")+"'"+tag+"'";
		}
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			st.execute("update help_tag set tag_queries=tag_queries + 1 where tag_label in ("+formattedTags+")");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeUnlinked() {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			//delete all links associated with article
			String sql = "delete from help_tag " +
					"where tag_id not in (select tag_id from help_article_tag)";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			
			ps.execute();
			
			ps.close();
			
		
			return ;
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
