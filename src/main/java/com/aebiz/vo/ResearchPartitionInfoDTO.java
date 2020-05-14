package com.aebiz.vo;

import lombok.Data;

@Data
public class ResearchPartitionInfoDTO {

	/**
	 * 分区编号
	 */
	private int partition;
	
	/**
	 * msg条数（该分区）
	 */
	private long logSize;
	
}
