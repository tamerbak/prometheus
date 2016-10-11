package fr.protogen.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

@SuppressWarnings("serial")
public class MtmDTO implements Serializable {
	private CBusinessClass mtmEntity;
	private List<Map<CAttribute, Object>> mtmData;
	
	public CBusinessClass getMtmEntity() {
		return mtmEntity;
	}
	public void setMtmEntity(CBusinessClass mtmEntity) {
		this.mtmEntity = mtmEntity;
	}
	public List<Map<CAttribute, Object>> getMtmData() {
		return mtmData;
	}
	public void setMtmData(List<Map<CAttribute, Object>> mtmData) {
		this.mtmData = mtmData;
	}
	
	
}
