package fr.protogen.asgard.metamodel;

import java.io.Serializable;
import java.util.List;

public class BPPage implements Serializable {
	
	private int id;
	private int orderIndex;
	private String title;
	private List<BPTab> tabs;
	private String footer;
	private List<String> titles;
	private List<BPVariable> variables;
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<BPTab> getTabs() {
		return tabs;
	}
	public void setTabs(List<BPTab> tabs) {
		this.tabs = tabs;
	}
	public String getFooter() {
		return footer;
	}
	public void setFooter(String footer) {
		this.footer = footer;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<String> getTitles() {
		return titles;
	}
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	public List<BPVariable> getVariables() {
		return variables;
	}
	public void setVariables(List<BPVariable> variables) {
		this.variables = variables;
	}
	public int getOrderIndex() {
		return orderIndex;
	}
	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}

}
