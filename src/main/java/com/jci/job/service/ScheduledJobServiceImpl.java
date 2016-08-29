/**
 * 
 */
package com.jci.job.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.jci.job.dao.ScheduledJobDao;
import com.jci.job.domain.ItemApigeeGet;
import com.jci.job.domain.ItemApigeePut;
import com.jci.job.domain.POApigeeGet;
import com.jci.job.domain.POApigeePut;
import com.jci.job.domain.SupplierApigeeGet;
import com.jci.job.domain.SupplierApigeePut;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.TableEntity;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service
@Configuration
public class ScheduledJobServiceImpl implements ScheduledJobService {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledJobServiceImpl.class);

	@Autowired
	private ScheduledJobDao scheduledJobDao;

	@Autowired
	private ScheduledJobEntityPrepareService scheduledJobEntityPrepareService;

	@Value("${apigee.url.po.poputurl}")
	private String poPUTUrl;
	
	@Value("${apigee.url.supplier.supplierputurl}")
	private String supplierPUTUrl;
	
	@Value("${apigee.url.item.itemputurl}")
	private String itemPUTUrl;

	RestTemplate restTemplate = new RestTemplate();

	@HystrixCommand(commandProperties = { @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),
			@HystrixProperty(name = "execution.timeout.enabled", value = "true"),
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "900000"),
			@HystrixProperty(name = "execution.isolation.thread.interruptOnTimeout", value = "true"),
			@HystrixProperty(name = "fallback.enabled", value = "true"),
			@HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "1000"),
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "10"),
			@HystrixProperty(name = "circuitBreaker.forceOpen", value = "false"),
			@HystrixProperty(name = "circuitBreaker.forceClosed", value = "false") }, fallbackMethod = "getPODetailsFallback")
	@Override
	public ResponseEntity<POApigeeGet> getPO(String url, String erp, String region, String plant, String ordernumber,
			String ordercreationdate) throws JsonGenerationException, JsonMappingException, IOException,
			InvalidKeyException, URISyntaxException, StorageException {
		LOG.info("### Starting ScheduledJobServiceImpl.getPoDetails ####");
		ResponseEntity<POApigeeGet> response = null;

		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("erp", erp);
			params.add("region", region);
			params.add("plant", plant);
			params.add("ordernumber", ordernumber);
			params.add("ordercreationdate", ordercreationdate);
			UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParams(params).build();
			URL apigeePOURL = new URL(uriComponents.toUriString());

			response = restTemplate.exchange(apigeePOURL.toString(), HttpMethod.GET, null,
					new ParameterizedTypeReference<POApigeeGet>() {
					});

		} catch (Exception e) {
			LOG.error("Exception in ScheduledJobServiceImpl.getPoDetails while getting PO data from APIGEE: " + e);
		}

		POApigeeGet poResponse = response.getBody();
		HashMap<String, List<TableEntity>> batchInsertionForPOAndPOItem = scheduledJobEntityPrepareService
				.poAndPOItemBatchInsertPrep(poResponse, erp, region, plant);
		LOG.info("req-->" + batchInsertionForPOAndPOItem);

		POApigeePut responseBackToApigee = scheduledJobDao.batchPOAndPOItemTransaction(batchInsertionForPOAndPOItem);
		if (responseBackToApigee.getPoList().size() != 0) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(responseBackToApigee);
			LOG.info("json" + json);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPut request = new HttpPut(poPUTUrl);
			StringEntity params = new StringEntity(json);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			httpClient.execute(request);

			/*
			 * HttpEntity<POApigeePut> entity = new HttpEntity<POApigeePut>();
			 * restTemplate.put(url, entity); restTemplate.exchange(url,
			 * HttpMethod.PUT, null,);
			 */
		}
		// LOG.info("res--->" + res);
		// }

		LOG.info("### Ending ScheduledJobServiceImpl.getPoDetails ####");
		return response;
	}

	public POApigeeGet getPODetailsFallback() {
		LOG.info("### Starting ApigeeClientService.getPoDetailsFallback ####");
		ResponseEntity<POApigeeGet> response = new ResponseEntity<POApigeeGet>(null);
		POApigeeGet responseBody = response.getBody();
		responseBody.setFallbackErrorMessage("Get Po Details Fallback call, seems PO API service is down");
		LOG.info("### Ending ApigeeClientService.getPoDetailsFallback ####" + responseBody);
		return responseBody;
	}

	@HystrixCommand(commandProperties = { @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),
			@HystrixProperty(name = "execution.timeout.enabled", value = "true"),
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "900000"),
			@HystrixProperty(name = "execution.isolation.thread.interruptOnTimeout", value = "true"),
			@HystrixProperty(name = "fallback.enabled", value = "true"),
			@HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "1000"),
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "10"),
			@HystrixProperty(name = "circuitBreaker.forceOpen", value = "false"),
			@HystrixProperty(name = "circuitBreaker.forceClosed", value = "false") }, fallbackMethod = "getPODetailsFallback")
	@Override
	public ResponseEntity<SupplierApigeeGet> getSupplier(String url, String erp, String region, String plant,
			String suppliername) throws JsonGenerationException, JsonMappingException, IOException {
		ResponseEntity<SupplierApigeeGet> response = null;

		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("erp", erp);
			params.add("region", region);
			params.add("plant", plant);
			params.add("suppliername", suppliername);
			UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParams(params).build();
			URL apigeeSupplierURL = new URL(uriComponents.toUriString());

			response = restTemplate.exchange(apigeeSupplierURL.toString(), HttpMethod.GET, null,
					new ParameterizedTypeReference<SupplierApigeeGet>() {
					});
			LOG.info("list : " + response.getBody().getSupplierList());
		} catch (Exception e) {
			LOG.error("Exception in ScheduledJobServiceImpl.getSupplier while getting Supplier data from APIGEE: " + e);
		}

		SupplierApigeeGet supplierResponse = response.getBody();
		List<TableEntity> batchInsertionForSupplier = scheduledJobEntityPrepareService
				.supplierBatchInsertPrep(supplierResponse, erp, region, plant);
		LOG.info("req-->" + batchInsertionForSupplier);
		SupplierApigeePut responseBackToApigee = scheduledJobDao.batchSupplierTransaction(batchInsertionForSupplier);
		if (responseBackToApigee.getSupplierList().size() != 0) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(responseBackToApigee);
			LOG.info("json" + json);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPut request = new HttpPut(poPUTUrl);
			StringEntity params = new StringEntity(json);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			httpClient.execute(request);
		}
		return response;
	}

	@HystrixCommand(commandProperties = { @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),
			@HystrixProperty(name = "execution.timeout.enabled", value = "true"),
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "900000"),
			@HystrixProperty(name = "execution.isolation.thread.interruptOnTimeout", value = "true"),
			@HystrixProperty(name = "fallback.enabled", value = "true"),
			@HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "1000"),
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "10"),
			@HystrixProperty(name = "circuitBreaker.forceOpen", value = "false"),
			@HystrixProperty(name = "circuitBreaker.forceClosed", value = "false") }, fallbackMethod = "getPODetailsFallback")
	@Override
	public ResponseEntity<ItemApigeeGet> getItem(String url, String erp, String region, String plant, String itemnumber)
			throws JsonGenerationException, JsonMappingException, IOException {
		ResponseEntity<ItemApigeeGet> response = null;

		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("erp", erp);
			params.add("region", region);
			params.add("plant", plant);
			params.add("itemnumber", itemnumber);
			UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParams(params).build();
			URL apigeeItemURL = new URL(uriComponents.toUriString());

			response = restTemplate.exchange(apigeeItemURL.toString(), HttpMethod.GET, null,
					new ParameterizedTypeReference<ItemApigeeGet>() {
					});
			LOG.info("list : " + response.getBody().getItemList());
		} catch (Exception e) {
			LOG.error("Exception in ScheduledJobServiceImpl.getItem while getting Item data from APIGEE: " + e);
		}

		ItemApigeeGet itemResponse = response.getBody();
		List<TableEntity> batchInsertionForItem = scheduledJobEntityPrepareService.itemBatchInsertPrep(itemResponse,
				erp, region, plant);
		LOG.info("req-->" + batchInsertionForItem);
		ItemApigeePut responseBackToApigee = scheduledJobDao.batchItemTransaction(batchInsertionForItem);
		return response;
	}

}
