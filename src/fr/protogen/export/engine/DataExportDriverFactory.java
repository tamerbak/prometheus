package fr.protogen.export.engine;

import fr.protogen.export.engine.excel.DataExportExcel;
import fr.protogen.export.engine.xml.DataExportXML;

public class DataExportDriverFactory {
	private static DataExportDriverFactory instance=null;
	public  synchronized static DataExportDriverFactory getInstance(){
		if(instance == null)
			instance = new DataExportDriverFactory();
		
		return instance;
	}
	
	public DataExportEngine create(ExportDriver driver){
		if(driver == ExportDriver.XML)
			return new DataExportXML();
		else
			return new DataExportExcel();
	}
}
