package fr.protogen.masterdata.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.masterdata.DAO.LocalizationEngine;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CView;
import fr.protogen.masterdata.model.CViewPart;
import fr.protogen.masterdata.model.CoreUser;

public class ViewsService {
	public List<CView> loadWindowViews(int windowId){
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
		CoreUser u = cache.getUser();
		LocalizationEngine translator = new LocalizationEngine();
		
		
		List<CView> results = new ArrayList<CView>();
		String sql = "SELECT   c_view.id as view_id, c_view.title as view_title, c_view.view_type, c_view.window_id, "
				+ "c_view_part.id as part_id, c_view_part.titles, c_view_part.query, c_view_part.view_id "
				+ "FROM public.c_view, public.c_view_part "
				+ "WHERE c_view_part.view_id = c_view.id and window_id=?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, windowId);
			
			ResultSet rs = ps.executeQuery();
			boolean flag = false;
			CView view = new CView();
			
			while (rs.next()){
				flag = (view.getId() == rs.getInt(1));
				if(!flag){
					if(view.getId()>0 && view.getParts().size()>0){
						results.add(view);
					}
					view = new CView();
					view.setId(rs.getInt("view_id"));
					String title = translator.viewTranslate(rs.getString("view_title"), view.getId(), u.getLanguage());
					view.setTitle(title);
					view.setType(rs.getString("view_type").charAt(0));
				}
				
				CViewPart p = new CViewPart();
				p.setId(rs.getInt("part_id"));
				p.setTitles(rs.getString("titles").split(","));
				p.setDataRows(new ArrayList<List<String>>());
				p.setQuery(rs.getString("query"));
				view.getParts().add(p);
			}
			if(view.getId()>0 && view.getParts().size()>0){
				results.add(view);
			}
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		return results;
	}
	
	public CView loadData(CView v){
		
		for(CViewPart p : v.getParts()){
			try{
				Connection cnx = ProtogenConnection.getInstance().getConnection();
				PreparedStatement ps = cnx.prepareStatement(p.getQuery());
				System.out.println("VIEW QUERY\n\t"+p.getQuery());
				ResultSet rs = ps.executeQuery();
				p.setDataRows(new ArrayList<List<String>>());
				while(rs.next()){
					System.out.println("VAL 0 : "+rs.getObject(1));
					List<String> line = new ArrayList<String>();
					for(int i = 1 ; i <= p.getTitles().length ; i++){
						
						if(rs.getObject(i) != null)
							line.add(rs.getObject(i).toString());
						else
							line.add("");
					}
					p.getDataRows().add(line);
				}
			}catch(Exception exc){
				System.out.println("VIEW FAILED ON PART : "+p.getId());
				exc.printStackTrace();
			}
		}
		return v;
	}
}
