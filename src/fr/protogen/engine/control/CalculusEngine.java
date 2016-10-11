package fr.protogen.engine.control;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;

import fr.protogen.dataload.GqlEngine;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dataload.QueryBuilder;
import fr.protogen.dto.MtmDTO;
import fr.protogen.engine.utils.GQLDataResult;
import fr.protogen.engine.utils.SourceCodeCleaner;
import fr.protogen.engine.utils.SpecialValuesEngine;
import fr.protogen.engine.utils.StringFormat;
import fr.protogen.engine.utils.UIControlElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.MAction;


public class CalculusEngine {

	private String compilerPath = "/protogenlang/"+FacesContext.getCurrentInstance().getExternalContext().getInitParameter("plcompiler");//"/protogenlang/plc.exe ";
	private String srv;
	
	public CalculusEngine(String serverPath){
		srv = serverPath;
		compilerPath = srv+compilerPath;
	}
	
	public String getCompilerPath() {
		return compilerPath;
	}

	public void setCompilerPath(String compilerPath) {
		this.compilerPath = compilerPath;
	}

	public List<Double> executePlainTextCode(String sourceCode) throws Exception{
		
		//	Check for RECUPERE
		if(sourceCode.contains("#!GQL")){
			List<String> scripts = new ArrayList<String>();
			while(sourceCode.contains("#!GQL")){
				int start = sourceCode.indexOf("#!GQL");
				int end = sourceCode.indexOf("#!FIN")+6;
				String sc = sourceCode.substring(start,end);
				sc = sc.replaceAll("#!GQL", "");
				sc = sc.replaceAll("#!FIN", "");
				
				scripts.add(sc);
				sourceCode = sourceCode.substring(end);
				sourceCode = sourceCode.replaceAll("#!FIN", "");
			}
			for(String s : scripts){
				s = s.replaceFirst("\r", "");
				s = s.replaceFirst("\n", "");
				GqlEngine engine = new GqlEngine();
				String operation = "";
				List<String> lignes = new ArrayList<String>();
				try {
					lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				for(String l : lignes){
					if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
						operation = l.split(":")[1].trim().replaceAll("\t", "");
					}
				}

				
				GQLDataResult res = engine.generate(s);
				if(!res.isDictionnaryMode()){
					double val = res.getSingleValue();
					sourceCode = sourceCode.replaceAll("#!"+operation+"!#", val+"");
				} else {
					for(String k : res.getValues().keySet()){
						sourceCode = sourceCode.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
					}
				}
				
			}
			
		}
		
		//		construct the temporary file
		Random rand = new Random(15);
		String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
		
		java.io.File file = new java.io.File(filename);
		file.createNewFile();
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
		writer.write(sourceCode);
		writer.close();
		
		//	Compile and run
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(compilerPath+" "+filename);
		BufferedReader in = new BufferedReader(
	               new InputStreamReader(process.getInputStream()) );
		String output = "";
		List<Double> result = new ArrayList<Double>();
		while((output = in.readLine()) != null){
			result.add(Double.parseDouble(output));
		}
		
		// Delete temporary file
		file = new java.io.File(filename);
		if(file.exists()){
			file.delete();
		}else
			throw new Exception("Can not access temporary file : "+filename);
		return result;
	}
	
	public List<Double> returnSingleExecution(Map<String,Double> input,List<String> args, MAction action, Map<String, String> processParameters) throws Exception{
			
			//	Inject arguments
			String sourceCode = action.getCode();
			
			if(sourceCode.lastIndexOf("\"")>0)
				sourceCode = sourceCode.substring(sourceCode.lastIndexOf("\"")+1);
			for(String arg : args){
				Double value = input.get(arg);
				
				sourceCode = sourceCode.replace("<<"+arg+">>", value.doubleValue()+"");
			}
			
			sourceCode = SpecialValuesEngine.getInstance().parseSpecialsInValues(sourceCode);
			
			for(String k : processParameters.keySet()){
				sourceCode = sourceCode.replace("<<"+k+">>", processParameters.get(k));
			}
			
//			Check for RECUPERE
			if(sourceCode.contains("#!GQL")){
				List<String> scripts = new ArrayList<String>();
				while(sourceCode.contains("#!GQL")){
					int start = sourceCode.indexOf("#!GQL");
					int end = sourceCode.indexOf("#!FIN")+6;
					String sc = sourceCode.substring(start,end);
					sc = sc.replaceAll("#!GQL", "");
					sc = sc.replaceAll("#!FIN", "");
					
					scripts.add(sc);
					sourceCode = sourceCode.substring(end);
				}
				for(String s : scripts){
					s = s.replaceFirst("\r", "");
					s = s.replaceFirst("\n", "");
					GqlEngine engine = new GqlEngine();
					String operation = "";
					List<String> lignes = new ArrayList<String>();
					try {
						lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					for(String l : lignes){
						if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
							operation = l.split(":")[1].trim().replaceAll("\t", "");
						}
					}
					GQLDataResult res = engine.generate(s);
					if(!res.isDictionnaryMode()){
						double val = res.getSingleValue();
						sourceCode = sourceCode.replaceAll("#!"+operation+"!#", val+"");
					} else {
						for(String k : res.getValues().keySet()){
							sourceCode = sourceCode.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
						}
					}
				}
				
			}
			
			//	construct the temporary file
			Random rand = new Random(15);
			String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
			
			java.io.File file = new java.io.File(filename);
			file.createNewFile();
			
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
			writer.write(sourceCode);
			writer.close();
			
			System.out.println("SINGLE EXECUTION\n\t"+sourceCode);
			
			//	Compile and run
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(compilerPath+" "+filename);
			BufferedReader in = new BufferedReader(
		               new InputStreamReader(process.getInputStream()) );
			String output = "";
			List<Double> result = new ArrayList<Double>();
			while((output = in.readLine()) != null){
				result.add(Double.parseDouble(output));
			}
			
			// Delete temporary file
			file = new java.io.File(filename);
			if(file.exists()){
				file.delete();
			}else
				throw new Exception("Can not access temporary file : "+filename);
			return result;
		
	}
	
	public List<List<Double>> returnListExecution(List<Map<String, Double>> input,List<String> args, MAction action, Map<String, String> processParameters) throws Exception{
		
		List<List<Double>> result = new ArrayList<List<Double>>();
		
		for(int i = 0 ; i < input.size() ; i++){
			result.add(returnSingleExecution(input.get(i), args, action, processParameters));
		}
		
		
		return result;
	}
	
	public double CalculateFormula(String formula, String entity, int entityID) throws Exception{
		
		//	SUM#class.attribute#script
		String[] tFormula = formula.split("#");
		if(tFormula[0].equals("SIMPLE")){
			String script = tFormula[2];
			List<String> attributes = getAttributes(script);
			ProtogenDataEngine engine = new ProtogenDataEngine();
			Map<String, Double> values = engine.getCalulationData(entity, attributes, entityID); 
			for(String k : values.keySet()){
				Double d = values.get(k);
				script = script.replaceAll("<<"+k+">>", d.doubleValue()+"");
			}
			
			//			Check for RECUPERE
			if(script.contains("#!GQL")){
				List<String> scripts = new ArrayList<String>();
				while(script.contains("#!GQL")){
					int start = script.indexOf("#!GQL");
					int end = script.indexOf("#!FIN");
					String sc = script.substring(start,end);
					sc = sc.replaceAll("#!GQL", "");
					sc = sc.replaceAll("#!FIN", "");
					
					scripts.add(sc);
					script = script.substring(end);
					script = script.replaceAll("#!FIN", "");
				}
				for(String s : scripts){
					s = s.replaceFirst("\r", "");
					s = s.replaceFirst("\n", "");
					GqlEngine gengine = new GqlEngine();
					String operation = "";
					List<String> lignes = new ArrayList<String>();
					try {
						lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					for(String l : lignes){
						if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
							operation = l.split(":")[1].trim().replaceAll("\t", "");
						}
					}
					GQLDataResult res = gengine.generate(s);
					if(!res.isDictionnaryMode()){
						double val = res.getSingleValue();
						script = script.replaceAll("#!"+operation+"!#", val+"");
					} else {
						for(String k : res.getValues().keySet()){
							script = script.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
						}
					}

				}
				
			}
			
			//		construct the temporary file
			Random rand = new Random(15);
			String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
			
			java.io.File file = new java.io.File(filename);
			file.createNewFile();
			
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
			writer.write(script);
			writer.close();
			
			//	Compile and run
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(compilerPath+" "+filename);
			BufferedReader in = new BufferedReader(
		               new InputStreamReader(process.getInputStream()) );
			String output = "";
			double result = 0;
			while((output = in.readLine()) != null){
				result = Double.parseDouble(output);
			}
			
			// Delete temporary file
			file = new java.io.File(filename);
			if(file.exists()){
				file.delete();
			}else
				throw new Exception("Can not access temporary file : "+filename);
			return result;
		} else if (tFormula[0].equals("SUM")){
			//	Main entity
			String script = tFormula[2];
			List<String> attributes = getAttributes(script);
			ProtogenDataEngine engine = new ProtogenDataEngine();
			Map<String, Double> values = engine.getCalulationData(entity, attributes, entityID); 
			for(String k : values.keySet()){
				Double d = values.get(k);
				script = script.replaceAll("<<"+k+">>", d.doubleValue()+"");
			}
			
			//	Mtm Entity
			String refTable = tFormula[1];
			List<String> refAttributes = getAttributes(script,refTable);
			List<Map<String, Double>> refValues = engine.getCalulationData(entity, refTable, refAttributes, entityID);
			double result = 0;
			for(Map<String, Double> row : refValues){
				for(String k : row.keySet()){
					Double d = row.get(k);
					script = script.replaceAll("<<"+k+">>", d.doubleValue()+"");
				}
				
				if(script.contains("#!GQL")){
					List<String> scripts = new ArrayList<String>();
					while(script.contains("#!GQL")){
						int start = script.indexOf("#!GQL");
						int end = script.indexOf("#!FIN");
						String sc = script.substring(start,end);
						sc = sc.replaceAll("#!GQL", "");
						sc = sc.replaceAll("#!FIN", "");
						
						scripts.add(sc);
						script = script.substring(end);
						script = script.replaceAll("#!FIN", "");
					}
					for(String s : scripts){
						s = s.replaceFirst("\r", "");
						s = s.replaceFirst("\n", "");
						GqlEngine gengine = new GqlEngine();
						String operation = "";
						List<String> lignes = new ArrayList<String>();
						try {
							lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
						} catch (IOException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						for(String l : lignes){
							if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
								operation = l.split(":")[1].trim().replaceAll("\t", "");
							}
						}
						GQLDataResult res = gengine.generate(s);
						if(!res.isDictionnaryMode()){
							double val = res.getSingleValue();
							script = script.replaceAll("#!"+operation+"!#", val+"");
						} else {
							for(String k : res.getValues().keySet()){
								script = script.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
							}
						}

					}
					
				}
				
				//	construct the temporary file
				Random rand = new Random(15);
				String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
				
				java.io.File file = new java.io.File(filename);
				file.createNewFile();
				
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
				writer.write(script);
				writer.close();
				
				//	Compile and run
				Runtime runtime = Runtime.getRuntime();
				Process process = runtime.exec(compilerPath+" "+filename);
				BufferedReader in = new BufferedReader(
			               new InputStreamReader(process.getInputStream()) );
				String output = "";
				while((output = in.readLine()) != null){
					result = result + Double.parseDouble(output);
				}
				
				// Delete temporary file
				file = new java.io.File(filename);
				if(file.exists()){
					file.delete();
				}else
					throw new Exception("Can not access temporary file : "+filename);
				
			}
			
			return result;
			
			
		} else if (tFormula[0].equals("MULTIPLY")){
			//	Main entity
			String script = tFormula[2];
			List<String> attributes = getAttributes(script);
			ProtogenDataEngine engine = new ProtogenDataEngine();
			Map<String, Double> values = engine.getCalulationData(entity, attributes, entityID); 
			for(String k : values.keySet()){
				Double d = values.get(k);
				script = script.replaceAll("<<"+k+">>", d.doubleValue()+"");
			}
			
			//	Mtm Entity
			String refTable = tFormula[1];
			List<String> refAttributes = getAttributes(script,refTable);
			List<Map<String, Double>> refValues = engine.getCalulationData(entity, refTable, refAttributes, entityID);
			double result = 0;
			for(Map<String, Double> row : refValues){
				for(String k : row.keySet()){
					Double d = row.get(k);
					script = script.replaceAll("<<"+k+">>", d.doubleValue()+"");
				}
				
				if(script.contains("#!GQL")){
					List<String> scripts = new ArrayList<String>();
					while(script.contains("#!GQL")){
						int start = script.indexOf("#!GQL");
						int end = script.indexOf("#!FIN");
						String sc = script.substring(start,end);
						sc = sc.replaceAll("#!GQL", "");
						sc = sc.replaceAll("#!FIN", "");
						
						scripts.add(sc);
						script = script.substring(end);
						script = script.replaceAll("#!FIN", "");
					}
					for(String s : scripts){
						s = s.replaceFirst("\r", "");
						s = s.replaceFirst("\n", "");
						GqlEngine gengine = new GqlEngine();
						String operation = "";
						List<String> lignes = new ArrayList<String>();
						try {
							lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
						} catch (IOException ex) {

							ex.printStackTrace();
						}
						for(String l : lignes){
							if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
								operation = l.split(":")[1].trim().replaceAll("\t", "");
							}
						}
						GQLDataResult res = gengine.generate(s);
						if(!res.isDictionnaryMode()){
							double val = res.getSingleValue();
							script = script.replaceAll("#!"+operation+"!#", val+"");
						} else {
							for(String k : res.getValues().keySet()){
								script = script.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
							}
						}
					}
					
				}
				
				//	construct the temporary file
				Random rand = new Random(15);
				String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
				
				java.io.File file = new java.io.File(filename);
				file.createNewFile();
				
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
				writer.write(script);
				writer.close();
				
				//	Compile and run
				Runtime runtime = Runtime.getRuntime();
				Process process = runtime.exec(compilerPath+" "+filename);
				BufferedReader in = new BufferedReader(
			               new InputStreamReader(process.getInputStream()) );
				String output = "";
				while((output = in.readLine()) != null){
					result = result + Double.parseDouble(output);
				}
				
				// Delete temporary file
				file = new java.io.File(filename);
				if(file.exists()){
					file.delete();
				}else
					throw new Exception("Can not access temporary file : "+filename);
				
			}
			
			return result;
			
			
		}
		
		
		
		return 0;
	}
	
	private List<String> getAttributes(String script, String refTable) {
		// TODO Auto-generated method stub
		List<String> results = new ArrayList<String>();
		for(int i = 0 ; i < script.length() ; i++){
			int index = script.indexOf("<<",i);
			int finish = script.indexOf(">>", index);
			
			String key = script.substring(index+2,finish-1);
			if(key.contains(refTable))
				results.add(key);
		}
		
		return results;
	}

	private List<String> getAttributes(String script) {
		// TODO Auto-generated method stub
		List<String> results = new ArrayList<String>();
		for(int i = 0 ; i < script.length() ; i++){
			int index = script.indexOf("<<",i);
			int finish = script.indexOf(">>", index);
			
			String key = script.substring(index+2,finish-1);
			if(!key.contains("."))
				results.add(key);
		}
		
		return results;
	}
	private List<String> getAttributes(String script, boolean lookAway) {
		// TODO Auto-generated method stub
		List<String> results = new ArrayList<String>();
		for(int i = 0 ; i < script.length() ; i++){
			int index = script.indexOf("<<",i);
			int finish = script.indexOf(">>", index);
			
			if(index <0 || finish<0)
				return results;
			
			String key = script.substring(index+2,finish);
			if(!key.contains(".") || lookAway)
				results.add(key);
			
			i=finish;
		}
		
		return results;
	}

	public double evaluateMtm(List<UIControlElement> controls,
			UIControlElement e) throws Exception {
		
		//	Get source code
		String code = e.getAttribute().getFormula();
		List<String> keys = getAttributes(code,true);
		Map<String, Double> values = new HashMap<String, Double>();
		
		for(String k : keys){
			//	deal with the normal attributes
			String[] tabattribute = k.split("\\.");
			String table = tabattribute[0];
			String attribute = tabattribute[1];
			if(!table.equals(e.getAttribute().getEntity().getName()))
				continue;
			for(UIControlElement c : controls){
				if(c.getAttribute().getEntity().getName().equals(table)
						&& c.getAttribute().getAttribute().equals(attribute)){
					values.put(k, new Double(format(c.getControlValue())));
					break;
				}
			}
		}
		
		for(String k : keys){
			//	deal with the referenced attributes
			String table = k.split("\\.")[0];
			String attribute = k.split("\\.")[1];
			if(table.equals(e.getAttribute().getEntity().getName()))
				continue;
			for(UIControlElement c : controls){
				if(c.isReference() && c.getAttribute().getDataReference().substring(3).equals(StringFormat.getInstance().tableDataReferenceFormat(table))){
					ProtogenDataEngine engine = new ProtogenDataEngine();
					double v = engine.getReferencedValueForFormula(c.getAttribute(),c.getControlValue(),attribute);
					values.put(k, new Double(v));
				}
			}
		}
		
		
		
		
		double result=0;
		for(String k : values.keySet()){
			Double d = values.get(k);
			code = code.replaceAll("<<"+k+">>", d.doubleValue()+"");
		}
		
		if(code.contains("#!GQL")){
			List<String> scripts = new ArrayList<String>();
			while(code.contains("#!GQL")){
				int start = code.indexOf("#!GQL");
				int end = code.indexOf("#!FIN");
				String sc = code.substring(start,end);
				sc = sc.replaceAll("#!GQL", "");
				sc = sc.replaceAll("#!FIN", "");
				
				scripts.add(sc);
				code = code.substring(end);
				code = code.replaceFirst("#!FIN", "");
			}
			for(String s : scripts){
				s = s.replaceFirst("\r", "");
				s = s.replaceFirst("\n", "");
				GqlEngine gengine = new GqlEngine();
				String operation = "";
				List<String> lignes = new ArrayList<String>();
				try {
					lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				for(String l : lignes){
					if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
						operation = l.split(":")[1].trim().replaceAll("\t", "");
					}
				}
				GQLDataResult res = gengine.generate(s);
				if(!res.isDictionnaryMode()){
					double val = res.getSingleValue();
					code = code.replaceAll("#!"+operation+"!#", val+"");
				} else {
					for(String k : res.getValues().keySet()){
						code = code.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
					}
				}
			}
			
		}
		
		//	construct the temporary file
		Random rand = new Random(15);
		String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
		
		File dir = new File(srv+"/tmp/");
		if(!dir.exists() || dir.isDirectory()){
			System.out.println("Temp directory not found, it will be created by Prometheus : "+srv+"/tmp/");
			dir.mkdir();
		}
		
		java.io.File file = new java.io.File(filename);
		file.createNewFile();
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
		writer.write(code);
		writer.close();
		
		//	Compile and run
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(compilerPath+" "+filename);
		BufferedReader in = new BufferedReader(
	               new InputStreamReader(process.getInputStream()) );
		String output = "";
		while((output = in.readLine()) != null){
			result = Double.parseDouble(output);
		}
		
		// Delete temporary file
		file = new java.io.File(filename);
		if(file.exists()){
			file.delete();
		}else
			throw new Exception("Can not access temporary file : "+filename);
		
		return result;
	}

	public double evaluateFormula(List<UIControlElement> controls,
			UIControlElement e, List<MtmDTO> dtos, String appKey) throws Exception {
			//		Get source code
			String code = e.getAttribute().getValidationFormula();
			List<String> keys = getAttributes(code,true);
			Map<String, Double> values = new HashMap<String, Double>();
			
			//	Check headers and substitute values
			if(code.startsWith("\"Cal")){
				SourceCodeCleaner cleaner = new SourceCodeCleaner();
				List<String> headers = cleaner.getHeaders(code);
				for(String header : headers){
					
					String variable=cleaner.getHeaderVariableSegment(header);
					
					//	Data source
					String datasource = cleaner.getHeaderValueSegment(header);
					datasource = datasource.replaceAll("<<", "").replaceAll(">>", "");
					
					String table = datasource.split("\\.")[0];
					table = "user_"+StringFormat.getInstance().attributeDataReferenceFormat(table);
					String column = datasource.split("\\.")[1];
					
					ApplicationLoader al = new ApplicationLoader();
					CBusinessClass cbc = al.getEntity(table);
					
					for(CAttribute a : cbc.getAttributes()){
						if(a.getAttribute().equals(column)){
							column = a.getDataReference();
							break;
						}
					}
										
					
					String sql = "select "+table+"."+column;
					
					//	Key Constraint
					String constraintCol = cleaner.getHeaderReferenceKey(header);
					constraintCol = constraintCol.replaceAll("<<", "").replaceAll(">>", "");
					String constraintKey="";
					String referencedTable="";
					for(UIControlElement c : controls){
						if(c.getAttribute().getAttribute().toLowerCase().equals(constraintCol.toLowerCase()) && c.getAttribute().isReference()){
							referencedTable=c.getAttribute().getDataReference().substring(3);
							String value="0";
							String keycolumn="";
							keycolumn = c.getAttribute().getDataReference();
							value = c.getControlValue();
							if(value.equals(""))
								value = c.getListReference().get(0).getKey();
							constraintKey = keycolumn+"="+value;
							break;
						}
					}
					String ssql ="";
					if(table.equals(referencedTable)){
						sql = sql+" from "+table+" ";
						ssql =" where "+constraintKey.replaceAll("fk_", "pk_")+" ";
					}else{
						sql = sql+" from "+table+", "+referencedTable;
						ssql =" where "+table+".fk_"+referencedTable+"="+referencedTable+".pk_"+referencedTable+" AND "+constraintKey+" ";
					}
					
					//	Other constraints
					List<String> cs = cleaner.getHeaderConstraints(header);
					List<String> constraints=new ArrayList<String>();
					ApplicationLoader dal = new ApplicationLoader();
					CBusinessClass headerEntity = dal.getEntity(table);
					CBusinessClass referenceEntity = dal.getEntity(referencedTable);
					for(String c : cs){
						String ctable = cleaner.getHeaderConstraintTable(c);
						String ccol = cleaner.getHeaderConstraintColumn(c);
						String constr = "";
						CBusinessClass en=null;
						if(headerEntity.getName().equals(ctable))
							en = headerEntity;
						else if (referenceEntity.getName().equals(ctable))
							en=referenceEntity;
						
						if(en == null){
							String dr = "user_"+StringFormat.getInstance().attributeDataReferenceFormat(ctable);
							en = dal.getEntity(dr);
							sql = sql+", "+dr;
							ssql = ssql+" AND "+dr+".pk_"+dr+"="+table+".fk_"+dr;
						}
						
						for(CAttribute a : en.getAttributes())
							if(a.getAttribute().equals(ccol)){
								constr = en.getDataReference()+"."+a.getDataReference();
								break;
							}
						
						String newcst = c.replaceAll("<<"+ctable+"."+ccol+">>", constr);
						constraints.add(newcst);
						ssql = ssql+" AND "+newcst+" ";
					}
					sql = sql+ssql;
					List<Double> dvals = new ArrayList<Double>();				
					try{
						Connection cnx=ProtogenConnection.getInstance().getConnection();
					    PreparedStatement ps = cnx.prepareStatement(sql);
						ResultSet rs = ps.executeQuery();
						while(rs.next())
							dvals.add(new Double(rs.getObject(1).toString()));
						rs.close();
						ps.close();
					}catch(Exception exc){
						exc.printStackTrace();
					}
					if(dvals.size()==0)
						dvals.add(new Double(0));
					for(String k : keys){
						if(k.equals(variable))
							values.put(k, dvals.get(0));
						
						if(k.equals("SOMME_"+variable)){
							double s=0;
							for(Double d : dvals){
								s = s+d.doubleValue();
							}
							values.put(k, new Double(s));
						}
						
						if(k.equals("MOYENNE_"+variable)){
							double s=0;
							for(Double d : dvals){
								s = s+d.doubleValue();
							}
							if(dvals.size()>0)
								s = s/dvals.size();
							values.put(k, new Double(s));
						}
						
						if(k.equals("PRODUIT_"+variable)){
							double s=1;
							for(Double d : dvals){
								s = s*d.doubleValue();
							}
							values.put(k, new Double(s));
						}
						
						if(k.equals("MAX_"+variable)){
							Double s=dvals.get(0);
							for(Double d : dvals){
								if(d.doubleValue()>s.doubleValue())
									s = d;
							}
							values.put(k, s);
						}
						if(k.equals("MIN_"+variable)){
							Double s=dvals.get(0);
							for(Double d : dvals){
								if(d.doubleValue()<s.doubleValue())
									s = d;
							}
							values.put(k, s);
						}
					}
				}
			}
				
			for(String k : keys){
				if(k.startsWith("SOMME_") || k.startsWith("MOYENNE_") || k.startsWith("PRODU_") || k.startsWith("DIAMOND_") || k.startsWith("GLOBAL#"))	//	Fonctions
					continue;
				//	deal with the normal attributes
				if(k.split("\\.").length==1)
					continue;
				
				String[] tabattribute = k.split("\\.");
				String table = tabattribute[0];
				String attribute = tabattribute[1];
				
				
				
				if(e.getAttribute().getEntity().getName()==null || e.getAttribute().getEntity().getName().length()==0){
					
					ProtogenDataEngine pde = new ProtogenDataEngine();
										
					CBusinessClass ent = pde.getAttributeEntity(e.getAttribute().getId()); 
					
					e.getAttribute().setEntity(ent);
				}
				
				for(UIControlElement ce : controls){
					if(ce.getAttribute().getId()==0)
						continue;
					ProtogenDataEngine pde = new ProtogenDataEngine();
					
					CBusinessClass ent = pde.getAttributeEntity(ce.getAttribute().getId()); 
					
					ce.getAttribute().setEntity(ent);
				}
				
				if(!table.equals(e.getAttribute().getEntity().getName()))
					continue;
				for(UIControlElement c : controls){
					if(c.getAttribute().getId()==0)
						continue;
					if(c.getAttribute().getEntity().getName().equals(table)
							&& c.getAttribute().getAttribute().equals(attribute)){
						double v = format(c);
						values.put(k, new Double(v));
						break;
					}
				}
			}
			
			for(String k : keys){
				//	deal with the referenced attributes
				if(k.startsWith("SOMME_") || k.startsWith("MOYENNE_") || k.startsWith("PRODU_") || k.startsWith("DIAMOND_") || k.startsWith("GLOBAL#"))	//	Fonctions
					continue;
				
				if(k.split("\\.").length==1)
					continue;
				String table = k.split("\\.")[0];
				String attribute = k.split("\\.")[1];
				if(table.equals(e.getAttribute().getEntity().getName()))
					continue;
				for(UIControlElement c : controls){
					if(c.getAttribute().getId()==0)
						continue;
					if(c.isReference() ){	
						ProtogenDataEngine engine = new ProtogenDataEngine();
						ApplicationLoader dal = new ApplicationLoader(); 
						String attributetable = c.getAttribute().getDataReference().substring(3);
						String tableName = dal.getEntityFromDR(attributetable);
						
						if(!table.equals(tableName))
							continue;
						
						double v = engine.getReferencedValueForFormula(c.getAttribute(),c.getControlValue(),attribute);
						values.put(k, new Double(v));
						break;
					}
				}
			}
			
			for(String k : keys){
				if(!k.startsWith("SOMME_"))
					continue;
				if(k.split("\\.").length==1)
					continue;
				String rk=k.substring(6);
				String table = rk.split("\\.")[0];
				String attribute = rk.split("\\.")[1];
				
				double somme = 0;
				for(MtmDTO dto : dtos){
					if(dto.getMtmEntity().getName().equals(table)){
						for(Map<CAttribute,Object> datum : dto.getMtmData()){
							for(CAttribute a : datum.keySet())
								if(a.getAttribute().equals(attribute)){
									double v = format(datum.get(a).toString());
									
									somme = somme + v;
									break;
								}
						}
						break;
					}
				}
				values.put(k, new Double(somme));
			}
			
			for(String k : keys){
				if(!k.startsWith("MOYENNE_"))
					continue;
				if(k.split("\\.").length==1)
					continue;
				String rk=k.substring(8);
				String table = rk.split("\\.")[0];
				String attribute = rk.split("\\.")[1];
				
				double somme = 0;
				for(MtmDTO dto : dtos){
					if(dto.getMtmEntity().getName().equals(table)){
						for(Map<CAttribute,Object> datum : dto.getMtmData()){
							for(CAttribute a : datum.keySet())
								if(a.getAttribute().equals(attribute)){
									double v = format(datum.get(a).toString());
									
									somme = somme + v;
									break;
								}
						}
						if(dto.getMtmData().size() >0)
							somme = somme/dto.getMtmData().size();
						break;
					}
				}
				values.put(k, new Double(somme));
			}
			
			for(String k : keys){
				if(!k.startsWith("PRODU_"))
					continue;
				if(k.split("\\.").length==1)
					continue;
				String rk=k.substring(6);
				String table = rk.split("\\.")[0];
				String attribute = rk.split("\\.")[1];
				
				double produit = 1;
				for(MtmDTO dto : dtos){
					if(dto.getMtmEntity().getName().equals(table)){
						for(Map<CAttribute,Object> datum : dto.getMtmData()){
							for(CAttribute a : datum.keySet())
								if(a.getAttribute().equals(attribute)){
									double v = format(datum.get(a).toString());
									
									produit = produit * v;
									break;
								}
						}
						break;
					}
				}
				values.put(k, new Double(produit));
			}
			
			
			for(String k : keys){
				if(!k.startsWith("DIAMOND_"))
					continue;
				
				// DIAMOND_tableval.valeur_tabler1_tabler2
				
				
			}
			
			for(String k : keys){
				if(!k.startsWith("GLOBAL#"))
					continue;
				String globalKey = k.split("#")[1];
				ProtogenDataEngine engine = new ProtogenDataEngine();
				double value = engine.getGlobalValue(globalKey,appKey);
				values.put(k, value);
			}
			
			//	Clean code from headers
			code = code.substring(code.lastIndexOf('"')+1);
			
			double result=0;
			for(String k : values.keySet()){
				Double d = values.get(k);
				code = code.replaceAll("<<"+k+">>", d.doubleValue()+"");
			}
			
			
			if(code.contains("#!GQL")){
				List<String> scripts = new ArrayList<String>();
				while(code.contains("#!GQL")){
					int start = code.indexOf("#!GQL");
					int end = code.indexOf("#!FIN");
					String sc = code.substring(start,end);
					sc = sc.replaceAll("#!GQL", "");
					sc = sc.replaceAll("#!FIN", "");
					
					scripts.add(sc);
					code = code.substring(end);
					code = code.replaceFirst("#!FIN", "");
				}
				for(String s : scripts){
					s = s.replaceFirst("\r", "");
					s = s.replaceFirst("\n", "");
					GqlEngine gengine = new GqlEngine();
					String operation = "";
					List<String> lignes = new ArrayList<String>();
					try {
						lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					for(String l : lignes){
						if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
							operation = l.split(":")[1].trim().replaceAll("\t", "");
						}
					}
					GQLDataResult res = gengine.generate(s);
					if(!res.isDictionnaryMode()){
						double val = res.getSingleValue();
						code = code.replaceAll("#!"+operation+"!#", val+"");
					} else {
						for(String k : res.getValues().keySet()){
							code = code.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
						}
					}
				}
				
			}
			
			//	construct the temporary file
			Random rand = new Random(15);
			String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
			
			java.io.File file = new java.io.File(filename);
			file.createNewFile();
			
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
			writer.write(code);
			writer.close();
			
			if(code.contains("#!GQL")){
				List<String> scripts = new ArrayList<String>();
				while(code.contains("#!GQL")){
					int start = code.indexOf("#!GQL");
					int end = code.indexOf("#!FIN");
					String sc = code.substring(start,end);
					sc = sc.replaceAll("#!GQL", "");
					sc = sc.replaceAll("#!FIN", "");
					
					scripts.add(sc);
					code = code.substring(end);
					code = code.replaceFirst("#!FIN", "");
				}
				for(String s : scripts){
					s = s.replaceFirst("\r", "");
					s = s.replaceFirst("\n", "");
					GqlEngine gengine = new GqlEngine();
					String operation = "";
					List<String> lignes = new ArrayList<String>();
					try {
						lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					for(String l : lignes){
						if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
							operation = l.split(":")[1].trim().replaceAll("\t", "");
						}
					}
					GQLDataResult res = gengine.generate(s);
					if(!res.isDictionnaryMode()){
						double val = res.getSingleValue();
						code = code.replaceAll("#!"+operation+"!#", val+"");
					} else {
						for(String k : res.getValues().keySet()){
							code = code.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
						}
					}
				}
				
			}
			
			//	Compile and run
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(compilerPath+" "+filename);
			BufferedReader in = new BufferedReader(
		               new InputStreamReader(process.getInputStream()) );
			String output = "";
			while((output = in.readLine()) != null){
				result = Double.parseDouble(output);
			}
			
			// Delete temporary file
			file = new java.io.File(filename);
			if(file.exists()){
				file.delete();
			}else
				throw new Exception("Can not access temporary file : "+filename);
			
			return result;
	}
	
	public double evaluateMtm(List<UIControlElement> controls,
			UIControlElement e, List<MtmDTO> dtos, String appKey) throws Exception {
		
			//	Get source code
			String code = e.getAttribute().getFormula();
			List<String> keys = getAttributes(code,true);
			Map<String, Double> values = new HashMap<String, Double>();
			
			//	Check headers and substitute values
			if(code.startsWith("\"Cal")){
				SourceCodeCleaner cleaner = new SourceCodeCleaner();
				List<String> headers = cleaner.getHeaders(code);
				for(String header : headers){
					
					String variable=cleaner.getHeaderVariableSegment(header);
					
					//	Data source
					String datasource = cleaner.getHeaderValueSegment(header);
					datasource = datasource.replaceAll("<<", "").replaceAll(">>", "");
					
					String table = datasource.split("\\.")[0];
					table = "user_"+StringFormat.getInstance().attributeDataReferenceFormat(table);
					String column = datasource.split("\\.")[1];
					column = StringFormat.getInstance().attributeDataReferenceFormat(column);
					
					String sql = "select "+table+"."+column;
					
					//	Key Constraint
					String constraintCol = cleaner.getHeaderReferenceKey(header);
					constraintCol = constraintCol.replaceAll("<<", "").replaceAll(">>", "");
					String constraintKey="";
					String referencedTable="";
					for(UIControlElement c : controls){
						if(c.getAttribute().getAttribute().toLowerCase().equals(constraintCol.toLowerCase()) && c.getAttribute().isReference()){
							referencedTable=c.getAttribute().getDataReference().substring(3);
							String value="0";
							String keycolumn="";
							keycolumn = c.getAttribute().getDataReference();
							value = c.getControlValue();
							constraintKey = keycolumn+"="+value;
							break;
						}
					}
					
					sql = sql+" from "+table+", "+referencedTable;
					String ssql =" where "+table+".fk_"+referencedTable+"="+referencedTable+".pk_"+referencedTable+" AND "+constraintKey+" ";
					
					//	Other constraints
					List<String> cs = cleaner.getHeaderConstraints(header);
					List<String> constraints=new ArrayList<String>();
					ApplicationLoader dal = new ApplicationLoader();
					CBusinessClass headerEntity = dal.getEntity(table);
					CBusinessClass referenceEntity = dal.getEntity(referencedTable);
					for(String c : cs){
						String ctable = cleaner.getHeaderConstraintTable(c);
						String ccol = cleaner.getHeaderConstraintColumn(c);
						String constr = "";
						CBusinessClass en=null;
						if(headerEntity.getName().equals(ctable))
							en = headerEntity;
						else if (referenceEntity.getName().equals(ctable))
							en=referenceEntity;
						
						if(en == null){
							String dr = "user_"+StringFormat.getInstance().attributeDataReferenceFormat(ctable);
							en = dal.getEntity(dr);
							sql = sql+", "+dr;
							ssql = ssql+" AND "+dr+".pk_"+dr+"="+table+".fk_"+dr;
						}
						
						for(CAttribute a : en.getAttributes())
							if(a.getAttribute().equals(ccol)){
								constr = en.getDataReference()+"."+a.getDataReference();
								break;
							}
						
						String newcst = c.replaceAll("<<"+ctable+"."+ccol+">>", constr);
						constraints.add(newcst);
						ssql = ssql+" AND "+newcst+" ";
					}
					sql = sql+ssql;
					List<Double> dvals = new ArrayList<Double>();				
					try{
						Connection cnx=ProtogenConnection.getInstance().getConnection();
					    PreparedStatement ps = cnx.prepareStatement(sql);
						ResultSet rs = ps.executeQuery();
						while(rs.next())
							dvals.add(new Double(rs.getObject(1).toString()));
						rs.close();
						ps.close();
					}catch(Exception exc){
						exc.printStackTrace();
					}
					if(dvals.size()==0)
						dvals.add(new Double(0));
					for(String k : keys){
						if(k.equals(variable))
							values.put(k, dvals.get(0));
						
						if(k.equals("SOMME_"+variable)){
							double s=0;
							for(Double d : dvals){
								s = s+d.doubleValue();
							}
							values.put(k, new Double(s));
						}
						if(k.equals("MOYENNE_"+variable)){
							double s=0;
							for(Double d : dvals){
								s = s+d.doubleValue();
							}
							if(dvals.size()>0)
								s=s/dvals.size();
							values.put(k, new Double(s));
						}
						
						if(k.equals("PRODUIT_"+variable)){
							double s=1;
							for(Double d : dvals){
								s = s*d.doubleValue();
							}
							values.put(k, new Double(s));
						}
						
						if(k.equals("MAX_"+variable)){
							Double s=dvals.get(0);
							for(Double d : dvals){
								if(d.doubleValue()>s.doubleValue())
									s = d;
							}
							values.put(k, s);
						}
						if(k.equals("MIN_"+variable)){
							Double s=dvals.get(0);
							for(Double d : dvals){
								if(d.doubleValue()<s.doubleValue())
									s = d;
							}
							values.put(k, s);
						}
					}
				}
			}
			
			for(String k : keys){
				if(k.startsWith("SOMME_") || k.startsWith("MOYENNE_") || k.startsWith("PRODU_") || k.startsWith("DIAMOND_") || k.startsWith("GLOBAL#") || k.startsWith("REF#"))	//	Fonctions
					continue;
				//	deal with the normal attributes
				if(k.split("\\.").length==1)
					continue;
				String[] tabattribute = k.split("\\.");
				
				String table = tabattribute[0];
				String attribute = tabattribute[1];
				if(!table.equals(e.getAttribute().getEntity().getName()))
					continue;
				for(UIControlElement c : controls){
					if(c.getAttribute().getEntity().getName().equals(table)
							&& c.getAttribute().getAttribute().equals(attribute)){
						double v = format(c);
						values.put(k, new Double(v));
						break;
					}
				}
			}
			
			for(String k : keys){
				//	deal with the referenced attributes
				if(k.startsWith("SOMME_") || k.startsWith("MOYENNE_") || k.startsWith("PRODU_") || k.startsWith("DIAMOND_") || k.startsWith("GLOBAL#") || k.startsWith("REF#"))	//	Fonctions
					continue;
				if(k.split("\\.").length==1)
					continue;
				
				String table = k.split("\\.")[0];
				String attribute = k.split("\\.")[1];
				if(table.equals(e.getAttribute().getEntity().getName()))
					continue;
				for(UIControlElement c : controls){
					if(c.isReference() && c.getReferenceTable().equals(table) ){
						ProtogenDataEngine engine = new ProtogenDataEngine();
						double v = engine.getReferencedValueForFormula(c.getAttribute(),c.getControlValue(),attribute);
						values.put(k, new Double(v));
					}
				}
			}
			
			for(String k : keys){
				if(!k.startsWith("SOMME_"))
					continue;
				if(k.split("\\.").length==1)
					continue;
				
				String rk=k.substring(6);
				String table = rk.split("\\.")[0];
				String attribute = rk.split("\\.")[1];
				
				double somme = 0;
				for(MtmDTO dto : dtos){
					if(dto.getMtmEntity().getName().equals(table)){
						for(Map<CAttribute,Object> datum : dto.getMtmData()){
							for(CAttribute a : datum.keySet())
								if(a.getAttribute().equals(attribute)){
									double v = format(datum.get(a).toString());
									
									somme = somme + v;
									break;
								}
						}
						break;
					}
				}
				values.put(k, new Double(somme));
			}
			
			for(String k : keys){
				if(!k.startsWith("MOYENNE_"))
					continue;
				if(k.split("\\.").length==1)
					continue;
				
				String rk=k.substring(8);
				String table = rk.split("\\.")[0];
				String attribute = rk.split("\\.")[1];
				
				double somme = 0;
				for(MtmDTO dto : dtos){
					if(dto.getMtmEntity().getName().equals(table)){
						for(Map<CAttribute,Object> datum : dto.getMtmData()){
							for(CAttribute a : datum.keySet())
								if(a.getAttribute().equals(attribute)){
									double v = format(datum.get(a).toString());
									
									somme = somme + v;
									break;
								}
						}
						if(dto.getMtmData().size() >0)
							somme = somme/dto.getMtmData().size();
						break;
					}
				}
				
				values.put(k, new Double(somme));
			}
			
			for(String k : keys){
				if(!k.startsWith("PRODU_"))
					continue;
				if(k.split("\\.").length==1)
					continue;
				
				String rk=k.substring(6);
				String table = rk.split("\\.")[0];
				String attribute = rk.split("\\.")[1];
				
				double produit = 1;
				for(MtmDTO dto : dtos){
					if(dto.getMtmEntity().getName().equals(table)){
						for(Map<CAttribute,Object> datum : dto.getMtmData()){
							for(CAttribute a : datum.keySet())
								if(a.getAttribute().equals(attribute)){
									double v = format(datum.get(a).toString());
									
									produit = produit * v;
									break;
								}
						}
						break;
					}
				}
				values.put(k, new Double(produit));
			}
			
			
			for(String k : keys){
				if(!k.startsWith("DIAMOND_"))
					continue;
				
				// DIAMOND_tableval.valeur_tabler1_tabler2
				
				
			}
			
			for(String k : keys){
				if(!k.startsWith("REF#"))
					continue;
				String[] rawreferences = k.split("#");
				String refTableName = rawreferences[1];
				String refTable = StringFormat.getInstance().tableDataReferenceFormat(refTableName);
				String mtmTableName = rawreferences[2];
				String mtmTable = StringFormat.getInstance().tableDataReferenceFormat(mtmTableName);
				String lineAttributeKey = rawreferences[3];
				
				String function = lineAttributeKey.split("_")[0];
				
				String lineTableName=lineAttributeKey.split("_")[1].split("\\.")[0];
				String lineTable = StringFormat.getInstance().tableDataReferenceFormat(lineTableName);

				String lineAttributeName=lineAttributeKey.split("_")[1].split("\\.")[1];
				String lineAttribute = StringFormat.getInstance().attributeDataReferenceFormat(lineAttributeName);
				
				String dbID = "0";
				for(UIControlElement c : controls){
					if(!c.isReference())
						continue;
					if(c.getAttribute().getAttribute().equals(refTableName)){
						dbID = c.getControlValue();
						refTable = c.getAttribute().getDataReference().substring(3);
					}
					
				}
				
				ProtogenDataEngine engine = new ProtogenDataEngine();
				List<Double> vals = engine.getValueDoubleReference(refTable,mtmTable, lineTable, lineAttribute,dbID); 
				double v = 0;
				if(function.equals("SOMME")){
					for(Double d : vals)
						v = v+d.doubleValue();
				}
				
				values.put(k, v);
			}
				
			for(String k : keys){
				if(!k.startsWith("GLOBAL#"))
					continue;
				String globalKey = k.split("#")[1];
				ProtogenDataEngine engine = new ProtogenDataEngine();
				double value = engine.getGlobalValue(globalKey,appKey);
				values.put(k, value);
			}
			
			double result=0;
			for(String k : values.keySet()){
				Double d = values.get(k);
				code = code.replaceAll("<<"+k+">>", d.doubleValue()+"");
			}
			
			//	Clean code from headers
			code = code.substring(code.lastIndexOf('"')+1);
			
			//	construct the temporary file
			Random rand = new Random(15);
			String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
			File dir = new File(srv+"/tmp/"); 
			if(!dir.exists() || dir.isFile())
				dir.mkdir();
			java.io.File file = new java.io.File(filename);
			file.createNewFile();
			
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
			writer.write(code);
			writer.close();
			
			//	Compile and run
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(compilerPath+" "+filename);
			BufferedReader in = new BufferedReader(
		               new InputStreamReader(process.getInputStream()) );
			String output = "";
			while((output = in.readLine()) != null){
				try{
					result = Double.parseDouble(output);
				}catch(Exception exc){
					System.out.println("Syntax Error \n-------------------------\n"
							+code
							+"\n-------------------------\n");
				}
			}
			
			// Delete temporary file
			file = new java.io.File(filename);
			if(file.exists()){
				file.delete();
			}else
				throw new Exception("Can not access temporary file : "+filename);
			
			return result;
	}

	private double format(UIControlElement c) {
		if(c.isCtrlDate()){
			return c.getDateValue().getTime()/ (24 * 60 * 60 * 1000);
		} else if (c.isBooleanValue()) {
			return c.isBooleanValue()?1.0:0.0;
		} else {
			return format(c.getControlValue());
		}
	}

	private double format(String controlValue) {
		// TODO Auto-generated method stub
		String[] temp = controlValue.split("\\:");
		if(controlValue == null || controlValue.length()==0)
			return 0;
		
		if(controlValue.trim().equals("OUI"))
			return 1.0;
		
		if(controlValue.trim().equals("NON"))
			return 0.0;
			
		if(temp == null || temp.length <2)
			return Double.parseDouble(controlValue);
		else {
			String t = temp[0]+temp[1];
			return Double.parseDouble(t);
		}
	}

	public double executeSimpleScript(String code) throws Exception {
		// TODO Auto-generated method stub
		double result=0;
		Random rand = new Random(15);
		String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
		
		java.io.File file = new java.io.File(filename);
		file.createNewFile();
		
		if(code.contains("#!GQL")){
			List<String> scripts = new ArrayList<String>();
			while(code.contains("#!GQL")){
				int start = code.indexOf("#!GQL");
				int end = code.indexOf("#!FIN")+6;
				String sc = code.substring(start,end);
				sc = sc.replaceAll("#!GQL", "");
				sc = sc.replaceAll("#!FIN", "");
				
				scripts.add(sc);
				code = code.substring(end);
			}
			for(String s : scripts){
				//	Eviter les retours chariot
				s = s.replaceFirst("\r", "");
				s = s.replaceFirst("\n", "");
				if(s.length() < 2)	
					continue;
				GqlEngine gengine = new GqlEngine();
				String operation = "";
				List<String> lignes = new ArrayList<String>();
				try {
					lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				for(String l : lignes){
					if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
						operation = l.split(":")[1].trim().replaceAll("\t", "");
					}
				}
				
				
				GQLDataResult res = gengine.generate(s);
				if(!res.isDictionnaryMode()){
					double val = res.getSingleValue();
					code = code.replaceAll("#!"+operation+"!#", val+"");
				} else {
					for(String k : res.getValues().keySet()){
						code = code.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
					}
				}
				
			}
			
		}
		
		System.out.println("----------------------------------------------------------");
		System.out.println(code);
		System.out.println("----------------------------------------------------------");
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
		writer.write(code);
		writer.close();
		
		//	Compile and run
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(compilerPath+" "+filename);
		BufferedReader in = new BufferedReader(
	               new InputStreamReader(process.getInputStream()) );
		String output = "";
		while((output = in.readLine()) != null){
			result = Double.parseDouble(output);
		}
		
		// Delete temporary file
		file = new java.io.File(filename);
		if(file.exists()){
			file.delete();
		}else
			throw new Exception("Can not access temporary file : "+filename);
		
		return result;
	}

	public double evaluateControlLayoutFormula(List<UIControlElement> controls,
			UIControlElement e, List<MtmDTO> dtos, String appKey) throws Exception {
		//		Get source code
		String code = e.getAttribute().getFormula();
		List<String> keys = getAttributes(code,true);
		Map<String, Double> values = new HashMap<String, Double>();
		
		//	Check headers and substitute values
		if(code.startsWith("\"Cal")){
			SourceCodeCleaner cleaner = new SourceCodeCleaner();
			List<String> headers = cleaner.getHeaders(code);
			for(String header : headers){
				
				String variable=cleaner.getHeaderVariableSegment(header);
				
				//	Data source
				String datasource = cleaner.getHeaderValueSegment(header);
				datasource = datasource.replaceAll("<<", "").replaceAll(">>", "");
				
				String table = datasource.split("\\.")[0];
				table = "user_"+StringFormat.getInstance().attributeDataReferenceFormat(table);
				String column = datasource.split("\\.")[1];
				
				ApplicationLoader al = new ApplicationLoader();
				CBusinessClass cbc = al.getEntity(table);
				
				for(CAttribute a : cbc.getAttributes()){
					if(a.getAttribute().equals(column)){
						column = a.getDataReference();
						break;
					}
				}
									
				
				String sql = "select "+table+"."+column;
				
				//	Key Constraint
				String constraintCol = cleaner.getHeaderReferenceKey(header);
				constraintCol = constraintCol.replaceAll("<<", "").replaceAll(">>", "");
				String constraintKey="";
				String referencedTable="";
				for(UIControlElement c : controls){
					if(c.getAttribute().getAttribute().toLowerCase().equals(constraintCol.toLowerCase()) && c.getAttribute().isReference()){
						referencedTable=c.getAttribute().getDataReference().substring(3);
						String value="0";
						String keycolumn="";
						keycolumn = c.getAttribute().getDataReference();
						value = c.getControlValue();
						
						if(value.equals(""))
							value = c.getListReference().get(0).getKey();
						constraintKey = keycolumn+"="+value;
						break;
					}
				}
				String ssql ="";
				if(table.equals(referencedTable)){
					sql = sql+" from "+table+" ";
					ssql =" where "+constraintKey.replaceAll("fk_", "pk_")+" ";
				}else{
					sql = sql+" from "+table+", "+referencedTable;
					ssql =" where "+table+".fk_"+referencedTable+"="+referencedTable+".pk_"+referencedTable+" AND "+constraintKey+" ";
				}
				
				//	Other constraints
				List<String> cs = cleaner.getHeaderConstraints(header);
				List<String> constraints=new ArrayList<String>();
				ApplicationLoader dal = new ApplicationLoader();
				CBusinessClass headerEntity = dal.getEntity(table);
				CBusinessClass referenceEntity = dal.getEntity(referencedTable);
				for(String c : cs){
					String ctable = cleaner.getHeaderConstraintTable(c);
					String ccol = cleaner.getHeaderConstraintColumn(c);
					String constr = "";
					CBusinessClass en=null;
					if(headerEntity.getName().equals(ctable))
						en = headerEntity;
					else if (referenceEntity.getName().equals(ctable))
						en=referenceEntity;
					
					if(en == null){
						String dr = "user_"+StringFormat.getInstance().attributeDataReferenceFormat(ctable);
						en = dal.getEntity(dr);
						sql = sql+", "+dr;
						ssql = ssql+" AND "+dr+".pk_"+dr+"="+table+".fk_"+dr;
					}
					
					for(CAttribute a : en.getAttributes())
						if(a.getAttribute().equals(ccol)){
							constr = en.getDataReference()+"."+a.getDataReference();
							break;
						}
					
					String newcst = c.replaceAll("<<"+ctable+"."+ccol+">>", constr);
					constraints.add(newcst);
					ssql = ssql+" AND "+newcst+" ";
				}
				sql = sql+ssql;
				List<Double> dvals = new ArrayList<Double>();				
				try{
					Connection cnx=ProtogenConnection.getInstance().getConnection();
				    PreparedStatement ps = cnx.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					while(rs.next())
						dvals.add(new Double(rs.getObject(1).toString()));
					rs.close();
					ps.close();
				}catch(Exception exc){
					exc.printStackTrace();
				}
				if(dvals.size()==0)
					dvals.add(new Double(0));
				for(String k : keys){
					if(k.equals(variable))
						values.put(k, dvals.get(0));
					
					if(k.equals("SOMME_"+variable)){
						double s=0;
						for(Double d : dvals){
							s = s+d.doubleValue();
						}
						values.put(k, new Double(s));
					}
					
					if(k.equals("PRODUIT_"+variable)){
						double s=1;
						for(Double d : dvals){
							s = s*d.doubleValue();
						}
						values.put(k, new Double(s));
					}
					
					if(k.equals("MAX_"+variable)){
						Double s=dvals.get(0);
						for(Double d : dvals){
							if(d.doubleValue()>s.doubleValue())
								s = d;
						}
						values.put(k, s);
					}
					if(k.equals("MIN_"+variable)){
						Double s=dvals.get(0);
						for(Double d : dvals){
							if(d.doubleValue()<s.doubleValue())
								s = d;
						}
						values.put(k, s);
					}
				}
			}
		}
			
		for(String k : keys){
			if(k.startsWith("SOMME_") || k.startsWith("MOYENNE_") || k.startsWith("PRODU_") || k.startsWith("DIAMOND_") || k.startsWith("GLOBAL#"))	//	Fonctions
				continue;
			//	deal with the normal attributes
			if(k.split("\\.").length==1)
				continue;
			
			String[] tabattribute = k.split("\\.");
			String table = tabattribute[0];
			String attribute = tabattribute[1];
			
			
			
			if(e.getAttribute().getEntity().getName()==null || e.getAttribute().getEntity().getName().length()==0){
				
				ProtogenDataEngine pde = new ProtogenDataEngine();
									
				CBusinessClass ent = pde.getAttributeEntity(e.getAttribute().getId()); 
				
				e.getAttribute().setEntity(ent);
			}
			
			for(UIControlElement ce : controls){
				if(ce.getAttribute().getId()==0)
					continue;
				ProtogenDataEngine pde = new ProtogenDataEngine();
				
				CBusinessClass ent = pde.getAttributeEntity(ce.getAttribute().getId()); 
				
				ce.getAttribute().setEntity(ent);
			}
			
			if(!table.equals(e.getAttribute().getEntity().getName()))
				continue;
			for(UIControlElement c : controls){
				if(c.getAttribute().getId()==0)
					continue;
				if(c.getAttribute().getEntity().getName().equals(table)
						&& c.getAttribute().getAttribute().equals(attribute)){
					double v = format(c);
					values.put(k, new Double(v));
					break;
				}
			}
		}
		
		for(String k : keys){
			//	deal with the referenced attributes
			if(k.startsWith("SOMME_") || k.startsWith("MOYENNE_") || k.startsWith("PRODU_") || k.startsWith("DIAMOND_") || k.startsWith("GLOBAL#"))	//	Fonctions
				continue;
			
			if(k.split("\\.").length==1)
				continue;
			String table = k.split("\\.")[0];
			String attribute = k.split("\\.")[1];
			if(table.equals(e.getAttribute().getEntity().getName()))
				continue;
			for(UIControlElement c : controls){
				if(c.getAttribute().getId()==0)
					continue;
				if(c.isReference() ){	
					ProtogenDataEngine engine = new ProtogenDataEngine();
					ApplicationLoader dal = new ApplicationLoader(); 
					String attributetable = c.getAttribute().getDataReference().substring(3);
					String tableName = dal.getEntityFromDR(attributetable);
					
					if(c.isReference() && (c.getControlValue() == null || c.getControlValue().length() == 0))
						if(c.getListReference()!= null && c.getListReference().size()>0){
							c.setControlValue(c.getListReference().get(0).getKey());
							
						}
					
					if(!table.equals(tableName))
						continue;
					
					double v = engine.getReferencedValueForFormula(c.getAttribute(),c.getControlValue(),attribute);
					values.put(k, new Double(v));
					break;
				}
			}
		}
		
		for(String k : keys){
			if(!k.startsWith("SOMME_"))
				continue;
			if(k.split("\\.").length==1)
				continue;
			String rk=k.substring(6);
			String table = rk.split("\\.")[0];
			String attribute = rk.split("\\.")[1];
			
			double somme = 0;
			for(MtmDTO dto : dtos){
				if(dto.getMtmEntity().getName().equals(table)){
					for(Map<CAttribute,Object> datum : dto.getMtmData()){
						for(CAttribute a : datum.keySet())
							if(a.getAttribute().equals(attribute)){
								double v = format(datum.get(a).toString());
								
								somme = somme + v;
								break;
							}
					}
					break;
				}
			}
			values.put(k, new Double(somme));
		}
		
		for(String k : keys){
			if(!k.startsWith("MOYENNE_"))
				continue;
			if(k.split("\\.").length==1)
				continue;
			String rk=k.substring(8);
			String table = rk.split("\\.")[0];
			String attribute = rk.split("\\.")[1];
			
			double somme = 0;
			for(MtmDTO dto : dtos){
				if(dto.getMtmEntity().getName().equals(table)){
					for(Map<CAttribute,Object> datum : dto.getMtmData()){
						for(CAttribute a : datum.keySet())
							if(a.getAttribute().equals(attribute)){
								double v = format(datum.get(a).toString());
								
								somme = somme + v;
								break;
							}
					}
					if(dto.getMtmData().size() >0)
						somme = somme/dto.getMtmData().size();
					break;
				}
			}
			
			values.put(k, new Double(somme));
		}
		
		for(String k : keys){
			if(!k.startsWith("PRODU_"))
				continue;
			if(k.split("\\.").length==1)
				continue;
			String rk=k.substring(6);
			String table = rk.split("\\.")[0];
			String attribute = rk.split("\\.")[1];
			
			double produit = 1;
			for(MtmDTO dto : dtos){
				if(dto.getMtmEntity().getName().equals(table)){
					for(Map<CAttribute,Object> datum : dto.getMtmData()){
						for(CAttribute a : datum.keySet())
							if(a.getAttribute().equals(attribute)){
								double v = format(datum.get(a).toString());
								
								produit = produit * v;
								break;
							}
					}
					break;
				}
			}
			values.put(k, new Double(produit));
		}
		
		
		for(String k : keys){
			if(!k.startsWith("DIAMOND_"))
				continue;
			
			// DIAMOND_tableval.valeur_tabler1_tabler2
			
			
		}
		
		for(String k : keys){
			if(!k.startsWith("GLOBAL#"))
				continue;
			String globalKey = k.split("#")[1];
			ProtogenDataEngine engine = new ProtogenDataEngine();
			double value = engine.getGlobalValue(globalKey,appKey);
			values.put(k, value);
		}
		
		//	Clean code from headers
		code = code.substring(code.lastIndexOf('"')+1);
		
		double result=0;
		for(String k : values.keySet()){
			Double d = values.get(k);
			code = code.replaceAll("<<"+k+">>", d.doubleValue()+"");
		}
		
		if(code.contains("#!GQL")){
			List<String> scripts = new ArrayList<String>();
			while(code.contains("#!GQL")){
				int start = code.indexOf("#!GQL");
				int end = code.indexOf("#!FIN");
				String sc = code.substring(start,end);
				sc = sc.replaceAll("#!GQL", "");
				sc = sc.replaceAll("#!FIN", "");
				
				scripts.add(sc);
				code = code.substring(end);
				code = code.replaceFirst("#!FIN", "");
			}
			for(String s : scripts){
				s = s.replaceFirst("\r", "");
				s = s.replaceFirst("\n", "");
				GqlEngine engine = new GqlEngine();
				String operation = "";
				List<String> lignes = new ArrayList<String>();
				try {
					lignes = IOUtils.readLines(new ByteArrayInputStream(s.getBytes()));
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				for(String l : lignes){
					if(l.split(":")[0].trim().replaceAll("\t", "").equals("VARIABLE")){
						operation = l.split(":")[1].trim().replaceAll("\t", "");
					}
				}
				GQLDataResult res = engine.generate(s);
				if(!res.isDictionnaryMode()){
					double val = res.getSingleValue();
					code = code.replaceAll("#!"+operation+"!#", val+"");
				} else {
					for(String k : res.getValues().keySet()){
						code = code.replaceAll("#!"+operation+":"+k+"!#", res.getValues().get(k).toString()+"");
					}
				}
			}
			
		}
		
		//	construct the temporary file
		Random rand = new Random(15);
		String filename = srv+"/tmp/protolangtemp-"+rand.nextInt()+".pgl";//"./tmp/protolangtemp-"+rand.nextInt()+".pgl";
		
		java.io.File file = new java.io.File(filename);
		file.createNewFile();
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
		writer.write(code);
		writer.close();
		
		
		
		//	Compile and run
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(compilerPath+" "+filename);
		BufferedReader in = new BufferedReader(
	               new InputStreamReader(process.getInputStream()) );
		String output = "";
		while((output = in.readLine()) != null){
			result = Double.parseDouble(output);
		}
		
		// Delete temporary file
		file = new java.io.File(filename);
		if(file.exists()){
			file.delete();
		}else
			throw new Exception("Can not access temporary file : "+filename);
		
		return result;
}

}
