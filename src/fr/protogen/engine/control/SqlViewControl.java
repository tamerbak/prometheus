package fr.protogen.engine.control;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.masterdata.model.CView;
import fr.protogen.masterdata.model.CViewPart;
import fr.protogen.masterdata.services.ViewsService;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class SqlViewControl implements Serializable {
	private String windowTitle;
	private String styleAffichage;
	private List<List<String>> values = new ArrayList<List<String>>();
	private List<List<String>> filteredValues = new ArrayList<List<String>>();
	private List<String> titles = new ArrayList<String>();
	private String fixedCols = "3";
	private boolean showDirections = false;
	private List<String> filters = new ArrayList<String>();
	
	
	@PostConstruct
	public void energize(){
		/*
		 * Check for authentication
		 */
		
		boolean notinsession=(!FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
				|| FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")==null); 

		
		if(notinsession){
			try {
			
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		/*
		 * Get view ID and construct View
		 */
		Map<String, Object> sessionMap =  FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		int winid=2045;
		int vid=0;
		if(sessionMap.containsKey(ProtogenConstants.SELECTED_VIEW)){
			winid = Integer.parseInt(sessionMap.get(ProtogenConstants.SELECTED_VIEW).toString());
			vid = Integer.parseInt(sessionMap.get(ProtogenConstants.SELECTED_VIEW_ID).toString());
		}
		
		ViewsService service = new ViewsService();
		List<CView> views = service.loadWindowViews(winid);
		CView view = new CView();
		for(CView v : views)
			if(v.getId() == vid){
				view = v;
				break;
			}
		view = service.loadData(view);
		windowTitle = view.getTitle();
		
		/*
		 * Format data and titles
		 */
		titles = new ArrayList<String>();
		values = new ArrayList<List<String>>();
		for(CViewPart p : view.getParts()){
			for(String t : p.getTitles()){
				titles.add(t);
				filters.add("");
			}
			for(List<String> v : p.getDataRows()){
				values.add(v);
			}
		}
		filteredValues = values;
		
		/*
		 * Layout
		 */
		if(titles.size()==1){
			styleAffichage = "quarter-size, three-quarters-size";
			
		} else
			styleAffichage = titles.size()>2?"full-size, no-size":"middle-size, middle-size";
			
		if(titles.size() == 1)
			fixedCols = "1";
		else if(titles.size()<=2)
			fixedCols = "2";
		else
			fixedCols = "3";
		
		showDirections = titles.size()<3;
	}
	
	public void search(){
		filteredValues = new ArrayList<List<String>>();
		for(List<String> l : values)
			for(int i = 0; i < filters.size() ; i++){
				if(filters.get(i)!=null && filters.get(i).length()>0){
					String v = l.get(i);
					if(v.toLowerCase().contains(filters.get(i).toLowerCase())){
						filteredValues.add(l);
						break;
					}
				}
			}
		
		boolean flag = true;
		for(int i = 0; i < filters.size() ; i++){
			if(filters.get(i)!=null && filters.get(i).length()>0){
				flag = false;
				break;
			}
		}
		if(flag)
			filteredValues = values;
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public String getWindowTitle() {
		return windowTitle;
	}
	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}
	public String getStyleAffichage() {
		return styleAffichage;
	}
	public void setStyleAffichage(String styleAffichage) {
		this.styleAffichage = styleAffichage;
	}
	public List<List<String>> getValues() {
		return values;
	}
	public void setValues(List<List<String>> values) {
		this.values = values;
	}
	public List<String> getTitles() {
		return titles;
	}
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	public String getFixedCols() {
		return fixedCols;
	}
	public void setFixedCols(String fixedCols) {
		this.fixedCols = fixedCols;
	}
	public boolean isShowDirections() {
		return showDirections;
	}
	public void setShowDirections(boolean showDirections) {
		this.showDirections = showDirections;
	}

	public List<List<String>> getFilteredValues() {
		return filteredValues;
	}

	public void setFilteredValues(List<List<String>> filteredValues) {
		this.filteredValues = filteredValues;
	}

	public List<String> getFilters() {
		return filters;
	}

	public void setFilters(List<String> filters) {
		this.filters = filters;
	}
}
