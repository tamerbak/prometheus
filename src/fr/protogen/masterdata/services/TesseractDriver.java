package fr.protogen.masterdata.services;

import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import net.sourceforge.tess4j.*;

import com.thoughtworks.xstream.XStream;

import fr.protogen.engine.utils.FileManipulation;
import fr.protogen.engine.utils.ProtogenParameters;
import fr.protogen.masterdata.DAO.OCRDataAccess;
import fr.protogen.masterdata.model.OCRDriverBean;
import fr.protogen.ocr.pojo.Cellule;
import fr.protogen.ocr.pojo.Colonne;
import fr.protogen.ocr.pojo.Document;
import fr.protogen.ocr.pojo.Header;
import fr.protogen.ocr.pojo.Ligne;
import fr.protogen.ocr.pojo.Relativite;
import fr.protogen.ocr.pojo.Singledata;
import fr.protogen.ocr.pojo.Tableau;

public class TesseractDriver {

	private OCRDriverBean driver;
	
	public TesseractDriver(OCRDriverBean driver)  {
		this.setDriver(driver);
	}

	public Document loadData(List<String> files){
		
		boolean incomplete=false;
		
		Document d = driver.getContent();
		try {
		String sdriver = (new XStream()).toXML(d); 
		InputStream is = IOUtils.toInputStream(sdriver);
		
		
		FileManipulation fm = FileManipulation.getInstance(FacesContext.getCurrentInstance().getExternalContext().getRealPath(""));
		
		fm.saveFile(FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+"/driver.xml", is);
		
		String pathtodriver = FacesContext.getCurrentInstance().getExternalContext().getRealPath("")+"/driver.xml";
		
		for(String f : files){
			String xml = readdocument(pathtodriver, f);
			
			System.out.println("************************");
			System.out.println(xml);
			System.out.println("************************");
			
			Document doc = (Document)(new XStream()).fromXML(xml);
			doc.setIddriver(driver.getId()+"");
			
			OCRDataAccess dao = new OCRDataAccess();
			int id = dao.dataInsert(doc);
			if(id>0)
				dao.insertHistory(id,doc.getMainEntity(),driver.getId(),f,"");
			else
				incomplete=true;
		}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TesseractException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(incomplete){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Numérisaion incomplète", "Veuillez vous référer à l'archive numérique pour vérifier vos documents"));
		}else{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Numérisation effectuée avec succès"));
		}
		
		return d;
	}
	
	
	public  Ligne header(Tableau t){
		Ligne l = new Ligne();
		for(Header h : t.getHeaders()){
			Colonne c = new Colonne();
			c.setHeader(h.getNom());
			l.getColonnes().add(c);
		}
		return l;
	}
	
	public  boolean in(List<String> ids,String id) {
		for(String i : ids){
			if(id.equals(i)){
				return true;
			}
		}
		return false;
	}
	
	
	public  String read(int x,int y,int w,int h, String image) throws NumberFormatException, IOException{
		String s="";String ss="";Process p=null;
		
		BufferedImage arriere = javax.imageio.ImageIO.read(new java.io.File(ProtogenParameters.SERVER_PATH+"/back.png"));
		BufferedImage avant = javax.imageio.ImageIO.read(new java.io.File(image)).getSubimage(x+1, y+1, w-1, h-1);
   		
		int wi = Math.max(arriere.getWidth(), avant.getWidth());
		int ha = Math.max(arriere.getHeight(), avant.getHeight());
		BufferedImage combined = new BufferedImage(wi, ha, BufferedImage.TYPE_INT_ARGB);
		
		Graphics g = combined.getGraphics();
		g.drawImage(arriere, 0, 0, null);
		g.drawImage(avant, 300, 200, null);
   		
   		try {
   			System.out.println("PNG FILE : "+ProtogenParameters.SERVER_PATH+"/out.png");
			ImageIO.write(combined, "PNG", new File(ProtogenParameters.SERVER_PATH+"/out.png"));
			System.out.println("COMMAND 1 : "+"tesseract "+ProtogenParameters.SERVER_PATH+"/out.png "+ProtogenParameters.SERVER_PATH+"/out");
			
			
			p = Runtime.getRuntime().exec("tesseract "+ProtogenParameters.SERVER_PATH+"/out.png "+ProtogenParameters.SERVER_PATH+"/out");
			p.waitFor();
			System.out.println("COMMAND 2 : "+"cat "+ProtogenParameters.SERVER_PATH+"/out.txt");
			p = Runtime.getRuntime().exec("cat "+ProtogenParameters.SERVER_PATH+"/out.txt");
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
         
         BufferedReader stdInput = new BufferedReader(new 
              InputStreamReader(p.getInputStream()));
         
         try {
			while ((s = stdInput.readLine()) != null) {
				
			     ss=ss+s;
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
   		
   		
   			
        return ss;
	}
	
	/*
	public  String read(int x,int y,int w,int h, String image) throws NumberFormatException, IOException, TesseractException{
				
				BufferedImage arriere = javax.imageio.ImageIO.read(new java.io.File(ProtogenParameters.SERVER_PATH+"/back.png"));
				BufferedImage avant = javax.imageio.ImageIO.read(new java.io.File(image)).getSubimage(x+1,y+1,w-1,h-1);
	       		
				int wi = Math.max(arriere.getWidth(), avant.getWidth());
				int ha = Math.max(arriere.getHeight(), avant.getHeight());
				BufferedImage combined = new BufferedImage(wi, ha, BufferedImage.TYPE_INT_ARGB);
				
				Graphics g = combined.getGraphics();
				g.drawImage(arriere, 0, 0, null);
				g.drawImage(avant, 300, 200, null);
	   			File out = new File(ProtogenParameters.SERVER_PATH+"/out.png");
	   			ImageIO.write(combined, "PNG", out);
	   			Tesseract instance = Tesseract.getInstance();
			
				ImageIO.scanForPlugins();
	   			String result = instance.doOCR(out);
	       		
	       			
	            return result;
	    }*/
	
	public Tableau lireTable(int x0, int y0, int w, int h, String file, Tableau tableau) throws IOException{
		
		// Detect origine point
		int x = x0;
		int y = y0;
		int xd = x0;
		int yd = y0;
		boolean flag = false;
		java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(new java.io.File(file));
				
		while (!flag){
			x = x0;
			while (x <= w){
				if(!isblack(x, y, bi)){
					x++;
					continue;
				}
				flag = true;
				break;
			}
			
			if(flag){
				xd = x;
				yd = y;
				break;
			}
			y++;
		}
		
		flag = false;
		
		if(xd == x0)
			flag = true;
		
		x = xd;
		while(!flag){
			x = x-1;
			y = yd;
			flag = true;
			for (y = y0 ; y <= h ;  ){
				if(isblack(x,y,bi)){
					flag = false;
					break;
				}
				y++;
			}
		}
		xd = x;
		
		//	Detect separation ligne
		int yl = yd;
		for(y = yd;y<h;y++){
			boolean lineflag = false;
			boolean debutflag = false;
			for(x = x0 ; x < w ; x++){
				if(isblack(x, y, bi) && !debutflag){
					debutflag = true;
					lineflag = true;
				}
				if(!debutflag)
					continue;
				if(!isblack(x, y, bi)){
					lineflag = false;
					break;
				}
			}
			
			if(lineflag){
				yl = y;
				break;
			}
				
		}
		
		// Detect columns
		List<Integer> colS = new ArrayList<Integer>();
		List<Integer> colE = new ArrayList<Integer>();
		int L = 20;
		int xs = xd;
		
		y = yl+1;
		
		for(x = xd ; x < w ; x++){
			boolean colflag = true;
			if(isblack(x, y, bi)){
				for(int y_hat = y ; y_hat < y+L; y_hat++){
					if(!isblack(x, y_hat, bi)){
						colflag = false;
						break;
					}
				}
				if(colflag){
					colS.add(xs);
					colE.add(x);
					xs = x;
				}
			}
		}
		
		//	Looping on lines and cells
		int ys = correctYF(xs, yd, bi);
		boolean begin = false;
		boolean line = false;
		xs = colS.get(0);
		int xv = correctXF(xs, ys, bi);
		List<List<Cellule>> cells = new ArrayList<List<Cellule>>();
		for(y = ys ; y < h ; y++){
			if(isblack(xs, ys, bi)){
				begin = true;
				line = true;
			}
			if(!begin)
				continue;
			for(x = xv ; x < xv+L ; x++){
				if(!isblack(x, ys, bi)){
					line = false;
					break;
				}
			}
			if(line){
				List<Cellule> lineCells = new ArrayList<Cellule>();
				for(int coo = 0 ; coo < colS.size() ; coo++){
					int ye = correctYB(xv, y, bi);
					String value=read(colS.get(coo),ys, colE.get(coo)-colS.get(coo)+1,ye-ys+1,file);
					Cellule c = new Cellule();
					c.setData(value);
					c.setH(ye-ys+1);
					c.setW(colE.get(coo)-colS.get(coo)+1);
					c.setX(colS.get(coo));
					c.setY(ys);
					lineCells.add(c);
				}
				cells.add(lineCells);
			}
			
		}
		
		tableau.setLignes(new ArrayList<Ligne>());
		for(List<Cellule> l : cells){
			Ligne tl = new Ligne();
			tl.setColonnes(new ArrayList<Colonne>());
			for(Cellule c : l){
				Colonne tc = new Colonne();
				tc.setData(c.getData());
				int index = l.indexOf(c);
				tc.setHeader(tableau.getHeaders().get(index).getNom());
				tl.getColonnes().add(tc);
			}
			tableau.getLignes().add(tl);
		}
		
		return tableau;
	}
	
	private int correctXF(int x, int y,
			java.awt.image.BufferedImage bi){
		while(isblack(x, y, bi)){
			x++;y++;
		}
		return x;
	}
	
	private int correctXB(int x, int y,
			java.awt.image.BufferedImage bi){
		while(isblack(x, y, bi)){
			x--;y--;
		}
		return x;
	}
	
	private int correctYF(int x, int y,
			java.awt.image.BufferedImage bi){
		while(isblack(y, y, bi)){
			x++;y++;
		}
		return y;
	}
	
	private int correctYB(int x, int y,
			java.awt.image.BufferedImage bi){
		while(isblack(x, y, bi)){
			x--;y--;
		}
		return y;
	}
	
	public  Tableau readtable(int xtab1, int ytab1,Ligne entete, String file) throws IOException, NumberFormatException, TesseractException {
		int xtab2=0;
		int ytab4=0;
		int nombredecolonne=0;
		int nombredeligne=0;
		int count=1;
		
		java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(new java.io.File(file));
		
		int xtab1c=xtab1;
		while(isblack(xtab1c, ytab1, bi)){
			xtab1c=xtab1c+1;
		}
		
		xtab2=xtab1c-1;
		
		
		int ytab1c=ytab1;
		while(isblack(xtab1, ytab1c, bi)){
			ytab1c=ytab1c+1;
		}
		
		ytab4=ytab1c-1;
		int hpt=ytab4;
		int toop=ytab1;
		xtab1c=xtab1+1;
		ytab1c=ytab1+1;
		
		while(!isblack(xtab1c, ytab1c, bi)){
			ytab1c=ytab1c+1;
		}
		
		
		xtab1c=xtab1;
		ytab1c=ytab1+1;
		
		for(int i=xtab1c; i<xtab2; i++){
			if(isblack(i, ytab1c, bi)){
				nombredecolonne++;
			}
		}
		
		xtab1c=xtab1+1;
		ytab1c=ytab1;
		
		for(int i=ytab1c; i<ytab4; i++){
			if(isblack(xtab1c, i, bi)){
				nombredeligne++;
			}
		}
		
		Cellule[][] contenu = new Cellule[nombredeligne][nombredecolonne];
		
		for(int i=0; i<nombredeligne; i++){
			for(int j=0; j<nombredecolonne; j++){
				Cellule c = new Cellule();
				if(j==0){
					c.setX(xtab1);
				}
				if(i==0){
					c.setY(ytab1);
				}
				contenu[i][j]=c;
			}
		}
		
		
		xtab1c=xtab1+1;
		ytab1c=ytab1+1;
		count=1;
		for(int i=xtab1c; i<xtab2; i++){
			if(isblack(i, ytab1c, bi)){
				for(int a=0; a<nombredeligne; a++){
					contenu[a][count].setX(i);
				}
				count++;
			}
		}
		
		xtab1c=xtab1+1;
		ytab1c=ytab1+1;
		count=1;
		for(int i=ytab1c; i<ytab4; i++){
			if(isblack(xtab1c, i, bi)){
				for(int a=0; a<nombredecolonne; a++){
					contenu[count][a].setY(i);
				}
				count++;
			}
		}
		
		for(int i=0; i<nombredeligne-1; i++){
			for(int j=0; j<nombredecolonne-1; j++){
				contenu[i][j].setW(contenu[i][j+1].getX()-contenu[i][j].getX());
			}
		}
		for(int i=0; i<nombredecolonne; i++){
			contenu[nombredeligne-1][i].setW(contenu[nombredeligne-2][i].getW());
		}
		
		
		for(int j=0; j<nombredecolonne-1; j++){
			for(int i=0; i<nombredeligne-1; i++){
				contenu[i][j].setH(contenu[i+1][j].getY()-contenu[i][j].getY());
			}
		}
		for(int i=0; i<nombredeligne; i++){
			contenu[i][nombredecolonne-1].setH(contenu[i][nombredecolonne-2].getH());
		}
		for(int i=0; i<nombredecolonne; i++){
			contenu[nombredeligne-1][i].setH(ytab4-contenu[nombredeligne-1][0].getY());
		}
		for(int i=0; i<nombredeligne; i++){
			contenu[i][nombredecolonne-1].setW(xtab2-contenu[0][nombredecolonne-1].getX());
		}
		
		for(int j=0; j<nombredecolonne; j++){
			for(int i=0; i<nombredeligne; i++){
				contenu[i][j].setData(read(contenu[i][j].getX(), contenu[i][j].getY(),contenu[i][j].getW(), contenu[i][j].getH(), file));
			}
		}
		
		
		Tableau tableau = new Tableau();
		int l=0; int c=0;
		
		for(int i=0; i<nombredeligne; i++){
			Ligne ligne = new Ligne();l++;ligne.setId(String.valueOf(l));
			for(int j=0; j<nombredecolonne; j++){
				Colonne colonne = new Colonne();
				colonne.setHeader(entete.getColonnes().get(c).getHeader());
				c++;
				colonne.setData(contenu[i][j].getData());
				ligne.getColonnes().add(colonne);
			}
			c=0;
			tableau.getLignes().add(ligne);
		}
		
		tableau.setH(hpt-toop);
		tableau.setW(xtab2);
		return tableau;
		
	}
	
	public  boolean isblack(int x, int y, BufferedImage bi){
		int rgb = bi.getRGB(x, y);
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = (rgb & 0xFF);
		if(r==0 && g==0 && b==0){
			return true;
		}else{
			return false;
		}
	}
	
	public  String readdocument(String pilote, String facture) throws NumberFormatException, IOException, TesseractException{
		
		File xmlFile = new File(pilote);
		Document d = (Document) new XStream().fromXML(new FileInputStream(xmlFile));
		
		List<String> relatifs = new ArrayList<String>();
		for(Relativite r : d.getRelativites()){
			relatifs.add(r.getRelatif());
		}
		List<Tableau> tbs = new ArrayList<Tableau>();
		for(Tableau t : d.getTableaux()){
			{
				if(!in(relatifs,t.getId())){
					Tableau ta = new Tableau();
					//ta=readtable(t.getX(), t.getY(), header(t), facture);
					ta = lireTable(t.getX(), t.getY(), t.getW(), t.getH(), facture, t);
					ta.setY(t.getY());
					ta.setId(t.getId());
					tbs.add(ta);
				}
			}
		}
		d.setTableaux(tbs);
		
		for(Singledata s : d.getSingledatas()){
			if(!in(relatifs,s.getId())){
			s.setData(read(s.getX(), s.getY(), s.getW(), s.getH(), facture));
			}
		}
		
		for(Relativite r : d.getRelativites()){
			for(Tableau t : d.getTableaux()){
				if(t.getId().equals(r.getRelatif())){
					if(r.getType().equals("h")){
						
					}
					if(r.getType().equals("v")){
						Tableau ta = new Tableau();
						for(Tableau tab : d.getTableaux()){
							if(tab.getId().equals(r.getCry())){
								
								ta=readtable(t.getX(), r.getH()+tab.getY()+tab.getW(), header(t), facture);
							}
						}
						for(Singledata s : d.getSingledatas()){
							if(s.getId().equals(r.getCry())){
								ta=readtable(t.getX(), r.getH()+s.getY()+s.getH(), header(t), facture);
							}
						}
						ta.setId(t.getId());
						tbs.add(ta);
					}
					if(r.getType().equals("b")){
						
					}
				}
			}
			for(Singledata s : d.getSingledatas()){
				if(s.getId().equals(r.getRelatif())){
					if(r.getType().equals("h")){
						
					}
					if(r.getType().equals("v")){
						
						for(Tableau tab : d.getTableaux()){
							if(tab.getId().equals(r.getCry())){
								System.out.println(r.getH());
								System.out.println(tab.getY());
								System.out.println(tab.getH());
								s.setData(read(s.getX(), r.getH()+tab.getY()+tab.getH(), s.getW(), s.getH(), facture));
							}
						}
						for(Singledata sd : d.getSingledatas()){
							if(sd.getId().equals(r.getCry())){
								s.setData(read(s.getX(), r.getH()+sd.getY()+sd.getH(), s.getW(), s.getH(), facture));
							}
						}
					}
					if(r.getType().equals("b")){
						
					}
				}
			}
		}
		
		
		return new XStream().toXML(d); 
		
	}
	
	public OCRDriverBean getDriver() {
		return driver;
	}

	public void setDriver(OCRDriverBean driver) {
		this.driver = driver;
	}	
}
