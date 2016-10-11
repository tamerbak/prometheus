package fr.protogen.masterdata.model;

@SuppressWarnings("serial")
public class Trigger implements java.io.Serializable {
	private int id;
	private String formula="";
	private CAttribute target=new CAttribute();
	private CBusinessClass reference=new CBusinessClass();
	private CWindow window= new CWindow();
	private boolean isDirect=true;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}
	public CAttribute getTarget() {
		return target;
	}
	public void setTarget(CAttribute target) {
		this.target = target;
	}
	public CBusinessClass getReference() {
		return reference;
	}
	public void setReference(CBusinessClass reference) {
		this.reference = reference;
	}
	public CWindow getWindow() {
		return window;
	}
	public void setWindow(CWindow window) {
		this.window = window;
	}
	public boolean isDirect() {
		return isDirect;
	}
	public void setDirect(boolean isDirect) {
		this.isDirect = isDirect;
	}
	
	
	
	
}
