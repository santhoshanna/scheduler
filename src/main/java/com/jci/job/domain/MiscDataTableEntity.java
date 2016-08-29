package com.jci.job.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microsoft.azure.storage.table.TableServiceEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MiscDataTableEntity extends TableServiceEntity {

	public MiscDataTableEntity(String partitionKey, String rowKey) {
		this.partitionKey = partitionKey;
		this.rowKey = rowKey;// po number
	}

	public MiscDataTableEntity() {
	}

	private Integer IntransitCount;
	private Integer ProcessedCount;
	private Integer ErrorCount;

	public Integer getIntransitCount() {
		return IntransitCount;
	}

	public void setIntransitCount(Integer intransitCount) {
		IntransitCount = intransitCount;
	}

	public Integer getProcessedCount() {
		return ProcessedCount;
	}

	public void setProcessedCount(Integer processedCount) {
		ProcessedCount = processedCount;
	}

	public Integer getErrorCount() {
		return ErrorCount;
	}

	public void setErrorCount(Integer errorCount) {
		ErrorCount = errorCount;
	}

	@Override
	public String toString() {
		return "MiscDataTableEntity [IntransitCount=" + IntransitCount + ", ProcessedCount=" + ProcessedCount
				+ ",ErrorCount=" + ErrorCount + "]";
	}

}
