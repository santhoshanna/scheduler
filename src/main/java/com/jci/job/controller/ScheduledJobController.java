/**
 * 
 */
package com.jci.job.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jci.job.domain.ItemApigeeGet;
import com.jci.job.domain.POApigeeGet;
import com.jci.job.domain.SupplierApigeeGet;
import com.jci.job.service.ScheduledJobService;
import com.microsoft.azure.storage.StorageException;

@Configuration
@RestController
public class ScheduledJobController {

	@Value("${apigee.url.erp}")
	private String erp;

	@Value("${apigee.url.region}")
	private String region;

	@Value("${apigee.url.plant}")
	private String plant;

	@Value("${apigee.url.po.ordernumber}")
	private String ordernumber;

	@Value("${apigee.url.po.ordercreationdate}")
	private String ordercreationdate;

	@Value("${apigee.url.po.pourl}")
	private String pourl;

	@Value("${apigee.url.po.podelayinms}")
	private long podelayinms;

	@Value("${apigee.url.supplier.supplierurl}")
	private String supplierurl;

	@Value("${apigee.url.supplier.suppliername}")
	private String suppliername;

	@Value("${apigee.url.supplier.supplierdelayinms}")
	private long supplierdelayinms;

	@Value("${apigee.url.item.itemurl}")
	private String itemurl;

	@Value("${apigee.url.item.itemnumber}")
	private String itemnumber;

	@Value("${apigee.url.item.itemdelayinms}")
	private long itemdelayinms;

	@Autowired
	ScheduledJobService scheduledJobService;
	private static final Logger LOG = LoggerFactory.getLogger(ScheduledJobController.class);

	@Scheduled(fixedDelay = 600000)
	@RequestMapping(value = "/getPO", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<POApigeeGet> getPO() throws JsonGenerationException, JsonMappingException,
			IOException, InvalidKeyException, URISyntaxException, StorageException {
		LOG.info("### Starting ScheduledJobController.getPoDetails ####");
		// PoRequest request = new PoRequest();
		ResponseEntity<POApigeeGet> response = scheduledJobService.getPO(pourl, erp, region, plant, ordernumber,
				ordercreationdate);
		LOG.info("response-->" + response);
		LOG.info("### Ending ScheduledJobController.getPoDetails ####");
		return response;
	}

	//@Scheduled(fixedDelay = 600000)
	@RequestMapping(value = "/getSupplier", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<SupplierApigeeGet> getSupplier()
			throws JsonGenerationException, JsonMappingException, IOException {
		LOG.info("### Starting ScheduledJobController.getSupplier ####");
		// PoRequest request = new PoRequest();
		ResponseEntity<SupplierApigeeGet> response = scheduledJobService.getSupplier(supplierurl, erp, region, plant,
				suppliername);
		LOG.info("response-->" + response);
		LOG.info("### Ending ScheduledJobController.getSupplier ####");
		return response;
	}

	//@Scheduled(fixedDelay = 600000)
	@RequestMapping(value = "/getItem", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<ItemApigeeGet> getItem()
			throws JsonGenerationException, JsonMappingException, IOException {
		LOG.info("### Starting ScheduledJobController.getItem ####");
		// PoRequest request = new PoRequest();
		ResponseEntity<ItemApigeeGet> response = scheduledJobService.getItem(itemurl, erp, region, plant, itemnumber);
		LOG.info("response-->" + response);
		LOG.info("### Ending ScheduledJobController.getItem ####");
		return response;
	}

}
