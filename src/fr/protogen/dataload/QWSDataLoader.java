package fr.protogen.dataload;

import java.util.HashMap;
import java.util.Map;

import fr.protogen.connector.model.QWSQuery;

public class QWSDataLoader {
	private static QWSDataLoader instance = null;
	public static synchronized QWSDataLoader getInstance(){
		if(instance == null){
			instance = new QWSDataLoader();
		}
		return instance;
	}
	private QWSDataLoader(){
		
	}
	
	public String execute(QWSQuery query){
		
		return "";
	}
	
	private String translateToSQL(QWSQuery query){
		String sql = "";
		if (query.getType().equals("READ")){
			return sqlSelect(query);
		} else if (query.getType().equals("CREATE")){
			return sqlInsert(query);
		} else if (query.getType().equals("UPDATE")){
			return sqlUpdate(query);
		} else if (query.getType().equals("DELETE")){
			return sqlDelete(query);
		}
		return sql;
	}
	
	private String sqlDelete(QWSQuery query) {
		
		return null;
	}
	private String sqlUpdate(QWSQuery query) {
		// TODO Auto-generated method stub
		return null;
	}
	private String sqlInsert(QWSQuery query) {
		// TODO Auto-generated method stub
		return null;
	}
	private String sqlSelect(QWSQuery query) {
		// TODO Auto-generated method stub
		return null;
	}
	private Map<String, String> executeSql(String sql, QWSQuery query){
		return new HashMap<String, String>();
	}
	
	private String parseJSON(Map<String, String> data){
		return "";
	}
}
