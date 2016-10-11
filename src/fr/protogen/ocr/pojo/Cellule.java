package fr.protogen.ocr.pojo;

import java.io.Serializable;



public class Cellule implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int x;
	private int y;
	private int w;
	private int h;
	
	private String data;

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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Cellule(int x, int y, int w, int h, String data) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.data = data;
	}

	public Cellule() {
		super();
		this.x=0;this.y=0;this.w=0;this.h=0;
		this.data="vide";
	}
	
}
