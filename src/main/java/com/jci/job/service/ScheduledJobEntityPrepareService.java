package com.jci.job.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.jci.job.domain.ItemApigeeGet;
import com.jci.job.domain.POApigeeGet;
import com.jci.job.domain.SupplierApigeeGet;
import com.microsoft.azure.storage.table.TableEntity;

public interface ScheduledJobEntityPrepareService {

	HashMap<String, List<TableEntity>> poAndPOItemBatchInsertPrep(POApigeeGet responseBody, String erp, String region, String plant) throws JsonGenerationException, JsonMappingException, IOException;
	List<TableEntity> supplierBatchInsertPrep(SupplierApigeeGet responseBody, String erp, String region, String plant) throws JsonGenerationException, JsonMappingException, IOException;
	List<TableEntity> itemBatchInsertPrep(ItemApigeeGet itemResponse, String erp, String region, String plant) throws JsonGenerationException, JsonMappingException, IOException;
}
