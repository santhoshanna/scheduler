package com.jci.job.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.jci.job.domain.POResponse;
import com.microsoft.azure.storage.table.TableEntity;

public interface POStorageService {

	HashMap<String, List<TableEntity>> poBatch(POResponse responseBody, String erp, String region, String plant) throws JsonGenerationException, JsonMappingException, IOException;

}
