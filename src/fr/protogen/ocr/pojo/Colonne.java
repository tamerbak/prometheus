package fr.protogen.ocr.pojo;

import java.io.Serializable;


public class Colonne implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String data;
	private String header;
	private String format;
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	public Colonne() {
		super();
		this.header="";
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public Colonne(String id, String data, String header) {
		super();
		this.data = data;
		this.header = header;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	
}
