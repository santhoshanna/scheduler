package com.jci.job.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.http.ResponseEntity;

import com.jci.job.domain.ItemApigeeGet;
import com.jci.job.domain.POApigeeGet;
import com.jci.job.domain.SupplierApigeeGet;
import com.microsoft.azure.storage.StorageException;

public interface ScheduledJobService {

	ResponseEntity<POApigeeGet> getPO(String url, String erp, String region, String plant, String ordernumber,
			String ordercreationdate) throws JsonGenerationException, JsonMappingException, IOException,
			InvalidKeyException, URISyntaxException, StorageException;

	ResponseEntity<SupplierApigeeGet> getSupplier(String url, String erp, String region, String plant,
			String suppliername) throws JsonGenerationException, JsonMappingException, IOException;

	ResponseEntity<ItemApigeeGet> getItem(String url, String erp, String region, String plant, String itemnumber)
			throws JsonGenerationException, JsonMappingException, IOException;

}
