package fr.protogen.export.engine;

import java.util.List;

import fr.protogen.engine.utils.PairKVElement;

public interface DataExportEngine {
	String serializeData(List<PairKVElement> data);
}
