package com.jci.job.domain;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class POTableEntity extends TableServiceEntity {

	public POTableEntity(String partitionKey, String rowKey) {
		this.partitionKey = partitionKey;
		this.rowKey = rowKey;// po number
	}

	public POTableEntity() {
	}

	private String erp;
	private String region;
	private String plant;
	private String orderNumber;
	private String orderCreationDate;
	private String supplierProcessingStatus;
	private Integer supplierProcessingStatusCode;
	private String supplierProcessingStatusmesssage;
	private String supplierType;
	private Integer supplierDeliveryState;

	@Override
	public String toString() {
		return "POTableEntity [erp=" + erp + ", region=" + region + ",plant=" + plant + ",orderNumber=" + orderNumber
				+ ",orderCreationDate=" + orderCreationDate + ",supplierProcessingStatus=" + supplierProcessingStatus
				+ ",supplierProcessingStatusCode=" + supplierProcessingStatusCode + ",supplierProcessingStatusmesssage="
				+ supplierProcessingStatusmesssage + ",supplierType=" + supplierType + ",supplierDeliveryState="
				+ supplierDeliveryState + "]";
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getSupplierProcessingStatus() {
		return supplierProcessingStatus;
	}

	public void setSupplierProcessingStatus(String supplierProcessingStatus) {
		this.supplierProcessingStatus = supplierProcessingStatus;
	}

	public Integer getSupplierProcessingStatusCode() {
		return supplierProcessingStatusCode;
	}

	public void setSupplierProcessingStatusCode(Integer supplierProcessingStatusCode) {
		this.supplierProcessingStatusCode = supplierProcessingStatusCode;
	}

	public String getSupplierProcessingStatusmesssage() {
		return supplierProcessingStatusmesssage;
	}

	public void setSupplierProcessingStatusmesssage(String supplierProcessingStatusmesssage) {
		this.supplierProcessingStatusmesssage = supplierProcessingStatusmesssage;
	}

	public String getOrderCreationDate() {
		return orderCreationDate;
	}

	public void setOrderCreationDate(String orderCreationDate) {
		this.orderCreationDate = orderCreationDate;
	}

	public String getErp() {
		return erp;
	}

	public void setErp(String erp) {
		this.erp = erp;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPlant() {
		return plant;
	}

	public void setPlant(String plant) {
		this.plant = plant;
	}

	public String getSupplierType() {
		return supplierType;
	}

	public void setSupplierType(String supplierType) {
		this.supplierType = supplierType;
	}

	public Integer getSupplierDeliveryState() {
		return supplierDeliveryState;
	}

	public void setSupplierDeliveryState(Integer supplierDeliveryState) {
		this.supplierDeliveryState = supplierDeliveryState;
	}

}
