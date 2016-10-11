package fr.protogen.dataload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import fr.protogen.engine.utils.GQLDataResult;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

/**
 * Moteur de transformation GQL SQL
 * @author Jakjoud ABdeslm
 * @version 1.0.0
 */
public class GqlEngine {
	
	/*
	 * Pattern
	  	#!GQL
		OPERATION	:	COMPTER
		VARIABLE	:	NOM
		VALEUR		:	Table.variable
		TABLES		:	T1;T2;T3
		CONTRAINTES	:	T1.champs:operateur:valeur;T2.champs:operateur:T1.champs	
		#!FIN
	 */
	
	public GQLDataResult generate(String gql){
		
		GQLDataResult returnResult = new GQLDataResult();
		ProtogenDataEngine pde = new ProtogenDataEngine();
		
		List<String> lignes = new ArrayList<String>();
		
		try {
			lignes = IOUtils.readLines(new ByteArrayInputStream(gql.getBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(lignes.size()!=6){
			System.out.println("GQL Compiler : Wrong data size");
			return returnResult;
		}
		
		String operation = "";
		for(String l : lignes){
			if(l.split(":")[0].trim().replaceAll("\t", "").equals("OPERATION")){
				operation = l.split(":")[1].trim().replaceAll("\t", "");
			}
		}
		
		double result=0;
		String sql = transform(gql);
		
		if(operation.equals("COMPTER")){
			List<Double> rs = pde.executeSelectQuery(sql);
			result = rs.size();
		}
			
		else if(operation.equals("SOMME")){
			List<Double> rs = pde.executeSelectQuery(sql);
			for(Double d : rs)
				result = result+d;
		} else if(operation.startsWith("VALEUR")){
			List<Double> rs = pde.executeSelectQuery(sql);
			String arg = operation.substring(operation.indexOf('('),operation.indexOf(')'));
			arg = arg.replaceAll("\\(", "");
			arg = arg.replaceAll("\\)", "");
			if(rs.size() == 0)
				return returnResult;
			int iIndex = Integer.parseInt(arg.substring(2));
			int i = rs.size()-iIndex-1;
			if(i<0)
				result = 0;
			else
				result = rs.get(i);
		} else if(operation.startsWith("DICTIONNAIRE")){
			Map<String, Double> valuesDico = pde.executeSelectDictionnaryQuery(sql);
			returnResult.setValues(valuesDico);
			returnResult.setDictionnaryMode(true);
		}
		
		
		
		return returnResult;
	}
	public String transform(String gql){
		String sql="";
		
		sql = "select ";
		
		sql = sql+getValue(gql);
		
		sql = sql+" FROM "+getTables(gql);
		
		sql = sql + " WHERE "+getConstraints(gql);
		
		return sql;
	}

	private String getConstraints(String gql) {
		String results="";
		List<String> lignes = new ArrayList<String>();
		
		try {
			lignes = IOUtils.readLines(new ByteArrayInputStream(gql.getBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(lignes.size()!=6){
			System.out.println("GQL Compiler : Wrong data size");
			return "";
		}
		
		String contraintes = "";
		for(String l : lignes){
			if(l.split(":")[0].trim().replaceAll("\t", "").equals("CONTRAINTES")){
				contraintes = l.split(":")[1].trim().replaceAll("\t", "");
			}
		}
		
		ApplicationLoader dal = new ApplicationLoader();
		
		for(String c : contraintes.split(";")){
			String[] lc = c.split("=");
			if(lc[0].contains("<") && !lc[0].contains("<<")){
				lc[0] = lc[0].replaceAll("<", "").replaceAll(">","").trim(); 
				CBusinessClass e = dal.getClassByName(lc[0].split("\\.")[0].trim());
				CAttribute a = dal.getAttributeByName(lc[0].split("\\.")[1].trim(), e);
				results = results+" and "+a.getDataReference();
			}else{
				results = results+" and "+lc[0];
			}
			
			results = results+" = ";
			
			if(lc[1].contains("<") && !lc[1].contains("<<")){
				lc[1] = lc[1].replaceAll("<", "").replaceAll(">","").trim(); 
				CBusinessClass e = dal.getClassByName(lc[1].split("\\.")[0].trim());
				CAttribute a = dal.getAttributeByName(lc[1].split("\\.")[1].trim(), e);
				results = results+" "+a.getDataReference();
			}else{
				results = results+" "+lc[1];
			}
		}
		
		
		if (results.length()>5)
			results = results.substring(5);
		return results;
	}

	private String getTables(String gql) {
		
		List<String> lignes = new ArrayList<String>();
		
		try {
			lignes = IOUtils.readLines(new ByteArrayInputStream(gql.getBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(lignes.size()!=6){
			System.out.println("GQL Compiler : Wrong data size");
			return "";
		}
		
		String tables = "";
		for(String l : lignes){
			if(l.split(":")[0].trim().replaceAll("\t", "").equals("TABLES")){
				tables = l.split(":")[1].trim().replaceAll("\t", "");
			}
		}
		
		String[] ts = tables.split(";");
		
		String results = "";
		
		ApplicationLoader dal = new ApplicationLoader();
		
		for(String t : ts){
			 t = t.trim();
			 results = results+","+(dal.getClassByName(t)).getDataReference();
		}
		
		return results.substring(1);
	}

	private String getValue(String gql) {
		
		List<String> lignes = new ArrayList<String>();
		
		try {
			lignes = IOUtils.readLines(new ByteArrayInputStream(gql.getBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(lignes.size()!=6){
			System.out.println("GQL Compiler : Wrong data size");
			return "";
		}
		
		String col = "";
		for(String l : lignes){
			if(l.split(":")[0].trim().replaceAll("\t", "").equals("VALEUR")){
				col = l.split(":")[1].trim().replaceAll("\t", "");
			}
		}
		String value = "";
		for(String cls : col.split("\\,")){
			cls = cls.trim();
			String table = cls.split("\\.")[0];
			cls = cls.split("\\.")[1];
			
			ApplicationLoader dal = new ApplicationLoader(); 
			
			CBusinessClass entity = dal.getClassByName(table);
			CAttribute a = dal.getAttributeByName(cls, entity);
			value = value+","+entity.getDataReference()+"."+a.getDataReference();
		}
		
		if(value.length()>0)
			value = value.substring(1);
		
		return value;
	}
}
