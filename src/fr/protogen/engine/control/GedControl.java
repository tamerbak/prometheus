package fr.protogen.engine.control;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.annotation.PostConstruct;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.UIFilterElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.OCRDataAccess;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.OCRDriverBean;
import fr.protogen.masterdata.model.OCRHistory;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped 
public class GedControl implements Serializable {
	private String selectedEntity;
	private int idEntity;
	private String selectedBean;
	private List<PairKVElement> beans = new ArrayList<PairKVElement>();
	private String selectedDriver;

	private List<UIFilterElement> searchControls;
	private boolean searchControlsEnabled = false;

	/*
	 * Results table
	 */
	private List<OCRHistory> docs = new ArrayList<OCRHistory>();
	private OCRHistory selectedDoc;
	
	/*
	 * OCR
	 */
	private List<OCRDriverBean> drivers;
	
	@SuppressWarnings("unchecked")
	public GedControl() {
		//SEARCH_CTRLS
		if(FacesContext.getCurrentInstance().
				getExternalContext().getSessionMap().containsKey("SEARCH_CTRLS")){
			searchControls = (List<UIFilterElement>)FacesContext.getCurrentInstance().
					getExternalContext().getSessionMap().get("SEARCH_CTRLS");
		}
	}
	
	@PostConstruct
	public void energize(){
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");
		String appkey = ApplicationRepository.getInstance().getCache(skey)
				.getAppKey();
		ApplicationLoader dal = new ApplicationLoader();
		drivers = dal.loadDrivers(appkey);
	}
	
	public void entityChanged() {
		if (selectedEntity.equals(""))
			return;
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationLoader dal = new ApplicationLoader();
		beans = engine.getDataKeys(selectedEntity, false, 0);
		CBusinessClass ent = dal.getEntity(selectedEntity);
		idEntity = ent.getId();

		// Prepare semantic access filters
		searchControls = new ArrayList<UIFilterElement>();
		List<CAttribute> attributes = ent.getAttributes();
		searchControlsEnabled = (attributes != null && attributes.size() > 0);
		for (CAttribute attribute : attributes) {

			if (attribute.getDataReference().startsWith("pk_")
					|| attribute.isMultiple())
				continue;

			UIFilterElement element = new UIFilterElement();

			System.out.println("************ parsing "
					+ attribute.getAttribute());
			String type = attribute.getCAttributetype().getType();
			if (type == null)
				type = "";
			if (attribute.isReference()) {
				String referenceTable = attribute.getDataReference().substring(
						3);
				String referencedEntity = dal.getEntityFromDR(referenceTable);
				CBusinessClass e = dal.getEntity(referenceTable);
				ApplicationCache cache = ApplicationRepository.getInstance()
						.getCache(
								(String) FacesContext.getCurrentInstance()
										.getExternalContext().getSessionMap()
										.get("USER_KEY"));
				List<PairKVElement> list = engine.getDataKeys(referenceTable,
						(e.getUserRestrict() == 'Y'), cache.getUser().getId());
				element.setReferenceTable(referencedEntity);
				List<PairKVElement> listElements = new ArrayList<PairKVElement>();
				if (!attribute.isMandatory())
					listElements.add(new PairKVElement("0", ""));
				for (PairKVElement kv : list) {
					listElements.add(kv);
				}
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute().replaceAll("ID ",
						""));
				element.setControlValue("");
				element.setListReference(listElements);
				element.setReference(true);
				searchControls.add(element);
				continue;
			}
			if (type.equals("ENTIER")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (type.toUpperCase().equals("HEURE")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (type.equals("TEXT") || type.equals("Texte")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (type.toUpperCase().equals("DATE")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
				element.setCtrlDate(true);
			} else if (type.toUpperCase().equals("DOUBLE")) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (attribute.getCAttributetype().getId() == 7) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				ApplicationCache cache = ApplicationRepository.getInstance()
						.getCache(
								(String) FacesContext.getCurrentInstance()
										.getExternalContext().getSessionMap()
										.get("USER_KEY"));
				CoreUser u = cache.getUser();
				element.setControlValue(u.getFirstName() + " - "
						+ u.getLastName());
				element.setTrueValue(u.getId() + "");
				searchControls.add(element);
			} else if (attribute.getCAttributetype().getId() == 8) {
				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			} else if (attribute.getCAttributetype().getId() == 9) {

				element.setAttribute(attribute);
				element.setControlID(attribute.getAttribute());
				element.setControlValue("");
				searchControls.add(element);
			}
			System.out.println("************ parsed "
					+ attribute.getAttribute());
		}
		
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("SEARCH_CTRLS", searchControls);
		
		return;
	}

	public String suivant() {
		return "protogen-datahistorys2";
	}
	
	/*
	 * Research
	 */
	public String search() {
		OCRDataAccess dal = new OCRDataAccess();
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				skey);
		docs = dal.lookUp(selectedBean, selectedDriver, idEntity,
				searchControls);

		for (OCRHistory d : docs) {
			for (OCRDriverBean db : drivers) {
				if (d.getDriver().getId() == db.getId()) {
					d.setDriver(db);
					break;
				}
			}
			for (CoreUser u : cache.getUsers()) {
				if (u.getId() == d.getUser().getId()) {
					d.setUser(u);
					break;
				}
			}
		}
		return "protogen-datahistoryres";
	}
	
	public String downloadSelectedDoc() {
		int iddoc = Integer.parseInt(
					FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("DOCTODWN")
				);
		for(OCRHistory d : docs)
			if(d.getId() == iddoc){
				selectedDoc = d;
				break;
			}
		
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		ec.responseReset();
		ec.setResponseContentType("image/png");
		ec.setResponseHeader("Content-Disposition",
				"attachment; filename=\"document.png\"");

		OutputStream output;

		try {
			output = ec.getResponseOutputStream();
			InputStream is = new FileInputStream(selectedDoc.getFileKey());

			byte[] content = IOUtils.toByteArray(is);

			is.close();
			output.write(content);
			output.close();
			output.flush();
			FacesContext.getCurrentInstance().responseComplete();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public String getSelectedEntity() {
		return selectedEntity;
	}
	public void setSelectedEntity(String selectedEntity) {
		this.selectedEntity = selectedEntity;
	}
	public int getIdEntity() {
		return idEntity;
	}
	public void setIdEntity(int idEntity) {
		this.idEntity = idEntity;
	}
	public String getSelectedBean() {
		return selectedBean;
	}
	public void setSelectedBean(String selectedBean) {
		this.selectedBean = selectedBean;
	}
	public List<PairKVElement> getBeans() {
		return beans;
	}
	public void setBeans(List<PairKVElement> beans) {
		this.beans = beans;
	}
	public String getSelectedDriver() {
		return selectedDriver;
	}
	public void setSelectedDriver(String selectedDriver) {
		this.selectedDriver = selectedDriver;
	}
	public List<UIFilterElement> getSearchControls() {
		return searchControls;
	}
	public void setSearchControls(List<UIFilterElement> searchControls) {
		this.searchControls = searchControls;
	}
	public boolean isSearchControlsEnabled() {
		return searchControlsEnabled;
	}
	public void setSearchControlsEnabled(boolean searchControlsEnabled) {
		this.searchControlsEnabled = searchControlsEnabled;
	}
	public List<OCRHistory> getDocs() {
		return docs;
	}
	public void setDocs(List<OCRHistory> docs) {
		this.docs = docs;
	}
	public OCRHistory getSelectedDoc() {
		return selectedDoc;
	}
	public void setSelectedDoc(OCRHistory selectedDoc) {
		this.selectedDoc = selectedDoc;
	}

	public List<OCRDriverBean> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<OCRDriverBean> drivers) {
		this.drivers = drivers;
	}

}
