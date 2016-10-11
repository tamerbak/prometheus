package fr.protogen.engine.reporting;

import java.util.List;

/**
 * ABSTRACTION OF A REPORT CONSTRUCTOR USING JASPER REPORTS
 * 
 * @author jakjoud
 *
 */
public interface PDFWriter {
	/**
	 * Creating a PDF stream or file
	 * @param list The Jasper or JRXML full path
	 * @param dataSource The data source can be a reference to a local file, a key/value array of parameters, or the string format of data
	 * @param encodedIn Is the data source base64 encoded
	 * @param encodedOut Will the output stream be encoded or should we create a temporary file
	 * @return
	 */
	String doPrint(List<String> list, String dataSource, boolean encodedIn, boolean encodedOut);
}
