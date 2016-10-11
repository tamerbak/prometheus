package fr.protogen.masterdata.services;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.primefaces.model.UploadedFile;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.control.ui.UIWindowACL;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CActionbutton;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CDocumentbutton;
import fr.protogen.masterdata.model.CoreDataConstraint;
import fr.protogen.masterdata.model.CoreProfil;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.WorkflowDefinition;

public class HabilitationsService {

	public boolean checkForUserBinding(String dataReference, ApplicationCache cache){
		boolean flag = false;
		
		CBusinessClass entity = new CBusinessClass();
		ProtogenDataEngine pde = new ProtogenDataEngine();
		entity = pde.getReferencedTable(dataReference);
		
		String sql = "select user_bound from core_role where user_bound=? and appkey=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, entity.getId());
			ps.setString(2, cache.getAppKey());
			ResultSet rs = ps.executeQuery();
			flag = rs.next();
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
	}
	
	public List<CoreProfil> loadRoles(List<CoreRole> roles, CoreUser user){
		List<CoreProfil> profiles = new ArrayList<CoreProfil>();
		
		String ids = "";
		for(CoreRole r : roles)
			ids = ids+","+r.getId();
		ids = "("+ids.substring(1)+")";
		
		String sql = "select id, code, libelle, date_effet, date_fin, id_role from core_profil where id_role in "+ids+" "
				+ " and id in (select profil_id from core_user_profiles where user_id=?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CoreProfil r = new CoreProfil();
				r.setId(rs.getInt(1));
				r.setCode(rs.getString(2));
				r.setLibelle(rs.getString(3));
				r.setDateEffet(rs.getDate(4));
				r.setDateFin(rs.getDate(5));
				
				for(CoreRole p : roles)
					if(p.getId() == rs.getInt(6)){
						r.setRole(p);
						break;
					}
				
				profiles.add(r);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		sql = "select entity, bean_id from core_data_constraint where id_role=?";
		for(CoreProfil p : profiles){
			p.setConstraints(new ArrayList<CoreDataConstraint>());
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, p.getId());
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					CoreDataConstraint cdc = new CoreDataConstraint();
					CBusinessClass e = new CBusinessClass();
					e.setId(rs.getInt(1));
					cdc.setEntity(e);
					cdc.setBeanId(rs.getInt(2));
					p.getConstraints().add(cdc);
				}
				
				rs.close();
				ps.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return profiles;
	}
	
	public List<CoreProfil> loadRoles(List<CoreRole> roles){
		List<CoreProfil> profiles = new ArrayList<CoreProfil>();
		
		String ids = "";
		for(CoreRole r : roles)
			ids = ids+","+r.getId();
		if(ids.length() == 0)
			return profiles;
		
		ids = "("+ids.substring(1)+")";
		
		String sql = "select id, code, libelle, date_effet, date_fin, id_role from core_profil where id_role in "+ids;
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CoreProfil r = new CoreProfil();
				r.setId(rs.getInt(1));
				r.setCode(rs.getString(2));
				r.setLibelle(rs.getString(3));
				r.setDateEffet(rs.getDate(4));
				r.setDateFin(rs.getDate(5));
				
				for(CoreRole p : roles)
					if(p.getId() == rs.getInt(6)){
						r.setRole(p);
						break;
					}
				
				profiles.add(r);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		sql = "select entity, bean_id from core_data_constraint where id_role=?";
		for(CoreProfil p : profiles){
			p.setConstraints(new ArrayList<CoreDataConstraint>());
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, p.getId());
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					CoreDataConstraint cdc = new CoreDataConstraint();
					CBusinessClass e = new CBusinessClass();
					e.setId(rs.getInt(1));
					cdc.setBeanId(rs.getInt(2));
					cdc.setEntity(e);
					p.getConstraints().add(cdc);
				}
				
				rs.close();
				ps.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return profiles;
	}

	public void RAZ(String mainEntity, int dbID) {
		String id_profils = "";
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass en = pde.getReferencedTable(mainEntity);
		String sql = "select id from core_role where user_bound=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, en.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				id_profils = id_profils+","+rs.getInt(1);
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(id_profils.length()==0)
			return;
		
		sql = "select id from core_user where \"idRole\" in ("+id_profils.substring(1)+") and user_bean=?";
		String id_users = "";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, dbID);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				id_users = id_users+","+rs.getInt(1);
			}
			rs.close();
			ps.close();
			if(id_users.length() == 0)
				return;
			sql = "delete from core_user_profiles where user_id in ("+id_users.substring(1)+")";
			ps = cnx.prepareStatement(sql);
			ps.execute();
			
			sql = "delete from core_user_role where user_id in ("+id_users.substring(1)+")";
			ps = cnx.prepareStatement(sql);
			ps.execute();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public void majRoles(List<CoreRole> selectedRoles, CoreUser nucreatingUser) {
		String sql = "insert into core_user_role (id_user, id_role) values (?,?)";
		for(CoreRole r : selectedRoles){
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				
				ps.setInt(1, nucreatingUser.getId());
				ps.setInt(2, r.getId());
				ps.execute();
				ps.close();
				
			}catch(Exception e){	
				e.printStackTrace();
			}

		}
		
	}
	
	public void majRoles(String mainEntity, int dbID, List<CoreRole> selectedRoles, boolean insert, CoreUser us) {
		
		String id_profils = "";
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass en = pde.getReferencedTable(mainEntity);
		String sql = "select id from core_role where user_bound=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, en.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				id_profils = id_profils+","+rs.getInt(1);
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(id_profils.length()==0)
			return;
		
		List<Integer> id_users =new ArrayList<Integer>();
		if(insert){
			UserDAOImpl dao = new UserDAOImpl();
			us.setCoreRole(selectedRoles.get(0));
			id_profils = id_profils+","+selectedRoles.get(0).getId();
			String fullID = dao.insertUser(us, true, en.getId());
			int id=0;
			if(fullID!=null && fullID.length()>0)
				id=Integer.parseInt(fullID.split("--")[1]);
			id_users.add(new Integer(id));
		} 	
		sql = "select id from core_user where \"idRole\" in ("+id_profils.substring(1)+") and user_bean=?";
		
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, dbID);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				Integer newId = new Integer(rs.getInt(1));
				boolean flag = false;
				for(Integer i : id_users)
					if(i.intValue() == newId.intValue()){
						flag=true;
						break;
					}
				if(!flag)
					id_users.add(newId);
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		sql = "insert into core_user_role (id_user, id_role) values (?,?)";
		for(CoreRole r : selectedRoles){
			for(Integer idu : id_users){
				try{
					Connection cnx = ProtogenConnection.getInstance().getConnection();
					PreparedStatement ps = cnx.prepareStatement(sql);
					
					ps.setInt(1, idu.intValue());
					ps.setInt(2, r.getId());
					ps.execute();
					ps.close();
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
	}

	public void majProfils(List<CoreProfil> selectedProfils,
			List<CoreRole> selectedRoles, CoreUser nucreatingUser) {
		// TODO Auto-generated method stub
		
	}
	
	public void majProfils(String mainEntity, int dbID,
			List<CoreProfil> selectedProfils, List<CoreRole> selectedRoles, boolean insert) {
		
		String id_profils = "";
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass en = pde.getReferencedTable(mainEntity);
		String sql = "select id from core_role where user_bound=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, en.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				id_profils = id_profils+","+rs.getInt(1);
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		id_profils = id_profils+","+selectedRoles.get(0).getId();
				
		if(id_profils.length()==0)
			return;
		
		sql = "select id from core_user where \"idRole\" in ("+id_profils.substring(1)+") and user_bean=?";
		List<Integer> id_users =new ArrayList<Integer>();
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, dbID);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				id_users.add(new Integer(rs.getInt(1)));
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		sql = "insert into core_user_profiles (user_id, profil_id) values (?,?)";
		for(CoreProfil r : selectedProfils){
			for(Integer idu : id_users){
				try{
					Connection cnx = ProtogenConnection.getInstance().getConnection();
					PreparedStatement ps = cnx.prepareStatement(sql);
					
					ps.setInt(1, idu.intValue());
					ps.setInt(2, r.getId());
					ps.execute();
					ps.close();
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public CoreUser loadBoundUser(String mainEntity, ApplicationCache cache,
			int dbID) {
		CoreUser user = new CoreUser();
		
		String id_profils = "";
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass en = pde.getReferencedTable(mainEntity);
		String sql = "select id from core_role where user_bound=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, en.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				id_profils = id_profils+","+rs.getInt(1);
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		sql = "select * from core_user where binding_entity=? and user_bean=?";
		
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, en.getId());
			ps.setInt(2, dbID);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				user.setLogin(rs.getString("login"));
				user.setId(rs.getInt("id"));
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return user;
	}

	public List<Integer> loadUsersRoles(int id) {
		List<Integer> results = new ArrayList<Integer>();
		
		String sql = "select id_role from core_user_role where id_user=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				results.add(new Integer(rs.getInt(1)));
			rs.close();
			ps.close();
				
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return results;
	}

	public List<Integer> loadUsersProfiles(int id) {
		List<Integer> results = new ArrayList<Integer>();
		
		String sql = "select profil_id from core_user_profiles where user_id=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				results.add(new Integer(rs.getInt(1)));
			rs.close();
			ps.close();
				
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return results;
	}

	public List<CoreProfil> loadProfils(String appKey) {
		List<CoreProfil> results = new ArrayList<CoreProfil>();
		String sql = "select id, libelle, description from core_type_role where appkey=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CoreProfil p = new CoreProfil();
				p.setId(rs.getInt(1));
				p.setLibelle(rs.getString(2));
				p.setDescription(rs.getString(3));
				results.add(p);
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}

	public int updateRole(CoreRole role, CoreProfil prf,
			List<UIWindowACL> windows, UploadedFile lgo, String appkey) {
		int dbID=0;
		try{
			InputStream logo = lgo.getInputstream();
			if(logo != null){
				role.setLogo(logo);
				role.setLogoResKey(UUID.randomUUID().toString());
				role.setFileName(lgo.getFileName());
			}
		}catch(Exception exc){
			System.out.println("LOGO PERSISTENCE ISSUE\n");
			
		}
		
		try{
			if(role.getLogo() != null){
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				byte[] lgbytes = IOUtils.toByteArray(role.getLogo());
				role.getLogo().close();
				role.getLogo();
				String rsql = "insert into s_resource_table (key,filename, content) values (?,?,?)";
				PreparedStatement rps = cnx.prepareStatement(rsql);
				rps.setString(1, role.getLogoResKey());
				rps.setString(2, role.getFileName());
				rps.setBytes(3, lgbytes);
				rps.execute();
				rps.close();
			}
		}catch(Exception e){
			System.out.println("RESSOURCES");
			e.printStackTrace();
		}
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "update core_role set role=?, logo=?, description=?, profil=? where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, role.getRole());
			ps.setString(2, role.getLogoResKey());
			ps.setString(3, role.getDescription());
			ps.setInt(4, prf!=null?prf.getId():0);
			ps.setInt(5, role.getId());
			ps.execute();
			ps.close();
		
			int idRole = role.getId();
			sql = "delete from core_acl_action where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
			
			sql = "delete from core_acl_document where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
			
			sql = "delete from core_acl_procedure where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
			
			sql = "delete from core_acl_workflow where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
			
			List<String> acls = new ArrayList<String>();
			sql = "select id from core_acl_screen where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				acls.add(rs.getInt(1)+"");
			rs.close();
			ps.close();
			
			String sacls = "";
			for(String s : acls)
				sacls = sacls+","+s;
			if(sacls.length()>0){
				sacls = "("+sacls.substring(1)+")";
				sql = "delete from core_acl_screen_attribute where acl in "+sacls;
				ps = cnx.prepareStatement(sql);
				ps.execute();
				ps.close();
				
				sql = "delete from core_acl_screen where role_id=?";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, idRole);
				ps.execute();
				ps.close();
			}
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(UIWindowACL wa : windows){
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				String sql = "insert into core_acl_screen (window_id, modification, suppression, role_id) "
								+ " values (?,?,?,?)";
				//core_acl_screen_id_seq
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, wa.getId());
				ps.setString(2, wa.isModification()?"Y":"N");
				ps.setString(3, wa.isSuppression()?"Y":"N");
				ps.setInt(4, role.getId());
				ps.execute();
				ps.close();
				sql = "select nextval('core_acl_screen_id_seq')";
				ps = cnx.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				int acl=0;
				if(rs.next())
					acl = rs.getInt(1)-1;
				rs.close();
				ps.close();
				for(CAttribute a : wa.getSelectedAttributes()){
					sql = "insert into core_acl_screen_attribute (acl, attribute_id) "
							+ " values (?,?)";
					ps = cnx.prepareStatement(sql);
					ps.setInt(1, acl);
					ps.setInt(2, a.getId());
					ps.execute();
					ps.close();
				}
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
		return role.getId();
	}
	
	public int persistRole(CoreRole role, CoreProfil prf,
			List<UIWindowACL> windows, UploadedFile lgo, String appkey) {
		int dbID=0;
		try{
			InputStream logo = lgo.getInputstream();
			if(logo != null){
				role.setLogo(logo);
				role.setLogoResKey(UUID.randomUUID().toString());
				role.setFileName(lgo.getFileName());
			}
		}catch(Exception exc){
			System.out.println("LOGO PERSISTENCE ISSUE\n");
			
		}
		
		try{
			if(role.getLogo() != null){
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				byte[] lgbytes = IOUtils.toByteArray(role.getLogo());
				role.getLogo().close();
				role.getLogo();
				String rsql = "insert into s_resource_table (key,filename, content) values (?,?,?)";
				PreparedStatement rps = cnx.prepareStatement(rsql);
				rps.setString(1, role.getLogoResKey());
				rps.setString(2, role.getFileName());
				rps.setBytes(3, lgbytes);
				rps.execute();
				rps.close();
			}
		}catch(Exception e){
			System.out.println("RESSOURCES");
			e.printStackTrace();
		}
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "insert into core_role (role,screens,actions,documents,appkey, options,user_bound,superadmin,form_mode,logo, description, profil) values (?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, role.getRole());
			ps.setString(2, "");
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, appkey);
			ps.setString(6, "");
			ps.setInt(7, 0);
			ps.setString(8, "N");
			ps.setString(9, "L");
			ps.setString(10, role.getLogoResKey());
			ps.setString(11, role.getDescription());
			ps.setInt(12, prf!=null?prf.getId():0);
			ps.execute();
			ps.close();
			
			sql = "select nextval('core_role_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				role.setId(rs.getInt(1)-1);
				dbID = role.getId();
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(UIWindowACL wa : windows){
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				String sql = "insert into core_acl_screen (window_id, modification, suppression, role_id) "
								+ " values (?,?,?,?)";
				//core_acl_screen_id_seq
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, wa.getId());
				ps.setString(2, wa.isModification()?"Y":"N");
				ps.setString(3, wa.isSuppression()?"Y":"N");
				ps.setInt(4, role.getId());
				ps.execute();
				ps.close();
				sql = "select nextval('core_acl_screen_id_seq')";
				ps = cnx.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				int acl=0;
				if(rs.next())
					acl = rs.getInt(1)-1;
				rs.close();
				ps.close();
				for(CAttribute a : wa.getSelectedAttributes()){
					sql = "insert into core_acl_screen_attribute (acl, attribute_id) "
							+ " values (?,?)";
					ps = cnx.prepareStatement(sql);
					ps.setInt(1, acl);
					ps.setInt(2, a.getId());
					ps.execute();
					ps.close();
				}
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		return dbID;
	}

	public List<CoreRole> loadRolesByProfil(List<CoreProfil> tprofils) {
		List<CoreRole> results = new ArrayList<CoreRole>();
		String profils = "";
		for(CoreProfil p : tprofils){
			profils = profils+","+p.getId();
		}
		if(profils.length()==0)
			return results;
		
		profils = profils.substring(1);
		
		String sql = "select r.id, r.role, r.profil from core_role r where profil in ("+profils+")";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CoreRole r = new CoreRole();
				r.setId(rs.getInt(1));
				r.setRole(rs.getString(2));
				CoreProfil cp = new CoreProfil();
				for(CoreProfil p : tprofils)
					if(p.getId() == rs.getInt(3)){
						cp = p;
						break;
					}
				r.setProfil(cp);
				results.add(r);
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}

	public void updateProfil(CoreProfil p, String appkey) {
		String sql = "update core_type_role set libelle=?, description=? where id=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, p.getLibelle());
			ps.setString(2, p.getDescription());
			ps.setInt(3, p.getId());
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public void persistProfil(CoreProfil p, String appkey) {
		String sql = "insert into core_type_role (libelle, appkey, description) values (?,?,?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, p.getLibelle());
			ps.setString(2, appkey);
			ps.setString(3, p.getDescription());
			ps.execute();
			ps.close();
			
			sql = "select nextval('core_type_role_id_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				p.setId(rs.getInt(1)-1);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public List<PairKVElement> listActions(String appkey){
		List<PairKVElement> results = new ArrayList<PairKVElement>();
		String sql = "select b.id, b.title from c_actionbutton b, c_window w where w.id = b.id_window and w.appkey=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appkey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				results.add(new PairKVElement(rs.getInt(1)+"",rs.getString(2)));
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}
	
	public List<PairKVElement> listDocuments(String appkey){
		List<PairKVElement> results = new ArrayList<PairKVElement>();
		String sql = "select b.id, b.title from c_documentbutton b, c_window w where w.id = b.id_window and w.appkey=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appkey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				results.add(new PairKVElement(rs.getInt(1)+"",rs.getString(2)));
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}
	
	public List<PairKVElement> listProcedures(String appkey){
		List<PairKVElement> results = new ArrayList<PairKVElement>();
		String sql = "select b.id, b.title from s_procedure b where b.app_key=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appkey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				results.add(new PairKVElement(rs.getInt(1)+"",rs.getString(2)));
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}
	
	public List<PairKVElement> listWorkflows(String appkey){
		List<PairKVElement> results = new ArrayList<PairKVElement>();
		String sql = "select b.id, b.title from s_wf_definition b where b.appkey=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appkey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				results.add(new PairKVElement(rs.getInt(1)+"",rs.getString(2)));
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}

	public void saveActionsACL(int rid, List<PairKVElement> actions,
			List<String> selectedTraitements) {
		String sql = "insert into core_acl_action (role_id, action_id) values (?,?)";
		for(String tr : selectedTraitements){
			int aid = Integer.parseInt(tr);
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, rid);
				ps.setInt(2, aid);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
		
	}

	public void saveDocumentsACL(int rid, List<PairKVElement> actions,
			List<String> selectedDocuments) {
		String sql = "insert into core_acl_document (role_id, document_id) values (?,?)";
		for(String tr : selectedDocuments){
			int aid = Integer.parseInt(tr);
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, rid);
				ps.setInt(2, aid);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
		
	}

	public void saveProceduresACL(int rid, List<PairKVElement> actions,
			List<String> selectedProcedures) {
		String sql = "insert into core_acl_procedure (role_id, procedure_id) values (?,?)";
		for(String tr : selectedProcedures){
			int aid = Integer.parseInt(tr);
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, rid);
				ps.setInt(2, aid);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
		
	}

	public void saveWorkflowsACL(int rid, List<PairKVElement> actions,
			List<String> selectedWorkflows) {
		String sql = "insert into core_acl_workflow (role_id, workflow_id) values (?,?)";
		for(String tr : selectedWorkflows){
			int aid = Integer.parseInt(tr);
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, rid);
				ps.setInt(2, aid);
				ps.execute();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
		
	}

	public boolean deleteProfil(int idProfil) {
		String sql = "select id from core_role where profil=?";
		boolean locked = false;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, idProfil);
			ResultSet rs = ps.executeQuery();
			locked = rs.next();
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		if(locked)
			return false;
		
		try{
			sql = "delete from core_profil where id=?";
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, idProfil);
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
			return false;
		}
		
		return true;
	}

	public boolean deleteRole(int idRole) {
		
		String sql = "select id from core_user where \"idRole\"=?";
		boolean locked = false;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ResultSet rs = ps.executeQuery();
			locked = rs.next();
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		if(locked)
			return false;
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			
			sql = "delete from core_acl_action where role_id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
			
			sql = "delete from core_acl_document where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
			
			sql = "delete from core_acl_procedure where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
			
			sql = "delete from core_acl_workflow where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
			
			List<String> acls = new ArrayList<String>();
			sql = "select id from core_acl_screen where role_id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				acls.add(rs.getInt(1)+"");
			rs.close();
			ps.close();
			
			String sacls = "";
			for(String s : acls)
				sacls = sacls+","+s;
			if(sacls.length()>0){
				sacls = "("+sacls.substring(1)+")";
				sql = "delete from core_acl_screen_attribute where acl in "+sacls;
				ps = cnx.prepareStatement(sql);
				ps.execute();
				ps.close();
				
				sql = "delete from core_acl_screen where role_id=?";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, idRole);
				ps.execute();
				ps.close();
			}
			
			sql = "delete from core_role where id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idRole);
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
			return false;
		}
		
		return true;
	}

	public boolean deleteUser(int idUser) {
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			String sql = "delete from s_wf_execution where \"user\"=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, idUser);
			ps.execute();
			ps.close();
			
			sql = "delete from core_user where id=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, idUser);
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
			return false;
		}
		
		return true;
	}

	public List<CActionbutton> filtrerActions(ApplicationCache cache,
			List<CActionbutton> buttons) {
		
		if(cache.isSuperAdmin())
			return buttons;
		
		String sroles = "";
		List<Integer> wheres = new ArrayList<Integer>();
		for(CoreRole r : cache.getRoles())
			sroles = sroles+","+r.getId();
		if(sroles.length()>0){
			sroles = "("+sroles.substring(1)+")";
			String sql = "select action_id from core_acl_action where role_id in "+sroles;
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while(rs.next())
					wheres.add(new Integer(rs.getInt(1)));
				rs.close();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
		
		List<CActionbutton> toremove = new ArrayList<CActionbutton>();
		
		for(CActionbutton b : buttons){
			boolean flag = false;
			for(Integer I : wheres){
				if(I.intValue() == b.getId()){
					flag = true;
					break;
				}
			}
			if(!flag)
				toremove.add(b);
		}
		
		buttons.removeAll(toremove);
		
		return buttons;
	}
	
	public List<CDocumentbutton> filtrerDocuments(ApplicationCache cache,
			List<CDocumentbutton> buttons) {
		
		if(cache.isSuperAdmin())
			return buttons;
		
		String sroles = "";
		List<Integer> wheres = new ArrayList<Integer>();
		for(CoreRole r : cache.getRoles())
			sroles = sroles+","+r.getId();
		if(sroles.length()>0){
			sroles = "("+sroles.substring(1)+")";
			String sql = "select document_id from core_acl_document where role_id in "+sroles;
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while(rs.next())
					wheres.add(new Integer(rs.getInt(1)));
				rs.close();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
		
		List<CDocumentbutton> toremove = new ArrayList<CDocumentbutton>();
		
		for(CDocumentbutton b : buttons){
			boolean flag = false;
			for(Integer I : wheres){
				if(I.intValue() == b.getId()){
					flag = true;
					break;
				}
			}
			if(!flag)
				toremove.add(b);
		}
		
		buttons.removeAll(toremove);
		
		return buttons;
	}

	public List<WorkflowDefinition> filtrerWorkflows(ApplicationCache cache,
			List<WorkflowDefinition> buttons) {
		if(cache.isSuperAdmin())
			return buttons;
		
		String sroles = "";
		List<Integer> wheres = new ArrayList<Integer>();
		for(CoreRole r : cache.getRoles())
			sroles = sroles+","+r.getId();
		if(sroles.length()>0){
			sroles = "("+sroles.substring(1)+")";
			String sql = "select workflow_id from core_acl_workflow where role_id in "+sroles;
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while(rs.next())
					wheres.add(new Integer(rs.getInt(1)));
				rs.close();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
		
		List<WorkflowDefinition> toremove = new ArrayList<WorkflowDefinition>();
		
		for(WorkflowDefinition b : buttons){
			boolean flag = false;
			for(Integer I : wheres){
				if(I.intValue() == b.getId()){
					flag = true;
					break;
				}
			}
			if(!flag)
				toremove.add(b);
		}
		
		buttons.removeAll(toremove);
		
		return buttons;
	}

	public boolean checkWindow(CoreUser u, int winId){
		boolean flag = false;
		
		String[] ssc = u.getCoreRole().getsWindows().split(";");
		for(String s : ssc){
			if(s!=null && s.length()>0){
				int id = Integer.parseInt(s);
				if(winId == id)
					return true;
			}
		}
		
		//	Didn't find it yet
		String sql = "select * from core_acl_screen where role_id=? and window_id=?";
		try {
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, u.getCoreRole().getId());
			ps.setInt(2, winId);
			ResultSet rs = ps.executeQuery();
			flag = rs.next();
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		if(flag)
			return true;
		
		//	Still nothing
		List<Integer> allRoles = new ArrayList<Integer>();
		sql = "select id_role from core_user_role where id_user=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, u.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				allRoles.add(rs.getInt(1));
			
			sql = "select * from core_acl_screen where role_id=? and window_id=?";
			for(Integer i : allRoles){
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, i.intValue());
				ps.setInt(2, winId);
				rs = ps.executeQuery();
				flag = rs.next();
				rs.close();
				ps.close();
				if(flag)
					break;
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		
		return flag;
	}

	
}
