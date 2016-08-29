package com.jci.job.dao;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;

import com.jci.job.domain.ItemApigeePut;
import com.jci.job.domain.POApigeePut;
import com.jci.job.domain.SupplierApigeePut;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.TableEntity;

public interface ScheduledJobDao {

	POApigeePut batchPOAndPOItemTransaction(HashMap<String, List<TableEntity>> tableEntitiesInsertionMap)
			throws InvalidKeyException, URISyntaxException, StorageException;

	SupplierApigeePut batchSupplierTransaction(List<TableEntity> tableEntities);

	ItemApigeePut batchItemTransaction(List<TableEntity> batchInsertionForItem);
}
