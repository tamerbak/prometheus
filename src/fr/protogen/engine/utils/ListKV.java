package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.model.CoreUser;

@SuppressWarnings("serial")
public class ListKV implements Serializable {
	private String key;
	private List<String> value;
	private List<String> roundValue;
	private boolean selected;
	private Map<Integer, Boolean> validatedMap = new HashMap<Integer, Boolean>();
	private int dbID;
	private boolean locked;
	private List<SimpleDataTable> innerData = new ArrayList<SimpleDataTable>();
	private String table="";
	private String theme = "whiteline";
	private boolean alphaMode=false;
	private String alphaReference="";
	private int alphaId=0;
	
	public void table(){
		
		table = "";
		for(SimpleDataTable d : innerData){
			
			table = table+"<label style=\"font-weight:bolder;padding:10px;margin:10px\" >"+d.getHeader()+"</label><br/>"+d.format()+"<br/><br/>";
		}
		
	}
	
	public static boolean isNumeric(String str)
	{
		if(str == null)
			return false;
	  return str.matches("-?\\d+\\.(\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	public ListKV(String key, List<String> value, int dbID, Map<String, Integer> referencesIndex, 
			Map<String, Integer> hourIndex, int userIndex, Map<String, Integer> autoIndex, 
			List<Integer> validationIndexes, List<PairKVElement> titles, boolean alphaMode, String entity, String windowTable){
		
		if(alphaMode){
			this.alphaMode = alphaMode;
			ProtogenDataEngine pde = new ProtogenDataEngine();
			alphaId = pde.loadAlphaReferenceID(windowTable, entity, dbID);
			PairKVElement pair = pde.getDataKeyByID(entity, alphaId);
			alphaReference = pair.getValue();
		}
		
		this.key= key;
		List<String> roundValue = new ArrayList<String>();
		
		
		for(String tableReference : referencesIndex.keySet()){
			int index = referencesIndex.get(tableReference).intValue();
			String v = value.get(index);
			int idRef = Integer.parseInt(v);
			
			ProtogenDataEngine engine = new ProtogenDataEngine();
			PairKVElement element = engine.getDataKeyByID(tableReference, idRef);
			List<String> newVals = new ArrayList<String>();
			
			for(int i = 0; i < value.size() ; i++){
				if(i==index){
					newVals.add((element!=null)?element.getValue().trim():"");
				}
				else {
					
					newVals.add((value.get(i)==null)?"":value.get(i).trim());
				}
			}
			value = newVals;
			
		}
		
		for(String av : autoIndex.keySet()){
			int index = autoIndex.get(av).intValue();
			
			String val = value.get(index);
			String suffix = titles.get(index).getSuffix();
			value.set(index, suffix+val.replaceAll(".0", ""));
		}
		
		for(String hr : hourIndex.keySet()){
			int index = hourIndex.get(hr).intValue();
			String v = value.get(index);
			String svalue;
			int ival = Integer.parseInt(v);
			if(ival>=1000)
				svalue=v.charAt(0)+""+v.charAt(1)+":"+v.charAt(2)+""+v.charAt(3);
			else if(ival>=100)
				svalue="0"+v.charAt(0)+":"+v.charAt(1)+""+v.charAt(2);
			else if(ival>=10)
				svalue="00:"+v.charAt(0)+""+v.charAt(1);
			else
				svalue="00:0"+v.charAt(0);
			
			List<String> newVals = new ArrayList<String>();
			for(int i = 0; i < value.size() ; i++){
				if(i==index){
					newVals.add(svalue);
				}
				else{
					newVals.add(value.get(i));
				}
			}
			value = newVals;
			
		}
		
		if(userIndex>0){
			int userID = Integer.parseInt(value.get(userIndex));
			UserDAOImpl dao = new UserDAOImpl();
			CoreUser u = dao.getUserByID(userID);
			value.set(userIndex, u.getFirstName()+" - "+u.getLastName());
		}
		
		for(String v : value){
			if(v==null || v.equals("null"))
				value.set(value.indexOf(v), "");
		}
		for(int i = 0 ; i < value.size() ; i++){
			roundValue.add(isNumeric(value.get(i))?StringFormat.round(Double.parseDouble(value.get(i)), 2)+"":value.get(i));
		}
		
		
		this.value = value;
		this.roundValue = roundValue;
		selected = false;
		this.dbID = dbID;
	}
	
	public static String formatAlpha(String alphaEntity, List<ListKV> data){
		int cols = formatSize(alphaEntity);
		List<String> alphaReferences = new ArrayList<String>();
		for(ListKV l : data)
			alphaReferences.add(l.getAlphaReference());
		for(String ar : alphaReferences){
			if(cols < formatSize(ar))
				cols = formatSize(ar);
			
		}
		
		return ""+cols;
	}
	
	public static List<String> formatStyles(List<ListKV> data, List<PairKVElement> titles, boolean alpha){
		List<String> styles = new ArrayList<String>();
		int[] colsWidth = new int[titles.size()];
		
		for(int i = 0 ; i < colsWidth.length ; i++){
			String t = titles.get(i).getValue();
			int s = formatSize(t);
			colsWidth[i] = s;
		}
		
		for(int i = 0 ; i < colsWidth.length ; i++){
			for(ListKV l : data){
				String v = l.getRoundValue().get(i);
				if(colsWidth[i] < formatSize(v))
					colsWidth[i] = formatSize(v);
			}
		}
		
		if(colsWidth.length>2){
			if(alpha){
				int w = colsWidth[0] + colsWidth[1] + colsWidth[2];
				int i = 0;
				while(w>400){
					if(colsWidth[i]>0)
						colsWidth[i]--;
					w = colsWidth[0] + colsWidth[1] + colsWidth[2];
					i++;
					if(i==3)
						i=0;
				}
			} else {
				int w = colsWidth[0] + colsWidth[1];
				int i = 0;
				while(w>400){
					if(colsWidth[i]>0)
						colsWidth[i]--;
					w = colsWidth[0] + colsWidth[1];
					i++;
					if(i==2)
						i=0;
				}
			}
		}
		
		for(int i = 0 ; i < colsWidth.length ; i++){
			styles.add(""+colsWidth[i]+"");
		}
		
		return styles;
	}
	
	private static int formatSize(String v){
		int s = 0;
		
		s = v.length()*10;
		if(s > 250)
			s=250;
		if(s < 60)
			s=60;
		return s;
		
	}
	
	public Map<Integer, Boolean> getValidatedMap() {
		return validatedMap;
	}

	public void setValidatedMap(Map<Integer, Boolean> validatedMap) {
		this.validatedMap = validatedMap;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public List<String> getValue() {
		return value;
	}
	public void setValue(List<String> value) {
		this.value = value;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public int getDbID() {
		return dbID;
	}

	public void setDbID(int dbID) {
		this.dbID = dbID;
	}

	public List<String> getRoundValue() {
		return roundValue;
	}

	public void setRoundValue(List<String> roundValue) {
		this.roundValue = roundValue;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public List<SimpleDataTable> getInnerData() {
		return innerData;
	}

	public void setInnerData(List<SimpleDataTable> innerData) {
		this.innerData = innerData;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public boolean isAlphaMode() {
		return alphaMode;
	}

	public void setAlphaMode(boolean alphaMode) {
		this.alphaMode = alphaMode;
	}

	public String getAlphaReference() {
		return alphaReference;
	}

	public void setAlphaReference(String alphaReference) {
		this.alphaReference = alphaReference;
	}

	public int getAlphaId() {
		return alphaId;
	}

	public void setAlphaId(int alphaId) {
		this.alphaId = alphaId;
	}
	
	
}
