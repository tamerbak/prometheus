package fr.protogen.webServices;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.FormWindowModel;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.control.ui.MtmBlock;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.engine.utils.UIControlsLine;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.GeneriumLinkDAL;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.services.MTMService;
import fr.protogen.webServices.refactoredClasses.FormViewController;

@Path("/formWindow")
public class FormWindowService {
	private ApplicationLoader appLoader;
	private FormViewController formController;
	private Gson gson;
	public static Type formResponseType=new TypeToken<Map<String, FormWindowModel>>(){}.getType();
	public static final String WINDOW_ENTITY_SEPARATOR="-@";
	
	public FormWindowService() {
		appLoader=new ApplicationLoader();
		formController=new FormViewController();
		gson=new GsonBuilder().create();
//		gson=new GsonBuilder().registerTypeAdapter(CBusinessClass.class, new GeneriumTypeAdapter<CBusinessClass>()).create();
		
	}
	
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
    @Produces(MediaType.APPLICATION_JSON)
	public String sayXMLHello(@QueryParam("appKey") String appKey) {
		// Get Parameter
		// appKey  CBusinessClass

		Map<String, FormWindowModel> icLinesMap = new HashMap<String, FormWindowModel>();
		List<CWindow> windows = loadAppFormWindows(appKey);
		MTMService service = new MTMService();
		for (CWindow cWindow : windows) {
			cWindow = appLoader.loadFullWindow(cWindow);
			formController.setWindow(cWindow);
			CBusinessClass entity = new CBusinessClass();
			entity.setDataReference(cWindow.getMainEntity());
			
			List<MtmDTO> dtos = service.getMtm(cWindow, entity);
			List<MtmBlock> mtmBlocks = populate(dtos);
			
			formController.loadVoidComponents();
			FormWindowModel fwm = new FormWindowModel();
			fwm.setControlLines(formController.getControlLines());
			fwm.setMtmBlocks(mtmBlocks);
			
			icLinesMap.put(cWindow.getTitle() + WINDOW_ENTITY_SEPARATOR + cWindow.getMainEntity(), fwm);
		}

//		String json = gson.toJson(icLinesMap, formResponseType);
		String json = gson.toJson(icLinesMap);
		return json;
	}
	
	/*
	 * Looks for all windows associated with an application
	 * @param appkey : Application Key
	 * @return a list of app's windows
	 */
	public List<CWindow> loadAppFormWindows(String appKey){
		try {
			
			List<CWindow> windows=new ArrayList<CWindow>();
			CWindow window;
			//getting DB Connection
		    Connection cnx=ProtogenConnection.getInstance().getConnection();
		    
			//building query
			String globalQuery = "SELECT c_window.id AS w_id FROM public.c_window WHERE c_window.appkey='"+appKey+"' AND c_window.id_windowtype=2 ";
		
			
			//PreparedStatement ps =cnx.prepareStatement(globalQuery);
			//replacing placeHolders
			//appKey
			//ps.setString(1,appKey);
			//Window Form Type
			// TODO use constant
			//ps.setInt(2,2);
//			ResultSet apprs = ps.executeQuery();
			
			Statement ps = cnx.createStatement();
			ResultSet apprs = ps.executeQuery(globalQuery);
		
			//fetching results
			while(apprs.next()){
				//Retrieve Window ID
				//A window is returned for each one of its attribute
				window=new CWindow();
				window.setId(apprs.getInt("w_id"));	
				windows.add(window);
			}
			apprs.close();
			
			return windows;
			
		} catch(Exception exc){
			exc.printStackTrace();
		}
		
		return null;
	}
	
	private List<MtmBlock> populate(List<MtmDTO> dtos) {
		
		List<MtmBlock> results = new ArrayList<MtmBlock>();
		for(MtmDTO dto : dtos){
			MtmBlock block = new MtmBlock();
			block.setEntity(dto.getMtmEntity());
			block.setEntityID(dto.getMtmEntity().getId());
			//block.setLines(new ArrayList<MtmLine>());
			block.setTitles(new ArrayList<String>());
			
			for(CAttribute a : dto.getMtmEntity().getAttributes()){
				a.setEntity(null);
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				if(a.isAutoValue())
					continue;
				block.getTitles().add(a.getAttribute());
			}
		
			
			/*
			 if(action.equals("update") )
				for(Map<CAttribute, Object> dataLine : dto.getMtmData()){
					MtmLine line = new MtmLine();
					line.setValues(new ArrayList<PairKVElement>());
					for(CAttribute a : dataLine.keySet()){
						if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
							continue;
						if(a.isAutoValue())
							continue;
						String val = dataLine.get(a).toString();
						PairKVElement pkv=new PairKVElement(a.getDataReference(), val);
						
						if(a.getCAttributetype().getId() == 3){
							pkv.setDate(true);
							String dval = val.split(" ")[0];
							pkv.setFormattedDateValue(dval.split("-")[2]+"/"+dval.split("-")[1]+"/"+dval.split("-")[0]);
						}
						line.getValues().add(pkv);
					}
					block.getLines().add(line);
				}
			*/
			
			//	constructing form
			block.setControls(new ArrayList<UIControlElement>());
			ProtogenDataEngine engine = new ProtogenDataEngine();
			for(CAttribute a : dto.getMtmEntity().getAttributes()){
				if(a.getDataReference().startsWith("pk_")|| (a.getDataReference().startsWith("fk_") && !a.isReference()))
					continue;
				if(a.isAutoValue())
					continue;
				if(!a.isReference()){
					if(a.getCAttributetype().getId() == 3){ //	Date
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(a.getAttribute());
						e.setCtrlDate(true);
						e.setControlValue("");
						block.getControls().add(e);
					} else {
						UIControlElement e = new UIControlElement();
						e.setAttribute(a);
						e.setControlID(a.getDataReference());
						e.setLabel(a.getAttribute());
						e.setControlValue("");
						block.getControls().add(e);
					}
				} else {
					UIControlElement element = new UIControlElement();
					
					ApplicationLoader dal = new ApplicationLoader();
					//CBusinessClass e = dal.getEntity(a.getDataReference().substring(3));
//					ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
//					List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3),(e.getUserRestrict()=='Y'),cache.getUser().getId());
					List<PairKVElement> listElements = engine.getDataKeys(a.getDataReference().substring(3), false, 0);
					
					element.setAttribute(a);
					element.setControlID(a.getDataReference());
					element.setLabel(a.getAttribute().replaceAll("ID ",""));
					element.setControlValue("");
					element.setListReference(listElements);
					element.setReference(true);
					block.getControls().add(element);
				}
				
			}
			
			results.add(block);
		}
		return results;
	}
	
	class GeneriumTypeAdapter<T> extends TypeAdapter<T> {
	    public T read(JsonReader reader) throws IOException {
	        return null;
	    }

	    public void write(JsonWriter writer, T obj) throws IOException {
	        if (obj == null) {
	            writer.nullValue();
	            return;
	        }
	        writer.value(obj.toString());
	    }
	}

	
}
