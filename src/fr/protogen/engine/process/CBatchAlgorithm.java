package fr.protogen.engine.process;

import fr.protogen.masterdata.model.CBatchUnit;

public interface CBatchAlgorithm {
	void executeBatchUnit(CBatchUnit unit);
}
