package fr.protogen.engine.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class UIControlsLine implements Serializable {
	public static final int COLS=7;
	
	private List<UIControlSingle> ctlines;
	private int index=0;
	
	public UIControlsLine() {
		ctlines = new ArrayList<UIControlSingle>();
	}
	
	public void addControl(UIControlElement e){
		if(index ==0)
			ctlines.add(new UIControlSingle());
		
		int i = ctlines.size()-1;
		ctlines.get(i).getControls().add(e);
		
		
		if(index>0)
			ctlines.get(i).getControls().get(ctlines.get(i).getControls().size()-1).setColspan(0);
		
		index++;
		
		
		if(index==COLS){
			index=0;
			e.setColspan(0);
		} /*else if(index==2)
			e.setColspan(3);
		else
			e.setColspan(5);*/
	}

	public List<UIControlSingle> getCtlines() {
		return ctlines;
	}

	public void setCtlines(List<UIControlSingle> ctlines) {
		this.ctlines = ctlines;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
