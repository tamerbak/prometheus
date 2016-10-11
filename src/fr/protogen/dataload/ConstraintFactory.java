package fr.protogen.dataload;

import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CParameterMetamodel;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.GParametersInstance;
import fr.protogen.masterdata.model.GParametersPackage;

public class ConstraintFactory {

	public String buildFKEqualityConstraint(CBusinessClass entity, String mtm, int id){
		return "fk_"+mtm+"__"+entity.getDataReference()+"='"+id+"'";
		
	}
	
	public String viewOrganizationConstraint(CoreUser user, String entity, boolean mainEntity, boolean parameterInstance){
		
		if(parameterInstance)
			return "";

		ApplicationLoader dal = new ApplicationLoader();
		CBusinessClass ent = dal.getEntity(entity);
		if(dal.checkForIndependantData(ent))
			return "";
		
		ApplicationCache cache = ApplicationRepository.getInstance().getCache((String)FacesContext
				.getCurrentInstance()
				.getExternalContext().getSessionMap().get("USER_KEY")); 
		
		for(GParametersInstance p : cache.getParameterPackages()){
			if(p.getModelPackage().getEntity().getDataReference().equals(entity))
				return "";
		}
		
		String constraint="";
				
		Boolean userbound= (Boolean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_BOUND");
		if(userbound){
			int ident = user.getCoreRole().getBoundEntity();
			int idinstance = user.getBoundEntity();
			
			ProtogenDataEngine pde = new ProtogenDataEngine();
			CBusinessClass bc = pde.getEntityById(ident);
			CBusinessClass reference = dal.getEntity(entity);
			
			if(reference.getDataReference().equals(bc.getDataReference()))
				constraint = "pk_";
			else{
				for(CAttribute a : reference.getAttributes())
					if(a.getDataReference().equals("fk_"+bc.getDataReference())){
						constraint = "fk_";
						break;
					}
			}
			
			if(constraint.length()>0){
				constraint = constraint+bc.getDataReference()+"="+idinstance;
				
				return constraint;
			}
		}
		
		
		
		int idorg = user.getOriginalOrganization().getIdBean();
		
		if(user.getOriginalOrganization().getRepresentativeEntity().getId() == 0)
			return "";
		
		if(ent.getId() == user.getOriginalOrganization().getRepresentativeEntity().getId())
			constraint = "("+ent.getDataReference()+".pk_"+ent.getDataReference()+"="+idorg+")";
		else
			constraint="("+ent.getDataReference()+".protogen_user_id="+idorg + ")";		//	organization data
		
		
		return constraint;
	}
}
