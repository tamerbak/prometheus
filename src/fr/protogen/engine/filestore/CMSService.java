package fr.protogen.engine.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import com.itextpdf.text.pdf.codec.Base64;

import fr.protogen.callout.service.RemoteCalloutService;
import fr.protogen.engine.control.ui.RowFolder;
import fr.protogen.engine.utils.ListKV;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CFolder;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.RowDocument;

public class CMSService {
	
	private ApplicationLoader dal = null;
	
	private static CMSService instance = null;
	public static synchronized CMSService getInstance(){
		if(instance == null)
			instance = new CMSService();
		return instance;
	}
	private CMSService(){
		this.dal = new ApplicationLoader();
	}
	
	public boolean checkFileStore(CWindow w){
		int count = dal.existsFolders(w.getId());
		return count>0;
	}
	
	public CFolder loadRootFolder(CWindow w){
		CFolder root = dal.loadRootFolder(w.getId());
		recursivePopulation(root);
		return root;
	}
	private void recursivePopulation(CFolder f) {
		int subcount = dal.countSubfolders(f.getId());
		if(subcount == 0)
			return;
		
		List<CFolder> subs = dal.loadSubfolders(f.getId());
		f.setSubFolders(subs);
		for(CFolder sf : subs)
			recursivePopulation(sf);
	}
	
	
	public void populateFolders(RowFolder root, int dbId, CWindow w){
		List<RowDocument> docs = dal.loadDocuments(root.getFolder().getId(), w.getId(), dbId);
		root.setDocuments(docs);
		if(root.getSubFolders() == null || root.getSubFolders().isEmpty())
			return;
		for(RowFolder f : root.getSubFolders())
			populateFolders(f, dbId, w);
	}
	
	public String downloadRowDoc(RowDocument doc, ListKV row) {
		String tmpFilePath = UUID.randomUUID().toString()+"."+doc.getExtension();
		if(doc.getStoreCallout().getId()>0){
			//	Prepare arguments
			String args = doc.getStorageIdentifier().replaceAll("<<DBID>>", row.getDbID()+"");
			String enc = new String(com.sun.jersey.core.util.Base64.encode(args)); 
			String res="";
			try{
				res = RemoteCalloutService.getInstance().remoteCallout(enc, doc.getStoreCallout().getId());
			}catch(Exception exc){
				exc.printStackTrace();
			}
			
			
			switch(doc.getType().getId()){
				case 1:printPDF(tmpFilePath, res);break;
				case 2:printImage(tmpFilePath, res, doc);break;
			}
			
		} else {
			String res = doc.getTextContent();
			if(!res.contains("data:"))
				tmpFilePath = "data:image/"+doc.getExtension()+";base64,"+res;
			
			return res;
		}
		return tmpFilePath;
	}
	
	private void printImage(String tmpFilePath, String res, RowDocument doc) {
		if(res.contains("data:")){
			tmpFilePath = res;
		} else {
			tmpFilePath = "data:image/"+doc.getExtension()+";base64,"+res;
		}
	}
	
	private void printPDF(String tmpFilePath, String res) {
		List<String> pdfs = new ArrayList<String>();
		String[] files = res.split(",");
		for(String e : files){
			try {
				String pdf = UUID.randomUUID().toString()+".pdf";
				byte[] data = Base64.decode(e);
				OutputStream output;
				output = new FileOutputStream(pdf);
				IOUtils.write(data, output);
				output.close();
				pdfs.add(pdf);
			} catch (FileNotFoundException exc) {
				exc.printStackTrace();
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
		String tempPDF = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+"/"+tmpFilePath;
		try{
			PDFMergerUtility pdfMU = new PDFMergerUtility();
			for(String pdf : pdfs){
				pdfMU.addSource(new File(pdf));
			}
			pdfMU.setDestinationFileName(tempPDF);
			pdfMU.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(String pdf : pdfs){
			File f = new File(pdf);
			if(f.exists())
				f.delete();
		}
		
	}
}
