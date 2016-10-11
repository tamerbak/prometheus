package fr.protogen.engine.control.ui;

import java.util.List;

import fr.protogen.engine.utils.PairKVElement;

public class UIMenu {

	private int itemId;
	private String supermenu;
	private List<PairKVElement> submenus;
	private int count;
	
	
	public String getSupermenu() {
		return supermenu;
	}
	public void setSupermenu(String supermenu) {
		this.supermenu = supermenu;
	}
	public List<PairKVElement> getSubmenus() {
		return submenus;
	}
	public void setSubmenus(List<PairKVElement> submenus) {
		this.submenus = submenus;
	}
	public int getCount() {
		if(submenus == null)
			return 0;
		else return submenus.size();
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	} 
}
