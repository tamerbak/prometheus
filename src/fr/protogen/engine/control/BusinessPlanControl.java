package fr.protogen.engine.control;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import fr.protogen.asgard.dao.MetadataLoader;
import fr.protogen.asgard.dao.ParameteredDataEngine;
import fr.protogen.asgard.metamodel.BPLigneTemplate;
import fr.protogen.asgard.metamodel.BPLigneValue;
import fr.protogen.asgard.metamodel.BPPage;
import fr.protogen.asgard.metamodel.BPTab;
import fr.protogen.asgard.metamodel.BPVariable;
import fr.protogen.asgard.metamodel.BPYear;
import fr.protogen.asgard.metamodel.BPYearValue;
import fr.protogen.asgard.metamodel.BusinessPlanTemplate;
import fr.protogen.asgard.metamodel.OptionAvancee;
import fr.protogen.asgard.metamodel.TypeBusinessPlan;

import javax.servlet.ServletContext; 

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class BusinessPlanControl implements Serializable{

	//private BusinessPlan businessPlan;
	private String selectedTypePlan;
	private double coefficientCroissance=1.20;
	private int nombreAnnee = 3;
	private TypeBusinessPlan typePlan;
	private int showPlanComptable=0;
	private int anneeDemarrage;
	private int typeInterval=1;
	private int inteval=3;
	private BusinessPlanTemplate businessplan;
	private List<BPPage> tabs = new ArrayList<BPPage>();
	
	private List<OptionAvancee> selectionOptionsAvancees;
	private List<OptionAvancee> optionsAvancees;
	private boolean ca=false;
	private boolean serviceExt=false;
	private boolean impotsTaxes=false;
	private boolean fraisPersonnel=false;
	private boolean dotations=false;
	private boolean chargesfin=false;
	private boolean autres=false;
	

	private String pageTitle="Paramétrage du Business Plan";
	
	
	public BusinessPlanControl() {
		MetadataLoader dal = new MetadataLoader();
		optionsAvancees = dal.loadAllOptions();
		selectionOptionsAvancees = optionsAvancees;
		businessplan = dal.getBusinessPlanById(1);
		tabs = businessplan.getPages();
		ParameteredDataEngine engine = new ParameteredDataEngine();
		anneeDemarrage = engine.getYear(businessplan.getCurrentYearQuery());
		
		
	}
	
	public void showHideCA(){
		ca = !ca;
	}
	public void showHideServiceExt(){
		serviceExt = !serviceExt;
	}
	
	public void showHideImpotsTaxes(){
		impotsTaxes = !impotsTaxes;
	}
	public void showHideFraisPersonnel(){
		fraisPersonnel = !fraisPersonnel;
	}
	public void showHideDotations(){
		dotations =!dotations;
	}
	public void showHideChargesfin(){
		chargesfin =!chargesfin;
	}
	public void showHideAutres(){
		autres =!autres;
	}
	

	public String onFlowProcess(FlowEvent event) { 
		if(event.getOldStep().equals("parametrebp")){
			pageTitle = "Paramétrage du Business Plan";
			loadYearsAndValues();
		}
		
		if(event.getOldStep().equals("bfrtab") && event.getNewStep().equals("ventetab")){
			return "parametrebp";
		}
		if(event.getOldStep().equals("ventetab")){
			return "cptprod";
		}
		
		RequestContext.getCurrentInstance().execute("dlg2.hide()");
		return event.getNewStep();
	}
	
	private void loadYearsAndValues() {
		ParameteredDataEngine engine = new ParameteredDataEngine();
		int baseYear = engine.getYear(businessplan.getCurrentYearQuery());
		businessplan.setYears(new ArrayList<BPYear>());
		for(int i = 0; i<inteval;i++){
			int year = anneeDemarrage+i;
			BPYear y = new BPYear();
			y.setFuture(year>baseYear);
			y.setYear(year);
			businessplan.getYears().add(y);
		}
		
		for(BPPage p : tabs){
			p.setTitles(new ArrayList<String>());
			
			for(BPYear r : businessplan.getYears())
				p.getTitles().add(r.getYear()+"");
			for(BPTab t : p.getTabs()){
				t.setFooter("Total "+t.getTitle());
				t.setTitles(new ArrayList<String>());
				
				for(BPYear r : businessplan.getYears())
					t.getTitles().add(r.getYear()+"");
				for(BPLigneTemplate l : t.getLignes()){
					l.setValues(new ArrayList<BPLigneValue>());
					for(BPYear y : businessplan.getYears()){
						if(y.isFuture())
							continue;
						double v = engine.getValue(y,l);
						BPLigneValue value = new BPLigneValue(y,v);
						l.getValues().add(value);
					}
					for(BPYear y : businessplan.getYears()){
						if(!y.isFuture())
							continue;
						
						int index = businessplan.getYears().indexOf(y)-1; 
						BPYear temoin = businessplan.getYears().get(index);
						
						double v =0;
						for(BPLigneValue lv : l.getValues())
							if(lv.getYear() == temoin){
								v=lv.getValue()*coefficientCroissance;
								break;
							}
						
						BPLigneValue value = new BPLigneValue(y, v);
						l.getValues().add(value);
					}
				}
				t.setParentLignes(new ArrayList<BPLigneTemplate>());
				for(BPLigneTemplate l : t.getLignes()){
					if(!l.isFinalLevel())
						continue;
					l.setParent(null);
				}
				for(BPLigneTemplate l : t.getLignes()){
					if(l.isFinalLevel())
						continue;
					t.getParentLignes().add(l);
					for(BPLigneTemplate c : l.getChildren()){
						c.setParent(l);
					}
					l.setExistChild(true);
				}
				for(BPLigneTemplate l : t.getLignes()){
					if(!l.isFinalLevel())
						continue;
					if(l.getParent()==null)
						t.getParentLignes().add(l);
					l.setChildren(new ArrayList<BPLigneTemplate>());
					l.setExistChild(false);
				}
				
				t.setTotal(new ArrayList<Double>());
				for(BPYear y : businessplan.getYears())
					t.getTotal().add(new Double(0));
				for(BPLigneTemplate l : t.getParentLignes()){
					for(BPLigneValue lv : l.getValues()){
						double d = t.getTotal().get(l.getValues().indexOf(lv))+lv.getValue();
						t.getTotal().set(l.getValues().indexOf(lv), new Double(d));
					}
				}
				
			}
			for(BPVariable v : p.getVariables()){
				v.setValues(new ArrayList<Double>());
				for(int i = 0; i < businessplan.getYears().size() ;i++){
				
					String formula=v.getFormula();
					for(BPTab t : p.getTabs()){
						Double tval = t.getTotal().get(i);
						formula = formula.replaceAll("<<"+t.getTitle()+">>", "("+tval+")");
					}
					ScriptEngineManager mgr = new ScriptEngineManager();
					ScriptEngine sengine = mgr.getEngineByName("JavaScript");
					Double d=new Double(0);
					try {
						String sd = sengine.eval(formula).toString();
						d = new Double(sd);
					} catch (ScriptException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					v.getValues().add(d);
				}
			}
		}
		return;
	}

	public void showHideChildren(){
		String sparentid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("PARENTID");
		int id=0;
		if(sparentid != null && sparentid.length()>0)
			id=Integer.parseInt(sparentid);
		
		//	look for the parent
		BPLigneTemplate parent = new BPLigneTemplate();
		
		for(BPPage p : tabs){
			boolean flag=false;
			for(BPTab t : p.getTabs()){
				
				for(BPLigneTemplate l : t.getParentLignes())
					if(l.getId() == id){
						parent = l;
						flag=true;
						break;
					}
				if(flag)
					break;
			}
			if(flag)
				break;
		}
		parent.setChildrenVisible(!parent.isChildrenVisible());
	}
	
	
	public String venteDownload(){
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
	    ec.responseReset();
	    ec.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"ventes.xlsx\"");
	    OutputStream output;
	    
		try {
			output = ec.getResponseOutputStream();
			InputStream is = new FileInputStream(ec.getRealPath("")+"/resources/Vente.xlsx");
			
			byte[] content = IOUtils.toByteArray(is);
			
			is.close();
			output.write(content);
			output.close();
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	    
		return "";
	}
	public String tresorDownload(){
		FacesContext fc = FacesContext.getCurrentInstance();
	    ExternalContext ec = fc.getExternalContext();
	    ec.responseReset();
	    ec.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"trésorerie.xlsx\"");
	    OutputStream output;
	    
		try {
			output = ec.getResponseOutputStream();
			InputStream is = new FileInputStream(ec.getRealPath("")+"/resources/tresor.xlsx");
			
			byte[] content = IOUtils.toByteArray(is);
			
			is.close();
			output.write(content);
			output.close();
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	    
		return "";
	}
	
	/*
	 * 	GETTERS AND SETTERS
	 */
	public String getSelectedTypePlan() {
		return selectedTypePlan;
	}

	public void setSelectedTypePlan(String selectedTypePlan) {
		this.selectedTypePlan = selectedTypePlan;
	}

	public List<OptionAvancee> getSelectionOptionsAvancees() {
		return selectionOptionsAvancees;
	}

	public void setSelectionOptionsAvancees(
			List<OptionAvancee> selectionOptionsAvancees) {
		this.selectionOptionsAvancees = selectionOptionsAvancees;
	}

	public List<OptionAvancee> getOptionsAvancees() {
		return optionsAvancees;
	}

	public void setOptionsAvancees(List<OptionAvancee> optionsAvancees) {
		this.optionsAvancees = optionsAvancees;
	}

	public double getCoefficientCroissance() {
		return coefficientCroissance;
	}

	public void setCoefficientCroissance(double coefficientCroissance) {
		this.coefficientCroissance = coefficientCroissance;
	}

	public int getNombreAnnee() {
		return nombreAnnee;
	}

	public void setNombreAnnee(int nombreAnnee) {
		this.nombreAnnee = nombreAnnee;
	}

	public TypeBusinessPlan getTypePlan() {
		return typePlan;
	}

	public void setTypePlan(TypeBusinessPlan typePlan) {
		this.typePlan = typePlan;
	}

	public int getShowPlanComptable() {
		return showPlanComptable;
	}

	public void setShowPlanComptable(int showPlanComptable) {
		this.showPlanComptable = showPlanComptable;
	}

	public int getAnneeDemarrage() {
		return anneeDemarrage;
	}

	public void setAnneeDemarrage(int anneeDemarrage) {
		this.anneeDemarrage = anneeDemarrage;
	}

	public int getTypeInterval() {
		return typeInterval;
	}

	public void setTypeInterval(int typeInterval) {
		this.typeInterval = typeInterval;
	}

	public int getInteval() {
		return inteval;
	}

	public void setInteval(int inteval) {
		this.inteval = inteval;
	}

	public BusinessPlanTemplate getBusinessplan() {
		return businessplan;
	}

	public void setBusinessplan(BusinessPlanTemplate businessplan) {
		this.businessplan = businessplan;
	}

	public List<BPPage> getTabs() {
		return tabs;
	}

	public void setTabs(List<BPPage> tabs) {
		this.tabs = tabs;
	}

	public boolean isCa() {
		return ca;
	}

	public void setCa(boolean ca) {
		this.ca = ca;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public boolean isImpotsTaxes() {
		return impotsTaxes;
	}

	public void setImpotsTaxes(boolean impotsTaxes) {
		this.impotsTaxes = impotsTaxes;
	}

	public boolean isDotations() {
		return dotations;
	}

	public void setDotations(boolean dotations) {
		this.dotations = dotations;
	}

	public boolean isChargesfin() {
		return chargesfin;
	}

	public void setChargesfin(boolean chargesfin) {
		this.chargesfin = chargesfin;
	}
	public boolean isAutres() {
		return autres;
	}

	public void setAutres(boolean autres) {
		this.autres = autres;
	}
	public boolean isFraisPersonnel() {
		return fraisPersonnel;
	}

	public void setFraisPersonnel(boolean fraisPersonnel) {
		this.fraisPersonnel = fraisPersonnel;
	}

		
	public boolean isServiceExt() {
		return serviceExt;
	}
	

	public void setServiceExt(boolean serviceExt) {
		this.serviceExt = serviceExt;
	}
	
}
