package com.jci.job.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microsoft.azure.storage.table.TableServiceEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class POItemTableEntity extends TableServiceEntity {

	public POItemTableEntity(String partitionKey, String rowKey) {
		this.partitionKey = partitionKey;
		this.rowKey = rowKey;// po number
	}

	public POItemTableEntity() {
	}

	private String OrderNumber;
	private String POItemJsonString;

	public String getOrderNumber() {
		return OrderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		OrderNumber = orderNumber;
	}

	public String getPOItemJsonString() {
		return POItemJsonString;
	}

	public void setPOItemJsonString(String pOItemJsonString) {
		POItemJsonString = pOItemJsonString;
	}

	@Override
	public String toString() {
		return "POItemTableEntity [OrderNumber=" + OrderNumber + ", POItemJsonString=" + POItemJsonString + "]";
	}

}
