package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;


@SuppressWarnings("serial")
public class CComposedBean implements Serializable {
	private int id;
	private int beanId;
	private List<CComposingeBean> composition;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBeanId() {
		return beanId;
	}
	public void setBeanId(int beanId) {
		this.beanId = beanId;
	}
	public List<CComposingeBean> getComposition() {
		return composition;
	}
	public void setComposition(List<CComposingeBean> composition) {
		this.composition = composition;
	}
}
