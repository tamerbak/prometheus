package fr.protogen.masterdata.model;

import java.io.Serializable;

/**
 * Represents Composition and Composition rule
 * @author JAKJOUD
 *
 */
@SuppressWarnings("serial")
public class CComposition implements Serializable {
	private int id;
	private CBusinessClass entity;
	private CAttribute ruleAttribute;
	private String compositionRule;
	
	public CAttribute getRuleAttribute() {
		return ruleAttribute;
	}
	public void setRuleAttribute(CAttribute ruleAttribute) {
		this.ruleAttribute = ruleAttribute;
	}
	public String getCompositionRule() {
		return compositionRule;
	}
	public void setCompositionRule(String compositionRule) {
		this.compositionRule = compositionRule;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
}
