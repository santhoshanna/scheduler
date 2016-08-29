package com.jci.job.dao;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import com.jci.job.domain.Item;
import com.jci.job.domain.ItemApigeePut;
import com.jci.job.domain.ItemTableEntity;
import com.jci.job.domain.MiscDataTableEntity;
import com.jci.job.domain.PO;
import com.jci.job.domain.POApigeePut;
import com.jci.job.domain.POItemTableEntity;
import com.jci.job.domain.POTableEntity;
import com.jci.job.domain.Supplier;
import com.jci.job.domain.SupplierApigeePut;
import com.jci.job.domain.SupplierTableEntity;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableEntity;
import com.microsoft.azure.storage.table.TableOperation;

@Repository
@Configuration
public class ScheduledJobDaoImpl implements ScheduledJobDao {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledJobDaoImpl.class);

	@Value("${azure.storage.connection.protocol}")
	private String protocol;

	@Value("${azure.storage.connection.accountname}")
	private String accountName;

	@Value("${azure.storage.connection.accountkey}")
	private String accountKey;

	@Value("${azure.storage.connectionstring}")
	private String connectionString;

	@Value("${azure.storage.potablename}")
	private String poTableName;

	@Value("${azure.storage.poitemtablename}")
	private String poItemTableName;

	@Value("${azure.storage.suppliertablename}")
	private String supplierTableName;

	@Value("${azure.storage.itemtablename}")
	private String itemTableName;

	@Value("${azure.storage.miscdatatablename}")
	private String miscDataTableName;

	@Value("${azure.storage.partionkey.miscdata}")
	private String miscDataTablePartionKey;

	@Value("${azure.storage.rowkey.miscdata}")
	private String miscDataTableRowKey;

	private static Integer inTransitCount;

	// private static final String proxy = "10.10.5.18";
	// private static final int port = 8080;

	// InetSocketAddress inetAddr = new InetSocketAddress(proxy, port);
	// Proxy proxyObj = new Proxy(Proxy.Type.HTTP, inetAddr);

	@SuppressWarnings("null")
	private MiscDataTableEntity readMiscDataTableEntity()
			throws InvalidKeyException, URISyntaxException, StorageException {
		// Retrieve storage account from connection-string.
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
		// Create the table client.
		CloudTableClient tableClient = storageAccount.createCloudTableClient();
		LOG.info("Table Name: " + miscDataTableName);
		// LOG.info("Connection String: " + connectionString);
		CloudTable cloudTable = tableClient.getTableReference(miscDataTableName);
		LOG.info("cloudTable: " + cloudTable);
		boolean tableExistsOrNOt = true;
		if (cloudTable == null) {
			tableExistsOrNOt = cloudTable.createIfNotExists();
		}
		if (!tableExistsOrNOt) {
			LOG.error("MiscData Table could not be created" + tableExistsOrNOt);
			return null;
		} else {
			TableOperation entity = TableOperation.retrieve(miscDataTablePartionKey, miscDataTableRowKey,
					MiscDataTableEntity.class);
			return cloudTable.execute(entity).getResultAsType();
		}
	}

	@SuppressWarnings("null")
	private void updateMiscDataTableEntity(MiscDataTableEntity entity)
			throws InvalidKeyException, URISyntaxException, StorageException {
		// Retrieve storage account from connection-string.
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
		// Create the table client.
		CloudTableClient tableClient = storageAccount.createCloudTableClient();
		LOG.info("Table Name: " + miscDataTableName);
		// LOG.info("Connection String: " + connectionString);
		CloudTable cloudTable = tableClient.getTableReference(miscDataTableName);
		boolean tableExistsOrNOt = true;
		if (cloudTable == null) {
			tableExistsOrNOt = cloudTable.createIfNotExists();
		}
		if (tableExistsOrNOt) {
			TableOperation insert = TableOperation.insertOrReplace(entity);
			cloudTable.execute(insert);
		}
	}

	@SuppressWarnings("unused")
	private boolean updateMiscDataTableEntityTransaction(Integer inTransitCount)
			throws InvalidKeyException, URISyntaxException, StorageException {
		MiscDataTableEntity miscEntity = readMiscDataTableEntity();
		if (miscEntity != null) {
			LOG.info("Initial intransit count:" + miscEntity.getIntransitCount());
			LOG.info("Current intransit count:" + inTransitCount);
			miscEntity.setIntransitCount((miscEntity.getIntransitCount() + inTransitCount));
			LOG.info("block 1");
		} else {
			miscEntity = new MiscDataTableEntity(miscDataTablePartionKey, miscDataTableRowKey);
			miscEntity.setIntransitCount(inTransitCount);
			LOG.info("block 2");
		}
		try {
			updateMiscDataTableEntity(miscEntity);
			LOG.info("Summed up intransit count in first block:" + miscEntity.getIntransitCount());
		} catch (InvalidKeyException | URISyntaxException | StorageException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@SuppressWarnings("null")
	private boolean batchInsertPOTransaction(List<TableEntity> entities, String tableName) {

		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			// Create the table client.
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			LOG.info("Table Name: " + tableName);
			// LOG.info("Connection String: " + connectionString);
			// Define a batch operation.
			TableBatchOperation batchPOOperation = new TableBatchOperation();
			// Create a cloud table object for the table.
			// OperationContext opContext = new OperationContext();
			// opContext.setProxy(proxyObj);
			CloudTable cloudTable = tableClient.getTableReference(tableName);
			LOG.info("cloudTable: " + cloudTable);
			boolean tableExistsOrNOt = true;
			if (cloudTable == null) {
				tableExistsOrNOt = cloudTable.createIfNotExists();
			}
			if (!tableExistsOrNOt) {
				LOG.error("PO Table could not be created" + tableExistsOrNOt);
				return false;
			} else {
				for (int i = 0; i < entities.size(); i++) {
					POTableEntity poEntity = (POTableEntity) entities.get(i);
					// Create a customer entity to add to the table.
					batchPOOperation.insertOrReplace(poEntity);
					// LOG.info("batch PO: "+ batchPOOperation.size());
				}
				cloudTable.execute(batchPOOperation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("ERROR While inserting POItems in ScheduledJobDaoImpl.batchInsertPOTransaction():" + e);
			return false;
		}
		return true;
	}

	@SuppressWarnings("null")
	private boolean batchInsertPOItemTransaction(List<TableEntity> entities, String tableName) {

		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			LOG.info("Table Name: " + tableName);
			// LOG.info("Connection String: " + connectionString);
			// Create the table client.
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			// Define a batch operation.
			TableBatchOperation batchPOItemOperation = new TableBatchOperation();
			// Create a cloud table object for the table.
			CloudTable cloudTable = tableClient.getTableReference(tableName);
			LOG.info("cloudTable: " + cloudTable);
			boolean tableExistsOrNOt = true;
			if (cloudTable == null) {
				tableExistsOrNOt = cloudTable.createIfNotExists();
			}
			if (tableExistsOrNOt == false) {
				LOG.error("PO Item Table could not be created" + tableExistsOrNOt);
				return false;
			} else {
				for (int i = 0; i < entities.size(); i++) {
					POItemTableEntity poItemEntity = (POItemTableEntity) entities.get(i);
					// Create a customer entity to add to the table.
					batchPOItemOperation.insertOrReplace(poItemEntity);
					// LOG.info("batch PO: "+ batchPOItemOperation.size());
				}
				cloudTable.execute(batchPOItemOperation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("ERROR While inserting POItems in ScheduledJobDaoImpl.batchInsertPOItemTransaction(): " + e);
			return false;
		}
		return true;
	}

	@SuppressWarnings("null")
	private boolean batchInsertSupplierTransaction(List<TableEntity> entities, String tableName) {

		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			LOG.info("Table Name: " + tableName);
			// LOG.info("Connection String: " + connectionString);
			// Create the table client.
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			// Define a batch operation.
			TableBatchOperation batchSupplierOperation = new TableBatchOperation();
			// Create a cloud table object for the table.
			CloudTable cloudTable = tableClient.getTableReference(tableName);
			LOG.info("cloudTable: " + cloudTable);
			boolean tableExistsOrNOt = true;
			if (cloudTable == null) {
				tableExistsOrNOt = cloudTable.createIfNotExists();
			}
			if (tableExistsOrNOt == false) {
				LOG.error("Supplier Table could not be created" + tableExistsOrNOt);
				return false;
			} else {
				for (int i = 0; i < entities.size(); i++) {
					SupplierTableEntity supplierItemEntity = (SupplierTableEntity) entities.get(i);
					// Create a customer entity to add to the table.
					batchSupplierOperation.insertOrReplace(supplierItemEntity);
					// LOG.info("batch PO: "+ batchPOItemOperation.size());
				}
				cloudTable.execute(batchSupplierOperation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("ERROR While inserting Supplier in ScheduledJobDaoImpl.batchInsertSupplierTransaction(): " + e);
			return false;
		}
		return true;
	}

	@SuppressWarnings("null")
	private boolean batchInsertItemTransaction(List<TableEntity> entities, String tableName) {

		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			LOG.info("Table Name: " + tableName);
			// LOG.info("Connection String: " + connectionString);
			// Create the table client.
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			// Define a batch operation.
			TableBatchOperation batchItemOperation = new TableBatchOperation();
			// Create a cloud table object for the table.
			CloudTable cloudTable = tableClient.getTableReference(tableName);
			LOG.info("cloudTable: " + cloudTable);
			boolean tableExistsOrNOt = true;
			if (cloudTable == null) {
				tableExistsOrNOt = cloudTable.createIfNotExists();
			}
			if (tableExistsOrNOt == false) {
				LOG.error("Item Table could not be created" + tableExistsOrNOt);
				return false;
			} else {
				for (int i = 0; i < entities.size(); i++) {
					ItemTableEntity itemItemEntity = (ItemTableEntity) entities.get(i);
					// Create a customer entity to add to the table.
					batchItemOperation.insertOrReplace(itemItemEntity);
				}
				cloudTable.execute(batchItemOperation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("ERROR While inserting Item in ScheduledJobDaoImpl.batchInsertItemTransaction(): " + e);
			return false;
		}
		return true;
	}

	private boolean deletePOTransaction(List<TableEntity> entities, String tableName) {

		try {

			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			CloudTable cloudTable = tableClient.getTableReference(tableName);
			for (int i = 0; i < entities.size(); i++) {
				TableOperation retrievePO = TableOperation.retrieve(entities.get(i).getPartitionKey(),
						entities.get(i).getRowKey(), POTableEntity.class);
				POTableEntity entityPO = cloudTable.execute(retrievePO).getResultAsType();
				TableOperation deletePO = TableOperation.delete(entityPO);
				cloudTable.execute(deletePO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(
					"ERROR While deleting failed transactions in POItems in ScheduledJobDaoImpl.batchInsertPOTransaction():"
							+ e);
			return false;
		}
		return true;
	}

	@Override
	public POApigeePut batchPOAndPOItemTransaction(HashMap<String, List<TableEntity>> tableEntitiesInsertionMap)
			throws InvalidKeyException, URISyntaxException, StorageException {
		LOG.info("#### Starting ScheduledJobDaoImpl.batchPOAndPOItemInsert ###" + tableEntitiesInsertionMap);
		POApigeePut poConfirmation = new POApigeePut();
		LOG.info("Size of the map is: " + tableEntitiesInsertionMap.entrySet().size());
		if (batchInsertPOTransaction(tableEntitiesInsertionMap.get(poTableName), poTableName)) {
			if (batchInsertPOItemTransaction(tableEntitiesInsertionMap.get(poItemTableName), poItemTableName)) {
				inTransitCount = tableEntitiesInsertionMap.get(poTableName).size();
				LOG.info("Size of successful PO: " + inTransitCount);
				if (updateMiscDataTableEntityTransaction(inTransitCount)) {
					List<PO> poList = new ArrayList<PO>();
					PO po = new PO();
					for (int i = 0; i < tableEntitiesInsertionMap.get(poTableName).size(); i++) {
						POTableEntity poEntity = (POTableEntity) tableEntitiesInsertionMap.get(poTableName).get(i);
						po.setOrderNumber(poEntity.getOrderNumber());
						poList.add(po);
					}
					poConfirmation.setPoList(poList);
				}
			} else {
				LOG.error("#### Error in inserting PO Items ScheduledJobDaoImpl.batchInsert ###" + poConfirmation);
				deletePOTransaction(tableEntitiesInsertionMap.get(poTableName), poTableName);
				return poConfirmation;
			}
		} else {
			LOG.error("#### Error in inserting PO ScheduledJobDaoImpl.batchInsert ###" + poConfirmation);
			LOG.info("#### Ending ScheduledJobDaoImpl.batchInsert ###" + poConfirmation);
			return poConfirmation;
		}
		LOG.info("#### Ending ScheduledJobDaoImpl.batchInsert ###" + poConfirmation);
		return poConfirmation;
	}

	@Override
	public SupplierApigeePut batchSupplierTransaction(List<TableEntity> tableEntities) {
		LOG.info("#### Starting ScheduledJobDaoImpl.batchSupplierTransaction ###" + tableEntities);
		SupplierApigeePut supplierConfirmation = new SupplierApigeePut();
		LOG.info("Size of the list is: " + tableEntities.size());
		if (batchInsertSupplierTransaction(tableEntities, supplierTableName)) {
			List<Supplier> supplierList = new ArrayList<Supplier>();
			Supplier supplier = new Supplier();
			for (int i = 0; i < tableEntities.size(); i++) {
				SupplierTableEntity supplierEntity = (SupplierTableEntity) tableEntities.get(i);
				supplier.setSupplierID(supplierEntity.getSupplierID());
				supplierList.add(supplier);
			}
			supplierConfirmation.setSupplierList(supplierList);
		} else {
			LOG.error("#### Error in inserting Supplier ScheduledJobDaoImpl.batchSupplierTransaction ###"
					+ supplierConfirmation);
			LOG.info("#### Ending ScheduledJobDaoImpl.batchSupplierTransaction ###" + supplierConfirmation);
			return supplierConfirmation;
		}
		LOG.info("#### Ending ScheduledJobDaoImpl.batchSupplierTransaction ###" + supplierConfirmation);
		return supplierConfirmation;
	}

	@Override
	public ItemApigeePut batchItemTransaction(List<TableEntity> tableEntities) {
		LOG.info("#### Starting ScheduledJobDaoImpl.batchItemTransaction ###" + tableEntities);
		ItemApigeePut itemConfirmation = new ItemApigeePut();
		LOG.info("Size of the list is: " + tableEntities.size());
		if (batchInsertItemTransaction(tableEntities, itemTableName)) {
			List<Item> itemList = new ArrayList<Item>();
			Item item = new Item();
			for (int i = 0; i < tableEntities.size(); i++) {
				ItemTableEntity itemEntity = (ItemTableEntity) tableEntities.get(i);
				item.setSupplierID(itemEntity.getSupplierID());
				itemList.add(item);
			}
			itemConfirmation.setItemList(itemList);
		} else {
			LOG.error("#### Error in inserting Item ScheduledJobDaoImpl.batchItemTransaction ###" + itemConfirmation);
			LOG.info("#### Ending ScheduledJobDaoImpl.batchItemTransaction ###" + itemConfirmation);
			return itemConfirmation;
		}
		LOG.info("#### Ending ScheduledJobDaoImpl.batchItemTransaction ###" + itemConfirmation);
		return itemConfirmation;
	}
}
