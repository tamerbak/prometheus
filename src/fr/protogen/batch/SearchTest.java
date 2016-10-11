package fr.protogen.batch;

import java.util.List;

import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.dataload.SearchEngine;
import fr.protogen.engine.utils.EntityDTO;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.DAO.ApplicationLoader;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public class SearchTest {

	public static void main(String[] args) {
		SearchEngine engine = new SearchEngine();
		ApplicationLoader dal = new ApplicationLoader();
		CBusinessClass target = dal.getEntity("user_salarie");
		ProtogenDataEngine pde = new ProtogenDataEngine();
		List<Integer> ids = engine.search(target, "java paris");
		 
		for(int id : ids){
			System.out.print(pde.getDataByID("user_salarie", "nom", ""+id));
			System.out.println(" "+pde.getDataByID("user_salarie", "prenom", ""+id));
		}
	}

}
