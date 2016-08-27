/**
 * 
 */
package com.jci.job.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.jci.job.dao.ScheduledJobDao;
import com.jci.job.domain.POResponse;
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
	private POStorageService poStorageService;

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
	public ResponseEntity<POResponse> getPO(String url, String erp, String region, String plant, String ordernumber,
			String ordercreationdate) throws JsonGenerationException, JsonMappingException, IOException {
		LOG.info("### Starting ScheduledJobServiceImpl.getPoDetails ####");
		ResponseEntity<POResponse> response = null;

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
					new ParameterizedTypeReference<POResponse>() {
					});
			/*
			 * System.out.println("rateResponse: " + response);
			 * System.out.println("poResponse: " + poResponse);
			 * System.out.println("poResponse code: " + poResponse.getCode());
			 * System.out.println("poResponse GR Number: " +
			 * poResponse.getPoList().get(0).getGrNumber());
			 */

			/*
			 * HashMap<String, String> hm = (HashMap<String, String>)
			 * poResponse.getPoList().get(0).getItemList().get(0); ObjectMapper
			 * mapper = new ObjectMapper(); String result =
			 * mapper.writeValueAsString(hm); System.out.println(result);
			 */

		} catch (Exception e) {
			LOG.info("Exception: " + e);
		}

		POResponse poResponse = response.getBody();
		HashMap<String, List<TableEntity>> batchInsertionForPOAndPOItem = poStorageService.poBatch(poResponse, erp,
				region, plant);
		LOG.info("req-->" + batchInsertionForPOAndPOItem);

		// for (BatchInsertReq request : batchInsertionForPOAndPOItem) {
		// BatchInsertRes res =
		scheduledJobDao.batchPOAndPOItemTransaction(batchInsertionForPOAndPOItem);
		// LOG.info("res--->" + res);
		// }

		LOG.info("### Ending ScheduledJobServiceImpl.getPoDetails ####");
		return response;
	}

	public POResponse getPODetailsFallback() {
		LOG.info("### Starting ApigeeClientService.getPoDetailsFallback ####");
		ResponseEntity<POResponse> response = new ResponseEntity<POResponse>(null);
		/*
		 * poFeignApigeeClientService.getPO(erp, region, plant, ordernumber,
		 * ordercreationdate);
		 */
		POResponse responseBody = response.getBody();
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
	public String getSupplier(String url, String erp, String region, String plant, String suppliername) {
		Object object;
		String jsonString = null;
		try {
			JSONParser parser = new JSONParser();
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("erp", erp);
			params.add("region", region);
			params.add("plant", plant);
			params.add("suppliername", suppliername);
			// URL apigeeURL = new URL(url);
			UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParams(params).build();
			URL apigeeSupplierURL = new URL(uriComponents.toUriString());
			object = parser.parse(new InputStreamReader(apigeeSupplierURL.openStream()));
			jsonString = object.toString();
			LOG.info("***********************");
			LOG.info("apigeeURL: " + apigeeSupplierURL);
			LOG.info("JSON String: " + jsonString);
			LOG.info("***********************");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
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
	public String getItem(String url, String erp, String region, String plant, String itemnumber) {
		Object object;
		String jsonString = null;
		try {
			JSONParser parser = new JSONParser();
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("erp", erp);
			params.add("region", region);
			params.add("plant", plant);
			params.add("itemnumber", itemnumber);
			// URL apigeeURL = new URL(url);
			UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParams(params).build();
			URL apigeeItemURL = new URL(uriComponents.toUriString());
			object = parser.parse(new InputStreamReader(apigeeItemURL.openStream()));
			jsonString = object.toString();
			LOG.info("***********************");
			LOG.info("apigeeURL: " + apigeeItemURL);
			LOG.info("JSON String: " + jsonString);
			LOG.info("***********************");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

}
