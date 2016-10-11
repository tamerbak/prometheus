package fr.protogen.engine.utils;

import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.SProcedure;

public class DTOProcessSession {
	private int id;
	private SProcedure process;
	private CoreUser user;
	private SAtom atom;
	private boolean voidFlag;
	
	public SProcedure getProcess() {
		return process;
	}
	public void setProcess(SProcedure process) {
		this.process = process;
	}
	public CoreUser getUser() {
		return user;
	}
	public void setUser(CoreUser user) {
		this.user = user;
	}
	public SAtom getAtom() {
		return atom;
	}
	public void setAtom(SAtom atom) {
		this.atom = atom;
	}
	public boolean isVoidFlag() {
		return voidFlag;
	}
	public void setVoidFlag(boolean voidFlag) {
		this.voidFlag = voidFlag;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	
}
