package fr.protogen.dataload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;


public class SearchEngine {

	private static final String[] todel = {"le","la","l'","les","de","du","des","d'",
			"pour","par","dans","je","tu","il","elle","nous","vous","ils","elles", "besoin"};
	
	public List<Integer> search(CBusinessClass targetTable, String query){
		List<Integer> ids = new ArrayList<Integer>();
		
		//	retrieve key words
		List<String> kw = sliceAndPurge(query);		
		
		//	retrieve all tables with instances of each key word
		ApplicationLoader dal = new ApplicationLoader();
		//ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		//List<CBusinessClass> allEntities = dal.loadAllEntities("c547cae1-7ced-4add-baf0-3c0537e029a8");//(cache.getAppKey());
		List<CBusinessClass> allEntities = chargerTablesIndexes(dal);
		
		Map<CBusinessClass, List<Integer>> mappedIds = new HashMap<CBusinessClass, List<Integer>>();
		for(CBusinessClass e : allEntities)
			mappedIds.put(e, new ArrayList<Integer>());
		
		for(String k : kw){
			for(CBusinessClass e : allEntities){
				String sql = constructQuery(k,e);
				if(sql == null)
					continue;
				
				try{
					Class.forName("org.postgresql.Driver");
				    Connection cnx = ProtogenConnection.getInstance().getConnection();
				    PreparedStatement ps = cnx.prepareStatement(sql);
				    ResultSet rs = ps.executeQuery();
				    while(rs.next()){
				    	mappedIds.get(e).add(new Integer(rs.getInt(1)));
				    }
				    rs.close();
				    ps.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
			}
		}
		
		//	for each table I need to establish the relationship with the target and get the target ID
		Map<CBusinessClass, List<Integer>> targetIDS = new HashMap<CBusinessClass, List<Integer>>();
		for(CBusinessClass e : allEntities)
			targetIDS.put(e, new ArrayList<Integer>());
		
		for(CBusinessClass e : allEntities){
			String sql = constructTargetQuery(targetTable,e,mappedIds.get(e), allEntities);
			if(sql.length() == 0 || mappedIds.get(e).size()==0)
				continue;
			try{
				Class.forName("org.postgresql.Driver");
			    Connection cnx = ProtogenConnection.getInstance().getConnection();
			    PreparedStatement ps = cnx.prepareStatement(sql);
			    System.out.println("RECHERCHE :\t"+sql);
			    ResultSet rs = ps.executeQuery();
			    while(rs.next()){
			    	targetIDS.get(e).add(new Integer(rs.getInt(1)));
			    }
			    rs.close();
			    ps.close();
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		
		List<Integer> distinct = new ArrayList<Integer>();
		for(CBusinessClass e : allEntities){
			for(Integer i : targetIDS.get(e)){
				boolean ifound = false;
				for(Integer j : distinct){
					if(i.intValue() == j.intValue()){
						ifound = true;
						break;
					}
				}
				if(ifound)
					continue;
				distinct.add(i);
			}
		}
		
		for(Integer i : distinct){
			boolean notFound = false;
			for(CBusinessClass e : allEntities){
				if(targetIDS.get(e).size() == 0)
					continue;
				boolean found = false;
				for(Integer j : targetIDS.get(e)){
					if(i.intValue() == j.intValue()){
						found = true;
						break;
					}
				}
				if(!found){
					notFound = true;
					break;
				}
					
			}
			if(notFound)
				continue;
			
			ids.add(i);
		}
		
		return ids;
	}

	private List<CBusinessClass> chargerTablesIndexes(ApplicationLoader dal) {
		List<CBusinessClass> resultats = new ArrayList<CBusinessClass>();
		resultats.add(dal.getEntity("user_pays"));
		resultats.add(dal.getEntity("user_ville"));
		resultats.add(dal.getEntity("user_competence"));
		resultats.add(dal.getEntity("user_langue"));
		return resultats;
	}

	private String constructTargetQuery(CBusinessClass targetTable, CBusinessClass e, List<Integer> list, List<CBusinessClass> allEntities) {
		String query = "SELECT pk_"+targetTable.getDataReference()+" FROM index_"+targetTable.getDataReference()+" "
				+ " WHERE index_table='"+e.getDataReference()+"' and id_index in (0,";
		for(Integer i : list){
			query = query+i+",";
		}
		
		query = query.substring(0,query.length()-1)+")";
		
		return query;
		
		/*String query = "";
		
		//	From target
		for(CAttribute a : targetTable.getAttributes()){
			if(a.getDataReference().contains(e.getDataReference()) && a.isReference()){
				query = referenceFromTarget(targetTable, list, a, e);
				return query;
			}
			if(a.getDataReference().contains(e.getDataReference()) && a.isMultiple()){
				query = multipleFromTarget(targetTable, list, a, e);
				return query;
			}
		}
		
		//	From entity
		for(CAttribute a : e.getAttributes()){
			if(a.getDataReference().contains(targetTable.getDataReference())&& a.isReference()){
				query = referenceFromEntity(targetTable, list, a);
				return query;
			}
		}
		
		for(CAttribute a : e.getAttributes()){
			if(a.getDataReference().contains(targetTable.getDataReference())&& a.isMultiple()){
				query = multipleFromEntity(targetTable, list, a);
				return query;
			}
		}
		
		// cross reference
		for(CBusinessClass c : allEntities){
			boolean tfound = false, efound = false;
			for(CAttribute a : c.getAttributes()){
				if(a.getDataReference().contains(targetTable.getDataReference()))
					tfound = true;
				if(a.getDataReference().contains(e.getDataReference()))
					efound = true;
			}
			
			if(!tfound || !efound)
				continue;
			
			query = crossReference(targetTable, list, c, e); 
			return query;
		}
		
		return query;*/
	}

	private String crossReference(CBusinessClass targetTable, List<Integer> list, CBusinessClass c, CBusinessClass e) {
		
		String query = "select fk_"+targetTable.getDataReference()+" from "+c.getDataReference()+" where fk_"+e.getDataReference()+" in (";
		for(Integer i : list)
			query = query+" "+i.intValue()+",";
		query = query.substring(0, query.length()-1)+")";
		return query;
	}

	private String multipleFromEntity(CBusinessClass targetTable, List<Integer> list, CAttribute a) {
		String query = "select pk_"+targetTable.getDataReference()+" from "+targetTable.getDataReference()+" where "+a.getDataReference()+" in (";
		for(Integer i : list)
			query = query+" "+i.intValue()+",";
		query = query.substring(0, query.length()-1)+")";
		return query;
	}

	private String referenceFromEntity(CBusinessClass targetTable, List<Integer> list, CAttribute a) {
		String query = "select fk_"+targetTable.getDataReference()+" from "+a.getEntity().getDataReference()+" where pk_"+a.getEntity().getDataReference()+" in (";
		for(Integer i : list)
			query = query+" "+i.intValue()+",";
		query = query.substring(0, query.length()-1)+")";
		return query;
	}

	private String multipleFromTarget(CBusinessClass targetTable, List<Integer> list, CAttribute a, CBusinessClass e) {

		String query = "select "+a.getDataReference()+" from "+e.getDataReference()+" where pk_"+e.getDataReference()+" in (";
		for(Integer i : list)
			query = query+" "+i.intValue()+",";
		query = query.substring(0, query.length()-1)+")";
		return query;
	}

	private String referenceFromTarget(CBusinessClass targetTable, List<Integer> list, CAttribute a, CBusinessClass e) {
		String query = "select pk_"+targetTable.getDataReference()+" from "+targetTable.getDataReference()+" where "+a.getDataReference()+" in (";
		for(Integer i : list)
			query = query+" "+i.intValue()+",";
		query = query.substring(0, query.length()-1)+")";
		return query;
	}

	private String constructQuery(String k, CBusinessClass e) {
		String query = "select pk_"+e.getDataReference()+" from "+e.getDataReference()+" where ";
		boolean flag = false;
		
		for(CAttribute a : e.getAttributes()){
			if(a.getCAttributetype().getId() !=2)
				continue;
			flag = true;
			query = query + " LOWER("+a.getDataReference()+") like LOWER('%"+k+"%') OR ";
		}
		
		if(!flag)
			return null;
		
		query = query.substring(0, query.length()-4);
		
		return query;
	}

	private List<String> sliceAndPurge(String query) {
		String[] t = query.split(" ");
		List<String> kw = new ArrayList<String>();
		
		for(String w : t){
			String[] it = w.split("\t");
			for(String iw : it){
				boolean found = false;
				for(String k : kw)
					if(k.equals(iw)){
						found = true;
						break;
					}
				if(!found)
					kw.add(iw);
			}
		}
		
		List<String> purge = new ArrayList<String>();
		for(String k : kw){
			for(String d : todel){
				if(k.toLowerCase().equals(d)){
					purge.add(k);
					break;
				}
			}
		}
		
		kw.removeAll(purge);
		
		return kw;
	}
}
