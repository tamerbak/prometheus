package fr.protogen.engine.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.SResource;

@ManagedBean
@RequestScoped
public class ResourcesControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3881708739613571826L;
	private SResource resource;
	private StreamedContent file;
	
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
		resource = (SResource)params.get(ProtogenConstants.RESOURCE_INOUT);
		
		if(resource.getContent() == null){
			ApplicationLoader dal = new ApplicationLoader();
			resource.setContent(dal.loadResourceContent(resource.getId()));
		}
		
		String filename = resource.getFileName();
		String mime = "";
		if(filename.endsWith("pdf"))
			mime = "application/pdf";
		if(filename.endsWith("doc") )
			mime = "application/msword";
		if(filename.endsWith("docx") )
			mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		
		file = new DefaultStreamedContent(resource.getContent(),mime,filename);
	}
	
	
	@SuppressWarnings("unused")
   	public void handleFileUpload(FileUploadEvent event) {
       	try {
       		FacesContext fc = FacesContext.getCurrentInstance();
       	    ExternalContext ec = fc.getExternalContext();
   			InputStream is = event.getFile().getInputstream();
   			int length = (int)event.getFile().getSize();
			ApplicationLoader dal = new ApplicationLoader();
			dal.saveContent(resource.getId(),is,length);
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
       }
    
	
	public SResource getResource() {
		return resource;
	}
	public void setResource(SResource resource) {
		this.resource = resource;
	}
	public StreamedContent getFile() {
		return file;
	}
	public void setFile(StreamedContent file) {
		this.file = file;
	}
	
	
}
