package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.ProtogenKeyGenerator;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.COrganization;
import fr.protogen.masterdata.model.CoreDataAccessRight;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GOrganization;
import fr.protogen.security.Md5;

public class UserDAOImpl {

	public CoreUser getUser(String login, String password){
		
		try {
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			String sql = "select r.screens, r.options as roptions, r.superadmin as superadmin, r.user_bound as rolebound, u.*, "
					+ "r.role, r.logo, r.description  "
					+ "from core_user u, core_role r "
					+ "where u.login='"+login+"' and u.password='"+password+"' and u.\"idRole\"=r.id and activated='Y'";
			ResultSet rs = st.executeQuery(sql);
			
			
			CoreUser user=null;
			if(rs.next()){
				user = populate(rs);
				
			} 
			
			rs.close();
			st.close();
			
			if(user==null)
				return user;
			
			/*
			 * Get data restrictions on role
			 */
			CoreRole r = user.getCoreRole();
			
			sql = "select entity, data_value from core_data_access_right where role="+r.getId();
			st = cnx.createStatement();
			rs = st.executeQuery(sql);
			r.setConstraints(new ArrayList<CoreDataAccessRight>());
			while (rs.next()){
				CoreDataAccessRight right = new CoreDataAccessRight();
				right.setEntity(new CBusinessClass());
				right.getEntity().setId(rs.getInt(1));
				right.setValue(rs.getInt(2));
				r.getConstraints().add(right);
			}
			
			rs.close();
			st.close();
			
			return user;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private CoreUser populate(ResultSet rs) {
		// TODO Auto-generated method stub
		
		CoreRole role;
		String logores;
		CoreRole r = null;
		try {
			role = new CoreRole(rs.getInt("idRole"),"");
			CoreUser user = new CoreUser(rs.getInt("id"), role, rs.getString("firstName"), rs.getString("lastName"), rs.getString("tel"), rs.getString("email"), rs.getString("adress"), rs.getString("login"), "");
			r = new CoreRole(rs.getInt("idRole"),"");
			logores = rs.getString("logo");
			r.setLogoResKey(logores);
			
			user.setLanguage(rs.getString("lang"));
			
			user.setPassword(rs.getString("password"));
			r.setsWindows(rs.getString("screens"));
			r.setSoptions(rs.getString("roptions"));
			r.setRole(rs.getString("role"));
			r.setSuperadmin(rs.getString("superadmin").equals("Y"));
			r.setBoundEntity(rs.getInt("rolebound"));
			r.setDescription(rs.getString("description"));
			user.setSoptions(rs.getString("options"));
			user.setCoreRole(r);
			GOrganization org = new GOrganization();
			org.setId(rs.getInt("organization"));	// danger
			user.setOriginalOrganization(org);
			user.setAppKey(rs.getString("appkey"));
			user.setUserTheme(rs.getString("user_theme"));
			user.setThemeColor(rs.getString("theme_color"));
			user.setThemeStyle(rs.getString("theme_style"));
			user.setOrgInstance(rs.getInt("org_instance"));
			user.setPhoto(rs.getString("picto"));
			int idorg = rs.getInt("organization");
			COrganization o = new COrganization();
			o.setId(idorg);
			user.setOrganization(o);
			
			user.setBoundEntity(rs.getInt("user_bean"));
			
			return user;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try{
			ApplicationLoader dal = new ApplicationLoader();
			dal.saveLogo(r);
		}catch(Exception exc)
		{	
			System.out.println("ERROR SAVING LOGO \n\t");
			exc.printStackTrace();
		}
		return null;
		
	} 

	public CoreUser getUserByID(int userID) {
		// TODO Auto-generated method stub
		CoreUser user = new CoreUser();
		
		try {
			 
		    Class.forName("org.postgresql.Driver");

		    
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			String sql = "select r.screens, r.options as roptions, r.superadmin as superadmin, r.user_bound as rolebound, u.*, r.role, r.logo, r.description "
					+ " from core_user u, core_role r "
					+ " where u.id='"+userID+"' and u.\"idRole\"=r.id and activated='Y'";
			ResultSet rs = st.executeQuery(sql);
			if(rs.next())
				user = populate(rs);
			rs.close();
			st.close();
			
			return user;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return user;
	}

	public CoreUser getUserByName(String prenom, String nom) {
		// TODO Auto-generated method stub
		CoreUser user = new CoreUser();
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			String sql = "select * from core_user where \"firstName\"='"+prenom+"' and \"lastName\"='"+nom+"' and appkey='"+
					""+ApplicationRepository.getInstance().getCache(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY").toString()).getAppKey()+"'";
			ResultSet rs = st.executeQuery(sql);
			if(rs.next())
				user = populate(rs);
			rs.close();
			st.close();
			
			return user;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return user;
	}

	public String insertUser(CoreUser u) {
		String uid="";
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    /*
		     * 	u.setThemeColor("css/colors.css?v=1");
			 *	u.setThemeStyle("css/style.css?v=1");
		     */
		    String sql = "insert into core_user (\"firstName\",\"lastName\",tel,email,adress,login,password,\"idRole\",appkey,activation_key,activated,organization,org_instance,user_theme, theme_color, theme_style,user_bean)" +
		    		"		VALUES (?,?,?,?,?,?,?,?,?,?,'N',1,?,'THEME:DEVELOPR','css/colors.css?v=1','css/style.css?v=1',?)";
		    
		    uid = ProtogenKeyGenerator.getInstance().generateKey(); 
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, u.getFirstName());
		    ps.setString(2, u.getLastName());
		    ps.setString(3, u.getTel());
		    ps.setString(4, u.getEmail());
		    ps.setString(5, u.getAdress());
		    ps.setString(6, u.getLogin());
		    ps.setString(7, Md5.encode(u.getPassword()));
		    ps.setInt(8, u.getCoreRole().getId());
		    ps.setString(9, u.getAppKey());
		    ps.setString(10, uid);
		    ps.setInt(11, u.getOrgInstance());
		    ps.setInt(12, u.getBoundEntity());
		    
		    ps.execute();
		    ps.close();
		    
		    sql = "select nextval('core_user_seq')";
		    ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    int id=0;
		    if(rs.next())
		    	id=rs.getInt(1)-1;
		    
		    rs.close();
		    ps.close();
		    
		    uid = uid+"--"+id;
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return uid;
	}

	public boolean activate(int id, String key) {
		
		boolean flag = false;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "update core_user set activated='Y' where id=? and activation_key=? and activated='N'";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, id);
		    ps.setString(2,key);
		    
		    int l = ps.executeUpdate();
		    
		    flag=(l>0);
		    
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
	}

	public boolean activate(String email, String activateKey) {
		boolean flag = false;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "update core_user set activated='Y' where login=? and activation_key=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, email);
		    ps.setString(2,activateKey);
		    
		    int l = ps.executeUpdate();
		    
		    flag=(l>0);
		    
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
	}

	public boolean checkUser(String email) {
		boolean flag = false;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "select id from core_user where login=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, email);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    flag=rs.next();
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
	}

	public boolean changeActivation(String email, String newPassword) {
		boolean flag=false;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "update core_user set activation_key=? where login=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, newPassword);
		    ps.setString(2,email);
		    
		    flag = (ps.executeUpdate()>0);
		    
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
		
	}

	public boolean lookup(String key) {
		boolean flag = false;
		
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "select id from core_user where activation_key=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, key);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    flag=rs.next();
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
	}

	public void updatePassword(String activateKey, String password) {
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "update core_user set password=? where login=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ps.setString(1,Md5.encode(password));
		    ps.setString(2,activateKey);
		    
		    ps.executeUpdate();
		    
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public String loadFormMode(CoreUser user) {
		String mode="protogen-formview";
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "select form_mode from core_role where id=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, user.getCoreRole().getId());
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	mode=rs.getString(1).equals("L")?"protogen-formline":"protogen-formview";
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return mode;
	}

	public int persist(CoreUser u) {
		String uid="";
		int dbID=0;
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    
		    String sql = "insert into core_user (\"firstName\",\"lastName\",tel,email,adress,login,password,\"idRole\",appkey,activation_key,activated,organization,org_instance,user_bean,profil, theme_color, theme_style,user_theme)" +
		    		"		VALUES (?,?,?,?,?,?,?,?,?,?,'Y',?,?,?,?,?,?,'THEME:DEVELOPR')";
		    
		    uid = ProtogenKeyGenerator.getInstance().generateKey(); 
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, u.getFirstName());
		    ps.setString(2, u.getLastName());
		    ps.setString(3, u.getTel());
		    ps.setString(4, u.getEmail());
		    ps.setString(5, u.getAdress());
		    ps.setString(6, u.getLogin());
		    ps.setString(7, Md5.encode(u.getPassword()));
		    ps.setInt(8, u.getCoreRole().getId());
		    ps.setString(9, u.getAppKey());
		    ps.setString(10, uid);
		    ps.setInt(11, u.getOrganization().getId());
		    ps.setInt(12, u.getOrgInstance());
		    ps.setInt(13, u.getBoundEntity());
		    if(u.getCoreRole().getProfil() != null)
		    	ps.setInt(14, u.getCoreRole().getProfil().getId());
		    else
		    	ps.setInt(14, 0);
		    ps.setString(15, "css/colors.css?v=1");
		    ps.setString(16, "css/style.css?v=1");
		    
		    ps.execute();
		    ps.close();
		    
		    sql="select nextval('core_user_seq')";
		    ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	dbID = rs.getInt(1)-1;
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return dbID;
	}

	public void updateProfilePicture(CoreUser user, String file) {
		String sql = "update core_user set picto = ? where id=?";
		user.setPhoto(file);
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, file);
			ps.setInt(2, user.getId());
			
			ps.execute();
			
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String insertUser(CoreUser u, boolean activated, int enid) {
		String uid="";
		String act = activated?"Y":"N";
		try {
			 
		    Class.forName("org.postgresql.Driver");
		    Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
		    /*
		     * 	u.setThemeColor("css/colors.css?v=1");
			 *	u.setThemeStyle("css/style.css?v=1");
		     */
		    String sql = "insert into core_user (\"firstName\",\"lastName\",tel,email,adress,login,password,\"idRole\",appkey,activation_key,activated,organization,org_instance,user_theme, theme_color, theme_style,user_bean, binding_entity)" +
		    		"		VALUES (?,?,?,?,?,?,?,?,?,?,'"+act+"',1,?,'THEME:DEVELOPR','css/colors.css?v=1','css/style.css?v=1',?, ?)";
		    
		    uid = ProtogenKeyGenerator.getInstance().generateKey(); 
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, u.getFirstName());
		    ps.setString(2, u.getLastName());
		    ps.setString(3, u.getTel());
		    ps.setString(4, u.getEmail());
		    ps.setString(5, u.getAdress());
		    ps.setString(6, u.getLogin());
		    ps.setString(7, Md5.encode(u.getPassword()));
		    ps.setInt(8, u.getCoreRole().getId());
		    ps.setString(9, u.getAppKey());
		    ps.setString(10, uid);
		    ps.setInt(11, u.getOrgInstance());
		    ps.setInt(12, u.getBoundEntity());
		    ps.setInt(13, enid);
		    
		    ps.execute();
		    ps.close();
		    
		    sql = "select nextval('core_user_seq')";
		    ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    int id=0;
		    if(rs.next())
		    	id=rs.getInt(1)-1;
		    
		    rs.close();
		    ps.close();
		    
		    uid = uid+"--"+id;
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return uid;
	}

	public void persistUserInfo(int id, String key, String value) {
		String sql = "insert into core_user_info (info_key, info_value, user_id) values (?,?,?)";
		try {
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement sp = cnx.prepareStatement(sql);
			sp.setString(1, key);
			sp.setString(2, value);
			sp.setInt(3, id);
			sp.execute();
			sp.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
}
