package fr.protogen.asgard.metamodel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BPLigneTemplate implements Serializable  {
	private int id;
	private String compte;
	private String label;
	private String parameteredQuery;
	private boolean finalLevel;
	private List<BPLigneTemplate> children;
	private BPLigneTemplate parent;
	private List<BPLigneValue> values;
	private boolean childrenVisible=false;
	private boolean existChild=false;;
	
	public String getCompte() {
		return compte;
	}
	public void setCompte(String compte) {
		this.compte = compte;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getParameteredQuery() {
		return parameteredQuery;
	}
	public void setParameteredQuery(String parameteredQuery) {
		this.parameteredQuery = parameteredQuery;
	}
	public boolean isFinalLevel() {
		return finalLevel;
	}
	public void setFinalLevel(boolean finalLevel) {
		this.finalLevel = finalLevel;
	}
	public List<BPLigneTemplate> getChildren() {
		return children;
	}
	public void setChildren(List<BPLigneTemplate> children) {
		this.children = children;
	}
	public BPLigneTemplate getParent() {
		return parent;
	}
	public void setParent(BPLigneTemplate parent) {
		this.parent = parent;
	}
	public List<BPLigneValue> getValues() {
		return values;
	}
	public void setValues(List<BPLigneValue> values) {
		this.values = values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isChildrenVisible() {
		return childrenVisible;
	}
	public void setChildrenVisible(boolean childrenVisible) {
		this.childrenVisible = childrenVisible;
	}
	public boolean isExistChild() {
		return existChild;
	}
	public void setExistChild(boolean existChild) {
		this.existChild = existChild;
	}
}
