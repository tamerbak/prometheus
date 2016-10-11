package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.primefaces.model.StreamedContent;

import fr.protogen.masterdata.model.CAttribute;

public class UIControlElement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8052061328725203573L;
	private String controlID;
	private String controlCode;
	private String controlValue="";
	private String label;
	private CAttribute attribute;
	private String title;
	private boolean visible;
	private boolean reference;
	private List<PairKVElement> listReference;
	private String trueValue="";
	private boolean ctrlDate = false;
	private Date dateValue;
	private boolean binaryContent;
	private boolean nonVoidContent;
	private StreamedContent file;
	private String content;
	private String referenceTable;
	private boolean readOnly = false;
	private boolean filtrable = false;
	private boolean money=false;
	private int colspan=0;
	private boolean hide;
	private boolean booleanValue = false;
	private boolean textarea;
	private String searchKeyWords="";
	private List<UIControlElement> inlineControls = new ArrayList<UIControlElement>();
	private UIControlsLine inlineControlLines = new UIControlsLine(); 
	
	private String uniqueID;
		
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
		if(booleanValue)	//	In case this field is a boolean one !!
			return "Oui";
		if(controlValue == null || controlValue.length()==0 || controlValue.equals(""))
			return "";
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
		if(dateValue==null && attribute.getDefaultValue().equals("DATE_JOUR"))
			dateValue = new Date();
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
	public boolean isFiltrable() {
		return filtrable;
	}
	public void setFiltrable(boolean filtrable) {
		this.filtrable = filtrable;
	}
	public boolean isMoney() {
		return money;
	}
	public void setMoney(boolean money) {
		this.money = money;
	}
	public int getColspan() {
		return colspan;
	}
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}
	public boolean isHide() {
		return hide;
	}
	public void setHide(boolean hide) {
		this.hide = hide;
	}
	public boolean isBooleanValue() {
		return booleanValue;
	}
	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
		this.controlValue = booleanValue?"Oui":"Non";
		this.trueValue = this.controlValue;
	}
	/**
	 * @return the textarea
	 */
	public boolean isTextarea() {
		return textarea;
	}
	/**
	 * @param textarea the textarea to set
	 */
	public void setTextarea(boolean textarea) {
		this.textarea = textarea;
	}
	public String getSearchKeyWords() {
		return searchKeyWords;
	}
	public void setSearchKeyWords(String searchKeyWords) {
		this.searchKeyWords = searchKeyWords;
	}
	public List<UIControlElement> getInlineControls() {
		return inlineControls;
	}
	public void setInlineControls(List<UIControlElement> inlineControls) {
		this.inlineControls = inlineControls;
	}
	public UIControlsLine getInlineControlLines() {
		return inlineControlLines;
	}
	public void setInlineControlLines(UIControlsLine inlineControlLines) {
		this.inlineControlLines = inlineControlLines;
	}
	public String getUniqueID() {
		return uniqueID;
	}
	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}
	
	
	
}
