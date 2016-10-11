package fr.protogen.engine.utils;

import java.util.Calendar;
import java.util.Date;

public class CalendarUtils {

	public static Date addDays(Date origine, int nbj){
		Date next = new Date();
		
		Calendar c = Calendar.getInstance();
		c.setTime(origine);
		c.add(Calendar.DATE, nbj);
		next = c.getTime();
		return next;
	}
	
	public static String formatDate(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH)+1;
		int year = c.get(Calendar.YEAR);
		
		String sday = (day<10?"0":"")+day;
		String smonth = (month<10?"0":"")+month;
		return sday+"/"+smonth+"/"+year;
	}
}
