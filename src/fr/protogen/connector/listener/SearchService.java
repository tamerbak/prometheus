package fr.protogen.connector.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.DataCouple;
import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.DataRow;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dataload.SearchEngine;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

@Path("/recherche")
public class SearchService {

	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_PLAIN)
	public String search(String fullQuery){
		DataModel inputModel = new DataModel();
		inputModel.setDataMap(new ArrayList<DataEntry>());
		inputModel.setRows(new ArrayList<DataRow>());
		XStream parser = new XStream();
		
		String table = fullQuery.split(";")[0].trim();
		String query = fullQuery.split(";")[1].trim();
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		SearchEngine se = new SearchEngine(); 
		List<Map<CAttribute, Object>> vals = new ArrayList<Map<CAttribute,Object>>();
		CBusinessClass target = pde.getReferencedTable(table);
		ApplicationLoader dal = new ApplicationLoader();
		inputModel.setEntity(table);
		
		List<Integer> res = se.search(target, query);
		String constraint = "pk_"+table+" in (0";
		for(Integer i : res){
			constraint = constraint+","+i.intValue();
		}
		constraint = constraint+")";
		vals = pde.getDataByConstraint(target, constraint);
		
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
					List<PairKVElement> elts;
					
					if(inputModel.getIgnoreList()!=null && 
							inputModel.getIgnoreList().toLowerCase().equals("true"))
						elts = new ArrayList<PairKVElement>(); //
					else
						elts = pde.getDataKeys(de.getType().substring(3), true, 0);
					
					
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
			Map<CBusinessClass, Integer> mtms = pde.getMtms(target,id);
			for(CBusinessClass cbc : mtms.keySet()){
				DataEntry de = new DataEntry();
				int i = mtms.get(cbc);
				de.setLabel("<![CDATA["+cbc.getName()+"]]>");
				de.setType("fk_"+cbc.getDataReference());
				de.setAttributeReference("fk_"+cbc.getDataReference()+"__"+target.getDataReference());
				de.setValue(i+"");
				r.getDataRow().add(de);
			}
			
			inputModel.getRows().add(r);
		}
		
		String serialized="";
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
}
