package com.jci.job.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemApigeeGet {

	private String fallbackErrorMessage;
	private String date;
	private String message;
	private String status;
	private Integer code;
	private List<Item> itemList;


	public List<Item> getItemList() {
		return itemList;
	}

	public void setItemList(List<Item> itemList) {
		this.itemList = itemList;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getFallbackErrorMessage() {
		return fallbackErrorMessage;
	}

	public void setFallbackErrorMessage(String fallbackErrorMessage) {
		this.fallbackErrorMessage = fallbackErrorMessage;
	}

}
