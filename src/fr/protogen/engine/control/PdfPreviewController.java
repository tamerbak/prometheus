package fr.protogen.engine.control;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.services.FileStoreService;

@ManagedBean
@RequestScoped
public class PdfPreviewController {
	private String pdfFile="";

	@PostConstruct
	public void energize(){
		FileStoreService service = new FileStoreService();
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		
		Integer bean = (Integer) map.get(ProtogenConstants.SELECTED_ROW);
		CBusinessClass entity = (CBusinessClass)map.get(ProtogenConstants.WINDOW_ENTITY);
		String fname = (String)map.get(ProtogenConstants.SELECTED_FILE_PREVIEW);
		
		pdfFile = service.fullPathFile(entity, bean.intValue(), fname);
	}
	
	public String getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(String pdfFile) {
		this.pdfFile = pdfFile;
	}
}
