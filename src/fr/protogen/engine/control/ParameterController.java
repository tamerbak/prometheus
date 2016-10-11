package fr.protogen.engine.control;



import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.MAction;

@ManagedBean
@RequestScoped
public class ParameterController {

	private String sourceCode="";
	private MAction action;
	
	public ParameterController(){
		ApplicationLoader loader = new ApplicationLoader();  
		action = loader.getFirstaction();
		sourceCode = action.getCode();
		sourceCode = sourceCode.replaceAll("\n", "\r");
		sourceCode = sourceCode.replaceAll("\r\n", "\r");
		sourceCode = sourceCode.replaceAll("\r\r", "\r");
		sourceCode = sourceCode.replaceAll("\r\r", "\r");

	}
	
	public String updateAction(){
		
		action.setCode(sourceCode);
		
		ApplicationLoader loader = new ApplicationLoader();  
		loader.storeAction(action);
		
		return "protogen";
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
}
