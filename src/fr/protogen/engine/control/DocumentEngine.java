package fr.protogen.engine.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import javax.faces.context.FacesContext;

import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CDocumentbutton;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;


public class DocumentEngine {

public String compile(String root, String report, Map<String, Object> parameters, CDocumentbutton btn){
		
		UUID unique = UUID.randomUUID();
		String filePath = root+"/tmp/"+unique.toString()+".pdf";
		
		File f = new File(root+report+".zip");
		if(!f.exists()){
			ApplicationLoader dal = new ApplicationLoader();
			dal.loadReport(btn,root+report);
		}
		
		try  {  

		      JasperDesign jasperDesign = JRXmlLoader.load(root+report+"/main.jrxml");
		      
		      JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);  

		      Connection conn =
		      ProtogenConnection.getInstance().getConnection();
		        
		      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);  

		      JasperExportManager.exportReportToPdfFile(jasperPrint,filePath);  
		    }  
		    catch (Exception e)  {  
		      System.out.println(e);  
		    }  
		
		return filePath;
	}

	public String compile(String root, String report, Map<String, Object> parameters, CDocumentbutton btn, OutputStream out){
		
		UUID unique = UUID.randomUUID();
		String filePath = root+"/tmp/"+unique.toString()+".pdf";
		
		String subreportdir = root+report+"/";
		parameters.put("SUBREPORT_DIR", subreportdir);
		
		
		File f = new File(root+report+".zip");
		if(!f.exists()){
			ApplicationLoader dal = new ApplicationLoader();
			dal.loadReport(btn,root+report);
		}
		
		try  {  
				
		      JasperDesign jasperDesign = JRXmlLoader.load(root+report+"/main.jrxml");
		      
		      JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);  
	
		      Connection conn =
		      ProtogenConnection.getInstance().getConnection();
		        
		      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);  
		      
		      JasperExportManager.exportReportToPdfStream(jasperPrint, out);
		      
		    }  
		    catch (Exception e)  {  
		      e.printStackTrace();
		    }  
		
		return filePath;
	}
}
