package fr.protogen.connector.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
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

@Path("/dasjs")
public class DataAccessServiceJS {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getData(InputStream sinputModel){
		StringBuilder jsBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sinputModel));
			String line = null;
			while ((line = in.readLine()) != null) {
				jsBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}
		System.out.println("Data Received: " + jsBuilder.toString());
		String jsonText = jsBuilder.toString();
		
		DataModel inputModel = new JSONDeserializer<DataModel>().deserialize(jsonText);
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setUnrecognized("TRUE");
			inputModel.setStatus("FAILURE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
		}
		
		if(inputModel.getOperation().equals("GET")){
			return postDataGet(inputModel);
		} else if(inputModel.getOperation().equals("UPDATE")) {
			return postdataUpdate(inputModel);
		} else if(inputModel.getOperation().equals("VOID")) {
			return loadVoidForm(inputModel);
		} else if(inputModel.getOperation().equals("PUT")){
			return insertData(jsonText);
		} else {
			return dataDelete(jsonText);
		}
	}

	
	private Response loadVoidForm(DataModel inputModel) {
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
		}
			
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
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
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
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
		
		String res = new JSONSerializer().serialize(inputModel);
		return Response.status(200).entity(res).build();
		
	}

	private Response postdataUpdate(DataModel inputModel){
		
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
		}
			
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
		}
		
		GeneriumLinkDAL dal = new GeneriumLinkDAL();
		boolean status = false;
		for(DataRow de : inputModel.getRows())
			status = status || dal.updateData(de.getDataRow(), inputModel);
		
		
		inputModel = new DataModel();
		inputModel.setStatus(status?"SUCCESS":"FAILURE");
		
		String res = new JSONSerializer().serialize(inputModel);
		return Response.status(200).entity(res).build();
	}
	
	private Response postDataGet(DataModel inputModel){
		
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setExpired("TRUE");
			inputModel.setStatus("FAILURE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
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
		
		
		for(Map<CAttribute, Object> row : vals){
			DataRow r = new DataRow();
			r.setDataRow(new ArrayList<DataEntry>());
			int id=0;
			for(CAttribute a : row.keySet()){
				DataEntry de = new DataEntry();
				Object datum = row.get(a);
				de.setValue((datum==null?"":datum.toString()));
				if(a.getDataReference().startsWith("pk_"))
					id = Integer.parseInt(datum.toString());
				de.setLabel(a.getAttribute());
				de.setType(dal.parseType(a));
				de.setAttributeReference(a.getDataReference());
				if(de.getType().startsWith("fk_") && !a.isMultiple()){
					/*List<PairKVElement> elts;
					
					if(inputModel.getIgnoreList()!=null && 
							inputModel.getIgnoreList().toLowerCase().equals("true"))
						elts = new ArrayList<PairKVElement>(); //
					else
						elts = pde.getDataKeys(de.getType().substring(3), true, 0);
					*/
					de.setValue((datum==null?"":datum.toString()));
					List<PairKVElement> elts = new ArrayList<PairKVElement>();
					if(datum!=null && datum.toString().length()>0){
						int idDatum = Integer.parseInt(datum.toString());
						PairKVElement pkve = pde.getDataKeyByID(de.getType().substring(3), idDatum);
						pkve.setKey(pkve.getDbID()+"");
						elts.add(pkve);
					}
					
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
				de.setLabel(cbc.getName());
				de.setType("fk_"+cbc.getDataReference());
				de.setAttributeReference("fk_"+cbc.getDataReference()+"__"+e.getDataReference());
				de.setValue(i+"");
				r.getDataRow().add(de);
			}
			
			inputModel.getRows().add(r);
		}
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
		String res = "[";
		
		for(DataRow r : inputModel.getRows()){
			res = res+"{";
			for(DataEntry de : r.getDataRow()){
				res = res+"'"+de.getAttributeReference()+"'";
				if(de.getAttributeReference().startsWith("fk_") && de.getList().size()>0){
					res = res+":{'id':'"+de.getValue()+"', 'label':'"+de.getList().get(0).getLabel()+"'},";
				} else {
					res = res+":'"+de.getValue()+"',";
				}
			}
			if(r.getDataRow().size()>0)
				res = res.substring(0, res.length()-1);
			res = res+"},";
		}
		if(inputModel.getRows().size()>0)
			res = res.substring(0, res.length()-1);
		res = res+"]";
		
		res = res+"]";
		//String res = new JSONSerializer().include("rows.dataRow").serialize(inputModel);
		return Response.status(200).entity(res).build();
	}
	
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insertData(String sinputModel){
		DataModel inputModel = new JSONDeserializer<DataModel>().deserialize(sinputModel);
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
		}
			
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
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
		
		String res = new JSONSerializer().serialize(inputModel);
		return Response.status(200).entity(res).build();
	}
	
	
	
	public Response dataDelete(String sinputModel){
		DataModel inputModel = new JSONDeserializer<DataModel>().deserialize(sinputModel);
		
		
		if(inputModel == null || inputModel.getToken()==null){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setUnrecognized("TRUE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
		}
			
		AmanToken token = inputModel.getToken();
		String sid = WebSessionManager.getInstance().ttlCheck(token.getSessionId());
		if(sid == null || sid.length()==0){
			inputModel = new DataModel();
			inputModel.setStatus("FAILURE");
			inputModel.setExpired("TRUE");
			String res = new JSONSerializer().serialize(inputModel);
			return Response.status(200).entity(res).build();
		}
		
		GeneriumLinkDAL dal = new GeneriumLinkDAL();
		DataRow de = inputModel.getRows().get(0);
		String st = "";
		st= dal.deleteData(de.getDataRow(), inputModel);
		
		
		inputModel = new DataModel();
		inputModel.setStatus(st);
		
		String res = new JSONSerializer().serialize(inputModel);
		return Response.status(200).entity(res).build();
	}
		
}
