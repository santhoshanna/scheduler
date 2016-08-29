package com.jci.job.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microsoft.azure.storage.table.TableServiceEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemTableEntity extends TableServiceEntity {

	public ItemTableEntity(String partitionKey, String rowKey) {
		this.partitionKey = partitionKey;
		this.rowKey = rowKey;// po number
	}

	public ItemTableEntity() {
	}

	private String SupplierID;
	private String CustomerItemID;
	private String ItemJsonString;

	public String getSupplierID() {
		return SupplierID;
	}

	public void setSupplierID(String supplierID) {
		SupplierID = supplierID;
	}

	public String getItemJsonString() {
		return ItemJsonString;
	}

	public void setItemJsonString(String itemJsonString) {
		ItemJsonString = itemJsonString;
	}

	public String getCustomerItemID() {
		return CustomerItemID;
	}

	public void setCustomerItemID(String customerItemID) {
		CustomerItemID = customerItemID;
	}

	@Override
	public String toString() {
		return "ItemTableEntity [ SupplierID=" + SupplierID + ",CustomerItemID=" + CustomerItemID + ",ItemJsonString="
				+ ItemJsonString + "]";
	}

}
