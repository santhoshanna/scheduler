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

import com.jci.job.domain.Item;
import com.jci.job.domain.ItemApigeeGet;
import com.jci.job.domain.ItemTableEntity;
import com.jci.job.domain.PO;
import com.jci.job.domain.POItemTableEntity;
import com.jci.job.domain.POApigeeGet;
import com.jci.job.domain.POItem;
import com.jci.job.domain.POTableEntity;
import com.jci.job.domain.Supplier;
import com.jci.job.domain.SupplierApigeeGet;
import com.jci.job.domain.SupplierTableEntity;
import com.microsoft.azure.storage.table.TableEntity;

@Configuration
public class ScheduledJobEntityPrepareServiceImpl implements ScheduledJobEntityPrepareService {

	@Value("${azure.storage.potablename}")
	private String poTableName;

	@Value("${azure.storage.poitemtablename}")
	private String poItemTableName;

	@Value("${azure.storage.partionkey.po}")
	private String poTablePartionKey;

	@Value("${azure.storage.partionkey.po_item}")
	private String poItemTablePartionKey;

	@Value("${azure.storage.suppliertablename}")
	private String supplierTableName;

	@Value("${azure.storage.partionkey.supplier}")
	private String supplierTablePartionKey;

	@Value("${azure.storage.itemtablename}")
	private String itemTableName;

	@Value("${azure.storage.partionkey.item}")
	private String itemTablePartionKey;

	@SuppressWarnings("unchecked")
	public HashMap<String, List<TableEntity>> poAndPOItemBatchInsertPrep(POApigeeGet poResponse, String erp,
			String region, String plant) throws JsonGenerationException, JsonMappingException, IOException {

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
			poEntity.setSupplierDeliveryState(1);
			List<POItem> poItemList = po.getItemList();

			for (POItem poItem : poItemList) {// loop2 po item

				POItemTableEntity poItemEntity = new POItemTableEntity(poItemTablePartionKey,
						poItem.getOrderNumber() + "_" + poItem.getRequestID() + "_" + poItem.getLineID());
				poItemEntity.setOrderNumber(poItem.getOrderNumber());
				poItemEntity.setRequestID(poItem.getRequestID());
				poItemEntity.setLineID(poItem.getLineID());
				HashMap<String, String> hm = (HashMap<String, String>) poItem.getItem();
				ObjectMapper mapper = new ObjectMapper();
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

	@SuppressWarnings("unchecked")
	@Override
	public List<TableEntity> supplierBatchInsertPrep(SupplierApigeeGet supplierResponse, String erp, String region,
			String plant) throws JsonGenerationException, JsonMappingException, IOException {
		List<Supplier> supplierList = supplierResponse.getSupplierList();
		List<TableEntity> supplierTableEntityList = new ArrayList<TableEntity>();

		for (Supplier supplier : supplierList) {// loop1 supplier

			SupplierTableEntity supplierEntity = new SupplierTableEntity(supplierTablePartionKey,
					supplier.getSupplierID());
			HashMap<String, String> hm = (HashMap<String, String>) supplier.getSupplier();
			ObjectMapper mapper = new ObjectMapper();
			supplierEntity.setSupplierID(supplier.getSupplierID());
			supplierEntity.setSupplierJsonString(mapper.writeValueAsString(hm));
			supplierTableEntityList.add(supplierEntity);
		}
		return supplierTableEntityList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TableEntity> itemBatchInsertPrep(ItemApigeeGet itemResponse, String erp, String region, String plant)
			throws JsonGenerationException, JsonMappingException, IOException {
		List<Item> itemList = itemResponse.getItemList();
		List<TableEntity> itemTableEntityList = new ArrayList<TableEntity>();

		for (Item item : itemList) {// loop1 supplier

			ItemTableEntity itemEntity = new ItemTableEntity(itemTablePartionKey,
					item.getSupplierID() + "_" + item.getCustomerItemID());
			HashMap<String, String> hm = (HashMap<String, String>) item.getItem();
			ObjectMapper mapper = new ObjectMapper();
			itemEntity.setSupplierID(item.getSupplierID());
			itemEntity.setCustomerItemID(item.getCustomerItemID());
			itemEntity.setItemJsonString(mapper.writeValueAsString(hm));
			itemTableEntityList.add(itemEntity);
		}
		return itemTableEntityList;
	}

}
