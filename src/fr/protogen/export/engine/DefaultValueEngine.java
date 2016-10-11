package fr.protogen.export.engine;

import java.util.Calendar;
import java.util.Date;

import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class DefaultValueEngine {

	public String getDefaultValue(String attributeName, String table) {
		String v = "";
		
		CAttribute attribute = null;
		ApplicationLoader dal = new ApplicationLoader();
		CBusinessClass entity = dal.getEntity(table);
		attribute = dal.getAttributeByName(attributeName, entity);
		
		if(attribute.getDefaultValue() == null || attribute.getDefaultValue().length()==0)
			return "";
		
		v = attribute.getDefaultValue();
		
		if(attribute.getDefaultValue().equals("DATE_JOUR"))
		{
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			v = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+
					" "+c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+"+00";
		}
		
		return v;
	}

}
