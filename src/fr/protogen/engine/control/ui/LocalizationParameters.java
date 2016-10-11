package fr.protogen.engine.control.ui;

import java.io.Serializable;

import fr.protogen.masterdata.model.CoreUser;

@SuppressWarnings("serial")
public class LocalizationParameters implements Serializable {
	private String langCode;
	private String dateCode;
	private String dateFormat;
	
	public LocalizationParameters(CoreUser user){
		langCode = user.getLanguage();
		dateCode = langCode;
		dateFormat = "dd/MM/yyyy";
		if(dateCode.equals("en")){
			dateFormat = "MM/dd/yyyy";
		}
	}
	
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	public String getDateCode() {
		return dateCode;
	}
	public void setDateCode(String dateCode) {
		this.dateCode = dateCode;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
}
