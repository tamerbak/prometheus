package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.primefaces.model.StreamedContent;

import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class UIFilterElement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8052061328725203573L;
	private String controlID;
	private String controlCode;
	private String controlValue;
	private String label;
	private CAttribute attribute;
	private String title;
	private boolean visible;
	private boolean reference;
	private List<PairKVElement> listReference;
	private String trueValue;
	private boolean ctrlDate = false;
	private Date dateValue;
	private boolean binaryContent;
	private boolean nonVoidContent;
	private StreamedContent file;
	private String content;
	private String referenceTable;
	private boolean readOnly = false;
	private boolean booleanValue;
	
	private String lthan;
	private String gthan;
	private Date bdateValue;
	private Date adateValue;
	
	private boolean forced = false;
	private boolean multiple=false;
	private CBusinessClass entity;
	
	private boolean activated=true;
	
	
	public boolean isReference() {
		return reference;
	}
	public void setReference(boolean reference) {
		this.reference = reference;
	}
	public List<PairKVElement> getListReference() {
		return listReference;
	}
	public void setListReference(List<PairKVElement> listReference) {
		this.listReference = listReference;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean isVisible) {
		this.visible = isVisible;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getControlID() {
		return controlID;
	}
	public void setControlID(String controlID) {
		this.controlID = controlID;
	}
	public String getControlCode() {
		return controlCode;
	}
	public void setControlCode(String controlCode) {
		this.controlCode = controlCode;
	}
	public String getControlValue() {
		return controlValue;
	}
	public void setControlValue(String controlValue) {
		this.controlValue = controlValue;
	}
	public CAttribute getAttribute() {
		return attribute;
	}
	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}
	public String getTrueValue() {
		return trueValue;
	}
	public void setTrueValue(String trueValue) {
		this.trueValue = trueValue;
		if(isReference())
		{
			for(PairKVElement e : listReference){
				if(e.getValue().equals(trueValue)){
					controlValue = e.getKey();
					break;
				}
			}
		}
	}
	public boolean isCtrlDate() {
		return ctrlDate;
	}
	public void setCtrlDate(boolean ctrlDate) {
		this.ctrlDate = ctrlDate;
	}
	public Date getDateValue() {
		return dateValue;
	}
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
		try{
			controlValue = dateValue.toString();
		}catch(Exception exc){
			
		}
	}
	public boolean isNonVoidContent() {
		return nonVoidContent;
	}
	public void setNonVoidContent(boolean nonVoidContent) {
		this.nonVoidContent = nonVoidContent;
	}
	public StreamedContent getFile() {
		return file;
	}
	public void setFile(StreamedContent file) {
		this.file = file;
	}
	public boolean isBinaryContent() {
		return binaryContent;
	}
	public void setBinaryContent(boolean binaryContent) {
		this.binaryContent = binaryContent;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getReferenceTable() {
		return referenceTable;
	}
	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public String getLthan() {
		return lthan;
	}
	public void setLthan(String lthan) {
		this.lthan = lthan;
	}
	public String getGthan() {
		return gthan;
	}
	public void setGthan(String gthan) {
		this.gthan = gthan;
	}
	public Date getBdateValue() {
		return bdateValue;
	}
	public void setBdateValue(Date bdateValue) {
		this.bdateValue = bdateValue;
		try{
			Calendar c = Calendar.getInstance();
			c.setTime(bdateValue);
			lthan = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
		}catch(Exception exc){
			lthan="";
		}
	}
	public Date getAdateValue() {
		return adateValue;
		
	}
	public void setAdateValue(Date adateValue) {
		 
		this.adateValue = adateValue;
		try{
			Calendar c = Calendar.getInstance();
			c.setTime(adateValue);
			gthan = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
		}catch(Exception exc){
			gthan="";
		}
	}
	public boolean isForced() {
		return forced;
	}
	public void setForced(boolean forced) {
		this.forced = forced;
	}
	public boolean isBooleanValue() {
		return booleanValue;
	}
	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
		this.controlValue = booleanValue?"OUI":"NON";
	}
	public boolean isMultiple() {
		return multiple;
	}
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public boolean isActivated() {
		return activated;
	}
	public void setActivated(boolean activated) {
		this.activated = activated;
	}
	
	
}
