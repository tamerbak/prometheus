package fr.protogen.engine.control;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.itextpdf.xmp.impl.Base64;

import fr.protogen.callout.service.RemoteCalloutService;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class CalloutController implements Serializable {
	private String message;
	
	public CalloutController(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int calloutId = parseInt(params.get("idcallout"));
		Map<String, String> args = new HashMap<String, String>();
		for(String m : params.keySet()){
			
			if(m.startsWith("carg_"))
				args.put(m.substring(5), params.get(m));
			else
				args.put(m, Base64.encode(params.get(m)));
		}
		
		try{
			 this.message = RemoteCalloutService.getInstance().remoteCallout(args, calloutId);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
	}
	
	private int parseInt(String str){
		try{
			return Integer.parseInt(str);
		}catch(Exception e){
			return 0;
		}
	}

	/*
	 * GETTERS AND SETTERS
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
