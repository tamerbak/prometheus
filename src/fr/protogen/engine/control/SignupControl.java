package fr.protogen.engine.control;

import javax.annotation.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class SignupControl {
	private String email;
	private String password;
	private String adress;
	private String tel;
	private int roleid;
	
	
	public String signup(){
		return "";
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getAdress() {
		return adress;
	}


	public void setAdress(String adress) {
		this.adress = adress;
	}


	public String getTel() {
		return tel;
	}


	public void setTel(String tel) {
		this.tel = tel;
	}


	public int getRoleid() {
		return roleid;
	}


	public void setRoleid(int roleid) {
		this.roleid = roleid;
	}
	
}
