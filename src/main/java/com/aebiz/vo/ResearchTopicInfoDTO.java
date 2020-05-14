package com.aebiz.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ResearchTopicInfoDTO {

	/**
	 * msg总条数（所有分区）
	 */
	private long logSize;
	
	/**
	 * 分区列表
	 */
	private List<ResearchPartitionInfoDTO> partitionList = 
			new ArrayList<>();
	
}
