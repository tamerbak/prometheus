package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class CUIParameter implements Serializable {
	private int id;
	private String parameterKey;
	private char parameterType;
	private String parameterLabel;
	private String parameterTypeLabel;
	private String value;
	private Date dateValue;
	private boolean ctrlDate = false;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getParameterKey() {
		return parameterKey;
	}
	public void setParameterKey(String parameterKey) {
		this.parameterKey = parameterKey;
	}
	public char getParameterType() {
		return parameterType;
	}
	public void setParameterType(char parameterType) {
		this.parameterType = parameterType;
		switch(parameterType){
		case 'I':parameterTypeLabel = "Entier";break;
		case 'F':parameterTypeLabel = "Nombre à décimale";break;
		case 'D':parameterTypeLabel = "Date";break;
		}
	}
	public String getParameterLabel() {
		return parameterLabel;
	}
	public void setParameterLabel(String parameterLabel) {
		this.parameterLabel = parameterLabel;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Date getDateValue() {
		return dateValue;
	}
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
		value = dateValue.toString();
	}
	public boolean isCtrlDate() {
		return ctrlDate;
	}
	public void setCtrlDate(boolean ctrlDate) {
		this.ctrlDate = ctrlDate;
	}
	public String getParameterTypeLabel() {
		return parameterTypeLabel;
	}
	public void setParameterTypeLabel(String parameterTypeLabel) {
		this.parameterTypeLabel = parameterTypeLabel;
	}
	
	
}
