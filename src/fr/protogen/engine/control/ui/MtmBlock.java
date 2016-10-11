package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.masterdata.model.CBusinessClass;

public class MtmBlock implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2768694258308049129L;
	private int entityID;
	private CBusinessClass entity=new CBusinessClass();
	private CBusinessClass mtmEntity=new CBusinessClass();
	private List<String> titles=new ArrayList<String>();
	private List<MtmLine> lines = new ArrayList<MtmLine>();
	private List<UIControlElement> controls=new ArrayList<UIControlElement>();
	private List<PairKVElement> keys = new ArrayList<PairKVElement>();
	private MtmDTO dto = new MtmDTO(); 
	private boolean visited;
		
	public CBusinessClass getEntity() {
		return entity;
	}
	public void setEntity(CBusinessClass entity) {
		this.entity = entity;
	}
	public List<MtmLine> getLines() {
		return lines;
	}
	public void setLines(List<MtmLine> lines) {
		this.lines = lines;
	}
	public int getEntityID() {
		return entityID;
	}
	public void setEntityID(int entityID) {
		this.entityID = entityID;
	}
	public List<String> getTitles() {
		return titles;
	}
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	public List<UIControlElement> getControls() {
		return controls;
	}
	public void setControls(List<UIControlElement> controls) {
		this.controls = controls;
	}
	public List<PairKVElement> getKeys() {
		return keys;
	}
	public void setKeys(List<PairKVElement> keys) {
		this.keys = keys;
	}
	public CBusinessClass getMtmEntity() {
		return mtmEntity;
	}
	public void setMtmEntity(CBusinessClass mtmEntity) {
		this.mtmEntity = mtmEntity;
	}
	public boolean isVisible(){
		return (lines!=null && lines.size()>0);
	}
	public String getWidthSize(){
		int width = 250*titles.size();
		return width+"px";
	}
	public int getCount(){
		return titles.size();
	}
	public MtmDTO getDto() {
		return dto;
	}
	public void setDto(MtmDTO dto) {
		this.dto = dto;
	}
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
}
