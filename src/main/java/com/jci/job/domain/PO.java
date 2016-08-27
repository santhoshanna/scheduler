package com.jci.job.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PO {

	private String orderNumber;
	private String orderCreationDate;
	private Boolean poACK;
	private Boolean asn;
	private String grNumber;
	private String supplierType;
	private List<Object> itemList;

	

	public List<Object> getItemList() {
		return itemList;
	}
	public void setItemList(List<Object> itemList) {
		this.itemList = itemList;
	}
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getSupplierType() {
		return supplierType;
	}

	public void setSupplierType(String supplierType) {
		this.supplierType = supplierType;
	}

	public String getOrderCreationDate() {
		return orderCreationDate;
	}

	public void setOrderCreationDate(String orderCreationDate) {
		this.orderCreationDate = orderCreationDate;
	}

	public Boolean getPoACK() {
		return poACK;
	}

	public void setPoACK(Boolean poACK) {
		this.poACK = poACK;
	}

	public Boolean getAsn() {
		return asn;
	}

	public void setAsn(Boolean asn) {
		this.asn = asn;
	}

	public String getGrNumber() {
		return grNumber;
	}

	public void setGrNumber(String grNumber) {
		this.grNumber = grNumber;
	}

}
