package com.jci.job.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microsoft.azure.storage.table.TableServiceEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplierTableEntity extends TableServiceEntity {

	public SupplierTableEntity(String partitionKey, String rowKey) {
		this.partitionKey = partitionKey;
		this.rowKey = rowKey;// po number
	}

	public SupplierTableEntity() {
	}

	private String SupplierID;
	private String SupplierJsonString;

	public String getSupplierJsonString() {
		return SupplierJsonString;
	}

	public void setSupplierJsonString(String supplierJsonString) {
		SupplierJsonString = supplierJsonString;
	}

	public String getSupplierID() {
		return SupplierID;
	}

	public void setSupplierID(String supplierID) {
		SupplierID = supplierID;
	}

	@Override
	public String toString() {
		return "SupplierTableEntity [ SupplierID=" + SupplierID + ",SupplierJsonString=" + SupplierJsonString + "]";
	}

}
