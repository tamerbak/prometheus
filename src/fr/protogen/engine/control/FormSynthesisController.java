package fr.protogen.engine.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import fr.protogen.engine.control.ui.MtmBlock;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.engine.utils.UIControlElement;

@ManagedBean
@ViewScoped
public class FormSynthesisController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4277788911006671840L;

	private String windowTitle;
	private List<UIControlElement> controls;
	private List<MtmBlock> dtos;
	
	@SuppressWarnings("unchecked")
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
		
		
		
		Map<String,Object> params =FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		windowTitle = (String)params.get(ProtogenConstants.FORM_WINDOW);
		controls = (List<UIControlElement>)params.get(ProtogenConstants.FORM_CONTROLS);
		Map<String, Integer> autovalues = (Map<String, Integer>)params.get(ProtogenConstants.FORM_AUTOVALUES);
		
		for(String k : autovalues.keySet()){
			UIControlElement element = new UIControlElement();
			element.setControlID(k);
			element.setControlValue(autovalues.get(k).intValue()+"");
			controls.add(element);
		}
		
		setDtos((List<MtmBlock>)params.get(ProtogenConstants.FORM_DTOS));
	}

	public String getWindowTitle() {
		return windowTitle;
	}

	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public List<UIControlElement> getControls() {
		return controls;
	}

	public void setControls(List<UIControlElement> controls) {
		this.controls = controls;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<MtmBlock> getDtos() {
		return dtos;
	}

	public void setDtos(List<MtmBlock> dtos) {
		this.dtos = dtos;
	}
	
}
