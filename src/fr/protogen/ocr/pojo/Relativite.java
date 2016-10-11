package fr.protogen.ocr.pojo;

import java.io.Serializable;



public class Relativite implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nom;
	private String relatif;
	private String crx;
	private String cry;
	private String type;
	private int x;
	private int y;
	private int w;
	private int h;
	public String getCrx() {
		return crx;
	}
	public void setCrx(String crx) {
		this.crx = crx;
	}
	public String getCry() {
		return cry;
	}
	public void setCry(String cry) {
		this.cry = cry;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
	public Relativite() {
		super();
		this.nom="";
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getRelatif() {
		return relatif;
	}
	public void setRelatif(String relatif) {
		this.relatif = relatif;
	}
	
	
	
}
