package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class ScreenDataHistory implements Serializable {
	private String windowTitle = "";
	private List<PairKVElement> titles;
	private List<ListKV> data;
	private int idIndex;
	private boolean subVisibility=false;
	
	public String getWindowTitle() {
		return windowTitle;
	}
	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}
	public List<PairKVElement> getTitles() {
		return titles;
	}
	public void setTitles(List<PairKVElement> titles) {
		this.titles = titles;
	}
	public List<ListKV> getData() {
		return data;
	}
	public void setData(List<ListKV> data) {
		this.data = data;
	}
	public int getIdIndex() {
		return idIndex;
	}
	public void setIdIndex(int idIndex) {
		this.idIndex = idIndex;
	}
	public boolean isSubVisibility() {
		
		if(titles == null)
			return false;
		
		
		for(PairKVElement e : titles){
			if(!e.isVisible())
				return true;
		}
		subVisibility = false;
		return subVisibility;
	}
	public void setSubVisibility(boolean subVisibility) {
		this.subVisibility = subVisibility;
	}
	
	
}
