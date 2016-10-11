package fr.protogen.ocr.pojo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tableau implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private int x;
	private int y;
	private int w;
	private int h;
	private List<Ligne> lignes;
	private List<Header> headers;
	
	
	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public int getX() {
		return x;
	}



	public void setX(int x) {
		this.x = x;
	}



	public int getY() {
		return y;
	}



	public void setY(int y) {
		this.y = y;
	}



	public List<Ligne> getLignes() {
		return lignes;
	}



	public void setLignes(List<Ligne> lignes) {
		this.lignes = lignes;
	}

	


	public int getW() {
		return w;
	}



	public void setW(int w) {
		this.w = w;
	}



	public int getH() {
		return h;
	}



	public void setH(int h) {
		this.h = h;
	}



	public List<Header> getHeaders() {
		return headers;
	}



	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}



	public Tableau() {
		super();
		this.id="";
		this.lignes=new ArrayList<Ligne>();
		this.headers=new ArrayList<Header>();
	}
	
	
	
	
}
