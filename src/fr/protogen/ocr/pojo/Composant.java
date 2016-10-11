package fr.protogen.ocr.pojo;

import java.io.Serializable;



public class Composant implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;

	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Composant() {
		super();
	}
	
	@Override
    public boolean equals(Object obj) {
            if(obj == null)
                    return false;
            if(!(obj instanceof Composant))
                    return false;
            
            return ((Composant)obj).getId() == this.id;
    }


    @Override
    public String toString() {
            return id+"";
    }
	
}
