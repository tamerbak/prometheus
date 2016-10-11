package fr.protogen.connector.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;
import fr.protogen.masterdata.DAO.GeneriumLinkDAL;

@Path("/read")
public class GeneriumService {

	
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public void load(String dto){
		try {
			dto = dto.trim();
			dto = dto.replaceAll(" encoding=\"utf-16\"", "");
			dto = dto.replaceAll(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"", "");
			System.out.println(dto);
			InputStream is = IOUtils.toInputStream(dto);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			Document document = dBuilder.parse(is);
			
			document.getDocumentElement().normalize();
			
			
			Node n = document.getElementsByTagName("driver").item(0);
			
			String driverID = n.getTextContent();
			
			GeneriumLinkDAL dal = new GeneriumLinkDAL();
			DataModel model = dal.loadDriver(driverID);
			
			List<List<DataEntry>> rows = new ArrayList<List<DataEntry>>();
			NodeList xmlRows = document.getElementsByTagName(model.getRootTag());
			for(int i=0; i < xmlRows.getLength() ; i++){
				n = xmlRows.item(i);
				Element elem = (Element)n;
				List<DataEntry> des = new ArrayList<DataEntry>();
				for(DataEntry e : model.getDataMap()){
					DataEntry de = new DataEntry();
					de.setLabel(e.getLabel());
					de.setType(e.getType());
					de.setValue(elem.getElementsByTagName(e.getLabel()).item(0).getTextContent());
					des.add(de);
				}
				rows.add(des);
			}
			
			for(List<DataEntry> row : rows){
				dal.insertData(row, model);
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXMLHello() {
	  return "<?xml version=\"1.0\"?>" + "<hello> Test WS" + "</hello>";
	}
}
