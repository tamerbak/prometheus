package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class GOrganization implements Serializable {

	private int id;
	private int idBean;
	private List<GOrganization> children;
	private GOrganization parent;
	private CBusinessClass representativeEntity;
	private String name;
	private int orgId;
	private GOrganizationRole role;
	private GStructureTemplate template;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIdBean() {
		return idBean;
	}
	public void setIdBean(int idBean) {
		this.idBean = idBean;
	}
	public List<GOrganization> getChildren() {
		return children;
	}
	public void setChildren(List<GOrganization> children) {
		this.children = children;
	}
	public CBusinessClass getRepresentativeEntity() {
		if(representativeEntity==null)
			representativeEntity = new CBusinessClass();
		return representativeEntity;
	}
	public void setRepresentativeEntity(CBusinessClass representativeEntity) {
		this.representativeEntity = representativeEntity;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public GOrganization getParent() {
		return parent;
	}
	public void setParent(GOrganization parent) {
		this.parent = parent;
	}
	public int getOrgId() {
		return orgId;
	}
	public void setOrgId(int orgId) {
		this.orgId = orgId;
	}
	public GOrganizationRole getRole() {
		return role;
	}
	public void setRole(GOrganizationRole role) {
		this.role = role;
	}
	public GStructureTemplate getTemplate() {
		return template;
	}
	public void setTemplate(GStructureTemplate template) {
		this.template = template;
	}
}
