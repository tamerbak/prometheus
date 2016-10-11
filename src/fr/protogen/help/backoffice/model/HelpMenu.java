package fr.protogen.help.backoffice.model;

import java.io.Serializable;
import java.util.List;

public class HelpMenu implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3391558884852900600L;
	private long menuId;
	private String title;
	private boolean leaf=true;
	
	private HelpMenu parent;
	private HelpArticle article;
	private List<HelpMenu> childs;
	
	public HelpMenu() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HelpMenu(long menuId, String title, boolean leaf, HelpMenu parent,
			HelpArticle article) {
		this();
		this.menuId = menuId;
		this.title = title;
		this.leaf = leaf;
		this.parent = parent;
		this.article = article;
	}

	
	public List<HelpMenu> getChilds() {
		return childs;
	}

	public void setChilds(List<HelpMenu> childs) {
		this.childs = childs;
	}

	public long getMenuId() {
		return menuId;
	}

	public void setMenuId(long menuId) {
		this.menuId = menuId;
	}

	public String getTitle() {
		if(title!=null)
			return title.substring(0,1).toUpperCase()+title.substring(1);
		
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public HelpMenu getParent() {
		return parent;
	}

	public void setParent(HelpMenu parent) {
		this.parent = parent;
	}

	public HelpArticle getArticle() {
		return article;
	}

	public void setArticle(HelpArticle article) {
		this.article = article;
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return title;
	}
	
}
