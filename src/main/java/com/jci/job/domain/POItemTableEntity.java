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
	private Integer LineID;
	private Integer RequestID;
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

	public Integer getLineID() {
		return LineID;
	}

	public void setLineID(Integer LineID) {
		this.LineID = LineID;
	}

	public Integer getRequestID() {
		return RequestID;
	}

	public void setRequestID(Integer RequestID) {
		this.RequestID = RequestID;
	}

	@Override
	public String toString() {
		return "POItemTableEntity [OrderNumber=" + OrderNumber + ", LineID=" + LineID + ", RequestID=" + RequestID
				+ ",POItemJsonString=" + POItemJsonString + "]";
	}

}
