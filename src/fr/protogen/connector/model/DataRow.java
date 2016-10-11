package fr.protogen.connector.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dataRow")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataRow {
	private List<DataEntry> dataRow;

	public List<DataEntry> getDataRow() {
		return dataRow;
	}

	public void setDataRow(List<DataEntry> dataRow) {
		this.dataRow = dataRow;
	}
}
