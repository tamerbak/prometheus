package fr.protogen.callout.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.DAO.CalloutDAO;
import fr.protogen.masterdata.DAO.ResourcesDAL;
import fr.protogen.masterdata.model.CCallout;
import fr.protogen.masterdata.model.CCalloutArguments;
import fr.protogen.masterdata.model.GResource;

public class CalloutEngine {
	
	private static final int BUFFER_SIZE = 4096;
	private String baseURI="calloutlibs";
	
	private static CalloutEngine instance = null;
	public static synchronized CalloutEngine getInstance(){
		if(instance == null)
			instance = new CalloutEngine();
		return instance;
	}
	private CalloutEngine(){}
	
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public synchronized Object executeCallout(CCallout c) throws Exception{
		System.out.println("[GENERIUM][CALLOUT] STARTUP CALLOUT ENGINE");
		Object results = null;
		CalloutDAO dao = new CalloutDAO();
		c = dao.getCallout(c);
		
		String jarFile = c.getId()+".jar";//UUID.randomUUID().toString()+".jar";
		File jarF = new File(jarFile);
		if(!jarF.exists()){
			System.out.println("[GENERIUM][CALLOUT] CREATING JAR FILE "+jarFile);
			OutputStream os = new FileOutputStream(jarFile);
			
			IOUtils.write(c.getFile(), os);
			
			os.close();
		}
		
		JarFile jar = new JarFile(jarFile);
		Enumeration e = jar.entries();
		URL[] urls = loadUrls(jarFile);/*{ new URL("jar:file:" + jarFile+"!/"), new URL("jar:file:"+baseURI+"generium_callout.jar!/"), new URL("jar:file:"+baseURI+"postgresql-9.1-902.jdbc4.jar!/")
				, new URL("jar:file:"+baseURI+"commons-io-2.4.jar!/"), new URL("jar:file:"+baseURI+"flexjson-2.1.jar!/")};*/
		URLClassLoader cl = URLClassLoader.newInstance(urls);
		boolean found = false;
		Object object = null;
		System.out.println("[GENERIUM][CALLOUT] LOADING CLASSES :");
		while(e.hasMoreElements()){
			
			JarEntry je = (JarEntry)e.nextElement();
			if(je.isDirectory() || !je.getName().endsWith(".class")){
	            continue;
	        }
			
			String className = je.getName().substring(0,je.getName().length()-6);
			className = className.replace('/', '.');
			
		    Class classe = cl.loadClass(className);
		    String base = "";
		    try{
		    	base = classe.getSuperclass().getName(); 
		    }catch(Exception exc){
		    	continue;
		    }
		    System.out.println("\t[GENERIUM][CALLOUT] EXAMINATING : "+className);
		    if(base.equals("fr.protogen.callout.GCallout")){
		    	found = true;
		    	object = classe.newInstance();
		    	System.out.println("\t[GENERIUM][CALLOUT] CALLOUT CLASS FOUND : "+className);
		    	break;
		    }
		}
		jar.close();
		
		if(!found)
			return results;
		System.out.println("[GENERIUM][CALLOUT] GENERATING ARGUMENTS ");
		List args = new ArrayList();
		Class argClasse = cl.loadClass("fr.protogen.callout.GCalloutData");
		Class[] param = new Class[1];
		param[0] = String.class;
		Method setLabel = argClasse.getMethod("setLabel", param);
		param = new Class[1];
		param[0] = Object.class;
		Method setValue = argClasse.getMethod("setValue", param);
		for(CCalloutArguments a : c.getArgs()){
			Object d = argClasse.newInstance();
			setLabel.invoke(d, a.getLibelle());
			String realValue = com.sun.jersey.core.util.Base64.base64Decode(a.getValue());//Base64.base64Decode(a.getValue());
			setValue.invoke(d, realValue);
			/*GCalloutData d = new GCalloutData();
			d.setLabel(a.getLibelle());
			d.setValue(d.getValue());*/
			args.add(d);
			
		}
		param = new Class[1];
		param[0] = List.class;
		Method creatrice = object.getClass().getMethod("execute", param);
		
		//results = object.execute(args);
		System.out.println("[GENERIUM][CALLOUT] sTARTUP CALLOUT ");
		results = creatrice.invoke(object, args);
		
		/*File f = new File(jarFile);
		f.delete();*/
		
		System.out.println("[GENERIUM][CALLOUT] RETURNING RESULTS");
		return results;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public synchronized String executeInterpretCallout(CCallout c) throws Exception{
		System.out.println("[GENERIUM][CALLOUT] STARTUP CALLOUT ENGINE");
		Object results = null;
		CalloutDAO dao = new CalloutDAO();
		c = dao.getCallout(c);
		
		String jarFile = c.getId()+".jar";//UUID.randomUUID().toString()+".jar";
		File jarF = new File(jarFile);
		if(!jarF.exists()){
			System.out.println("[GENERIUM][CALLOUT] CREATING JAR FILE "+jarFile);
			OutputStream os = new FileOutputStream(jarFile);
			
			IOUtils.write(c.getFile(), os);
			
			os.close();
		}
		
		JarFile jar = new JarFile(jarFile);
		Enumeration e = jar.entries();
		URL[] urls = loadUrls(jarFile);/*{ new URL("jar:file:" + jarFile+"!/"), new URL("jar:file:"+baseURI+"generium_callout.jar!/"), new URL("jar:file:"+baseURI+"postgresql-9.1-902.jdbc4.jar!/")
				, new URL("jar:file:"+baseURI+"commons-io-2.4.jar!/"), new URL("jar:file:"+baseURI+"flexjson-2.1.jar!/")};*/
		URLClassLoader cl = URLClassLoader.newInstance(urls);
		boolean found = false;
		Object object = null;
		System.out.println("[GENERIUM][CALLOUT] LOADING CLASSES :");
		while(e.hasMoreElements()){
			
			JarEntry je = (JarEntry)e.nextElement();
			if(je.isDirectory() || !je.getName().endsWith(".class")){
	            continue;
	        }
			
			String className = je.getName().substring(0,je.getName().length()-6);
			className = className.replace('/', '.');
			
		    Class classe = cl.loadClass(className);
		    String base = "";
		    try{
		    	base = classe.getSuperclass().getName(); 
		    }catch(Exception exc){
		    	continue;
		    }
		    System.out.println("\t[GENERIUM][CALLOUT] EXAMINATING : "+className);
		    if(base.equals("fr.protogen.callout.GCallout")){
		    	found = true;
		    	object = classe.newInstance();
		    	System.out.println("\t[GENERIUM][CALLOUT] CALLOUT CLASS FOUND : "+className);
		    	break;
		    }
		}
		jar.close();
		
		if(!found)
			return "[]";
		System.out.println("[GENERIUM][CALLOUT] GENERATING ARGUMENTS ");
		List args = new ArrayList();
		Class argClasse = cl.loadClass("fr.protogen.callout.GCalloutData");
		Class[] param = new Class[1];
		param[0] = String.class;
		Method setLabel = argClasse.getMethod("setLabel", param);
		param = new Class[1];
		param[0] = Object.class;
		Method setValue = argClasse.getMethod("setValue", param);
		for(CCalloutArguments a : c.getArgs()){
			Object d = argClasse.newInstance();
			setLabel.invoke(d, a.getLibelle());
			String realValue = com.sun.jersey.core.util.Base64.base64Decode(a.getValue());//Base64.base64Decode(a.getValue());
			setValue.invoke(d, realValue);
			/*GCalloutData d = new GCalloutData();
			d.setLabel(a.getLibelle());
			d.setValue(d.getValue());*/
			args.add(d);
			
		}
		param = new Class[1];
		param[0] = List.class;
		Method creatrice = object.getClass().getMethod("execute", param);
		
		//results = object.execute(args);
		System.out.println("[GENERIUM][CALLOUT] sTARTUP CALLOUT ");
		results = creatrice.invoke(object, args);
		String v = "";
		try{
			List L = (List)results;
			if(L.isEmpty())
				return "[]";
			Object o0 = L.get(0);
			Method getValue = argClasse.getMethod("getValue");
			Object r = getValue.invoke(o0);
			v = r.toString();
		}catch(Exception exc){
			exc.printStackTrace();
		}
		
		
		
		System.out.println("[GENERIUM][CALLOUT] RETURNING RESULTS");
		return v;
	}
	
	private URL[] loadUrls(String jarFile) throws MalformedURLException {
		List<URL> results = new ArrayList<URL>();
		
		File dir = new File(baseURI);
		
		if(!dir.exists()){
			deflateResources();
			dir = new File(baseURI);
		}
		
		File[] files = dir.listFiles();
		
		for(File f : files){
			if(f.getName().endsWith(".jar")){
				results.add(new URL("jar:file:" + f.getAbsolutePath()+"!/"));
				System.out.println("LOADING JAR : "+f.getAbsolutePath());
			}
		}
		URL[] table = new URL[results.size()+1];
		for(int i = 0 ; i < results.size() ; i++)
			table[i] = results.get(i);
		table[table.length-1] = new URL("jar:file:" + jarFile+"!/");
		return table;
	}
	
	private void deflateResources() {
		String key = "CALLOUT_LIBS";
		GResource resource = ResourcesDAL.getInstance().loadResourceByKey(key);
		
		String zipFile = UUID.randomUUID().toString()+".zip";
		
		OutputStream os;
		try {
			os = new FileOutputStream(zipFile);
			IOUtils.write(resource.getFile(), os);
			os.close();
			unZipIt(zipFile, "calloutlibs");
			File fzip = new File(zipFile);
			fzip.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void unZipIt(String zipFilePath, String destDirectory) throws IOException{

		File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
	}
	
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
	
	//	Load a callout
	/*
	 * #APPEL
	 * nom		:	nomFonction
	 * fichier	:	nomfichier
	 * IN		:	[{label: test1, valeur: <<val1>>}, {label: test2, valeur: varA}]
	 * OUT		:	[{label: test3, valeur: varB}, {label: test4, valeur: varC}]
	 * #FIN
	 * soit nombre varA, varB, varC.
	 * dans
	 * 	varA:=<<val2>>;
	 * 	#nomFonction;
	 * 	ecrire varB*varC;
	 * fin.
	 */
	public String executeSerialize(String calloutCode){
		
		return null;
	}
	
}
