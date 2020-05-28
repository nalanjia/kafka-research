package com.aebiz.vo;

import lombok.Data;

@Data
public class ResearchPartitionInfoDTO {

	/**
	 * 所属主题
	 */
	private String topicName;
	/**
	 * 分区编号
	 */
	private int partition;
	
	/**
	 * msg条数（该分区）
	 */
	private long logSize;
	
	/**
	 * 消费位移（某个消费者的）
	 */
	private long offset;
	
}
