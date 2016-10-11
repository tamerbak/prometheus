package fr.protogen.masterdata.services;

import java.util.List;

import fr.protogen.engine.utils.ListKV;
import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.model.CAttribute;
import fr.protogen.masterdata.model.CBusinessClass;

public interface ValidationPostAction {
	void executePostAction(int dbID, CAttribute attribute, CBusinessClass entity, String formula, String arguments, ListKV row, List<PairKVElement> titles);
}
