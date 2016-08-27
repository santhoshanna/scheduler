package com.jci.job.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.jci.job.domain.PO;
import com.jci.job.domain.POItemTableEntity;
import com.jci.job.domain.POResponse;
import com.jci.job.domain.POTableEntity;
import com.microsoft.azure.storage.table.TableEntity;

@Configuration
public class POStorageServiceImpl implements POStorageService {

	@Value("${azure.storage.potablename}")
	private String poTableName;

	@Value("${azure.storage.poitemtablename}")
	private String poItemTableName;

	@Value("${azure.storage.partionkey.po}")
	private String poTablePartionKey;

	@Value("${azure.storage.partionkey.po_item}")
	private String poItemTablePartionKey;

	@SuppressWarnings("unchecked")
	public HashMap<String, List<TableEntity>> poBatch(POResponse poResponse, String erp, String region, String plant)
			throws JsonGenerationException, JsonMappingException, IOException {

		List<PO> poList = poResponse.getPoList();
		List<TableEntity> poTableEntityList = new ArrayList<TableEntity>();
		List<TableEntity> poItemTableEntityList = new ArrayList<TableEntity>();

		for (PO po : poList) {// loop1 po

			POTableEntity poEntity = new POTableEntity(poTablePartionKey, po.getOrderNumber());
			poEntity.setErp(erp);
			poEntity.setRegion(region);
			poEntity.setPlant(plant);
			poEntity.setOrderNumber(po.getOrderNumber());
			poEntity.setOrderCreationDate(po.getOrderCreationDate());
			poEntity.setSupplierType(po.getSupplierType());
			poEntity.setSupplierDeliveryState("TRANSIT");

			List<Object> poItemList = po.getItemList();
			Integer rowNo = 1;

			for (Object poItem : poItemList) {// loop2 po item

				POItemTableEntity poItemEntity = new POItemTableEntity(
						poItemTablePartionKey + "_" + po.getSupplierType(), po.getOrderNumber() + "_" + rowNo++);

				poItemEntity.setOrderNumber(po.getOrderNumber());
				HashMap<String, String> hm = (HashMap<String, String>) poItem;
				ObjectMapper mapper = new ObjectMapper();
				// String result = mapper.writeValueAsString(hm);
				poItemEntity.setPOItemJsonString(mapper.writeValueAsString(hm));
				poItemTableEntityList.add(poItemEntity);

			}
			poTableEntityList.add(poEntity);
		}
		HashMap<String, List<TableEntity>> tableNameToEntityMap = new HashMap<String, List<TableEntity>>();
		tableNameToEntityMap.put(poTableName, poTableEntityList);
		tableNameToEntityMap.put(poItemTableName, poItemTableEntityList);

		return tableNameToEntityMap;
	}

}
