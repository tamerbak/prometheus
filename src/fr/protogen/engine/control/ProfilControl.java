package fr.protogen.engine.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import fr.protogen.engine.control.ui.UIWindowACL;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreProfil;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.WindowModule;
import fr.protogen.masterdata.services.HabilitationsService;

@ManagedBean
@SessionScoped
public class ProfilControl {
	private CoreRole role = new CoreRole();
	private int selectedProfilId=0;
	private List<CoreProfil> profils = new ArrayList<CoreProfil>();
	private Date dateDebut=new Date();
	private Date dateFin=new Date();;
	private UploadedFile logo;
	
	private List<Integer> chosenWindows = new ArrayList<Integer>();
	private List<Integer> chosenModules = new ArrayList<Integer>();
	private List<CWindow> allWindows = new ArrayList<CWindow>();
	private List<WindowModule> allModules=new ArrayList<WindowModule>();
	private List<UIWindowACL> windows = new ArrayList<UIWindowACL>();
	private UIWindowACL selectedWindow;
	
	private boolean modification = false;
	private boolean suppression = false;
	private List<Integer> chosenFields=new ArrayList<Integer>();
	private List<CAttribute> attributes = new ArrayList<CAttribute>();
	private List<String> allAtts = new ArrayList<String>();
	private List<String> selAtts = new ArrayList<String>();
	
	/*
	 * TABLES DES HABILITATIONS
	 */
	private List<CoreProfil> tableProfils = new ArrayList<CoreProfil>();
	private CoreProfil profilSelectionne;
	private List<CoreRole> allRoles = new ArrayList<CoreRole>();
	private List<CoreRole> filteredRoles;
	
	/*
	 * NOUVEAU PROFIL
	 */
	private CoreProfil profilToCreate = new CoreProfil();
	
	/*
	 * RESSOURCES
	 */
	private List<PairKVElement> actions = new ArrayList<PairKVElement>();
	private List<PairKVElement> document = new ArrayList<PairKVElement>();
	private List<PairKVElement> procedures = new ArrayList<PairKVElement>();
	private List<PairKVElement> workflows = new ArrayList<PairKVElement>();
	
	private List<String> allTraitements = new ArrayList<String>();
	private List<String> allDocuments = new ArrayList<String>();
	private List<String> allProcedures = new ArrayList<String>();
	private List<String> allWorkflows = new ArrayList<String>();
	
	private List<String> selectedTraitements = new ArrayList<String>();
	private List<String> selectedDocuments = new ArrayList<String>();
	private List<String> selectedProcedures = new ArrayList<String>();
	private List<String> selectedWorkflows = new ArrayList<String>();
	private boolean updateRole=false;
	private boolean updateProfil=false;
	
	private int idProfil = 0;
	
	@PostConstruct
	public void initialize() {
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		HabilitationsService hsrv = new HabilitationsService();
		profils = hsrv.loadProfils(cache.getAppKey());
		tableProfils = profils;
		
		allRoles = hsrv.loadRolesByProfil(profils);		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		setUpdateRole(params.containsKey("ROLE_UPDATE") && params.get("ROLE_UPDATE").equals("TRUE"));
		setUpdateProfil(params.containsKey("PROFIL_UPDATE") && params.get("PROFIL_UPDATE").equals("TRUE"));
		if(updateRole){
			int idrole = Integer.parseInt(params.get("ROLEID"));
			for(CoreRole r : allRoles){
				if(r.getId() == idrole) {
					role = r;
				}
			}
		}
		if(updateProfil){
			int idrole = Integer.parseInt(params.get("PROFILID"));
			for(CoreProfil r : tableProfils){
				if(r.getId() == idrole) {
					profilToCreate = r;
				}
			}
		}
		
		ApplicationLoader dal= new ApplicationLoader();
		allWindows = dal.loadWindows(cache.getAppKey());
		allModules = dal.loadModules(cache.getAppKey());
		actions = hsrv.listActions(cache.getAppKey());
		document = hsrv.listDocuments(cache.getAppKey());
		procedures = hsrv.listProcedures(cache.getAppKey());
		workflows = hsrv.listWorkflows(cache.getAppKey());
		
		for(PairKVElement e : actions)
			allTraitements.add(e.getValue());
		for(PairKVElement e : document)
			allDocuments.add(e.getValue());
		for(PairKVElement e : procedures)
			allProcedures.add(e.getValue());
		for(PairKVElement e : workflows)
			allWorkflows.add(e.getValue());
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		logo = event.getFile();
	}

	public void selectAllScreens(){
		if(windows!=null && !windows.isEmpty()){
			windows = new ArrayList<UIWindowACL>();
		}
		else{
			windows = new ArrayList<UIWindowACL>();
			for(CWindow w : allWindows)
				windows.add(new UIWindowACL(w));
		}
		
	}
	
	public void selectedScreensChange(){
		List<Integer> showedWindows = new ArrayList<Integer>();
		showedWindows.addAll(chosenWindows);
		
		for(Integer moduleId:chosenModules){
			for(WindowModule module:allModules){
				if(module.getId()==moduleId){
					for(CWindow window:module.getCWindows()){
						if(!showedWindows.contains(window.getId())){
							showedWindows.add(window.getId());
						}
					}
					break;
				}
			}
		}
		windows=new ArrayList<UIWindowACL>();
		for(Integer windowId:showedWindows){
			for(CWindow window:allWindows){
				if(window.getId()==windowId){
					windows.add(new UIWindowACL(window));
					break;
				}
			}
		}
	}
	
	public void updateHabil(){
		if(selectedWindow==null)
			return;
		selectedWindow.setModification(modification);
		selectedWindow.setSuppression(suppression);
	}
	
	public void onRowSelection(){
		modification = selectedWindow.isModification();
		suppression = selectedWindow.isSuppression();
		allAtts = new ArrayList<String>();
		selAtts = new ArrayList<String>();
		
		attributes = selectedWindow.getAttributes();
		for(CAttribute a : selectedWindow.getSelectedAttributes()){
			allAtts.add(a.getAttribute());
			selAtts.add(a.getAttribute());
		}
	}
	
	public void selectedFieldsChange(){
		if(selectedWindow == null)
			return;
		
		selectedWindow.setSelectedAttributes(new ArrayList<CAttribute>());
		for(String al : selAtts)
			for(CAttribute a : selectedWindow.getAttributes()){
				if(a.getAttribute().equals(al)){
					selectedWindow.getSelectedAttributes().add(a);
					break;
				}
			}
	}
	
	public void saveRole(){
		
		List<CWindow> selwins = new ArrayList<CWindow>();
		for(Integer I : chosenWindows){
			for(CWindow w : allWindows)
				if(w.getId() == I.intValue()){
					selwins.add(w);
					break;
				}
		}
		
		for(CWindow w : selwins){
			boolean flag = false;
			for(UIWindowACL wacl : windows){
				if(w.getId() == wacl.getId()){
					flag = true;
					break;
				}
			}
			if(flag)
				continue;
			windows.add(new UIWindowACL(w));
		}
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		
		
		CoreProfil prf = new CoreProfil();
		for(CoreProfil p : profils){
			if(p.getId() == selectedProfilId){
				prf = p;
				break;
			}
		}
		
		
		HabilitationsService hsrv = new HabilitationsService();
		int rid;
		if(!updateRole)
			rid = hsrv.persistRole(role, prf, windows, logo, cache.getAppKey());
		else
			rid = hsrv.updateRole(role, prf, windows, logo, cache.getAppKey());
		
		hsrv.saveActionsACL(rid,actions,selectedTraitements);
		hsrv.saveDocumentsACL(rid,actions,selectedDocuments);
		hsrv.saveProceduresACL(rid,actions,selectedProcedures);
		hsrv.saveWorkflowsACL(rid,actions,selectedWorkflows);
	}
	
	public void saveNewProfil(){
		HabilitationsService hsrv = new HabilitationsService();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		
		if(!updateProfil){
			hsrv.persistProfil(profilToCreate, cache.getAppKey());
			profils.add(profilToCreate);
		}
		else
			hsrv.updateProfil(profilToCreate, cache.getAppKey());
		
		
		profilToCreate = new CoreProfil();
	}
	
	public void doDeleteProfil(){
		HabilitationsService hsrv = new HabilitationsService();
		boolean deleted = hsrv.deleteProfil(idProfil);
		if(!deleted){
			FacesContext.getCurrentInstance().addMessage
			(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Impossible de suprimmer ce profil", 
					"Vérifiez s'il existe toujours des rôles attachés à ce profil"));
			return;
		} else {
			FacesContext.getCurrentInstance().addMessage
			(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Profil supprimé avec succès", 
					""));
		}
		
		CoreProfil cp = new CoreProfil();
		for(CoreProfil p : profils)
			if(p.getId() == idProfil){
				cp = p;
				break;
			}
		profils.remove(cp);
	}
	
	public void deleteProfil(){
		
		try{
			idProfil = Integer.parseInt(
					FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("PROFILID")
					);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
	
	public void deleteRole(){
		int idRole = 0;
		try{
			idRole = Integer.parseInt(
					FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("ROLEID")
					);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		HabilitationsService hsrv = new HabilitationsService();
		boolean deleted = hsrv.deleteRole(idRole);
		if(!deleted){
			FacesContext.getCurrentInstance().addMessage
			(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Impossible de suprimmer ce rôle", 
					"Vérifiez s'il existe toujours des utilisateurs attachés à ce rôle"));
			return;
		} else {
			FacesContext.getCurrentInstance().addMessage
			(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Rôle supprimé avec succès", 
					""));
		}
		
		
		CoreRole role = new CoreRole();
		for(CoreRole r : allRoles)
			if(r.getId() == idRole){
				role = r;
				break;
			}
		allRoles.remove(role);
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public CoreRole getRole() {
		return role;
	}

	public void setRole(CoreRole role) {
		this.role = role;
	}

	public int getSelectedProfilId() {
		return selectedProfilId;
	}

	public void setSelectedProfilId(int selectedProfilId) {
		this.selectedProfilId = selectedProfilId;
	}

	public List<CoreProfil> getProfils() {
		return profils;
	}

	public void setProfils(List<CoreProfil> profils) {
		this.profils = profils;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public UploadedFile getLogo() {
		return logo;
	}

	public void setLogo(UploadedFile logo) {
		this.logo = logo;
	}

	public List<Integer> getChosenWindows() {
		return chosenWindows;
	}

	public void setChosenWindows(List<Integer> chosenWindows) {
		this.chosenWindows = chosenWindows;
	}

	public List<CWindow> getAllWindows() {
		return allWindows;
	}

	public void setAllWindows(List<CWindow> allWindows) {
		this.allWindows = allWindows;
	}

	public UIWindowACL getSelectedWindow() {
		return selectedWindow;
	}

	public void setSelectedWindow(UIWindowACL selectedWindow) {
		this.selectedWindow = selectedWindow;
	}

	public boolean isModification() {
		return modification;
	}

	public void setModification(boolean modification) {
		this.modification = modification;
	}

	public boolean isSuppression() {
		return suppression;
	}

	public void setSuppression(boolean suppression) {
		this.suppression = suppression;
	}

	public List<Integer> getChosenFields() {
		return chosenFields;
	}

	public void setChosenFields(List<Integer> chosenFields) {
		this.chosenFields = chosenFields;
	}

	public List<CAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<CAttribute> attributes) {
		this.attributes = attributes;
	}
	
	public List<UIWindowACL> getWindows() {
		return windows;
	}

	public void setWindows(List<UIWindowACL> windows) {
		this.windows = windows;
	}

	public List<String> getAllAtts() {
		return allAtts;
	}

	public void setAllAtts(List<String> allAtts) {
		this.allAtts = allAtts;
	}

	public List<String> getSelAtts() {
		return selAtts;
	}

	public void setSelAtts(List<String> selAtts) {
		this.selAtts = selAtts;
	}

	public List<CoreProfil> getTableProfils() {
		return tableProfils;
	}

	public void setTableProfils(List<CoreProfil> tableProfils) {
		this.tableProfils = tableProfils;
	}

	public CoreProfil getProfilSelectionne() {
		return profilSelectionne;
	}

	public void setProfilSelectionne(CoreProfil profilSelectionne) {
		this.profilSelectionne = profilSelectionne;
	}

	public List<CoreRole> getAllRoles() {
		return allRoles;
	}

	public void setAllRoles(List<CoreRole> allRoles) {
		this.allRoles = allRoles;
	}

	public List<CoreRole> getFilteredRoles() {
		return filteredRoles;
	}

	public void setFilteredRoles(List<CoreRole> filteredRoles) {
		this.filteredRoles = filteredRoles;
	}

	public CoreProfil getProfilToCreate() {
		return profilToCreate;
	}

	public void setProfilToCreate(CoreProfil profilToCreate) {
		this.profilToCreate = profilToCreate;
	}

	public List<PairKVElement> getActions() {
		return actions;
	}

	public void setActions(List<PairKVElement> actions) {
		this.actions = actions;
	}

	public List<PairKVElement> getDocument() {
		return document;
	}

	public void setDocument(List<PairKVElement> document) {
		this.document = document;
	}

	public List<PairKVElement> getProcedures() {
		return procedures;
	}

	public void setProcedures(List<PairKVElement> procedures) {
		this.procedures = procedures;
	}

	public List<PairKVElement> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<PairKVElement> workflows) {
		this.workflows = workflows;
	}

	public List<String> getAllTraitements() {
		return allTraitements;
	}

	public void setAllTraitements(List<String> allTraitements) {
		this.allTraitements = allTraitements;
	}

	public List<String> getAllDocuments() {
		return allDocuments;
	}

	public void setAllDocuments(List<String> allDocuments) {
		this.allDocuments = allDocuments;
	}

	public List<String> getAllProcedures() {
		return allProcedures;
	}

	public void setAllProcedures(List<String> allProcedures) {
		this.allProcedures = allProcedures;
	}

	public List<String> getAllWorkflows() {
		return allWorkflows;
	}

	public void setAllWorkflows(List<String> allWorkflows) {
		this.allWorkflows = allWorkflows;
	}

	public List<String> getSelectedTraitements() {
		return selectedTraitements;
	}

	public void setSelectedTraitements(List<String> selectedTraitements) {
		this.selectedTraitements = selectedTraitements;
	}

	public List<String> getSelectedDocuments() {
		return selectedDocuments;
	}

	public void setSelectedDocuments(List<String> selectedDocuments) {
		this.selectedDocuments = selectedDocuments;
	}

	public List<String> getSelectedProcedures() {
		return selectedProcedures;
	}

	public void setSelectedProcedures(List<String> selectedProcedures) {
		this.selectedProcedures = selectedProcedures;
	}

	public List<String> getSelectedWorkflows() {
		return selectedWorkflows;
	}

	public void setSelectedWorkflows(List<String> selectedWorkflows) {
		this.selectedWorkflows = selectedWorkflows;
	}

	public boolean isUpdateRole() {
		return updateRole;
	}

	public void setUpdateRole(boolean updateRole) {
		this.updateRole = updateRole;
	}

	public boolean isUpdateProfil() {
		return updateProfil;
	}

	public void setUpdateProfil(boolean updateProfil) {
		this.updateProfil = updateProfil;
	}

	public int getIdProfil() {
		return idProfil;
	}

	public void setIdProfil(int idProfil) {
		this.idProfil = idProfil;
	}

	public List<WindowModule> getAllModules() {
		return allModules;
	}

	public void setAllModules(List<WindowModule> allModules) {
		this.allModules = allModules;
	}

	public List<Integer> getChosenModules() {
		return chosenModules;
	}

	public void setChosenModules(List<Integer> chosenModules) {
		this.chosenModules = chosenModules;
	}
	

}
