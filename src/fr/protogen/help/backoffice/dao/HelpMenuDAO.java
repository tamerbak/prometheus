package fr.protogen.help.backoffice.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.help.backoffice.model.HelpMenu;
import fr.protogen.masterdata.dbutils.ProtogenConnection;

public class HelpMenuDAO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4497882585147168538L;

	public static final long NO_PARENT=-1;
	
	private HelpArticleDAO articleDAO;
	
	
	
	public HelpMenuDAO() {
		super();
		articleDAO=new HelpArticleDAO();
	}

	public boolean save(HelpMenu menu){
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "insert into help_menu " +
					"(title,leaf,parent_id,article_id)" +
					" values " +
					"(?,?,?,?) RETURNING menu_id";
			
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1,menu.getTitle());
			ps.setBoolean(2,menu.isLeaf());
			ps.setLong(3,(menu.getParent()==null)?NO_PARENT:menu.getParent().getMenuId());
			ps.setLong(4,menu.getArticle().getArticleId());
			
			ResultSet rs=ps.executeQuery();
			if(rs.next()){
				menu.setMenuId(rs.getLong("menu_id"));
			}
			ps.close();
			
			//make parent a node 
			if(menu.getParent()!=null){
					sql="update help_menu set leaf=? where menu_id=?";
					PreparedStatement ps1 = cnx.prepareStatement(sql);
					
					ps1.setBoolean(1, false);
					ps1.setLong(2, menu.getParent().getMenuId());
					
					ps1.execute();
					ps1.close();
			}
		
			
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}
	
	public List<HelpMenu> getAllMenus() {
		// TODO Auto-generated method stub
		List<HelpMenu> menus=new ArrayList<HelpMenu>();
		HelpMenu menu,parent;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select * from help_menu where menu_id not in ("+NO_PARENT+") order by menu_id ASC");
			while(rs.next()){
				menu=new HelpMenu();
				menu.setMenuId(rs.getLong("menu_id"));
				menu.setLeaf(rs.getBoolean("leaf"));
				menu.setTitle(rs.getString("title"));
				if(rs.getLong("parent_id")==NO_PARENT){
					menu.setParent(null);
				}else {
					parent=new HelpMenu();
					parent.setMenuId(rs.getLong("parent_id"));
					parent.setTitle(getMenuTitleByID(parent.getMenuId()));
					menu.setParent(parent);
				}
				menu.setChilds(getChildMenus(menu.getMenuId()));
				menu.setArticle(articleDAO.getArticleByID(rs.getLong("article_id")));
				
				menus.add(menu);
			}
			
			rs.close();
			st.close();
			
			return menus;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return menus;
	}
	
	public List<HelpMenu> getParentMenus() {
		// TODO Auto-generated method stub
		List<HelpMenu> menus=new ArrayList<HelpMenu>();
		HelpMenu menu;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select * from help_menu where menu_id not in ("+NO_PARENT+") and parent_id="+NO_PARENT+" order by menu_id ASC");
			while(rs.next()){
				menu=new HelpMenu();
				menu.setMenuId(rs.getLong("menu_id"));
				menu.setLeaf(rs.getBoolean("leaf"));
				menu.setTitle(rs.getString("title"));
				menu.setArticle(articleDAO.getArticleByID(rs.getLong("article_id")));
				menus.add(menu);
			}
			
			rs.close();
			st.close();
			
			return menus;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return menus;
	}
	
	public String getMenuTitleByID(long menuID) {
		// TODO Auto-generated method stub
		String menuTitle="";
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select title from help_menu hm where hm.menu_id="+menuID+"");
			if(rs.next()){
				menuTitle=rs.getString("title");
			}
			
			rs.close();
			st.close();
			
			return menuTitle;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return menuTitle;
	}

	public List<HelpMenu> getMenuTree() {
		List<HelpMenu> parents=getParentMenus();
		
		if(parents!=null && parents.size()>0){
			for(HelpMenu parent:parents){
				if(!parent.isLeaf()){
					parent.setChilds(getChildMenus(parent.getMenuId()));
				}
				
			}
			
		}
		return parents;
	}

	private List<HelpMenu> getChildMenus(long parentId) {
		List<HelpMenu> childs=new ArrayList<HelpMenu>();
		HelpMenu menu;
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery("select * from help_menu where parent_id="+parentId+" order by menu_id ASC");
			while(rs.next()){
				menu=new HelpMenu();
				menu.setMenuId(rs.getLong("menu_id"));
				menu.setLeaf(rs.getBoolean("leaf"));
				menu.setTitle(rs.getString("title"));
				menu.setArticle(articleDAO.getArticleByID(rs.getLong("article_id")));
				childs.add(menu);
			}
			
			rs.close();
			st.close();
			
			return childs;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return childs;
	}

	public boolean deleteMenu(HelpMenu menuToDelete) {
		try {
			
			
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			
			long articleId=menuToDelete.getArticle().getArticleId();
			long menuId=menuToDelete.getMenuId();
			
			st.execute("delete from help_menu where menu_id="+menuId+";"
					+"update help_menu set parent_id=-1 where parent_id="+menuId+";"
					+"delete from help_article_tag where article_id="+articleId+";"
					+"delete from help_article_question where article_id="+articleId+";"
					+"delete from help_article_link where src_article_id="+articleId+" or dest_article_id="+articleId+";"
					+"delete from help_article where article_id="+articleId+";"
					+"delete from help_tag where tag_id not in (select tag_id from help_article_tag);"
					+"delete from help_question where question_id not in (select question_id from help_article_question)");
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	public void modify(HelpMenu menu) throws Exception {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql = "update help_menu " +
					"set title=?,leaf=?,parent_id=?,article_id=?" +
					" where menu_id=?";
			
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1,menu.getTitle());
			ps.setBoolean(2,menu.isLeaf());
			ps.setLong(3,(menu.getParent()==null)?NO_PARENT:menu.getParent().getMenuId());
			ps.setLong(4,menu.getArticle().getArticleId());
			ps.setLong(5,menu.getMenuId());
			
			ps.execute();
			ps.close();
			
			//updating parent menu tree structure 
			sql="update help_menu set leaf=? where menu_id not in (select parent_id from help_menu)";
			if(menu.getParent()!=null){
				sql+=";update help_menu set leaf=? where menu_id=?";
			}

			PreparedStatement ps1 = cnx.prepareStatement(sql);
					
			ps1.setBoolean(1, true);
			
			if(menu.getParent()!=null){
				ps1.setBoolean(2, false);
				ps1.setLong(3, menu.getParent().getMenuId());

			}

					
			ps1.execute();
			ps1.close();
			
			//getting the new parent
			if(menu.getParent()!=null){
				menu.getParent().setTitle(getMenuTitleByID(menu.getParent().getMenuId()));
			}

			return ;
		}catch(Exception e){
			throw e;
		}
		
	}



	
	

	
	

}
