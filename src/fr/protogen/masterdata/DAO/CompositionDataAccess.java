package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.DualListModel;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CComposedBean;
import fr.protogen.masterdata.model.CComposingeBean;
import fr.protogen.masterdata.model.CComposition;

public class CompositionDataAccess {
	
	/**
	 * Load composition structure for entity
	 * @param entity
	 * @return
	 */
	public CComposition loadComposition(CBusinessClass entity){
		CComposition result = new CComposition();
		result.setId(0);
		
		String sql = "SELECT id FROM c_composition WHERE entity=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, entity.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				result.setEntity(entity);
				result.setId(rs.getInt(1));
			}
			rs.close();
			ps.close();
			
			if(result.getId() == 0)
				return result;
			
			sql = "select attribute, criterion from c_composition_rule where "
					+ " composition=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, result.getId());
			rs = ps.executeQuery();
			
			if(rs.next()){
				CAttribute a = new CAttribute();
				a.setId(rs.getInt(1));
				result.setRuleAttribute(a);
				result.setCompositionRule(rs.getString(2));
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return result;
	}
	
	public List<PairKVElement> loadComposableBeans(CComposition c){
		List<PairKVElement> results = new ArrayList<PairKVElement>();
		
		ProtogenDataEngine pde = new ProtogenDataEngine();
		List<String> cs = new ArrayList<String>();
		cs = loadConstraint(c);
		if(c.getCompositionRule() != null && c.getCompositionRule().length()>0)
			results = pde.getDataKeys(c.getEntity().getDataReference(), false, 0, cs);
		else
			results = pde.getDataKeys(c.getEntity().getDataReference(), false, 0);
		return results;
	}

	public void saveComposition(CComposition composition, int dbID,
			DualListModel<String> toCompose, List<PairKVElement> elts) {
		//	Get prior composition
		int id = 0;
		String sql = "select id from c_composed_bean where bean = ? and composition = ?";
		
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1,dbID);
			ps.setInt(2, composition.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				id = rs.getInt(1);
			rs.close();
			ps.close();
			
			sql = "delete from c_composing_bean where composed=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, id);
			ps.execute();
			ps.close();
			
			sql = "insert into c_composed_bean (bean, composition) "
					+ " values (?,?)";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, dbID);
			ps.setInt(2, composition.getId());
			ps.execute();
			ps.close();
			sql = "select nextval('c_composed_bean_seq')";
			ps = cnx.prepareStatement(sql);
			rs = ps.executeQuery();
			int cid = 0;
			if(rs.next())
				cid = rs.getInt(1)-1;
			rs.close();
			ps.close();
			
			sql = "insert into c_composing_bean (composed, bean) "
					+ " values (?,?)";
			for(String str : toCompose.getTarget()){
				int compoID = 0;
				for(PairKVElement e : elts)
					if(e.getValue().equals(str)){
						compoID = Integer.parseInt(e.getKey());
						break;
					}
				if(compoID == 0)
					continue;
				
				ps = cnx.prepareStatement(sql);
				ps.setInt(1, cid);
				ps.setInt(2, compoID);
				ps.execute();
				ps.close();
			}
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public CComposedBean loadCompnents(int dbID, CComposition composition, List<PairKVElement> elts) {
		CComposedBean result = new CComposedBean();
		result.setId(0);
		result.setBeanId(dbID);
		result.setComposition(new ArrayList<CComposingeBean>());
		
		String sql = "select id from c_composed_bean where bean=? and composition=?";
		try{
			Connection cnx = ProtogenConnection.getInstance().getConnection();
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1,dbID);
			ps.setInt(2, composition.getId());
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				result.setId(rs.getInt(1));
			}
			
			rs.close();
			ps.close();
			
			sql = "select bean from c_composing_bean where composed=?";
			ps = cnx.prepareStatement(sql);
			ps.setInt(1, result.getId());
			rs = ps.executeQuery();
			while(rs.next()){
				CComposingeBean b = new CComposingeBean();
				b.setId(rs.getInt(1));
				for(PairKVElement e : elts){
					if(e.getKey().equals(b.getId()+"")){
						b.setLibelle(e.getValue());
						break;
					}
				}
				result.getComposition().add(b);
			}
			
			rs.close();
			ps.close();
						
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		return result;
	}
	
	/*
	 * UTILS
	 */
	private List<String> loadConstraint(CComposition c) {
		if(c.getCompositionRule() == null || c.getCompositionRule().length()==0){
			return null;
		}
		
		List<String> results = new ArrayList<String>();
		
		results.add(c.getRuleAttribute().getDataReference());
		results.add("");
		results.add(c.getCompositionRule());
			
		return results;
	}

	
}
