package fr.protogen.masterdata.model;

public class CoreUserRole implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private CoreUser coreUser;
	private CoreRole coreRole;
	
	public CoreUserRole() {
	}

	public CoreUserRole(CoreUser coreUser, CoreRole coreRole) {
		this.coreUser = coreUser;
		this.coreRole = coreRole;
	}

	public CoreUser getCoreUser() {
		return this.coreUser;
	}

	public void setCoreUser(CoreUser coreUser) {
		this.coreUser = coreUser;
	}

	public CoreRole getCoreRole() {
		return this.coreRole;
	}

	public void setCoreRole(CoreRole coreRole) {
		this.coreRole = coreRole;
	}
}
