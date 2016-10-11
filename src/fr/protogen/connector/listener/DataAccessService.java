package fr.protogen.connector.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.AmanToken;
import fr.protogen.connector.model.DataCouple;
import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.DataRow;
import fr.protogen.connector.session.WebSessionManager;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dataload.QueryBuilder;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.GeneriumLinkDAL;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

@Path("/das")
public class DataAccessService {

	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public String getData(String sinputModel){
		
		DataModel inputModel = new DataModel();
		XStream parser = new XStream();
		inputModel = (DataModel) parser.fromXML(sinputModel);
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setUnrecognized("TRUE");
			inputModel.setStatus("FAILURE");
			return parser.toXML(inputModel);
		}
		
		if(inputModel.getOperation().equals("GET")){
			return postDataGet(inputModel);
		} else if(inputModel.getOperation().equals("UPDATE")) {
			return postdataUpdate(inputModel);
		} else if(inputModel.getOperation().equals("VOID")) {
			return loadVoidForm(inputModel);
		} else if(inputModel.getOperation().equals("PUT")){
			return insertData(sinputModel);
		} else {
			return dataDelete(sinputModel);
		}
	}

	private String loadVoidForm(DataModel inputModel) {
		XStream parser = new XStream();
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			return parser.toXML(inputModel);
		}
			
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			return parser.toXML(inputModel);
		}
		
		//	get entity from driver
		GeneriumLinkDAL gdal = new GeneriumLinkDAL();
		String dr;
		if(inputModel.getIddriver()>0)
			dr = gdal.getDriverReference(inputModel.getIddriver());
		else
			dr = inputModel.getEntity();
		
		if(dr == null || dr.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("Driver not correctly mapped with entity");
			return parser.toXML(inputModel);
		}
		
		inputModel.setEntity(dr);
		
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass e = pde.getReferencedTable(inputModel.getEntity());
		List<Map<CAttribute, Object>> vals = pde.getAllData(e);
		ApplicationLoader dal = new ApplicationLoader();
		inputModel.setDataMap(new ArrayList<DataEntry>());
		inputModel.setRows(new ArrayList<DataRow>());
		Map<CAttribute, Object> row = vals.get(0);
		DataRow r = new DataRow();
		r.setDataRow(new ArrayList<DataEntry>());
		for(CAttribute a : row.keySet()){
			DataEntry de = new DataEntry();
			de.setValue("");
			de.setLabel(a.getAttribute());
			de.setType(dal.parseType(a));
			de.setAttributeReference(a.getDataReference());
			if(de.getType().startsWith("fk_")){
				List<PairKVElement> elts = pde.getDataKeys(de.getType().substring(3), false, 0);
				de.setList(new ArrayList<DataCouple>());
				if(elts !=null)
					for(PairKVElement p : elts){
						DataCouple dc = new DataCouple();
						dc.setId(Integer.parseInt(p.getKey()));
						dc.setLabel(p.getValue());
						de.getList().add(dc);
					}
			}
			r.getDataRow().add(de);
			
		}
		inputModel.getRows().add(r);
		
		String serialized = parser.toXML(inputModel);
		return serialized;
		
	}

	private String postdataUpdate(DataModel inputModel){
		
		XStream parser = new XStream();
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			return parser.toXML(inputModel);
		}
			
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			return parser.toXML(inputModel);
		}
		
		GeneriumLinkDAL dal = new GeneriumLinkDAL();
		boolean status = false;
		for(DataRow de : inputModel.getRows())
			status = status || dal.updateData(de.getDataRow(), inputModel);
		
		
		inputModel = new DataModel();
		inputModel.setStatus(status?"SUCCESS":"FAILURE");
		
		return parser.toXML(inputModel);
	}
	
	private String postDataGet(DataModel inputModel){
		
		XStream parser = new XStream();

		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setExpired("TRUE");
			inputModel.setStatus("FAILURE");
			return parser.toXML(inputModel);
		}	
		
		
		String constraint = "";
		
		if(inputModel.getClauses() != null && inputModel.getClauses().size()>0){
			QueryBuilder builder = new QueryBuilder();
			constraint = builder.CreateWSConstraints(inputModel.getClauses());
		}
		
		int page = inputModel.getPage();
		int pages = inputModel.getPages();
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass e = pde.getReferencedTable(inputModel.getEntity());
		List<Map<CAttribute, Object>> vals;
		if(constraint!= null && constraint.length()>0)
			vals = pde.getDataByConstraint(e, constraint);
		else
			vals = pde.getAllData(e);
		ApplicationLoader dal = new ApplicationLoader();
		inputModel.setDataMap(new ArrayList<DataEntry>());
		inputModel.setRows(new ArrayList<DataRow>());
		
		/*
		 * Cache the key references
		 */
		Map<String, List<PairKVElement>> referencesCache = new HashMap<String, List<PairKVElement>>(); 
		
		
		for(Map<CAttribute, Object> row : vals){
			DataRow r = new DataRow();
			r.setDataRow(new ArrayList<DataEntry>());
			int id=0;
			for(CAttribute a : row.keySet()){
				DataEntry de = new DataEntry();
				Object datum = row.get(a);
				de.setValue("<![CDATA["+(datum==null?"":datum.toString())+"]]>");
				if(a.getDataReference().startsWith("pk_"))
					id = Integer.parseInt(datum.toString());
				de.setLabel("<![CDATA["+a.getAttribute()+"]]>");
				de.setType(dal.parseType(a));
				de.setAttributeReference(a.getDataReference());
				if(de.getType().startsWith("fk_") && !a.isMultiple()){
					de.setValue((datum==null?"":datum.toString()));
					List<PairKVElement> elts = new ArrayList<PairKVElement>();
					if(datum!=null && datum.toString().length()>0){
						int idDatum = Integer.parseInt(datum.toString());
						PairKVElement pkve = pde.getDataKeyByID(de.getType().substring(3), idDatum);
						pkve.setKey(pkve.getDbID()+"");
						elts.add(pkve);
					}
					/*if(inputModel.getIgnoreList()!=null && 
							inputModel.getIgnoreList().toLowerCase().equals("true"))
						elts = new ArrayList<PairKVElement>(); //
					else
						elts = pde.getDataKeys(de.getType().substring(3), true, 0);*/
					
					
					de.setList(new ArrayList<DataCouple>());
					if(elts !=null)
						for(PairKVElement p : elts){
							DataCouple dc = new DataCouple();
							dc.setId(p.getDbID());
							dc.setLabel(p.getValue());
							de.getList().add(dc);
						}
				}
				
				
				r.getDataRow().add(de);
				
			}
			Map<CBusinessClass, Integer> mtms = pde.getMtms(e,id);
			for(CBusinessClass cbc : mtms.keySet()){
				DataEntry de = new DataEntry();
				int i = mtms.get(cbc);
				de.setLabel("<![CDATA["+cbc.getName()+"]]>");
				de.setType("fk_"+cbc.getDataReference());
				de.setAttributeReference("fk_"+cbc.getDataReference()+"__"+e.getDataReference());
				de.setValue(i+"");
				r.getDataRow().add(de);
			}
			
			inputModel.getRows().add(r);
		}
		String serialized = "";
		referencesCache = null;
		/*
		 * Dealing with pages
		 */
		if(pages>0){
			int dataSize = inputModel.getRows().size();
			int nbpages = dataSize/pages;
			inputModel.setNbpages(nbpages);
			if(dataSize>pages) {
				int start = (page-1)*pages;
				int end = start+pages;
				if(end >= inputModel.getRows().size())
					end = inputModel.getRows().size()-1;
				
				if(start>0){
					List<DataRow> rtd = new ArrayList<DataRow>();
					for(int i = 0 ; i < start ; i++){
						rtd.add(inputModel.getRows().get(i));
					}
					inputModel.getRows().removeAll(rtd);
				}
				List<DataRow> rowtd = new ArrayList<DataRow>();
				for(int i = end ; i < inputModel.getRows().size() ; i++){
					rowtd.add(inputModel.getRows().get(i));
				}
				inputModel.getRows().removeAll(rowtd);
			}
		}
		
		
		try{
			String tempo = UUID.randomUUID().toString()+".xml";
			FileOutputStream out = new FileOutputStream(tempo);
			parser.toXML(inputModel, out);
			out.close();
			FileInputStream in = new FileInputStream(tempo);
			serialized = IOUtils.toString(in);
			in.close();
			File f = new File(tempo);
			f.delete();
		} catch(Exception exc) {
			System.out.print("Parsing heap problem\n*******************************\n");
			exc.printStackTrace();
			System.out.print("\n*******************************\n");
		}
		return serialized;
	}
	
	
	@PUT
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public String insertData(String sinputModel){
		DataModel inputModel = new DataModel();
		XStream parser = new XStream();
		inputModel = (DataModel) parser.fromXML(sinputModel);
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			return parser.toXML(inputModel);
		}
			
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			return parser.toXML(inputModel);
		}
		
		
		//	Insert data
		GeneriumLinkDAL dal = new GeneriumLinkDAL();
		
		int id=-1;
		String msg="";
		for(DataRow dr : inputModel.getRows()){
			msg = dal.insertData(dr.getDataRow(), inputModel);
			if(!msg.startsWith("-1"))
				id=Integer.parseInt(msg);
		}
		
		inputModel = new DataModel();
		inputModel.setStatus(id>0?""+id:msg);
		
		return parser.toXML(inputModel);
	}
	
	
	
	public String dataDelete(String sinputModel){
		DataModel inputModel = new DataModel();
		XStream parser = new XStream();
		inputModel = (DataModel) parser.fromXML(sinputModel);
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			return parser.toXML(inputModel);
		}
			
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			return parser.toXML(inputModel);
		}
		
		GeneriumLinkDAL dal = new GeneriumLinkDAL();
		DataRow de = inputModel.getRows().get(0);
		String st = "";
		st= dal.deleteData(de.getDataRow(), inputModel);
		
		
		inputModel = new DataModel();
		inputModel.setStatus(st);
		
		return parser.toXML(inputModel);
	}
	
	/*@GET
	@Produces(MediaType.TEXT_XML)
	public String getSampleData(){
		
		DataModel inputModel = new DataModel();
		inputModel.setEntity("user_lignes_commande_achat");
		ProtogenDataEngine pde = new ProtogenDataEngine();
		CBusinessClass e = pde.getReferencedTable("user_lignes_commande_achat");
		List<Map<CAttribute, Object>> vals = pde.getAllData(e);
		ApplicationLoader dal = new ApplicationLoader();
		inputModel.setDataMap(new ArrayList<DataEntry>());
		inputModel.setRows(new ArrayList<DataRow>());
		for(Map<CAttribute, Object> row : vals){
			DataRow r = new DataRow();
			r.setDataRow(new ArrayList<DataEntry>());
			int id=0;
			for(CAttribute a : row.keySet()){
				DataEntry de = new DataEntry();
				Object datum = row.get(a);
				de.setValue("<![CDATA["+(datum==null?"":datum.toString())+"]]>");
				if(a.getDataReference().startsWith("pk_"))
					id = Integer.parseInt(datum.toString());
				de.setLabel("<![CDATA["+a.getAttribute()+"]]>");
				de.setType(dal.parseType(a));
				de.setAttributeReference(a.getDataReference());
				if(de.getType().startsWith("fk_") && !a.isMultiple()){
					List<PairKVElement> elts = pde.getDataKeys(de.getType().substring(3), true, 0);
					de.setList(new ArrayList<DataCouple>());
					if(elts !=null)
						for(PairKVElement p : elts){
							DataCouple dc = new DataCouple();
							dc.setId(Integer.parseInt(p.getKey()));
							dc.setLabel(p.getValue());
							de.getList().add(dc);
						}
				}
				
				
				r.getDataRow().add(de);
				
			}
			Map<CBusinessClass, Integer> mtms = pde.getMtms(e,id);
			for(CBusinessClass cbc : mtms.keySet()){
				DataEntry de = new DataEntry();
				int i = mtms.get(cbc);
				de.setLabel("<![CDATA["+cbc.getName()+"]]>");
				de.setType("fk_"+e.getDataReference()+"__"+cbc.getDataReference());
				de.setValue(i+"");
				r.getDataRow().add(de);
			}
			
			inputModel.getRows().add(r);
		}
		XStream parser = new XStream();
		String serialized = parser.toXML(inputModel);
		return serialized;
	}
	
	
//	@GET
//	@Produces(MediaType.TEXT_XML)
//	public String insertData(){
//		
//		String sinputModel="";
//		try {
//			sinputModel = IOUtils.toString(new FileInputStream("d:\\puws.xml"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		DataModel inputModel = new DataModel();
//		XStream parser = new XStream();
//		inputModel = (DataModel) parser.fromXML(sinputModel);
//		
//		if(inputModel == null || inputModel.getToken()==null){
//			inputModel = new DataModel();
//			inputModel.setStatus("FAILURE");
//			inputModel.setUnrecognized("TRUE");
//			return parser.toXML(inputModel);
//		}
//			
//		/*AmanToken token = inputModel.getToken();
//		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
//		if(sid == null || sid.length()==0){
//			inputModel = new DataModel();
//			inputModel.setStatus("FAILURE");
//			inputModel.setExpired("TRUE");
//			return parser.toXML(inputModel);
//		}*/
//		
//		
//		//	Insert data
//		GeneriumLinkDAL dal = new GeneriumLinkDAL();
//		
//		Boolean status = false;
//		int id=-1;
//		for(DataRow dr : inputModel.getRows()){
//			id = dal.insertData(dr.getDataRow(), inputModel);
//			
//			status = status || (id>0);
//		}
//		
//		inputModel = new DataModel();
//		inputModel.setStatus(status?""+id:"FAILURE");
//		
//		return parser.toXML(inputModel);
//	}
	
//	@GET
//	@Produces(MediaType.TEXT_XML)
//	public String updateData(){
//		
//		String sinputModel="";
//		try {
//			sinputModel = IOUtils.toString(new FileInputStream("D:\\paystest.xml"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		DataModel inputModel = new DataModel();
//		XStream parser = new XStream();
//		inputModel = (DataModel) parser.fromXML(sinputModel);
//		
//		if(inputModel == null || inputModel.getToken()==null){
//			inputModel = new DataModel();
//			inputModel.setStatus("FAILURE");
//			inputModel.setUnrecognized("TRUE");
//			return parser.toXML(inputModel);
//		}
//			
//		/*AmanToken token = inputModel.getToken();
//		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
//		if(sid == null || sid.length()==0){
//			inputModel = new DataModel();
//			inputModel.setStatus("FAILURE");
//			inputModel.setExpired("TRUE");
//			return parser.toXML(inputModel);
//		}*/
//		
//		
//		//	Insert data
//		GeneriumLinkDAL dal = new GeneriumLinkDAL();
//		
//		boolean status = false;
//		for(DataRow de : inputModel.getRows())
//			status = status || dal.updateData(de.getDataRow(), inputModel);
//		
//		
//		inputModel = new DataModel();
//		inputModel.setStatus(status?"SUCCESS":"FAILURE");
//		
//		return parser.toXML(inputModel);
//	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String deleteData(){
		
		String sinputModel="";
		try {
			sinputModel = IOUtils.toString(new FileInputStream("D:\\paystest.xml"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		DataModel inputModel = new DataModel();
		XStream parser = new XStream();
		inputModel = (DataModel) parser.fromXML(sinputModel);
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			return parser.toXML(inputModel);
		}
			
		/*AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			return parser.toXML(inputModel);
		}*/
		
		
		//	Insert data
		GeneriumLinkDAL dal = new GeneriumLinkDAL();
		
		DataRow de = inputModel.getRows().get(0);
		String st =  dal.deleteData(de.getDataRow(), inputModel);
		
		
		inputModel = new DataModel();
		inputModel.setStatus(st);
		
		return parser.toXML(inputModel);
	}
}
