package fr.protogen.engine.control.ui;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;

public class AttributeConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
			throws ConverterException {
		ApplicationLoader dal = new ApplicationLoader();
		CAttribute a = dal.loadAttributeById(Integer.parseInt(arg2));
		return a;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
			throws ConverterException {
		// TODO Auto-generated method stub
		CAttribute a = (CAttribute)arg2;
		
		return a.getId()+"";
	}

}
