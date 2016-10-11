package fr.protogen.masterdata.model;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * THIS IS PROFILE
 */
public class CoreRole implements java.io.Serializable {

	private static final long serialVersionUID = 1373600538444111111L;
	private int id;
	private String role;
	private String sWindows;
	private List<CWindow> windows; 
	private String sActions;
	private List<CActionbutton> actions;
	private String sDocuments;
	private List<CDocumentbutton> documents; 
	private String sProcesses;
	private List<SProcess> processes;
	private String soptions;
	private List<SMenuitem> options = new ArrayList<SMenuitem>();
	private boolean superadmin;
	private int boundEntity;
	//	Logo
	private String logoResKey = "";
	private InputStream logo = null;
	private String fileName = "";
	
	//	Role description
	private String description;
	
	//	Role Access Constraints
	private List<CoreDataAccessRight> constraints = new ArrayList<CoreDataAccessRight>();
	
	private CoreProfil profil;
	
	public CoreRole() {
	}

	public CoreRole(int id, String role) {
		this.id = id;
		this.role = role;
	}

	public CoreRole(int id, String role, Set<CoreUser> coreUsers,
			Set<CoreAclElement> coreAclElements) {
		this.id = id;
		this.role = role;
		
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getsWindows() {
		return sWindows;
	}

	public void setsWindows(String sWindows) {
		this.sWindows = sWindows;
	}

	public List<CWindow> getWindows() {
		return windows;
	}

	public void setWindows(List<CWindow> windows) {
		this.windows = windows;
	}

	public String getsActions() {
		return sActions;
	}

	public void setsActions(String sActions) {
		this.sActions = sActions;
	}

	public List<CActionbutton> getActions() {
		return actions;
	}

	public void setActions(List<CActionbutton> actions) {
		this.actions = actions;
	}

	public String getsDocuments() {
		return sDocuments;
	}

	public void setsDocuments(String sDocuments) {
		this.sDocuments = sDocuments;
	}

	public List<CDocumentbutton> getDocuments() {
		return documents;
	}

	public void setDocuments(List<CDocumentbutton> documents) {
		this.documents = documents;
	}

	public String getsProcesses() {
		return sProcesses;
	}

	public void setsProcesses(String sProcesses) {
		this.sProcesses = sProcesses;
	}

	public List<SProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<SProcess> processes) {
		this.processes = processes;
	}

	public List<SMenuitem> getOptions() {
		return options;
	}

	public void setOptions(List<SMenuitem> options) {
		this.options = options;
	}

	public String getSoptions() {
		return soptions;
	}

	public void setSoptions(String soptions) {
		this.soptions = soptions;
	}

	public boolean isSuperadmin() {
		return superadmin;
	}

	public void setSuperadmin(boolean superadmin) {
		this.superadmin = superadmin;
	}

	public int getBoundEntity() {
		return boundEntity;
	}

	public void setBoundEntity(int boundEntity) {
		this.boundEntity = boundEntity;
	}

	public String getLogoResKey() {
		return logoResKey;
	}

	public void setLogoResKey(String logoResKey) {
		this.logoResKey = logoResKey;
	}

	public InputStream getLogo() {
		return logo;
	}

	public void setLogo(InputStream logo) {
		this.logo = logo;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<CoreDataAccessRight> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<CoreDataAccessRight> constraints) {
		this.constraints = constraints;
	}

	public CoreProfil getProfil() {
		return profil;
	}

	public void setProfil(CoreProfil profil) {
		this.profil = profil;
	}

	

	

}
