package fr.protogen.engine.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import fr.protogen.engine.utils.ApplicationCache;
import fr.protogen.engine.utils.ApplicationRepository;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CoreProfil;
import fr.protogen.masterdata.model.CoreRole;
import fr.protogen.masterdata.model.CoreUser;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class MyAdminControl implements Serializable {
	private List<CoreUser> users = new ArrayList<CoreUser>();
	private CoreUser selectedUser = new CoreUser();
	private List<CoreRole> roles = new ArrayList<CoreRole>();
	private String newPassword="";
	private ApplicationCache cache;
	

	@PostConstruct
	public void postLoad(){
		//	Load users
		ApplicationLoader dal = new ApplicationLoader(); 
		
		//	Load roles
		String skey = (String)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("USER_KEY");
		
		cache = ApplicationRepository.getInstance().getCache(skey);
		roles = dal.loadApplicationRoles(cache.getAppKey());
		
		//	Load users
		users = dal.loadApplicationUsers(cache.getAppKey(), new ArrayList<CoreProfil>());
		
		for(CoreUser u : users){
			for(CoreRole r : roles)
				if(r.getId()==u.getCoreRole().getId()){
					u.setCoreRole(r);
					break;
				}
		}
	}
	
	public void changePassword(){
		
	}
	
	public void saveUser(){
		
	}

	/*
	 * 	GETTERS AND SETTERS
	 */
	
	public List<CoreUser> getUsers() {
		return users;
	}

	public void setUsers(List<CoreUser> users) {
		this.users = users;
	}

	public CoreUser getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(CoreUser selectedUser) {
		this.selectedUser = selectedUser;
	}

	public List<CoreRole> getRoles() {
		return roles;
	}

	public void setRoles(List<CoreRole> roles) {
		this.roles = roles;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
