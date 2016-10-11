package fr.protogen.engine.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.model.CoreUser;

@ManagedBean
@RequestScoped
public class ExportDocumentView {
	
	//	Document statics
	@ManagedProperty(value="#{listViewControl.windowTitle}")
	private String documentTitle;
	
	private String exportDateTime;
	private String currentUser;
		
	
	//	Linking to data
	@ManagedProperty(value="#{listViewControl.titles}")
	private List<PairKVElement> dataTitles = new ArrayList<PairKVElement>();
	@ManagedProperty(value="#{listViewControl.values}")
	private List<List<String>> dataValues = new ArrayList<List<String>>();
	@ManagedProperty(value="#{userServices.user}")
	private CoreUser user;
	
	
	@PostConstruct
	public void postExportDocumentView(){
		
		boolean notinsession=(documentTitle==null || !FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
				|| FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")==null); 

		if(notinsession){
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		java.util.Date date = new java.util.Date();
		exportDateTime = date.toString();
		currentUser = user.getFirstName()+" "+user.getLastName();
		
		
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	public String getExportDateTime() {
		return exportDateTime;
	}

	public void setExportDateTime(String exportDateTime) {
		this.exportDateTime = exportDateTime;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public List<PairKVElement> getDataTitles() {
		return dataTitles;
	}

	public void setDataTitles(List<PairKVElement> dataTitles) {
		this.dataTitles = dataTitles;
	}

	public List<List<String>> getDataValues() {
		return dataValues;
	}

	public void setDataValues(List<List<String>> dataValues) {
		this.dataValues = dataValues;
	}

	public CoreUser getUser() {
		return user;
	}

	public void setUser(CoreUser user) {
		this.user = user;
	}

	

}
