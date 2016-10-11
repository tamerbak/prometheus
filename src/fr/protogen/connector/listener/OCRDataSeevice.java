package fr.protogen.connector.listener;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.thoughtworks.xstream.XStream;

import fr.protogen.masterdata.DAO.OCRDataAccess;
import fr.protogen.ocr.pojo.Document;

@Path("/ocr")
public class OCRDataSeevice {
	
	@POST
	@Consumes(MediaType.TEXT_XML)
	public void loadOCR(String xml){
		Document doc = (Document)(new XStream()).fromXML(xml);
		
		OCRDataAccess dao = new OCRDataAccess();
		int id = dao.dataInsert(doc);
		if(id>0)
			dao.insertHistory(id,doc.getMainEntity(),Integer.parseInt(doc.getIddriver()),null,doc.getIddocument());
		
	}
}
