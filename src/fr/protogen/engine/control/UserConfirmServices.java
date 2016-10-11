package fr.protogen.engine.control;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.ProtogenParameters;
import fr.protogen.masterdata.DAO.UserDAOImpl;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped
public class UserConfirmServices implements Serializable {

	private boolean validform;
	private String message;
	private String email;
	private String activateKey;
	
	
	@PostConstruct
	public void energize(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		validform = false;
		if(params.containsKey("uid") && params.get("uid")!=null){
			String uid = params.get("uid").split("--")[1];
			String key = params.get("uid").split("--")[0];
			UserDAOImpl dao = new UserDAOImpl();

			boolean flag =dao.activate(Integer.parseInt(uid),key);
			if(flag)
				message="Votre compte est activé <a href=\""+ProtogenParameters.APPLICATION+"/prometheus/login.xhtml\" style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\">connectez-vous</a>";			
			else
				message="Ce lien de validation n'est pas valide, nous vous invitons à <a href=\"mailto:admin@gpsdelagestion.com\" style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\" >nous contacter</a>";
		} else if(params.containsKey("pwdcode") && params.get("pwdcode")!=null){
			UserDAOImpl dao = new UserDAOImpl();
			
			validform =dao.lookup(params.get("pwdcode"));
			activateKey=params.get("pwdcode");
		} else {
			validform=true;
		}
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	public void doActivate(){
		UserDAOImpl dao = new UserDAOImpl();
		
		boolean flag =dao.activate(email,activateKey);
		if(flag)
			message="Votre compte a été activé avec succès ";
		else
			message="Ce code n'est pas valide, nous vous invitons à <a style=\"font: 19px/20px verdana,sans-serif;color: #2E6E9E;margin: 0 0 4px 0;\" href=\"mailto:admin@gpsdelagestion.com\">nous contacter</a>";
		validform=false;
	}

	public boolean isValidform() {
		return validform;
	}

	public void setValidform(boolean validform) {
		this.validform = validform;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getActivateKey() {
		return activateKey;
	}

	public void setActivateKey(String activateKey) {
		this.activateKey = activateKey;
	}
}
