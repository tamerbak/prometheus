package fr.protogen.event.aspect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import fr.protogen.dataload.DataDefinitionOperation;
import fr.protogen.dataload.ProtogenDataEngine;
import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.event.geb.GeneriumEventBus;
import fr.protogen.event.geb.EventModel.GDataEvent;
import fr.protogen.event.geb.EventModel.GEvent;
import fr.protogen.masterdata.model.CBusinessClass;
import fr.protogen.masterdata.model.CWindow;

public aspect DataAccessAspect {
	pointcut dataUpdate(ProtogenDataEngine engine) : execution (* ProtogenDataEngine.executeSaveAction(..))
		&& target(engine);
	pointcut dataInsert(ProtogenDataEngine engine) : execution (* ProtogenDataEngine.executeInsertAction(..))
		&& target(engine);
	pointcut dataDelete(ProtogenDataEngine engine) : execution (* ProtogenDataEngine.deleteRow(..))
		&& target(engine);

	after(ProtogenDataEngine engine) : dataUpdate(engine){
		CBusinessClass entity = engine.getEntity();
		DataDefinitionOperation operation = engine.getOperation();
		int dbID = engine.getDbID();
		CWindow window = engine.getWindow();
		
		DataAccessAlgorithms algos = new DataAccessAlgorithms();
		//	Table dependant
		algos.notifyTableDependant(window, entity, dbID, engine, operation);
		
		//	Row dependant
		algos.notifyRowDependant(window, entity, dbID, engine, operation);
		
	}
	
	after(ProtogenDataEngine engine) : dataInsert(engine){
		CBusinessClass entity = engine.getEntity();
		DataDefinitionOperation operation = engine.getOperation();
		int dbID = engine.getDbID();
		CWindow window = engine.getWindow();
		
		DataAccessAlgorithms algos = new DataAccessAlgorithms();
		//	Table dependant
		algos.notifyTableDependant(window, entity, dbID, engine, operation);
		
		//	Row dependant
		algos.notifyRowDependant(window, entity, dbID, engine, operation);
		
	}
	
	after(ProtogenDataEngine engine) : dataDelete(engine){
		CBusinessClass entity = engine.getEntity();
		DataDefinitionOperation operation = engine.getOperation();
		int dbID = engine.getDbID();
		CWindow window = engine.getWindow();
		
		DataAccessAlgorithms algos = new DataAccessAlgorithms();
		//	Table dependant
		algos.notifyTableDependant(window, entity, dbID, engine, operation);
		
		//	Row dependant
		algos.notifyRowDependant(window, entity, dbID, engine, operation);
		
	}
}
