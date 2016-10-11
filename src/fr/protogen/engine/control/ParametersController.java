package fr.protogen.engine.control;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import fr.protogen.engine.control.process.ProcessScreenListener;
import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.masterdata.model.SAtom;

@SuppressWarnings("serial")
@ViewScoped
@ManagedBean
public class ParametersController implements Serializable{

	private SAtom window;
	
	@PostConstruct
	public void initialize(){
		
		
		boolean notinsession=(!FacesContext.getCurrentInstance().getExternalContext().getSessionMap().containsKey("USER_KEY")
				|| FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY")==null); 

		if(notinsession){
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		
		//	Get screen ID from arguments
		Map<String,Object> params = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();		
		window = (SAtom)params.get(ProtogenConstants.PARAMETER_ATOM);
		
	}

	
	public String next(){
		
		
		
		return "";
	}
	
	public SAtom getWindow() {
		return window;
	}

	public void setWindow(SAtom window) {
		this.window = window;
	}
}
