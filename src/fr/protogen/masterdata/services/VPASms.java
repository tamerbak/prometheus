package fr.protogen.masterdata.services;

import java.io.IOException;
import java.util.List;

import fr.protogen.communication.client.SmsClient;
import fr.protogen.engine.utils.ListKV;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class VPASms implements ValidationPostAction {

	@Override
	public void executePostAction(int dbID, CAttribute attribute, CBusinessClass entity, String formula,
			String arguments, ListKV row,List<PairKVElement> titles) {
		
		String[] args = arguments.split(",");
		String num = "";
		for(int j = 0 ; j < args.length ; j++){
			String a = args[j];
			for(int i = 0 ; i < titles.size() ; i++){
				String t = titles.get(i).getValue();
				if(t.equals(a)){
					formula = formula.replaceAll("<<"+a+">>", row.getValue().get(i));
					if(j == 0)
						num = row.getValue().get(i);
				}
				
			}
			
		}
		
		try {
			SmsClient.getInstance().sendSMS(formula, num);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
