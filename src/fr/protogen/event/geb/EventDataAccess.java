package fr.protogen.event.geb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.protogen.dataload.DataDefinitionOperation;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.CalendarUtils;
import fr.protogen.engine.utils.EntityDTO;
import fr.protogen.engine.utils.StringFormat;
import fr.protogen.event.geb.EventModel.EventType;
import fr.protogen.event.geb.EventModel.GDataEvent;
import fr.protogen.event.geb.EventModel.GEvent;
import fr.protogen.event.geb.EventModel.GEventInstance;
import fr.protogen.event.geb.EventModel.PEAMail;
import fr.protogen.event.geb.EventModel.PEASms;
import fr.protogen.event.geb.EventModel.PEAWindow;
import fr.protogen.event.geb.EventModel.PostEventAction;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.DAO.UserDAOImpl;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CWindow;
import fr.protogen.masterdata.model.CoreUser;

public class EventDataAccess {

	public List<GEvent> loadDataEvents(CoreUser user)
	{
		List<GEvent> evts = new ArrayList<GEvent>();
		
		String sql = "select id, titre, contenu, entite, operation, bean, auto_event, differe,"
				+ "date_lancement, periode, nb_relance "
				+ "from e_event where destinataire=? and type=1";
		System.out.println("FETCH ALL EVENT DEFINITION OF TYPE DATA\n\t"+sql+"--destinataire="+user.getId());
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				GDataEvent e = new GDataEvent();
				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setType(EventType.DATA_ACCESS);
				e.setContenu(rs.getString(3));
				e.setEntity(new CBusinessClass());
				e.getEntity().setId(rs.getInt(4));
				e.setOperation(getOperation(rs.getInt(5)));
				
				e.setBeanId(rs.getInt(6));
				e.setAutoEvent(rs.getString(7).equals("Y"));
				e.setDiffere(rs.getString(8).equals("Y"));
				e.setDateLancement(rs.getDate(9));
				e.setPeriode(rs.getInt(10));
				e.setNbRelances(rs.getInt(11));
				
				e.setDestinataire(user);
				
				evts.add(e);
			}
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return evts;
	}

	public List<GEventInstance> fetch(GEvent e) {
		
		List<GEventInstance> results = new ArrayList<GEventInstance>();
		
		String sql = "select id, creation, content, row_id, next_exec, nb_exec from e_event_instance "
				+ "where event=? and state='N'";
		System.out.println("FETCH ALL EVENT INSTANCES \n\t"+sql+"--Event="+e.getId());
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, e.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				GEventInstance i = new GEventInstance();
				i.setEvent(e);
				i.setState(false);
				i.setId(rs.getInt(1));
				i.setCreation(rs.getDate(2));
				i.setContent(rs.getString(3));
				i.setRowId(rs.getInt(4));
				i.setNextExecution(rs.getDate(5));
				i.setRelancesRestantes(rs.getInt(6));
				
				if(e.isDiffere()){
					if(i.getNextExecution().compareTo(new java.util.Date())>0)
						continue;
				}
				
				results.add(i);
			}
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return results;
	}
	
	public GEventInstance persist(GEventInstance i) {
		String sql = "insert into e_event_instance (event, creation, content, state, row_id, next_exec, nb_exec) "
				+ "values (?,?,?,?,?,?,?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, i.getEvent().getId());
			ps.setDate(2, new Date(i.getCreation().getTime()));
			ps.setString(3, i.getContent());
			ps.setString(4, "N");
			ps.setInt(5, i.getRowId());
			if(i.getEvent().getDateLancement() == null){
				java.util.Date d = new java.util.Date();
				d = CalendarUtils.addDays(d, i.getEvent().getPeriode());
				i.getEvent().setDateLancement(d);
			}
			
			i.setRelancesRestantes(i.getEvent().getNbRelances());
			i.setNextExecution(i.getEvent().getDateLancement());
			
			ps.setDate(6, new Date(i.getEvent().getDateLancement().getTime()));
			ps.setInt(7, i.getRelancesRestantes());
			ps.execute();
			ps.close();
			
			sql = "select nextval('e_event_instance_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				i.setId(rs.getInt(1));
			}
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return i;
	}

	public PostEventAction mark(GEventInstance evt) {
		PostEventAction act = new PostEventAction();
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps;
			String sql;
			if(evt.getRelancesRestantes() == 0){
				sql = "update e_event_instance set state='Y' where id=? ";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, evt.getId());
				ps.execute();
				ps.close();
			} else {
				
				java.util.Date d = evt.getNextExecution();
				d = CalendarUtils.addDays(d, evt.getEvent().getPeriode());
				evt.setNextExecution(d);
				evt.setRelancesRestantes(evt.getRelancesRestantes()-1);
				
				sql = "update e_event_instance set nb_exec=?, next_exec=? where id=? ";
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, evt.getRelancesRestantes());
				ps.setDate(2, new Date(evt.getNextExecution().getTime()));
				ps.setInt(3, evt.getId());
				ps.execute();
				ps.close();
			}
			sql = "select id, type from e_postevent_action "
					+ " where event=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, evt.getEvent().getId());
			boolean flag = false;
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				flag = true;
				act.setId(rs.getInt(1));
				act.setType(loadPostaction(rs.getInt(2)));
				act.setEvent(evt.getEvent());
			}
			
			rs.close();
			ps.close();
			if(!flag)
				return act;
			

			if(act.getType() == PEAType.SCREEN){
				return loadScreenPEAS(act);
			}
			if(act.getType() == PEAType.SMS){
				return loadSMSPEAS(act);
			}
			if(act.getType() == PEAType.MAIL){
				return loadMailPEAS(act);
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return act;
	}
	
	public void unmark(GEventInstance evt) {
		String sql = "update e_event_instance set state='N' where id=? ";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, evt.getId());
			ps.execute();
			ps.close();
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public List<GDataEvent> loadTableDataEvents(CBusinessClass entity, DataDefinitionOperation operation) {
		List<GDataEvent> results = new ArrayList<GDataEvent>();
		
		String sql = "SELECT id, titre, contenu, destinataire, auto_event, differe,"
				+ "date_lancement, periode, nb_relance from e_event "
				+ " where type = 1 and entite=? and operation=? and bean=0";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, entity.getId());
			int o = parseOperation(operation);
			ps.setInt(2, o);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				GDataEvent e = new GDataEvent();
				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setContenu(rs.getString(3));
				e.setDestinataire(new CoreUser());
				e.getDestinataire().setId(rs.getInt(4));
				e.setAutoEvent(rs.getString(5).equals("Y"));
				e.setBeanId(0);
				e.setType(EventType.DATA_ACCESS);
				e.setEntity(entity);
				e.setOperation(operation);
				e.setDiffere(rs.getString(6).equals("Y"));
				e.setDateLancement(rs.getDate(7));
				e.setPeriode(rs.getInt(8));
				e.setNbRelances(rs.getInt(9));
				results.add(e);
			}
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}

	public List<GDataEvent> loadRowDataEvents(DataDefinitionOperation operation) {
		List<GDataEvent> results = new ArrayList<GDataEvent>();
		
		String sql = "SELECT id, titre, contenu, destinataire, entite, bean, auto_event, differe,"
				+ "date_lancement, periode, nb_relance from e_event "
				+ " where type = 1 and operation=? and bean>0";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			int o = parseOperation(operation);
			ps.setInt(1, o);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				GDataEvent e = new GDataEvent();
				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setContenu(rs.getString(3));
				e.setDestinataire(new CoreUser());
				e.getDestinataire().setId(rs.getInt(4));
				e.setBeanId(rs.getInt(6));
				e.setType(EventType.DATA_ACCESS);
				e.setEntity(new CBusinessClass());
				e.getEntity().setId(rs.getInt(5));
				e.setBeanId(rs.getInt(6));
				e.setAutoEvent(rs.getString(7).equals("Y"));
				e.setOperation(operation);
				
				results.add(e);
			}
			
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}
	
	public GEvent saveEvent(GEvent e) {
		return saveDataEvent(e);
	}
	
	public GEvent saveDataEvent(GEvent evt) {
		/*
		 * SAVE EVENT
		 */
		
		String sql = "insert into e_event "
				+ " (titre,type,contenu,destinataire,entite,operation,bean, auto_event, differe,"
				+ "date_lancement, periode, nb_relance) "
				+ " values (?,?,?,?,?,?,?, ?, ?,"
				+ "?, ?, ?) ";
		
		GDataEvent e = (GDataEvent)evt;
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setString(1, e.getTitle());
			ps.setInt(2, eventType2int(e.getType()));
			ps.setString(3, e.getContenu());
			ps.setInt(4, e.getDestinataire().getId());
			ps.setInt(5, e.getEntity().getId());
			ps.setInt(6,parseOperation(e.getOperation()));
			ps.setInt(7, e.getBeanId());
			ps.setString(8, e.isAutoEvent()?"Y":"Y");
			
			ps.setString(9, e.isDiffere()?"Y":"N");
			
			if(e.getDateLancement() == null)
				e.setDateLancement(new java.util.Date());
			
			ps.setDate(10, new Date(e.getDateLancement().getTime()));
			ps.setInt(11, e.getPeriode());
			ps.setInt(12, e.getNbRelances());
			
			ps.execute();
			ps.close();
			
			sql = "select nextval('e_event_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			int ide = 0;
			if(rs.next())
				ide = rs.getInt(1)-1;
			e.setId(ide);
			rs.close();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return e;
	}
	
	/*
	 * 	PERSIST POST EVENT ACTION
	 */
	
	public void persistScreenPEA(PEAWindow a) {
		String sql = "insert into e_postevent_action (event, type) values (?,?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1,a.getEvent().getId());
			ps.setInt(2, peaType2int(a.getType()));
			ps.execute();
			ps.close();
			sql = "select nextval('e_postevent_action_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				a.setId(rs.getInt(1)-1);
			}
			rs.close();
			ps.close();
			
			
			
			sql = "insert into e_pea_window (action, screen, details) values (?,?,?)";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, a.getId());
			ps.setInt(2, a.getWindow().getId());
			ps.setString(3, a.isModeDetails()?"Y":"N");
			
			ps.execute();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	public void persistSMSPEA(PEASms a) {
		String sql = "insert into e_postevent_action (event, type) values (?,?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1,a.getEvent().getId());
			ps.setInt(2, peaType2int(a.getType()));
			ps.execute();
			ps.close();
			sql = "select nextval('e_postevent_action_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				a.setId(rs.getInt(1)-1);
			}
			rs.close();
			ps.close();
			
			
			sql = "insert into e_pea_sms (action, sms_subject, sms_text_pattern) values (?,?,?)";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, a.getId());
			ps.setString(2, a.getSubject());
			ps.setString(3, a.getText());
			
			ps.execute();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}

	public void persistMailPEA(PEAMail a) {
		String sql = "insert into e_postevent_action (event, type) values (?,?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1,a.getEvent().getId());
			ps.setInt(2, peaType2int(a.getType()));
			ps.execute();
			ps.close();
			sql = "select nextval('e_postevent_action_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				a.setId(rs.getInt(1)-1);
			}
			rs.close();
			ps.close();
			
			sql = "insert into e_pea_email (action, email_subject, email_text_pattern) values (?,?,?)";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, a.getId());
			ps.setString(2, a.getSubject());
			ps.setString(3, a.getMessage());
			
			ps.execute();
			ps.close();
			
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
	
	public void persistDestinat(CBusinessClass chosen, GEvent evenement) {
		if (chosen.getId() == 0)
			return;
		
		String sql = "insert into e_event_dest (event, destinataire) values (?,?)";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, evenement.getId());
			ps.setInt(2, chosen.getId());
			ps.execute();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * DESTINATAIRES
	 */
	public String getDestinataireTel(GEventInstance i) {
		UserDAOImpl dao = new UserDAOImpl();
		CoreUser u = dao.getUserByID(i.getEvent().getDestinataire().getId());
		
		int rowId = i.getRowId();
		if(rowId == 0){
			return u.getTel();
		}
		
		int idDestEnt = 0;
		
		String sql = "select destinataire from e_event_dest where event=?";
		try {
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, i.getEvent().getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				idDestEnt = rs.getInt(1);
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(idDestEnt == 0){
			return u.getTel();
		}
		
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		GDataEvent dev = (GDataEvent)i.getEvent();
		if(idDestEnt == dev.getEntity().getId()){
			EntityDTO en = pde.getDatumByID(dev.getEntity().getDataReference(), rowId);
			for(CAttribute a : en.getValues().keySet()){
				if(a.getDataReference().startsWith("tel")){
					String tel = en.getValues().get(a);
					return tel;
				}
			}
		}
		
		CBusinessClass c = new CBusinessClass();
		c.setId(idDestEnt);
		ApplicationLoader dal = new ApplicationLoader();
		c = dal.getEntityById(idDestEnt);
		
		EntityDTO en = pde.getDatumByID(c.getDataReference(), rowId);
		for(CAttribute a : en.getValues().keySet()){
			if(a.getDataReference().startsWith("tel")){
				
				String tel = en.getValues().get(a);
				tel = StringFormat.getInstance().formatPhone(tel);
				
				return tel;
			}
		}
		
		return u.getTel();
	}
	
	public String getDestinataireEmail(GEventInstance i) {
		UserDAOImpl dao = new UserDAOImpl();
		CoreUser u = dao.getUserByID(i.getEvent().getDestinataire().getId());
		
		int rowId = i.getRowId();
		if(rowId == 0){
			return u.getEmail();
		}
		
		int idDestEnt = 0;
		
		String sql = "select destinataire from e_event_dest where event=?";
		try {
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, i.getEvent().getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				idDestEnt = rs.getInt(1);
			rs.close();
			ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			ProtogenDataEngine pde = new ProtogenDataEngine();
			GDataEvent dev = (GDataEvent)i.getEvent();
			if(idDestEnt == dev.getEntity().getId()){
				EntityDTO en = pde.getDatumByID(dev.getEntity().getDataReference(), rowId);
				if(en == null || en.getValues() == null)
					return u.getEmail();
				for(CAttribute a : en.getValues().keySet()){
					if(a.getDataReference().startsWith("email") || a.getDataReference().startsWith("courrier_electronique")){
						String tel = en.getValues().get(a);
						return tel;
					}
				}
			}
		
			CBusinessClass c = new CBusinessClass();
			c.setId(idDestEnt);
			ApplicationLoader dal = new ApplicationLoader();
			c = dal.getEntityById(idDestEnt);
		
			EntityDTO en = pde.getDatumByID(c.getDataReference(), rowId);
			for(CAttribute a : en.getValues().keySet()){
				if(a.getDataReference().startsWith("email") || a.getDataReference().startsWith("courrier_electronique")){
					
					String tel = en.getValues().get(a);
					
					return tel;
				}
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return u.getEmail();
	}
	/*
	 * UTILS
	 */
	
	private int eventType2int(EventType t){
		if(t == EventType.DATA_ACCESS)
			return 1;
		if(t == EventType.CALENDAR)
			return 2;
		if(t == EventType.EXTERN)
			return 3;
		if(t == EventType.WEB_SERVICE)
			return 4;
		return 1;
	}
	
	private DataDefinitionOperation getOperation(int o) {
		switch(o){
			case 1: return DataDefinitionOperation.INSERT;
			case 2: return DataDefinitionOperation.UPDATE;
			default: return DataDefinitionOperation.DELETE;
		}
	}

	private int parseOperation(DataDefinitionOperation operation) {
		if(operation == DataDefinitionOperation.INSERT)
			return 1;
		if(operation == DataDefinitionOperation.UPDATE)
			return 2;
		
		return 3;
	}

	private PEAType loadPostaction(int idtype) {
		
		switch(idtype){
		case 1 : return PEAType.SCREEN;
		case 2 : return PEAType.SMS;
		case 3 : return PEAType.MAIL;
		}
		
		return PEAType.SCREEN;
	}

	private int peaType2int(PEAType t){
		if(t== PEAType.SCREEN)
			return 1;
		if(t== PEAType.SMS)
			return 2;
		if(t== PEAType.MAIL)
			return 3;
		return 1;
	}
	
	private PostEventAction loadScreenPEAS(PostEventAction act) {
		PEAWindow a = new PEAWindow();
		a.setEvent(act.getEvent());
		a.setType(act.getType());
		
		String sql = "select screen, details from e_pea_window where action=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, act.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				a.setWindow(new CWindow());
				a.getWindow().setId(rs.getInt(1));
				a.setModeDetails(rs.getString(2).equals("Y"));
			}
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return a;
	}
	
	private PostEventAction loadSMSPEAS(PostEventAction act) {
		PEASms a = new PEASms();
		a.setEvent(act.getEvent());
		a.setType(act.getType());
		
		String sql = "select sms_subject, sms_text_pattern from e_pea_sms where action=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, act.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				a.setSubject(rs.getString(1));
				a.setText(rs.getString(2));
			}
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return a;
	}
	
	
	private PostEventAction loadMailPEAS(PostEventAction act) {
		PEAMail a = new PEAMail();
		a.setEvent(act.getEvent());
		a.setType(act.getType());
		
		String sql = "select email_subject, email_text_pattern from e_pea_email where action=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, act.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				a.setSubject(rs.getString(1));
				a.setMessage(rs.getString(2));
			}
			
			rs.close();
			ps.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return a;
	}

	

	
	
}
