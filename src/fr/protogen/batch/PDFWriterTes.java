package fr.protogen.batch;

import java.util.ArrayList;
import java.util.List;

import fr.protogen.engine.reporting.JSONPDFWriter;
import fr.protogen.engine.reporting.PDFWriter;

public class PDFWriterTes {
	public static void main(String[] args) {
		PDFWriter w = new JSONPDFWriter();
		List<String> jrxml =new ArrayList<String>();
		jrxml.add("/home/jakjoud/tmp/reportjson.jrxml");
		jrxml.add("/home/jakjoud/tmp/reportjson.jrxml");
		jrxml.add("/home/jakjoud/tmp/reportjson.jrxml");
		jrxml.add("/home/jakjoud/tmp/reportjson.jrxml");
		String pdf = w.doPrint(jrxml, "{     \"userName\": \"Evil Raat\",    \"details\": {        \"email\": \"not_really@test.com\"    }}", 
				false, true);
		System.out.println(pdf);
	}
}
