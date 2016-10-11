package fr.protogen.dataload;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import fr.protogen.asgard.model.ResultTable;
import fr.protogen.asgard.model.VisitingDimension;
import fr.protogen.connector.session.WebSessionManager;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.control.ui.MtmBlock;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.DBFormattedObjects;
import fr.protogen.engine.utils.DTOProcessSession;
import fr.protogen.engine.utils.EntityDTO;
import fr.protogen.engine.utils.HeaderExecutionResult;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.SpecialValuesEngine;
import fr.protogen.engine.utils.StringFormat;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.engine.utils.UIFilterElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.HistoryDataAccess;
import fr.protogen.masterdata.DAO.OrganizationDAL;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CAttributetype;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CDataHistory;
import fr.protogen.masterdata.model.CIdentificationRow;
import fr.protogen.masterdata.model.CInstanceHistory;
import fr.protogen.masterdata.model.CSchedulableEntity;
import fr.protogen.masterdata.model.CScheduleEvent;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreDataAccessRight;
import fr.protogen.masterdata.model.CoreDataConstraint;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GParameterValues;
import fr.protogen.masterdata.model.GParametersInstance;
import fr.protogen.masterdata.model.MPostAction;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.SProcedure;

public class ProtogenDataEngine {

	private QueryBuilder builder = new QueryBuilder();
	private List<Integer> currentIDS;
	private List<Map<String, String>> foreignKeys;
	private List<String> mtmTables;
	private Map<String,Integer> autovalues;
	
	private int dbID;
	private CBusinessClass entity;
	private DataDefinitionOperation operation;
	private Map<CBusinessClass, Integer> fkClasses = new HashMap<CBusinessClass, Integer>();
	private CWindow window;
	private Map<CAttribute, Object> dataToDelete;
	
	private List<String> rowsStyle;

	
	public int countData(CWindow window, List<UIFilterElement> filters,
			CoreUser u) {
		
		int result=0;
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		String referenceTable = window.getMainEntity();
		OrganizationDAL odal = new OrganizationDAL();
		CBusinessClass referenceBC = this.getReferencedTable(referenceTable);
		CoreUser user = cache.getUser();
		CoreRole role = user.getCoreRole();
		
		// Get applied models
		List<GParametersInstance> models = cache.getParameterPackages();
		for(GParametersInstance m : models){
			List<CBusinessClass> modelEntities = odal.loadModelEntities(m);
			m.getModelPackage().setImplicatedEntities(modelEntities);
			
		}
		
		//	Check if entity belongs to any model
		List<String> parameters = new ArrayList<String>();
		for(GParametersInstance m : models){
			boolean foundFlag=false;
			for(CBusinessClass bc : m.getModelPackage().getImplicatedEntities()){
				if(bc.getId() == referenceBC.getId()){
					foundFlag = true;
					break;
				}
			}
			
			if(!foundFlag)
				break;
			
			//	Get allowed values
			List<GParameterValues> values = odal.loadAllowedValues(m, referenceBC);
			
			if(values.size()>0){
				String p = "pk_"+referenceTable+" in (";
				for(GParameterValues pv : values){
					p = p+pv.getRowDbId()+",";
				}
				if(p.length()>1)
					p = p.substring(0,p.length()-1); //	remove the last  comma
				p = p+")";
				parameters.add(p);
			}
				
		}
		
		//	Now check for role constraints
		if(role.getConstraints() != null && role.getConstraints().size()>0){
			for(CoreDataAccessRight r : role.getConstraints()){
				if(r.getEntity().getId() == referenceBC.getId()){
					String p = "pk_"+referenceTable+"="+r.getValue();
					parameters.add(p);
					break;
				}
			}
		}
		
		//	Now check for Roles constraints
		List<CoreDataConstraint> constraints = cache.getConstraints();
		for(CoreDataConstraint c : constraints)
			if(c.getEntity().getId() == referenceBC.getId()){
				String p = "pk_"+referenceTable+"="+c.getBeanId();
				parameters.add(p);
				break;
			}
		
		for(CoreDataAccessRight c : user.getCoreRole().getConstraints()){
			if(c.getEntity().getId() == referenceBC.getId()){
				String p = "pk_"+referenceTable+"="+c.getValue();
				parameters.add(p);
				break;
			}
		}
		
		for(CAttribute a : window.getCAttributes()){
			if(!a.getEntity().getDataReference().equals(window.getMainEntity()))
				continue;
			if(!a.isReference())
				continue;
			for(CoreDataAccessRight c : user.getCoreRole().getConstraints()){
				String referencedTable =a.getDataReference().substring(3); 
				if(c.getEntity().getDataReference().equals(referencedTable)){
					String p = a.getDataReference()+"="+c.getValue();
					parameters.add(p);
					break;
				}
			}
		}
		
		//	Detect history option
		//ApplicationLoader al = new ApplicationLoader();
		//CBusinessClass entity = al.getEntity(window.getMainEntity());
		//HistoryDataAccess hda = new HistoryDataAccess();
		//CDataHistory h = hda.checkForHistory(entity);
		//boolean historyMode = h.getId()>0;
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			builder = new QueryBuilder(); 
			
			String query = builder.buildCountQuery(window,filters,u, parameters);
			
			System.out.println("COUNT QUERY "+query);
			
			ResultSet rs = st.executeQuery(query);
			
			if(rs.next())
				result = rs.getInt(1);
			
			rs.close();
			st.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return result;
	}
	
	@SuppressWarnings("resource")
	public List<List<String>> executeSelectWindowQuery(CWindow window, List<UIFilterElement> filters, CoreUser u, int currentPage, int pagesCount){
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		String referenceTable = window.getMainEntity();
		OrganizationDAL odal = new OrganizationDAL();
		CBusinessClass referenceBC = this.getReferencedTable(referenceTable);
		CoreUser user = cache.getUser();
		CoreRole role = user.getCoreRole();
		
		// Get applied models
		List<GParametersInstance> models = cache.getParameterPackages();
		for(GParametersInstance m : models){
			List<CBusinessClass> modelEntities = odal.loadModelEntities(m);
			m.getModelPackage().setImplicatedEntities(modelEntities);
		}
		
		//	Check if entity belongs to any model
		List<String> parameters = new ArrayList<String>();
		for(GParametersInstance m : models){
			boolean foundFlag=false;
			for(CBusinessClass bc : m.getModelPackage().getImplicatedEntities()){
				if(bc.getId() == referenceBC.getId()){
					foundFlag = true;
					break;
				}
			}
			
			if(!foundFlag)
				break;
			
			//	Get allowed values
			List<GParameterValues> values = odal.loadAllowedValues(m, referenceBC);
			
			if(values.size()>0){
				String p = "pk_"+referenceTable+" in (";
				for(GParameterValues pv : values){
					p = p+pv.getRowDbId()+",";
				}
				if(p.length()>1)
					p = p.substring(0,p.length()-1); //	remove the last  comma
				p = p+")";
				parameters.add(p);
			}
				
		}
		
		//	Now check for role constraints
		if(role.getConstraints() != null && role.getConstraints().size()>0){
			for(CoreDataAccessRight r : role.getConstraints()){
				if(r.getEntity().getId() == referenceBC.getId()){
					String p = "pk_"+referenceTable+"="+r.getValue();
					parameters.add(p);
					break;
				}
			}
		}
		
		//	Now check for Roles constraints
		List<CoreDataConstraint> constraints = cache.getConstraints();
		for(CoreDataConstraint c : constraints)
			if(c.getEntity().getId() == referenceBC.getId()){
				String p = "pk_"+referenceTable+"="+c.getBeanId();
				parameters.add(p);
				break;
			}
		
		for(CoreDataAccessRight c : user.getCoreRole().getConstraints()){
			if(c.getEntity().getId() == referenceBC.getId()){
				String p = "pk_"+referenceTable+"="+c.getValue();
				parameters.add(p);
				break;
			}
		}
		
		for(CAttribute a : window.getCAttributes()){
			if(!a.getEntity().getDataReference().equals(window.getMainEntity()))
				continue;
			if(!a.isReference())
				continue;
			for(CoreDataAccessRight c : user.getCoreRole().getConstraints()){
				String referencedTable =a.getDataReference().substring(3); 
				if(c.getEntity().getDataReference().equals(referencedTable)){
					String p = a.getDataReference()+"="+c.getValue();
					parameters.add(p);
					break;
				}
			}
		}
		
		//	Detect history option
		ApplicationLoader al = new ApplicationLoader();
		CBusinessClass entity = al.getEntity(window.getMainEntity());
		HistoryDataAccess hda = new HistoryDataAccess();
		CDataHistory h = hda.checkForHistory(entity);
		boolean historyMode = h.getId()>0;
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			builder = new QueryBuilder(); 
			
			List<List<String>> results = new ArrayList<List<String>>();
			Map<String,String> queries = builder.buildSelectQuery(window,filters,u, parameters, currentPage, pagesCount);
			String query = queries.get(window.getMainEntity());
			System.out.println("[GENERIUM][SCREEN QUERY]\n\t"+query+"\n");
			ResultSet rs = st.executeQuery(query);
			ResultSetMetaData metars = rs.getMetaData();
			int count = metars.getColumnCount();
			List<String> headers = new ArrayList<String>();
			for(int i=0 ; i < count ; i++){
				String column = metars.getColumnName(i+1);
				String header = fetchForAttribute(window, column);
				headers.add(header);
			}
			
			//	If history is on add two columns for dates
			if(historyMode){
				headers.add("Date de début");
				headers.add("Date de fin");
			}
			results.add(headers);
			
			List<String> types = new ArrayList<String>();
			for(int i=0 ; i < count ; i++){
				String type = metars.getColumnTypeName(i+1);
				types.add(type);
			}
			
			results.add(types);
			currentIDS = new ArrayList<Integer>();
			foreignKeys = new ArrayList<Map<String,String>>();
			
			//	Histo mode
			int histoReference=0;
			CAttribute refattribute = new CAttribute();
			if(historyMode){
				
				for(CAttribute a : window.getCAttributes())
					if(a.getId() == h.getReference().getId()){
						refattribute = a;
						break;
					}
			}
			
			rowsStyle = new ArrayList<String>();
			while(rs.next()){
				Map<String, String> fkeys = new LinkedHashMap<String, String>();
				List<String> row = new ArrayList<String>();
				for(int i = 0 ; i < count ; i++){
					if(types.get(i).equals("varchar") || types.get(i).equals("text") || types.get(i).equals("bpchar"))
						row.add(rs.getString(i+1));
					if(types.get(i).equals("float8"))
						row.add(rs.getDouble(i+1)+"");
					if(types.get(i).equals("int4") || types.get(i).equals("serial"))
						row.add(rs.getInt(i+1)+"");
					if(types.get(i).startsWith("timestamp"))
						row.add(rs.getDate(i+1)+"");
					String column = metars.getColumnName(i+1);
					if(column.startsWith("fk_")){
						fkeys.put(column, rs.getInt(i+1)+"");
						if(historyMode){
							if(column.equals(refattribute.getDataReference())){
								histoReference = rs.getInt(i+1);
							}
						}
					}
					
				}
				
				foreignKeys.add(fkeys);
				results.add(row);
				int id = rs.getInt("pk_"+window.getMainEntity());
				currentIDS.add(id);
				
				String theme = "whiteline";
				
				//	If history is on add two columns for dates
				if(historyMode){
					CInstanceHistory inst = hda.getHistoryInstance(h, id, histoReference);
					row.add(inst.getDateDebut()+"");
					String dfin = (inst.getDateFin()!=null)?inst.getDateFin()+"":"-";
					row.add(dfin);
					theme = inst.isCourant()?"greenline":"yellowline";
				}
				rowsStyle.add(theme);
			}
			for(int i=0 ; i < count ; i++){
				results.get(1).set(i, i+"");
			}
			if(historyMode){
				results.get(1).add(count+"");
				results.get(1).add(count+1+"");
			}
			rs.close();
			
			//	Bismi allah
			List<Map<CAttribute,String>> indirectTable = new ArrayList<Map<CAttribute,String>>();
			List<Map<String,String>> indirectMultiTable = new ArrayList<Map<String,String>>();
			List<Map<String,String>> indirectMTM = new ArrayList<Map<String,String>>();
			List<Map<CAttribute,String>> lsommes = new ArrayList<Map<CAttribute, String>>();
			
			for (Integer dbID : currentIDS){
				
				//	somme
				Map<CAttribute,String> sommes = new LinkedHashMap<CAttribute, String>();
				for(CAttribute a : window.getCAttributes()){
					if(a.getIndirectFunction() != 3)
						continue;
					CBusinessClass ref = a.getEntity();
					
					String sql = "select sum("+a.getDataReference()+") from "+ref.getDataReference()+ " where fk_"+window.getMainEntity()+"="+dbID;
					
					rs = st.executeQuery(sql);
					
					double sum = 0;
					if(rs.next())
						sum = rs.getDouble(1);
					
					rs.close();
					sommes.put(a, sum+"");
				}
				lsommes.add(sommes);
					
				
				Map<CAttribute,String> values = new LinkedHashMap<CAttribute, String>();
				for (CAttribute a : window.getCAttributes()){
					if(a.getIndirectFunction() != 1)
						continue;
					if(a.isIndirectMtmKey() || a.isIndirectMtmValue() || !a.isIndirectReference())
						continue;
					CBusinessClass e = a.getEntity();
					List<String> junctions = new ArrayList<String>();
					for(UIFilterElement f : filters){
						ApplicationLoader dal = new ApplicationLoader();
						if(!dal.checkAttributeInEntity(e.getDataReference(),f.getAttribute()))
							continue;
						//	Reference
						if(f.getAttribute().getDataReference().startsWith("fk_"))
							junctions.add(f.getAttribute().getDataReference()+"="+f.getControlValue());
						else if (f.getAttribute().getCAttributetype().getId() == 2){
						//	String
							if(f.getLthan() != null && f.getLthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" like '"+f.getLthan()+"%'");
							}
							if(f.getGthan() != null && f.getGthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" like '%"+f.getGthan()+"'");
							}
						} else {
						//	Number or date
							if(f.getLthan() != null && f.getLthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" < "+f.getLthan());
							}
							if(f.getGthan() != null && f.getGthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" > "+f.getGthan());
							}
						}
						
					}
					String where = " where fk_"+window.getMainEntity()+"="+dbID;
					
					for(String j : junctions){
						where = where+" AND "+j;
					}
					String sql = "select "+a.getDataReference()+" from "+e.getDataReference()+" "+where+" order by pk_"+e.getDataReference()+" desc" ;
					rs = st.executeQuery(sql);
					String value="";
					if(rs.next()){
						value = (rs.getObject(1)==null)?"":rs.getObject(1).toString();
					}
					rs.close();
					
					if(value.length()>0) {
						if(a.getDataReference().startsWith("fk_")){
							int id = Integer.parseInt(value);
							String lookTable = a.getDataReference().substring(3);
							
							ArrayList<String> twheres = new ArrayList<String>();
							twheres.add("pk_"+lookTable+"="+id);
							Map<Integer, String> tlist = getDataKeys(lookTable,twheres);
							for(Integer i : tlist.keySet()){
								value=tlist.get(i);
								break;
							}
						} else if(a.getCAttributetype().getId() == 7) {
							String uq = "select \"firstName\", \"lastName\" from core_user where id="+value;
							rs = st.executeQuery(uq);
							if(rs.next())
								value = rs.getString(1)+" "+rs.getString(2);
						}
							
						if(a.getCAttributetype().getId()==4){
							double d = new Double(value);
							value=""+d;
						}
							
						values.put(a,value);
					} else {
						values.put(a,"-");
					}
				}
				indirectTable.add(values);
				
				//	multiple
				Map<String,String> mvalues = new LinkedHashMap<String, String>();
				for(CAttribute a : window.getCAttributes()){
					if(a.getIndirectFunction()!=2)
						continue;
					if(!a.isIndirectMtmKey())
						continue;
					
					//	get the value
					CBusinessClass e = a.getEntity();
					CAttribute av = new CAttribute();
					for (CAttribute ca : window.getCAttributes()){
						if(!ca.isIndirectReference() || a.getIndirectFunction()!=2)
							continue;
						if(ca.getEntity().getDataReference().equals(e.getDataReference()) && !ca.isIndirectMtmKey()){
							av = ca;
							break;
						}
					}
					
					List<String> junctions = new ArrayList<String>();
					for(UIFilterElement f : filters){
						ApplicationLoader dal = new ApplicationLoader();
						if(!dal.checkAttributeInEntity(e.getDataReference(),f.getAttribute()))
							continue;
						//	Reference
						if(f.getAttribute().getDataReference().startsWith("fk_"))
							junctions.add(f.getAttribute().getDataReference()+"="+f.getControlValue());
						else if (f.getAttribute().getCAttributetype().getId() == 2){
						//	String
							if(f.getLthan() != null && f.getLthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" like '"+f.getLthan()+"%'");
							}
							if(f.getGthan() != null && f.getGthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" like '%"+f.getGthan()+"'");
							}
						} else {
						//	Number or date
							if(f.getLthan() != null && f.getLthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" < "+f.getLthan());
							}
							if(f.getGthan() != null && f.getGthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" > "+f.getGthan());
							}
						}
						
					}
					String where = " where fk_"+window.getMainEntity()+"="+dbID;
					
					for(String j : junctions){
						where = where+" AND "+j;
					}
					
					String sql = "select "+a.getDataReference()+", "+av.getDataReference()+" from "+e.getDataReference()+" "+where;
					
					rs = st.executeQuery(sql);
					
					System.out.println("DBROW : "+dbID+"\tSQL for Multitable : "+sql);
					
					while(rs.next()){
						String k = rs.getObject(1).toString(); 
						String v = rs.getObject(2).toString();
						
						if(a.getDataReference().startsWith("fk_")){
							int id = Integer.parseInt(k);
							String lookTable = a.getDataReference().substring(3);
							
							ArrayList<String> twheres = new ArrayList<String>();
							twheres.add("pk_"+lookTable+"="+id);
							Map<Integer, String> tlist = getDataKeys(lookTable,twheres);
							for(Integer i : tlist.keySet()){
								k=tlist.get(i);
								break;
							}
						}
						
						if(a.getCAttributetype().getId()==4){
							double d = new Double(v);
							v=""+d;
						}
						
						mvalues.put(window.getCAttributes().indexOf(a)+"---"+k,v );
					}
					
						
				}
				indirectMultiTable.add(mvalues);
				mvalues = new HashMap<String, String>();
				
				//	mtm
				Map<String,String> svalues = new LinkedHashMap<String, String>();
				for (CAttribute a : window.getCAttributes()){
					if(a.getIndirectFunction() != 1 && a.getIndirectFunction() != 5)
						continue;
					if(!a.isIndirectMtmKey())
						continue;
					
					CBusinessClass e = a.getEntity();
					//	Get the value
					CAttribute av = new CAttribute();
					for (CAttribute ca : window.getCAttributes()){
						if(!ca.isIndirectReference())
							continue;
						if(ca.getEntity().getDataReference().equals(e.getDataReference()) && ca.isIndirectMtmValue()){
							av = ca;
							break;
						}
					}
					
					
					String sql=" select * from "+e.getDataReference()+" limit 1 ";
					rs = st.executeQuery(sql);
					String dr="";
					ResultSetMetaData md = rs.getMetaData();
					for(int i = 0 ; i < md.getColumnCount();i++){
						dr = md.getColumnName(i+1);
						if(dr.startsWith("fk_"+e.getDataReference()+"__"))
							break;
					}
					rs.close();
					
					String lineTable=e.getDataReference();
					String mtmTable=dr.split("__")[1];
					
					List<String> junctions = new ArrayList<String>();
					for(UIFilterElement f : filters){
						ApplicationLoader dal = new ApplicationLoader();
						if(!dal.checkAttributeInEntity(lineTable,f.getAttribute()) && !dal.checkAttributeInEntity(mtmTable,f.getAttribute()))
							continue;
						//	Reference
						if(f.getAttribute().getDataReference().startsWith("fk_"))
							junctions.add(f.getAttribute().getDataReference()+"="+f.getControlValue());
						else if (f.getAttribute().getCAttributetype().getId() == 2){
						//	String
							if(f.getLthan() != null && f.getLthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" like '"+f.getLthan()+"%'");
							}
							if(f.getGthan() != null && f.getGthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" like '%"+f.getGthan()+"'");
							}
						} else {
						//	Number or date
							if(f.getLthan() != null && f.getLthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" < "+f.getLthan());
							}
							if(f.getGthan() != null && f.getGthan().length()>0){
								junctions.add(f.getAttribute().getDataReference()+" > "+f.getGthan());
							}
						}
						
					}
					
					
					
					if(av.getDataReference()!=null && av.getDataReference().length()>0){
						
						String where = " where "+lineTable+"."+dr+"="+mtmTable+".pk_"+mtmTable+" AND " +
								" "+mtmTable+".fk_"+window.getMainEntity()+"="+dbID;
						
						for(String j : junctions){
							where = where+" AND "+j;
						}
						
						sql = "select "+lineTable+"."+a.getDataReference()+", "+lineTable+"."+av.getDataReference()+" " +
								" from "+lineTable+", "+mtmTable+" "+where;
						rs = st.executeQuery(sql);
						
						while(rs.next()){
							String k = rs.getObject(1).toString(); 
							String v = rs.getObject(2).toString();
							
							if(a.getDataReference().startsWith("fk_")){
								int id = Integer.parseInt(k);
								String lookTable = a.getDataReference().substring(3);
								
								ArrayList<String> twheres = new ArrayList<String>();
								twheres.add("pk_"+lookTable+"="+id);
								Map<Integer, String> tlist = getDataKeys(lookTable,twheres);
								for(Integer i : tlist.keySet()){
									k=tlist.get(i);
									break;
								}
							}
							
							if(a.getCAttributetype().getId()==4){
								double d = new Double(v);
								v=""+d;
							}
							
							svalues.put(window.getCAttributes().indexOf(a)+"---"+k,v );
							
						}
						rs.close();
					} else {
						
						String where = " where "+lineTable+"."+dr+"="+mtmTable+".pk_"+mtmTable+" AND " +
								" "+mtmTable+".fk_"+window.getMainEntity()+"="+dbID;
						
						for(String j : junctions){
							where = where+" AND "+j;
						}
						
						sql = "select "+lineTable+"."+a.getDataReference()+" " +
								" from "+lineTable+", "+mtmTable+" where "+lineTable+"."+dr+"="+mtmTable+".pk_"+mtmTable+" AND " +
								" "+mtmTable+".fk_"+window.getMainEntity()+"="+dbID ;
						rs = st.executeQuery(sql);
						
						while(rs.next()){
							int mtmcount=0;
							String k = rs.getObject(1).toString(); 
							
							
							if(a.getDataReference().startsWith("fk_")){
								int id = Integer.parseInt(k);
								String lookTable = a.getDataReference().substring(3);
								
								ArrayList<String> twheres = new ArrayList<String>();
								twheres.add("pk_"+lookTable+"="+id);
								Map<Integer, String> tlist = getDataKeys(lookTable,twheres);
								for(Integer i : tlist.keySet()){
									
									k=tlist.get(i);
									if(svalues.containsKey(window.getCAttributes().indexOf(a)+"---"+k) && a.getIndirectFunction() == 5){
										if(!svalues.get(window.getCAttributes().indexOf(a)+"---"+k).equals("-"))
											mtmcount = Integer.parseInt(svalues.get(window.getCAttributes().indexOf(a)+"---"+k));
									}
										
									break;
								}
							}
							mtmcount++;
							if(a.getIndirectFunction() == 1)
								svalues.put(window.getCAttributes().indexOf(a)+"---"+k,"Oui" );
							else
								svalues.put(window.getCAttributes().indexOf(a)+"---"+k,""+mtmcount );
							
						}
						rs.close();
					}
				}
				indirectMTM.add(svalues);
				
			}
			
			
			if(lsommes.size()>0){
				for(CAttribute a : lsommes.get(0).keySet()){
					results.get(0).add(a.getEntity().getName()+" - "+ a.getAttribute());
					results.get(1).add(window.getCAttributes().indexOf(a)+"");
				}
				for(int i = 2 ; i < results.size() ; i++){
					for(CAttribute a : lsommes.get(i-2).keySet()){
						results.get(i).add(lsommes.get(i-2).get(a));
					}
				}
			}
			
			
			
			if(indirectTable.size() >0){
				for(CAttribute a : indirectTable.get(0).keySet()){
					if(a.getCAttributetype().getId()==7)
						results.get(0).add("#IN_U#"+a.getAttribute());
					else if(a.getDataReference().startsWith("fk_"))
						results.get(0).add("#IN_R#"+a.getAttribute());
					else
						results.get(0).add(a.getAttribute());
					results.get(1).add(window.getCAttributes().indexOf(a)+"");
				}
				for(int i = 2 ; i < results.size() ; i++){
					for(CAttribute a : indirectTable.get(i-2).keySet()){
						results.get(i).add(indirectTable.get(i-2).get(a));
					}
				}
			}
			
			
			
			if(indirectMTM.size() >0){
				//	Reccuperer tous les titres
				List<String> titles = new ArrayList<String>();
				for(Map<String,String> l : indirectMTM){
					for(String t : l.keySet())
						if(!titles.contains(t))
							titles.add(t);
				}
				for(String t : titles){
					results.get(0).add(t);
					results.get(1).add(t);
				}
				for(int i = 2 ; i < results.size() ; i++){
					Map<String,String> datum = indirectMTM.get(i-2);
					
					for(String t : titles){
						if(datum.containsKey(t))
							results.get(i).add(datum.get(t));
						else
							results.get(i).add("-");
					}
				}
			}
			
			if(indirectMultiTable.size() >0){
				//	Reccuperer tous les titres
				List<String> titles = new ArrayList<String>();
				for(Map<String,String> l : indirectMultiTable){
					for(String t : l.keySet())
						if(!titles.contains(t))
							titles.add(t);
				}
				for(String t : titles){
					results.get(0).add(t);
					results.get(1).add(t);
				}
				for(int i = 2 ; i < results.size() ; i++){
					if(indirectMultiTable.size()<=(i-2))
						break;
					Map<String,String> datum = indirectMultiTable.get(i-2);
					for(String t : titles){
						if(datum.containsKey(t))
							results.get(i).add(datum.get(t));
						else
							results.get(i).add("-");
					}
				}
			}
			for(int i = 0 ; i < results.get(0).size();i++){
				String t = results.get(0).get(i);
				if(t.split("---").length>1){
					
					results.get(0).set(i, t.split("---")[1]);
					results.get(1).set(i, t.split("---")[0]);
					
				}
			}
			results = cleanTable(results);
			return results;
			
		}catch(Exception exc){
			System.out.println("**********************************");
			System.out.println("Erreur de récupération des données");
			System.out.println("**********************************");
			exc.printStackTrace();
		}
		
		return null;
	}

	public void executePost(MPostAction action, double value, String entity, int ID){
		
		
		String query = builder.generatePostActionQuery(action, value, entity, ID);
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			st.execute(query);
			
			st.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
	

	private List<List<String>> cleanTable(List<List<String>> cloned) {
		// TODO Auto-generated method stub
		
		//	Unused Columns
		
		List<Integer> ids = new ArrayList<Integer>();		
		
		for(int i = 0 ; i < cloned.get(0).size() ; i++)
			if(cloned.get(0).get(i).toLowerCase().startsWith("id") || cloned.get(0).get(i).toLowerCase().startsWith("mtmk")){
				ids.add(new Integer(i));
			}
		
		
		
		for(int i=0 ; i < cloned.size() ; i++){
			for(int k=ids.size()-1;k>=0;k--){
				cloned.get(i).remove(ids.get(k).intValue());
			}
		}
		
		
		
		return cloned;
	}

	
	private String fetchForAttribute(CWindow window, String column) {
		// TODO Auto-generated method stub
		List<CAttribute> attributes = window.getCAttributes();
		
		for(CAttribute attribute : attributes){
			if(attribute.getDataReference().equals(column) || (attribute.getEntity().getDataReference()+"."+attribute.getDataReference()).equals(column)){
				if(attribute.isReference())
					return "#REF#"+attribute.getDataReference();
				else if(attribute.getCAttributetype().getId() == 5)
					return "#HOUR#"+attribute.getAttribute();
				else if(attribute.getCAttributetype().getId() == 7)
					return "#CURRENT_USER#"+attribute.getAttribute();
				else if(attribute.isAutoValue()){
					return "#AUTO#"+((attribute.getDefaultValue()==null)?"":attribute.getDefaultValue())+"#"+attribute.getAttribute();
				}
				else
					return attribute.getAttribute();
					
			}
		}
		return null;
	}

	public List<Integer> getLastIDS() {
		// TODO Auto-generated method stub
		return currentIDS;
	}

	

	public DBFormattedObjects getDataByID(int dbID, CWindow window) {
		// TODO Auto-generated method stub
		QueryBuilder builder = new QueryBuilder();
		Map<String, String> queries = builder.buildSelectQueryWithAlias(window,dbID);
		
		DBFormattedObjects result = new DBFormattedObjects();
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    String query  = queries.get(window.getMainEntity());
			Statement st = cnx.createStatement();
			System.out.println("[Data By ID]\n\t"+query);
			ResultSet res = st.executeQuery(query);
			
			if(!res.next())
				return null;
			
			//	Get main entity
			for(CAttribute attribute : window.getCAttributes()){
				if(attribute.getCAttributetype().getId() == 6 || attribute.isMultiple())	//	Fichier ou multiple
					continue;
				if(attribute.getEntity().getDataReference().equals(window.getMainEntity())){
					String prop = attribute.getEntity().getDataReference()+"___"+attribute.getDataReference();
					if(prop.length()>60)
						prop = prop.substring(0,60);
					result.getMainEntity().put(attribute.getAttribute(), res.getObject(prop)==null?"":res.getObject(prop).toString());
				}
			}
			
			
			
			//	Build all referenced tables as otm
			Map<String,String> entities = new LinkedHashMap<String,String>();
			for(CAttribute attribute : window.getCAttributes()){
				if(attribute.isMultiple())
					continue;
				String dataref = attribute.getEntity().getDataReference();
				String name = attribute.getEntity().getName();
				
				if(entities.containsKey(name) || dataref.equals(window.getMainEntity()))
					continue;
				else
					entities.put(name, dataref);
			}
			
			result.setOtmEntities(new LinkedHashMap<String, Map<String,String>>());
			for(String table : queries.keySet()) {
				if(table.equals(window.getMainEntity())){
					continue;
				}
				String col = window.getMainEntity()+"___fk_"+table;
				ResultSetMetaData rsmd = res.getMetaData();
				boolean found = false;
				for(int i = 1 ; i <= rsmd.getColumnCount();i++){
					if(rsmd.getColumnLabel(i).equals(col)){
						found = true;
						break;
					}
				}
				if(!found)
					continue;
				if(res.getObject(window.getMainEntity()+"___fk_"+table)!=null) {
					String subQ  = queries.get(table)+res.getObject(window.getMainEntity()+"___fk_"+table).toString();
					Statement subSt = cnx.createStatement();
					
					ResultSet subRes = subSt.executeQuery(subQ);
					if(!subRes.next()) {
						continue;
					}
					//	Get OTM Entities
					for(String name : entities.keySet()){
						String ref = entities.get(name);
						Map<String, String> currentOTM = new LinkedHashMap<String, String>();
						for(CAttribute attribute : window.getCAttributes()){
							if(attribute.getEntity().getDataReference().equals(ref)){
								String prop = attribute.getEntity().getDataReference()+"___"+attribute.getDataReference();
								currentOTM.put(attribute.getAttribute(), subRes.getObject(prop)==null?"":subRes.getObject(prop).toString());
							}
						}
						if(currentOTM.size()>0)
							result.getOtmEntities().put(name, currentOTM);
					}
				} else {
					for(String name : entities.keySet()){
						String ref = entities.get(name);
						Map<String, String> currentOTM = new LinkedHashMap<String, String>();
						for(CAttribute attribute : window.getCAttributes()){
							if(attribute.getEntity().getDataReference().equals(ref)){
								//String prop = attribute.getEntity().getDataReference()+"___"+attribute.getDataReference();
								currentOTM.put(attribute.getAttribute(), "");
								if(attribute.getDataReference().startsWith("fk_")) {
									attribute.setReference(true);
								}
							}
						}
						if(currentOTM.size()>0)
							result.getOtmEntities().put(name, currentOTM);
					}
				}
			}
			
			//	Get referenced entities
			
			return result;
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		
		return null;
	}
	
	public Boolean executeInsertAction(CWindow window,
			List<UIControlElement> controls, List<MtmDTO> dtos, CoreUser u) {
	 return executeInsertAction(window, controls, dtos, u, null);	
	}

	public Boolean executeInsertAction(CWindow window,
			List<UIControlElement> controls, List<MtmDTO> dtos, CoreUser u, Map<String, String> refForeignKeys) {
		
		
		//	Check if this row can be inserted
		List<CAttribute> uattributes = new ArrayList<CAttribute>();
		CBusinessClass bc = this.getReferencedTable(window.getMainEntity());
		uattributes = getUniqueAttributes(bc);
		Map<CAttribute, String> newVals = new HashMap<CAttribute, String>();
		for(CAttribute a : uattributes){
			for(UIControlElement e : controls){
				if(e.getAttribute().getId() == a.getId()){
					newVals.put(a, e.getControlValue());
					break;
				}
			}
		}
		boolean existent = false;
		
		if(newVals.size()>0){
			String fields = "";
			
			for(CAttribute a : newVals.keySet()){
				fields = fields+" AND "+a.getDataReference()+"='"+newVals.get(a)+"' ";
			}
			
			fields = fields.substring(5);
			
			String sql = "select pk_"+window.getMainEntity()+" from "+window.getMainEntity() +" WHERE "+fields;
			try{
				Connection cnx=ProtogenConnection.getInstance().getConnection();
				Statement st = cnx.createStatement();
				ResultSet rs = st.executeQuery(sql);
				existent = rs.next();
				
				rs.close();
				st.close();
					
			}catch(Exception exc){
				
			}
		}
		
		if(existent){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Impossible d'enregistrer les données","Cet enreigstrement semble dupliqué, prière de vérifier les données"));
			return false;
		}
		
		//	Get all references
		Map<String, String> references = new LinkedHashMap<String, String>();
			for(UIControlElement c : controls){
				if(c.isReference() && !c.getControlValue().equals("0") && c.getAttribute().getId()!=0)
					references.put(c.getAttribute().getDataReference(), c.getControlValue());
			}
		
		List<String> tables = new ArrayList<String>();
		Map<String, List<String>> attributes = new LinkedHashMap<String, List<String>>();
		Map<String, String> foreignKeys;
		if(refForeignKeys == null){
			foreignKeys = new LinkedHashMap<String, String>();
		} else {
			foreignKeys = refForeignKeys;
		}
		boolean[] flags;
		
		//	Get tables
		for(UIControlElement element : controls){
			if(element.isReference())
				continue;
			if(element.isReadOnly())
				continue;
			String tableRef = element.getAttribute().getEntity().getDataReference();
			if(tables.contains(tableRef))
				continue;
			tables.add(tableRef);
		}
		
		if(tables.size()==0)
			tables.add(window.getMainEntity());
		
		for(String table : tables){
			List<String> tableAttributes = getTableAttributes(table);
			attributes.put(table, tableAttributes);
		}
		
		//	Iterate over tables
		flags = new boolean[tables.size()];
		while(!check(flags)){
			for(String table : tables){
				int index = tables.indexOf(table);
				if(flags[index])
					continue;
				
				boolean ready = true;
				for(String attribute : attributes.get(table)){
					if(attribute.startsWith("fk_")){
						String referenced = attribute.substring(3);
						for(String otherTable : tables){
							if(referenced.equals(window.getMainEntity()))	//	Reflexive references
								continue;
							if(otherTable.equals(referenced) && !flags[tables.indexOf(otherTable)]){
								ready = false;
								break;
							}
						}
					}
				}
				
				if(ready){
					Map<String, String> vals = new LinkedHashMap<String, String>();
					for(UIControlElement element : controls){
						if(element.getAttribute().getId()==0)
							continue;
						if(element.getAttribute().getEntity().getDataReference().equals(table)){
							if(element.isBinaryContent() || (element.isReference() && element.getControlValue().equals("0")))
								continue;
							if(element.isReadOnly())
								continue;
							if(element.getAttribute().getCAttributetype().getId()==5){
								//	Type heure
								String h = element.getControlValue().split(":")[0]+element.getControlValue().split(":")[1];
								vals.put(element.getAttribute().getDataReference(), h);
							}else {
								if(element.getAttribute().getCAttributetype().getId() == 7 ||element.getAttribute().getCAttributetype().getId() == 9)
									vals.put(element.getAttribute().getDataReference(), StringFormat.getInstance().format(element.getTrueValue()));
								else if(element.isCtrlDate()){
									Calendar c = Calendar.getInstance();
									c.setTime(element.getDateValue());
									String value = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
									vals.put(element.getAttribute().getDataReference(), value);
								}
								else {
									if((element.getControlValue()==null || element.getControlValue().length()==0) && 
												element.getAttribute().getDefaultValue()!=null && element.getAttribute().getDefaultValue().length()>0)
										element.setControlValue(SpecialValuesEngine.getInstance().parseSpecialValues(element.getAttribute().getDefaultValue()));
									vals.put(element.getAttribute().getDataReference(), StringFormat.getInstance().format(element.getControlValue()));
								}
							}
						}
					}
					
					for(String attribute : attributes.get(table)){
						if(attribute.startsWith("fk_")){
							if(!foreignKeys.containsKey(attribute))
								continue;
							String value = foreignKeys.get(attribute);
							vals.put(attribute, value);
						}
					}
					
					for(CAttribute a : window.getCAttributes()){
						if(a.getEntity().getDataReference().equals(table)){
							for(String key : references.keySet()){
								if(a.getDataReference().equals(key.replace("pk_", "fk_")))
									vals.put(key.replace("pk_", "fk_"), references.get(key));
							}
						}
					}
					
					int paramID=0;
					for(UIControlElement c : controls){
						if(c.getAttribute().getId()==0 && c.isReference())
							paramID=Integer.parseInt(c.getControlValue()); 
					}
					
					String query = builder.buildInsertQuery(table, vals,u, paramID);
					try{
						

					    Connection cnx=ProtogenConnection.getInstance().getConnection();
					    
						Statement st = cnx.createStatement();
						
						st.execute(query);
						
						int pk = getLastInserted(table);
						foreignKeys.put("fk_"+table, ""+pk);
						st.close();
						
					}catch(Exception exc){
						exc.printStackTrace();
						return new Boolean(false);
					}
					
					flags[index] = true;
				}
				
				
			}
			
			
			
					//			Get auto values
			try{
				

			    Connection cnx=ProtogenConnection.getInstance().getConnection();
			    
				Statement st = cnx.createStatement();
				autovalues = new LinkedHashMap<String, Integer>();
				for(CAttribute a : window.getCAttributes()){
					if(!a.isAutoValue())
						continue;
					String seq = "autoseq_"+a.getEntity().getDataReference()+"_"+a.getDataReference();
					int value=0;
					String sequery = "select nextval('"+seq+"')";
					ResultSet rs = st.executeQuery(sequery);
					if(rs.next())
						value = rs.getInt(1)-1;
					rs.close();
					autovalues.put(a.getAttribute(), new Integer(value));
					if(value>0){
						sequery = "select setval('"+seq+"',"+value+")";
						st.execute(sequery);
					}
				}
			}catch(Exception exc){
				exc.printStackTrace();
				return new Boolean(false);
			}
		}
		
		//	Binary fields
		
		//		Update binary content fields
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			for(UIControlElement e : controls){
				if(!e.isBinaryContent() || e.getContent()==null)
					continue;
				
				String table =e.getAttribute().getEntity().getDataReference();
				int id = 0;
				
				String sequence = e.getAttribute().getEntity().getDataReference()+"_seq";
				String sql = "select nextval('"+sequence+"')";
				ResultSet rs = st.executeQuery(sql);
				if(rs.next())
					id = rs.getInt(1)-1;
				rs.close();
				
				InputStream is = new FileInputStream(e.getContent());
				File f = new File(e.getContent());
				int length = (int)f.length();
					
				sql = "update "+table+" set "+e.getAttribute().getDataReference()+"=? where pk_"+table+"=?";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setBinaryStream(1, is,length);
				ps.setInt(2, id);
				ps.execute();
				ps.close();
				
				if(f.exists())
					f.delete();
			}
		} catch(Exception exc){
			exc.printStackTrace();
		}
		
		/*
		 * Prepare metadeata for event logs
		 */
		operation = DataDefinitionOperation.INSERT;
		entity = this.getReferencedTable(window.getMainEntity());
		dbID = getLastInserted(window.getMainEntity());
		this.window = window;
		
		//	insert mtm
		List<String> queries = builder.getMtmInsertQueries(window, dbID, dtos);
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			for(String query : queries){
				st.execute(query);
			}
			st.close();
			
			
			return new Boolean(true);
		}catch(Exception exc){
			exc.printStackTrace();
			return new Boolean(false);
		}
		
	}
	
	private List<CAttribute> getUniqueAttributes(CBusinessClass bc) {
		String sql = "select id, data_reference from c_attribute where id_class=? and unicity=?";
		List<CAttribute> results = new ArrayList<CAttribute>();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, bc.getId());
			ps.setString(2, "Y");
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CAttribute a = new CAttribute();
				a.setId(rs.getInt(1));
				a.setDataReference(rs.getString(2));
				results.add(a);
				
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
			
		return results;
	}

	private int getLastInserted(String table) {
		// TODO Auto-generated method stub
		String query = builder.buildLastInsertedQuery(table);
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			if(rs.next()){
				return rs.getInt("pk_"+table); 
			}
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
		
		return 0;
	}

	private boolean check(boolean[] flags) {
		// TODO Auto-generated method stub
		for(boolean b : flags){
			if(!b)
				return false;
		}
		return true;
	}

	private List<String> getTableAttributes(String table) {
		// TODO Auto-generated method stub
		String query = "select * from "+table;
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			ResultSetMetaData metars = rs.getMetaData();
			List<String> result = new ArrayList<String>();
			for(int i = 1 ; i <= metars.getColumnCount() ; i++){
				result.add(metars.getColumnName(i));
			}
			return result;
		}catch(Exception exc){
			exc.printStackTrace();
			
		}

		return null;
	}
	public Boolean executeSaveAction(CWindow window,
			List<UIControlElement> controls, int ID, Map<String, String> foreignKeys, List<MtmDTO> dtos) {
		return executeSaveAction(window, controls, ID, foreignKeys, dtos, null);
	}
	public Boolean executeSaveAction(CWindow window,
			List<UIControlElement> controls, int ID, Map<String, String> foreignKeys, List<MtmDTO> dtos, List<MtmBlock> refBlocks) {
		// TODO Auto-generated method stub
		/*
		 * Prepare metadeata for event logs
		 */
		operation = DataDefinitionOperation.UPDATE;
		entity = this.getReferencedTable(window.getMainEntity());
		dbID = ID;
		this.window = window;
		
		List<String> queries = builder.buildUpdateQuery(window, controls, ID, foreignKeys, dtos, refBlocks);
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			for(String query : queries){
				try{
					System.out.println("SAVE QUERY \n\t"+query);
					st.execute(query);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			
			//	Update binary content fields
			for(UIControlElement e : controls){
				if(!e.isBinaryContent() || e.getContent()==null)
					continue;
				
				String table =e.getAttribute().getEntity().getDataReference();
				int id = 0;
				
				if(e.getAttribute().getEntity().getDataReference().equals(window.getMainEntity()))
					id=ID;
				else{
					for(String dr : foreignKeys.keySet())
						if(dr.equals(e.getAttribute().getEntity().getDataReference())){
							id=Integer.parseInt(foreignKeys.get(dr));
							break;
						}
				}
					
				
				InputStream is = new FileInputStream(e.getContent());
				File f = new File(e.getContent());
				int length = (int)f.length();
					
				String sql = "update "+table+" set "+e.getAttribute().getDataReference()+"=? where pk_"+table+"=?";
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setBinaryStream(1, is,length);
				ps.setInt(2, id);
				ps.execute();
				ps.close();
				
				f = new File(e.getContent());
				if(f.exists())
					f.delete();
				
			}
			
			//	Get auto values
			autovalues = new LinkedHashMap<String, Integer>();
			for(CAttribute a : window.getCAttributes()){
				if(!a.isAutoValue())
					continue;
				String seq = "autoseq_"+a.getEntity().getDataReference()+"_"+a.getDataReference();
				int value=0;
				String sequery = "select nextval('"+seq+"')";
				ResultSet rs = st.executeQuery(sequery);
				if(rs.next())
					value = rs.getInt(1)-1;
				rs.close();
				autovalues.put(a.getAttribute(), new Integer(value));
				if(value>0){
					sequery = "select setval('"+seq+"',"+value+")";
					st.execute(sequery);
				}
			}
			
			st.close();
			
			
			return new Boolean(true);
		}catch(Exception exc){
			exc.printStackTrace();
			return new Boolean(false);
		}
		
		
	}

	public Boolean deleteRow(CWindow window, String rowID, Map<String, String> foreignKeys, List<String> mtmTables) {
		// TODO Auto-generated method stub
		
		operation = DataDefinitionOperation.DELETE;
		entity = this.getReferencedTable(window.getMainEntity());
		dbID = Integer.parseInt(rowID);
		setDataToDelete(this.getDataByConstraint(entity, "pk_"+entity.getDataReference()+"="+dbID).get(0));
		this.window = window;
		
		builder = new QueryBuilder();
		List<String> queries = builder.buildDeleteQuery(window, rowID, foreignKeys, mtmTables);
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			for(String query : queries){
				st.execute(query);
			}
			st.close();
			
			
			return new Boolean(true);
		}catch(Exception exc){
			exc.printStackTrace();
			return new Boolean(false);
		}
		
	}

	public List<Map<String, String>> getForeignKeys() {
		return foreignKeys;
	}

	public void setForeignKeys(List<Map<String, String>> foreignKeys) {
		this.foreignKeys = foreignKeys;
	}

	public List<String> getMtmTables() {
		return mtmTables;
	}

	public void setMtmTables(List<String> mtmTables) {
		this.mtmTables = mtmTables;
	}
	
	
	/*
	 * 			MTM TOOLS EXTENSION
	 */
	public List<CBusinessClass> getMtmEntities(CBusinessClass entityReference){
		builder = new QueryBuilder();
		String queries = builder.buildGetMtmQuery(entityReference);
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(queries);
			List<CBusinessClass> results = new ArrayList<CBusinessClass>();
			while(rs.next()){
				CBusinessClass mtmEntity = new CBusinessClass(rs.getInt("id"), rs.getString("entitydf"),true);
				mtmEntity.setName(rs.getString("name"));
				results.add(mtmEntity);
			}
			rs.close();
			st.close();
			
			
			return results;
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
		return null;
	}
	
	public CBusinessClass getReferencedTable(String reference){
		builder = new QueryBuilder();
		String queries = builder.buildGetMtmRefQuery(reference);
		
		try{
			
			Connection cnx=null;
			try{
				cnx=ProtogenConnection.getInstance().getConnection();
			}catch(Exception e){
				cnx = WebSessionManager.getInstance().getCnx();
				System.out.println("DATA BASE IN OFFLINE MODE");
			}
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(queries);
			CBusinessClass entity = new CBusinessClass();
			if(rs.next()){
				entity = new CBusinessClass(rs.getInt("id"), rs.getString("data_reference"),true);
				entity.setName(rs.getString("name"));
				
			}
			entity = populateEntity(entity);
			
			
			return entity;
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
		return null;
	}
	
	public CBusinessClass populateEntity(CBusinessClass origine){
		
		builder = new QueryBuilder();
		String queries = builder.buildEntityPopulateQuery(origine.getId());
		
		try{
			
			Connection cnx=null;
			try{
				cnx=ProtogenConnection.getInstance().getConnection();
			}catch(Exception e){
				cnx = WebSessionManager.getInstance().getCnx();
				
				System.out.println("DATA BASE IN OFFLINE MODE");
			}
			
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(queries);
			List<CAttribute> attributes = new ArrayList<CAttribute>();
			while(rs.next()){
				CAttribute attribute = new CAttribute();
				attribute.setAttribute(rs.getString("attribute"));
				attribute.setDataReference(rs.getString("data_reference"));
				attribute.setId(rs.getInt("id"));
				attribute.setKeyAttribute(rs.getInt("key_attribute")==1);
				attribute.setReference(rs.getString("reference").equals("Y"));
				attribute.setRequiresValidation(rs.getString("requires_validation").equals("Y"));
				attribute.setValidationFormula(rs.getString("validation_formula"));
				attribute.setMultiple(rs.getString("multiple").equals("Y"));
				CAttributetype type  = new CAttributetype(rs.getInt("id_attributetype"), "");
				attribute.setCAttributetype(type);
				
				attributes.add(attribute);
			}
			origine.setAttributes(attributes);
			
			
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
		return origine;
		
		
	}
	
	/*
	 * Get all data from a business entity
	 */
public List<Map<CAttribute, Object>> getAllData(CBusinessClass entity){
		
		String query = "select * from "+entity.getDataReference() + " where dirty='N' ";
		
		try{
			
			Connection cnx=null;
			try{
				cnx=ProtogenConnection.getInstance().getConnection();
			}catch(Exception e){
				cnx = WebSessionManager.getInstance().getCnx();
				System.out.println("DATA BASE IN OFFLINE MODE");
			}
			
			Statement st = cnx.createStatement();
			System.out.println("GET ALL DATA QUERY : \n\t"+query);
			ResultSet rs = st.executeQuery(query);
			List<Map<CAttribute, Object>> result = new ArrayList<Map<CAttribute,Object>>();
			
			
			ResultSetMetaData metars = rs.getMetaData();
			int count = metars.getColumnCount();
			List<String> headers = new ArrayList<String>();
			for(int i=0 ; i < count ; i++){
				String column = metars.getColumnName(i+1);
				headers.add(column);
			}
			while(rs.next()){
				Map<CAttribute, Object> datarow = new LinkedHashMap<CAttribute, Object>();
				for(CAttribute attribute : entity.getAttributes()){
					if(headers.contains(attribute.getDataReference()))
						datarow.put(attribute, rs.getObject(attribute.getDataReference()));
					else
						continue;
					
				}
				
				result.add(datarow);
			}
			
			
			rs.close();
			st.close();
			return result;
			
		} catch(Exception exc){
			exc.printStackTrace();
			
		}
		return null;
	}



	public List<Map<CAttribute, Object>> getDataByConstraint(
			CBusinessClass entity, String constraint) {
		
		String query = "select * from "+entity.getDataReference() + " where "+constraint +" and dirty='N' ";
		System.out.println("GET FILTRED DATA\n\t"+query);
		
		Connection cnx=null;
		try{
			cnx=ProtogenConnection.getInstance().getConnection();
		}catch(Exception e){
			cnx = WebSessionManager.getInstance().getCnx();
			System.out.println("DATA BASE IN OFFLINE MODE");
		}
		
		
		try{
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			List<Map<CAttribute, Object>> result = new ArrayList<Map<CAttribute,Object>>();
			
			while(rs.next()){
				Map<CAttribute, Object> datarow = new LinkedHashMap<CAttribute, Object>();
				for(CAttribute attribute : entity.getAttributes()){
					if(attribute.isMultiple())
						continue;
					datarow.put(attribute, rs.getObject(attribute.getDataReference()));
				}
				result.add(datarow);
			}
			
			return result;
			
		} catch(Exception exc){
			exc.printStackTrace();
		}
		return null;
	}

	public PairKVElement getDataKeyByID(String tableReference, int idRef) {
		// TODO Auto-generated method stub
		
		if(tableReference == null || tableReference.length()==0 || tableReference.equals("null"))
			return new PairKVElement("0","");
		
		String keyQuery = builder.createSelectKeyAttributes(tableReference);
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(keyQuery);
			List<String> keyAttributes = new ArrayList<String>();
			while(rs.next()){
				keyAttributes.add(rs.getString("data_reference"));				
			}
			rs.close();
			String query = builder.createSelectDataKeyByID(tableReference, idRef, keyAttributes);
			System.out.println("DATA KEY BY ID : \n\t"+query);
			rs = st.executeQuery(query);
			
			PairKVElement element = new PairKVElement(tableReference, "");
			if(rs.next()){
				String key = "";
				for(String a : keyAttributes){
					String k = rs.getString(a);
					if(k == null)
						k="";
					if(a.startsWith("fk_") && k.length()>0){
						int id = Integer.parseInt(k);
						String table = a.substring(3);
						PairKVElement p = getDataKeyByID(table,id);
						if(p!=null)
							k = p.getValue();
					}
					if(k.split(" ").length == 2 && k.split("-").length==3){	//	Date
						k = k.split(" ")[0];
						k = k.split("-")[2]+"/"+k.split("-")[1]+"/"+k.split("-")[0];
					}
					key = key+k+"  -  ";
				}
				if(key.length()>0)
					key = key.substring(0, key.length()-3);
				element = new PairKVElement(tableReference, key.trim());
				element.setDbID(idRef);
			}
			
			rs.close();
			st.close();
			
			
			return element;
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return new PairKVElement("0","");
	}

	public List<PairKVElement> getDataKeys(String referenceTable, boolean restrict, int iduser, List<String> constraintDecomposited) {
		// TODO Auto-generated method stub
				String keyQuery = builder.createSelectKeyAttributes(referenceTable);
				OrganizationDAL odal = new OrganizationDAL();
				CBusinessClass referenceBC = this.getReferencedTable(referenceTable);
				
				// Get applied models
				ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
				CoreRole role = cache.getUser().getCoreRole();
				List<GParametersInstance> models = cache.getParameterPackages();
				for(GParametersInstance m : models){
					List<CBusinessClass> modelEntities = odal.loadModelEntities(m);
					m.getModelPackage().setImplicatedEntities(modelEntities);
					
				}
				
				//	Check if entity belongs to any model
				List<String> parameters = new ArrayList<String>();
				for(GParametersInstance m : models){
					boolean foundFlag=false;
					for(CBusinessClass bc : m.getModelPackage().getImplicatedEntities()){
						if(bc.getId() == referenceBC.getId()){
							foundFlag = true;
							break;
						}
					}
					
					if(!foundFlag)
						break;
					
					//	Get allowed values
					List<GParameterValues> values = odal.loadAllowedValues(m, referenceBC);
					if(values.size()>0){
						String p = "pk_"+referenceTable+" in (";
						for(GParameterValues pv : values){
							p = p+pv.getRowDbId()+",";
						}
						if(p.length()>1);
						p = p.substring(0,p.length()-1); //	remove the last  comma
						p = p+")";
						parameters.add(p);
					}
						
				}
				
				//	Now check for role constraints
				if(role.getConstraints() != null && role.getConstraints().size()>0){
					for(CoreDataAccessRight r : role.getConstraints()){
						if(r.getEntity().getId() == referenceBC.getId()){
							String p = "pk_"+referenceTable+"="+r.getValue();
							parameters.add(p);
							break;
						}
					}
				}
				
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
					List<String> keyAttributes = new ArrayList<String>();
					while(rs.next()){
						keyAttributes.add(rs.getString("data_reference"));				
					}
					rs.close();
					
					String constraint = constraintDecomposited.get(0)+constraintDecomposited.get(1)+constraintDecomposited.get(2);
					String order = "order by pk_"+referenceTable+" asc";
					
					String query = builder.createSelectDataKeys(referenceTable, keyAttributes,restrict,iduser,parameters);
					
					
					if(query.toLowerCase().contains(" where "))
						query = query.replaceAll(order, " AND "+constraint+" "+order);
					else
						query = query.replaceAll(order, " WHERE "+constraint+" "+order);
					
					
					System.out.println("*****************\n"+query+"\n*****************");
					rs = st.executeQuery(query);
					
					List<PairKVElement> results = new ArrayList<PairKVElement>();
					while(rs.next()){
						Integer id = new Integer(rs.getInt("pk_"+referenceTable));
						String key = "";
						for(String a : keyAttributes){
							String k = rs.getString(a);
							if(k==null)
								continue;
							if(k.split(" ").length == 2 && k.split("-").length==3)	//	Date
								k = k.split(" ")[0];
							key = key+k+"  -  ";
						}
						if(key.length()>0)
							key = key.substring(0,key.length()-3);
						
						if(key.length()>30)
							key = key.substring(0, 30)+"...";
						results.add(new PairKVElement(""+id, key.trim()));
					}
					
					rs.close();
					st.close();
					
					
					return results;
					
				}catch(Exception exc){
					exc.printStackTrace();
				}
				
				return null;
	}
	
	public List<PairKVElement> getDataKeys(String referenceTable, boolean restrict, int iduser) {
		// TODO Auto-generated method stub
		String keyQuery = builder.createSelectKeyAttributes(referenceTable);
		OrganizationDAL odal = new OrganizationDAL();
		CBusinessClass referenceBC = this.getReferencedTable(referenceTable);
		System.out.println(keyQuery);
		ApplicationCache cache = null;
		try{
			cache = ApplicationRepository.getInstance().
					getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		}catch(Exception exc){
			System.out.println("ACCES FROM WS");
			//exc.printStackTrace();
		}
		List<String> parameters = new ArrayList<String>();
		
		//	Constraints
		if(cache !=null) {
			List<CoreDataConstraint> constraints = cache.getConstraints();
			
			for(CoreDataConstraint c : constraints){
				if(c.getEntity().getId() == referenceBC.getId()){
					String p = "pk_"+referenceTable+"="+c.getBeanId();
					parameters.add(p);
				}
			}
		}
		// Get applied models
		try{
			CoreRole role = cache.getUser().getCoreRole();
			List<GParametersInstance> models = new ArrayList<GParametersInstance>();
			if(cache != null)
				models = cache.getParameterPackages();
			
			for(GParametersInstance m : models){
				List<CBusinessClass> modelEntities = odal.loadModelEntities(m);
				m.getModelPackage().setImplicatedEntities(modelEntities);
				
			}
			
			
			for(GParametersInstance m : models){
				boolean foundFlag=false;
				for(CBusinessClass bc : m.getModelPackage().getImplicatedEntities()){
					if(bc.getId() == referenceBC.getId()){
						foundFlag = true;
						break;
					}
				}
				
				if(!foundFlag)
					break;
				
				//	Get allowed values
				List<GParameterValues> values = odal.loadAllowedValues(m, referenceBC);
				if(values.size()>0){
					String p = "pk_"+referenceTable+" in (";
					for(GParameterValues pv : values){
						p = p+pv.getRowDbId()+",";
					}
					if(p.length()>1);
					p = p.substring(0,p.length()-1); //	remove the last  comma
					p = p+")";
					parameters.add(p);
				}
					
			}
			
			//	Now check for role constraints
			if(role.getConstraints() != null && role.getConstraints().size()>0){
				for(CoreDataAccessRight r : role.getConstraints()){
					if(r.getEntity().getId() == referenceBC.getId()){
						String p = "pk_"+referenceTable+"="+r.getValue();
						parameters.add(p);
						break;
					}
				}
			}
		}catch(Exception exc){
			System.out.print("NO PARAMETER");
		}
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
			List<String> keyAttributes = new ArrayList<String>();
			while(rs.next()){
				keyAttributes.add(rs.getString("data_reference"));				
			}
			rs.close();
			List<PairKVElement> results = new ArrayList<PairKVElement>();
			if(keyAttributes == null || keyAttributes.size()==0)
				return results;
			String query = builder.createSelectDataKeys(referenceTable, keyAttributes,restrict,iduser,parameters);
			System.out.println("*****************\n"+query+"\n*****************");
			rs = st.executeQuery(query);
			
			
			while(rs.next()){
				Integer id = new Integer(rs.getInt("pk_"+referenceTable));
				String key = "";
				for(String a : keyAttributes){
					String k = rs.getString(a);
					if(k==null)
						continue;
					if(k.split(" ").length == 2 && k.split("-").length==3)	//	Date
						k = k.split(" ")[0];
					key = key+k+"  -  ";
				}
				if(key.length()>0)
					key = key.substring(0,key.length()-3);
				
				if(key.length()>30)
					key = key.substring(0, 30)+"...";
				results.add(new PairKVElement(""+id, key.trim()));
			}
			
			rs.close();
			st.close();
			
			
			return results;
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return new ArrayList<PairKVElement>();
	}
	
	public List<PairKVElement> getDataKeysMultiLVL(String referenceTable, boolean restrict, int iduser) {
		// TODO Auto-generated method stub
		String keyQuery = builder.createSelectKeyAttributes(referenceTable);
		OrganizationDAL odal = new OrganizationDAL();
		CBusinessClass referenceBC = this.getReferencedTable(referenceTable);
		System.out.println(keyQuery);
		ApplicationCache cache = null;
		try{
			cache = ApplicationRepository.getInstance().
					getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		}catch(Exception exc){
			System.out.println("ACCES FROM WS");
			//exc.printStackTrace();
		}
		List<String> parameters = new ArrayList<String>();
		
		//	Constraints
		if(cache !=null) {
			List<CoreDataConstraint> constraints = cache.getConstraints();
			
			for(CoreDataConstraint c : constraints){
				if(c.getEntity().getId() == referenceBC.getId()){
					String p = "pk_"+referenceTable+"="+c.getBeanId();
					parameters.add(p);
				}
			}
		}
		// Get applied models
		try{
			CoreRole role = cache.getUser().getCoreRole();
			List<GParametersInstance> models = new ArrayList<GParametersInstance>();
			if(cache != null)
				models = cache.getParameterPackages();
			
			for(GParametersInstance m : models){
				List<CBusinessClass> modelEntities = odal.loadModelEntities(m);
				m.getModelPackage().setImplicatedEntities(modelEntities);
				
			}
			
			
			for(GParametersInstance m : models){
				boolean foundFlag=false;
				for(CBusinessClass bc : m.getModelPackage().getImplicatedEntities()){
					if(bc.getId() == referenceBC.getId()){
						foundFlag = true;
						break;
					}
				}
				
				if(!foundFlag)
					break;
				
				//	Get allowed values
				List<GParameterValues> values = odal.loadAllowedValues(m, referenceBC);
				if(values.size()>0){
					String p = "pk_"+referenceTable+" in (";
					for(GParameterValues pv : values){
						p = p+pv.getRowDbId()+",";
					}
					if(p.length()>1);
					p = p.substring(0,p.length()-1); //	remove the last  comma
					p = p+")";
					parameters.add(p);
				}
					
			}
			
			//	Now check for role constraints
			if(role.getConstraints() != null && role.getConstraints().size()>0){
				for(CoreDataAccessRight r : role.getConstraints()){
					if(r.getEntity().getId() == referenceBC.getId()){
						String p = "pk_"+referenceTable+"="+r.getValue();
						parameters.add(p);
						break;
					}
				}
			}
		}catch(Exception exc){
			System.out.print("NO PARAMETER");
		}
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
			List<String> keyAttributes = new ArrayList<String>();
			while(rs.next()){
				keyAttributes.add(rs.getString("data_reference"));				
			}
			rs.close();
			List<PairKVElement> results = new ArrayList<PairKVElement>();
			if(keyAttributes == null || keyAttributes.size()==0)
				return results;
			String query = builder.createSelectDataKeys(referenceTable, keyAttributes,restrict,iduser,parameters);
			System.out.println("*****************\n"+query+"\n*****************");
			rs = st.executeQuery(query);
			
			
			while(rs.next()){
				Integer id = new Integer(rs.getInt("pk_"+referenceTable));
				String key = "";
				for(String a : keyAttributes){
					String k = rs.getString(a);
					if(k==null)
						continue;
					
					if(a.startsWith("fk_")){
						PairKVElement pkv = this.getDataKeyByID(a.substring(3), Integer.parseInt(k));
						key = key+pkv.getValue()+" - ";
						continue;
					}
					
					if(k.split(" ").length == 2 && k.split("-").length==3)	//	Date
						k = k.split(" ")[0];
					key = key+k+"  -  ";
				}
				if(key.length()>0)
					key = key.substring(0,key.length()-3);
				
				if(key.length()>30)
					key = key.substring(0, 30)+"...";
				results.add(new PairKVElement(""+id, key.trim()));
			}
			
			rs.close();
			st.close();
			
			
			return results;
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return new ArrayList<PairKVElement>();
	}

	public int executePost(MPostAction postAction, String mainEntity, Integer integer,
			Map<String, String> postActionParameters, Map<String, String> processParameters, List<HeaderExecutionResult> headers, 
				int index, Map<CAttribute, String> keyMap, CoreUser org) {
		// TODO Auto-generated method stub
		
			// Check if this row can be inserted
			String dref = postAction.getAttributes().get(0).getEntity().getDataReference();
			List<CAttribute> uattributes = new ArrayList<CAttribute>();
			CBusinessClass bc = this.getReferencedTable(dref);
			uattributes = getUniqueAttributes(bc);
			Map<CAttribute, String> newVals = new HashMap<CAttribute, String>();
			for(CAttribute a : uattributes){
				for(int i = 0 ; i < postAction.getAttributes().size() ; i++){
					CAttribute paa = postAction.getAttributes().get(i);
					if(paa.getId() == a.getId()){
						String key = postAction.getParametersValues().get(i);
						key = key.replaceAll("<<", "");
						key = key.replaceAll(">>", "");
						String val="";
						if(processParameters.containsKey(key))
							val = processParameters.get(key);
						else if(postActionParameters.containsKey(key))
							val = postActionParameters.get(key);
						else if(postActionParameters.containsKey(StringFormat.getInstance().attributeDataReferenceFormat(key)))
							val = postActionParameters.get(StringFormat.getInstance().attributeDataReferenceFormat(key));
						
						if(val.length()==0)
							continue;
						 
						newVals.put(a, val);
					}
				}
			}
			boolean existent = false;
			
			if(newVals.size()>0){
				String fields = "";
				
				for(CAttribute a : newVals.keySet()){
					fields = fields+" AND "+a.getDataReference()+"='"+newVals.get(a)+"' ";
				}
				
				fields = fields.substring(5);
				
				String sql = "select pk_"+dref+" from "+dref +" WHERE "+fields;
				try{
					Connection cnx=ProtogenConnection.getInstance().getConnection();
					Statement st = cnx.createStatement();
					ResultSet rs = st.executeQuery(sql);
					existent = rs.next();
					
					rs.close();
					st.close();
						
				}catch(Exception exc){
					
				}
			}
			
			if(existent){
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Impossible d'enregistrer les données","Cet enreigstrement semble dupliqué, prière de vérifier les données"));
				return -1;
			}
		
		
		
		
		
		
		String query = builder.generatePostActionQuery(postAction, mainEntity, integer, postActionParameters, processParameters);
		int id=0;
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			System.out.println("POST ACTION INSERT \n\t"+query);
			
			st.execute(query);
			
			String targetTable = postAction.getAttributes().get(0).getEntity().getDataReference();
			
			String sql = "select nextval('"+targetTable+"_seq')";
			ResultSet rs = st.executeQuery(sql);
			int mtmID = 0;
			if(rs.next())
				mtmID=rs.getInt(1)-1;
			rs.close();
			
			
			//	Insert organization
			sql = "update "+targetTable+" set protogen_user_id="+org.getOrgInstance() +" where pk_"+targetTable+"="+mtmID;
			st.execute(sql);
			
			id=mtmID;
			
			//	Get all mtm tables that I have to insert
			List<String> mtmTables = new ArrayList<String>();
			for(CAttribute a : postAction.getAttributes()){
				if(a.isMultiple() && a.getEntity().getDataReference().equals(targetTable)){
					int i=0;
					i = postAction.getAttributes().indexOf(a);
					String vpar = postAction.getParametersValues().get(i).replaceAll("<<", "").replaceAll(">>", "");
					boolean flag=false;
					if(vpar.startsWith("multiple:"))
						flag = true;
						
					if(postAction.getParametersValues().get(i)!=null && postAction.getParametersValues().get(i).length()>0)
						mtmTables.add(a.getDataReference().split("__")[0].substring(3)+((flag)?"--H":""));
				}
			}
			
			for(String table : mtmTables){
				List<String> queries = new ArrayList<String>();
				if(table.endsWith("--H")){
					table = table.substring(0,table.length()-3);
					queries = builder.buildPostActionHeadersQuery(table, headers, postAction, mtmID, index, mainEntity, integer,keyMap, postActionParameters);
				} else
					queries = builder.buildPostActionMTMQuery(table, headers, postAction, mtmID, index, mainEntity, integer,keyMap, postActionParameters);
				for(String q : queries)
					st.execute(q);
			}
			
			/*
			for(HeaderExecutionResult her : headers){
				List<String> queries = builder.buildPostActionMtmQuery(her, postAction, mtmID, index);
				for(String q : queries)
					st.execute(q);
			}
			*/
			st.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return id;
		
	}

	public Map<String, Double> getCalulationData(String entity,
			List<String> attributes, int entityID) {
		// TODO Auto-generated method stub
		
		String selectQuery = builder.buildSelectQuery(entity, attributes, entityID);
		Map<String, Double> results = new LinkedHashMap<String, Double>();
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(selectQuery);
			if(rs.next()){
				for(String a : attributes)
					results.put(a, new Double(rs.getDouble(a)));
			}
			
			return results;
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return null;
	}

	public List<Map<String, Double>> getCalulationData(String entity,
			String refTable, List<String> attributes, int entityID) {
		// TODO Auto-generated method stub
		String selectQuery = builder.buildSelectQuery(entity, refTable, attributes, entityID);
		List<Map<String, Double>> results = new ArrayList<Map<String, Double>>();
		
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			
			ResultSet rs = st.executeQuery(selectQuery);
			while(rs.next()){
				Map<String, Double> rowResults = new LinkedHashMap<String, Double>();
				for(String a : attributes)
					rowResults.put(a, new Double(rs.getDouble(a)));
				
				results.add(rowResults);
			}
			
			return results;
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return null;
	}

	public Map<Integer, String> getDataKeys(String referenceTable,
			List<String> wheres) {
		// TODO Auto-generated method stub
		
		String keyQuery = builder.createSelectKeyAttributes(referenceTable);
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(keyQuery);
			List<String> keyAttributes = new ArrayList<String>();
			while(rs.next()){
				keyAttributes.add(rs.getString("data_reference"));				
			}
			
			rs.close();
			wheres.add("dirty='N'");
			String query = builder.createSelectDataKeys(referenceTable, keyAttributes,wheres);
			System.out.println(query);
			rs = st.executeQuery(query);
			
			Map<Integer, String> results = new LinkedHashMap<Integer, String>();
			while(rs.next()){
				Integer id = new Integer(rs.getInt("pk_"+referenceTable));
				String key = "";
				for(String a : keyAttributes){
					String k=rs.getString(a);
					if(k==null)
						k="";
					if(k.split(" ").length == 2 && k.split("-").length==3)	//	Date
						k = k.split(" ")[0];
					key = key+k+" - ";
				}
				if(key.length()>0)
					key = key.substring(0,key.length()-3);
				
				if(key.length()>30)
					key=key.substring(0,30)+"...";
				
				results.put(id, key.trim());
			}
			
			rs.close();
			st.close();
			
			
			return results;
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return null;
		
	}
	
	public Map<Integer, String> getDataKeys(String referenceTable,
			List<String> wheres, List<String> keyWords) {
		// TODO Auto-generated method stub
		
		String keyQuery = builder.createSelectKeyAttributes(referenceTable);
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(keyQuery);
			List<String> keyAttributes = new ArrayList<String>();
			List<Integer> attType = new ArrayList<Integer>();
			while(rs.next()){
				keyAttributes.add(rs.getString("data_reference"));
				attType.add(rs.getInt("id_attributetype"));
			}
			
			rs.close();
			String query = builder.createSelectDataKeys(referenceTable, keyAttributes, attType, wheres, keyWords);
			System.out.println(query);
			rs = st.executeQuery(query);
			
			Map<Integer, String> results = new LinkedHashMap<Integer, String>();
			while(rs.next()){
				Integer id = new Integer(rs.getInt("pk_"+referenceTable));
				String key = "";
				for(String a : keyAttributes){
					String k=rs.getString(a);
					if(k==null)
						k="";
					if(k.split(" ").length == 2 && k.split("-").length==3)	//	Date
						k = k.split(" ")[0];
					key = key+k+" - ";
				}
				if(key.length()>0)
					key = key.substring(0,key.length()-3);
				
				if(key.length()>30)
					key=key.substring(0,30)+"...";
				
				results.put(id, key.trim());
			}
			
			rs.close();
			st.close();
			
			
			return results;
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return null;
		
	}
	
	public double getReferencedValueForFormula(CAttribute attribute,
			String controlValue, String attname) {
		// TODO Auto-generated method stub
		double result = 0;
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    Statement st = cnx.createStatement();
		    
		    String query = "select c_attribute.data_reference from c_attribute, c_businessclass where c_attribute.id_class=c_businessclass.id and c_businessclass.data_reference='"+attribute.getDataReference().substring(3)+"' " +
		    		" and c_attribute.attribute='"+attname.replace("\'", "''")+"'";
		    String dataReference="";
			ResultSet rs = st.executeQuery(query);
			if(rs.next()){
				dataReference = rs.getString(1);
			}
			rs.close();
			
			//	Get the value
			String table = attribute.getDataReference().substring(3);
			query = "select "+dataReference+" from "+table+" where pk_"+table+"="+controlValue;
			
			System.out.println("REFERENCED VALUE FROM FORMULA : "+query);
			
			rs = st.executeQuery(query);
			if(rs.next()){
				result = rs.getDouble(1);
			}
			rs.close();
			st.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
		return result;
	}

	public void updateGlobalValues(List<UIControlElement> globalControls) {
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    for(UIControlElement e : globalControls){
		    	String query = "update c_globalvalue set \"value\"=? where id=?";
		    	PreparedStatement ps = cnx.prepareStatement(query);
		    	ps.setString(1, e.getControlValue());
		    	ps.setInt(2, Integer.parseInt(e.getControlID()));
		    	ps.execute();
		    	ps.close();
		    }
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
	}
	
	public InputStream getStream(CAttribute selectedFca, CWindow window,
			int intValue) {
		InputStream is = null;
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "";
		    if(selectedFca.getEntity().getDataReference().equals(window.getMainEntity()))
		    	sql = "select "+selectedFca.getDataReference()+" from "+window.getMainEntity()+" where pk_"+window.getMainEntity()+"=?";
		    else
		    	sql = "select "+selectedFca.getDataReference()+" from "+window.getMainEntity()+","+selectedFca.getEntity().getDataReference()+" " +
		    			"where pk_"+window.getMainEntity()+"=? AND fk_"+selectedFca.getEntity().getDataReference()+"=pk_"+selectedFca.getEntity().getDataReference();
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, intValue);
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	is = rs.getBinaryStream(1);
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
		// TODO Auto-generated method stub
		return is;
	}
	
	public String getDataByID(String table, String dataReference, String dbID) {
		// TODO Auto-generated method stub
		String value="";
		if(dbID == null || dbID.equals(""))
			return value;
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    String sql = "select "+dataReference+" from "+table+" where pk_"+table+"="+dbID;
		    Statement st = cnx.createStatement();
		    System.out.println("[GET DATA BY ID] \n\t"+sql);
		    ResultSet rs = st.executeQuery(sql);
		    if(rs.next())
		    	value = ""+rs.getObject(1);
		    
		    rs.close();
		    st.close();
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
			
		}  
		
		return value;
	}
	
	public String getDatumByReferenceID(String tableReference, String adr,
			String rappelReference, int idReference) {
		String value = "";
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    Statement st = cnx.createStatement();
		    
		    String sql = "select "+adr+" from "+tableReference+" where "+rappelReference+"="+idReference+" order by pk_"+tableReference+" desc LIMIT 1";
		    ResultSet rs = st.executeQuery(sql);
		    if(rs.next())
		    	value = rs.getObject(1).toString();
		    
		    rs.close();
		    st.close();
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return value;
	}
	
	public double getGlobalValue(String globalKey, String appkey) {
		// TODO Auto-generated method stub
		double value = 0;
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    String sql = "select \"value\" from c_globalvalue where \"key\"=? and appkey=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, globalKey);
		    ps.setString(2, appkey);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	value = rs.getDouble(1);
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
		return value;
	}
	
	public List<Double> getValueDoubleReference(String refTable,
			String mtmTable, String lineTable, String lineAttribute, String dbID) {
		
		List<Double> results = new ArrayList<Double>();
		
		String sql = "select "+lineTable+"."+lineAttribute+" from "+mtmTable+", "+lineTable+" where " +
				" "+lineTable+".fk_"+lineTable+"__"+mtmTable+"="+mtmTable+".pk_"+mtmTable+" AND "+mtmTable+".fk_"+refTable+"="+dbID;
		try{
			

		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ResultSet rs = ps.executeQuery();
		    while(rs.next()){
		    	double d = rs.getDouble(1);
		    	results.add(new Double(d));
		    }
		    rs.close();
		    ps.close();
		    
		}catch(Exception exc){
			String dummy = "";
			dummy = dummy+exc.getLocalizedMessage();
			exc.printStackTrace();
		}
		
		return results;
	}
	
	public EntityDTO getDatumByID(String dataReference, int ID){
		EntityDTO dto = new EntityDTO();
		
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    //	Get entity and attributes
		    ApplicationLoader dal = new ApplicationLoader();
		    CBusinessClass entity = dal.getEntity(dataReference);
		    
		    //	Get Data
		    List<String> attDataReferences = new ArrayList<String>();
		    for(CAttribute a : entity.getAttributes())
		    	attDataReferences.add(a.getDataReference());
		    
		    QueryBuilder builder = new QueryBuilder();
		    
		    String sql = builder.buildSelectQuery(entity.getDataReference(), attDataReferences,ID);
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery();
		    dto.setEntity(entity);
		    dto.setValues(new LinkedHashMap<CAttribute, String>());
		    if(rs.next()){
		    	for(CAttribute a : entity.getAttributes()){
		    		if(a.getCAttributetype().getId()!=6){
		    			try{
		    				dto.getValues().put(a, rs.getObject(a.getDataReference()).toString());
		    			} catch(Exception e){
	    					dto.getValues().put(a, "");
	    				}
		    		}
		    	}
		    }
		    
		    rs.close();
		    ps.close();
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return dto;		
	}
	
	public int getForeignKeyValue(String referenceTable, int calcRowID,
			String dataReference) {
		int dr=0;
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select "+dataReference+" from "+referenceTable+" where pk_"+referenceTable+"="+calcRowID;
		    
		    Statement st = cnx.createStatement();
		    ResultSet rs = st.executeQuery(sql);
		    if(rs.next())
		    	dr = rs.getInt(1);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return dr;
	}
	
	public double getByPassedValue(String usefulField,
			String intermidiateTable, String mainDataReference,
			String mainEntity, Integer drID, Integer dbID, Map<CAttribute, String> keyValues) {
		double value = 0;
		
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    Statement st = cnx.createStatement();
		    
		    String sql = "select "+usefulField+" from "+intermidiateTable+" where "+mainDataReference+"="+drID+" AND fk_"+mainEntity+"="+dbID;
		    
		    for(CAttribute a : keyValues.keySet())
		    	sql=sql+" AND "+a.getDataReference()+"="+keyValues.get(a);
		    
		    ResultSet rs = st.executeQuery(sql);
		    
		    if(rs.next())						//	CECI EST MAUVAIS VU QU'IL N'Y A AUCUN AUTRE CRITERE PRIS EN CONSIDERATION, DANS LE CAS DE LA PAIE LA PERIODE N'EST PAS PRISE EN CONSIDERATION
		    	value=rs.getDouble(1);
		    
		    rs.close();
		    st.close();
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return value;
	}
	
	public CBusinessClass getEntityByName(String table) {
		CBusinessClass entity = new CBusinessClass();
		
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select id from c_businessclass where name=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setString(1, table);
		    ResultSet rs = ps.executeQuery();
		    
		    int id = 0;
		    if(rs.next()){
		    	id=rs.getInt(1);
		    }
		    rs.close();
		    ps.close();
		    
		    
		    entity.setId(id);
		    
		} catch(Exception e){
			e.printStackTrace();
		}
		
		entity = populateEntity(entity);
		
		return entity;
	}
	
	public double getInjectedValue(CBusinessClass entity, CAttribute vat, int iD) {
		double result = 0;
		
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select "+vat.getDataReference()+" from "+entity.getDataReference()+" where pk_"+entity.getDataReference()+"=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, iD);
		    ResultSet rs = ps.executeQuery();
		    
		    
		    if(rs.next()){
		    	result=rs.getDouble(1);
		    }
		    rs.close();
		    ps.close();
		    
		    
		    
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	public DTOProcessSession getProcessSession(CoreUser user) {
		DTOProcessSession ps = new DTOProcessSession();
		
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select id_process, id_atom,id from s_process_session where id_user=?";
		    
		    PreparedStatement p = cnx.prepareStatement(sql);
		    p.setInt(1, user.getId());
		    
		    ResultSet rs = p.executeQuery();
		    if(rs.next()){
		    	ps.setVoidFlag(false);
		    	SProcedure proc = new SProcedure();
		    	proc.setId(rs.getInt(1));
		    	SAtom at = new SAtom();
		    	at.setId(rs.getInt(2));
		    	ps.setUser(user);
		    	ps.setProcess(proc);
		    	ps.setAtom(at);
		    	ps.setId(rs.getInt(3));
		    }else {
		    	ps.setVoidFlag(true);
		    }
		    
		    rs.close();
		    p.close();
		    
		    
		}catch(Exception exc){
			exc.printStackTrace();
			
		}
		
		return ps;
	}
	
	public void clearProcessSession(DTOProcessSession ps) {
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "delete from s_process_session where id=?" ;
		    PreparedStatement p = cnx.prepareStatement(sql);
		    
		    p.setInt(1, ps.getId());
		    
		    p.execute();
		    p.close();
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void saveProcessSession(DTOProcessSession ps) {
		
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "delete from s_process_session where id_user=?" ;
		    PreparedStatement p = cnx.prepareStatement(sql);
		    
		    p.setInt(1, ps.getUser().getId());
		    
		    p.execute();
		    p.close();
		    
		    
		    sql = "insert into s_process_session (id_user,id_process, id_atom) values (?,?,?)";
		    p = cnx.prepareStatement(sql);
		    
		    p.setInt(1, ps.getUser().getId());
		    p.setInt(2, ps.getProcess().getId());
		    p.setInt(3, ps.getAtom().getId());
		    p.execute();
		    
		    p.close();
		    
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public CBusinessClass getAttributeEntity(int id) {
		CBusinessClass bc = new CBusinessClass();
		
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select b.data_reference, b.name from c_businessclass b, c_attribute a where a.id_class=b.id and a.id=?";
		    String tablename="";
		    String dataReference="";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ps.setInt(1, id);
		    
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	dataReference=rs.getString(1);
		    	tablename = rs.getString(2);
		    	
		    }
		    rs.close();
		    ps.close();
		    
		    
		    bc = getEntityByName(tablename);
		    
		    
		    bc.setDataReference(dataReference);
		    bc.setName(tablename);
		   }catch(Exception exc){
			exc.printStackTrace();
		}
		
		return bc;
	}
	
	public void lock(CAttribute a, int id) {
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "update "+a.getEntity().getDataReference()+" set "+a.getDataReference()+"='Oui' " +
		    		"where pk_"+a.getEntity().getDataReference()+"=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, id);
		    
		    ps.execute();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
	}
	
	public void unlock(CAttribute a, int id) {
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "update "+a.getEntity().getDataReference()+" set "+a.getDataReference()+"='Non' " +
		    		"where pk_"+a.getEntity().getDataReference()+"=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, id);
		    
		    ps.execute();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public int insertNewReference(CBusinessClass toCreateEntity,
			List<UIControlElement> inlineCreation) {
		int id=0;
		String sql = "insert into "+toCreateEntity.getDataReference()+" (protogen_user_id,";
		try{
			for(UIControlElement e : inlineCreation){
				if(!e.isReference() || e.getListReference() == null || e.getListReference().size()==0)
					continue;
				
				if(e.getControlValue().equals(""))
					e.setControlValue(e.getListReference().get(0).getKey());
			}
			
			for(UIControlElement e : inlineCreation){
				if(e.isReference() && (e.getControlValue().equals("0") || e.getControlValue().equals("")))
					continue;
				if((e.getControlValue() == null || e.getControlValue().equals("") || e.getControlValue().equals("null")))
					continue;
				sql = sql+e.getAttribute().getDataReference()+",";
			}
			ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
			CoreUser u = cache.getUser(); 
			
			if(u.getOrgInstance() == 0)
				u.setOrgInstance(u.getOriginalOrganization().getIdBean());
			
			sql = sql.substring(0,sql.length()-1)+") values ("+u.getOrgInstance()+",";
			
			for(UIControlElement e : inlineCreation){
				if(e.isReference() && (e.getControlValue().equals("0") || e.getControlValue().equals("")))
					continue;
				if((e.getControlValue() == null || e.getControlValue().equals("") || e.getControlValue().equals("null")))
					continue;
				if(e.isCtrlDate())
					sql = sql+"'"+e.getDateValue()+"',";
				else
					sql = sql+"'"+StringFormat.getInstance().format(e.getControlValue())+"',";
			}
			sql = sql.substring(0,sql.length()-1)+")";
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    System.out.println("INLINE INSERT QUERY\n\t"+sql);
		    
		    ps.execute();
		    ps.close();
		    
		    
		    sql = "select nextval('"+toCreateEntity.getDataReference()+"_seq')";
		    ps = cnx.prepareStatement(sql);
		    
		    
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	id=rs.getInt(1)-1;
		    }
		    
		    ps.close();
		}catch(Exception e){
			System.out.println("******************************************************************************");
			System.out.println("*********************** Inline insert query **********************************");
			System.out.println("******************************************************************************");
			System.out.println(sql);
			System.out.println("******************************************************************************");
			System.out.println("******************************************************************************");
			e.printStackTrace();
		}
		
		return id;
	}
	
	public int nextVal(String seq, boolean modifyFlag) {
		int v=0;
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
		    String sql = "select nextval('"+seq+"')";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    
		    ResultSet rs = ps.executeQuery();
		    if(rs.next())
		    	v = rs.getInt(1);
		    
		    rs.close();
		    ps.close();
		    
		    if(!modifyFlag){
		    	sql="select setval('"+seq+"',"+(v-1)+")";
		    	ps = cnx.prepareStatement(sql);
		    	ps.execute();
		    	ps.close();
		    	
		    }
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return v;
	}
	
	public int getIdentifierByField(String table, String field, String data) {
		int id=0;
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql="select pk_"+table+" from "+table+" where "+field+"=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setObject(1, data);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}
	
	/*
	 * 		Parameters metamodel
	 */
	public int loadParameteredFor(String mainEntity, int dbID) {

		int id=0;
		try{
			
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    String sql = "select parametered_for from "+mainEntity+ " where pk_"+mainEntity+"=?";
		    
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, dbID);
		    
		    ResultSet rs = ps.executeQuery();
		    
		    if(rs.next())
		    	id = rs.getInt(1);
		    		
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		return id;
	}
	
	public CBusinessClass getEntityById(int iden) {
		CBusinessClass entity = new CBusinessClass();
		
		try{
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
		    String sql ="select data_reference,name from c_businessclass where id=?";
		    PreparedStatement ps = cnx.prepareStatement(sql);
		    ps.setInt(1, iden);
		    
		    ResultSet rs = ps.executeQuery();
		    if(rs.next()){
		    	entity.setDataReference(rs.getString(1));
		    	entity.setName(rs.getString(2));
		    	entity.setId(iden);
		    }
			
		    rs.close();
		    ps.close();
		    
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return entity;
	}
	
	/*
	 * 	Asgard project
	 */
	
	public List<List<String>> getAsgardTree(ResultTable resultTable) {
		List<List<String>> results = new ArrayList<List<String>>(); 
		
		QueryBuilder b = new QueryBuilder();
		String sql = b.buildAsgardQuery(resultTable);
		resultTable.setQuery(sql);
		List<String> titles = new ArrayList<String>();
		for(VisitingDimension d : resultTable.getDimensions())
			titles.add(d.getAttribute().getAttribute());
			
		titles.add(resultTable.getRepresentedValue().getAttribute().getAttribute());
		results.add(titles);
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				List<String> row = new ArrayList<String>();
				int count = resultTable.getDimensions().size();
				for(int i=0;i<count;i++){
					CAttribute a = resultTable.getDimensions().get(i).getAttribute();
					if(a.getDataReference().startsWith("fk_")){
						PairKVElement e = this.getDataKeyByID(a.getDataReference().substring(3), rs.getInt(i+1));
						row.add(e.getValue());
					} else {
						String v="";
						if(a.getCAttributetype().getId() == 3){
							DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
							v=df.format(rs.getDate(i+1));
						} else
							v = rs.getObject(i+1).toString();
						
						row.add(v);
						
					}
				}
				
				row.add(""+rs.getDouble(count+1));
				results.add(row);
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return results;
	}
	
	/*
	 * 	GETTERS AND SETTERS
	 */
	public QueryBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(QueryBuilder builder) {
		this.builder = builder;
	}

	public List<Integer> getCurrentIDS() {
		return currentIDS;
	}

	public void setCurrentIDS(List<Integer> currentIDS) {
		this.currentIDS = currentIDS;
	}

	public Map<String, Integer> getAutovalues() {
		return autovalues;
	}

	public void setAutovalues(Map<String, Integer> autovalues) {
		this.autovalues = autovalues;
	}

	public double getFactures() {
		double d=0;
	
		String sql = "SELECT sum(user_facture_de_vente.total_ttc) FROM public.user_facture_de_vente WHERE user_facture_de_vente.date_de_facturation > '2013-11-30 00:00:00+00' AND user_facture_de_vente.date_de_facturation < '2014-02-01 00:00:00+00'";
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			if(rs.next()){
				d = rs.getDouble(1);
			}
			rs.close();ps.close();
		}catch(Exception e){
			e.printStackTrace();
			}
		return d;
	}

	public List<PairKVElement> getTypesEntreprises() {
		List<PairKVElement> results = new ArrayList<PairKVElement>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql="select pk_user_type_entreprise, libelle_type from user_type_entreprise order by pk_user_type_entreprise asc";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs= ps.executeQuery();
			while(rs.next()){
				PairKVElement e = new PairKVElement(rs.getInt(1)+"", rs.getString(2));
				results.add(e);
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
		e.printStackTrace();
		}
		return results;
	}

	public List<PairKVElement> getFormesEntreprises() {
		List<PairKVElement> results = new ArrayList<PairKVElement>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql="select pk_user_forme_entite, libelle from user_forme_entite";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs= ps.executeQuery();
			while(rs.next()){
				PairKVElement e = new PairKVElement(rs.getInt(1)+"", rs.getString(2));
				results.add(e);
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}

	public int getLastRowId(String mainEntity) {
		int dbid=0;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql="select nextval('"+mainEntity+"_seq')";
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				dbid = rs.getInt(1)-1;
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return dbid;
	}

	public List<List<String>> loadSimpleData(CAttribute a,
			List<String> titles, int dbID, String dataReference) {
		
		String references = "";
		for(String s : titles){
			references = references+","+s.split("::")[0];
		}
		if(references.length()>0)
			references = references.substring(1);
		
		String mtmTableReference = dataReference.split("__")[0].substring(3);
		
		String sql = "select "+references+" from "+mtmTableReference+" where "+dataReference+"="+dbID;
		List<List<String>> results = new ArrayList<List<String>>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			
			ResultSet rs = ps.executeQuery();
			int i=1;
			while(rs.next()){
				List<String> row = new ArrayList<String>();
				
				for(i=0;i<titles.size();i++){
					if(titles.get(i).startsWith("fk_")){
						int fid = rs.getInt(i+1);
						if(fid == 0){
							row.add("-");
							continue;
						}
						String tableRef = titles.get(i).split("::")[0].substring(3);
						PairKVElement e = this.getDataKeyByID(tableRef, fid);
						row.add(e.getValue());
					}else
						row.add(rs.getObject(i+1)==null?"-":rs.getObject(i+1).toString());
				}
				
				
				
				results.add(row);	
			}
			
			
			
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}

	public String getDatumByQuery(String s) {
		try{
			String v = "0";
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(s);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				v = rs.getObject(1).toString();
			rs.close();ps.close();
			
			
			return v;
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return "0";
	}
	
	public Map<String, Double> executeSelectDictionnaryQuery(String sql) {
		Map<String, Double> results = new HashMap<String, Double>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String k = (rs.getObject(1)==null)?"0":rs.getObject(1).toString();
				String v = (rs.getObject(2)==null)?"0":rs.getObject(2).toString();
				results.put(k, new Double(v));
			}
			rs.close();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}
	
	public List<Double> executeSelectQuery(String sql) {
		List<Double> results = new ArrayList<Double>();
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String v = (rs.getObject(1)==null)?"0":rs.getObject(1).toString();
				results.add(new Double(v));
			}
			rs.close();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}

	public Map<CBusinessClass, Integer> getMtms(CBusinessClass e, int id) {
		Map<CBusinessClass, Integer> res = new HashMap<CBusinessClass, Integer>();
		
		String sql = "select c_attribute.data_reference from c_attribute where data_reference like 'fk_"+e.getDataReference()+"%' and multiple='Y'";
		List<String> drs = new ArrayList<String>();
		Connection cnx = null;
		try{
			cnx = ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				drs.add(rs.getString(1));
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(String dr : drs){
			String entity = dr.split("__")[1];
			CBusinessClass cbc = getReferencedTable(entity);
			sql = "select "+dr+" from "+e.getDataReference()+" where pk_"+e.getDataReference()+"="+id;
			
			try{
				PreparedStatement ps = cnx.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				Integer i = new Integer(0);
				if(rs.next()){
					i = new Integer(rs.getInt(1));
				}
				rs.close();
				ps.close();
				res.put(cbc, i);
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
		}
		
		
		return res;
	}

	/*
	 * IDENTIFICATION ROWS
	 */
	
	public int getDefault(CIdentificationRow identificationRow, String value) {
		int id = 0;
		QueryBuilder builder = new QueryBuilder();
		
		String sql = builder.buildIdentifierInstanceQuery(identificationRow, value); 
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return id;
	}

	public void updateDefaultRow(CIdentificationRow identificationRow,
			int idsource, int dbID) {
		
		QueryBuilder builder = new QueryBuilder();
		
		String dsql = builder.buildDeleteDefaultRow(identificationRow.getId(), idsource);
		String isql = builder.buildInsertDefaultRow(identificationRow.getId(), idsource, dbID);
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(dsql);
			ps.execute();
			ps.close();
			
			ps = cnx.prepareStatement(isql);
			ps.execute();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	/**
	 * Loads the next value of a given automatic attribute
	 * @param attribute
	 * @return the formated next value
	 */
	public String loadNextValue(CAttribute attribute) {
		String seq = "autoseq_"+attribute.getEntity().getDataReference()+"_"+attribute.getDataReference();
		String sql = "select nextval('"+seq+"')";
		int nextid = 0;
		String toRem = "";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				nextid = rs.getInt(1);
			rs.close();
			ps.close();
			
			sql = "select setval('"+seq+"',"+(nextid-1)+")";
			toRem = ""+(nextid-1);			//	I have to remove the last ID from the default value in multi insert mode
			ps = cnx.prepareStatement(sql);
			ps.execute();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		String nextValue= nextid+"";
		
		if(toRem.trim().equals(attribute.getDefaultValue().trim()))		//	Purging the last id from default value
			attribute.setDefaultValue("");
		
		if(attribute.getDefaultValue()!=null && attribute.getDefaultValue().length()>0)
			nextValue = attribute.getDefaultValue().replace(toRem, "") + " " + nextValue;
		return nextValue;
	}
	
	public Map<String, String> executeInsertReference(CWindow window, MtmBlock refBlock, CoreUser u) {
		List<UIControlElement> controls = refBlock.getControls();
		Map<String, String> references = new LinkedHashMap<String, String>();
		for(UIControlElement c : controls){
			if(c.isReference() && !c.getControlValue().equals("0") && c.getAttribute().getId()!=0)
				references.put(c.getAttribute().getDataReference(), c.getControlValue());
		}
		List<String> tables = new ArrayList<String>();
		Map<String, List<String>> attributes = new LinkedHashMap<String, List<String>>();
		Map<String, String> foreignKeys = new LinkedHashMap<String, String>();
		boolean flags[];
		
//		Get tables
		for(UIControlElement element : controls){
			if(element.isReference())
				continue;
			if(element.isReadOnly())
				continue;
			String tableRef = element.getAttribute().getEntity().getDataReference();
			if(tables.contains(tableRef))
				continue;
			tables.add(tableRef);
		}
		for(String table : tables){
			List<String> tableAttributes = getTableAttributes(table);
			attributes.put(table, tableAttributes);
		}
		flags = new boolean[tables.size()];
		for(String table : tables){
			int index = tables.indexOf(table);
			if(flags[index])
				continue;
			
			boolean ready = true;
			for(String attribute : attributes.get(table)){
				if(attribute.startsWith("fk_")){
					String referenced = attribute.substring(3);
					for(String otherTable : tables){
						if(referenced.equals(window.getMainEntity()))	//	Reflexive references
							continue;
						if(otherTable.equals(referenced) && !flags[tables.indexOf(otherTable)]){
							ready = false;
							break;
						}
					}
				}
			}
			
			if(ready){
				Map<String, String> vals = new LinkedHashMap<String, String>();
				for(UIControlElement element : controls){
					if(element.getAttribute().getId()==0)
						continue;
					if(element.getAttribute().getEntity().getDataReference().equals(table)){
						if(element.isBinaryContent() || (element.isReference() && element.getControlValue().equals("0")))
							continue;
						if(element.isReadOnly())
							continue;
						if(element.getAttribute().getCAttributetype().getId()==5){
							//	Type heure
							String h = element.getControlValue().split(":")[0]+element.getControlValue().split(":")[1];
							vals.put(element.getAttribute().getDataReference(), h);
						}else {
							if(element.getAttribute().getCAttributetype().getId() == 7 ||element.getAttribute().getCAttributetype().getId() == 9)
								vals.put(element.getAttribute().getDataReference(), StringFormat.getInstance().format(element.getTrueValue()));
							else if(element.isCtrlDate()){
								if(element.getDateValue()!=null){
									Calendar c = Calendar.getInstance();
									c.setTime(element.getDateValue());
									String value = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
									vals.put(element.getAttribute().getDataReference(), value);
								} else {
									vals.put(element.getAttribute().getDataReference(), "");
								}
							}
							else {
								if((element.getControlValue()==null || element.getControlValue().length()==0) && 
											element.getAttribute().getDefaultValue()!=null && element.getAttribute().getDefaultValue().length()>0)
									element.setControlValue(SpecialValuesEngine.getInstance().parseSpecialValues(element.getAttribute().getDefaultValue()));
								vals.put(element.getAttribute().getDataReference(), StringFormat.getInstance().format(element.getControlValue()));
							}
						}
					}
				}
				
				for(String attribute : attributes.get(table)){
					if(attribute.startsWith("fk_")){
						if(!foreignKeys.containsKey(attribute))
							continue;
						String value = foreignKeys.get(attribute);
						vals.put(attribute, value);
					}
				}
				
				for(CAttribute a : window.getCAttributes()){
					if(a.getEntity().getDataReference().equals(table)){
						for(String key : references.keySet()){
							if(a.getDataReference().equals(key.replace("pk_", "fk_")))
								vals.put(key.replace("pk_", "fk_"), references.get(key));
						}
					}
				}
				
				int paramID=0;
				for(UIControlElement c : controls){
					if(c.getAttribute().getId()==0 && c.isReference())
						paramID=Integer.parseInt(c.getControlValue()); 
				}
				
				String query = builder.buildInsertQuery(table, vals,u, paramID);
				if(query=="")
					continue;
				try{
					

				    Connection cnx=ProtogenConnection.getInstance().getConnection();
				    
					Statement st = cnx.createStatement();
					
					st.execute(query);
					
					int pk = getLastInserted(table);
					foreignKeys.put("fk_"+table, ""+pk);
					st.close();
					
					return foreignKeys;
				}catch(Exception exc){
					exc.printStackTrace();
				}
				
				flags[index] = true;
			}
			
			
		}
		return foreignKeys;
	}
	
	public Map<String,String> executeUpdateReference(CWindow window2, MtmBlock block, CoreUser u) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public CSchedulableEntity loadEvents(CSchedulableEntity ce, ApplicationCache cache) {
		String sql="";
		if(ce.getToAttribute().getId() == 0)
			sql = "select pk_"+ce.getEntity().getDataReference()+","+ce.getFromAttribute().getDataReference()+
				" from "+ce.getEntity().getDataReference();
		else
			sql = "select pk_"+ce.getEntity().getDataReference()+","+ce.getFromAttribute().getDataReference()+","+ce.getToAttribute().getDataReference()+
				" from "+ce.getEntity().getDataReference();
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)-30);
		String threshold = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" 00:00:00+00";
		sql = sql+" where "+ce.getFromAttribute().getDataReference()+">='"+threshold+"' ";
				
		List<String> parameters = new ArrayList<String>();
		
		//	Constraints
		if(cache !=null) {
			List<CoreDataConstraint> constraints = cache.getConstraints();
			
			for(CoreDataConstraint cons : constraints){
				if(cons.getEntity().getId() == ce.getEntity().getId()){
					String p = "pk_"+ce.getEntity().getDataReference()+"="+cons.getBeanId();
					parameters.add(p);
				}
			}
		}
		// Get applied models
		OrganizationDAL odal = new OrganizationDAL();
		try{
			CoreRole role = cache.getUser().getCoreRole();
			List<GParametersInstance> models = new ArrayList<GParametersInstance>();
			if(cache != null)
				models = cache.getParameterPackages();
			
			for(GParametersInstance m : models){
				List<CBusinessClass> modelEntities = odal.loadModelEntities(m);
				m.getModelPackage().setImplicatedEntities(modelEntities);
				
			}
			
			
			for(GParametersInstance m : models){
				boolean foundFlag=false;
				for(CBusinessClass bc : m.getModelPackage().getImplicatedEntities()){
					if(bc.getId() == ce.getEntity().getId()){
						foundFlag = true;
						break;
					}
				}
				
				if(!foundFlag)
					break;
				
				//	Get allowed values
				List<GParameterValues> values = odal.loadAllowedValues(m, ce.getEntity());
				if(values.size()>0){
					String p = "pk_"+ce.getEntity().getDataReference()+" in (";
					for(GParameterValues pv : values){
						p = p+pv.getRowDbId()+",";
					}
					if(p.length()>1);
					p = p.substring(0,p.length()-1); //	remove the last  comma
					p = p+")";
					parameters.add(p);
				}
					
			}
			
			//	Now check for role constraints
			if(role.getConstraints() != null && role.getConstraints().size()>0){
				for(CoreDataAccessRight r : role.getConstraints()){
					if(r.getEntity().getId() == ce.getEntity().getId()){
						String p = "pk_"+ce.getEntity().getDataReference()+"="+r.getValue();
						parameters.add(p);
						break;
					}
				}
			}
		}catch(Exception exc){
			System.out.print("NO PARAMETER");
		}
		
		sql = sql+" order by "+ce.getFromAttribute().getDataReference()+" desc LIMIT 500 ";
		
		
		List<CScheduleEvent> evts = new ArrayList<CScheduleEvent>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				CScheduleEvent e = new CScheduleEvent();
				e.setDbID(rs.getInt(1));
				e.setDateEvent(rs.getDate(2));
				if(ce.getToAttribute().getId()>0)
					e.setEndEvent(rs.getDate(3));
				else
					e.setEndEvent(rs.getDate(2));
				PairKVElement key = this.getDataKeyByID(ce.getEntity().getDataReference(), e.getDbID());
				e.setLabel(key.getValue());
				e.setEntity(ce.getEntity());
				evts.add(e);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		ce.setEvents(evts);
		return ce;
	}
	
	public int loadAlphaReferenceID(String windowTable, String entity2,
			int dbID2) {
		String sql = "select fk_"+windowTable+"__"+entity2+" from "+windowTable+" where pk_"+windowTable+"="+dbID2;
		int id=0;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return id;
	}
	
	public List<Integer> loadAuthorizedAlphas(String alphaDataReference,
			ApplicationCache cache) {
		List<Integer> results = new ArrayList<Integer>();
		
		for(CoreDataAccessRight dar : cache.getUser().getCoreRole().getConstraints()){
			if(dar.getEntity().getDataReference().equals(alphaDataReference)){
				results.add(new Integer(dar.getValue()));
				return results;
			}
		}
		
		CBusinessClass cb = this.getReferencedTable(alphaDataReference);
		String constraint = "";
		for(CoreDataAccessRight dar : cache.getUser().getCoreRole().getConstraints()){
			CAttribute fkatt = null;
			boolean flag = false;
			for(CAttribute a : cb.getAttributes()){
				if(a.getDataReference().equals("fk_"+alphaDataReference)){
					flag = true;
					fkatt = a;
					break;
				}
			}
			if(!flag)
				continue;
			constraint= constraint +fkatt.getDataReference()+ "="+dar.getValue()+" and ";
		}
		if(constraint.length()>0){
			constraint = constraint.substring(0,constraint.length()-4);
			constraint = " where "+constraint;
		}
		String sql = "select pk_"+alphaDataReference+" from "+alphaDataReference;
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				results.add(new Integer(rs.getInt(1)));
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}
	
	/*
	 * MTM Lines update
	 */
	public void executeUpdate(String dataReference, int rowId,
			Map<CAttribute, String> values) {
		QueryBuilder builder = new QueryBuilder();
		String sql = builder.buildInlineUpdate(dataReference, rowId, values);
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public String getAssociatedValue(CAttribute tableAttribute,
			CAttribute targetAttribute, int id) {
		String sql = "select "+targetAttribute.getDataReference()+
				" from "+tableAttribute.getDataReference().substring(3)+
				" where pk_"+tableAttribute.getDataReference().substring(3)+"="+id;
		String val = "";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				val = rs.getObject(1).toString();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		if(val == null || val.length() == 0)
			return "";
		
		if(targetAttribute.getDataReference().startsWith("fk_")){
			PairKVElement pkv = getDataKeyByID(targetAttribute.getDataReference().substring(3), Integer.parseInt(val));
			return pkv.getValue();
		}
		
		if(targetAttribute.getCAttributetype().getId() == 3){
			String sdate = val.split(" ")[0];
			int y = Integer.parseInt(sdate.split("-")[0]);
			int m = Integer.parseInt(sdate.split("-")[1]);
			int d = Integer.parseInt(sdate.split("-")[2]);
			
			return d+"/"+m+"/"+y;
		}
		
		return val;
	}
	
	public void deleteSimple(CWindow window, int dbID) {
		String sql = "update "+window.getMainEntity()+" set dirty='Y' where pk_"+window.getMainEntity()+"="+dbID;
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.execute();
		}
		catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public List<Map<String, String>> executeSelectSynthesis(String sql, int id) {
		String query = sql.replace('?', '#');
		System.out.println("SYNTH SELECT : "+query.replaceAll("#", id+""));
		List<Map<String, String>> results = new ArrayList<Map<String,String>>();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			while(rs.next()){
				Map<String, String> row = new HashMap<String, String>();
				for(int i = 1 ; i <= rsmd.getColumnCount() ; i++){
					String v = rs.getObject(i)!=null?rs.getObject(i).toString() : "";
					row.put(rsmd.getColumnName(i), v);
				}
				results.add(row);
			}
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}

	
	/*
	 * GETTERS AND SETTERS
	 */
	public int getDbID() {
		return dbID;
	}

	public void setDbID(int dbID) {
		this.dbID = dbID;
	}

	public CBusinessClass getEntity() {
		return entity;
	}

	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}

	public DataDefinitionOperation getOperation() {
		return operation;
	}

	public void setOperation(DataDefinitionOperation operation) {
		this.operation = operation;
	}

	public Map<CBusinessClass, Integer> getFkClasses() {
		return fkClasses;
	}

	public void setFkClasses(Map<CBusinessClass, Integer> fkClasses) {
		this.fkClasses = fkClasses;
	}

	public CWindow getWindow() {
		return window;
	}

	public void setWindow(CWindow window) {
		this.window = window;
	}

	public Map<CAttribute, Object> getDataToDelete() {
		return dataToDelete;
	}

	public void setDataToDelete(Map<CAttribute, Object> dataToDelete) {
		this.dataToDelete = dataToDelete;
	}

	public List<String> getRowsStyle() {
		return rowsStyle;
	}

	public void setRowsStyle(List<String> rowsStyle) {
		this.rowsStyle = rowsStyle;
	}

}
