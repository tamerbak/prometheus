package fr.protogen.engine.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.DataRow;

@SuppressWarnings("serial")
public class InstructionsUnit implements Serializable {
	private Map<String, String> inputVars;
	private Map<String, String> outputVars;
	private DataModel dataModel;
	private String sdataModel;
	private String instructions;
	
	public static final String sep = "=-=-";
	
	public InstructionsUnit(String model){
		String temp = model.replaceAll("\n", "").replaceAll("\r", "");
		int sepIndex = temp.indexOf(sep);
		String inVar = temp.substring(0, sepIndex);
		temp = temp.substring(sepIndex+4);
		sepIndex = temp.indexOf(sep);
		String ouVar = temp.substring(0, sepIndex-1);
		temp = temp.substring(sepIndex+4);
		sepIndex = temp.indexOf(sep);
		setSdataModel(temp.substring(0, sepIndex));
		temp = temp.substring(sepIndex+4);
		
		inputVars = new HashMap<String, String>();
		for(String couple : inVar.split(";"))
			inputVars.put(couple.split(":")[0], couple.split(":")[1]);
		
		outputVars = new HashMap<String, String>();
		for(String couple : ouVar.split(";"))
			outputVars.put(couple.split(":")[0], couple.split(":")[1]);
		
		
		instructions = temp;
	}
	
	public void parse(){
		try{
			dataModel = (DataModel)(new XStream()).fromXML(sdataModel);
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public String processInstructions(String outv) {
		/*
		 * 	INSERT
		 */
		if(instructions.equals("liste"))
			return outv;
		if(instructions.equals("unique"))
			return outv.split(",")[0];
		
		/*
		 * SELECT
		 */
		if(instructions.equals("flux"))
			return outv;
		if(instructions.startsWith("VALS")){
			DataModel dm = (DataModel)(new XStream()).fromXML(outv);
			String output = "";
			String field = instructions.split(":")[1];
			for(DataRow r : dm.getRows()){
				for(DataEntry e : r.getDataRow())
					if(e.getAttributeReference().equals(field)){
						output = output+","+e.getValue();
						break;
					}
			}
			
			if(output.length()>1)
				output = output.substring(1);
			return output;
		}
		
		return outv;
	}
	
	public Map<String, String> getInputVars() {
		return inputVars;
	}
	public void setInputVars(Map<String, String> inputVars) {
		this.inputVars = inputVars;
	}
	public Map<String, String> getOutputVars() {
		return outputVars;
	}
	public void setOutputVars(Map<String, String> outputVars) {
		this.outputVars = outputVars;
	}
	public DataModel getDataModel() {
		return dataModel;
	}
	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	public String getInstructions() {
		return instructions;
	}
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getSdataModel() {
		return sdataModel;
	}

	public void setSdataModel(String sdataModel) {
		this.sdataModel = sdataModel;
	}

	
}
