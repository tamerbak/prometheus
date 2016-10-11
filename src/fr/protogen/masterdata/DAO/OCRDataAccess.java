package fr.protogen.masterdata.DAO;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.faces.context.FacesContext;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.FileManipulation;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.ProtogenParameters;
import fr.protogen.engine.utils.UIFilterElement;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.OCRDriverBean;
import fr.protogen.masterdata.model.OCRHistory;
import fr.protogen.ocr.pojo.Colonne;
import fr.protogen.ocr.pojo.Document;
import fr.protogen.ocr.pojo.Header;
import fr.protogen.ocr.pojo.Ligne;
import fr.protogen.ocr.pojo.Singledata;
import fr.protogen.ocr.pojo.Tableau;

public class OCRDataAccess {

	public int dataInsert(Document d) {
		
		Connection cnx = null;
		
			cnx = ProtogenConnection.getInstance().getConnection();
		String sql = "insert into "+d.getMainEntity()+" ";
		String fields="";
		String values="";
		int iddb=0;
		
		//	Simple attributes
		for(Singledata f : d.getSingledatas()){
			if(f.getId().split("\\.").length==2 || (f.getFormat()!=null && f.getFormat().equals("ignore")))
				continue;
			
			
			fields=fields+f.getId()+",";
			values=values+"'"+format(f.getFormat(),trim(f.getData()))+"',";
		}
		
		//	Single references
		for(Singledata f : d.getSingledatas()){
			if(f.getId().split("\\.").length!=2 || f.getFormat().equals("ignore"))
				continue;
			
			ProtogenDataEngine engine = new ProtogenDataEngine();
			String datalabel = trim(f.getData());
			datalabel = datalabel.replaceAll("'", "\\'");
			
			int id=engine.getIdentifierByField(f.getId().split("\\.")[0],f.getId().split("\\.")[1],datalabel);
			fields = fields+"fk_"+f.getId().split("\\.")[0]+",";
			values = values+id+",";
		}
		
		if(fields.length()>1){
			fields = fields.substring(0,fields.length()-1);
			values = values.substring(0,values.length()-1);
		}
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		if(u.getOrgInstance() == 0){
			u.setOrgInstance(u.getOriginalOrganization().getIdBean());
		}
		sql = sql+"("+fields+",protogen_user_id) values ("+values+","+u.getOrgInstance()+")";
		System.out.println("*************************");
		System.out.println("******SQL REQUEST********");
		System.out.println("*************************");
		System.out.println(sql);
		System.out.println("*************************");
		System.out.println("******SQL REQUEST********");
		System.out.println("*************************");
		int mainID=0;
		try{
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.execute();
			ps.close();
			
			sql = "select nextval('"+d.getMainEntity()+"_seq');";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				mainID = rs.getInt(1)-1;
			iddb = mainID;
			rs.close();
			ps.close();
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(d.getTableaux() == null || d.getTableaux().size()==0)
			return iddb;
		for(Tableau t : d.getTableaux()){
			Ligne fl = new Ligne();
			fl.setColonnes(new ArrayList<Colonne>());
			if(t.getHeaders()==null || t.getHeaders().size()==0)
				return iddb;
			for(Header h : t.getHeaders()){
				Colonne c = new Colonne();
				c.setHeader(h.getNom());
				c.setData(h.getNom());
				c.setFormat(h.getFormat());
				fl.getColonnes().add(c);
			}
			for(Ligne l : t.getLignes()){
				
				sql = "insert into "+t.getId() +" ";
				fields = "";
				values = "";
				
				for(Colonne f : l.getColonnes()){
					String h = fl.getColonnes().get(l.getColonnes().indexOf(f)).getHeader();
					String form = fl.getColonnes().get(l.getColonnes().indexOf(f)).getFormat();
					if(h.split("\\.").length==2 || form.toLowerCase().equals("ignore") )
						continue;
					
					fields=fields+h+",";
					values=values+"'"+format(fl.getColonnes().get(l.getColonnes().indexOf(f)).getFormat(),trim(f.getData()))+"',";
				}
				for(Colonne f : l.getColonnes()){
					String h = fl.getColonnes().get(l.getColonnes().indexOf(f)).getHeader();
					String form = fl.getColonnes().get(l.getColonnes().indexOf(f)).getFormat();
					if(h.split("\\.").length!=2 ||  form.toLowerCase().equals("ignore"))
						continue;
					
					ProtogenDataEngine engine = new ProtogenDataEngine();
					String datalabel = f.getData().replaceAll("\'", "\\'").trim();
					int id=engine.getIdentifierByField(h.split("\\.")[0],h.split("\\.")[1],datalabel);
					fields = fields+"fk_"+h.split("\\.")[0]+",";
					values = values+id+",";
				}
				
				fields = fields+"fk_"+t.getId()+"__"+d.getMainEntity();
				values=values+mainID;
				
				sql = sql+"("+fields+") values ("+values+")";
				
				System.out.println("*************************");
				System.out.println("******SQL REQUEST********");
				System.out.println("*************************");
				System.out.println(sql);
				System.out.println("*************************");
				System.out.println("******SQL REQUEST********");
				System.out.println("*************************");
				
				try{
					PreparedStatement ps = cnx.prepareStatement(sql);
					ps.execute();
					ps.close();
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return iddb;
		
	}

	private String trim(String data) {
		data = data.replaceAll("\n", "");
		data = data.trim();
		return data;
	}

	private String format(String format, String data) {
		
		if(format==null || format.length()==0)
			return data.trim();
		
		String value = "";
		
		if(format.equals("remplace:/:-")){
			//	Date
			data=data.replaceAll("Z", "2");
			data = data.replaceAll("/", "-");
			String[] dateArray = data.split("-");
			data = dateArray[2]+"-"+dateArray[1]+"-"+dateArray[0]+" 00:00:00+00";
			value=data;
		}
		if(format.startsWith("remplace:,:.")){
			String o = format.split(":")[1];
			String s = format.split(":")[2];
			
			value = data.replaceAll(o, s);
			value = value.replaceAll(" ", "");
		}
		if(format.startsWith("force:")){
			String o = format.split(":")[1];
			String s = format.split(":")[2];
			
			value = data.replaceFirst(o, s);
			
		}
		
		return value.trim();
	}

	public List<OCRHistory> lookUp(String selectedBean, String selectedDriver,
			int selectedEntity, List<UIFilterElement> searchControls) {
		ApplicationLoader dal = new ApplicationLoader();
		CBusinessClass filterEntity = dal.getEntityById(selectedEntity);
		
		List<OCRHistory> results = new ArrayList<OCRHistory>();
		
		String sql = "select id, bean_id, entity_id, ocr_date, cuser, driver, file_key from ged_ocr_history ";
		if(filterEntity.getDataReference()!=null && filterEntity.getDataReference().length()>0)
			sql = sql+", "+filterEntity.getDataReference();
		
		List<String> constraints=new ArrayList<String>();
		
		if(selectedBean!=null && selectedBean.length()>0)
			constraints.add("bean_id="+selectedBean);
		if(selectedEntity>0)
			constraints.add("entity_id="+selectedEntity);
		if(selectedDriver!=null && selectedDriver.length()>0)
			constraints.add("driver="+selectedDriver);
		
		
		
		/*
		 * 	Semantic search
		 */
		List<String> filters = new ArrayList<String>();
		if(searchControls!=null && searchControls.size()>0 && 
				filterEntity.getDataReference()!=null && filterEntity.getDataReference().length()>0){
			String dr = filterEntity.getDataReference();
			constraints.add(dr+".pk_"+dr+"=ged_ocr_history.bean_id");
			for(UIFilterElement c : searchControls){
				if(c.getAttribute().getCAttributetype().getId()==3){
					// Date
					if(c.getAdateValue()!=null){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						String d = sdf.format(c.getAdateValue());
						filters.add(dr+"."+c.getAttribute().getDataReference()+">'"+d+"'");
					}
					if(c.getBdateValue()!=null){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						String d = sdf.format(c.getBdateValue());
						filters.add(dr+"."+c.getAttribute().getDataReference()+"<'"+d+"'");
					}
				} else if(c.isReference()){
					//	Data Reference
					if(c.getControlValue()!=null && c.getControlValue().length()>0){
						filters.add(dr+"."+c.getAttribute().getDataReference()+"="+c.getControlValue());
					}
				} else if(c.getAttribute().getCAttributetype().getId()==2){
					if(c.getGthan()!= null && c.getGthan().length()>0)
						filters.add(dr+"."+c.getAttribute().getDataReference()+" like '%"+c.getGthan()+"'");
					if(c.getLthan()!= null && c.getLthan().length()>0)
						filters.add(dr+"."+c.getAttribute().getDataReference()+" like '"+c.getLthan()+"%'");
				} else if((c.getAttribute().getCAttributetype().getId()==4)||(c.getAttribute().getCAttributetype().getId()==8)){
					if(c.getGthan()!= null && c.getGthan().length()>0)
						filters.add(dr+"."+c.getAttribute().getDataReference()+" > "+c.getGthan()+"");
					if(c.getLthan()!= null && c.getLthan().length()>0)
						filters.add(dr+"."+c.getAttribute().getDataReference()+" < "+c.getLthan()+"");
				}
			}
		}
		if(constraints.size()>0){
			sql = sql+" WHERE ";
			for(String c : constraints){
				sql = sql+c+" AND ";
			}
			sql = sql.substring(0,sql.length()-5);
		}
		if(filters.size()>0){
			if(constraints.size()>0)
				sql = sql+" AND ";
			else
				sql = sql+" WHERE ";
			for(String c : filters){
				sql = sql+c+" AND ";
			}
			sql = sql.substring(0,sql.length()-5);
		}
		
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				OCRHistory doc = new OCRHistory();
				doc.setId(rs.getInt(1));
				doc.setBeanId(rs.getInt(2));
				doc.setEntity(new CBusinessClass());
				doc.getEntity().setId(rs.getInt(3));
				doc.setOcrDate(rs.getDate(4));
				doc.setUser(new CoreUser());
				doc.getUser().setId(rs.getInt(5));
				doc.setDriver(new OCRDriverBean());
				doc.getDriver().setId(rs.getInt(6));
				doc.setFileKey(rs.getString(7));
				results.add(doc);
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		for(OCRHistory d : results){
			CBusinessClass e = dal.getEntityById(d.getEntity().getId());
			PairKVElement p = (new ProtogenDataEngine()).getDataKeyByID(e.getDataReference(), d.getBeanId());
			d.setBean(p.getValue());
			d.setEntity(e);
		}
		
		return results;
	}

	public void insertHistory(int id, String mainEntity, int idDriver, String f, String iddoc) {
		
		Date d = new Date();
		Calendar c = new GregorianCalendar();
		d = c.getTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String dt = df.format(d);
		String sql = "insert into ged_ocr_history (bean_id,entity_id,ocr_date,cuser,appkey,driver,file_key) values (?,?,'"+dt+"',?,?,?,?)";
		String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
		ApplicationCache cache = ApplicationRepository.getInstance().getCache(skey);
		
		int userId = cache.getUser().getId();
		
		String histoDir = ProtogenParameters.SERVER_PATH+"/history/";
		File dir = new File(histoDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		String filename = ProtogenParameters.SERVER_PATH+"/history/"+UUID.randomUUID().toString()+"--"+iddoc+".png";
		CBusinessClass en = (new ApplicationLoader()).getEntity(mainEntity);
		int entityId = en.getId();
		String appkey = cache.getAppKey();
		
		
		
		try{
			if(f!=null)
				FileManipulation.getInstance(ProtogenParameters.SERVER_PATH).copyFiles(f,filename);
			
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ps.setInt(2, entityId);
			ps.setInt(3, userId);
			ps.setString(4, appkey);
			ps.setInt(5, idDriver);
			ps.setString(6, filename);
			
			ps.execute();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

}
