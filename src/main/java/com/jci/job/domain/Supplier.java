package com.jci.job.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Supplier {

	private String supplierID;
	private Object supplier;

	public String getSupplierID() {
		return supplierID;
	}

	public void setSupplierID(String supplierID) {
		this.supplierID = supplierID;
	}

	public Object getSupplier() {
		return supplier;
	}

	public void setSupplier(Object supplier) {
		this.supplier = supplier;
	}

}
