package fr.protogen.masterdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.PieChartModel;

import fr.protogen.engine.utils.PairKVElement;

public class SWidget implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private String label;
	private String lvalues;
	private char type;
	private String query;
	private PieChartModel model;
	private CartesianChartModel lineModel;
	private List<String> dataCaptions;
	private List<List<String>> dataTable;
	private List<PairKVElement> pieData = new ArrayList<PairKVElement>();
	private double max;
	private Boolean toShow;
	private Boolean toDel;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public char getType() {
		return type;
	}
	public void setType(char type) {
		this.type = type;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public PieChartModel getModel() {
		return model;
	}
	public void setModel(PieChartModel model) {
		this.model = model;
	}
	public List<List<String>> getDataTable() {
		return dataTable;
	}
	public void setDataTable(List<List<String>> dataTable) {
		this.dataTable = dataTable;
	}
	public List<String> getDataCaptions() {
		return dataCaptions;
	}
	public void setDataCaptions(List<String> dataCaptions) {
		this.dataCaptions = dataCaptions;
	}
	public String getLvalues() {
		return lvalues;
	}
	public void setLvalues(String lvalues) {
		this.lvalues = lvalues;
	}
	public CartesianChartModel getLineModel() {
		return lineModel;
	}
	public void setLineModel(CartesianChartModel lineModel) {
		this.lineModel = lineModel;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public List<PairKVElement> getPieData() {
		return pieData;
	}
	public void setPieData(List<PairKVElement> pieData) {
		this.pieData = pieData;
	}
	public Boolean getToShow() {
		return toShow;
	}
	public void setToShow(Boolean toShow) {
		this.toShow = toShow;
	}
	/**
	 * @return the toDel
	 */
	public Boolean getToDel() {
		return toDel;
	}
	/**
	 * @param toDel the toDel to set
	 */
	public void setToDel(Boolean toDel) {
		this.toDel = toDel;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SWidget [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", label=");
		builder.append(label);
		builder.append(", type=");
		builder.append(type);
		builder.append(", toShow=");
		builder.append(toShow);
		builder.append("]\n");
		return builder.toString();
	}
	
	
}
