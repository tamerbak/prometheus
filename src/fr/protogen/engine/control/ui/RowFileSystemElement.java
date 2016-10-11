package fr.protogen.engine.control.ui;

import java.io.Serializable;

import fr.protogen.masterdata.model.RowDocument;

@SuppressWarnings("serial")
public class RowFileSystemElement implements Serializable, Comparable<RowFileSystemElement> {

	private String name;
	private String type;
	private RowFolder folder;
	private RowDocument document;
	
	public RowFileSystemElement(RowFolder folder) {
		this.folder = folder;
		this.document = null;
		this.name = folder.getFolder().getName();
		this.type = "folder";
	}
	public RowFileSystemElement(RowDocument document) {
		this.folder = null;
		this.document = document;
		this.name = document.getName();
		this.type = "file";
	}
	
	@Override
	public int compareTo(RowFileSystemElement ref) {
		String sourcehash = constructRepresentative(this);
		String refhash = constructRepresentative(ref);
		return sourcehash.compareTo(refhash);
	}

	private static String constructRepresentative(RowFileSystemElement f){
		String source = f.name+"-"+f.type;
		int id=0;
		if(f.folder != null){
			id = f.folder.getFolder().getId();
		}
		if(f.document != null){
			id = f.document.getId();
		}
		source = source+"-"+id;
		return source;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public RowFolder getFolder() {
		return folder;
	}

	public void setFolder(RowFolder folder) {
		this.folder = folder;
	}

	public RowDocument getDocument() {
		return document;
	}

	public void setDocument(RowDocument document) {
		this.document = document;
	}

}
