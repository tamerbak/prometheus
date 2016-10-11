package fr.protogen.engine.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import com.sun.jersey.core.util.Base64;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class JSONPDFWriter implements PDFWriter {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String doPrint(List<String> filePath, String dataSource, boolean encodedIn, boolean encodedOut) {
		String in = "";	
		if(encodedIn){
			byte[] decoded = Base64.decode(dataSource);
			try {
				in = new String(decoded, "UTF-8");
				System.out.println("Decoded JSON Data stream : "+in);
			} catch (UnsupportedEncodingException e) {
				in=Base64.base64Decode(dataSource);
				System.out.println("UTF-8 Decoding ended in failure returning to binary decoding : "+in);
				e.printStackTrace();
			}
			//in=Base64.base64Decode(dataSource);
		}else
			in = dataSource;
		
		String tempJson = UUID.randomUUID().toString()+".json";
		String tempPDF = UUID.randomUUID().toString()+".pdf";
		List<String> pdfs = new ArrayList<String>();
		String out = "";
		for(String f : filePath){
			try{
				String pdf = UUID.randomUUID().toString()+".pdf";
				OutputStream os = new FileOutputStream(tempJson);
				IOUtils.write(in, os);
				os.close();
				InputStream is = new FileInputStream(f);
				Map params = new HashMap();
			    
			    JsonDataSource jds = new JsonDataSource(new File(tempJson));
			    JasperDesign jasperDesign = JRXmlLoader.load(is);
			    JasperReport jasperReport  = JasperCompileManager.compileReport(jasperDesign);
			    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,  params,  jds);
			    JasperExportManager.exportReportToPdfFile(jasperPrint, pdf);
			    is.close();
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
				temp = new File(tempJson);
				temp.delete();
			} else
				out = tempPDF;
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		return out;
	}

}
