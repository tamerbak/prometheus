package fr.protogen.engine.reporting;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;

public class ExcelStyleFactory {

		public static HSSFCellStyle headerACellStyle(HSSFWorkbook hwb){
			HSSFCellStyle style = hwb.createCellStyle();
			style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			
			
			HSSFFont font = hwb.createFont();
		    font.setFontHeightInPoints((short)16);
		    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			style.setFont(font);
		    
			return style;
		}
		
		public static HSSFCellStyle headerBCellStyle(HSSFWorkbook hwb){
			HSSFCellStyle style = hwb.createCellStyle();
			style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			
			HSSFFont font = hwb.createFont();
		    font.setFontHeightInPoints((short)14);
			style.setFont(font);
		    
			return style;
		}
		
		public static HSSFCellStyle titleCellStyle(HSSFWorkbook hwb){
			HSSFCellStyle style = hwb.createCellStyle();
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
			style.setFillBackgroundColor(IndexedColors.BLUE_GREY.getIndex());
			
			HSSFFont font = hwb.createFont();
		    font.setFontHeightInPoints((short)12);
		    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		    font.setUnderline(HSSFFont.U_SINGLE);
		    style.setFont(font);
		    
			return style;
		}
		
		public static HSSFCellStyle gridCellStyle(HSSFWorkbook hwb){
			HSSFCellStyle style = hwb.createCellStyle();
			style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
			
			HSSFFont font = hwb.createFont();
		    font.setFontHeightInPoints((short)12);
		    style.setFont(font);
		    
			return style;
		}
}
