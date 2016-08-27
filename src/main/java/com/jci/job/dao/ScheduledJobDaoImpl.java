package com.jci.job.dao;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import com.jci.job.domain.BatchInsertRes;
import com.jci.job.domain.POItemTableEntity;
import com.jci.job.domain.POTableEntity;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableEntity;

@Repository
@Configuration
public class ScheduledJobDaoImpl implements ScheduledJobDao {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledJobDaoImpl.class);
	private static int intransitCount;
	static int counter = 0;
	final int batchSize = 20;

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
	
//	private static final String proxy = "10.10.5.18";
//	private static final int port = 8080;
	
//	InetSocketAddress inetAddr = new InetSocketAddress(proxy, port);
//	Proxy proxyObj = new Proxy(Proxy.Type.HTTP, inetAddr);

	/**
	 * Validates the connection string and returns the storage table client. The
	 * connection string must be in the Azure connection string format.
	 *
	 * @return The newly created CloudTableClient object
	 */
	public CloudTableClient getTableClientReference() {

		CloudStorageAccount storageAccount = null;
		try {
			storageAccount = CloudStorageAccount.parse(connectionString);
		} catch (IllegalArgumentException | URISyntaxException e) {
			LOG.info("\nConnection string specifies an invalid URI.");
			LOG.info("Please confirm the connection string is in the Azure connection string format.");
			try {
				throw e;
			} catch (Exception e1) {
				LOG.error("ERROR: " + e1);
			}
		} catch (InvalidKeyException e) {
			LOG.error("\nConnection string specifies an invalid key.");
			LOG.error("Please confirm the AccountName and AccountKey in the connection string are valid.");
			try {
				throw e;
			} catch (InvalidKeyException e1) {
				LOG.error("ERROR: " + e1);
			}
		}

		return storageAccount.createCloudTableClient();
	}

	@SuppressWarnings("null")
	public boolean createAzureTableIfNotExists(CloudTableClient tableClient, String azureStorageTableName) {
		CloudTable table = null;
		try {
			table = tableClient.getTableReference(azureStorageTableName);
		} catch (URISyntaxException | StorageException e) {
			LOG.error("ERROR: " + e);
		}
		if (table == null) {
			LOG.info("Created new table since it exist");
			try {
				table.createIfNotExists();
				return true;
			} catch (StorageException e) {
				LOG.error("ERROR: " + e);
				return false;
			}
		} else {
			// LOG.info("AccountName: "+ accountName);
			// LOG.info("Connection String: "+ connectionString);
			// LOG.info("Table Name: "+ azureStorageTableName);
			LOG.info("Table already exists");
		}
		return true;
	}

	public CloudTable getTable(CloudTableClient cloudTableClient, String tableName)
			throws InvalidKeyException, URISyntaxException, StorageException {
		return cloudTableClient.getTableReference(tableName);
	}

	private boolean batchInsertPOTransaction(List<TableEntity> entities, String tableName) {

		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);

			// Create the table client.
			CloudTableClient tableClient = storageAccount.createCloudTableClient();
			LOG.info("Table Name: " + tableName);
			LOG.info("Connection String: " + connectionString);

			// Define a batch operation.
			TableBatchOperation batchPOOperation = new TableBatchOperation();

			// Create a cloud table object for the table.
			//   OperationContext opContext = new OperationContext();
		//	   opContext.setProxy(proxyObj);
			   
			CloudTable cloudTable = tableClient.getTableReference(tableName);
			LOG.info("cloudTable: " + cloudTable);
		//	boolean tableExistsOrNOt = cloudTable.createIfNotExists(null,opContext);
			//LOG.info("PO Exists or not" + tableExistsOrNOt);

			for (int i = 0; i < entities.size(); i++) {

				POTableEntity poEntity = (POTableEntity) entities.get(i);

				// Create a customer entity to add to the table.
				batchPOOperation.insertOrReplace(poEntity);
				LOG.info("batch PO: "+ batchPOOperation.size());

			}

			cloudTable.execute(batchPOOperation);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("ERROR While inserting POItems in ScheduledJobDaoImpl.batchInsertPOTransaction():" + e);
			return false;
		}
		return true;
	}

	private boolean batchInsertPOItemTransaction(List<TableEntity> entities, String tableName) {

		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			LOG.info("Table Name: " + tableName);
			LOG.info("Connection String: " + connectionString);

			// Create the table client.
			CloudTableClient tableClient = storageAccount.createCloudTableClient();

			// Define a batch operation.
			TableBatchOperation batchPOItemOperation = new TableBatchOperation();

			// Create a cloud table object for the table.
			CloudTable cloudTable = tableClient.getTableReference(tableName);
			LOG.info("cloudTable: " + cloudTable);
			//boolean tableExistsOrNOt = cloudTable.createIfNotExists();
			//LOG.info("PO Item Exists or not" + tableExistsOrNOt);

			for (int i = 0; i < entities.size(); i++) {

				POItemTableEntity poItemEntity = (POItemTableEntity) entities.get(i);

				// Create a customer entity to add to the table.
				batchPOItemOperation.insertOrReplace(poItemEntity);
				LOG.info("batch PO: "+ batchPOItemOperation.size());

			}

			cloudTable.execute(batchPOItemOperation);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("ERROR While inserting POItems in ScheduledJobDaoImpl.batchInsertPOItemTransaction(): " + e);
			return false;
		}
		return true;
	}

	@Override
	public BatchInsertRes batchPOAndPOItemTransaction(HashMap<String, List<TableEntity>> tableEntitiesInsertionMap) {
		LOG.info("#### Starting ScheduledJobDaoImpl.batchPOAndPOItemInsert ###" + tableEntitiesInsertionMap);
		BatchInsertRes response = new BatchInsertRes();

		HashMap<String, List<TableEntity>> errorMap = new HashMap<String, List<TableEntity>>();
		HashMap<String, List<TableEntity>> successMap = new HashMap<String, List<TableEntity>>();

		// CloudTableClient cloudTableClient = null;
		// CloudTable cloudTable = null;

		// for (Map.Entry<String, List<TableEntity>> entry :
		// tableEntitiesInsertionMap.entrySet()) {
		LOG.info("Size of the map is: " + tableEntitiesInsertionMap.entrySet().size());
		LOG.info("Value returned by PO Insertion: "
				+ batchInsertPOTransaction(tableEntitiesInsertionMap.get(poTableName), poTableName));
		LOG.info("Value returned by PO Item Insertion: "
				+ batchInsertPOItemTransaction(tableEntitiesInsertionMap.get(poItemTableName), poItemTableName));

		// }

		/*
		 * for (Map.Entry<String, List<TableEntity>> entry :
		 * tableEntitiesInsertionMap.entrySet()) { try { cloudTableClient =
		 * getTableClientReference(); if
		 * (createAzureTableIfNotExists(cloudTableClient, entry.getKey())) {
		 * cloudTable = getTable(cloudTableClient, entry.getKey()); }
		 * 
		 * } catch (Exception e) { errorMap.put(entry.getKey(),
		 * entry.getValue()); LOG.error(
		 * "### Exception in ScheduledJobDaoImpl.batchPOAndPOItemInsert.getTable ###"
		 * + e); response.setError(true); continue; }
		 * 
		 * LOG.error("Table Name--->" + cloudTable.getName()); // Define a batch
		 * operation. TableBatchOperation batchOperation = new
		 * TableBatchOperation(); List<TableEntity> value = entry.getValue();
		 * LOG.error("value.size()--->" + value.size());
		 * LOG.error("value.toString()--->" + value.toString());
		 * 
		 * for (int i = 0; i < value.size(); i++) { TableEntity entity =
		 * value.get(i); if (entity instanceof POTableEntity) { counter =
		 * counter + 1; }
		 * 
		 * batchOperation.insertOrReplace(entity);
		 * LOG.error("batchOperation.size()--->" + batchOperation.size());
		 * LOG.error("intransitCount--->" + intransitCount);
		 * LOG.error("counter--->" + counter); if (i != 0 && i % batchSize == 0)
		 * { try { cloudTable.execute(batchOperation); batchOperation.clear();
		 * successMap.put(entry.getKey(), entry.getValue()); intransitCount =
		 * intransitCount + counter; counter = 0; } catch (Exception e) {
		 * errorMap.put(entry.getKey(), entry.getValue());
		 * response.setError(true); counter = 0; LOG.error(
		 * "### Exception in ScheduledJobDaoImpl.batchPOAndPOItemInsert.execute ###"
		 * + e); e.printStackTrace(); continue; } } }
		 * 
		 * // LOG.error("intransitCount 1--->"+intransitCount); // LOG.error(
		 * "counter 1--->"+counter);
		 * 
		 * // LOG.error("batchOperation.size()--->"+batchOperation.size());
		 * 
		 * if (batchOperation.size() > 0) { try {
		 * cloudTable.execute(batchOperation); successMap.put(entry.getKey(),
		 * entry.getValue()); intransitCount = intransitCount + counter; counter
		 * = 0; } catch (Exception e) { errorMap.put(entry.getKey(),
		 * entry.getValue()); response.setError(true); counter = 0; LOG.error(
		 * "### Exception in JobRepoImpl.batchInsert.execute ###" + e);
		 * e.printStackTrace(); continue; } } }
		 */

		response.setErrorMap(errorMap);
		response.setSuccessMap(successMap);

		// LOG.error("intransitCount 2--->"+intransitCount);

		// Insert MIsc data
		/*
		 * MiscDataEntity miscEntity = null; try { miscEntity =
		 * getStatusCountEntity(Constants.PARTITION_KEY_MISCDATA, erpName); }
		 * catch (InvalidKeyException | URISyntaxException | StorageException e)
		 * { e.printStackTrace(); } if (miscEntity != null) {
		 * miscEntity.setIntransitCount((miscEntity.getIntransitCount() +
		 * intransitCount)); } else { miscEntity = new
		 * MiscDataEntity(Constants.PARTITION_KEY_MISCDATA, erpName);
		 * miscEntity.setIntransitCount(intransitCount); }
		 * 
		 * try { updateStatusCountEntity(miscEntity); } catch
		 * (InvalidKeyException | URISyntaxException | StorageException e) {
		 * response.setError(true); e.printStackTrace(); }
		 */
		LOG.info("#### Ending JobRepoImpl.batchInsert ###" + response);
		return response;
	}

	/*
	 * public MiscDataEntity getStatusCountEntity(String partitionKey, String
	 * rowKey) throws InvalidKeyException, URISyntaxException, StorageException
	 * { CloudTable cloudTable = azureStorage.getTable(Constants.TABLE_MISC);
	 * TableOperation entity = TableOperation.retrieve(partitionKey, rowKey,
	 * MiscDataEntity.class); return
	 * cloudTable.execute(entity).getResultAsType(); }
	 * 
	 * public void updateStatusCountEntity(MiscDataEntity entity) throws
	 * InvalidKeyException, URISyntaxException, StorageException { CloudTable
	 * cloudTable = azureStorage.getTable(Constants.TABLE_MISC);
	 * 
	 * TableOperation insert = TableOperation.insertOrReplace(entity);
	 * cloudTable.execute(insert); }
	 */
}
