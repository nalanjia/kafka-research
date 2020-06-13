package com.aebiz.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.springframework.kafka.listener.ContainerProperties;

import lombok.Data;

/**
 * @KafkaListener详情
 */
@Data
public class ResearchListenerContainerDTO {

	private int concurrency;
	
	private String topicPartitions;
	
	private ContainerProperties containerProperties;
	
	private String groupId;
	private String listenerId;
	
	private int phase;
	
	private boolean isAutoStartup;
	private boolean isContainerPaused;
	private boolean isPauseRequested;
	private boolean isRunning;
	
	/**
	 * Map<度量client-id, Map<度量类别, List<ResearchMetricDTO>>>
	 */
	private Map<String, Map<String, List<ResearchMetricDTO>>> metricMap = new TreeMap<>();
	
}
