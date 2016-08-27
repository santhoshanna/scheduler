package com.jci.job.service;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.http.ResponseEntity;

import com.jci.job.domain.POResponse;

public interface ScheduledJobService {

	ResponseEntity<POResponse> getPO(String url, String erp, String region, String plant, String ordernumber,
			String ordercreationdate) throws JsonGenerationException, JsonMappingException, IOException;

	String getSupplier(String url, String erp, String region, String plant, String suppliername);

	String getItem(String url, String erp, String region, String plant, String itemnumber);

}
