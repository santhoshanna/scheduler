package com.jci.job.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {

	private String supplierID;
	private String customerItemID;
	private Object item;

	public Object getItem() {
		return item;
	}

	public void setItem(Object item) {
		this.item = item;
	}

	public String getSupplierID() {
		return supplierID;
	}

	public void setSupplierID(String supplierID) {
		this.supplierID = supplierID;
	}

	public String getCustomerItemID() {
		return customerItemID;
	}

	public void setCustomerItemID(String customerItemID) {
		this.customerItemID = customerItemID;
	}

}
