package fr.protogen.engine.control.ui;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class GDataRow implements Serializable{
	private int dbID;
	private List<GDatum> cells;

	public List<GDatum> getCells() {
		return cells;
	}

	public void setCells(List<GDatum> cells) {
		this.cells = cells;
	}
}
