package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;






import java.util.Map;

import fr.protogen.connector.model.DataCouple;
import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.DataRow;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.StringFormat;
import fr.protogen.export.engine.DefaultValueEngine;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class GeneriumLinkDAL {

	public DataModel loadDriver(String driver){
		DataModel results = new DataModel();
		try{
			Class.forName("org.postgresql.Driver");

			Connection cnx=ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			String sql = "select l.main_entity, l.data_root_tag, m.tag_value, m.generium_field "
					+ "from s_link l, s_link_mapping m "
					+ "where l.id=m.driver_id and l.driver_id=?";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setString(1, driver);
			
			ResultSet rs = ps.executeQuery();
			boolean flag = true;
			while(rs.next()){
				if(flag){
					results.setEntity(rs.getString(1));
					results.setRootTag(rs.getString(2));
					results.setDataMap(new ArrayList<DataEntry>());
					flag=false;
				}
				DataEntry e = new DataEntry();
				e.setLabel(rs.getString(3));
				e.setType(rs.getString(4));
				results.getDataMap().add(e);
			}
			rs.close();
			ps.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return results;
	}

	public boolean updateData(List<DataEntry> row, DataModel model) {
		try{
			Class.forName("org.postgresql.Driver");

			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "update "+model.getEntity()+" set ";
			DataEntry pk = new DataEntry();
			for(DataEntry de : row){
				if(de.getType().equals("PK")){
					pk = de;
					continue;
				}
				if(de.getAttributeReference().startsWith("fk_") && de.getValue().equals("0"))
					continue;
				String val = de.getValue().replaceAll("<\\!\\[CDATA\\[", "").replaceAll("\\]\\]>", "").replaceAll("&lt;\\!\\[CDATA\\[", "").replaceAll("\\]\\]&gt;", "");
				sql = sql+de.getAttributeReference()+"='"+val+"' , ";
			}
			sql = sql.substring(0,sql.length()-3);
			sql = sql+" WHERE pk_"+model.getEntity()+"="+pk.getValue();
			System.out.println("WS UPDATE QUERY \t "+sql);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.execute();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
			return false;
			
		}
		return true;
	}
	public String insertData(List<DataEntry> row, DataModel model) {
		try{
			Class.forName("org.postgresql.Driver");

			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql="insert into "+model.getEntity()+" ";
			String cols="";
			String vals="";
			
			for(DataEntry de : row){
				if(de.getType().equals("PK")){
					continue;
				}
				
				String value = de.getValue().replaceAll("<\\!\\[CDATA\\[", "").replaceAll("\\]\\]>", "").replaceAll("&lt;\\!\\[CDATA\\[", "").replaceAll("\\]\\]&gt;", "");
				
				if(de.getAttributeReference().startsWith("fk_") && (value.equals("0") || value.equals("")))
					continue;
				
				if(value==""){
					DefaultValueEngine engine = new DefaultValueEngine();
					value=engine.getDefaultValue(model.getEntity(), de.getLabel());
				}
				
				cols = cols+","+de.getAttributeReference();
				vals = vals+",'"+StringFormat.getInstance().format(value)+"'";
			}
			
			
			sql = sql+"("+cols.substring(1)+") values ("+vals.substring(1)+")";
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.execute();
			
			ps.close();
			
			sql = "select nextval('"+model.getEntity()+"_seq')";
			ps = cnx.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			int id=-1;
			if(rs.next())
				id = rs.getInt(1)-1;
			
			rs.close();
			ps.close();
			
			return ""+id;
		}catch(Exception e){
			return -1+":"+e.getMessage();
		}
	}

	public String deleteData(List<DataEntry> row, DataModel model) {
		String status="SUCCESS";
		try{
			Class.forName("org.postgresql.Driver");

			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql="delete from "+model.getEntity()+" where pk_"+model.getEntity()+"=";
			
			DataEntry pk = null;
			for(DataEntry de : row){
				if(de.getType().equals("PK")){
					pk = de;
					break;
				}
				
			}
			
			
			sql = sql+pk.getValue();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.execute();
			
			ps.close();
			
			
		}catch(Exception e){
			e.printStackTrace();
			status = e.getMessage();
		}
		
		return status;
		
	}

	public String getDriverReference(int iddriver) {
		String table="";
		try{
			Class.forName("org.postgresql.Driver");

			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			String sql="select reference from user_ocr_driver where pk_user_ocr_driver=? ";
			System.out.println("GET REFERENCE FROM DRIVER : \n\t"+sql);
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, iddriver);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				
				table = rs.getString(1);
			}
			
			System.out.println("CLOSING");
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			System.out.println("EXCEPTION\t"+exc.getMessage());
			exc.printStackTrace();
		}
		
		return table;
	}
	
	/*
	 * 	SEARCH QUERY
	 */
	public List<DataModel> lookUp(List<String> tables, String cle, boolean ignoreList){
		
		List<DataModel> results = new ArrayList<DataModel>();
		
		for(String table : tables){
			
			ProtogenDataEngine pde = new ProtogenDataEngine();
			CBusinessClass e = pde.getReferencedTable(table);
			List<Map<CAttribute, Object>> vals;
			vals = pde.getAllData(e);
			DataModel inputModel = new DataModel();
			inputModel.setEntity(table);
			inputModel.setRows(new ArrayList<DataRow>());
			ApplicationLoader dal = new ApplicationLoader();
			for(Map<CAttribute, Object> row : vals){
				boolean found = false;
				DataRow r = new DataRow();
				r.setDataRow(new ArrayList<DataEntry>());
				int id=0;
				for(CAttribute a : row.keySet()){
					DataEntry de = new DataEntry();
					Object datum = row.get(a);
					de.setValue("<![CDATA["+(datum==null?"":datum.toString())+"]]>");
					found = found || (datum!=null && datum.toString().contains(cle));
					if(a.getDataReference().startsWith("pk_"))
						id = Integer.parseInt(datum.toString());
					de.setLabel("<![CDATA["+a.getAttribute()+"]]>");
					de.setType(dal.parseType(a));
					de.setAttributeReference(a.getDataReference());
					if(de.getType().startsWith("fk_") && !a.isMultiple()){
						List<PairKVElement> elts;
						if(ignoreList)
							elts = new ArrayList<PairKVElement>();
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
				Map<CBusinessClass, Integer> mtms = pde.getMtms(e,id);
				for(CBusinessClass cbc : mtms.keySet()){
					DataEntry de = new DataEntry();
					int i = mtms.get(cbc);
					de.setLabel("<![CDATA["+cbc.getName()+"]]>");
					de.setType("fk_"+cbc.getDataReference());
					de.setValue(i+"");
					r.getDataRow().add(de);
				}
				if(found)
					inputModel.getRows().add(r);
			}
			results.add(inputModel);
		}
		
		return results;
		
	}
}
