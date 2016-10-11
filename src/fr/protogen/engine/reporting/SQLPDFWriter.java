package fr.protogen.engine.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import com.sun.jersey.core.util.Base64;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class SQLPDFWriter implements PDFWriter {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String doPrint(List<String> filePath, String dataSource, boolean encodedIn, boolean encodedOut) {
		String in = "";
		if(encodedIn)
			in=Base64.base64Decode(dataSource);
		else
			in = dataSource;
		
		String tempPDF=UUID.randomUUID().toString()+".pdf";
		List<String> pdfs = new ArrayList<String>();
		String out = "";
		for(String f : filePath){
			try{
				String pdf = UUID.randomUUID().toString()+".pdf";
				JasperDesign jasperDesign = JRXmlLoader.load(f);
			    JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);  
			    Map params = prepareDataSource(in);
			    Connection conn = ProtogenConnection.getInstance().getConnection();
				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);  
			    JasperExportManager.exportReportToPdfFile(jasperPrint,pdf);  
			    pdfs.add(pdf);
				
			}catch(Exception exc){
				exc.printStackTrace();
			}
		}
		try{
			PDFMergerUtility pdfMU = new PDFMergerUtility();
			for(String pdf : pdfs){
				pdfMU.addSource(new File(pdf));
			}
			pdfMU.setDestinationFileName(tempPDF);
			pdfMU.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
			if(encodedOut){
				FileInputStream fis = new FileInputStream(tempPDF);
				byte[] pdfBytes = IOUtils.toByteArray(fis);
				fis.close();
				out = new String (Base64.encode(pdfBytes));
				File temp = new File(tempPDF);
				temp.delete();
			} else
				out = tempPDF;
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		return out;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map prepareDataSource(String str) {
		Map map = new HashMap();
		if(str==null || str.length() == 0 || str.split(":").length<2)
			return map;
		
		String[] couples = str.split(";");
		for(String c : couples){
			if(c==null || c.length() == 0 || c.split(":").length<2)
				continue;
			String key = c.split(":")[0];
			String value = c.split(":")[1];
			String type = c.split(":")[2];
			Object v = null;
			if(type.equals("int"))
				v = new Integer(value);
			else if(type.equals("double"))
				v = new Double(value);
			else 
				v = value;
			map.put(key, v);
		}
		
		return map;
	}

}
