package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CLocalizedEntity;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.masterdata.model.GOrganizationRole;
import fr.protogen.masterdata.model.GParameterValues;
import fr.protogen.masterdata.model.GParametersInstance;
import fr.protogen.masterdata.model.GParametersPackage;

public class OrganizationDAL {

	public GOrganization loadUserOrganization(CoreUser user) {
		GOrganization org = new GOrganization();
		org.setRepresentativeEntity(new CBusinessClass());
		String sql = "SELECT g_organization_instance.id AS id_instance, g_organization_instance.bean, g_organization_instance.parent_organization, "
							+ "g_organization.id AS id_org, g_organization.id_parent, g_organization.root_organization, g_organization.representative "
							+ "FROM public.g_organization, public.g_organization_instance "
							+ "WHERE g_organization_instance.organization = g_organization.id AND g_organization_instance.id=?";
		
		Connection cnx=null;
		try {
			Class.forName("org.postgresql.Driver");
			cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getOriginalOrganization().getId());
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				org.setId(rs.getInt(1));
				org.setIdBean(rs.getInt(2));
				org.setParent(new GOrganization());
				if(rs.getString("root_organization").equals("N"))
					org.getParent().setId(rs.getInt("id_parent"));
				else
					org.getParent().setId(0);
				org.setRepresentativeEntity(new CBusinessClass());
				org.getRepresentativeEntity().setId(rs.getInt("representative"));
				org.setOrgId(rs.getInt("id_org"));
			}
			rs.close();
			ps.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return org;
		
	}

	public GOrganization loadAdminOrganization() {
		GOrganization org = new GOrganization();
		
		String sql = "SELECT g_organization_instance.id AS id_instance, g_organization_instance.bean, g_organization_instance.parent_organization, "
							+ "g_organization.id AS id_org, g_organization.id_parent, g_organization.root_organization, g_organization.representative "
							+ "FROM public.g_organization, public.g_organization_instance "
							+ "WHERE g_organization_instance.organization = g_organization.id AND g_organization_instance.parent_organization=0";
		
		Connection cnx=null;
		try {
			Class.forName("org.postgresql.Driver");
			cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				org.setId(rs.getInt(1));
				org.setIdBean(rs.getInt(2));
				org.setParent(new GOrganization());
				if(rs.getString("root_organization").equals("N"))
					org.getParent().setId(rs.getInt("id_parent"));
				else
					org.getParent().setId(0);
				org.setRepresentativeEntity(new CBusinessClass());
				org.getRepresentativeEntity().setId(rs.getInt("representative"));
				org.setOrgId(rs.getInt("id_org"));
			}
			rs.close();
			ps.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return org;
		
	}
	
	public List<GOrganization> loadAllOrgs() {
		List<GOrganization> orgs = new ArrayList<GOrganization>();
		String sql = "SELECT g_organization_instance.id AS id_instance, g_organization_instance.bean, g_organization_instance.parent_organization, "
							+ "g_organization.id AS id_org, g_organization.id_parent, g_organization.root_organization, g_organization.representative "
							+ "FROM public.g_organization, public.g_organization_instance "
							+ "WHERE g_organization_instance.organization = g_organization.id";
		
		Connection cnx=null;
		try {
			Class.forName("org.postgresql.Driver");
			cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				GOrganization org = new GOrganization();
				org.setId(rs.getInt(1));
				org.setIdBean(rs.getInt(2));
				org.setParent(new GOrganization());
				if(rs.getString("root_organization").equals("N"))
					org.getParent().setId(rs.getInt("id_parent"));
				else
					org.getParent().setId(0);
				org.setRepresentativeEntity(new CBusinessClass());
				org.getRepresentativeEntity().setId(rs.getInt("representative"));
				org.setOrgId(rs.getInt("id_org"));
				orgs.add(org);
			}
			rs.close();
			ps.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return orgs;
		
	}
	
	public GOrganization populate(GOrganization organization) {
		
		if(organization == null || organization.getRepresentativeEntity()==null)
			return new GOrganization();
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		ApplicationLoader al = new ApplicationLoader();
		CBusinessClass e = al.getEntityById(organization.getRepresentativeEntity().getId());
		organization.setRepresentativeEntity(e);
		
		PairKVElement elt = pde.getDataKeyByID(e.getDataReference(), organization.getIdBean());
		if(elt.getValue()==null|| elt.getValue().length()==0)
			elt.setValue(organization.getName());
		organization.setName(elt.getValue());
		
		//	Load children
		String sql = "select id, nom, representative, role_id "
					+ "from g_organization where id_parent=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, organization.getId());
			ResultSet rs = ps.executeQuery();
			organization.setChildren(new ArrayList<GOrganization>());
			while(rs.next()){
				GOrganization o = new GOrganization();
				o.setId(rs.getInt(1));
				o.setName(rs.getString(2));
				o.setParent(organization);
				o.setRepresentativeEntity(new CBusinessClass());
				o.getRepresentativeEntity().setId(rs.getInt(3));
				o.setRole(new GOrganizationRole());
				o.getRole().setId(rs.getInt(4));
				o.setChildren(new ArrayList<GOrganization>());
				organization.getChildren().add(o);
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		ApplicationLoader dal = new ApplicationLoader();
		sql = "select libelle from g_org_role where id=?";
		for(GOrganization o : organization.getChildren()){
			CBusinessClass entity = dal.getEntityById(o.getRepresentativeEntity().getId());
			o.setRepresentativeEntity(entity);
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, o.getRole().getId());
				ResultSet rs = ps.executeQuery();
				if(rs.next())
					o.getRole().setLibelle(rs.getString(1));
				rs.close();
				ps.close();
				
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
		
		return organization;
	}

	public GOrganization loadOrganizationParent(GOrganization parent) {
		
		if(parent == null || parent.getRepresentativeEntity()==null)
			return null;
		
		if(parent.getId()==0)
			return null;
		
		String sql = "SELECT g_organization_instance.id AS id_instance, g_organization_instance.bean, g_organization_instance.parent_organization, "
				+ "g_organization.id AS id_org, g_organization.id_parent, g_organization.root_organization, g_organization.representative "
				+ "FROM public.g_organization, public.g_organization_instance "
				+ "WHERE g_organization_instance.organization = g_organization.id AND id_instance=?";
		
		Connection cnx=null;
		try {
			Class.forName("org.postgresql.Driver");
			cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, parent.getId());
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				parent.setId(rs.getInt(1));
				parent.setIdBean(rs.getInt(2));
				parent.setParent(new GOrganization());
				parent.setRepresentativeEntity(new CBusinessClass());
				parent.getRepresentativeEntity().setId(rs.getInt("representative"));
			}
			
			rs.close();
			ps.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		parent = populate(parent);
		
		return parent;
	}

	public List<GOrganization> loadChildren(GOrganization organization) {
		List<GOrganization> orgs = new ArrayList<GOrganization>();
		
		if(organization == null || organization.getRepresentativeEntity()==null)
			return orgs;
		
		String sql = "SELECT g_organization_instance.id AS id_instance, g_organization_instance.bean, g_organization_instance.parent_organization, "
				+ "g_organization.id AS id_org, g_organization.id_parent, g_organization.root_organization, g_organization.representative "
				+ "FROM public.g_organization, public.g_organization_instance "
				+ "WHERE g_organization_instance.organization = g_organization.id AND id_parent=?";
		Connection cnx=null;
		try {
			Class.forName("org.postgresql.Driver");
			cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, organization.getId());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()){
				GOrganization org = new GOrganization();
				org.setId(rs.getInt(1));
				org.setIdBean(rs.getInt(2));
				org.setParent(organization);
				org.setRepresentativeEntity(new CBusinessClass());
				org.getRepresentativeEntity().setId(rs.getInt("representative"));
				orgs.add(org);
			}
			rs.close();
			ps.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(GOrganization o : orgs)
			o=populate(o);
		
		return orgs;
	}

	public List<GParametersInstance> loadParameterModels(
			GOrganization organization) {
		List<GParametersInstance> results = new ArrayList<GParametersInstance>();
		
		if(organization==null || organization.getId()==0)
			return results;
		
		String sql = "select pm_id from g_organization_parameters_map_instance where org_id=?";
		Connection cnx=null;
		try{
			Class.forName("org.postgresql.Driver");
			cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1	, organization.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				GParametersInstance in = new GParametersInstance();
				in.setId(rs.getInt(1));
				results.add(in);
			}
			rs.close();
			ps.close();
			
			for(GParametersInstance i : results){
				sql = "select bean_id, package_id from g_parameters_model_instance where id=?";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, i.getId());
				rs = ps.executeQuery();
				while(rs.next()){
					GParametersPackage pkg = new GParametersPackage();
					i.setBeanId(rs.getInt(1));
					pkg.setId(rs.getInt(2));
					i.setModelPackage(pkg);
				}
				rs.close();
				ps.close();
				
				sql = "select nom, representative from g_parameters_pkg where id=?";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, i.getModelPackage().getId());
				rs = ps.executeQuery();
				ProtogenDataEngine pde = new ProtogenDataEngine();
				while(rs.next()){
					i.getModelPackage().setNom(rs.getString(1));
					CBusinessClass e = new CBusinessClass();
					e.setId(rs.getInt(2));
					e = pde.getEntityById(e.getId());
					i.getModelPackage().setEntity(e);
					
				}
				rs.close();
				ps.close();
			}
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}

	/*
	 * 	Get all entites of configuration linked to a model
	 */
	public List<CBusinessClass> loadModelEntities(GParametersInstance m) {
		List<CBusinessClass>es = new ArrayList<CBusinessClass>();
		int idmodel = m.getModelPackage().getId();
		String sql = "select business_class from g_parameters_entities where parameters_pkg=?";
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, idmodel);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CBusinessClass e = new CBusinessClass();
				e.setId(rs.getInt(1));
				es.add(e);
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return es;
	}

	//	Get all allowed values 
	public List<GParameterValues> loadAllowedValues(GParametersInstance m, CBusinessClass referenceBC) {
		List<GParameterValues> values = new ArrayList<GParameterValues>();
		String sql = "select db_row_id from g_parameters_mapping_values where instance_id=? and entitiy_id=?";
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, m.getId());
			ps.setInt(2, referenceBC.getId());
			ResultSet rs = ps.executeQuery();
			ProtogenDataEngine pde = new ProtogenDataEngine();
			while(rs.next()){
				GParameterValues v = new GParameterValues();
				v.setEntity(m.getModelPackage().getEntity());
				v.setPackagedParameters(m);
				v.setRowDbId(rs.getInt(1));
				v.setKey(pde.getDataKeyByID(referenceBC.getDataReference(), v.getRowDbId()));
				values.add(v);
				
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return values;
	}

	public void updateParametersMap(CBusinessClass e, int dbID,
			int idIn, GParametersPackage pkg) {
		int instance = idIn;
		String sql = "insert into g_parameters_mapping_values (package_id, entitiy_id, instance_id, db_row_id) values(?,?,?,?)";
		
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, pkg.getId());
			ps.setInt(2, e.getId());
			ps.setInt(3, instance);
			ps.setInt(4, dbID);
			
			
			ps.execute();
			ps.close();
					
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public int loadModelInstance(int dbId, int pkgId) {
		int id=0;
		
		String sql = "select id from g_parameters_model_instance where package_id=? and bean_id=?";
		
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, pkgId);
			ps.setInt(2, dbId);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				id = rs.getInt(1);
			
			rs.close();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return id;
	}

	public List<PairKVElement> loadPackageInstances(int ip, String tableName) {
		List<PairKVElement> results = new ArrayList<PairKVElement>();
		String sql = "select id, bean_id from g_parameters_model_instance where package_id=?";
		
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, ip);
			ResultSet rs = ps.executeQuery();
			
			ProtogenDataEngine pde = new ProtogenDataEngine();
			
			while(rs.next()){
				int idi = rs.getInt(1);
				PairKVElement e = pde.getDataKeyByID(tableName, rs.getInt(2));
				results.add(new PairKVElement(""+idi, e.getValue()));
			}
			
			rs.close();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}

	public void saveInstanceMapping(int idorg, List<Integer> ins) {
		
		
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		
			String sql = "delete from g_organization_parameters_map_instance where org_id=?";
			PreparedStatement st = cnx.prepareStatement(sql);
			st.setInt(1, idorg);
			st.execute();
			st.close();
			
			sql = "insert into g_organization_parameters_map_instance (pm_id, org_id) values (?,?)";
			
			for(Integer ii : ins){
				int i = ii.intValue();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, i);
				ps.setInt(2, idorg);
				ps.execute();
				ps.close();
			}
			
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public List<GParametersPackage> loadModelPackages(GOrganization organization) {
		List<GParametersPackage> models = new ArrayList<GParametersPackage>();
		String sql = " select nom, representative, id from g_parameters_pkg, g_org_package_association where id_pkg=id and id_org=?";
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, organization.getOrgId());
			ResultSet rs = ps.executeQuery();
			ProtogenDataEngine pde = new ProtogenDataEngine();
			while (rs.next()){
				GParametersPackage p = new GParametersPackage();
				p.setNom(rs.getString(1));
				CBusinessClass e = new CBusinessClass();
				e.setId(rs.getInt(2));
				e = pde.getEntityById(e.getId());
				p.setEntity(e);
				p.setId(rs.getInt(3));
				models.add(p);
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return models;
	}

	public void addInstances(int pkgId, List<Integer> ins) {
		try{
			Class.forName("org.postgresql.Driver");
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		
			
			for(Integer ii : ins){
				int i = ii.intValue();
				String sql = "delete from g_parameters_model_instance where package_id=? and bean_id=?";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, pkgId);
				ps.setInt(2, i);
				ps.execute();
				ps.close();
				sql = "insert into g_parameters_model_instance (package_id,bean_id) values (?,?)";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, pkgId);
				ps.setInt(2, i);
				ps.execute();
				ps.close();
			}
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}

	public List<GOrganizationRole> loadOrganisationRoles(String appKey) {
		
		List<GOrganizationRole> results = new ArrayList<GOrganizationRole>();
		
		String sql = "select id, libelle from g_org_role where appkey=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				GOrganizationRole r = new GOrganizationRole();
				r.setId(rs.getInt(1));
				r.setLibelle(rs.getString(2));
				results.add(r);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return results;
	}

	public GOrganization persist(GOrganization o) {
		
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");

		ApplicationCache cache = ApplicationRepository.getInstance().getCache(skey);
		String appkey = cache.getAppKey();
		
		String sql= "insert into g_organization (nom, id_parent, root_organization, representative, appkey, role_id) "
						+ " values (?, ?, ?, ?, ?, ?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, o.getName());
			ps.setInt(2, o.getParent().getId());
			ps.setString(3, o.getParent().getId()==0?"Y":"N");
			ps.setInt(4, o.getRepresentativeEntity().getId());
			ps.setString(5, appkey);
			ps.setInt(6, o.getRole().getId());
			
			ps.execute();
			ps.close();
			
			sql = "select nextval('g_organization_seq'::regclass)";
			ps = cnx.prepareStatement(sql);
			o.setId(0);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				o.setId(rs.getInt(1)-1);
			}
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return o;
	}

	/**
	 * Check if this entity is localized
	 * @param organization
	 * @param mainEntity
	 * @return
	 */
	public CLocalizedEntity checkForLocalizationMode(GOrganization organization,
			String mainEntity) {
		CLocalizedEntity localizedEntity = new CLocalizedEntity();
		
		//	Load entity
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass entity = pde.getReferencedTable(mainEntity);
		localizedEntity.setEntity(entity);
		localizedEntity.setOrganization(organization);
		
		String sql = "select id from c_localizable_entity where id_organzation=? and id_entity=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, organization.getId());
			ps.setInt(2, entity.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				localizedEntity.setId(rs.getInt(1));
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return localizedEntity;
	}

	public List<GOrganization> loadOrganizations(GOrganization o) {
		List<GOrganization> results = new ArrayList<GOrganization>();
		
		results.addAll(o.getChildren());
		
		List<GOrganization> file = new ArrayList<GOrganization>();
		for(GOrganization or : o.getChildren()){
			or = populate(or);
			file.add(or);
		}
		
		while(file.size()>0){
			GOrganization or = file.get(0);
			List<GOrganization> children = this.loadChildren(or);
			or.setChildren(children);
			results.addAll(children);
			file.addAll(or.getChildren());
			file.remove(or);
		}
		return results;
	}

	public void persistLocalization(CLocalizedEntity localizedEntity, int dbID, GOrganization org) {
		
		String sql = "insert into c_localized_bean (localization_id, bean_id, org_id) values (?,?,?)";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, localizedEntity.getId());
			ps.setInt(2, dbID);
			ps.setInt(3, org.getId());
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}

	public void updateLocalization(CLocalizedEntity localizedEntity, int dbID,
			GOrganization org) {
		String sql = "update c_localized_bean set org_id=? where localization_id=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, org.getId());
			ps.setInt(2, localizedEntity.getId());
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
	
	public int loadLocalization(int lid, int dbID) {
		String sql = "select org_id from c_localized_bean where localization_id=? and bean_id=?";
		int id=0;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, lid);
			ps.setInt(2, dbID);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return id;
	}

	

}
