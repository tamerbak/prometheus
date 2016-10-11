package fr.protogen.masterdata.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import fr.protogen.dataload.ConstraintFactory;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CWindow;

public class MTMService {

	public List<MtmDTO> getMtm(CWindow window, CBusinessClass entity){
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationLoader dal = new ApplicationLoader();
		List<String> mtmTables = new ArrayList<String>();
		List<MtmDTO> dtos = new ArrayList<MtmDTO>();
		
		for(CAttribute a : window.getCAttributes()){
			if(!a.isMultiple() || mtmTables.contains(a.getEntity().getDataReference()))
				continue;
			
			MtmDTO dto = new MtmDTO();
			String tableReference = a.getDataReference().split("__")[0].substring(3);//a.getEntity().getDataReference();
			mtmTables.add(tableReference);
			List<CAttribute> attributes = dal.getEntityAttributes(tableReference);
			
			CBusinessClass e = attributes.get(0).getEntity();
			e.setAttributes(attributes);
			dto.setMtmEntity(e);
			dto.setMtmData(new ArrayList<Map<CAttribute,Object>>());
			Map<CAttribute, Object> map = new HashMap<CAttribute, Object>();
			
			for(CAttribute aa : attributes){
				if(aa.isReference()){
					
					String rrtable=aa.getDataReference().substring(3);
					CBusinessClass cee = dal.getEntity(rrtable);
//					ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
//					List<PairKVElement> references = engine.getDataKeys(rrtable,(cee.getUserRestrict()=='Y'),cache.getUser().getId());
					List<PairKVElement> references = engine.getDataKeys(rrtable, false, 0);

					Map<Integer, String> refmap = new HashMap<Integer, String>();
					for(PairKVElement kv : references){
						refmap.put(new Integer(kv.getKey()),kv.getValue());
					}
					map.put(aa, refmap);
				}
				else
					map.put(aa, null);
			}
			dto.getMtmData().add(map);
			dtos.add(dto);
			
		}
		
		
		return dtos;
		
	}
	
	public List<MtmDTO> getMtmFull(CBusinessClass entity, int id){
		ProtogenDataEngine engine = new ProtogenDataEngine();
		ApplicationLoader dal = new ApplicationLoader();
		List<CBusinessClass> mtmEntities = dal.getFullDependentEntities(entity.getDataReference());
		
		List<MtmDTO> dtos = new ArrayList<MtmDTO>();
		for(CBusinessClass mtmEntity : mtmEntities){
			mtmEntity = engine.populateEntity(mtmEntity);
						
			mtmEntity.setAttributes(dal.getEntityAttributes(mtmEntity.getDataReference()));
			ConstraintFactory factory = new ConstraintFactory();
			String constraint = factory.buildFKEqualityConstraint(entity,mtmEntity.getDataReference(), id);
			List<Map<CAttribute, Object>> mtmData = engine.getDataByConstraint(mtmEntity, constraint);
			
			for(Map<CAttribute, Object> m : mtmData){
				for(CAttribute a : m.keySet()){
					if(a.isReference()){
						
						String rrtable=a.getDataReference().substring(3);
						CBusinessClass cee = dal.getEntity(rrtable);
						ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY"));
						List<PairKVElement> list = engine.getDataKeys(rrtable,(cee.getUserRestrict()=='Y'),cache.getUser().getId());
						int i=0;
						if(m.get(a) != null)
							i = Integer.parseInt(m.get(a).toString());
						for(PairKVElement kv : list)
							if(kv.getKey().equals(""+i))
								m.put(a, kv.getValue());
					}
				}
			}
			
			MtmDTO dto = new MtmDTO();
			dto.setMtmEntity(mtmEntity);
			dto.setMtmData(mtmData);
			
			dtos.add(dto);
		}
		
		return dtos;
		
	}
}
