package com.aebiz.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;

public class ConsumerUtil {

	/**
	 * 展示监听器容器
	 */
	public static String prepareListenerContainerStr(Collection<MessageListenerContainer> list) {
		StringBuffer buf = new StringBuffer();
		buf.append("<br>" + OtherUtil.getNow() + "MessageListenerContainer总数量为[" + list.size() + "]");
		
		list.forEach(t -> {
			Collection<TopicPartition> topicPartitions = t.getAssignedPartitions();
			ContainerProperties containerProperties = t.getContainerProperties();
			String groupId = t.getGroupId();
			String listenerId = t.getListenerId();
			int phase = t.getPhase();
			boolean isAutoStartup = t.isAutoStartup();
			boolean isContainerPaused = t.isContainerPaused();
			boolean isPauseRequested = t.isPauseRequested();
			boolean isRunning = t.isRunning();
			Map<String, Map<MetricName, ? extends Metric>> metrics = t.metrics();
			
			String spanBegin = "<span style='color:red;font-weight:bold;'>";
			String spanEnd = "</span>";
			buf.append("<br>" + spanBegin + "topicPartitions : " + spanEnd)
			.append(topicPartitions)
			.append("<br>" + spanBegin + "containerProperties : " + spanEnd)
			.append(containerProperties)
			.append("<br>" + spanBegin + "groupId : " + spanEnd)
			.append(groupId)
			.append("<br>" + spanBegin + "listenerId : " + spanEnd)
			.append(listenerId)
			.append("<br>" + spanBegin + "phase : " + spanEnd)
			.append(phase)
			.append("<br>" + spanBegin + "isAutoStartup : " + spanEnd)
			.append(isAutoStartup)
			.append("<br>" + spanBegin + "isContainerPaused : " + spanEnd)
			.append(isContainerPaused)
			.append("<br>" + spanBegin + "isPauseRequested : " + spanEnd)
			.append(isPauseRequested)
			.append("<br>" + spanBegin + "isRunning : " + spanEnd)
			.append(isRunning)
			.append("<br>" + spanBegin + "metrics : " + spanEnd)
			.append(ConsumerUtil.prepareMetricStr(metrics))
			.append("<br>------------------------------------------------------------------------------------------")
			;
		});
		return buf.toString();
	}
	
	/**
	 * 展示度量指标数据-按分组展示
	 */
	private static String prepareMetricStrComplex(Map<String, Map<MetricName, ? extends Metric>> metrics) {
		StringBuffer buf = new StringBuffer();
		
		metrics.forEach((k, v) -> {
			//消费者
			buf.append("<br>" + k);
			
			//按MetricName的group分组
			Map<String, List<MetricName>> groupMap = 
					new HashMap<>();
			v.forEach((metricKey, metricValue) -> {
				String group = metricKey.group();
				List<MetricName> groupList = groupMap.get(group);
				if(groupList == null) {
					groupList = new ArrayList<>();
					groupMap.put(group, groupList);
				}
				groupList.add(metricKey);
			});
			
			//分组名，排序
			Map<String, List<MetricName>> groupMap2 = groupMap.entrySet().stream()
				    .sorted(Map.Entry.comparingByKey())
				    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
			
			//按分组打印
			String spanBegin = "<span style='color:blue;font-weight:bold;'>";
			String spanEnd = "</span>";
			groupMap2.forEach((group, groupList) -> {
				
				buf.append("<br>　　" + spanBegin + group + "[" + groupList.size() + "]" + spanEnd);
				
				groupList.forEach(metric -> {
					buf.append("<br>　　　　名称 : " + metric.name());
					buf.append("<br>　　　　描述 : " + metric.description());
					buf.append("<br>　　　　标签 : " + metric.tags());
					
					Metric value = v.get(metric);
					buf.append("<br>　　　　值　 : " + value.metricValue());
					
					if(groupList.size() > 1) {
						buf.append("<br>　　-------------");
					}
				});
				
				buf.append("<br>　　---------------------------------------");
			});
		});
		
		return buf.toString();
	
	}
	
	/**
	 * 展示度量指标数据-简单展示
	 */
	private static String prepareMetricStrSimple(Map<String, Map<MetricName, ? extends Metric>> metrics) {
		StringBuffer buf = new StringBuffer();
		
		metrics.forEach((k, v) -> {
			//消费者
			buf.append("<br>" + k);
			
			//消费者的度量指标
			v.forEach((metricKey, metricValue) -> {
				buf.append("<br>　　" + metricKey);
				buf.append("<br>　　" + metricValue.metricValue());
				buf.append("<br>　　-------------");
			});
		});
		
		return buf.toString();
	}
	
	/**
	 * 展示度量指标数据
	 */
	public static String prepareMetricStr(Map<String, Map<MetricName, ? extends Metric>> metrics) {
//		String ret = prepareMetricStrSimple(metrics);
		String ret = prepareMetricStrComplex(metrics);
		return ret;
	}
	
	/**
	 * 展示消费者组的消费进度
	 */
	public static String prepareGroupIdDetails(Map<TopicPartition, OffsetAndMetadata> map,
			Map<TopicPartition, Long> leos) {
		//按topic分组
		Map<String, List<TopicPartition>> topicMap = 
				new HashMap<>();
		map.forEach((k, v) -> {
			String topicName = k.topic();
			List<TopicPartition> list = topicMap.get(topicName);
			if(list == null) {
				list = new ArrayList<>();
				topicMap.put(topicName, list);
			}
			list.add(k); //将TopicPartition收集起来
		});
		
		//分组名，排序
		Map<String, List<TopicPartition>> topicMap2 = topicMap.entrySet().stream()
			    .sorted(Map.Entry.comparingByKey())
			    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
			    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		
		//按分组打印
		StringBuffer buf = new StringBuffer(OtherUtil.getNow());
		String spanBegin = "<span style='color:blue;font-weight:bold;'>";
		String spanEnd = "</span>";
		topicMap2.forEach((topic, partitionList) -> {
			buf.append("<br>　　" + spanBegin + topic + "[" + partitionList.size() + "]" + spanEnd);
			
			partitionList.forEach(partition -> {
				OffsetAndMetadata meta = map.get(partition);
				Long leo = leos.get(partition);
				buf.append("<br>　　　　分区编号 : " + partition.partition());
				buf.append("<br>　　　　消费位移 : " + meta.offset());
				buf.append("<br>　　　　　　LEO : " + leo);
				buf.append("<br>　　　　　　LAG : " + (leo - meta.offset()));
				buf.append("<br>　　　　--------------------");
			});
			buf.append("<br>　　----------------------------------------");
		});
		
		return buf.toString();		
	}
	
}
