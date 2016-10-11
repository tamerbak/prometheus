package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.masterdata.model.CListViewSynthesis;

@SuppressWarnings("serial")
public class SynthesisTab implements Serializable {
	private String libelle;
	private CListViewSynthesis clvs = new CListViewSynthesis();
	private List<STTable> tables = new ArrayList<STTable>();
	private String htmlContent;
	
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public CListViewSynthesis getClvs() {
		return clvs;
	}
	public void setClvs(CListViewSynthesis clvs) {
		this.clvs = clvs;
	}
	public List<STTable> getTables() {
		return tables;
	}
	public void setTables(List<STTable> tables) {
		this.tables = tables;
	}
	public String getHtmlContent() {
		return htmlContent;
	}
	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	
}
