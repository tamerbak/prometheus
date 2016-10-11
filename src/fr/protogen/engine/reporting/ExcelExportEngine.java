package fr.protogen.engine.reporting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import fr.protogen.engine.utils.ListKV;

public class ExcelExportEngine {

	public String generateExcel(List<ListKV> values, String window, List<String> winTitles) throws IOException{
		String filename = UUID.randomUUID().toString()+".xls";
		
		HSSFWorkbook hwb=new HSSFWorkbook();
		HSSFSheet sheet =  hwb.createSheet(window);
		HSSFRow rowhead=   sheet.createRow(0);
		rowhead.setHeightInPoints(32);
		rowhead.createCell(0).setCellValue(window);
		rowhead.getCell(0).setCellStyle(ExcelStyleFactory.headerACellStyle(hwb));

		HSSFRow titles = sheet.createRow(3);
		titles.setHeightInPoints(25);
		int i=0;
		for(String t : winTitles){
			titles.createCell(i).setCellValue(t);
			titles.getCell(i).setCellStyle(ExcelStyleFactory.titleCellStyle(hwb));
			i++;
		}
		
		int j=4;
		for(ListKV l : values){
			HSSFRow vals = sheet.createRow(j);
			vals.setHeightInPoints(20);
			i=0;
			for(String v : l.getValue()){
				vals.createCell(i).setCellValue(v);
				vals.getCell(i).setCellStyle(ExcelStyleFactory.gridCellStyle(hwb));
				i++;
			}
			j++;
		}
		
		for(i = 0 ; i < winTitles.size() ; i++){
			sheet.autoSizeColumn(i);
		}
		
		FileOutputStream fileOut =  new FileOutputStream(filename);
		hwb.write(fileOut);
		fileOut.close();
		
		return filename;
	}
}
