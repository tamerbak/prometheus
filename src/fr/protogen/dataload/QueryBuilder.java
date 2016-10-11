package fr.protogen.dataload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.model.SUIAlert;
import fr.protogen.asgard.model.AgregationFunction;
import fr.protogen.asgard.model.ResultTable;
import fr.protogen.asgard.model.VisitingDimension;
import fr.protogen.connector.model.SearchClause;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.control.ui.MtmBlock;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.HeaderExecutionResult;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.StringFormat;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.engine.utils.UIFilterElement;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CIdentificationRow;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.MPostAction;

public class QueryBuilder {
	
	private List<String> collumntables = new ArrayList<String>();
	private Map<String, String> mtmMapping;
	private List<String> mtmTables = new ArrayList<String>();
	private List<String> mtmRelatedTables = new ArrayList<String>();
	
	public String buildCountQuery(CWindow window,
			List<UIFilterElement> filters, CoreUser u,
			List<String> parameters) {
		String sql = "select count(pk_"+window.getMainEntity()+") from "+window.getMainEntity();
		
		//		WHERE
		List<String> junctions = new ArrayList<String>();
		
		ConstraintFactory cf = new ConstraintFactory();
		ApplicationLoader dal = new ApplicationLoader();
		
		String modpars = "";
		for(String p : parameters)
			modpars = modpars+p+" OR ";
		if(modpars.length()>1){
			modpars = "("+modpars.substring(0,modpars.length()-4)+")";
			junctions.add(modpars);
		}
		
		String orgconstraint=cf.viewOrganizationConstraint(u, window.getMainEntity(),true,(modpars.length()>1));
		if(orgconstraint!= null && orgconstraint.length()>0)
			junctions.add(orgconstraint);
		
		//	Add filters from same entity
		for(UIFilterElement f : filters){
			if(!f.getEntity().getDataReference().equals(window.getMainEntity()))
				continue;
			
			if(f.isMultiple()){
				String pk = "pk_"+f.getAttribute().getDataReference().split("__")[1];
				String q = pk+" in (select "+f.getAttribute().getDataReference()+
						" from "+f.getAttribute().getDataReference().split("__")[0].substring(3)+
						" where pk_"+f.getAttribute().getDataReference().split("__")[0].substring(3)+"="+f.getControlValue()+")";
				
				junctions.add(" ( "+q+" ) ");
				continue;
			}
			
			//	Reference
			if(f.getAttribute().getDataReference().startsWith("fk_"))
				junctions.add(f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+"="+f.getControlValue());
			else if (f.getAttribute().getCAttributetype().getId() == 2){
			//	String
				if(f.getLthan() != null && f.getLthan().length()>0){
					junctions.add(f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" like '%"+f.getLthan()+"%'");
				}
				
			} else {
			//	Number or date
				if(f.getLthan() != null && f.getLthan().length()>0){
					junctions.add(f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" = '"+f.getLthan()+"'");
				}
			}
		}
		
		// Add filters from referenced entities
		for(UIFilterElement f : filters){
			if(!dal.references(window.getMainEntity(), f.getAttribute().getEntity().getDataReference()))
				continue;
			//	Reference
			if(f.getAttribute().getDataReference().startsWith("fk_"))
				junctions.add(window.getMainEntity()+".fk_"+f.getAttribute().getEntity().getDataReference()+
						" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
						f.getAttribute().getDataReference()+"="+
						f.getControlValue()+" )");
			else if (f.getAttribute().getCAttributetype().getId() == 2){
			//	String
				if(f.getLthan() != null && f.getLthan().length()>0){
					junctions.add(window.getMainEntity()+".fk_"+f.getAttribute().getEntity().getDataReference()+
							" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
							f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" like '%"+f.getLthan()+"%'"+" )");
				}
				
			} else {
			//	Number or date
				if(f.getLthan() != null && f.getLthan().length()>0){
					junctions.add(window.getMainEntity()+".fk_"+f.getAttribute().getEntity().getDataReference()+
							" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
							f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" = '"+f.getLthan()+"'"+" )");
					
				}
			}
		}
		
		// Add filters from embedded entities
		for(UIFilterElement f : filters){
			if(!dal.embeds(window.getMainEntity(), f.getAttribute().getEntity().getDataReference()))
				continue;
			//	Reference
			if(f.getAttribute().getDataReference().startsWith("fk_"))
				junctions.add("pk_"+window.getMainEntity()+
						" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
						f.getAttribute().getDataReference()+"="+
						f.getControlValue()+" )");
			else if (f.getAttribute().getCAttributetype().getId() == 2){
			//	String
				if(f.getLthan() != null && f.getLthan().length()>0){
					junctions.add("pk_"+window.getMainEntity()+
							" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
							f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" like '%"+f.getLthan()+"%'"+" )");
				}
				
			} else {
			//	Number or date
				if(f.getLthan() != null && f.getLthan().length()>0){
					junctions.add("pk_"+window.getMainEntity()+
							" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
							f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" = '"+f.getLthan()+"'"+" )");
					
				}
			}
		}
		
		for(String c : window.getConstraints())
			junctions.add(c);
			
		junctions.add(window.getMainEntity()+".dirty='N'");
		if(junctions.size() > 0){
			sql = sql+" WHERE "+junctions.get(0);
			for(int i = 1 ; i < junctions.size() ; i++){
				sql = sql+" AND "+junctions.get(i);
			}
		}
		
		return sql;
	}
	
	public Map<String, String> buildSelectQuery(CWindow window, List<UIFilterElement> filters, CoreUser u, 
			List<String> parameters, int currentPage, int pagesCount){
		
		String query = "";
		
		Map<String, String> queries = new HashMap<String, String>();
		List<CAttribute> attributes = window.getCAttributes();
		
		//String mainentity = window.getMainEntity();
		
		ApplicationLoader dal = new ApplicationLoader();
		
		//CBusinessClass e = dal.getEntity(mainentity);
		
		List<String> tables = new ArrayList<String>();
		mtmTables = new ArrayList<String>();
		mtmRelatedTables = new ArrayList<String>();
		mtmMapping = new HashMap<String, String>();
		
		//	FROM Clause
		for(CAttribute attribute : attributes){
			//	We will not show multiple related  attributes on list view screens
			if(attribute.isMultiple() || (attribute.getCAttributetype().getId()==6) || (attribute.isIndirectReference())
					|| !attribute.getEntity().getDataReference().equals(window.getMainEntity()))
				continue;
			
			String tableReference = attribute.getEntity().getDataReference();
			
			boolean found = false;
			for(String table : tables){
				if(table.equals(tableReference)){
					found=true;
					break;
				}
					
			}
			if(!found){
				tables.add(tableReference);
			}
		}
		
		
		
		// SELECT
		List<String> attributeReferences = new ArrayList<String>();
		for(CAttribute attribute : attributes){
			if(attribute.isMultiple() || !attribute.getEntity().getDataReference().equals(window.getMainEntity()) || (attribute.getCAttributetype().getId()==6) || (attribute.isIndirectReference()))
				continue;
			if(attribute.isVisible()){
				attributeReferences.add(attribute.getEntity().getDataReference()+"."+attribute.getDataReference());
				collumntables.add(attribute.getEntity().getDataReference());
			}
		}
		
		//	WHERE
		List<String> junctions = new ArrayList<String>();
		for(CAttribute attribute : attributes){
			if(attribute.isMultiple() || (attribute.getCAttributetype().getId()==6) || (attribute.isIndirectReference()))
				continue;
			if(attribute.getDataReference().startsWith("fk_")){
				String referenced = attribute.getDataReference().substring(3);
				if(referenced.equals(window.getMainEntity())){
					//	reflexive join
					//String whereclause = referenced+"_reflexed.pk_"+referenced+"="+attribute.getEntity().getDataReference()+"."+attribute.getDataReference();
					if(!tables.contains(referenced+" "+referenced+"_reflexed"))
						tables.add(referenced+" "+referenced+"_reflexed");
					//junctions.add(whereclause);
				} /*else {
					String whereclause = referenced+".pk_"+referenced+"="+attribute.getEntity().getDataReference()+"."+attribute.getDataReference();
					if(!tables.contains(referenced))
						tables.add(referenced);
					junctions.add(whereclause);
				}*/
				
			}
		}
		
		ConstraintFactory cf = new ConstraintFactory();
		
		
		String modpars = "";
		for(String p : parameters)
			modpars = modpars+p+" OR ";
		if(modpars.length()>1){
			modpars = "("+modpars.substring(0,modpars.length()-4)+")";
			junctions.add(modpars);
		}
		
		String orgconstraint=cf.viewOrganizationConstraint(u, window.getMainEntity(),true,(modpars.length()>1));
		if(orgconstraint!= null && orgconstraint.length()>0)
			junctions.add(orgconstraint);
		
		for(String c : window.getConstraints())
			junctions.add(c);
		
			//	Add filters from same entity
			for(UIFilterElement f : filters){
				if(!f.getEntity().getDataReference().equals(window.getMainEntity()))
					continue;
				
				if(f.isMultiple()){
					String pk = "pk_"+f.getAttribute().getDataReference().split("__")[1];
					String q = pk+" in (select "+f.getAttribute().getDataReference()+
							" from "+f.getAttribute().getDataReference().split("__")[0].substring(3)+
							" where pk_"+f.getAttribute().getDataReference().split("__")[0].substring(3)+"="+f.getControlValue()+")";
					
					junctions.add(" ( "+q+" ) ");
					continue;
				}
				
				//	Reference
				if(f.getAttribute().getDataReference().startsWith("fk_"))
					junctions.add(f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+"="+f.getControlValue());
				else if (f.getAttribute().getCAttributetype().getId() == 2){
				//	String
					if(f.getLthan() != null && f.getLthan().length()>0){
						junctions.add("LOWER("+f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+") like LOWER('%"+f.getLthan()+"%')");
					}
					
				} else {
				//	Number or date
					if(f.getLthan() != null && f.getLthan().length()>0){
						junctions.add(f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" = '"+f.getLthan()+"'");
					}
				}
			}
			
			// Add filters from referenced entities
			for(UIFilterElement f : filters){
				if(!dal.references(window.getMainEntity(), f.getAttribute().getEntity().getDataReference()))
					continue;
				//	Reference
				if(f.getAttribute().getDataReference().startsWith("fk_"))
					junctions.add(window.getMainEntity()+".fk_"+f.getAttribute().getEntity().getDataReference()+
							" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
							f.getAttribute().getDataReference()+"="+
							f.getControlValue()+" )");
				else if (f.getAttribute().getCAttributetype().getId() == 2){
				//	String
					if(f.getLthan() != null && f.getLthan().length()>0){
						junctions.add(window.getMainEntity()+".fk_"+f.getAttribute().getEntity().getDataReference()+
								" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where LOWER("+
								f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+") like LOWER('%"+f.getLthan()+"%'"+") )");
					}
					
				} else {
				//	Number or date
					if(f.getLthan() != null && f.getLthan().length()>0){
						junctions.add(window.getMainEntity()+".fk_"+f.getAttribute().getEntity().getDataReference()+
								" in (select pk_"+f.getAttribute().getEntity().getDataReference()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
								f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" = '"+f.getLthan()+"'"+" )");
						
					}
				}
			}
			
			// Add filters from embedded entities
			for(UIFilterElement f : filters){
				if(!dal.embeds(window.getMainEntity(), f.getAttribute().getEntity().getDataReference()))
					continue;
				//	Reference
				if(f.getAttribute().getDataReference().startsWith("fk_"))
					junctions.add("pk_"+window.getMainEntity()+
							" in (select fk_"+f.getAttribute().getEntity().getDataReference()+"__"+window.getMainEntity()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
							f.getAttribute().getDataReference()+"="+
							f.getControlValue()+" )");
				else if (f.getAttribute().getCAttributetype().getId() == 2){
				//	String
					if(f.getLthan() != null && f.getLthan().length()>0){
						junctions.add("pk_"+window.getMainEntity()+
								" in (select fk_"+f.getAttribute().getEntity().getDataReference()+"__"+window.getMainEntity()+" from "+f.getAttribute().getEntity().getDataReference()+" where LOWER("+
								f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+") like LOWER('%"+f.getLthan()+"%'"+") )");
					}
					
				} else {
				//	Number or date
					if(f.getLthan() != null && f.getLthan().length()>0){
						junctions.add("pk_"+window.getMainEntity()+
								" in (select fk_"+f.getAttribute().getEntity().getDataReference()+"__"+window.getMainEntity()+" from "+f.getAttribute().getEntity().getDataReference()+" where "+
								f.getAttribute().getEntity().getDataReference()+"."+f.getAttribute().getDataReference()+" = '"+f.getLthan()+"'"+" )");
						
					}
				}
			}
		
		//	Build query
		
		query = "SELECT "+attributeReferences.get(0)+" ";
		for(int i = 1 ; i < attributeReferences.size() ; i++){
			query = query+", "+attributeReferences.get(i)+" ";
		}
		String mainTable =tables.get(0);
		query = query+" FROM "+mainTable; 
		for(int i = 1 ; i < tables.size() ; i++){
			String tbl = tables.get(i);
			String alias = tables.get(i);
			if(tables.get(i).split(" ").length>1){
				tbl=tables.get(i).split(" ")[0];
				alias=tables.get(i).split(" ")[1];
				
			}
			query = query+" left join "+tables.get(i)+" on "+mainTable+".fk_"+tbl+"="+alias+".pk_"+tbl;
		}
		junctions.add(window.getMainEntity()+".dirty='N'");
		if(junctions.size() > 0){
			query = query+" WHERE "+junctions.get(0);
			for(int i = 1 ; i < junctions.size() ; i++){
				query = query+" AND "+junctions.get(i);
			}
		}
		
		
		query = query+" order by "+window.getMainEntity()+".pk_"+window.getMainEntity()+" desc";
		query = query+ " OFFSET "+(currentPage-1)*10+" LIMIT 10 ";
		queries.put(window.getMainEntity(), query);
		
		return queries;
	}

	public String generatePostActionQuery(MPostAction action, double value, String entity, int ID){
		String query = "";
		
		if(action.getType().getLabel().equals("SAVE")){
			//	CREATE NEW ROW
			query = "insert into ";
			String vals="("+ID+",";
			
			//	DETECT TARGET TABLE
			String targetTable = action.getAttributes().get(0).getEntity().getDataReference();
			query = query+targetTable+" (fk_"+entity+",";
			for(int i = 0; i < action.getAttributes().size()-1 ; i++){
				CAttribute attribute = action.getAttributes().get(i);
				query = query+attribute.getDataReference()+",";
				
				String val = parseDefaultValue(action.getDefaultValues().get(i), value);
				vals = vals+val+",";
			}
			String att = action.getAttributes().get(action.getAttributes().size()-1).getDataReference();
			String val = parseDefaultValue(action.getDefaultValues().get(action.getAttributes().size()-1),value);
			query = query+att+") values "+vals+val+")";
			
		}
		
		return query;
	}
	private String parseDefaultValue(String key, double value) {
		// TODO Auto-generated method stub
		
		if(key.equals("#target_value#")){
			return value+"";
		} else if(key.equals("YEAR_NOW")){
			//	Default value is current year
			java.util.Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			
			return calendar.get(Calendar.YEAR)+"";
		} else if(key.equals("MONTH_NOW")){
			//	Default value is current month
			java.util.Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return calendar.get(Calendar.MONTH)+"";
		}
		
		return null;
	}

	public List<String> getTables() {
		return collumntables;
	}

	public void setTables(List<String> tables) {
		this.collumntables = tables;
	}

	

	public String buildSelectQuery(CWindow window, int dbID) {
		// TODO Auto-generated method stub
		String query = "";
		
		List<CAttribute> attributes = window.getCAttributes();
		
		
		List<String> tables = new ArrayList<String>();
		
		
		
		//	FROM Clause
		for(CAttribute attribute : attributes){
			if(attribute.isMultiple())
				continue;
			if(attribute.getDataReference().startsWith("fk_") && attribute.getDataReference().contains("__"))
				continue;
			String tableReference = attribute.getEntity().getDataReference();
			boolean found = false;
			for(String table : tables){
				if(table.equals(tableReference)){
					found=true;
					break;
				}
					
			}
			if(!found){
				tables.add(tableReference);
				
			}
		}
		
		
		
		// SELECT
		List<String> attributeReferences = new ArrayList<String>();
		for(CAttribute attribute : attributes){
			if(attribute.isMultiple())
				continue;
			if(attribute.isVisible() && attribute.getCAttributetype().getId()!=6){
					attributeReferences.add(attribute.getEntity().getDataReference()+"."+attribute.getDataReference());
					collumntables.add(attribute.getEntity().getDataReference());
				
			}
		}
		
		//	WHERE
		List<String> junctions = new ArrayList<String>();
		for(CAttribute attribute : attributes){
			if(attribute.getDataReference().startsWith("fk_") && !attribute.isMultiple()){
				
				if(attribute.getDataReference().substring(3).equals(window.getMainEntity()))		//	Reflexive
					continue;
				
				String referenced = attribute.getDataReference().substring(3);
				String whereclause = referenced+".pk_"+referenced+"="+attribute.getEntity().getDataReference()+"."+attribute.getDataReference();
				if(!tables.contains(referenced))
					tables.add(referenced);
				junctions.add(whereclause);
			}
		}
		
		//	Build query
		
		query = "SELECT "+attributeReferences.get(0)+" ";
		for(int i = 1 ; i < attributeReferences.size() ; i++){
			query = query+", "+attributeReferences.get(i)+" ";
		}
		
		query = query+" FROM "+tables.get(0);
		for(int i = 1 ; i < tables.size() ; i++){
			query = query+" LEFT JOIN "+tables.get(i)+" on "+tables.get(0)+".fk_"+tables.get(i)+"="+tables.get(i)+".pk_"+tables.get(i)+" ";
		}
		
		query = query+" WHERE "+window.getMainEntity()+".pk_"+window.getMainEntity()+"="+dbID;
		
		/*if(junctions.size() > 0){
			query = query+" WHERE "+junctions.get(0);
			for(int i = 1 ; i < junctions.size() ; i++){
				query = query+" AND "+junctions.get(i);
			}
			query = query+" AND "+window.getMainEntity()+".pk_"+window.getMainEntity()+"="+dbID;

		} else {
			query = query+" Where "+window.getMainEntity()+".pk_"+window.getMainEntity()+"="+dbID;
		}
		*/
		return query;
	}
	public Map<String, String> buildSelectQueryWithAlias(CWindow window, int dbID) {
		// TODO Auto-generated method stub
		String query = "";
		
		List<CAttribute> attributes = window.getCAttributes();
		
		List<String> tables = new ArrayList<String>();
		
		Map<String, List<String>> tablesWithAttr = new HashMap<String, List<String>>();
		
		//	FROM Clause
		for(CAttribute attribute : attributes){
			if(attribute.isMultiple())
				continue;
			if(attribute.getDataReference().startsWith("fk_") && attribute.getDataReference().contains("__"))
				continue;
			String tableReference = attribute.getEntity().getDataReference();
			boolean found = false;
			for(String table : tables){
				if(table.equals(tableReference)){
					found=true;
					break;
				}
					
			}
			if(!found){
				tables.add(tableReference);
				
			}
		}
		
		
		
		// SELECT
		List<String> attributeReferences = new ArrayList<String>();
		List<String> attributesForTableReferences = new ArrayList<String>();
		
		for(CAttribute attribute : attributes){
			if(attribute.isMultiple())
				continue;
			if(attribute.getEntity().getDataReference().equals(window.getMainEntity()) &&attribute.isVisible() && attribute.getCAttributetype().getId()!=6){
					String alias = attribute.getEntity().getDataReference()+"___"+attribute.getDataReference();
					if(alias.length()>60)
						alias = alias.substring(0,60);
					attributeReferences.add(attribute.getEntity().getDataReference()+"."+attribute.getDataReference()+ " as "+alias);
					collumntables.add(attribute.getEntity().getDataReference());
				
			} else if(!attribute.getEntity().getDataReference().equals(window.getMainEntity()) &&attribute.isVisible() && attribute.getCAttributetype().getId()!=6) {
				String alias = attribute.getEntity().getDataReference()+"___"+attribute.getDataReference();
				if(alias.length()>60)
					alias = alias.substring(0,60);
				attributesForTableReferences.add(attribute.getEntity().getDataReference()+"."+attribute.getDataReference()+ " as "+alias);
				if(tablesWithAttr.get(attribute.getEntity().getDataReference()) == null) {
					tablesWithAttr.put(attribute.getEntity().getDataReference(), new ArrayList<String>());
				}
				tablesWithAttr.get(attribute.getEntity().getDataReference()).add(attribute.getEntity().getDataReference()+"."+attribute.getDataReference()+ " as "+alias);
				
			}
		}
		
		//	WHERE
		List<String> junctions = new ArrayList<String>();
		for(CAttribute attribute : attributes){
			if(attribute.getDataReference().startsWith("fk_") && !attribute.isMultiple()){
				
				if(attribute.getDataReference().substring(3).equals(window.getMainEntity()))		//	Reflexive
					continue;
				
				String referenced = attribute.getDataReference().substring(3);
				String whereclause = referenced+".pk_"+referenced+"="+attribute.getEntity().getDataReference()+"."+attribute.getDataReference();
				if(!tables.contains(referenced))
					tables.add(referenced);
				junctions.add(whereclause);
			}
		}
		
		//	Build query
		
		query = "SELECT "+attributeReferences.get(0)+" ";
		for(int i = 1 ; i < attributeReferences.size() ; i++){
			query = query+", "+attributeReferences.get(i)+" ";
		}
		
		query = query+" FROM "+tables.get(0);
//		for(int i = 1 ; i < tables.size() ; i++){
//			query = query+" LEFT JOIN "+tables.get(i)+" on "+tables.get(0)+".fk_"+tables.get(i)+"="+tables.get(i)+".pk_"+tables.get(i)+" ";
//		}
		query = query+" WHERE "+window.getMainEntity()+".pk_"+window.getMainEntity()+"="+dbID;
		
		
		Map<String, String> queries = new HashMap<String,String>();
		queries.put(window.getMainEntity(), query);
		for(String table : tablesWithAttr.keySet()){
			String q = "SELECT pk_"+table;
			for(String attr : tablesWithAttr.get(table)) {
				q+=", "+attr;
			}
			q+= " FROM "+table;
			q+=" WHERE pk_"+table+"=";
			
			queries.put(table, q);
		}
		
		
		
		/*if(junctions.size() > 0){
			query = query+" WHERE "+junctions.get(0);
			for(int i = 1 ; i < junctions.size() ; i++){
				query = query+" AND "+junctions.get(i);
			}
			query = query+" AND "+window.getMainEntity()+".pk_"+window.getMainEntity()+"="+dbID;

		} else {
			query = query+" Where "+window.getMainEntity()+".pk_"+window.getMainEntity()+"="+dbID;
		}
		*/
		return queries;
	}

	public String buildInsertQuery(String table, Map<String, String> vals, CoreUser u, int paramID) {
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		String dateInsert = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
		
		String query = "INSERT INTO "+table+" ";
		String labels ="";
		String values ="";
		
		for(String label : vals.keySet()){
			if(vals.get(label) == null || vals.get(label).equals(""))
				continue;
			labels = labels+","+label;
			values = values+",'"+vals.get(label)+"'";
		}
		
		if(labels == null || labels.length()==0)
			return "";
		
		labels = labels.substring(1);
		values = values.substring(1);
		
		labels = "("+labels+",protogen_user_id";
		values = "("+values+","+u.getOrgInstance()+"";
		
		labels = labels+",created";
		values = values+",'"+dateInsert+"'";
		
		labels = labels+",parametered_for)";
		values = values+","+paramID+")";
		
		
		query = query+labels+" VALUES "+values;
		
		return query;
	}
	
	public List<String> buildUpdateQuery(CWindow window,
			List<UIControlElement> controls, int ID, Map<String, String> foreignKeys, List<MtmDTO> dtos, List<MtmBlock> refBlocks) {
		// TODO Auto-generated method stub
		List<String> queries = new ArrayList<String>();
		
		//	Get all references
		Map<String, String> references = new HashMap<String, String>();
		for(UIControlElement c : controls){
			if(c.isReference() &&  c.getControlValue() != null && !c.getControlValue().equals("0") && !c.getControlValue().equals("") && c.getAttribute().getId()>0)
				references.put(c.getAttribute().getDataReference(), c.getControlValue());
		}
		
		//	manage main entity
		String query = "UPDATE "+window.getMainEntity()+" SET ";
		for(UIControlElement element : controls){
			if(element.getAttribute().getId()==0)
				continue;
			if(element.isBinaryContent())
				continue;
			if(element.isReadOnly())
				continue;
			if(element.getAttribute().getEntity().getDataReference().equals(window.getMainEntity()) && !element.isReference()){
				String queryPart;
				if(element.getAttribute().getCAttributetype().getId()==5){
					//	Type heure
					String h = element.getControlValue().split(":")[0]+element.getControlValue().split(":")[1];
					queryPart = element.getAttribute().getDataReference()+"="+h+",";
				} else if (element.isCtrlDate()) {
					Calendar c = Calendar.getInstance();
					c.setTime(element.getDateValue());
					String value = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
					if(value.equals("0000-00-00 00:00:00+00"))
						continue;
					queryPart = element.getAttribute().getDataReference()+"='"+value+"',";
					//vals.put(element.getAttribute().getDataReference(), "2014-09-24 00:00:00+00");
					
				}
				else{
					String value;
					if(element.getAttribute().getCAttributetype().getId()==7)
						value = StringFormat.getInstance().format(element.getTrueValue());
					else
						value = StringFormat.getInstance().format(element.getControlValue());
					queryPart = element.getAttribute().getDataReference()+"='"+value+"',";
					
				}
				query = query+queryPart;
			}
		}
		
		for(CAttribute a : window.getCAttributes()){
			if(a.getEntity().getDataReference().equals(window.getMainEntity())){
				for(String key : references.keySet()){
					if(a.getDataReference().equals(key.replace("pk_", "fk_")))
						if(!references.get(key).equals(""))
							query = query+a.getDataReference()+"="+references.get(key)+",";
				}
			}
		}
		
		UIControlElement e = controls.get(0);
		if(e.getAttribute().getId()==0){
			query=query+" parametered_for="+e.getControlValue();
		} else		
			query = query.substring(0, query.length()-1);
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		String dateUpdate = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
		
		query = query+", updated='"+dateUpdate+"' ";
		
		query = query+" WHERE pk_"+window.getMainEntity()+"='"+ID+"'";
		
		queries.add(query);
		
		//	get related tables
		List<String> ftables = new ArrayList<String>();
		for(UIControlElement element : controls){
			if(element.getAttribute().getId()==0)
				continue;
			
			
			if(!element.getAttribute().getEntity().getDataReference().equals(window.getMainEntity()) && !ftables.contains(element.getAttribute().getEntity().getDataReference())){
				ftables.add(element.getAttribute().getEntity().getDataReference());
			}
		}
		if(refBlocks != null) {
			for(MtmBlock block : refBlocks) {
				String ftable = block.getEntity().getDataReference();
				String foreignID = foreignKeys.get("fk_"+ftable);
				String fquery = "UPDATE "+ftable+" SET ";
				
				for(UIControlElement element : block.getControls()) {
					String label = element.getAttribute().getDataReference();
					String value = StringFormat.getInstance().format(element.getControlValue());
					if(value.equals("0000-00-00 00:00:00+00"))
						continue;
					if(element.getAttribute().getCAttributetype().getId() == 4
							&& (value==null || value.equals("")))
						value="0.0";
					
					value = StringFormat.getInstance().format(value);
					
					fquery = fquery+" "+label+"='"+value+"',";
				}
				
				for(CAttribute a : window.getCAttributes()){
					if(a.getEntity().getDataReference().equals(ftable)){
						for(String key : references.keySet()){
							if(a.getDataReference().equals(key.replace("pk_", "fk_")))
								fquery = fquery+a.getDataReference()+"="+references.get(key)+",";
						}
					}
				}
				
				fquery = fquery.substring(0, fquery.length()-1);
				fquery = fquery+" WHERE pk_"+ftable+"='"+foreignID+"'";
				queries.add(fquery);
				
			}
		} else {
			for(String ftable : ftables){
				boolean flag = false;
				for(String key : references.keySet()){
					if(key.equals("pk_"+ftable)){
						flag = true;
					}
				}
				if(flag)
					continue;
				String foreignID = foreignKeys.get("fk_"+ftable);
				String fquery = "UPDATE "+ftable+" SET ";
				for(UIControlElement element : controls){
					String ctrlTable = element.getAttribute().getEntity().getDataReference();
					if(ctrlTable.equals(ftable)){
						String label = element.getAttribute().getDataReference();
						String value = StringFormat.getInstance().format(element.getControlValue());
						if(value.equals("0000-00-00 00:00:00+00"))
							continue;
						if(element.getAttribute().getCAttributetype().getId() == 4
								&& (value==null || value.equals("")))
							value="0.0";
						value = StringFormat.getInstance().format(value);
						fquery = fquery+" "+label+"='"+value+"',";
					}
				}
				
				for(CAttribute a : window.getCAttributes()){
					if(a.getEntity().getDataReference().equals(ftable)){
						for(String key : references.keySet()){
							if(a.getDataReference().equals(key.replace("pk_", "fk_")))
								fquery = fquery+a.getDataReference()+"="+references.get(key)+",";
						}
					}
				}
				
				fquery = fquery.substring(0, fquery.length()-1);
				fquery = fquery+" WHERE pk_"+ftable+"='"+foreignID+"'";
				queries.add(fquery);
			}
		}
		//	let's delete all mtm rows
		
		for(MtmDTO dto : dtos){
			queries.add("delete from "+dto.getMtmEntity().getDataReference()+" where fk_"+dto.getMtmEntity().getDataReference()+"__"+window.getMainEntity()+"='"+ID+"'");
		}
		//	now we shall recreate them
		for(MtmDTO dto : dtos){
			for(Map<CAttribute, Object> datarow : dto.getMtmData()){
				String mtmQuery = "insert into "+dto.getMtmEntity().getDataReference()+" ";
				String mlabel="(fk_"+dto.getMtmEntity().getDataReference()+"__"+window.getMainEntity()+",";
				String mvalue="("+ID+",";
				for(CAttribute a : datarow.keySet()){
					if(datarow.get(a).equals("0000-00-00 00:00:00+00"))
						continue;
					if((a.getCAttributetype().getId() == 4 || a.getCAttributetype().getId()==8)
							&& (datarow.get(a)==null || datarow.get(a).equals(""))){
						mlabel=mlabel+a.getDataReference()+",";
						mvalue=mvalue+"'0.0',";
						continue;
					}
					mlabel=mlabel+a.getDataReference()+",";
					String value = datarow.get(a).toString();
					value = StringFormat.getInstance().format(value);
					mvalue=mvalue+"'"+value+"',";
				}
				mlabel = mlabel.substring(0,mlabel.length()-1)+")";
				mvalue = mvalue.substring(0,mvalue.length()-1)+")";
				mtmQuery = mtmQuery+mlabel+" values "+mvalue;
				queries.add(mtmQuery);
			}
		}
		return queries;
	}
	
	public List<String> buildUpdateQueryWithReftables(CWindow window,
			List<UIControlElement> controls, int ID, Map<String, String> foreignKeys,  List<MtmDTO> dtos) {
		// TODO Auto-generated method stub
		List<String> queries = new ArrayList<String>();
		
		//	Get all references
		Map<String, String> references = new HashMap<String, String>();
		for(UIControlElement c : controls){
			if(c.isReference() &&  c.getControlValue() != null && !c.getControlValue().equals("0") && !c.getControlValue().equals("") && c.getAttribute().getId()>0)
				references.put(c.getAttribute().getDataReference(), c.getControlValue());
		}
		
		//	manage main entity
		String query = "UPDATE "+window.getMainEntity()+" SET ";
		for(UIControlElement element : controls){
			if(element.getAttribute().getId()==0)
				continue;
			if(element.isBinaryContent())
				continue;
			if(element.isReadOnly())
				continue;
			if(element.getAttribute().getEntity().getDataReference().equals(window.getMainEntity()) && !element.isReference()){
				String queryPart;
				if(element.getAttribute().getCAttributetype().getId()==5){
					//	Type heure
					String h = element.getControlValue().split(":")[0]+element.getControlValue().split(":")[1];
					queryPart = element.getAttribute().getDataReference()+"="+h+",";
				} else if (element.isCtrlDate()) {
					Calendar c = Calendar.getInstance();
					c.setTime(element.getDateValue());
					String value = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
					queryPart = element.getAttribute().getDataReference()+"='"+value+"',";
					//vals.put(element.getAttribute().getDataReference(), "2014-09-24 00:00:00+00");
					
				}
				else{
					String value;
					if(element.getAttribute().getCAttributetype().getId()==7)
						value = StringFormat.getInstance().format(element.getTrueValue());
					else
						value = StringFormat.getInstance().format(element.getControlValue());
					queryPart = element.getAttribute().getDataReference()+"='"+value+"',";
					
				}
				query = query+queryPart;
			}
		}
		
		for(CAttribute a : window.getCAttributes()){
			if(a.getEntity().getDataReference().equals(window.getMainEntity())){
				for(String key : references.keySet()){
					if(a.getDataReference().equals(key.replace("pk_", "fk_")))
						if(!references.get(key).equals(""))
							query = query+a.getDataReference()+"="+references.get(key)+",";
				}
			}
		}
		
		UIControlElement e = controls.get(0);
		if(e.getAttribute().getId()==0){
			query=query+" parametered_for="+e.getControlValue();
		} else		
			query = query.substring(0, query.length()-1);
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		String dateUpdate = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
		
		query = query+", updated='"+dateUpdate+"' ";
		
		query = query+" WHERE pk_"+window.getMainEntity()+"='"+ID+"'";
		
		queries.add(query);
		
		//	get related tables
		List<String> ftables = new ArrayList<String>();
		for(UIControlElement element : controls){
			if(element.getAttribute().getId()==0)
				continue;
			
			
			if(!element.getAttribute().getEntity().getDataReference().equals(window.getMainEntity()) && !ftables.contains(element.getAttribute().getEntity().getDataReference())){
				ftables.add(element.getAttribute().getEntity().getDataReference());
			}
		}
		
		for(String ftable : ftables){
			boolean flag = false;
			for(String key : references.keySet()){
				if(key.equals("pk_"+ftable)){
					flag = true;
				}
			}
			if(flag)
				continue;
			String foreignID = foreignKeys.get("fk_"+ftable);
			String fquery = "UPDATE "+ftable+" SET ";
			for(UIControlElement element : controls){
				String ctrlTable = element.getAttribute().getEntity().getDataReference();
				if(ctrlTable.equals(ftable)){
					String label = element.getAttribute().getDataReference();
					String value = StringFormat.getInstance().format(element.getControlValue());
					fquery = fquery+" "+label+"='"+value+"',";
				}
			}
			
			for(CAttribute a : window.getCAttributes()){
				if(a.getEntity().getDataReference().equals(ftable)){
					for(String key : references.keySet()){
						if(a.getDataReference().equals(key.replace("pk_", "fk_")))
							fquery = fquery+a.getDataReference()+"="+references.get(key)+",";
					}
				}
			}
			
			fquery = fquery.substring(0, fquery.length()-1);
			fquery = fquery+" WHERE pk_"+ftable+"='"+foreignID+"'";
			queries.add(fquery);
		}
		
		//	let's delete all mtm rows
		
		for(MtmDTO dto : dtos){
			queries.add("delete from "+dto.getMtmEntity().getDataReference()+" where fk_"+dto.getMtmEntity().getDataReference()+"__"+window.getMainEntity()+"='"+ID+"'");
		}
		//	now we shall recreate them
		for(MtmDTO dto : dtos){
			for(Map<CAttribute, Object> datarow : dto.getMtmData()){
				String mtmQuery = "insert into "+dto.getMtmEntity().getDataReference()+" ";
				String mlabel="(fk_"+dto.getMtmEntity().getDataReference()+"__"+window.getMainEntity()+",";
				String mvalue="("+ID+",";
				for(CAttribute a : datarow.keySet()){
					mlabel=mlabel+a.getDataReference()+",";
					mvalue=mvalue+"'"+datarow.get(a).toString()+"',";
				}
				mlabel = mlabel.substring(0,mlabel.length()-1)+")";
				mvalue = mvalue.substring(0,mvalue.length()-1)+")";
				mtmQuery = mtmQuery+mlabel+" values "+mvalue;
				queries.add(mtmQuery);
			}
		}
		return queries;
	}

	public String buildLastInsertedQuery(String table) {
		// TODO Auto-generated method stub
		return "SELECT pk_"+table+" FROM "+table+" ORDER BY pk_"+table+" DESC";
	}

	public Map<String, String> getMtmMapping() {
		return mtmMapping;
	}

	public void setMtmMapping(Map<String, String> mtmMapping) {
		this.mtmMapping = mtmMapping;
	}

	public List<String> getMtmTables() {
		return mtmTables;
	}

	public void setMtmTables(List<String> mtmTables) {
		this.mtmTables = mtmTables;
	}

	public List<String> getMtmRelatedTables() {
		return mtmRelatedTables;
	}

	public void setMtmRelatedTables(List<String> mtmRelatedTables) {
		this.mtmRelatedTables = mtmRelatedTables;
	}

	public List<String> buildDeleteQuery(CWindow window, String rowID,
			Map<String, String> foreignKeys, List<String> mtmTables2) {
		List<String> queries = new ArrayList<String>();
		String q = "update "+window.getMainEntity()+" set dirty='Y' where pk_"+window.getMainEntity()+"="+rowID;
		queries.add(q);
		return queries;
//		//	Let's start with mtmTables
//		if(mtmTables2!=null){
//			for(String table : mtmTables2){
//				String query = "DELETE FROM "+table+" WHERE fk_"+table+"__"+window.getMainEntity()+"='"+rowID+"'";
//				queries.add(query);
//			}
//		}
//		//Cette partie est en commentaire pour empêcher de supprimer les enregistrements en cascade
//		/*String tableQuery = "SELECT c_businessclass.data_reference as table_ref FROM public.c_businessclass, public.c_attribute WHERE c_attribute.id_class = c_businessclass.id and c_attribute.data_reference='fk_"+window.getMainEntity()+"'";
//		
//		try{
//			Class.forName("org.postgresql.Driver");
//
//		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
//		    
//			Statement st = cnx.createStatement();
//			ResultSet rs = st.executeQuery(tableQuery);
//			while(rs.next()){
//				String nquery = "delete from "+rs.getString(1)+" where fk_"+window.getMainEntity()+"='"+rowID+"'";
//				queries.add(nquery);
//			}
//		}catch(Exception exc){
//			
//		}*/
//		
//		//	Now main entity
//		String query = "DELETE FROM "+window.getMainEntity()+" WHERE pk_"+window.getMainEntity()+"='"+rowID+"'";
//		queries.add(query);
//		
//		return queries;
		
	}
	
	public String buildSelectAlert(UUID identifier) {
		// TODO Auto-generated method stub
		return "select * from s_alert where alert_access_id='"+identifier+"'";
	}

	public String builCodeQuery(int programId) {
		// TODO Auto-generated method stub
		return "SELECT code FROM m_action WHERE id='"+programId+"'";
	}

	public String buildCreateUIAlert(SUIAlert alert) {
		// TODO Auto-generated method stub
		int isnew = (alert.isNewInstance())?1:0;
		return "insert into s_ui_alert (window_id, constraint, isNew, identifier) values ('"+alert.getWindow().getId()+"', '"+alert.getConstraint()+"', '"+isnew+"', '"+alert.getIdentifier()+"')";
	}

	public String buildSelectUIAlert(UUID identifier) {
		// TODO Auto-generated method stub
		
		return "select identifier, outputData from s_ui_alert where identifier='"+identifier+"'";
	}

	public String buildDeleteUIAlert(String identifier) {
		
		return "delete from s_ui_alert where identifier='"+identifier+"'";
	}

	public String buildSelectAllAlerts() {
		// TODO Auto-generated method stub
		return "select * from s_alert";
	}

	public String buildSelectAllUIAlerts() {
		// TODO Auto-generated method stub
		
		String query = "SELECT   s_ui_alert.created,   s_ui_alert.constraint,   s_ui_alert.outputData,   s_ui_alert.isNew,   s_ui_alert.identifier,   c_window.id,   c_window.title,   c_window.stepDescription,   c_window.percentage,   c_window.helpVideo,   c_window.id_windowtype,   c_window.id_screen_sequence,   c_businessclass.id,   c_businessclass.data_reference,   c_businessclass.name " +
						"FROM   public.s_ui_alert,   public.c_window,   public.c_businessclass " +
						"WHERE   s_ui_alert.window_id = c_window.id AND  c_window.id_entity = c_businessclass.id ";
		
		return query;
	}
	
	/*
	 * 			MTM TOOLS EXTENSION
	 */

	public String buildGetMtmQuery(CBusinessClass entityReference) {
		// TODO Auto-generated method stub
		return "SELECT entity.id, entity.data_reference as entityDF, entity.name, attr.data_reference FROM 	c_businessclass entity, c_attribute attr WHERE 	entity.id = attr.id_class AND 	entity.data_reference like 'user_mtm_%' AND 	attr.data_reference = 'fk_"+entityReference.getDataReference()+"'";
	}

	public String buildGetMtmRefQuery(String reference) {
		// TODO Auto-generated method stub
		
		return "select * from c_businessclass where data_reference='"+reference+"'";
	}

	public String buildEntityPopulateQuery(int id) {
		// TODO Auto-generated method stub
		return "select * from c_attribute where id_class='"+id+"'";
	}

	public List<String> getMtmInsertQueries(CWindow window, int lastInserted,
			List<MtmDTO> dtos) {
		// TODO Auto-generated method stub
		List<String> queries = new ArrayList<String>();
		for(MtmDTO dto : dtos){
			for(Map<CAttribute, Object> datarow : dto.getMtmData()){
				String mtmQuery = "insert into "+dto.getMtmEntity().getDataReference()+" ";
				String mlabel="(fk_"+dto.getMtmEntity().getDataReference()+"__"+window.getMainEntity()+",";
				String mvalue="('"+lastInserted+"',";
				for(CAttribute a : datarow.keySet()){
					mlabel=mlabel+a.getDataReference()+",";
					String type = a.getCAttributetype().getType();
					if((type.equals("Double") || type.equals("Date")) && (datarow.get(a).toString().equals("")||datarow.get(a).toString().equals("0000-00-00 00:00:00+00"))){
						mvalue=mvalue + "null" + ",";
					}
					else{
						mvalue=mvalue+"'"+datarow.get(a).toString()+"',";
					}
				}
				mlabel = mlabel.substring(0,mlabel.length()-1)+")";
				mvalue = mvalue.substring(0,mvalue.length()-1)+")";
				mtmQuery = mtmQuery+mlabel+" values "+mvalue;
				queries.add(mtmQuery);
			}
		}
		return queries;
	}

	public String createSelectDataKeyByID(String tableReference, int idRef, List<String> attributes) {
		// TODO Auto-generated method stub
		String query = "Select ";
		for(String a : attributes){
			query = query+a+",";
		}
		query = query.substring(0, query.length()-1);
		query = query+" from "+tableReference+" where pk_"+tableReference+"='"+idRef+"'" ;
		return query;
	}
	
	public String createSelectKeyAttributes(String table){
		return "select a.data_reference, id_attributetype from c_attribute a, c_businessclass b where a.key_attribute=1 and a.id_class = b.id and b.data_reference='"+table+"'";
	}

	public String createSelectDataKeys(String referenceTable,
			List<String> keyAttributes, boolean restrict, int iduser, List<String> parameters) {

		String query = "Select pk_"+referenceTable+", ";
		for(String a : keyAttributes){
			query = query+a+",";
		}
		query = query.substring(0, query.length()-1);
		String where=" where "+referenceTable+".dirty='N' ";
		ApplicationCache cache=null;
		try{
			cache=ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		}catch(Exception exc){
			System.out.println("NO APPLICATION REPOSITORY : MOST LIKELY IN WEB SERVICE MODE");
		}
		try{
		
		
		ConstraintFactory fact = new ConstraintFactory();
		String constr="";
		
		
		if(parameters != null && parameters.size()>0){
			if(where.length()==0)
				where = " where (";
			else
				where =where+" and (";
			String w="";
			for(String p : parameters){
				w=w+" ("+p+") or ";
			}
			if(w.length()>1);{
				w=w.substring(0,w.length()-4);
				w = w+")";
			}
			where = where+w;
		}
		
		
		if(cache != null)
			constr = fact.viewOrganizationConstraint(cache.getUser(), referenceTable,true,(parameters != null && parameters.size()>0));
		
		if(constr.length()>0){
			if(where.length()==0)
				where = " where "+constr;
			else
				where = where+" and "+constr;
		}
		
		}catch(Exception exc){
			System.out.println("Out of session");
			exc.printStackTrace();
		}
		

		
		
		//query = query+" from "+referenceTable +where+" order by pk_"+referenceTable+" asc "+(restrict?" limit 1000 ":"");
		query = query+" from "+referenceTable +where+" order by pk_"+referenceTable+" asc limit 50 ";
		
		System.out.println("[GENERIUM][QUERY BUILDER] Query : \n\t"+query+"\n");
		
		return query;
	}

	public String generatePostActionQuery(MPostAction action, String entity, int ID,
			Map<String, String> postActionParameters, Map<String, String> processParameters) {
		// TODO Auto-generated method stub
		String query = "";
		
		if(action.getType().getLabel().equals("SAVE")){
			//	CREATE NEW ROW
			query = "insert into ";
			String vals="(";
			
			//	DETECT TARGET TABLE
			String targetTable = action.getAttributes().get(0).getEntity().getDataReference();
			query = query+targetTable+" (";
			for(int i = 0; i < action.getAttributes().size() ; i++){
				
				CAttribute attribute = action.getAttributes().get(i);
				
				if(attribute.getDataReference().startsWith("pk_") || attribute.isMultiple() || !attribute.getEntity().getDataReference().equals(targetTable) || attribute.isAutoValue())
					continue;
				
				
				if(action.getParametersValues().get(i) != null && action.getParametersValues().get(i).length()>0){
					query = query+attribute.getDataReference()+",";
					String val;
					String key = action.getParametersValues().get(i);
					key = key.replaceAll("<<", "");
					key = key.replaceAll(">>", "");
					if(processParameters.containsKey(key))
						val = processParameters.get(key);
					else if(postActionParameters.containsKey(key))
						val = postActionParameters.get(key);
					else if(postActionParameters.containsKey(StringFormat.getInstance().attributeDataReferenceFormat(key)))
						val = postActionParameters.get(StringFormat.getInstance().attributeDataReferenceFormat(key));
					
					else
						val="0";
					
					if(action.getPrefixes().size()>i && action.getPrefixes().get(i)!=null && action.getPrefixes().get(i).length()>0)
						val = action.getPrefixes().get(i)+" "+val;
					
					if(attribute.getCAttributetype().getId() == 3){
						String dateTable [] = val.split("/");
						try{
							int day = Integer.parseInt(dateTable[0]);
							int month = Integer.parseInt(dateTable[1]);
							int year = Integer.parseInt(dateTable[2]);
							val = year+"-"+month+"-"+day+" 00:00:00+00";
						}catch(Exception exc){
							exc.printStackTrace();
						}
					}
					
					//	check if it is a data reference key
					if(!val.equals("0") && !key.startsWith("#")){
						String datareference = attribute.getDataReference();
						if(datareference.startsWith("fk_") && !key.startsWith("resultat_")){
							String table = datareference.substring(3);
							ProtogenDataEngine engine = new ProtogenDataEngine();
							List<PairKVElement> listElements = engine.getDataKeys(table, false,0);
							for(PairKVElement e : listElements){
								if(e.getValue().equals(val)){
									val = e.getKey();
									break;
								}
							}
						} else if(datareference.startsWith("fk_") || attribute.getCAttributetype().getId()==7){
							double dtemp = Double.parseDouble(val);
							int ival = (int)dtemp;
							val = ival+"";
						} 
					}
					vals = vals+"'"+val+"',";
				}
				
			}
			
			if(vals.length()>0)
				vals = vals.substring(0,vals.length()-1);
			query = query.substring(0,query.length()-1);
			
			query = query+") values "+vals+")";
			
		} else {
			String targets = "";
			
			for(int i = 0; i <action.getAttributes().size();i++){
				
				if(action.getParametersValues().get(i).startsWith("<<resultat_")){
					String k = action.getParametersValues().get(i).replaceAll("<<", "");
					k = k.replaceAll(">>", "");
					String v = postActionParameters.get(k);
					
					targets=" "+targets+action.getAttributes().get(i).getDataReference()+"='"+v+"' ";
					
				}
			}
				
			query = "update "+entity+" set "+targets+" where pk_"+entity+"="+ID;
		}
		
		return query;
	}
	
	public List<String> buildPostActionMtmQuery(HeaderExecutionResult her, MPostAction action, int mtmID, int index){
		List<String> sql= new ArrayList<String>();
		
		String table="";
		String calculatedAttribute = "";
		String idTarget="";
		String idReference="";
		for(String parameter : action.getParametersValues()){
			if(parameter.equals(StringFormat.getInstance().attributeDataReferenceFormat("<<"+her.getVariable()+">>"))){
				CAttribute a = action.getAttributes().get(action.getParametersValues().indexOf(parameter));
				if(a.isMultiple()){
					table = a.getDataReference().split("__")[0].substring(3);
					idTarget = a.getDataReference();
					break;
				}
			}
		}
		
		ApplicationLoader dal = new ApplicationLoader();  
		
		//	Load table attribute
		CBusinessClass entity = dal.getEntity(table); 
		
		
		//	Select the value attribute
		for(CAttribute a : entity.getAttributes()){
			if(a.getCAttributetype().getId()==4)
				calculatedAttribute = a.getDataReference();
			if(a.isReference() && !a.isMultiple())
				idReference = a.getDataReference();
		}
		
		//	Construct the query
		Map<Integer, Double> vals = her.getValues().get(index);
		for(Integer i : vals.keySet()){
			String q = "insert into "+entity.getDataReference()+" ("+idTarget+","+idReference+","+calculatedAttribute+") values " +
					"("+mtmID+","+i.intValue()+","+vals.get(i)+")";
			sql.add(q);
		}
		
		return sql;
	}

	public List<String> buildPostActionHeadersQuery(String table,
			List<HeaderExecutionResult> headers, MPostAction postAction,
			int mtmID, int index, String mainEntity, Integer dbID, Map<CAttribute, String> keyMap, Map<String, String> postActionParameters) {
		List<String> queries = new ArrayList<String>();
		
		String targetTable = postAction.getAttributes().get(0).getEntity().getDataReference();
		targetTable="fk_"+table+"__"+targetTable;
		
		ApplicationLoader dal = new ApplicationLoader();
		CBusinessClass mtmEntity = dal.getEntity(table);
		
		//	Get the main reference
		String mainDataReference = "";
		for(CAttribute a : mtmEntity.getAttributes())
			if(a.isReference()){
				mainDataReference = a.getDataReference().substring(3);
				break;
			}
		
		ProtogenDataEngine engine = new ProtogenDataEngine();
		List<PairKVElement> elements = engine.getDataKeys(mainDataReference,false,0);
		
		for(PairKVElement e : elements){
			String sql = "insert into "+table+" (fk_"+mainDataReference+", "+targetTable;
			String vals = " values ("+e.getKey()+","+mtmID;
			boolean flag = false;
			for(CAttribute a : postAction.getAttributes()){
				if(!a.getEntity().getDataReference().equals(table))
					continue;
				int i = postAction.getAttributes().indexOf(a);
				if(postAction.getParametersValues().get(i)==null || postAction.getParametersValues().get(i).length()==0)
					continue;
				double val = 0;
				String fvar = postAction.getParametersValues().get(i).replaceAll("<<", "");
				fvar = fvar.replaceAll(">>", "");
				
				if(a.getDataReference().equals("fk_"+mainDataReference))
					continue;
				boolean foundflag=false;
				for(HeaderExecutionResult her : headers){
					if(fvar.split("\\.").length>1)
						break;
					if(fvar.equals(StringFormat.getInstance().attributeDataReferenceFormat(her.getVariable()))){
						Map<Integer, Double> hervals = her.getValues().get(index);
						Integer INT = new Integer(e.getKey());
						if(hervals.containsKey(INT)){
							flag = true;
							val = hervals.get(INT).doubleValue();
							foundflag = true;
							break;
						}
					}
				}
				if(fvar.split("\\.").length>1){
					String intermidiateTable=fvar.split("\\.")[0];
					String usefulField = fvar.split("\\.")[1];
					foundflag = true;
					val = engine.getByPassedValue(usefulField, "user_"+intermidiateTable, "fk_"+mainDataReference, mainEntity, new Integer(e.getKey()),dbID,keyMap);
					flag = true;
				}
				String sval="";
				if(!foundflag){
					if(postActionParameters.containsKey(StringFormat.getInstance().attributeDataReferenceFormat(fvar))){
						sval = postActionParameters.get(StringFormat.getInstance().attributeDataReferenceFormat(fvar));
						if(sval!=null && sval.length()>0)
							flag=true;
					}
				}
				
				sql=sql+","+a.getDataReference();
				
				vals = vals+","+(foundflag?val+"":sval);
				
			}
			
			if(flag){
				sql = sql+") "+vals+")";
				queries.add(sql);
			}
		}
		
		return queries;
	}
	
	
	public List<String> buildPostActionMTMQuery(String table,
			List<HeaderExecutionResult> headers, MPostAction postAction,
			int mtmID, int index, String mainEntity, Integer dbID, Map<CAttribute, String> keyMap, Map<String, String> postActionParameters) {
		List<String> queries = new ArrayList<String>();
		
		String mtmFK = postAction.getAttributes().get(0).getEntity().getDataReference();
		mtmFK="fk_"+table+"__"+mtmFK;
		
		ApplicationLoader dal = new ApplicationLoader();
		
		//	Detecting source mtm
		String transformedAttribute=""; 
		for(CAttribute a : postAction.getAttributes())
			if(a.getDataReference().equals(mtmFK)){
				transformedAttribute = postAction.getParametersValues().get(postAction.getAttributes().indexOf(a));
				transformedAttribute = transformedAttribute.replaceAll("<<", "");
				transformedAttribute = transformedAttribute.replaceAll(">>", "");
				break;
			}
		CBusinessClass mainE = dal.getEntity(mainEntity);
		String sourceMtm = "";
		for(CAttribute a : mainE.getAttributes())
			if(StringFormat.getInstance().attributeDataReferenceFormat(a.getAttribute()).equals(transformedAttribute)){
				sourceMtm = a.getDataReference().substring(3);
				sourceMtm = sourceMtm.split("__")[0];
				break;
			}
		
		String sql = "select * from "+sourceMtm+" where fk_"+sourceMtm+"__"+mainEntity+"=?";
		List<Map<String,String>> res = new ArrayList<Map<String,String>>();
		try{
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, dbID);
		    ResultSet rs = ps.executeQuery();
		    
		    while(rs.next()){
		    	Map<String, String> r = new HashMap<String, String>();
		    	for(CAttribute a : postAction.getAttributes()){
		    		if(a.getEntity().getDataReference().equals(table) && !a.getDataReference().startsWith("pk_"))
		    			r.put(a.getDataReference(), rs.getObject(a.getDataReference()).toString());
		    	}
		    	res.add(r);
		    }
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(Map<String,String> r : res){
			sql = "insert into "+table+" (";
			String values = "(";
			for(String k : r.keySet()){
				sql = sql+k+",";
				values = values+"'"+r.get(k)+"',";
			}
			sql=sql+" "+mtmFK+")";
			values=values+" "+mtmID+")";
			
			sql = sql +" Values "+values;
			queries.add(sql);
		}
		
		return queries;
	}
	
	
	public String buildSelectQuery(String entity, List<String> attributes,
			int entityID) {
		String query = "select ";
		for(String a : attributes){
			query = query+a+", ";
		}
		
		query = query.substring(0, query.length()-2);
		
		query = query+" from "+entity +" where pk_"+entity+"='"+entityID+"'";
		
		return query;
	}
	
	public String buildSelectQuery(String entity, String refTable, List<String> attributes,
			int entityID) {
		String query = "select ";
		for(String a : attributes){
			query = query+a+", ";
		}
		
		query = query.substring(0, query.length()-1);
		
		query = query+" from "+refTable +" where pk_"+entity+"='"+entityID+"'";
		
		return query;
	}

	public String createSelectDataKeys(String referenceTable,
			List<String> keyAttributes, List<String> wheres,
			List<String> keyWords) {

				String query = "Select pk_"+referenceTable+", ";
				for(String a : keyAttributes){
					query = query+a+",";
				}
				query = query.substring(0, query.length()-1);
				query = query+" from "+referenceTable;
				
				
				String where="";
				if(keyWords != null && keyWords.size()>0){
					where = " (";
					for(String k : keyWords){
						for(String a : keyAttributes)
							where = where + " " + a + " like '%"+k+"%' or ";
					}
					
					where = where.substring(0,where.length()-3)+")";
				}
				
				/*
				 * Organization constraints
				 */
				ConstraintFactory factory = new ConstraintFactory();
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				String constraint = factory.viewOrganizationConstraint(cache.getUser(), referenceTable,true, false);
				if(constraint.length()>0)
					wheres.add(constraint);
				
				if(wheres.size()>0)
					query = query+" where "+wheres.get(0);
				for(int i = 1 ; i < wheres.size() ; i++)
					query = query+" AND "+wheres.get(i);
				
				if(where.length()>0){
					if(wheres.size()>0)
						query = query+" and "+where+" ";
					else
						query = query+" where "+where+" ";
				}
				
				//return query+" order by pk_"+referenceTable;
				return query+" order by pk_"+referenceTable+" limit 50";
	}
	
	public String createSelectDataKeys(String referenceTable,
			List<String> keyAttributes, List<Integer> attTypes, List<String> wheres,
			List<String> keyWords) {

				String query = "Select pk_"+referenceTable+", ";
				for(String a : keyAttributes){
					query = query+a+",";
				}
				query = query.substring(0, query.length()-1);
				query = query+" from "+referenceTable;
				
				
				String where="";
				if(keyWords != null && keyWords.size()>0){
					where = " (";
					for(String k : keyWords){
						for(String a : keyAttributes){
							
							int type = attTypes.get(keyAttributes.indexOf(a));
							if(type!=3)
								where = where + " lower(" + a + ") like lower('%"+k+"%') or ";
							else{
								where = where + " " + a + "::text like '%"+k+"%' or ";
							}
						}
					}
					
					where = where.substring(0,where.length()-3)+")";
				}
				
				/*
				 * Organization constraints
				 */
				ConstraintFactory factory = new ConstraintFactory();
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				String constraint = factory.viewOrganizationConstraint(cache.getUser(), referenceTable,true, false);
				if(constraint.length()>0)
					wheres.add(constraint);
				
				if(wheres.size()>0)
					query = query+" where "+wheres.get(0);
				for(int i = 1 ; i < wheres.size() ; i++)
					query = query+" AND "+wheres.get(i);
				
				if(where.length()>0){
					if(wheres.size()>0)
						query = query+" and "+where+" ";
					else
						query = query+" where "+where+" ";
				}
				
				//return query+" order by pk_"+referenceTable;
				return query+" order by pk_"+referenceTable+" limit 50";
	}
	
	public String createSelectDataKeys(String referenceTable,
			List<String> keyAttributes, List<String> wheres) {
		// TODO Auto-generated method stub
		String query = "Select pk_"+referenceTable+", ";
		for(String a : keyAttributes){
			query = query+a+",";
		}
		query = query.substring(0, query.length()-1);
		query = query+" from "+referenceTable;
		
		/*
		 * Organization constraints
		 */
		ConstraintFactory factory = new ConstraintFactory();
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		String constraint = factory.viewOrganizationConstraint(cache.getUser(), referenceTable,true, false);
		if(constraint.length()>0)
			wheres.add(constraint);
		
		if(wheres.size()>0)
			query = query+" where "+wheres.get(0);
		for(int i = 1 ; i < wheres.size() ; i++)
			query = query+" AND "+wheres.get(i);
		//return query+" order by pk_"+referenceTable;
		return query+" order by pk_"+referenceTable+" limit 50";
		
	}

	/*
	 * 	Asgard Project
	 */
	public String buildAsgardQuery(ResultTable resultTable) {
		String sql = "SELECT ";
		List<String> foreignJunctions=new ArrayList<String>();
		List<String> tables = new ArrayList<String>();
		List<String> cols = new ArrayList<String>();
		String restable = resultTable.getRepresentedValue().getAttribute().getEntity().getDataReference();
		tables.add(restable);
		
		
		//	Dimensions
		for(VisitingDimension d : resultTable.getDimensions()){
				sql = sql+d.getAttribute().getEntity().getDataReference()+"."+d.getAttribute().getDataReference()+",";
				cols.add(d.getAttribute().getEntity().getDataReference()+"."+d.getAttribute().getDataReference());
		}
		
		//	Value
		AgregationFunction f = resultTable.getRepresentedValue().getPreformatFunction();
		boolean groupby=false;
		if(f == AgregationFunction.SUM){
			sql = sql+"sum("+restable+"."+resultTable.getRepresentedValue().getAttribute().getDataReference()+"),";
			groupby=true;
		} else if(f == AgregationFunction.AVG){
			sql = sql+"avg("+restable+"."+resultTable.getRepresentedValue().getAttribute().getDataReference()+"),";
			groupby=true;
		} else if(f == AgregationFunction.COUNT){
			sql = sql+"count("+restable+"."+resultTable.getRepresentedValue().getAttribute().getDataReference()+"),";
			groupby=true;
		}
		else{
			sql = sql+restable+"."+resultTable.getRepresentedValue().getAttribute().getDataReference()+",";
		}
		
		sql = sql.substring(0,sql.length()-1);
		
		//	Tables
		sql = sql+" FROM ";
		for(String t : tables){
			sql = sql+t+",";
		}
		sql = sql.substring(0,sql.length()-1);
		
		//	Junctions
		if(foreignJunctions.size()>0){
			sql=sql+" WHERE ";
			for(String j : foreignJunctions){
				sql=sql+j+" AND ";
			}
			sql = sql.substring(0,sql.length()-5);
		}
		
		if(groupby){
			sql = sql+" GROUP BY ";
			for(String c : cols)
				sql = sql+c+" ,";
			
			sql = sql.substring(0,sql.length()-1);
			
		}
		
		//	Order
		sql = sql+" ORDER BY ";
		for(String c : cols)
			sql = sql+c+" asc,";
		
		sql = sql.substring(0,sql.length()-1);
		
		return sql;
	}

	/*
	 * 	Case : RECUPERER
	 * 	#! RECUPERER
	 * 	OBJECTIF:table.colonne
	 * 	ENTITES:table2;table3;table4....
	 * 	ARGUMENTS:tablei.colonnej=valeur;tablek.colonne=tablei.colonne
	 *  #! FIN
	 */
	public String recupererToSQL(String str) throws IOException{
		String query = "";
		ApplicationLoader dal = new ApplicationLoader();
		
		List<String> content;
		InputStream is = new ByteArrayInputStream(str.getBytes());
		content = IOUtils.readLines(is);
		
		if(content == null || content.size()!=3)
			return "";
		
		//	Récupérer la ligne objectif
		String objectif = content.get(0);
		objectif = objectif.replaceAll(";", "");
		String tableObjectif = objectif.split("\\:")[1].split("\\.")[0];
		String champsObjectif = objectif.split("\\:")[1].split("\\.")[1];
		
		CBusinessClass classObjectif = dal.getClassByName(tableObjectif);
		CAttribute attObjectif = dal.getAttributeByName(champsObjectif,classObjectif);
		
		query = "SELECT "+classObjectif.getDataReference()+"."+attObjectif.getDataReference()+" ";
		
		//	Récupérer les tables
		String[] tables = content.get(1).split("\\:")[1].split(";");
		query = query+" FROM "+classObjectif.getDataReference()+" ";
		for(String table : tables){
			query = query+", "+dal.getClassByName(table).getDataReference()+" ";
		}
		
		//	Récupérer les arguments
		String largs = content.get(2).split("\\:")[1];
		String[] args = largs.split(";");
		if(args.length>0)
			query = query+" where ";
		for(String arg : args){
			String col, val;
			col = arg.split("=")[0];
			val = arg.split("=")[1];
			
			String tcol = col.split("\\.")[0].trim();
			String ccol = col.split("\\.")[1].trim();
			CBusinessClass ecol = dal.getClassByName(tcol);
			CAttribute acol = dal.getAttributeByName(ccol, ecol);
			
			String sqlarg = acol.getDataReference()+"=";
			
			//	Traiter les valeurs/jointures
			if(!val.contains("ID")){
				sqlarg=sqlarg+val;
			}else{
				String vtable = val.split("\\.")[0];
				String vcol = val.split("\\.")[1];
				
				CBusinessClass e = dal.getClassByName(vtable);
				
				sqlarg=sqlarg+dal.getAttributeByName(vcol, e).getDataReference(); 
			}
			
			query = query+" "+sqlarg+" AND ";
		}
		if(args.length>0)
			query = query.substring(0,query.length()-5);
		
		return query;
	}

	public String CreateWSConstraints(List<SearchClause> clauses) {
		String constraint = "";
		List<String> cs = new ArrayList<String>();
		
		for(SearchClause c : clauses){
			if(c.getGt().equals(c.getLt())){
				cs.add(c.getField()+"='"+c.getGt()+"'");
				continue;
			}
			
			
			if(c.getGt()!=null && c.getGt().length()>0){
				String f = c.getField();
				if(c.getType().equals("TEXT"))
					f = "lower("+f+")";
				cs.add(f+getGClause(c.getType(),c.getGt()));
			}
			if(c.getLt()!=null && c.getLt().length()>0){
				String f = c.getField();
				if(c.getType().equals("TEXT"))
					f = "lower("+f+")";
				cs.add(f+getLClause(c.getType(),c.getLt()));
			}
		}
		
		
		for(String c : cs)
			constraint = constraint + " AND "+c;
		
		return " "+constraint.substring(4)+" ";
	}

	private String getLClause(String type, String lt) {
		if(type.equals("TEXT"))
			return "like lower('%"+lt+"')";
		else
			return ">='"+lt+"'";
	}

	private String getGClause(String type, String gt) {
		if(type.equals("TEXT"))
			return "like lower('"+gt+"%')";
		else
			return ">='"+gt+"'";
	}

	/*
	 * IDENTIFICATION ROWS
	 */
	public String buildIdentificationRowLoad(CBusinessClass entity) {
		String sql = "select id, reference_source from c_identification_row where reference_row="+entity.getId();
		return sql;
	}

	public String buildIdentifierInstanceQuery(
			CIdentificationRow identificationRow, String value) {
		return "select bean_id from c_unique_row_instance "
				+ " where identification_row="+identificationRow.getId()+" and source_id="+value;
	}

	public String buildDeleteDefaultRow(int id, int idsource) {
		
		return "delete from c_unique_row_instance where identification_row="+id+" "
				+ " and source_id="+idsource;
	}

	public String buildInsertDefaultRow(int id, int idsource, int dbID) {
		
		return "insert into c_unique_row_instance (identification_row, source_id, bean_id ) values "
				+ "("+id+","+idsource+","+dbID+")";
	}

	public String buildInlineUpdate(String dataReference, int rowId,
			Map<CAttribute, String> values) {
		String sql = "update "+dataReference+" set ";
		for(CAttribute a : values.keySet()){
			if(a.getCAttributetype().getId() == 4 && values.get(a) == null || values.get(a).length() == 0)
				values.put(a, "0.0");
			
			sql = sql+a.getDataReference()+"='"+values.get(a)+"', ";
		}
		sql = sql.substring(0,sql.length()-2)+" where pk_"+dataReference+"="+rowId;
		return sql;
	}

	

	

	
	
}
