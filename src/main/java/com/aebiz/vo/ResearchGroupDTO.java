package com.aebiz.vo;

import lombok.Data;

@Data
public class ResearchGroupDTO {
	
	/**
	 * 消费者组ID
	 */
	private String customerGroupId;
	
	/**
	 * If consumer group is simple or not
	 */
	private boolean isSimpleConsumerGroup;
}
