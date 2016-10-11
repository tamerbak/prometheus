package fr.protogen.engine.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SpecialValuesEngine {
	
	private static SpecialValuesEngine instance = null;
	public static synchronized SpecialValuesEngine getInstance(){
		if(instance==null)
			instance=new SpecialValuesEngine();
		
		return instance;
	}
	
	public synchronized String parseSpecialValues(String formula){
		String sv = formula;
		
		//	Date
		Date jour = new Date();
		SimpleDateFormat sdDate = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdDay = new SimpleDateFormat("dd");
		SimpleDateFormat sdMonth = new SimpleDateFormat("MM");
		SimpleDateFormat sdYear = new SimpleDateFormat("yyyy");
		
		sv = sv.replaceAll("<<date du jour>>", sdDate.format(jour));
		sv = sv.replaceAll("<<jour courant>>", sdDay.format(jour));
		sv = sv.replaceAll("<<mois courant>>", sdMonth.format(jour));
		sv = sv.replaceAll("<<année courante>>", sdYear.format(jour));
		sv = sv.replaceAll("<<Date du jour>>", sdDate.format(jour));
		sv = sv.replaceAll("<<Jour courant>>", sdDay.format(jour));
		sv = sv.replaceAll("<<Mois courant>>", sdMonth.format(jour));
		sv = sv.replaceAll("<<Année courante>>", sdYear.format(jour));
		
		Calendar c = Calendar.getInstance();
		c.setTime(jour);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.MONTH, 0);
		Date debutannee = c.getTime();
		sv = sv.replaceAll("<<début année en cours>>", sdDate.format(debutannee));
		sv = sv.replaceAll("<<Début année en cours>>", sdDate.format(debutannee));
		
		
		c.set(Calendar.DAY_OF_MONTH, 31);
		c.set(Calendar.MONTH, 11);
		Date finannee = c.getTime();
		sv = sv.replaceAll("<<fin année en cours>>", sdDate.format(finannee));
		sv = sv.replaceAll("<<Fin année en cours>>", sdDate.format(finannee));
		
		return sv;
	}
	
	public synchronized String parseSpecialsInValues(String formula){
		String sv = formula;
		
		//	Date
		Date jour = new Date();
		SimpleDateFormat sdDate = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdDay = new SimpleDateFormat("dd");
		SimpleDateFormat sdMonth = new SimpleDateFormat("MM");
		SimpleDateFormat sdYear = new SimpleDateFormat("yyyy");
		
		sv = sv.replaceAll("<<date du jour>>", jour.getTime()+"");
		sv = sv.replaceAll("<<jour courant>>", sdDay.format(jour));
		sv = sv.replaceAll("<<mois courant>>", sdMonth.format(jour));
		sv = sv.replaceAll("<<année courante>>", sdYear.format(jour));
		sv = sv.replaceAll("<<Date du jour>>", sdDate.format(jour));
		sv = sv.replaceAll("<<Jour courant>>", jour.getTime()+"");
		sv = sv.replaceAll("<<Mois courant>>", sdMonth.format(jour));
		sv = sv.replaceAll("<<Année courante>>", sdYear.format(jour));
		
		Calendar c = Calendar.getInstance();
		c.setTime(jour);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.MONTH, 0);
		Date debutannee = c.getTime();
		sv = sv.replaceAll("<<début année en cours>>", debutannee.getTime()+"");
		sv = sv.replaceAll("<<Début année en cours>>", debutannee.getTime()+"");
		
		
		c.set(Calendar.DAY_OF_MONTH, 31);
		c.set(Calendar.MONTH, 11);
		Date finannee = c.getTime();
		sv = sv.replaceAll("<<fin année en cours>>", finannee.getTime()+"");
		sv = sv.replaceAll("<<Fin année en cours>>",finannee.getTime()+"");
		
		return sv;
	}
}
