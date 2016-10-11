package fr.protogen.engine.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.LocalizationEngine;
import fr.protogen.masterdata.DAO.OrganizationDAL;
import fr.protogen.masterdata.DAO.SProcessDataAccess;
import fr.protogen.masterdata.model.CoreDataConstraint;
import fr.protogen.masterdata.model.CoreProfil;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.GParametersInstance;
import fr.protogen.masterdata.model.GParametersPackage;
import fr.protogen.masterdata.model.MailCache;
import fr.protogen.masterdata.model.SMainwindow;
import fr.protogen.masterdata.model.SMenuitem;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SProcess;
import fr.protogen.masterdata.model.SRubrique;
import fr.protogen.masterdata.model.SScreensequence;
import fr.protogen.masterdata.services.HabilitationsService;


public class ApplicationCache {

	private Map<String, SScreensequence> sequences = new HashMap<String, SScreensequence>();
	private List<SProcess> processes = new ArrayList<SProcess>();
	private List<SProcedure> procedures = new ArrayList<SProcedure>();
	private CoreUser user;
	private List<CoreUser> users;
	private List<CoreRole> roles;
	private List<CoreProfil> profils;
	private List<CoreDataConstraint> constraints;
	private String appKey;
	private String moneyCode;
	private SMainwindow window;
	private List<SRubrique> menu;
	private List<SMenuitem> options;
	private GOrganization organization;
	private List<GOrganization> organizations;
	private GOrganization parentOrganization;
	private List<GParametersInstance> parameterPackages;
	private List<GParametersPackage> models;
	private boolean superAdmin = false;
	private MailCache mails=new MailCache();
	
	private Map<String, String> translation = new HashMap<String, String>();
	
	public ApplicationCache(String appKey, CoreUser user){
		this.appKey = appKey;
		this.user = user;
		updateCache();
	}
	
	public void updateCache(){
		ApplicationLoader dao = new ApplicationLoader();
		SProcessDataAccess pdal = new SProcessDataAccess();
		HabilitationsService hsrv = new HabilitationsService();
		dao.updateDataRights(user);
		roles = dao.loadUserRoles(appKey,user);
		profils = hsrv.loadRoles(roles, user);
		constraints = new ArrayList<CoreDataConstraint>();
		for(CoreProfil p : profils){
			constraints.addAll(p.getConstraints());
		}
		menu = dao.loadRolesMenu(appKey,roles);
		options = dao.loadOptions(appKey, user);
		window = dao.loadMainApp(appKey);
		moneyCode = dao.getMoneyCode(appKey);
		List<CoreProfil> listeProfils = hsrv.loadProfils(appKey);
		users = dao.loadApplicationUsers(appKey,listeProfils);
		organization = user.getOriginalOrganization();
		OrganizationDAL odal = new OrganizationDAL();
		organization = odal.populate(organization);
		organization.setParent(odal.loadOrganizationParent(organization.getParent()));
		parentOrganization = organization.getParent();
		organizations = odal.loadChildren(organization);
		parameterPackages = odal.loadParameterModels(organization);
		models = odal.loadModelPackages(organization);
		procedures = pdal.getProcedures(appKey, organization,roles);
		user.setOriginalOrganization(organization);
		
		translateApplication();
		
		/*
		 * Mail cache
		 */
		mails = dao.loadMailCache(user);
		/*
		 * Super admin role
		 */
		if(!user.getCoreRole().isSuperadmin())
			return;
		menu = dao.loadMenu(appKey,user);
		setSuperAdmin(true);
		
		organization = odal.loadAdminOrganization();
		organization = odal.populate(organization);
		organization.setParent(odal.loadOrganizationParent(organization.getParent()));
		parentOrganization = organization.getParent();
		organizations = odal.loadChildren(organization);
		parameterPackages = odal.loadParameterModels(organization);
		models = odal.loadModelPackages(organization);
		user.setOriginalOrganization(organization);
	}
	
	private void translateApplication() {
		
		String code = user.getLanguage();
		LocalizationEngine len = new LocalizationEngine();
		
		//	Translate menu
		for(SRubrique r : menu){
			r.setTitre(len.rubriqueTranslate(r.getTitre(), r.getId(), code));
			for(SMenuitem m : r.getItems()){
				m.setTitle(len.menuTranslate(m.getTitle(), m.getId(), code));
				if(m.isParent()){
					for(SMenuitem i : m.getSubs())
						i.setTitle(len.menuTranslate(i.getTitle(), i.getId(), code));
				}
			}
		}
		
		translation = len.loadApplicationTerminology(user.getAppKey(), user.getLanguage());
		
	}

	public void updateOrganization(GOrganization org) {
		OrganizationDAL odal = new OrganizationDAL();
		organization = odal.populate(org);
		organization.setParent(odal.loadOrganizationParent(organization.getParent()));
		parentOrganization = organization.getParent();
		organizations = odal.loadChildren(organization);
		parameterPackages = odal.loadParameterModels(organization);
		models = odal.loadModelPackages(organization);
		user.setOriginalOrganization(organization);
	}
	
	public List<SRubrique> getMenu() {
		return menu;
	}
	public void setMenu(List<SRubrique> menu) {
		this.menu = menu;
	}
	public SMainwindow getWindow() {
		return window;
	}
	public void setWindow(SMainwindow window) {
		this.window = window;
	}

	public void setSequence(int idMenu, SScreensequence sequence) {
		// TODO Auto-generated method stub
		String key = idMenu+"";
		
		if(sequences.containsKey(key))
			return;
		else
			sequences.put(key, sequence);
		
	}
	
	public SScreensequence getSquence(int idMenu){
		String key = ""+idMenu;
		
		if(sequences.containsKey(key))
			return sequences.get(key);
		
		return null;
	}

	public List<SProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<SProcess> processes) {
		this.processes = processes;
	}

	public void putUser(CoreUser user) {
		// TODO Auto-generated method stub
		if(!users.contains(user))
			users.add(user);
		this.user = user;
	}

	public CoreUser getUser() {
		return user;
	}

	public void setUser(CoreUser user) {
		this.user = user;
	}

	public List<CoreUser> getUsers() {
		return users;
	}

	public void setUsers(List<CoreUser> users) {
		this.users = users;
	}

	public void logout(CoreUser user2) {
		// TODO Auto-generated method stub
		for(CoreUser u : users)
			if(u.getLogin().equals(user2.getLogin())){
				users.remove(u);
				break;
			}
		
		user = null;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public List<SProcedure> getProcedures() {
		return procedures;
	}

	public void setProcedures(List<SProcedure> procedures) {
		this.procedures = procedures;
	}
	public String getMoneyCode() {
		return moneyCode;
	}
	public void setMoneyCode(String moneyCode) {
		this.moneyCode = moneyCode;
	}




	public List<SMenuitem> getOptions() {
		return options;
	}




	public void setOptions(List<SMenuitem> options) {
		this.options = options;
	}




	public Map<String, SScreensequence> getSequences() {
		return sequences;
	}




	public void setSequences(Map<String, SScreensequence> sequences) {
		this.sequences = sequences;
	}




	public GOrganization getOrganization() {
		return organization;
	}




	public void setOrganization(GOrganization organization) {
		this.organization = organization;
	}




	public List<GOrganization> getOrganizations() {
		return organizations;
	}




	public void setOrganizations(List<GOrganization> organizations) {
		this.organizations = organizations;
	}




	public GOrganization getParentOrganization() {
		return parentOrganization;
	}




	public void setParentOrganization(GOrganization parentOrganization) {
		this.parentOrganization = parentOrganization;
	}




	public List<GParametersInstance> getParameterPackages() {
		return parameterPackages;
	}




	public void setParameterPackages(List<GParametersInstance> parameterPackages) {
		this.parameterPackages = parameterPackages;
	}




	public List<GParametersPackage> getModels() {
		return models;
	}




	public void setModels(List<GParametersPackage> models) {
		this.models = models;
	}

	public List<CoreRole> getRoles() {
		return roles;
	}

	public void setRoles(List<CoreRole> roles) {
		this.roles = roles;
	}

	public List<CoreProfil> getProfils() {
		return profils;
	}

	public void setProfils(List<CoreProfil> profils) {
		this.profils = profils;
	}

	public List<CoreDataConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<CoreDataConstraint> constraints) {
		this.constraints = constraints;
	}

	public boolean isSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(boolean superAdmin) {
		this.superAdmin = superAdmin;
	}

	public MailCache getMails() {
		return mails;
	}

	public void setMails(MailCache mails) {
		this.mails = mails;
	}

	public Map<String, String> getTranslation() {
		return translation;
	}

	public void setTranslation(Map<String, String> translation) {
		this.translation = translation;
	}

		
}