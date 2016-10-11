package fr.protogen.asgard.metamodel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class BusinessPlanTemplate implements Serializable {
	private double coefficientCroissance=1.20;
	private int nombreAnnee = 3;
	private TypeBusinessPlan typePlan;
	private int showPlanComptable=0;
	private int anneeDemarrage=0;
	private int typeInterval=1;
	private int inteval=3;
	private List<BPYear> years;
	private List<OptionAvancee> optionsAvancees;
	private List<BPPage> pages;
	private List<BPTab> tabs;
	private String currentYearQuery;
	
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
	public List<OptionAvancee> getOptionsAvancees() {
		return optionsAvancees;
	}
	public void setOptionsAvancees(List<OptionAvancee> optionsAvancees) {
		this.optionsAvancees = optionsAvancees;
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
	public List<BPYear> getYears() {
		return years;
	}
	public void setYears(List<BPYear> years) {
		this.years = years;
	}
	public String getCurrentYearQuery() {
		return currentYearQuery;
	}
	public void setCurrentYearQuery(String currentYearQuery) {
		this.currentYearQuery = currentYearQuery;
	}
	public List<BPTab> getTabs() {
		return tabs;
	}
	public void setTabs(List<BPTab> tabs) {
		this.tabs = tabs;
	}
	public List<BPPage> getPages() {
		return pages;
	}
	public void setPages(List<BPPage> pages) {
		this.pages = pages;
	}
	
}
