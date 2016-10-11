package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CListViewSynthesis implements Serializable {
	private int id = 0;
	private String expression;
	private CWindow window = new CWindow();
	private String libelle;
	private List<CLVSTable> tables = new ArrayList<CLVSTable>();
	
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public List<CLVSTable> getTables() {
		return tables;
	}
	public void setTables(List<CLVSTable> tables) {
		this.tables = tables;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
}
