package fr.protogen.engine.control;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FlowEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

import fr.protogen.asgard.dao.MetadataLoader;
import fr.protogen.asgard.metamodel.BusinessPlanTemplate;
import fr.protogen.asgard.metamodel.OptionAvancee;
import fr.protogen.asgard.model.AgregationFunction;
import fr.protogen.asgard.model.AsgardModel;
import fr.protogen.asgard.model.RepresentedValue;
import fr.protogen.asgard.model.ResultTable;
import fr.protogen.asgard.model.VisitingDimension;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.control.ui.StringListDTO;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class AsgardControl implements Serializable {
	private String selectedValEntityId="";
	private List<CBusinessClass> valEntities=new ArrayList<CBusinessClass>();
	private String selectedValAttributeId="";
	private List<CAttribute> valAttributes = new ArrayList<CAttribute>();
	private String selectedValAgregation="NONE";
	private String selectedValAgregationFormula="";
	private CBusinessClass selectedEntity;
	private CAttribute selectedAttribute;
	private List<CAttribute> dimensions=new ArrayList<CAttribute>();
	
	private AsgardModel model;
	
	private DualListModel<CAttribute> dimensionsAttributes = new DualListModel<CAttribute>();
	
	private List<StringListDTO> simulationData = new ArrayList<StringListDTO>();
	private List<String> titles = new ArrayList<String>();
	
	
	/*
	 * Business Plan
	 */
	private BusinessPlanTemplate businessPlan;
	private String selectedTypePlan;
	private List<OptionAvancee> selectionOptionsAvancees;
	private List<OptionAvancee> optionsAvancees;
	
	/*
	 * 	Analyse des ecarts
	 */
	private String selectedDimensions;
	private String selectedPeriode;
	private String ventesCalculated;
	private String ecartVentes;
	private CartesianChartModel categoryModel;
	
	@PostConstruct
	public void energize(){
		
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
		
		String appkey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("APP_KEY");
		ApplicationLoader dal = new ApplicationLoader();
		valEntities = dal.loadAllEntities(appkey);
		selectedValEntityId = valEntities.get(0).getId()+"";
		valAttributes = new ArrayList<CAttribute>();
		
		for(CAttribute a : valEntities.get(0).getAttributes()){
			if(a.getCAttributetype().getId()==4 || a.getCAttributetype().getId()==8)
				valAttributes.add(a);
		}
		model = new AsgardModel();
		model.setTables(new ArrayList<ResultTable>());
		model.getTables().add(new ResultTable());
		selectedValEntityChange();
		
		
		/*
		 * Business plan
		 */
		MetadataLoader dao = new MetadataLoader();
		optionsAvancees = dao.loadAllOptions();
		selectionOptionsAvancees = optionsAvancees;
		
		/*
		 * Analyse des ecarts
		 */
		ProtogenDataEngine eng = new ProtogenDataEngine();
		double v = eng.getFactures();
		double ecart=v-6319.444;
		v = Math.round(v*100)/100;
		ecart = Math.round(ecart*100)/100;
		ventesCalculated = v+"";
		ecartVentes = ecart+"";;	
		
		
		categoryModel = new CartesianChartModel();  
		  
        ChartSeries resn = new ChartSeries();  
        resn.setLabel("Résultat 2013");
        
        resn.set("Janv", 5800);
        resn.set("Fev", 5000);
        resn.set("Mars", 6500);
        resn.set("Avr", 1900);
        resn.set("Mai", 8000);
        resn.set("Juin", 7050);
        resn.set("Juil", 5500);
        resn.set("Août", 5118);
        resn.set("Sept", 6000);
        resn.set("Oct", 2020);
        resn.set("Nov", 7110);
        resn.set("Dec", 8500);
		
        ChartSeries prev = new ChartSeries();  
        prev.setLabel("Prévisions 2014");
        
        prev.set("Janv", 6319.44);
        prev.set("Fev", 6319.44);
        prev.set("Mars", 6319.44);
        prev.set("Avr", 6319.44);
        prev.set("Mai", 6319.44);
        prev.set("Juin", 6319.44);
        prev.set("Juil", 6319.44);
        prev.set("Août", 6319.44);
        prev.set("Sept", 6319.44);
        prev.set("Oct", 6319.44);
        prev.set("Nov", 6319.44);
        prev.set("Dec", 6319.44);
        
        ChartSeries effectif = new ChartSeries();  
        effectif.setLabel("Réalisé 2014");
        
        effectif.set("Janv", v);
        effectif.set("Fev", 0);
        effectif.set("Mars", 0);
        effectif.set("Avr", 0);
        effectif.set("Mai", 0);
        effectif.set("Juin", 0);
        effectif.set("Juil", 0);
        effectif.set("Août", 0);
        effectif.set("Sept", 0);
        effectif.set("Oct", 0);
        effectif.set("Nov", 0);
        effectif.set("Dec", 0);
        
        categoryModel.addSeries(resn);
        categoryModel.addSeries(prev);
        categoryModel.addSeries(effectif);
	}
	
	public void selectedValEntityChange(){
		CBusinessClass entity = new CBusinessClass();
		int sid = Integer.parseInt(selectedValEntityId);
		for(CBusinessClass e : valEntities)
			if(e.getId()==sid){
				entity = e;
				break;
			}
		valAttributes = new ArrayList<CAttribute>();
		
		for(CAttribute a : entity.getAttributes()){
			if(a.getCAttributetype().getId()==4 || a.getCAttributetype().getId()==8)
				valAttributes.add(a);
		}
		
		selectedEntity = entity;
		selectedValAttributeId = valAttributes.get(0).getId()+"";
		selectedValAttributeChange();
	}
	public void selectedValAttributeChange(){
		selectedAttribute = new CAttribute();
		
		for(CAttribute a : valAttributes)
			if(a.getId()==Integer.parseInt(selectedValAttributeId)){
				selectedAttribute = a;
				break;
			}
		
		List<CAttribute> source =new ArrayList<CAttribute>();
		List<CAttribute> target =new ArrayList<CAttribute>();
		for(CAttribute a : selectedEntity.getAttributes()){
			if (a.getCAttributetype().getId()==4 || a.getCAttributetype().getId()==8 || a.getCAttributetype().getId()==9)
				continue;
			source.add(a);
		}
		dimensionsAttributes = new DualListModel<CAttribute>(source,target);
		
		RepresentedValue representedValue = new RepresentedValue();
		representedValue.setAttribute(selectedAttribute);
		representedValue.setPreformatFunction(AgregationFunction.valueOf(selectedValAgregation));
		representedValue.setPreformatCode(selectedValAgregationFormula);
		
		model.getTables().get(0).setRepresentedValue(representedValue);
	}
	
	public String onFlowProcess(FlowEvent event) {
		
		//	if we are going to hyper dimensions from dimensions
		if(event.getNewStep().equals("hyperdimensions") && event.getOldStep().equals("visitdimensions")){
			dimensions = dimensionsAttributes.getTarget();
			constructAndSimulate();
		}
		return event.getNewStep();
	}
	
	/*
	 * 	GETTERS AND SETTERS
	 */

	private void constructAndSimulate() {
		model.getTables().get(0).setDimensions(new ArrayList<VisitingDimension>());
		
		for(CAttribute a : dimensions){
			VisitingDimension d = new VisitingDimension();
			d.setAttribute(a);
			d.setEndpoint(true);
			model.getTables().get(0).getDimensions().add(d);
		}
		ProtogenDataEngine pde = new ProtogenDataEngine();
		model.getTables().get(0).getRepresentedValue().setPreformatFunction(AgregationFunction.valueOf(selectedValAgregation));
		
		List<List<String>> sData = pde.getAsgardTree(model.getTables().get(0));
		titles = sData.get(0);
		sData.remove(0);
		
		simulationData = new ArrayList<StringListDTO>();
		for(List<String> l : sData)
			simulationData.add(new StringListDTO(l));
	}

	public String getSelectedValEntityId() {
		return selectedValEntityId;
	}

	public void setSelectedValEntityId(String selectedValEntityId) {
		this.selectedValEntityId = selectedValEntityId;
	}

	public List<CBusinessClass> getValEntities() {
		return valEntities;
	}

	public void setValEntities(List<CBusinessClass> valEntities) {
		this.valEntities = valEntities;
	}

	public String getSelectedValAttributeId() {
		return selectedValAttributeId;
	}

	public void setSelectedValAttributeId(String selectedValAttributeId) {
		this.selectedValAttributeId = selectedValAttributeId;
	}

	public List<CAttribute> getValAttributes() {
		return valAttributes;
	}

	public void setValAttributes(List<CAttribute> valAttributes) {
		this.valAttributes = valAttributes;
	}

	public String getSelectedValAgregation() {
		return selectedValAgregation;
	}

	public void setSelectedValAgregation(String selectedValAgregation) {
		this.selectedValAgregation = selectedValAgregation;
	}

	public String getSelectedValAgregationFormula() {
		return selectedValAgregationFormula;
	}

	public void setSelectedValAgregationFormula(
			String selectedValAgregationFormula) {
		this.selectedValAgregationFormula = selectedValAgregationFormula;
	}

	public DualListModel<CAttribute> getDimensionsAttributes() {
		return dimensionsAttributes;
	}

	public void setDimensionsAttributes(DualListModel<CAttribute> dimensionsAttributes) {
		this.dimensionsAttributes = dimensionsAttributes;
	}

	public CBusinessClass getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(CBusinessClass selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public CAttribute getSelectedAttribute() {
		return selectedAttribute;
	}

	public void setSelectedAttribute(CAttribute selectedAttribute) {
		this.selectedAttribute = selectedAttribute;
	}

	public AsgardModel getModel() {
		return model;
	}

	public void setModel(AsgardModel model) {
		this.model = model;
	}

	public List<CAttribute> getDimensions() {
		return dimensions;
	}

	public void setDimensions(List<CAttribute> dimensions) {
		this.dimensions = dimensions;
	}

	public List<StringListDTO> getSimulationData() {
		return simulationData;
	}

	public void setSimulationData(List<StringListDTO> simulationData) {
		this.simulationData = simulationData;
	}

	public List<String> getTitles() {
		return titles;
	}

	public void setTitles(List<String> titles) {
		this.titles = titles;
	}

	public BusinessPlanTemplate getBusinessPlan() {
		return businessPlan;
	}

	public void setBusinessPlan(BusinessPlanTemplate businessPlan) {
		this.businessPlan = businessPlan;
	}

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

	public String getSelectedDimensions() {
		return selectedDimensions;
	}

	public void setSelectedDimensions(String selectedDimensions) {
		this.selectedDimensions = selectedDimensions;
	}

	public String getSelectedPeriode() {
		return selectedPeriode;
	}

	public void setSelectedPeriode(String selectedPeriode) {
		this.selectedPeriode = selectedPeriode;
	}

	public String getVentesCalculated() {
		return ventesCalculated;
	}

	public void setVentesCalculated(String ventesCalculated) {
		this.ventesCalculated = ventesCalculated;
	}

	public String getEcartVentes() {
		return ecartVentes;
	}

	public void setEcartVentes(String ecartVentes) {
		this.ecartVentes = ecartVentes;
	}

	public CartesianChartModel getCategoryModel() {
		return categoryModel;
	}

	public void setCategoryModel(CartesianChartModel categoryModel) {
		this.categoryModel = categoryModel;
	}
}
