package fr.protogen.engine.gexpression;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CLVSField;
import fr.protogen.masterdata.model.CLVSTable;
import fr.protogen.masterdata.model.CListViewSynthesis;
import fr.protogen.masterdata.model.CWindow;

public class ExpressionParserEngine {
	
	private ApplicationLoader dal = null;
	
	private static ExpressionParserEngine instance = null;
	public static synchronized ExpressionParserEngine getInstance(){
		if(instance == null)
			instance = new ExpressionParserEngine();
		return instance;
	}
	private ExpressionParserEngine(){
		dal = new ApplicationLoader();
	}
	
	public void buildSynthesisWindow(CListViewSynthesis subject, CWindow w){
		CBusinessClass rootTable = dal.getEntity(w.getMainEntity());
		
		//	Seperate lines
		String[] expLines = subject.getExpression().replaceAll("\n", "").replaceAll("\r", "").split(";");
		
		//	Load tables
		List<CLVSTable> allTables = new ArrayList<CLVSTable>();
		for(String l : expLines){
			if(l.indexOf("#TABLES#")<0)
				continue;
			l = l.replaceAll("#TABLES#", "");
			String[] tables = l.split(",");
			for(String t : tables){
				String[] tcols = t.split(":");
				String dref = tcols[0];
				String lib = tcols[1];
				CBusinessClass entity = dal.getEntity(dref);
				CLVSTable clvst = new CLVSTable();
				clvst.setFields(new ArrayList<CLVSField>());
				clvst.setLabel(lib);
				clvst.setTable(entity);
				clvst.setTables(new ArrayList<CLVSTable>());
				allTables.add(clvst);
			}
			break;
		}
		
		// 	Load fields
		for(String l : expLines){
			if(l.indexOf("#CHAMPS#")<0)
				continue;
			 l = l.replaceAll("#CHAMPS#", "");
			 String[] champs = l.split(",");
			 for(String sc : champs){
				 String cdataref = sc.split(":")[0];
				 String clibelle = sc.split(":")[1];
				 String tableRef = cdataref.split("\\.")[0].trim();
				 String attributeRef = cdataref.split("\\.")[1].trim();
				 for(CLVSTable t : allTables){
					 if(!t.getTable().getDataReference().equals(tableRef))
						 continue;
					 CLVSField f = new CLVSField();
					 for(CAttribute a : t.getTable().getAttributes()){
						 if(!a.getDataReference().equals(attributeRef))
							 continue;
						 f.setAttribute(a);
						 f.setLibelle(clibelle);
						 t.getFields().add(f);
						 break;
					 }
					 break;
				 }
			 }
			 
		}
		
		
		//	Set subtables
		for(String l : expLines){
			int index = l.indexOf("#JOINTURES#"); 
			if(index<0)
				continue;
			l = l.replaceAll("#JOINTURES#", "");
			String[] sjs = l.split(",");
			for(String sj : sjs){
				if(sj.split("->").length<=1)
					continue;
				String src = sj.split("->")[0];
				String dst = sj.split("->")[1];
				CLVSTable ts = null;
				for(CLVSTable t : allTables){
					if(!t.getTable().getDataReference().equals(src))
						continue;
					ts = t;
					break;
				}
				
				if(ts == null)
					continue;
				
				CLVSTable td = null;
				for(CLVSTable t : allTables){
					if(!t.getTable().getDataReference().equals(dst))
						continue;
					td = t;
					break;
				}
				
				if(td == null)
					continue;
				
				ts.getTables().add(td);
			}
		}
		
		// 	Generate SQL
		String sql = "";
		String select = "SELECT "+w.getMainEntity()+".pk_"+w.getMainEntity()+" as "+w.getMainEntity()+"_pk_"+w.getMainEntity()+", ";
		String from = " FROM "+w.getMainEntity()+" ";
		String where = " WHERE ";
			//	Let us start by adding primary keys
		for(CLVSTable t : allTables){
			select=select+t.getTable().getDataReference()+".pk_"+t.getTable().getDataReference()+" as "+t.getTable().getDataReference()+"_pk_"+t.getTable().getDataReference()+", ";
		}
			//	Now we add  other fields
		for(CLVSTable t : allTables){
			for(CLVSField f : t.getFields()){
				select=select+t.getTable().getDataReference()+"."+f.getAttribute().getDataReference()+" as "+t.getTable().getDataReference()+"_"+f.getAttribute().getDataReference()+", ";
			}
		}
		select = select.substring(0, select.length()-2);
		List<CLVSTable> rootTables = new ArrayList<CLVSTable>();
		for(CLVSTable t : allTables){
			boolean found = false;
			for(CLVSTable ot : allTables){
				for(CLVSTable subt : ot.getTables()){
					if(subt == t){
						found = true;
						break;
					}
				}
				if(found)
					break;
			}
			if(!found)
				rootTables.add(t);
		}
		
		subject.setTables(rootTables);
		
		for(CLVSTable t : rootTables){
			String f = constructFrom(t, false);
			String dref = "";
			for(CAttribute a : t.getTable().getAttributes())
				if(a.getDataReference().indexOf(w.getMainEntity())>=0){
					dref = a.getDataReference();
					break;
				}
			from=from+" left join "+t.getTable().getDataReference()+" on "+t.getTable().getDataReference()+"."+dref+"="+w.getMainEntity()+".pk_"+w.getMainEntity()+" "+f;
		}
		//from = from.substring(0, from.length()-2);
		
		/*for(CLVSTable t : rootTables){
			for(CAttribute a : t.getTable().getAttributes()){
				if(a.getDataReference().contains(rootTable.getDataReference())){
					where = where + t.getTable().getDataReference()+"."+a.getDataReference()+"="+rootTable.getDataReference()+".pk_"+rootTable.getDataReference()+" AND ";
				}
			}
			
			for(CAttribute a : rootTable.getAttributes()){
				if(a.getDataReference().contains(t.getTable().getDataReference())){
					where = where +  rootTable.getDataReference()+"."+a.getDataReference()+"="+t.getTable().getDataReference()+".pk_"+t.getTable().getDataReference()+" AND ";
				}
			}
		}*/
		where = where+rootTable.getDataReference()+".pk_"+rootTable.getDataReference()+"=?";
		
		sql = select+from+where;
		subject.setExpression(sql);
	}
	
	private String constructFrom(CLVSTable t, boolean sub) {
		String from = "";
		/*if(!sub)
			from = t.getTable().getDataReference();*/
		if(t.getTables().isEmpty())
			return from;
		for(CLVSTable st  : t.getTables()){
			boolean isRight = checkRight(t.getTable(), st.getTable());
			if(isRight)
				from = from + " LEFT JOIN " + st.getTable().getDataReference() + " ON ";
			else
				from = from + " JOIN " + st.getTable().getDataReference() + " ON ";
			
			for(CAttribute a : st.getTable().getAttributes())
				if(a.getDataReference().contains(t.getTable().getDataReference())){
					from = from + st.getTable().getDataReference()+"."+a.getDataReference()+"="+t.getTable().getDataReference()+".pk_"+t.getTable().getDataReference(); 
				}
			for(CAttribute a : t.getTable().getAttributes())
				if(a.getDataReference().contains(st.getTable().getDataReference())){
					from = from + t.getTable().getDataReference()+"."+a.getDataReference()+"="+st.getTable().getDataReference()+".pk_"+st.getTable().getDataReference(); 
				}
			from = from+" "+constructFrom(st, true);
		}
		return from;
	}
	
	private boolean checkRight(CBusinessClass source, CBusinessClass destination) {
		for(CAttribute a : source.getAttributes()){
			if(a.getDataReference().contains(destination.getDataReference()))
				if(a.isReference())
					return false;
				else
					return true;
		}
		for(CAttribute a : destination.getAttributes()){
			if(a.getDataReference().contains(source.getDataReference()))
				if(a.isReference())
					return true;
				else
					return false;
		}
		return false;
	}
	public String formatValue(CLVSField f, String sval) {
		
		if(f.getAttribute().getDataReference().startsWith("fk_") && f.getAttribute().isReference()){
			ProtogenDataEngine pde = new ProtogenDataEngine();
			String table = f.getAttribute().getDataReference().substring(3); 
			PairKVElement pkv = pde.getDataKeyByID(table, tryParseInt(sval));
			return pkv.getValue();
		}
		
		int idType = f.getAttribute().getCAttributetype().getId();
		switch(idType){
			case 1 : return sval;
			case 2 : return sval;
			case 3 : return formatDateFromSQL(sval);
			case 4 : return formatNumber(sval);
			case 5 : return formatHeure(sval);
			case 8 : return formatNumber(sval);
		}
		if(sval == null)
			return "";
		return sval;
	}
	private String formatHeure(String sval) {
		int minutes = tryParseInt(sval);
		int h = minutes/60;
		int m = minutes - h*60;
		String heure = h<10?"0"+h:""+h;
		heure = heure+":"+(m<10?"0"+m:""+m);
		return heure;
	}
	private String formatNumber(String sval) {
		double v = tryParseDouble(sval);
		DecimalFormat df = new DecimalFormat("####0.00");
		return df.format(v);
	}
	private String formatDateFromSQL(String sval) {
		if(sval == null || sval.length() == 0 || sval.split("-").length<2)
			return "";
		String day = sval.split(" ")[0];
		return day.split("-")[2]+"/"+day.split("-")[1]+"/"+day.split("-")[0];
	}
	
	private int tryParseInt(String s){
		try{
			return Integer.parseInt(s);
		}catch(Exception e){
			
		}
		return 0;
	}
	private double tryParseDouble(String s){
		try{
			return Double.parseDouble(s);
		}catch(Exception e){
			
		}
		return 0.0;
	}
}
