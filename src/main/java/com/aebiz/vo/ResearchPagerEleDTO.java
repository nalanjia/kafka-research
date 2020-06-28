package com.aebiz.vo;

import lombok.Data;

@Data
public class ResearchPagerEleDTO {
	
	private String topicName;
	private int partition;
	private long offset;
	
	private String msgKey;
	private long msgTimestamp;
	private String msgTimestampType;
	private String msg;
	
}
