package com.jci.job.dao;

import java.util.HashMap;
import java.util.List;

import com.jci.job.domain.BatchInsertRes;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableEntity;

public interface ScheduledJobDao {

	BatchInsertRes batchPOAndPOItemTransaction(HashMap<String, List<TableEntity>> tableEntitiesInsertionMap);
	CloudTableClient getTableClientReference();
}
