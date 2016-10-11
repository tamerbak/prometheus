package fr.protogen.masterdata.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CUserMailConfig implements Serializable {
	private int id;
	private String imap;
	private String smtp;
	private String imapPort;
	private String login;
	private String password;
	private CoreUser user; 
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getImap() {
		return imap;
	}
	public void setImap(String imap) {
		this.imap = imap;
	}
	public String getSmtp() {
		return smtp;
	}
	public void setSmtp(String smtp) {
		this.smtp = smtp;
	}
	public String getImapPort() {
		return imapPort;
	}
	public void setImapPort(String imapPort) {
		this.imapPort = imapPort;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
	
	
}
