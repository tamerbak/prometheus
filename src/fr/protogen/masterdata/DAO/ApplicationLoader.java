package fr.protogen.masterdata.DAO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;

import com.thoughtworks.xstream.XStream;

import flexjson.JSONDeserializer;
import fr.protogen.connector.session.WebSessionManager;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dataload.QueryBuilder;
import fr.protogen.engine.control.ui.MailDTO;
import fr.protogen.engine.gexpression.ExpressionParserEngine;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.CompressionEngine;
import fr.protogen.engine.utils.ListKV;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.*;
import fr.protogen.ocr.pojo.Document;

public class ApplicationLoader {
	
	public String getMoneyCode(String appKey) {
		String code = "";
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    
		    String sql = "select value from s_application_parameters where appkey=? and key='MC'";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, appKey);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	code = rs.getString(1);
		    
		    rs.close();
		    ps.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return code;
	}

	/*
	 * Menu de l'application
	 */

	public List<SRubrique> loadRolesMenu(String appKey, List<CoreRole> roles) {
		try {
			
			String swindows = "";
			List<Integer> iwin = new ArrayList<Integer>();
			for(CoreRole r : roles){
				String sql = "select window_id from core_acl_screen where role_id=?";
				try{
					Connection cnx = ProtogenConnection.getInstance().getConnection();
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.setInt(1, r.getId());
					ResultSet rs = ps.executeQuery();
					while(rs.next()){
						boolean found=false;
						for(Integer i : iwin)
							if(i.intValue() == rs.getInt(1)){
								found=true;
								break;
							}
						if(!found)
							iwin.add(new Integer(rs.getInt(1)));
					}
					rs.close();
					ps.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			}
			
			for(Integer i : iwin){
				swindows = swindows+","+i.intValue();
			}
			
			if(swindows.length()>0)
				swindows = swindows.substring(1);
			else {
				return new ArrayList<SRubrique>(); //	No windows in roles, most likely in Admin mode
			}
			
		    List<SRubrique> rubriques = new ArrayList<SRubrique>();

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			List<SMenuitem> results = new ArrayList<SMenuitem>();
			
			//	Get the parents
			ResultSet apprs = st.executeQuery("select * from s_menuitem m where m.\"isParent\"=true and appkey='"+appKey+"' order by id asc");
			
			while (apprs.next()){
				SMenuitem item = new SMenuitem(apprs.getInt("id"), apprs.getString("title"), true);
				item.setRubrique(apprs.getInt("rubrique_id"));
				item.setAppKey(appKey);
				item.setSubs(new ArrayList<SMenuitem>());
				results.add(item);
			}
			
			// Get the children
			apprs.close();
			apprs = null;
			String sql = "select * from s_menuitem m where m.\"isParent\"=false and appkey='"+appKey+"' and m.window_id in ("+swindows+") order by id asc";
			System.out.println("MENU QUERY "+sql);
			apprs = st.executeQuery(sql);

			while(apprs.next()){
				SMenuitem item = new SMenuitem(apprs.getInt("id"), apprs.getString("title"), false);
				item.setAppKey(appKey);
				for(SMenuitem parent : results){
					if(parent.isParent() && parent.getId() == apprs.getInt("id_parent")){
						item.setParent(parent);
						parent.getSubs().add(item);
						break;
					}
					
				}
				results.add(item);
				
			}
			apprs.close();
			
			
			sql ="select * from s_rubrique where appkey=? order by id asc";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			
			apprs = ps.executeQuery();
			while(apprs.next()){
				SRubrique r = new SRubrique();
				r.setId(apprs.getInt("id"));
				r.setTitre(apprs.getString("title"));
				r.setDescription(apprs.getString("description"));
				r.setItems(new ArrayList<SMenuitem>());
				r.setPilotage(apprs.getString("flag")!=null&&apprs.getString("flag").equals("P"));
				r.setTechnical(apprs.getString("technical").equals("Y"));
				rubriques.add(r);
			}
			
			apprs.close();
			ps.close();

			for(SMenuitem m : results){
				if(m.getRubrique()==0)
					continue;
				for(SRubrique r : rubriques)
					if(r.getId()==m.getRubrique()){
						r.getItems().add(m);
						break;
					}
			}
			
			/*
			 * CLEAN UP 
			 */
			for(SRubrique r : rubriques){
				List<SMenuitem> toDelete = new ArrayList<SMenuitem>();
				for(SMenuitem i : r.getItems()){
					if(i.getSubs()==null || i.getSubs().size()==0)
						toDelete.add(i);
				}
				r.getItems().removeAll(toDelete);
			}
			
			for(SRubrique r : rubriques){
				r.setOneColumne(r.getItems().size()==1);
				if(r.isOneColumne()){
					SMenuitem i = r.getItems().get(0);
					r.setItems(i.getSubs());
				}
				try{
					//si un menu parent a un seul sous menu, mettre le sous menu directement dans la rubrique
					List<SMenuitem> addedMenuItems = new ArrayList<SMenuitem>();
					addedMenuItems.addAll(r.getItems());
					List<SMenuitem> menuItemsToDelete = new ArrayList<SMenuitem>();
					for(SMenuitem menuItem : r.getItems()){
						if(menuItem.isParent() && menuItem.getSubs().size() <= 1){
							int i = addedMenuItems.indexOf(menuItem);
							addedMenuItems.addAll(i, menuItem.getSubs());
							menuItemsToDelete.add(menuItem);
						}
					}
					r.setItems(addedMenuItems);
					r.getItems().removeAll(menuItemsToDelete);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			
			List<SRubrique> rubtodel = new ArrayList<SRubrique>();
			for(SRubrique r : rubriques){
				if((r.getItems()==null || r.getItems().size()==0) && !r.isTechnical())
					rubtodel.add(r);
			}
			rubriques.removeAll(rubtodel);
			
			return rubriques;
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<SRubrique> loadMenu(String appKey, CoreUser user){
		try {
		    List<SRubrique> rubriques = new ArrayList<SRubrique>();

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			List<SMenuitem> results = new ArrayList<SMenuitem>();
			
			//	Get the parents
			ResultSet apprs = st.executeQuery("select * from s_menuitem m where m.\"isParent\"=true and appkey='"+appKey+"' order by id asc");
			
			while (apprs.next()){
				SMenuitem item = new SMenuitem(apprs.getInt("id"), apprs.getString("title"), true);
				item.setRubrique(apprs.getInt("rubrique_id"));
				item.setAppKey(appKey);
				item.setSubs(new ArrayList<SMenuitem>());
				results.add(item);
			}
			
			// Get the children
			apprs.close();
			apprs = null;
			String sql = "select * from s_menuitem m where m.\"isParent\"=false and appkey='"+appKey+"' and m.window_id in ("+user.getCoreRole().getsWindows().replaceAll(";", ",")+") order by id asc";
			apprs = st.executeQuery(sql);

			while(apprs.next()){
				SMenuitem item = new SMenuitem(apprs.getInt("id"), apprs.getString("title"), false);
				item.setAppKey(appKey);
				for(SMenuitem parent : results){
					if(parent.isParent() && parent.getId() == apprs.getInt("id_parent")){
						item.setParent(parent);
						parent.getSubs().add(item);
						break;
					}
					
				}
				results.add(item);
				
			}
			apprs.close();
			
			//	Update children from all profiles
			List<CoreRole> rls = loadUserRoles(appKey, user);
			for(CoreRole r : rls){
				sql = "select * from s_menuitem m where m.\"isParent\"=false and appkey='"+appKey+"' and m.window_id in ("+r.getsWindows().replaceAll(";", ",")+") order by id asc";
				apprs = st.executeQuery(sql);
				while(apprs.next()){
					SMenuitem item = new SMenuitem(apprs.getInt("id"), apprs.getString("title"), false);
					item.setAppKey(appKey);
					boolean found = false;
					for(SMenuitem i : results){
						if(i.getId() == item.getId()){
							found=true;
							break;
						}
					}
					if(found)
						continue;
					for(SMenuitem parent : results){
						if(parent.isParent() && parent.getId() == apprs.getInt("id_parent")){
							item.setParent(parent);
							parent.getSubs().add(item);
							break;
						}
						
					}
					results.add(item);
					
				}
				apprs.close();
			}
			
			if(user.getSoptions()!=null && user.getSoptions().length()>0){
				sql = "select * from s_menuitem m where m.\"isParent\"=false and appkey='"+appKey+"' and m.id_parent in ("+user.getSoptions().replaceAll(";", ",")+") order by id asc";
				apprs = st.executeQuery(sql);
				while(apprs.next()){
					SMenuitem item = new SMenuitem(apprs.getInt("id"), apprs.getString("title"), false);
					item.setAppKey(appKey);
					for(SMenuitem parent : results){
						if(parent.isParent() && parent.getId() == apprs.getInt("id_parent")){
							item.setParent(parent);
							parent.getSubs().add(item);
							break;
						}
						
					}
					results.add(item);
					
				}
				
				apprs.close();
			}
			st.close();
			
			sql ="select * from s_rubrique where appkey=? order by id asc";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			
			apprs = ps.executeQuery();
			while(apprs.next()){
				SRubrique r = new SRubrique();
				r.setId(apprs.getInt("id"));
				r.setTitre(apprs.getString("title"));
				r.setDescription(apprs.getString("description"));
				r.setItems(new ArrayList<SMenuitem>());
				r.setPilotage(apprs.getString("flag")!=null&&apprs.getString("flag").equals("P"));
				r.setTechnical(apprs.getString("technical")!=null&&apprs.getString("technical").equals("Y"));
				rubriques.add(r);
			}
			
			apprs.close();
			ps.close();

			for(SMenuitem m : results){
				if(m.getRubrique()==0)
					continue;
				for(SRubrique r : rubriques)
					if(r.getId()==m.getRubrique()){
						r.getItems().add(m);
						break;
					}
			}
			
			/*
			 * CLEAN UP 
			 */
			for(SRubrique r : rubriques){
				List<SMenuitem> toDelete = new ArrayList<SMenuitem>();
				for(SMenuitem i : r.getItems()){
					if(i.getSubs()==null || i.getSubs().size()==0)
						toDelete.add(i);
				}
				r.getItems().removeAll(toDelete);
			}
			
			//si la rubrique contient un seul menu, mettre les sous menu de ce menu dans directement dans la rubrique
			for(SRubrique r : rubriques){
				r.setOneColumne(r.getItems().size()==1);
				if(r.isOneColumne()){
					SMenuitem i = r.getItems().get(0);
					r.setItems(i.getSubs());
				}
				try{
					//si un menu parent a un seul sous menu, mettre le sous menu directement dans la rubrique
					List<SMenuitem> addedMenuItems = new ArrayList<SMenuitem>();
					addedMenuItems.addAll(r.getItems());
					List<SMenuitem> menuItemsToDelete = new ArrayList<SMenuitem>();
					for(SMenuitem menuItem : r.getItems()){
						if(menuItem.isParent() && menuItem.getSubs().size() <= 1){
							int i = addedMenuItems.indexOf(menuItem);
							addedMenuItems.addAll(i, menuItem.getSubs());
							menuItemsToDelete.add(menuItem);
						}
					}
					r.setItems(addedMenuItems);
					r.getItems().removeAll(menuItemsToDelete);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			
			List<SRubrique> rubtodel = new ArrayList<SRubrique>();
			for(SRubrique r : rubriques){
				if((r.getItems()==null || r.getItems().size()==0) && !r.isTechnical())
					rubtodel.add(r);
			}
			rubriques.removeAll(rubtodel);
			
			return rubriques;
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Details des l'application
	 */
	public SMainwindow loadMainApp(String appKey){
		SMainwindow w = new SMainwindow();
		try {
		    

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			ResultSet apprs = st.executeQuery("select project_name, version, a.description as description" +
					" from s_application a" +
					" where a.appkey='"+appKey+"'");
			
			
			
			if(apprs.next()){
				w = populate(apprs);
			}
			apprs.close();
			st.close();
			
			String sql = "select fb_id, fb_url, tw_id, tw_url,tw_flag from s_social, s_application where s_social.application=s_application.id and appkey='"+appKey+"'";
			st = cnx.createStatement();
			apprs = st.executeQuery(sql);
			
			if(apprs.next()){
				w.getSApplication().setFbID(apprs.getString(1));
				w.getSApplication().setFbUrl(apprs.getString(2));
				w.getSApplication().setTwID(apprs.getString(3));
				w.getSApplication().setTwUrl(apprs.getString(4));
				w.getSApplication().setTwitter(apprs.getString(5));
			}
			apprs.close();
			st.close();
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return w;
	}
	
	private SMainwindow populate(ResultSet apprs) {
		// TODO Auto-generated method stub
		
		try {
			SMainwindow window = new SMainwindow();
			SApplication app = new SApplication();
			app.setProjectName(apprs.getString("project_name"));
			app.setVersion(apprs.getString("version"));
			app.setDescription(apprs.getString("description"));
			window.setSApplication(app);
			
			return window;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	

	public SScreensequence loadSequence(SMenuitem menuitem){
		
		try {
		    

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			String q = "SELECT s_menuitem.id AS idmenuitem, s_screensequence.id AS idsequence, s_screensequence.title AS title_sequence, c_window.id AS idwindow, c_window.title AS title_window,   c_window.\"stepDescription\" AS stepdescription,   c_window.percentage AS percentage,   c_window.\"helpVideo\" AS helpvideo,   c_windowtype.id AS idwindowtype,   c_windowtype.type AS windowtype, c_businessclass.data_reference as w_mainentity FROM public.s_menuitem,  public.s_screensequence,   public.c_window,   public.c_windowtype, c_businessclass WHERE  s_menuitem.id_screensequence = s_screensequence.id AND   s_screensequence.id = c_window.id_screen_sequence AND  c_window.id_windowtype = c_windowtype.id AND c_window.id_entity=c_businessclass.id AND s_screensequence.appkey='"+menuitem.getAppKey()+"' AND s_menuitem.id="+menuitem.getId();
			ResultSet apprs = st.executeQuery(q);
			
			return populateScreens(apprs,menuitem.getAppKey());
			
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}
	
	public SScreensequence populateScreens(ResultSet rs,String appKey) {
		
		try {
			SScreensequence sequence = new SScreensequence();
			boolean firstrowflag = true;
			
		
			while(rs.next()){
				if(firstrowflag){
					
					sequence.setId(rs.getInt("idsequence"));
					sequence.setTitle(rs.getString("title_sequence"));
					sequence.setWindows(new ArrayList<CWindow>());
					firstrowflag = false;
				}
				
				CWindow window = new CWindow();
				window.setHelpVideo(rs.getString("helpvideo"));
				window.setId(rs.getInt("idwindow"));
				window.setPercentage(rs.getInt("percentage"));
				window.setStepDescription(rs.getString("stepdescription"));
				window.setTitle(rs.getString("title_window"));
				CWindowtype type = new CWindowtype(rs.getInt("idwindowtype"),rs.getString("windowtype"));
				window.setMainEntity(rs.getString("w_mainentity"));
				window.setAppKey(appKey);
				
				window.setCWindowtype(type);
				sequence.getWindows().add(window);
				
			}
			
			return sequence;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public CWindow loadFullWindow(CWindow window){
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		LocalizationEngine translator = new LocalizationEngine();
		try {
			ResultSet apprs = null;

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			window.setTabsReferences(new HashMap<String, List<CAttribute>>());
			
			if(window.getCWindowtype() == null){
				String query = "select c_windowtype.* from c_window, c_windowtype where c_window.id_windowtype=c_windowtype.id and c_window.id="+window.getId();
				apprs = st.executeQuery(query);
				if(apprs.next()){
					CWindowtype type = new CWindowtype(apprs.getInt("id"), apprs.getString("type"));
					window.setCWindowtype(type);
				}
				apprs.close();
				
				query="select c_window.*, c_businessclass.data_reference from c_window, c_businessclass where c_window.id_entity=c_businessclass.id and c_window.id="+window.getId();
				apprs = st.executeQuery(query);
				if(apprs.next()){
					window.setHelpVideo(apprs.getString("helpVideo"));
					window.setMainEntity(apprs.getString("data_reference"));
					window.setStepDescription(apprs.getString("stepDescription"));
					window.setTitle(apprs.getString("title"));
					String consts = apprs.getString("selection_constraint");
					window.setConstraints(new ArrayList<String>());
					if(consts != null && consts.length()>0)
						for(String c : consts.split(";"))
							window.getConstraints().add(c);
				}
				apprs.close();
			}
			
			
			
			String globalQuery = "SELECT   c_window.id AS w_id, c_window.rappel_reference as rappel_reference , c_window.selection_constraint, c_window_windowattribute.id_attribute,   c_window_windowattribute.id_window,  " +
					"c_window_windowattribute.is_reference, c_attribute.id AS a_id,  c_attribute.autovalue AS a_autovalue, c_attribute.data_reference AS a_datareference, " +
					"c_attribute.multiple as multiple, c_attribute.mandatory as mandatory, c_attribute.requires_validation as requires_validation, c_attribute.validation_formula as validation_formula, " +
					"c_window_windowattribute.visible AS a_visible,   c_attribute.id_attributetype,   c_attribute.attribute AS attribute,   c_attribute.id_class, c_attribute.default_value, " +
					"c_attribute.key_attribute, c_window_windowattribute.rappel as rappel, c_attribute.is_calculated as is_calculated, c_attribute.formula as formula, c_attributetype.id AS at_id,   " +
					"c_attributetype.type AS at_type,   c_businessclass.id AS c_id,   c_businessclass.data_reference AS c_datareference,   c_businessclass.visible AS c_visible,   " +
					"c_businessclass.name AS c_name, c_attribute.file_title as file_title, c_attribute.file_extension as file_extension, c_window_windowattribute.indirect_reference, c_window_windowattribute.indirect_mtm_key," +
					" c_window_windowattribute.indirect_mtm_value, c_window_windowattribute.indirect_function, c_window_windowattribute.filter_enabled as filter_enabled" +
					", c_attribute.lock_attribute as lock_attribute, c_attribute.field_width as field_width, c_attribute.unlock_label as unlock_label, c_attribute.lock_label as lock_label, c_window.form_window as form_window, "+ 
					" c_attribute.conditional_layout as conditional_layout, c_window.update_option as update_option, c_window.drop_option as drop_option, " +
					" c_window.mandatory_filter, c_window_windowattribute.forced_filter, c_window_windowattribute.subvisible, c_attribute.meta_table_ref as meta_table_ref " +
					" ,c_attribute.textarea as textarea " +
					"FROM   public.c_window,   public.c_window_windowattribute,   public.c_attribute,   public.c_attributetype,   public.c_businessclass " +
					" " +
					" " +
					"WHERE   c_window_windowattribute.id_window = c_window.id AND  c_window_windowattribute.id_attribute = c_attribute.id AND  " +
					"c_attributetype.id = c_attribute.id_attributetype AND  c_businessclass.id = c_attribute.id_class AND " +
					"c_window_windowattribute.visible=TRUE AND c_window.id="+window.getId()+" ORDER BY c_window_windowattribute.display_order";
			
			apprs = st.executeQuery(globalQuery);
			
			window.setCAttributes(new ArrayList<CAttribute>());
			int idRappelReference = 0;
			while(apprs.next()){
				CAttributetype type = new CAttributetype(apprs.getInt("at_id"),apprs.getString("at_type"));
				CBusinessClass entity = new CBusinessClass(apprs.getInt("c_id"), apprs.getString("c_datareference"), apprs.getBoolean("c_visible"));
				entity.setName(apprs.getString("c_name"));
				
				CAttribute attribute  = new CAttribute(apprs.getInt("a_id"), apprs.getString("a_datareference"), apprs.getBoolean("a_visible"));
				String title = translator.attributeTranslate(apprs.getString("attribute"), attribute.getId(), u.getLanguage());
				attribute.setAttribute(title);
				attribute.setCAttributetype(type);
				attribute.setEntity(entity);
				attribute.setKeyAttribute(apprs.getInt("key_attribute")==1);
				attribute.setReference(apprs.getInt("is_reference") == 1);
				attribute.setMultiple(apprs.getString("multiple").equals("Y"));
				attribute.setCalculated(apprs.getString("is_calculated").equals("Y"));
				attribute.setFormula(apprs.getString("formula"));
				attribute.setAutoValue(apprs.getString("a_autovalue").equals("Y"));
				attribute.setRequiresValidation(apprs.getString("requires_validation").equals("Y"));
				attribute.setValidationFormula(apprs.getString("validation_formula"));
				attribute.setMandatory(apprs.getString("mandatory").equals("Y"));
				attribute.setRappel(apprs.getString("rappel").equals("Y"));
				attribute.setIndirectMtmKey(apprs.getString("indirect_mtm_key").equals("Y"));
				attribute.setIndirectReference(apprs.getString("indirect_reference").equals("Y"));
				attribute.setIndirectMtmValue(apprs.getString("indirect_mtm_value").equals("Y"));
				attribute.setIndirectFunction(apprs.getInt("indirect_function"));
				attribute.setFilterEnabled(apprs.getString("filter_enabled").equals("Y"));
				attribute.setLockLabel(apprs.getString("lock_label"));
				attribute.setUnlockLabel(apprs.getString("unlock_label"));
				attribute.setDefaultValue(apprs.getString("default_value"));
				attribute.setConditionalLayout(apprs.getString("conditional_layout").equals("Y"));
				attribute.setFilterMandatory(apprs.getString("forced_filter").equals("Y"));
				attribute.setMetatableReference(apprs.getString("meta_table_ref"));
				attribute.setTextarea(apprs.getString("textarea").equals("Y"));
				
				attribute.setFieldWidth(apprs.getInt("field_width"));
				
				if(!attribute.isCalculated() && !attribute.isConditionalLayout() && attribute.getFormula()!=null && attribute.getFormula().length()>0 && !attribute.getFormula().contains("null")){
					attribute.setRegular(true);
					attribute.setRegexp(attribute.getFormula().split(":")[0]);
					attribute.setRegexpMessage(attribute.getFormula().split(":")[1]);
					
				}
				//	Binary content
				attribute.setFileExtension(apprs.getString("file_extension"));
				attribute.setFileName(apprs.getString("file_title"));
				
				idRappelReference = apprs.getInt("rappel_reference");
				window.getCAttributes().add(attribute);
				window.setFormId(apprs.getInt("form_window"));
				window.setUpdateBtn(apprs.getString("update_option").equals("Y"));
				window.setDeleteBtn(apprs.getString("drop_option").equals("Y"));
				window.setForcedFilter(!apprs.getString("mandatory_filter").equals("Y"));
				String consts = apprs.getString("selection_constraint");
				window.setConstraints(new ArrayList<String>());
				if(consts != null && consts.length()>0)
					for(String c : consts.split(";"))
						window.getConstraints().add(c);
				
				attribute.setSubvisible(apprs.getString("subvisible").equals("Y"));
				
//				String attributeEntityRef = attribute.getEntity().getDataReference();
//				String pkElm = "";
//				 if (!attribute.isReference() && attribute.getDataReference().startsWith("fk_")){
//					 pkElm = attribute.getDataReference().substring(3);
//				 }
//				if(!attributeEntityRef.equals(window.getMainEntity())) {
//					if(window.getTabsReferences().get(attributeEntityRef) == null) {
//						window.getTabsReferences().put(attributeEntityRef, new ArrayList<CAttribute>());
//					}
//					window.getTabsReferences().get(attributeEntityRef).add(attribute);
//					window.getCAttributes().remove(attribute);
//					
//				} else if (!pkElm.equals(window.getMainEntity()) &&  pkElm.equals(attributeEntityRef)) {
//					attributeEntityRef = attribute.getDataReference().substring(3);
//					if(window.getTabsReferences().get(attributeEntityRef) == null) {
//						window.getTabsReferences().put(attributeEntityRef, new ArrayList<CAttribute>());
//					}
//					window.getTabsReferences().get(attributeEntityRef).add(attribute);
//					window.getCAttributes().remove(attribute);
//				}
			}
			
			if(idRappelReference>0){
				for(CAttribute a : window.getCAttributes())
					if(a.getId() == idRappelReference){
						window.setRappelReference(a);
						break;
					}
			}
			
			apprs.close();
//			if(window.getCWindowtype() == null){
//				String query = "select c_windowtype.* from c_window, c_windowtype where c_window.id_windowtype=c_windowtype.id and c_window.id="+window.getId();
//				apprs = st.executeQuery(query);
//				if(apprs.next()){
//					CWindowtype type = new CWindowtype(apprs.getInt("id"), apprs.getString("type"));
//					window.setCWindowtype(type);
//				}
//				apprs.close();
//				
//				query="select c_window.*, c_businessclass.data_reference from c_window, c_businessclass where c_window.id_entity=c_businessclass.id and c_window.id="+window.getId();
//				apprs = st.executeQuery(query);
//				if(apprs.next()){
//					window.setHelpVideo(apprs.getString("helpVideo"));
//					window.setMainEntity(apprs.getString("data_reference"));
//					window.setStepDescription(apprs.getString("stepDescription"));
//					window.setTitle(apprs.getString("title"));
//				}
//				apprs.close();
//			}
//			
			//	Global values
			String query = "select c_globalvalue.id, c_globalvalue.\"key\", c_globalvalue.\"label\", c_globalvalue.\"value\", c_globalvalue.appkey, c_window.id from c_globalvalue, c_window, c_window_globalvalue where c_window.id=c_window_globalvalue.id_window and c_globalvalue.id=c_window_globalvalue.id_global and c_window.id=?";
		    PreparedStatement ps = cnx.prepareStatement(query);
		    ps.setInt(1, window.getId());
		    
		    ResultSet rs = ps.executeQuery();
		    window.setGlobalValues(new ArrayList<CGlobalValue>());
		    while(rs.next()){
		    	CGlobalValue v = new CGlobalValue();
		    	v.setId(rs.getInt(1));
		    	v.setKey(rs.getString(2));
		    	v.setLabel(rs.getString(3));
		    	v.setValue(rs.getString(4));
		    	v.setAppKey(rs.getString(5));
		    	window.getGlobalValues().add(v);
		    }
			
			return window;
		} catch(Exception exc){
			exc.printStackTrace();
		}
		
		return null;
	}
	
	public CWindow loadWindowWithActions(CWindow window){
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		LocalizationEngine translator = new LocalizationEngine();
		
		try {
		    

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			ResultSet apprs = st.executeQuery("SELECT   m_action.title AS action_title,   m_action.code AS action_code,   c_actionbutton.title AS button_title,  c_actionbutton.parameters as parameters, " +
					"							c_window.id AS window_id,   c_actionbutton.id AS button_id,   m_action.id AS action_id, m_action.id_postaction AS postaction, " +
					"							c_actionbutton.document_bound as document_bound, c_actionbutton.id_document as id_document,  c_actionbutton.description as b_description " +
					"							FROM   " +
					"							public.c_window,   public.c_actionbutton,   public.m_action " +
					"							WHERE   " +
					"							c_actionbutton.id_window = c_window.id AND  c_actionbutton.id_action = m_action.id AND c_window.id="+window.getId());
			
			window.setCActionbuttons(new ArrayList<CActionbutton>());
			
			while(apprs.next()){
				MAction action = new MAction();
				action.setTitle(apprs.getString(1));
				action.setCode(apprs.getString(2));
				action.setId(apprs.getInt("action_id"));
				MPostAction postaction = new MPostAction();
				postaction.setId(apprs.getInt("postaction"));
				action.setPostAction(postaction);
				CActionbutton button = new CActionbutton();
				button.setBound(apprs.getString("document_bound").equals("Y"));
				button.setBoundDocument(new CDocumentbutton());
				button.getBoundDocument().setId(apprs.getInt("id_document"));
				button.setId(apprs.getInt("button_id"));
				String title = translator.actionTranslate(apprs.getString(3), button.getId(), u.getLanguage());
				button.setTitle(title);
				button.setParameters(apprs.getString("parameters"));
				button.setMAction(action);
				button.setDescription(apprs.getString("b_description"));
				
				button.setParametered(button.getParameters()!=null && button.getParameters().length()>0);
				
				window.getCActionbuttons().add(button);
				
				
			}
			
			apprs.close();
			
			for(CActionbutton b : window.getCActionbuttons()){
				if(!b.isBound())
					continue;
				String sql = "SELECT c_documentbutton.id as id, c_documentbutton.title as btntitle, c_documentbutton.parameters as parameters,c_documentbutton.description as b_description, m_document.title as doctitle, m_document.stream as filepath, m_document.id as mdid, m_document.parameter_mode as paramode   FROM  public.c_documentbutton, public.m_document WHERE m_document.id = c_documentbutton.id_document and c_documentbutton.id="+b.getBoundDocument().getId();
				apprs = st.executeQuery(sql);
				while(apprs.next()){
					CDocumentbutton btn = new CDocumentbutton();
					btn.setId(apprs.getInt("id"));
					btn.setParameters(apprs.getString("parameters"));
					String title = translator.actionTranslate(apprs.getString("btntitle"), btn.getId(), u.getLanguage());
					btn.setTitle(title);
					btn.setDescription(apprs.getString("b_description"));
					MDocument doc = new MDocument();
					doc.setStream(apprs.getString("filepath"));
					doc.setId(apprs.getInt("mdid"));
					doc.setParameterMode(apprs.getString("paramode").charAt(0));
					btn.setMDocument(doc);
					b.setBoundDocument(btn);
				}
				apprs.close();
			}
						
			String query = "SELECT c_documentbutton.id as id, c_documentbutton.title as btntitle, c_documentbutton.parameters as parameters, c_documentbutton.description as b_description, m_document.title as doctitle, m_document.stream as filepath, m_document.id as mdid, m_document.parameter_mode as paramode   FROM public.c_window, public.c_documentbutton, public.m_document WHERE c_documentbutton.id_window = c_window.id AND m_document.id = c_documentbutton.id_document and c_window.id="+window.getId();
			
			apprs = st.executeQuery(query);
			window.setCDocumentbuttons(new ArrayList<CDocumentbutton>());
			while(apprs.next()){
				CDocumentbutton btn = new CDocumentbutton();
				btn.setId(apprs.getInt("id"));
				btn.setParameters(apprs.getString("parameters"));
				String title = translator.actionTranslate(apprs.getString("btntitle"), btn.getId(), u.getLanguage());
				btn.setTitle(title);
				MDocument doc = new MDocument();
				doc.setStream(apprs.getString("filepath"));
				doc.setId(apprs.getInt("mdid"));
				doc.setParameterMode(apprs.getString("paramode").charAt(0));
				btn.setMDocument(doc);
				btn.setDescription(apprs.getString("b_description"));
				window.getCDocumentbuttons().add(btn);
			}
			
			//	Get triggers
			String sql = "select t.id, t.formula, " +
					"a.attribute, a.data_reference, " +
					"b.name, b.data_reference, " +
					"t.is_direct " +
					"from c_trigger t, c_attribute a, c_businessclass b " +
					" where t.id_target=a.id " +
					" and t.id_reference=b.id " +
					" and t.id_window=?";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, window.getId());
			
			ResultSet rs = ps.executeQuery();
			window.setTriggers(new ArrayList<Trigger>());
			while(rs.next()){
				CAttribute a = new CAttribute();
				CBusinessClass b = new CBusinessClass();
				
				Trigger t = new Trigger();
				t.setWindow(window);
				t.setId(rs.getInt(1));
				t.setFormula(rs.getString(2));
				a.setAttribute(rs.getString(3));
				a.setDataReference(rs.getString(4));
				b.setName(rs.getString(5));
				b.setDataReference(rs.getString(6));
				t.setDirect(rs.getInt(7)==0);
				t.setTarget(a);
				t.setReference(b);
				
				window.getTriggers().add(t);
			}
			
			rs.close();
			st.close();
			
			//	Get workflow entries
			sql = "select id, title, description from s_wf_definition where \"window\"=?";
			ps = cnx.prepareStatement(sql);
			
			ps.setInt(1, window.getId());
			
			rs = ps.executeQuery();
			window.setWorkflows(new ArrayList<WorkflowDefinition>());
			while (rs.next()){
				WorkflowDefinition  definition = new WorkflowDefinition();
				definition.setId(rs.getInt(1));
				definition.setTitle(rs.getString(2));
				definition.setDescription(rs.getString(3));
				window.getWorkflows().add(definition);
			}
			
			rs.close();
			ps.close();
			
			return window;
		} catch(Exception exc){
			exc.printStackTrace();
		}
		
		return null;
	}

	public CWindow filterWindow(CWindow window, List<CoreRole> roles) {

		boolean modification = false;
		boolean suppression = false;
		String allroles = "";
		for(CoreRole r : roles)
			allroles = allroles+","+r.getId();
		if(allroles.length() == 0)
			return window;
		
		allroles = allroles.substring(1);
		
		String sql = "select id from core_acl_screen where window_id=? and role_id in ("+allroles+") and modification='Y'";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, window.getId());
			ResultSet rs = ps.executeQuery();
			modification = rs.next();
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		window.setUpdateBtn(modification);
		
		sql = "select id from core_acl_screen where window_id=? and role_id in ("+allroles+") and suppression='Y'";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, window.getId());
			ResultSet rs = ps.executeQuery();
			suppression = rs.next();
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		window.setDeleteBtn(suppression);
		
		sql="select distinct aa.attribute_id from core_acl_screen_attribute aa, core_acl_screen aw where aa.acl=aw.id "
				+ " and aw.window_id=? and role_id in ("+allroles+")";
		List<CAttribute> tokeep = new ArrayList<CAttribute>();
		List<CAttribute> torem = new ArrayList<CAttribute>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, window.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				int ida = rs.getInt(1);
				for(CAttribute a : window.getCAttributes()){
					if(a.getId() == ida){
						tokeep.add(a);
						break;
					}
				}
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(CAttribute a : window.getCAttributes()){
			boolean found = false;
			for(CAttribute k : tokeep){
				if(k == a){
					found=true;
					break;
				}
			}
			if(!found)
				torem.add(a);
		}
		
		window.getCAttributes().removeAll(torem);
		return window;
	}

	
	public List<String> loadArguments(MAction act) {
		// TODO Auto-generated method stub
		try{
			List<String> results = new ArrayList<String>();
			
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			String query = "SELECT   m_arguments.tag FROM   public.m_action,   public.m_arguments WHERE   m_arguments.id_action = m_action.id AND m_action.id ="+act.getId();
			
			ResultSet rs = st.executeQuery(query);
			
			while(rs.next()){
				String result = rs.getString(1);
				results.add(result);
			}
			
			return results;
			
		}catch(Exception exc){
			
		}
		
		return null;
	}
	
	public MPostAction getPostAction(MAction action){
		
		int paID=action.getPostAction().getId();
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			String query = "SELECT   m_postaction_type.id as idtype,   m_postaction_type.type as ptype,   m_postaction_attribute.id_postaction, m_postaction_attribute.prefixe as prefixe,  m_postaction_attribute.id_attribute,  m_postaction_attribute.default_value, m_postaction_attribute.parameter,   m_postaction.id as postactio_id,   m_postaction.idtype,   c_attribute.id as attribute_id,   c_attribute.data_reference,   c_attribute.visible,  c_attribute.autovalue, c_attribute.id_attributetype,    c_attribute.attribute, c_attribute.multiple as ismultiple, c_attribute.\"reference\" as is_ref,   c_attribute.id_class,   c_businessclass.data_reference AS \"targetTable\" FROM   public.m_postaction,  public.m_postaction_attribute,   public.m_postaction_type,   public.c_attribute,   public.c_businessclass WHERE   m_postaction.idtype = m_postaction_type.id AND m_postaction_attribute.id_postaction = m_postaction.id AND  m_postaction_attribute.id_attribute = c_attribute.id AND  c_attribute.id_class = c_businessclass.id AND m_postaction.id="+paID;
			
			ResultSet rs = st.executeQuery(query);
			boolean flag = false;
			MPostAction postaction = new MPostAction();
			postaction.setId(paID);
			postaction.setAttributes(new ArrayList<CAttribute>());
			postaction.setDefaultValues(new ArrayList<String>());
			postaction.setParametersValues(new ArrayList<String>());
			while(rs.next()){
				if(!flag){
					MPostactionType type = new MPostactionType();
					type.setId(rs.getInt("idtype"));
					type.setLabel(rs.getString("ptype"));
					postaction.setType(type);
					postaction.setPrefixes(new ArrayList<String>());
					flag = true;
				}
				
				CAttribute attribute = new CAttribute();
				CAttributetype t = new CAttributetype();
				t.setId(rs.getInt("id_attributetype"));
				attribute.setAttribute(rs.getString("attribute"));
				attribute.setCAttributetype(t);
				attribute.setAutoValue(rs.getString("autovalue").equals("Y"));
				attribute.setDataReference(rs.getString("data_reference"));
				attribute.setId(rs.getInt("id_attribute"));
				attribute.setReference(rs.getString("is_ref").equals("Y"));
				System.out.println(rs.getInt("attribute_id")+" : "+rs.getString("ismultiple"));
				attribute.setMultiple(rs.getString("ismultiple").equals("Y"));
				CBusinessClass bc = new CBusinessClass();
				bc.setDataReference(rs.getString("targetTable"));
				attribute.setEntity(bc);
				postaction.getAttributes().add(attribute);
				postaction.getDefaultValues().add(rs.getString("default_value"));
				postaction.getPrefixes().add(rs.getString("prefixe"));
				postaction.getParametersValues().add(rs.getString("parameter"));
			}
			
			return postaction;
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		return null;
		
	}

	public MAction getFirstaction() {
		// TODO Auto-generated method stub

		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			String query = "select * from m_action";
			
			ResultSet rs = st.executeQuery(query);
			
			if(rs.next()){
				MAction act = new MAction(rs.getInt(1), rs.getString(2), rs.getString(3));
				return act;
			}
			
			return null;
			
		} catch(Exception exc){
			
		}
					return null;
	}

	public void storeAction(MAction action) {
		// TODO Auto-generated method stub
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			String query = "update m_action set code='"+action.getCode()+"'";
			
			st.execute(query);			
			
			
		} catch(Exception exc){
			
		}

	}

	public void cleanSalaires() {
		// TODO Auto-generated method stub
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			String query = "delete  from user_salaires";
			
			st.execute(query);			
			
			
		} catch(Exception exc){
			
		}
	}

	public CParametersWindow loadFullParametersScreen(int id) {
		// TODO Auto-generated method stub
		String query = "SELECT  c_parameters_window.title AS title,  c_parameters_window.\"stepDescription\" AS description,  c_parameter_ctrl.parameter_key,  c_parameter_ctrl.parameter_type,  c_parameter_ctrl.parameter_label,  c_parameters_window.id AS window_id,  c_parameter_ctrl.id AS parameter_id FROM  public.c_parameters_window,  public.c_parameter_ctrl WHERE c_parameter_ctrl.id_window = c_parameters_window.id and c_parameters_window.id="+id;
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(query);
			boolean visited = false;
			CParametersWindow window = new CParametersWindow();
			List<CUIParameter> parameters = new ArrayList<CUIParameter>();
			while(rs.next()){
				if(!visited){
					window.setDescription(rs.getString("description"));
					window.setId(rs.getInt("window_id"));
					window.setTitle(rs.getString("title"));
					visited = true;
				}
				CUIParameter parameter = new CUIParameter();
				parameter.setId(rs.getInt("parameter_id"));
				parameter.setParameterKey(rs.getString("parameter_key"));
				parameter.setParameterType(rs.getString("parameter_type").charAt(0));
				parameter.setParameterLabel(rs.getString("parameter_label"));
				if(parameter.getParameterType() == 'D')
					parameter.setCtrlDate(true);
				parameters.add(parameter);
			}
			window.setUiParameters(parameters);
			rs.close();
			st.close();
			
			
			return window;
			
			
		}catch(Exception exc){
			
		}
		
		
		return null;
	}

	public CWindow loadWindowFromMenu(SMenuitem menuitem) {
		// TODO Auto-generated method stub
		try {
		    

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			String q = "SELECT s_menuitem.id AS idmenuitem, c_window.id AS idwindow, c_window.title AS title_window,   c_window.\"stepDescription\" AS stepdescription,   c_window.percentage AS percentage,   c_window.\"helpVideo\" AS helpvideo,   c_windowtype.id AS idwindowtype,   c_windowtype.type AS windowtype, c_businessclass.data_reference as w_mainentity FROM public.s_menuitem,  public.c_window,   public.c_windowtype, c_businessclass WHERE  s_menuitem.window_id = c_window.id  AND  c_window.id_windowtype = c_windowtype.id AND c_window.id_entity=c_businessclass.id AND c_window.appkey='"+menuitem.getAppKey()+"' AND s_menuitem.id="+menuitem.getId();
			ResultSet rs = st.executeQuery(q);
			
			CWindow window = new CWindow();
			if(rs.next()){
				window.setHelpVideo(rs.getString("helpvideo"));
				window.setId(rs.getInt("idwindow"));
				window.setPercentage(rs.getInt("percentage"));
				window.setStepDescription(rs.getString("stepdescription"));
				window.setTitle(rs.getString("title_window"));
				CWindowtype type = new CWindowtype(rs.getInt("idwindowtype"),rs.getString("windowtype"));
				window.setMainEntity(rs.getString("w_mainentity"));
				window.setAppKey(menuitem.getAppKey());
				
				window.setCWindowtype(type);
			}
			rs.close();
			
			q = "select destination, c_window.title from c_window_link, c_window where source=? and c_window.id=destination";
			PreparedStatement ps = cnx.prepareStatement(q);
			ps.setInt(1, window.getId());
			rs = ps.executeQuery();
			window.setLinks(new ArrayList<CWindow>());
			while(rs.next()){
				CWindow w = new CWindow();
				w.setAppKey(menuitem.getAppKey());
				w.setId(rs.getInt(1));
				w.setTitle(rs.getString(2));
				window.getLinks().add(w);
			}
			rs.close();
			ps.close();
			
			
			
			return window;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	public CWindow loadWindowFromLink(CWindow window) {
		// TODO Auto-generated method stub
		try {
		    

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			String q = "SELECT c_window.id AS idwindow, c_window.title AS title_window,   c_window.\"stepDescription\" AS stepdescription,   c_window.percentage AS percentage,   c_window.\"helpVideo\" AS helpvideo,   c_windowtype.id AS idwindowtype,   c_windowtype.type AS windowtype, c_businessclass.data_reference as w_mainentity, "
							+ "c_window.update_option, c_window.drop_option FROM public.c_window,   public.c_windowtype, c_businessclass WHERE   c_window.id_windowtype = c_windowtype.id AND c_window.id_entity=c_businessclass.id AND c_window.appkey='"+window.getAppKey()+"' AND c_window.id="+window.getId();
			ResultSet rs = st.executeQuery(q);
			
			if(rs.next()){
				window.setHelpVideo(rs.getString("helpvideo"));
				window.setId(rs.getInt("idwindow"));
				window.setPercentage(rs.getInt("percentage"));
				window.setStepDescription(rs.getString("stepdescription"));
				window.setTitle(rs.getString("title_window"));
				CWindowtype type = new CWindowtype(rs.getInt("idwindowtype"),rs.getString("windowtype"));
				window.setMainEntity(rs.getString("w_mainentity"));
				window.setAppKey(window.getAppKey());
				window.setUpdateBtn(rs.getString("update_option").equals("Y"));
				window.setDeleteBtn(rs.getString("drop_option").equals("Y"));
				
				window.setCWindowtype(type);
			}
			rs.close();
			
			q = "select destination, c_window.title from c_window_link, c_window where source=? and c_window.id=destination";
			PreparedStatement ps = cnx.prepareStatement(q);
			ps.setInt(1, window.getId());
			rs = ps.executeQuery();
			window.setLinks(new ArrayList<CWindow>());
			while(rs.next()){
				CWindow w = new CWindow();
				w.setAppKey(window.getAppKey());
				w.setId(rs.getInt(1));
				w.setTitle(rs.getString(2));
				window.getLinks().add(w);
			}
			rs.close();
			ps.close();
			
			
			st.close();
			
			
			return window;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;	
	}

	public CBusinessClass getEntity(String dataReference){
		CBusinessClass entity = new CBusinessClass();
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    String sql = "select id, name, appkey,user_restrict, storable_entity from c_businessclass where data_reference=?";
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, dataReference);
		    
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	entity.setId(rs.getInt(1));
		    	entity.setName(rs.getString(2));
		    	entity.setAppKey(rs.getString(3));
		    	entity.setUserRestrict(rs.getString("user_restrict").charAt(0));
		    	entity.setDataReference(dataReference);
		    	entity.setStorable(rs.getString("storable_entity").equals("Y"));
		    }
		    rs.close();
		    
		    
		    List<CAttribute> attributes = getEntityAttributes(dataReference);
		    entity.setAttributes(attributes);
		    
		}catch(Exception exc){
			String dummy = "";
			dummy = dummy+exc.getMessage();
			exc.printStackTrace();
		}
		
		return entity;
	}
	
	public List<CAttribute> getEntityAttributes(String tableReference) {
		// TODO Auto-generated method stub
		
		String query = "SELECT c_attribute.id AS a_id, c_attribute.requires_validation AS requires_validation, c_attribute.validation_formula AS validation_formula, "
				+ "c_attribute.data_reference AS a_datareference, c_attribute.id_attributetype AS a_attributetype,   c_attribute.attribute AS a_attribute, "
				+ "c_attribute.id_class AS a_identity, c_attribute.key_attribute AS a_key, c_attribute.formula AS a_formula, c_attribute.is_calculated AS a_calculated, "
				+ "c_attribute.mandatory AS a_mandatory, c_attribute.reference AS a_reference, c_attribute.multiple AS a_multiple, c_businessclass.id AS e_id, "
				+ "c_businessclass.data_reference AS e_datareference, c_businessclass.appkey, c_businessclass.description, c_businessclass.name AS e_name, "
				+ "c_attribute.textarea AS textarea, c_attribute.field_width as field_width "
				+ ""
				+ "FROM public.c_attribute, public.c_businessclass "
				+ ""
				+ "WHERE c_businessclass.id = c_attribute.id_class AND c_businessclass.data_reference='"+tableReference+"' order by c_attribute.id asc";
		List<CAttribute> results = new ArrayList<CAttribute>();
		try {
		    

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			boolean flag=false;
			CBusinessClass e = new CBusinessClass();

			while(rs.next()){
				
				//	CBusinessclass
				if(!flag){
					e.setDataReference(tableReference);
					e.setId(rs.getInt("e_id"));
					e.setName(rs.getString("e_name"));
					e.setAppKey(rs.getString("appkey"));
					flag=true;
				}
				//	CAttributetype
				CAttributetype t = new CAttributetype();
				t.setId(rs.getInt("a_attributetype"));
				switch(t.getId()){
					case 1 : t.setType("Entier");break;
					case 2 : t.setType("Texte");break;
					case 3 : t.setType("Date");break;
					case 4 :case 8: t.setType("Double");break;
					case 5 : t.setType("Heure");break;
					case 6 : t.setType("Fichier");break;
					case 7 : t.setType("Utilisateur");break;
					case 9 : t.setType("Verroux");break;
					case 10 : t.setType("Table");break;
					case 11 : t.setType("Référence");break;
					case 12 : t.setType("Booléen");break;
				}
				
				//	CAttribute
				CAttribute a = new CAttribute();
				a.setAttribute(rs.getString("a_attribute"));
				a.setCalculated(rs.getString("a_calculated").equals("Y"));
				a.setDataReference(rs.getString("a_datareference"));
				a.setFormula(rs.getString("a_formula"));
				a.setId(rs.getInt("a_id"));
				a.setKeyAttribute(rs.getInt("a_key")==1);
				a.setMandatory(rs.getString("a_mandatory").equals("Y"));
				a.setReference(rs.getString("a_reference").equals("Y"));
				a.setMultiple(rs.getString("a_multiple").equals("Y"));
				a.setRequiresValidation(rs.getString("requires_validation").equals("Y"));
				a.setValidationFormula(rs.getString("validation_formula"));
				a.setTextarea(rs.getString("textarea").equals("Y"));
				a.setEntity(e);
				a.setCAttributetype(t);
				a.setFieldWidth(rs.getInt("field_width"));
				results.add(a);
			}
			for(CAttribute a: results)
				a.setEntity(e);
			
			
		} catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}

	public String getEntityFromDR(String referenceTable) {
		// TODO Auto-generated method stub
		String result="";
		try {
		    

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			String query = "select name from c_businessclass where data_reference='"+referenceTable+"'";
			ResultSet rs = st.executeQuery(query);
			if(rs.next())
				result = rs.getString(1);
			
			rs.close();
			st.close();
			
		} catch(Exception exc){
			
		}
		
		return result;
	}

	public List<String> getDependentEntities(String mainEntity) {
		// TODO Auto-generated method stub
		List<String> tables = new ArrayList<String>();
		try {
		    
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			String query = "select data_reference from c_businessclass";
			ResultSet rs = st.executeQuery(query);
			List<String> alltables = new ArrayList<String>();
			
			while(rs.next()){
				alltables.add(rs.getString(1));
			}
			rs.close();
			
			for(String t : alltables){
				String key="fk_"+t+"__"+mainEntity;
				query = "select id from c_attribute where data_reference='"+key+"'";
				rs = st.executeQuery(query);
				if(rs.next())
					tables.add(t);
				rs.close();
			}
			
			st.close();
			
		} catch(Exception exc){
			
		}
		
		return tables;
	}
	
	public List<CBusinessClass> getFullDependentEntities(String mainEntity) {
		// TODO Auto-generated method stub
		List<CBusinessClass> tables = new ArrayList<CBusinessClass>();
		try {
		    
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			String query = "select id, name, data_reference,user_restrict from c_businessclass";
			ResultSet rs = st.executeQuery(query);
			List<CBusinessClass> alltables = new ArrayList<CBusinessClass>();
			
			while(rs.next()){
				CBusinessClass e = new CBusinessClass();
				e.setId(rs.getInt(1));
				e.setName(rs.getString(2));
				e.setDataReference(rs.getString(3));
				e.setUserRestrict(rs.getString(4).charAt(0));
				alltables.add(e);
			}
			rs.close();
			
			for(CBusinessClass t : alltables){
				String key="fk_"+t.getDataReference()+"__"+mainEntity;
				query = "select id from c_attribute where data_reference='"+key+"'";
				rs = st.executeQuery(query);
				if(rs.next())
					tables.add(t);
				rs.close();
			}
			
			st.close();
			
		} catch(Exception exc){
			
		}
		
		return tables;
	}

	public void loadLogo(String logoPath, int id) {
		// TODO Auto-generated method stub
		try {
		    
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    PreparedStatement ps = cnx.prepareStatement("select logo from s_mainwindow where id=?");
		    
		    ps.setInt(1, id);
		    ResultSet rs = ps.executeQuery();
		    InputStream is = null;
		    if(rs.next()){
		    	is = rs.getBinaryStream(1);
		    }
		    rs.close();
		    
		    File f = new File(logoPath);
		    if(!f.exists())
		    	f.createNewFile();
		    
		    byte[] data = org.apache.commons.io.IOUtils.toByteArray(is);
			FileOutputStream w = new FileOutputStream(f);
			w.write(data);
		    
		    w.close();
		    is.close();
		    
		    ps.close();
		    
		} catch(Exception exc){
			String dummy = ""+exc.getMessage();
			dummy=dummy+"";
		}
		   
	}

	public void loadReport(CDocumentbutton btn, String root) {
		// TODO Auto-generated method stub
		try {
		    
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    PreparedStatement ps = cnx.prepareStatement("select zip_content from m_document where id=?");
		    
		    ps.setInt(1, btn.getMDocument().getId());
		    ResultSet rs = ps.executeQuery();
		    InputStream is = null;
		    if(rs.next()){
		    	is = rs.getBinaryStream(1);
		    }
		    rs.close();
		    
		    File f = new File(root+".zip");
		    if(!f.exists())
		    	f.createNewFile();

		    byte[] data = org.apache.commons.io.IOUtils.toByteArray(is);
			FileOutputStream w = new FileOutputStream(f);
			w.write(data);
		    
		    w.close();
		    is.close();
		    
		    //	now unzip it
		    CompressionEngine.getInstance().unZipIt(root+".zip", root);
		    ps.close();
		    
		} catch(Exception exc){
			
		}
	}

	public InputStream loadResourceContent(int id) {
		// TODO Auto-generated method stub
		try {
		    
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    PreparedStatement ps = cnx.prepareStatement("select content from s_resource where id=?");
		    ps.setInt(1, id);
		    ResultSet rs = ps.executeQuery();
		    InputStream is = null;
		    if(rs.next()){
		    	is = rs.getBinaryStream(1);
		    }
		    rs.close();
		    
		    
		    ps.close();
		    
		    
		    return is;
		    
		} catch(Exception exc){
			String dummy = "";
			dummy=dummy+exc.getMessage();
		}
		return null;
	}

	public void saveContent(int id, InputStream is, int length) {
		// TODO Auto-generated method stub
		try {
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			PreparedStatement ps = cnx.prepareStatement("update s_resource set content=? where id=?");
			ps.setBinaryStream(1, is, length);
			ps.setInt(2, id);
			ps.executeUpdate();
			ps.close();
			
		} catch(Exception exc){
			String dummy = "";
			dummy=dummy+exc.getMessage();
		}
	}

	public List<SResource> loadResources(String appkey) {
		// TODO Auto-generated method stub
		List<SResource> resources = new ArrayList<SResource>();
		try{
		
		Connection cnx=ProtogenConnection.getInstance().getConnection();
		
		PreparedStatement ps = cnx.prepareStatement("select id, title, description, content from s_resource where appkey='"+appkey+"'");
		ResultSet rs = ps.executeQuery();
		
		while(rs.next()){
			SResource r = new SResource();
			r.setId(rs.getInt(1));
			r.setTitle(rs.getString(2));
			r.setDescription(rs.getString(3));
			r.setContent(rs.getBinaryStream(4));
			resources.add(r);
		}
		
		rs.close();
		ps.close();
		
		} catch(Exception exc){
			
		}
		return resources;
	}

	public List<String> getReferencedEntities(String table) {
		// TODO Auto-generated method stub
		List<String> dependencies = new ArrayList<String>();
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			Statement st = cnx.createStatement();
			
			String query = "select c_attribute.data_reference from c_attribute, c_businessclass where c_attribute.id_class = c_businessclass.id and c_businessclass.data_reference = '"+table+"' and c_attribute.data_reference like 'fk_%'";
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
				dependencies.add(rs.getString(1));
			rs.close();
			st.close();
			
		}catch(Exception exc){
			String dummy = exc.getMessage();
			dummy=dummy+"";
		}
		return dependencies;
	}

	public boolean checkAttributeInEntity(String entity, CAttribute a) {
		// TODO Auto-generated method stub
		boolean flag = false;
		
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			Statement st = cnx.createStatement();
		
			String sql = "select * from c_attribute a, c_businessclass b where a.id_class=b.id and a.data_reference='"+a.getDataReference()+"' and b.data_reference='"+entity+"'";
			ResultSet rs = st.executeQuery(sql);
			
			if(rs.next())
				flag=true;
			
			rs.close();
			st.close();
			
			
		}catch(Exception exc){
			String dummy = exc.getMessage();
			dummy=dummy+"";
		}
		
		return flag;
	}

	/*****************************************************************************************************************************
	 * 									ADMINISTRATION LOADER
	 * @param profils 
	 *****************************************************************************************************************************/
	public List<CoreUser> loadApplicationUsers(String appKey, List<CoreProfil> profils){
		List<CoreUser> users = new ArrayList<CoreUser>();
		
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "select u.*, r.id as roleid, r.role as rolename, r.logo, r.profil as cprof from core_user u, core_role r where r.id=u.\"idRole\" and r.appkey=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CoreUser u = new CoreUser();
				u.setId(rs.getInt("id"));
				u.setAdress(rs.getString("adress"));
				u.setAppKey(appKey);
				CoreRole r = new CoreRole();
				r.setId(rs.getInt("roleid"));
				r.setRole(rs.getString("rolename"));
				r.setLogoResKey(rs.getString("logo"));
				saveLogo(r);
				u.setCoreRole(r);
				u.setLastName(rs.getString("lastName"));
				u.setLogin(rs.getString("login"));
				u.setPassword(rs.getString("password"));
				u.setFirstName(rs.getString("firstName"));
				u.setTel(rs.getString("tel"));
				u.setProfil(new CoreProfil());
				for(CoreProfil p : profils){
					if(p.getId() == rs.getInt("cprof")){
						u.setProfil(p);
						break;
					}
				}
				users.add(u);
			}
			rs.close();
			ps.close();
			for(CoreUser user : users){
				/*
				 * Get data restrictions on role
				 */
				CoreRole r = user.getCoreRole();
				
				sql = "select entity, data_value from core_data_access_right where role="+r.getId();
				ps = cnx.prepareStatement(sql);
				rs = ps.executeQuery();
				r.setConstraints(new ArrayList<CoreDataAccessRight>());
				while (rs.next()){
					CoreDataAccessRight right = new CoreDataAccessRight();
					right.setEntity(new CBusinessClass());
					right.getEntity().setId(rs.getInt(1));
					right.setValue(rs.getInt(2));
					r.getConstraints().add(right);
				}
				
				rs.close();
				ps.close();
			}
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return users;
	}
	
	public void saveLogo(CoreRole r) {
		
		String sql = "select filename, content from s_resource_table where key=?";
		String file = "";
		byte[] content = null;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, r.getLogoResKey());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				file = rs.getString(1);
				content = rs.getBytes(2);
			}
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		r.setFileName(file);
		if(file == "" || content == null)
			return;
		
		String ext = file.substring(file.lastIndexOf('.'));
		
		String filelogo = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+
				"/logo/"+r.getLogoResKey()+ext;
		
		File f = new File(filelogo);
		
		if(f.exists())
			f.delete();
		
		try{
			FileOutputStream os = new FileOutputStream(f);
			IOUtils.write(content, os);
			os.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public List<CoreRole> loadUserRoles(String appKey, CoreUser user) {
		List<CoreRole> roles = loadApplicationRoles(appKey);
		List<CoreRole> rtkeep = new ArrayList<CoreRole>();
		rtkeep.add(user.getCoreRole());
		String sql = "select id_role from core_user_role where id_user=?";
		List<Integer> rids = new ArrayList<Integer>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				rids.add(new Integer(rs.getInt(1)));
			}
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(Integer id : rids){
			for(CoreRole r : roles){
				if(r.getId() == id.intValue()){
					rtkeep.add(r);
					break;
				}
			}
		}
		
		return rtkeep;
	}
	
	public List<CoreRole> loadApplicationRoles(String appKey, CoreProfil pr) {
			List<CoreRole> roles = new ArrayList<CoreRole>();
		
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "select r.* from core_role r where r.appkey=? and r.profil=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			ps.setInt(2, pr.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CoreRole r = new CoreRole();
				
				r.setId(rs.getInt("id"));
				r.setRole(rs.getString("role"));
				r.setsWindows(rs.getString("screens"));
				r.setBoundEntity(rs.getInt("user_bound"));
				r.setLogoResKey(rs.getString("logo"));
				
				roles.add(r);
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return roles;
	}
	
	public List<CoreRole> loadApplicationRoles(String appKey){
		List<CoreRole> roles = new ArrayList<CoreRole>();
		
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "select r.* from core_role r where r.appkey=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CoreRole r = new CoreRole();
				
				r.setId(rs.getInt("id"));
				r.setRole(rs.getString("role"));
				r.setsWindows(rs.getString("screens"));
				r.setBoundEntity(rs.getInt("user_bound"));
				r.setLogoResKey(rs.getString("logo"));
				
				roles.add(r);
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return roles;
	}
	
	public void updateApplicationUser(CoreUser user){
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			/*
			  id integer NOT NULL DEFAULT nextval('core_user_seq'::regclass),
			  "firstName" character varying(256),
			  "lastName" character varying(256),
			  tel character varying(32),
			  email character varying(32),
			  adress text,
			  login character varying(128) NOT NULL,
			  password character varying(256),
			  "idRole" integer NOT NULL,
			 */
			String sql = "update core_user set \"firstName\"=?, \"lastName\"=?, tel=?, email=?, adress=?, login=?, password=?, \"idRole\"=?, user_theme=?, options=?, theme_color=?, theme_style=? " +
					" Where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setString(1, user.getFirstName());
			ps.setString(2, user.getLastName());
			ps.setString(3,user.getTel());
			ps.setString(4, user.getEmail());
			ps.setString(5, user.getAdress());
			ps.setString(6, user.getLogin());
			ps.setString(7, user.getPassword());
			ps.setInt(8, user.getCoreRole().getId());
			ps.setString(9, user.getUserTheme());
			ps.setString(10, user.getSoptions());
			ps.setString(11, user.getThemeColor());
			ps.setString(12, user.getThemeStyle());
			ps.setInt(13, user.getId());
			
			ps.execute();
			
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public List<SMenuitem> loadOptions(String appKey, CoreUser user) {
		try {
			List<SMenuitem> results = new ArrayList<SMenuitem>();

			if(user.getCoreRole().getSoptions()==null || user.getCoreRole().getSoptions().length()==0)
				return results;
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			//	Get the parents
			ResultSet apprs = st.executeQuery("select * from s_menuitem m where m.\"isParent\"=true and appkey='"+appKey+"' and m.id in ("+user.getCoreRole().getSoptions().replaceAll(";", ",")+")" +
					" order by id asc");
			
			while (apprs.next()){
				SMenuitem item = new SMenuitem(apprs.getInt("id"), apprs.getString("title"), true);
				item.setRubrique(apprs.getInt("rubrique_id"));
				item.setAppKey(appKey);
				item.setSubs(new ArrayList<SMenuitem>());
				results.add(item);
			}
			
			
			apprs.close();
			st.close();
			
			return results;
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<String> loadInstalledOptions(CoreUser selectedUser) {
		List<String> res = new ArrayList<String>();
		if(selectedUser.getSoptions()==null || selectedUser.getSoptions().length()==0)
			return res;
		try {
			 Connection cnx=ProtogenConnection.getInstance().getConnection();
			 String sql = "select title from s_menuitem m where m.\"isParent\"=true and m.id in ("+selectedUser.getSoptions().replaceAll(";", ",")+")" +
					" order by id asc";
			 PreparedStatement ps = cnx.prepareStatement(sql);
			 
			 ResultSet apprs = ps.executeQuery();
			 while(apprs.next()){
				 res.add(apprs.getString(1));
			 }
			 apprs.close();
			 ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return res;
	}

	/*
	 * 		OCR
	 */
	public List<OCRDriverBean> loadDrivers(String appkey){
		
		List<OCRDriverBean> drivers = new ArrayList<OCRDriverBean>();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			 String sql ="select id, label, content from s_ocr_drvier where appkey=?";
			 
			 PreparedStatement ps = cnx.prepareStatement(sql);
			 ps.setString(1, appkey);
			 
			 ResultSet rs = ps.executeQuery();
			 while(rs.next()){
				 OCRDriverBean driver = new OCRDriverBean();
				 driver.setId(rs.getInt(1));
				 driver.setLabel(rs.getString(2));
				 driver.setStringContent(rs.getString(3));
				 XStream encoder = new XStream();
				 driver.setContent((Document)encoder.fromXML(driver.getStringContent()));
				 drivers.add(driver);
			 }
			 
			 rs.close();
			 ps.close();
			 
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return drivers;
	}

	/*
	 * 	ADMINISTRATION
	 */
	public List<COrganization> loadOrganizations(String appkey) {
		List<COrganization> results = new ArrayList<COrganization>();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = " select o.id as orgid, o.label as orglabel, e.data_reference as data_reference, e.name as name, e.id as entity_id "
					+ "from c_organization o, c_businessclass e "
					+ "where o.representative = e.id and o.appkey=?";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appkey);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				COrganization o = new COrganization();
				o.setId(rs.getInt(1));
				o.setLabel(rs.getString(2));
				
				CBusinessClass e = new CBusinessClass();
				e.setDataReference(rs.getString(3));
				e.setName(rs.getString(4));
				e.setId(rs.getInt(5));
				
				o.setRepresentative(e);
				results.add(o);
			}
			
			rs.close();
			ps.close();
			
			for(COrganization o : results){
				sql = "select instance from c_org_instance where organization=?";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, o.getId());
				List<Integer> ids = new ArrayList<Integer>();
				rs = ps.executeQuery();
				while(rs.next())
					ids.add(new Integer(rs.getInt(1)));
				
				rs.close();
				ps.close();
				
				ProtogenDataEngine eng = new ProtogenDataEngine();
				List<PairKVElement> instances =eng.getDataKeys(o.getRepresentative().getDataReference(), false, 0) ;
				o.setInstances(new ArrayList<PairKVElement>());
				
				for(PairKVElement el : instances){
					boolean flag = false;
					for(Integer i : ids)
						if(el.getDbID()==i.intValue()){
							flag=true;
							break;
						}
					if(!flag)
						o.getInstances().add(el);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}

	public void saveOrganization(int org, int inst) {

		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql ="insert into c_org_instance (organization, instance) values (?,?)";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1	, org);
			ps.setInt(2	, inst);
			
			ps.execute();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public COrganization loadOrganizationById(int id) {
		COrganization results = new COrganization();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql = " select o.id as orgid, o.label as orglabel, e.data_reference as data_reference, e.name as name, e.id as entity_id "
					+ "from c_organization o, c_businessclass e "
					+ "where o.representative = e.id and o.id=?";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			
			boolean found = false;
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				found = true;
				COrganization o = new COrganization();
				o.setId(rs.getInt(1));
				o.setLabel(rs.getString(2));
				
				CBusinessClass e = new CBusinessClass();
				e.setDataReference(rs.getString(3));
				e.setName(rs.getString(4));
				e.setId(rs.getInt(5));
				
				o.setRepresentative(e);
				results = o;
			}
			
			rs.close();
			ps.close();
			
			if(!found)		//	Mode super admin
				return results;
			
			sql = "select instance from c_org_instance where organization=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, results.getId());
			List<Integer> ids = new ArrayList<Integer>();
			rs = ps.executeQuery();
			while(rs.next())
				ids.add(new Integer(rs.getInt(1)));
			
			rs.close();
			ps.close();
			
			ProtogenDataEngine eng = new ProtogenDataEngine();
			List<PairKVElement> instances =eng.getDataKeys(results.getRepresentative().getDataReference(), false, 0) ;
			results.setInstances(new ArrayList<PairKVElement>());
			
			for(PairKVElement el : instances){
			for(Integer i : ids)
				if(Integer.parseInt(el.getKey())==i.intValue()){
					results.getInstances().add(el);
					break;
				}
				
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}
	
	public CParameterMetamodel loadParametersMetamodel(CBusinessClass ment) {
		CParameterMetamodel result = new CParameterMetamodel();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql ="SELECT c_metaparameters.id, c_metaparameters.label, c_metaparameters.organization "
					+ "FROM public.c_meta_entity_mapping, public.c_metaparameters "
					+ "WHERE c_meta_entity_mapping.metaparameters = c_metaparameters.id and c_meta_entity_mapping.businessclass=?";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, ment.getId());
			ResultSet rs = ps.executeQuery();
			
			
			if(rs.next()){
				result.setId(rs.getInt(1));
				result.setLabel(rs.getString(2));
				int idorg = rs.getInt(3);
				result.setOrganization(loadOrganizationById(idorg));
			} else {
				rs.close();
				ps.close();
				return null;
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

	public boolean existsParametersMetamodel(CBusinessClass ment) {
		boolean flag = false;
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql ="SELECT c_metaparameters.id, c_metaparameters.label, c_metaparameters.organization "
					+ "FROM public.c_meta_entity_mapping, public.c_metaparameters "
					+ "WHERE c_meta_entity_mapping.metaparameters = c_metaparameters.id and c_meta_entity_mapping.businessclass=?";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, ment.getId());
			ResultSet rs = ps.executeQuery();
				
			flag = rs.next();
			
			rs.close();
			ps.close();
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	

	/*
	 * 	Asgard Engine
	 */
	public List<CBusinessClass> loadAllEntities(String appkey) {
		String entQuery = "select * from c_businessclass where appkey='"+appkey+"'";
		List<CBusinessClass> entities = new ArrayList<CBusinessClass>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(entQuery);
			while(rs.next()){
				CBusinessClass e = new CBusinessClass();
				e.setId(rs.getInt("id"));
				e.setDataReference(rs.getString("data_reference"));
				e.setName(rs.getString("name"));
				e.setAppKey(appkey);
				entities.add(e);
			}
			rs.close();
			
			for(CBusinessClass e : entities){
				String query = "select * from c_attribute where id_class="+e.getId();
				rs = st.executeQuery(query);
				e.setAttributes(new ArrayList<CAttribute>());
				while(rs.next()){
					CAttribute a = new CAttribute();
					a.setId(rs.getInt("id"));
					a.setDataReference(rs.getString("data_reference"));
					a.setAttribute(rs.getString("attribute"));
					a.setCalculated(rs.getString("is_calculated").equals("Y"));
					a.setCAttributetype(new CAttributetype(rs.getInt("id_attributetype"),""));
					a.setEntity(e);
					a.setFormula(rs.getString("formula"));
					a.setKeyAttribute(rs.getInt("key_attribute")==1);
					a.setMandatory(rs.getString("mandatory").equals("Y"));
					a.setMultiple(rs.getString("multiple").equals("Y"));
					a.setReference(rs.getString("reference").equals("Y"));
					a.setRequiresValidation(rs.getString("requires_validation").equals("Y"));
					a.setValidationFormula(rs.getString("validation_formula"));
					
					e.getAttributes().add(a);
				}
			}
			st.close();
		}catch(Exception exc){
			
		}
		return entities;
	}

	public CAttribute loadAttributeById(int id) {
		
		CAttribute a = new CAttribute();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = ProtogenConnection.getInstance().getConnection();
		    String query = "select a.*, e.data_reference as table_reference from c_attribute a, c_businessclass e where a.id_class=e.id and a.id=?";
		    PreparedStatement ps = cnx.prepareStatement(query);
		    ps.setInt(1, id);
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
				a.setId(rs.getInt("id"));
				a.setDataReference(rs.getString("data_reference"));
				a.setAttribute(rs.getString("attribute"));
				a.setCalculated(rs.getString("is_calculated").equals("Y"));
				a.setCAttributetype(new CAttributetype(rs.getInt("id_attributetype"),""));
				a.setFormula(rs.getString("formula"));
				a.setKeyAttribute(rs.getInt("key_attribute")==1);
				a.setMandatory(rs.getString("mandatory").equals("Y"));
				a.setMultiple(rs.getString("multiple").equals("Y"));
				a.setReference(rs.getString("reference").equals("Y"));
				a.setRequiresValidation(rs.getString("requires_validation").equals("Y"));
				a.setValidationFormula(rs.getString("validation_formula"));
				CBusinessClass e = new CBusinessClass();
				e.setDataReference(rs.getString("table_reference"));
				a.setEntity(e);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return a;
	}

	public List<String> getKeyAttributes(String foreignTable) {
		QueryBuilder builder = new QueryBuilder(); 
		String keyQuery = builder.createSelectKeyAttributes(foreignTable);
		List<String> keyAttributes = new ArrayList<String>();
		try{
			
			Connection cnx=null;
			try{
				cnx=ProtogenConnection.getInstance().getConnection();
			}catch(Exception e){
				cnx = WebSessionManager.getInstance().getCnx();
				System.out.println("DATA BASE IN OFFLINE MODE");
			}
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(keyQuery);
			
			while(rs.next()){
				keyAttributes.add(rs.getString("data_reference"));				
			}
			rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return keyAttributes;
	}

	public CBusinessClass getEntityById(int id) {
		CBusinessClass en = new CBusinessClass();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "select data_reference from c_businessclass where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				en.setDataReference(rs.getString(1));
			rs.close();
			ps.close();
			en = this.getEntity(en.getDataReference());
		}catch(Exception e){
			e.printStackTrace();
		}
		return en;
	}

	public List<CWindow> loadWindows(String appKey) {
		String sql = "select id, title from c_window where appkey=?";
		List<CWindow> results = new ArrayList<CWindow>();
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CWindow w = new CWindow();
				w.setId(rs.getInt(1));
				w.setTitle(rs.getString(2));
				results.add(w);
			}
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		sql = "select a.id, a.attribute from c_attribute a, c_window_windowattribute wa "
				+ "where a.id=wa.id_attribute and wa.id_window=? "
				+ "and wa.visible=TRUE";
		for(CWindow w : results){
			w.setCAttributes(new ArrayList<CAttribute>());
			try{
				Connection cnx=ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, w.getId());
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					CAttribute a = new CAttribute();
					a.setId(rs.getInt(1));
					a.setAttribute(rs.getString(2));
					w.getCAttributes().add(a);
				}
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		return results;
	}
	
	//	For the global search
	public List<CWindow> loadFormWindows(String appKey) {
		String sql = "select id, title from c_window where appkey=? and id_windowtype=?";
		List<CWindow> results = new ArrayList<CWindow>();
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			ps.setInt(2, 2);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CWindow w = new CWindow();
				w.setId(rs.getInt(1));
				w.setTitle(rs.getString(2));
				results.add(w);
			}
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}

	public String parseType(CAttribute a) {
		if(a.getDataReference().startsWith("pk_"))
			return "PK";
		if(a.getDataReference().startsWith("fk_"))
			return a.getDataReference();
		
		switch(a.getCAttributetype().getId()){
		case 1:return "ENTIER";
		case 3:return "DATE";
		case 4:return "DOUBLE";
		default: return "TEXT";
		}
		
	}
	
	/*
	 * Data base fields and table
	 */
	
	public CBusinessClass getClassByName(String att){
		CBusinessClass a = new CBusinessClass ();
		String sql = "select id, data_reference from c_businessclass where name=?";
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, att);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				a.setId(rs.getInt(1));
				a.setName(att);
				a.setDataReference(rs.getString(2));
			}
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		return a;
	}
	
	public CAttribute getAttributeByName(String att, CBusinessClass classObjectif){
		CAttribute a = new CAttribute();
		String sql = "select id, data_reference from c_attribute where attribute=? and id_class=?";
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, att);
			ps.setInt(2, classObjectif.getId());
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				a.setId(rs.getInt(1));
				a.setAttribute(att);
				a.setDataReference(rs.getString(2));
			}
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		return a;
	}
	
	public List<String> getKeyAttributesReference(String table){
		List<String> res = new ArrayList<String>();
		QueryBuilder builder = new QueryBuilder();
		
		String sql = builder.createSelectKeyAttributes(table);
		
		try{
			Class.forName("org.postgresql.Driver");

			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next())
				res.add(rs.getString(1));
			
			rs.close();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return res;
	}

	/*
	 * IDENTIFICATION ROWS
	 */
	public CIdentificationRow chechForIdentificationRwSystem(String mainEntity) {
		
		CIdentificationRow res = new CIdentificationRow();
		
		//	Load entity
		CBusinessClass entity = this.getEntity(mainEntity);
		
		//	Get the first unirque row associated
		boolean exists = false;
		QueryBuilder builder = new QueryBuilder();
		String sql = builder.buildIdentificationRowLoad(entity);
		
		try{
			Class.forName("org.postgresql.Driver");

			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				res.setId(rs.getInt(1));
				CBusinessClass source = new  CBusinessClass();
				source = this.getEntityById(rs.getInt(2));
				res.setReference(entity);
				res.setSource(source);
				exists = true;
			}
			rs.close();
			ps.close();
			
			if(exists)
				return res;
		}catch(Exception exc){
			System.out.println("FETCH ROW IDENTIFICATION MECHANISM : \t");
			exc.printStackTrace();
		}
		
		return null;
	}
	
	public List<CoreUserRole> getRolesSec(CoreUser user){
		String sql = "select distinct r.id, r.role from core_user_role cur, core_role r where cur.id_user=?";
		List<CoreUserRole> results = new ArrayList<CoreUserRole>();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CoreUserRole cur = new CoreUserRole();
				cur.setCoreUser(user);
				CoreRole role = new CoreRole(rs.getInt(1), rs.getString(2));
				cur.setCoreRole(role);
				results.add(cur);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}	
	
	public CoreRole getRoleByID(int id){
		String sql = "select * from core_role where id=?";
		CoreRole r = new CoreRole(); 
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				r.setId(id);
				r.setRole(rs.getString(2));
				r.setsWindows(rs.getString(3));
				r.setsActions(rs.getString(4));
				r.setsDocuments(rs.getString(5));
				r.setsProcesses(rs.getString(6));
				//r.setAppKey(rs.getString(7));
				//r.setFormMode(rs.getString(8));
				r.setSoptions(rs.getString(9));
				r.setSuperadmin(rs.getBoolean(10));
				r.setBoundEntity(rs.getInt(11));
				r.setLogoResKey(rs.getString(12));
				r.setDescription(rs.getString(13));
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return r;
	}

	public boolean checkForIndependantData(CBusinessClass ent) {
		String sql = "select ent_id from g_org_independant_data where ent_id=?";
		boolean flag = false;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, ent.getId());
			ResultSet rs = ps.executeQuery();
			flag = rs.next();
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}

	@SuppressWarnings("rawtypes")
	public List<WindowModule> loadModules(String appKey) {
		List<WindowModule> modules=new ArrayList<WindowModule>();
		String sql = "select s.id as seqid, s.title as seqtitle,c.id as screenid,c.title as screentitle from c_window c, s_screensequence s where s.appkey=? and s.id=c.id_screen_sequence";
		Map<Integer,WindowModule> modulesMap= new HashMap<Integer, WindowModule>(); 
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appKey);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				int id=rs.getInt(1);
				CWindow window=new CWindow();
				window.setId(rs.getInt(3));
				window.setTitle(rs.getString(4));
				
				if(modulesMap.containsKey(new Integer(id))){
					modulesMap.get(new Integer(id)).getCWindows().add(window);
				}else{
					WindowModule w = new WindowModule();
					w.setId(id);
					w.setTitle(rs.getString(2));

					w.getCWindows().add(window);
					modulesMap.put(id,w);
					
				}
			}
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		for (Iterator iterator = modulesMap.values().iterator(); iterator.hasNext();) {
			WindowModule module = (WindowModule) iterator.next();
			modules.add(module);
		}
		return modules;
	}

	public OCRHistory loadHistoryRow(OCRDriverBean driver, int dbID) {
		OCRHistory bean = new OCRHistory();
		
		String sql = "select id, file_key from ged_ocr_history where bean_id=? and driver=?";
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, dbID);
			ps.setInt(2, driver.getId());
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				bean.setId(rs.getInt(1));
				bean.setFileKey(rs.getString(2));
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return bean;
	}

	public List<CSchedulableEntity> loadScheduledEntities(String appkey) {
		List<CSchedulableEntity> results = new ArrayList<CSchedulableEntity>();
		String sql = "select id, entity_id, attribute_id, window_id, end_attribute from c_scheduled_entity where entity_id in (select id from c_businessclass where appkey=?)";
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, appkey);
			ResultSet rs = ps.executeQuery();
			while (rs.next()){
				CSchedulableEntity e = new CSchedulableEntity();
				e.setId(rs.getInt(1));
				CBusinessClass entity = this.getEntityById(rs.getInt(2));
				e.setEntity(entity);
				int ida = rs.getInt(3);
				for(CAttribute a : entity.getAttributes())
					if(a.getId() == ida){
						e.setFromAttribute(a);
						break;
					}
				int idTo = rs.getInt(5);
				if(idTo > 0){
					for(CAttribute a : entity.getAttributes())
						if(a.getId() == idTo){
							e.setToAttribute(a);
							break;
						}
				} else
					e.setToAttribute(new CAttribute());
				CWindow w = new CWindow();w.setId(rs.getInt(4));e.setWindow(w);
				results.add(e);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}

	public String checkForAlphaTable(String mainEntity) {
		String dataReference="";
		String template = "fk_"+mainEntity+"__user_%";
		
		String sql = "select data_reference from c_attribute where data_reference like '"+template+"'";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				dataReference = rs.getString(1).split("__")[1];
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return dataReference;
	}

	public void updateDataRights(CoreUser user) {
		for(CoreDataAccessRight dar : user.getCoreRole().getConstraints()){
			CBusinessClass e = this.getEntityById(dar.getEntity().getId());
			dar.setEntity(e);
			
		}
		
	}

	public boolean isMailWindow(CWindow window) {
		boolean mail = false;
		String sql = "select mail_store from c_window where id="+window.getId();
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				mail = rs.getString(1).equals("Y");
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return mail;
	}

	private static final String EMAIL_PATTERN = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	public List<MailDTO> loadMails(ListKV row, int id) {
		List<MailDTO> results = new ArrayList<MailDTO>();
		List<MailDTO> selectedMails = new ArrayList<MailDTO>();
		/*String pop = "";
		String smtp = "";
		String hostIn = "";
		String hostOut = "";
		String login = "";
		String password = "";
		boolean found = false;
		try{
			String sql = "select pop, smtp, login, pass, host from c_user_mailconfig where user_id=?";
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				found = true;
				pop = rs.getString(1);
				smtp = rs.getString(2);
				login = rs.getString(3);
				password = rs.getString(4);
				hostIn = rs.getString(5);
			}
			rs.close();
			ps.close();
			if(found)
				results = (new EmailManager()).loadEmails(hostIn,pop,login, password);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		*/
		String skey = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY");

		ApplicationCache cache = ApplicationRepository.getInstance().getCache(skey);
		
		int i = 1;
		for(MUserMail m : cache.getMails().getContenu()){
			MailDTO d = new MailDTO();
			d.setContent(m.getContenu());
			d.setId(i);
			d.setCorrespondant(m.getCorrespondant());
			d.setInBox(m.isEntrant());
			d.setSentOn(m.getDateMessage());
			d.setTitle(m.getSujet());
			results.add(d);
			i++;
		}
		
		
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		
		List<String> potentialAdresses = new ArrayList<String>();
		
		for(String v : row.getValue()){
			Matcher matcher = pattern.matcher(v);
			if(matcher.matches())
				potentialAdresses.add(v);
		}
		for(MailDTO m : results){
			for(String a : potentialAdresses){
				if(m.getCorrespondant().contains(a))
					selectedMails.add(m);
			}
		}
		return selectedMails;
	}

	public MailCache loadMailCache(CoreUser user) {
		MailCache cache = new MailCache();
		
		String sql = "select id, dateMaj, taille from m_mail_cache where user_id=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ResultSet rs = ps.executeQuery();
			boolean flag = false;
			if(rs.next()){
				flag = true;
				cache.setId(rs.getInt(1));
				cache.setDerniereMAJ(rs.getDate(2));
				cache.setTaille(rs.getInt(3));
				cache.setContenu(new ArrayList<MUserMail>());
				cache.setUtilisateur(user);
			}
			rs.close();
			ps.close();
			
			if(!flag)
				return cache;
			
			sql = "select id, sujet, correspondant, dateMail, content, sense from m_user_mail where cache_id=? LIMIT "+cache.getTaille();
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, cache.getId());
			rs = ps.executeQuery();
			while(rs.next()){
				MUserMail m = new MUserMail();
				m.setId(rs.getInt(1));
				m.setSujet(rs.getString(2));
				m.setCorrespondant(rs.getString(3));
				m.setDateMessage(rs.getDate(4));
				m.setContenu(rs.getString(5));
				m.setEntrant(rs.getString(6).equals("I"));
				m.setUtilisateur(user);
				cache.getContenu().add(m);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return cache;
	}

	public CUserMailConfig loadMailConfig(CoreUser usr) {
		
		CUserMailConfig cfg = new CUserMailConfig();
		cfg.setUser(usr);
		
		String sql = "select id, host, pop, smtp, login, pass from c_user_mailconfig where user_id=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, usr.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				cfg.setId(rs.getInt(1));
				cfg.setImap(rs.getString(4));
				cfg.setImapPort(rs.getString(3));
				cfg.setLogin(rs.getString(5));
				cfg.setPassword(rs.getString(6));
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return cfg;
	}

	public void saveMailConfig(CoreUser selectedUser, CUserMailConfig mailCfg) {
		String sql = "insert into c_user_mailconfig (host, pop, inbox, smtp, login, pass, user_id) values "
				+ "(?,?,?,?,?,?,?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, mailCfg.getImap());
			ps.setString(2, mailCfg.getImap());
			ps.setString(3, "INBOX");
			ps.setString(4, mailCfg.getImap());
			ps.setString(5, mailCfg.getLogin());
			ps.setString(6, mailCfg.getPassword());
			ps.setInt(7, selectedUser.getId());
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public void updateMailConfig(CoreUser selectedUser, CUserMailConfig mailCfg) {
		String sql = "update c_user_mailconfig set host=?, pop=?, inbox=?, smtp=?, login=?, pass=? where user_id=? ";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, mailCfg.getImap());
			ps.setString(2, mailCfg.getImap());
			ps.setString(3, "INBOX");
			ps.setString(4, mailCfg.getImap());
			ps.setString(5, mailCfg.getLogin());
			ps.setString(6, mailCfg.getPassword());
			ps.setInt(7, selectedUser.getId());
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public List<CAttribute> getFilterAttributes(CWindow window) {
		List<CAttribute> results = new ArrayList<CAttribute>();
		String sql = "select id_attribute from c_window_windowattribute where id_window=? and filter_enabled='Y'";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, window.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CAttribute a = new CAttribute();
				a.setId(rs.getInt(1));
				results.add(a);
			}
			rs.close();
			ps.close();
			
			sql = "select attribute, id_attributetype, key_attribute, reference, multiple, id_class, data_reference from c_attribute "
					+ " where id=?";
			for(CAttribute a : results){
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, a.getId());
				rs = ps.executeQuery();
				if(rs.next()){
					a.setAttribute(rs.getString(1));
					a.setCAttributetype(new CAttributetype());
					a.getCAttributetype().setId(rs.getInt(2));
					a.setKeyAttribute(rs.getInt(3)==1);
					a.setReference(rs.getString(4).equals("Y"));
					a.setMultiple(rs.getString(5).equals("Y"));
					CBusinessClass en = this.getEntityById(rs.getInt(6));
					a.setEntity(en);
					a.setDataReference(rs.getString(7));
				}
				rs.close();
				ps.close();
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return results;
	}
	
	public boolean references(String mainEntity, String dataReference) {
		boolean result = false;
		String sql = "select data_reference from c_attribute a where id_class in (select id from c_businessclass where data_reference =?)";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, mainEntity);
			ResultSet rs = ps.executeQuery();
					
			while(rs.next()){
				if(rs.getString(1).equals("fk_"+dataReference)){
					result = true;
					break;
				}
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return result;
	}
	
	public boolean embeds(String mainEntity, String dataReference) {
		boolean result = false;
		String sql = "select data_reference from c_attribute a where id_class in (select id from c_businessclass where data_reference =?)";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, mainEntity);
			ResultSet rs = ps.executeQuery();
					
			while(rs.next()){
				if(rs.getString(1).equals("fk_"+dataReference+"__"+mainEntity)){
					result = true;
					break;
				}
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return result;
	}

	public List<CWindowCallout> loadCallouts(CWindow window) {
		List<CWindowCallout> results = new ArrayList<CWindowCallout>();
		String sql = "select wc.id, wc.callout_id, wc.c_type, wc.arg_map, c.nom  from c_window_callout wc, c_callout c "
				+ " where c.id = wc.callout_id and wc.window_id=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, window.getId());
			ResultSet rs = ps.executeQuery();
					
			while(rs.next()){
				CWindowCallout c = new CWindowCallout();
				c.setArguments(new ArrayList<CWindowCalloutArgument>());
				c.setId(rs.getInt(1));
				c.setCallout(new CCallout());
				c.getCallout().setId(rs.getInt(2));
				c.setType(rs.getInt(3));
				c.setJsonArg(rs.getString(4));
				c.getCallout().setNom(rs.getString(5));
				results.add(c);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		for(CWindowCallout c : results){
			sql = "select nom, arguments from c_callout where id=?";
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, c.getCallout().getId());
				ResultSet rs = ps.executeQuery();
						
				while(rs.next()){
					c.getCallout().setNom(rs.getString(1));
					c.getCallout().setJsonArguments(rs.getString(2));
					List<CCalloutArguments> args = new JSONDeserializer<List<CCalloutArguments>>().deserialize(c.getCallout().getJsonArguments());
					c.getCallout().setArgs(args);
				}
				rs.close();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
			c.setArguments(new ArrayList<CWindowCalloutArgument>());
			sql = "select callout_arg, prompt_opt, selection_opt, created_opt, attribute_id from c_win_call_argument where window_callout_id=?";
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, c.getId());
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					CWindowCalloutArgument a = new CWindowCalloutArgument();
					for(CCalloutArguments ca : c.getCallout().getArgs())
						if(ca.getLibelle().equals(rs.getString(1))){
							a.setArgument(ca);
							break;
						}
					a.setPrompt(rs.getString(2).equals("Y"));
					a.setSelection(rs.getString(3).equals("Y"));
					a.setCreated(rs.getString(4).equals("Y"));
					if(rs.getObject(5)!= null){
						for(CAttribute t : window.getCAttributes())
							if(t.getId() == rs.getInt(5)){
								a.setAttribute(t);
								break;
							}
					}
					c.getArguments().add(a);
				}
				rs.close();
				ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
		return results;
	}

	/**
	 * Loading potential synthesis window
	 * @param window corresponding window
	 * @return 
	 */
	public CListViewSynthesis loadSLVModel(CWindow window) {
		CListViewSynthesis result = new CListViewSynthesis();
		result.setWindow(window);
		String sql = "select id, s_title, s_expression from c_window_synthesis where id_window=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, window.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				result.setId(rs.getInt(1));
				result.setLibelle(rs.getString(2));
				result.setExpression(rs.getString(3));
			}
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(result.getId()>0)
			ExpressionParserEngine.getInstance().buildSynthesisWindow(result, window);
		return result;
	}

	/**
	 * checking if the screen is attached to any root folders
	 * @param id windo id
	 * @return root folders count
	 */
	public int existsFolders(int id) {
		int count = 0;
		
		String sql = "select count(id) from c_window_folder where id_window=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				count = rs.getInt(1);
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return count;
	}

	/**
	 * LOADING ROOT FOLDER FOR STORABLE WINDOW
	 * @param id WINDOW ID
	 * @return ROOT FOLDER
	 */
	public CFolder loadRootFolder(int id) {
		CFolder root = new CFolder();
		
		String sql = "select id, folder_name, folder_description from c_folder where is_root='Y' and id in (select f.id_folder from c_window_folder f where id_window=?)";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				root.setId(rs.getInt(1));
				root.setName(rs.getString(2));
				root.setDescription(rs.getString(3));
			}
				
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return root;
	}

	/**
	 * Count the number of subfolders
	 * @param id parent ID
	 * @return sub folders count
	 */
	public int countSubfolders(int id) {
		int count = 0;
		
		String sql = "select count(id) from c_folder where id_parent=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				count = rs.getInt(1);
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * Loading the subfolders
	 * @param id parent folder id
	 * @return list of subfolders
	 */
	public List<CFolder> loadSubfolders(int id) {
		List<CFolder> subs = new ArrayList<CFolder>();
		
		String sql = "select id, folder_name, folder_description from c_folder where id_parent=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CFolder f = new CFolder();
				f.setId(rs.getInt(1));
				f.setName(rs.getString(2));
				f.setDescription(rs.getString(3));
				subs.add(f);
			}
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return subs;
	}

	/**
	 * Loading documents of a specific document
	 * @param fid folder id 
	 * @param wid window id
	 * @param dbId database row id
	 * @return
	 */
	public List<RowDocument> loadDocuments(int fid, int wid, int dbId) {
		List<RowDocument> docs = new ArrayList<RowDocument>();
		String sql = "select id, file_name, file_extension, date_creation, file_version, file_signature, text_content, storage_identifier, storage_callout_id, id_type, id_user "
				+ "from row_document where id_folder=? and id_window=? and id_row=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, fid);
			ps.setInt(2, wid);
			ps.setInt(3, dbId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				RowDocument d = new RowDocument();
				d.setId(rs.getInt(1));
				d.setName(rs.getString(2));
				d.setExtension(rs.getString(3));
				d.setCreation(rs.getDate(4));
				d.setVersion(rs.getInt(5));
				d.setSignature(rs.getString(6));
				d.setTextContent(rs.getString(7));
				d.setStorageIdentifier(rs.getString(8));
				d.setStoreCallout(new CCallout());
				d.getStoreCallout().setId(rs.getInt(9));
				d.setType(new CDocumentType());
				d.getType().setId(rs.getInt(10));
				d.setUser(new CoreUser());
				d.getUser().setId(rs.getInt(11));
				docs.add(d);
			}
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(RowDocument d : docs){
			sql = "select type_label from c_document_type where id=?";
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, d.getType().getId());
				ResultSet rs = ps.executeQuery();
				if(rs.next())
					d.getType().setLabel(rs.getString(1));
				rs.close();
				ps.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			sql = "select \"firstName\", \"lastName\" from core_user where id=?";
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, d.getUser().getId());
				ResultSet rs = ps.executeQuery();
				if(rs.next()){
					d.getUser().setFirstName(rs.getString(1));
					d.getUser().setLastName(rs.getString(2));
				}
				rs.close();
				ps.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return docs;
	}

}