package fr.protogen.masterdata.services;

import fr.protogen.masterdata.model.ExportMap;

public interface ExportDriverAlgorithm {
	void importData(byte[] inputData, ExportMap map);
	byte[] exportData(int[] selectedRows, ExportMap map);
}
