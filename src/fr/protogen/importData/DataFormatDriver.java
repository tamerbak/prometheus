package fr.protogen.importData;

public interface DataFormatDriver {

	CheckStatus chechFormat(String st);
	//DataStructure importData();
	DataStructure importData(String filename);
}
