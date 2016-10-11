package fr.protogen.engine.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

import org.apache.el.ExpressionFactoryImpl;
import org.primefaces.component.column.Column;
import org.primefaces.component.dashboard.Dashboard;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.panel.Panel;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.PieChartModel;

import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.WidgetLoader;
import fr.protogen.masterdata.model.SWidget;

@ManagedBean
@RequestScoped
public class ManageDashboardBean {  
	  
    /**
	 * 
	 */
	private DashboardModel model=new DefaultDashboardModel();  
    private List<SWidget> widgets = new ArrayList<SWidget>();
    private Dashboard dashboard;
    private String typeDashboard;
    
    private List<SWidget> selectedWidgets;
    
    private List<SWidget> pies = new ArrayList<SWidget>();
    private SWidget pi = new SWidget();
    private PairKVElement messagePie;
    
    private String titre;
    private String type;
    private String contenu;
    
    private boolean pause=true;
    
    @PostConstruct
    public void doLoad() {  
    	
    	
    	boolean notinsession=(!FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
				|| FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")==null); 

		if(notinsession){
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		FacesContext fc = FacesContext.getCurrentInstance();
		Application application = fc.getApplication();
    	
        model = new DefaultDashboardModel();  
        DashboardColumn dcolumn = new DefaultDashboardColumn();
        
        
        dashboard = (Dashboard) application.createComponent(fc, "org.primefaces.component.Dashboard", "org.primefaces.component.DashboardRenderer");
        
        dashboard.setModel(model);
        WidgetLoader loader = new WidgetLoader();
        
        String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
        int userId = ApplicationRepository.getInstance().getCache(skey).getUser().getId();
        String appKey = ApplicationRepository.getInstance().getCache(skey).getAppKey();
        widgets = loader.getWidgetsApplication(appKey,userId);
        model.addColumn(dcolumn);
        typeDashboard = loader.getTypedashboard(userId, appKey);
        
        for( int i=0;i<widgets.size();i++ ) {
        	
        	SWidget widget = widgets.get(i);
            Panel panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel", "org.primefaces.component.PanelRenderer");
            panel.setId("measure_" + i);
            panel.setHeader(widget.getTitle());
            panel.setClosable(true);
 
            getDashboard().getChildren().add(panel);
            DashboardColumn column = model.getColumn(0);
            column.addWidget(panel.getId());
            if(widget.getType() == 'C'){
            	populateLineChart(panel,i,application, widget);
            } else if(widget.getType() == 'P'){
            	populateChart(panel,i,application, widget);
            } else if(widget.getType() == 'T'){
            	populateTable(panel,i,application, widget);
            }
        	 
            
        }
    }  
    
    public void switchPlay(){
		pause=!pause;
	}
    
    private void populateTable(Panel panel, int i, Application application, SWidget widget) {
		// TODO Auto-generated method stub
    	ELContext context = FacesContext.getCurrentInstance().getELContext();
    	DataTable table = (DataTable) application.createComponent(DataTable.COMPONENT_TYPE);
        table.setValue(widget.getDataTable());
        table.setVar("item");
        
        if(widget.getDataTable() == null || widget.getDataTable().size()==0){
        	ValueExpression value = (new ExpressionFactoryImpl()).createValueExpression(context, "Aucune donnée disponible", String.class);
        	UIOutput output = new UIOutput() ;
        	output.setValueExpression("value", value);
        	panel.getChildren().add(output);
        	return;
        }
        for(int j = 0 ; j<widget.getDataTable().get(0).size();j++){
	        Column indexColumn = (Column) application.createComponent(Column.COMPONENT_TYPE);
	        UIOutput indexColumnTitle = (UIOutput)application.createComponent(UIOutput.COMPONENT_TYPE);
	        indexColumnTitle.setValue(widget.getDataCaptions().get(j));
	        indexColumn.getFacets().put("header", indexColumnTitle);
	        table.getChildren().add( indexColumn );
	        
	        ValueExpression indexValueExp = (new ExpressionFactoryImpl()).createValueExpression(context, "#{item["+j+"]}", Object.class);
	        HtmlOutputText indexOutput = (HtmlOutputText)application.createComponent( HtmlOutputText.COMPONENT_TYPE );
	        indexOutput.setValueExpression("value", indexValueExp);
	        indexColumn.getChildren().add( indexOutput );  
        }
        table.setStyle("width:100%;");
        panel.setStyle("margin-bottom:20px");
        panel.setStyle("margin-top:20px");
        panel.getChildren().add(table);
        
        
	}

    private void populateChart(Panel panel, int i, Application application, SWidget widget) {
		// TODO Auto-generated method stub
		ELContext context = FacesContext.getCurrentInstance().getELContext();
    	
    	if(widget.getModel() == null || widget.getModel().getData() == null || widget.getModel().getData().size()==0){
			ValueExpression value = (new ExpressionFactoryImpl()).createValueExpression(context, "Aucune donnée disponible", String.class);
        	UIOutput output = new UIOutput() ;
        	output.setValueExpression("value", value);
        	panel.getChildren().add(output);
        	
			return;
		}
    	
    	/*PieChart chart = (PieChart) application.createComponent(PieChart.COMPONENT_TYPE);
    	chart.setValueExpression("value", (new ExpressionFactoryImpl()).createValueExpression(context, "#{dashboardBean.widgets["+i+"].model}", PieChartModel.class));
    	chart.setShowDataLabels(true);
    	chart.setDiameter(200);
    	chart.setFill(true);
    	chart.setLegendPosition("w");
    	chart.setLegendCols(2);
    	chart.setSliceMargin(5);
    	chart.setId("wdg_"+widget.getId());
    	chart.setWidgetVar("wdg_"+widget.getId());
    	chart.setStyle("width:100%;height:100%;text-align:center;padding:0;margin:0 auto");
    	panel.setStyle("margin:20px");
    	
    	AjaxBehavior ajaxBehavior = new AjaxBehavior();
    	FacesContext fc = FacesContext.getCurrentInstance();
    	ajaxBehavior.setOncomplete("pieres.show();");
    	ExpressionFactory ef = fc.getApplication().getExpressionFactory();
    	
    	MethodExpression me = ef.createMethodExpression( fc.getELContext(), "#{dashboardBean.selectPie}", String.class, new Class[0]);

    	ajaxBehavior.addAjaxBehaviorListener( new AjaxBehaviorListenerImpl(me,me) );
    	
    	
    	chart.addClientBehavior("itemSelect", ajaxBehavior);
    	panel.getChildren().add(chart);*/
	}
    
    public void selectPie(ItemSelectEvent event){
    /*	PieChart p = (PieChart)event.getSource();
    	int i = event.getItemIndex();
    	int wid = Integer.parseInt(p.getId().split("_")[1]);
    	
    	for(SWidget w : widgets)
    		if(w.getId()==wid){
    			messagePie = w.getPieData().get(i);
    			RequestContext.getCurrentInstance().update("protogen_main:pieres");
    		}*/
    }
    
   
    
    public void updatePieChart(int w){
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"ça marche",""));
    }
    
    private void populateLineChart(Panel panel, int i, Application application, SWidget widget) {
		// TODO Auto-generated method stub
		ELContext context = FacesContext.getCurrentInstance().getELContext();
    	
    	if(widget.getLineModel() == null || widget.getLineModel().getSeries().get(0).getData() == null || widget.getLineModel().getSeries().get(0).getData().size()==0){
			ValueExpression value = (new ExpressionFactoryImpl()).createValueExpression(context, "Aucune donnée disponible", String.class);
        	UIOutput output = new UIOutput();
        	output.setValueExpression("value", value);
        	panel.getChildren().add(output);
        	
			return;
		}
    	
   /* 	BarChart chart = (BarChart) application.createComponent(BarChart.COMPONENT_TYPE);
    	chart.setValueExpression("value", (new ExpressionFactoryImpl()).createValueExpression(context, "#{dashboardBean.widgets["+i+"].lineModel}", CartesianChartModel.class));
    	chart.setLegendPosition("s");
    	chart.setLegendCols(2);
    	chart.setMax(widget.getMax());
    	chart.setAnimate(true);
    	chart.setMin(0);
    	chart.setStyle("width:100%;height:100%;text-align:center;padding:0;margin:0 auto");
    	panel.setStyle("margin-bottom:20px");
    	panel.setStyle("margin-top:20px");
    	
    	panel.getChildren().add(chart);*/
	}

    public String actionValider() {
//    	System.out.println("===========================================================\n");
    	String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
    	String appkey = ApplicationRepository.getInstance().getCache(skey).getAppKey();
    	int userId = ApplicationRepository.getInstance().getCache(skey).getUser().getId();
    	WidgetLoader loader = new WidgetLoader();
    	System.out.println(widgets);
    	try {
			loader.updateWidget(widgets, userId, appkey);
			loader.saveDashboard(typeDashboard, userId, appkey);
		} catch (Exception e) {
			e.printStackTrace();
		}
//        System.out.println("===========================================================\n");
        return "";
    }
    
    public String deleteAction() {
    	String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
    	String appkey = ApplicationRepository.getInstance().getCache(skey).getAppKey();
    	int userId = ApplicationRepository.getInstance().getCache(skey).getUser().getId();
    	WidgetLoader loader = new WidgetLoader();
    	System.out.println(widgets);
    	try {
    		String wid =(String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("widget_id");
    		int widgetId = Integer.parseInt(wid);
    		SWidget w = null;
    		for(SWidget sw : widgets) {
    			if(sw.getId() == widgetId) {
    				w = sw ;
    				break;
    			}
    		}
			loader.deleteWidget(w, userId, appkey);
			widgets.remove(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
//        System.out.println("===========================================================\n");
        return "";
    }
    
    public void createWidget() {
    	String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
    	String appkey = ApplicationRepository.getInstance().getCache(skey).getAppKey();
    	int userId = ApplicationRepository.getInstance().getCache(skey).getUser().getId();
    	WidgetLoader loader = new WidgetLoader();
    	SWidget widget = new SWidget();
    	widget.setTitle(getTitre());
    	widget.setType(getType().toUpperCase().charAt(0));
    	widget.setQuery(getContenu());
    	try{
    		widgets.add(loader.createWidget(widget, userId, appkey));
    	}catch(Exception e) {
    		
    	}
    }

	public Dashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public void setModel(DashboardModel model) {
		this.model = model;
	}

	public DashboardModel getModel() {  
        return model;  
    }

	public List<SWidget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<SWidget> widgets) {
		this.widgets = widgets;
	}

	public List<SWidget> getPies() {
		return pies;
	}

	public void setPies(List<SWidget> pies) {
		this.pies = pies;
	}

	public SWidget getPi() {
		return pi;
	}

	public void setPi(SWidget pi) {
		this.pi = pi;
	}

	public PairKVElement getMessagePie() {
		return messagePie;
	}

	public void setMessagePie(PairKVElement messagePie) {
		this.messagePie = messagePie;
	}  
	
	public boolean isPause(){
		return pause;
	}
	
	public void setPause(boolean pause){
		this.pause = pause;
	}

	public List<SWidget> getSelectedWidgets() {
		return selectedWidgets;
	}

	public void setSelectedWidgets(List<SWidget> selectedWidgets) {
		this.selectedWidgets = selectedWidgets;
	}

	/**
	 * @return the titre
	 */
	public String getTitre() {
		return titre;
	}

	/**
	 * @param titre the titre to set
	 */
	public void setTitre(String titre) {
		this.titre = titre;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the contenu
	 */
	public String getContenu() {
		return contenu;
	}

	/**
	 * @param contenu the contenu to set
	 */
	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	/**
	 * @return the typeDashboard
	 */
	public String getTypeDashboard() {
		return typeDashboard;
	}

	/**
	 * @param typeDashboard the typeDashboard to set
	 */
	public void setTypeDashboard(String typeDashboard) {
		this.typeDashboard = typeDashboard;
	}
}
