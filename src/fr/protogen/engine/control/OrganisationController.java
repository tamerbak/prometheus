package fr.protogen.engine.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.control.ui.OrganisationBean;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.OrganizationDAL;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.GOrganizationRole;
import fr.protogen.masterdata.model.GOrganizationStructure;
import fr.protogen.masterdata.model.GParametersPackage;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class OrganisationController implements Serializable {
	private GOrganizationStructure structure = new GOrganizationStructure();
	private GOrganization organisation = new GOrganization();
	private int roleId;
	private List<GOrganizationRole> roles = new ArrayList<GOrganizationRole>();;
	private int parentId;
	private List<GOrganization> organisations = new ArrayList<GOrganization>();
	private TreeNode organisationsTree;
	private Map<GOrganization, TreeNode> structureMap = new HashMap<GOrganization, TreeNode>();
	private int entityId;
	private List<CBusinessClass> entities=new ArrayList<CBusinessClass>();
	
	/*
	 * GOrganization management
	 */
	private String orgName;
	private String selectedPkgId;
	private List<PairKVElement> pkgs = new ArrayList<PairKVElement>();
	private List<String> selectedInstances;
	private List<PairKVElement> pkinstances = new ArrayList<PairKVElement>();
	private String iselectedPkgId;
	private List<String> selectedRows;
	private List<PairKVElement> pkgRows = new ArrayList<PairKVElement>();;
	private boolean adminMode;
	private int selectedOrgId;
	private List<GOrganization> allOrganizations = new ArrayList<GOrganization>();
	
	private boolean superAdmin;
	private String selectedOrg;
	private List<PairKVElement> organizations = new ArrayList<PairKVElement>();
	private List<COrganization> objOrgs = new ArrayList<COrganization>();
	private String selectedInstance;
	private List<PairKVElement> instances = new ArrayList<PairKVElement>();
	
	@PostConstruct
	public void energize(){
		
		superAdmin = ((Boolean) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("SUPER_ADMIN"))
				.booleanValue();
		
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");

		ApplicationCache cache = ApplicationRepository.getInstance().getCache(skey);
		OrganizationDAL dal = new OrganizationDAL();
		roles = dal.loadOrganisationRoles(cache.getAppKey());
		if(roles.size()>0)
			roleId = roles.get(0).getId();
		ApplicationLoader al = new ApplicationLoader();
		entities = al.loadAllEntities(cache.getAppKey());
		entityId = entities.get(0).getId();
				

		objOrgs = al.loadOrganizations(cache.getAppKey());
		organizations = new ArrayList<PairKVElement>();
		for (COrganization o : objOrgs)
			organizations.add(new PairKVElement(o.getId() + "", o.getLabel()));
		if (objOrgs != null && objOrgs.size() > 0) {
			selectedOrg = objOrgs.get(0).getId() + "";
			instances = objOrgs.get(0).getInstances();
		}
		OrganizationDAL odal = new OrganizationDAL();
		allOrganizations = odal.loadAllOrgs();	
		for(GOrganization o : allOrganizations)
			o = odal.populate(o);
		
		organisation = cache.getOrganization();
		//organisation.setChildren(odal.loadChildren(organisation));
		List<GOrganization> orgsAndSuborgs = new ArrayList<GOrganization>();
		orgsAndSuborgs = odal.loadOrganizations(organisation);
		orgsAndSuborgs.add(organisation);
		organisation.setParent(new GOrganization());
		OrganisationBean rt = new OrganisationBean(new GOrganization());
		organisationsTree = new DefaultTreeNode(rt, null);
		OrganisationBean b = new OrganisationBean(organisation);
		TreeNode tn = new DefaultTreeNode(b, organisationsTree);
		structureMap.put(organisation, tn);
		List<GOrganization> added = new ArrayList<GOrganization>();
		added.add(organisation);
		while(added.size()<orgsAndSuborgs.size())
			for(GOrganization o : orgsAndSuborgs){
				if(structureMap.containsKey(o))
					continue;
				GOrganization parent = o.getParent();
				if(parent == null || parent.getId() == 0)
					continue;
				if(!structureMap.containsKey(parent))
					continue;
				added.add(o);
				TreeNode pnode = structureMap.get(parent);
				OrganisationBean obean = new OrganisationBean(o);
				TreeNode n = new DefaultTreeNode(obean, pnode);
				structureMap.put(o, n);
			}
		organisations.add(organisation);
		organisation = new GOrganization();
		
		// GOrganization
		orgName = cache.getOrganization().getName();
		pkgs = new ArrayList<PairKVElement>();

		for (GParametersPackage p : cache.getModels()) {
			pkgs.add(new PairKVElement(p.getId() + "", p.getNom()));
		}

		if (pkgs.size() > 0) {
			selectedPkgId = pkgs.get(0).getKey();
			int ip = Integer.parseInt(selectedPkgId);
			pkinstances = new ArrayList<PairKVElement>();

			String table = "";
			for (GParametersPackage i : cache.getModels()) {
				if (i.getId() == ip) {
					table = i.getEntity().getDataReference();
					break;
				}

			}

			pkinstances = odal.loadPackageInstances(ip, table);
			ProtogenDataEngine pde = new ProtogenDataEngine();
			pkgRows = pde.getDataKeys(table, false, 0);
		}
	}
	
	public void addOrganisation(){
		
		
		OrganizationDAL odal = new OrganizationDAL();
		CBusinessClass representativeEntity = new CBusinessClass();
		GOrganizationRole role = new GOrganizationRole();
		
		for(GOrganizationRole r : roles)
			if(r.getId() == roleId){
				role = r;
				break;
			}
		organisation.setRole(role);
		
		for(CBusinessClass e : entities)
			if(e.getId() == entityId){
				representativeEntity = e;
				break;
			}
		organisation.setRepresentativeEntity(representativeEntity);
		
		if(parentId > 0){
			for(GOrganization o : organisations)
				if(o.getId() == parentId){
					organisation.setParent(o);
					organisation.setChildren(new ArrayList<GOrganization>());
					o.getChildren().add(organisation);
					organisation = odal.persist(organisation);
					TreeNode tn = structureMap.get(o);
					OrganisationBean b = new OrganisationBean(organisation);
					TreeNode newNode = new DefaultTreeNode(b, tn);
					structureMap.put(organisation, newNode);
					break;
				}
			organisations.add(organisation);
			organisation = new GOrganization();
		} 
	}
	public void saveStructure(){
		
	}

	/*
	 * GOrganization management
	 */
	public void parameterPkgChange() {
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		int ip = Integer.parseInt(selectedPkgId);
		pkinstances = new ArrayList<PairKVElement>();
		OrganizationDAL odal = new OrganizationDAL();

		String table = "";
		for (GParametersPackage i : cache.getModels()) {
			if (i.getId() == ip) {
				table = i.getEntity().getDataReference();
				break;
			}

		}

		pkinstances = odal.loadPackageInstances(ip, table);
	}

	public void iparameterPkgChange() {

		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		int ip = Integer.parseInt(selectedPkgId);
		pkinstances = new ArrayList<PairKVElement>();
		OrganizationDAL odal = new OrganizationDAL();

		String table = "";
		for (GParametersPackage i : cache.getModels()) {
			if (i.getId() == ip) {
				table = i.getEntity().getDataReference();
				break;
			}

		}

		pkinstances = odal.loadPackageInstances(ip, table);
		ProtogenDataEngine pde = new ProtogenDataEngine();
		pkgRows = pde.getDataKeys(table, false, 0);

	}

	public void saveParameters() {
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(
				(String) FacesContext.getCurrentInstance().getExternalContext()
						.getSessionMap().get("USER_KEY"));
		OrganizationDAL odal = new OrganizationDAL();
		List<Integer> ins = new ArrayList<Integer>();
		for (String si : selectedInstances) {
			ins.add(new Integer(si));
		}
		int idorg = cache.getOrganization().getId();

		odal.saveInstanceMapping(idorg, ins);
	}

	public void saveInstanceParameters() {
		List<Integer> ins = new ArrayList<Integer>();

		for (String si : selectedRows)
			ins.add(new Integer(si));

		OrganizationDAL odal = new OrganizationDAL();
		int pkgId = Integer.parseInt(iselectedPkgId);
		odal.addInstances(pkgId, ins);

	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public GOrganizationStructure getStructure() {
		return structure;
	}

	public void setStructure(GOrganizationStructure structure) {
		this.structure = structure;
	}

	public GOrganization getOrganisation() {
		return organisation;
	}

	public void setOrganisation(GOrganization organisation) {
		this.organisation = organisation;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public List<GOrganizationRole> getRoles() {
		return roles;
	}

	public void setRoles(List<GOrganizationRole> roles) {
		this.roles = roles;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public List<GOrganization> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<GOrganization> organisations) {
		this.organisations = organisations;
	}

	public TreeNode getOrganisationsTree() {
		return organisationsTree;
	}

	public void setOrganisationsTree(TreeNode organisationsTree) {
		this.organisationsTree = organisationsTree;
	}

	public Map<GOrganization, TreeNode> getStructureMap() {
		return structureMap;
	}

	public void setStructureMap(Map<GOrganization, TreeNode> structureMap) {
		this.structureMap = structureMap;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public List<CBusinessClass> getEntities() {
		return entities;
	}

	public void setEntities(List<CBusinessClass> entities) {
		this.entities = entities;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getSelectedPkgId() {
		return selectedPkgId;
	}

	public void setSelectedPkgId(String selectedPkgId) {
		this.selectedPkgId = selectedPkgId;
	}

	public List<PairKVElement> getPkgs() {
		return pkgs;
	}

	public void setPkgs(List<PairKVElement> pkgs) {
		this.pkgs = pkgs;
	}

	public List<String> getSelectedInstances() {
		return selectedInstances;
	}

	public void setSelectedInstances(List<String> selectedInstances) {
		this.selectedInstances = selectedInstances;
	}

	public List<PairKVElement> getPkinstances() {
		return pkinstances;
	}

	public void setPkinstances(List<PairKVElement> pkinstances) {
		this.pkinstances = pkinstances;
	}

	public String getIselectedPkgId() {
		return iselectedPkgId;
	}

	public void setIselectedPkgId(String iselectedPkgId) {
		this.iselectedPkgId = iselectedPkgId;
	}

	public List<String> getSelectedRows() {
		return selectedRows;
	}

	public void setSelectedRows(List<String> selectedRows) {
		this.selectedRows = selectedRows;
	}

	public List<PairKVElement> getPkgRows() {
		return pkgRows;
	}

	public void setPkgRows(List<PairKVElement> pkgRows) {
		this.pkgRows = pkgRows;
	}

	public boolean isAdminMode() {
		return adminMode;
	}

	public void setAdminMode(boolean adminMode) {
		this.adminMode = adminMode;
	}

	public int getSelectedOrgId() {
		return selectedOrgId;
	}

	public void setSelectedOrgId(int selectedOrgId) {
		this.selectedOrgId = selectedOrgId;
	}

	public List<GOrganization> getAllOrganizations() {
		return allOrganizations;
	}

	public void setAllOrganizations(List<GOrganization> allOrganizations) {
		this.allOrganizations = allOrganizations;
	}
	public boolean isSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(boolean superAdmin) {
		this.superAdmin = superAdmin;
	}

	public String getSelectedOrg() {
		return selectedOrg;
	}

	public void setSelectedOrg(String selectedOrg) {
		this.selectedOrg = selectedOrg;
	}

	public List<PairKVElement> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<PairKVElement> organizations) {
		this.organizations = organizations;
	}

	public List<COrganization> getObjOrgs() {
		return objOrgs;
	}

	public void setObjOrgs(List<COrganization> objOrgs) {
		this.objOrgs = objOrgs;
	}

	public String getSelectedInstance() {
		return selectedInstance;
	}

	public void setSelectedInstance(String selectedInstance) {
		this.selectedInstance = selectedInstance;
	}

	public List<PairKVElement> getInstances() {
		return instances;
	}

	public void setInstances(List<PairKVElement> instances) {
		this.instances = instances;
	}
}
