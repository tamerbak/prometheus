package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.protogen.masterdata.model.CAttribute;

@SuppressWarnings("serial")
public class PairKVElement implements Serializable{
	private String key;
	private String value;
	private String formattedDateValue="";
	private boolean hour;
	private boolean selected;
	private boolean date;
	private int dbID;
	private boolean visible = true;
	private String trimmedValue;
	
	private boolean autoValue;
	private String suffix;
	
	private CAttribute attribute;
	private boolean booleanValue;
	private Date dateValue;
	private boolean reference;
	private List<PairKVElement> listReferences = new ArrayList<PairKVElement>();
	
	public PairKVElement(String k, String v){
		key = k;
		value = v;
		selected = false;
	}
	
	public PairKVElement(String k, String v, boolean h) {
		// TODO Auto-generated constructor stub
		key = k;
		
		value = v;
		selected = false;
		hour = h;
	}

	public PairKVElement(String k, String v, boolean b, String suffix) {
		autoValue = b;
		this.suffix = suffix;
		key = k;
		value = v;
		selected = false;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public int getDbID() {
		return dbID;
	}

	public void setDbID(int dbID) {
		this.dbID = dbID;
	}

	public boolean isHour() {
		return hour;
	}

	public void setHour(boolean hour) {
		this.hour = hour;
	}

	public boolean isDate() {
		return date;
	}

	public void setDate(boolean date) {
		this.date = date;
	}

	public String getFormattedDateValue() {
		return formattedDateValue;
	}

	public void setFormattedDateValue(String formattedDateValue) {
		this.formattedDateValue = formattedDateValue;
	}

	public boolean isAutoValue() {
		return autoValue;
	}

	public void setAutoValue(boolean autoValue) {
		this.autoValue = autoValue;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getTrimmedValue() {
		trimmedValue="";
		if(value!= null && value.length()>40)
			trimmedValue = value.substring(0,40)+"...";
		if(value!= null && value.length()<=40)
			trimmedValue = value;
		return trimmedValue;
	}

	public void setTrimmedValue(String trimmedValue) {
		this.trimmedValue = trimmedValue;
	}

	public CAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(CAttribute attribute) {
		this.attribute = attribute;
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
		this.value = "Oui";
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public List<PairKVElement> getListReferences() {
		return listReferences;
	}

	public void setListReferences(List<PairKVElement> listReferences) {
		this.listReferences = listReferences;
	}

	public boolean isReference() {
		return reference;
	}

	public void setReference(boolean reference) {
		this.reference = reference;
	}

	

}
