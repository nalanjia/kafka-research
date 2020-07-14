package com.aebiz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.config.SpringBeanTool;
import com.aebiz.vo.ResearchListenerContainerDTO;
import com.aebiz.vo.ResearchMetricDTO;

public class ConsumerUtil {
	
	/**
	 * 展示监听器容器
	 */
	public static List<ResearchListenerContainerDTO> listListener(Collection<MessageListenerContainer> list) {
		List<ResearchListenerContainerDTO> retList = new ArrayList<>();
		list.forEach(t -> {
			ResearchListenerContainerDTO dto = new ResearchListenerContainerDTO();
			
			ConcurrentMessageListenerContainer container = (ConcurrentMessageListenerContainer)t;
			
			dto.setConcurrency(container.getConcurrency());
			
			//分区
			Collection<TopicPartition> topicPartitions = t.getAssignedPartitions();
			dto.setTopicPartitions(topicPartitions.toString());
			
			//消费者配置
			ContainerProperties containerProperties = t.getContainerProperties();
			dto.setContainerProperties(containerProperties);
			
			//消费者组
			dto.setGroupId(t.getGroupId());
			//监听器id
			dto.setListenerId(t.getListenerId());
			
			dto.setPhase(t.getPhase());
			
			//随项目启动
			dto.setAutoStartup(t.isAutoStartup());
			//已暂停
			dto.setContainerPaused(t.isContainerPaused());
			//发出暂停
			dto.setPauseRequested(t.isPauseRequested());
			//正在运行
			dto.setRunning(t.isRunning());
			
			//度量
			Map<String, Map<MetricName, ? extends Metric>> metrics = t.metrics();
			
			metrics.forEach((k, v) -> {
				
				//Map<度量类别, List<ResearchMetricDTO>>
				Map<String, List<ResearchMetricDTO>> map = new TreeMap<>();
				dto.getMetricMap().put(k, map);
				
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
				
				groupMap2.forEach((group, groupList) -> {
					
					List<ResearchMetricDTO> mList = new ArrayList<>();
					
					groupList.forEach(metric -> {
						ResearchMetricDTO metricDTO = new ResearchMetricDTO();
						
						metricDTO.setName(metric.name());
						metricDTO.setDesc(metric.description());
						Metric value = v.get(metric);
						metricDTO.setValue(value.metricValue().toString());
						mList.add(metricDTO);
						
					});
					
					//排序
					List<ResearchMetricDTO> mList2 = mList.stream()
					.sorted(Comparator.comparing(ResearchMetricDTO::getName,Comparator.naturalOrder()))
					.collect(Collectors.toList());
					
					map.put(group, mList2);
				});
				
			});
			
			retList.add(dto);
		});
		return retList;
	}
	
	/**
	 * 查询分区的消息数量
	 */
	public static long getLogSize(String topicName, int partition) {
		String groupId = ConsumerUtil.getGroupId();
		KafkaConsumer consumer = ConsumerUtil.getKafkaConsumer(groupId);
		
		TopicPartition tp = new TopicPartition(topicName, partition);
		//为消费者指定分区
		List<TopicPartition> tpList = Arrays.asList(tp);
		consumer.assign(tpList);
		
		//查询分区的first offset，end offset
		Map<TopicPartition, Long> beginMap = consumer.beginningOffsets(tpList);
		Map<TopicPartition, Long> endMap = consumer.endOffsets(tpList);
				
		Long beginOffset = beginMap.get(tp);
		Long endOffset = endMap.get(tp);
		long logSize = endOffset.longValue() - beginOffset.longValue();
		
		//关闭consumer
		ConsumerGroupUtil.deleteConsumerGroup(groupId);
		return logSize;
	}
	
	
	/**
	 * 展示消费者组的消费进度
	 */
//	public static String prepareGroupIdDetails(Map<TopicPartition, OffsetAndMetadata> map,
//			Map<TopicPartition, Long> leos) {
//		//按topic分组
//		Map<String, List<TopicPartition>> topicMap = 
//				new HashMap<>();
//		map.forEach((k, v) -> {
//			String topicName = k.topic();
//			List<TopicPartition> list = topicMap.get(topicName);
//			if(list == null) {
//				list = new ArrayList<>();
//				topicMap.put(topicName, list);
//			}
//			list.add(k); //将TopicPartition收集起来
//		});
//		
//		//分组名，排序
//		Map<String, List<TopicPartition>> topicMap2 = topicMap.entrySet().stream()
//			    .sorted(Map.Entry.comparingByKey())
//			    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//			    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//		
//		//按分组打印
//		StringBuffer buf = new StringBuffer(OtherUtil.getNow());
//		String spanBegin = "<span style='color:blue;font-weight:bold;'>";
//		String spanEnd = "</span>";
//		topicMap2.forEach((topic, partitionList) -> {
//			buf.append("<br>　　" + spanBegin + topic + "[" + partitionList.size() + "]" + spanEnd);
//			
//			partitionList.forEach(partition -> {
//				OffsetAndMetadata meta = map.get(partition);
//				Long leo = leos.get(partition);
//				buf.append("<br>　　　　分区编号 : " + partition.partition());
//				buf.append("<br>　　　　消费位移 : " + meta.offset());
//				buf.append("<br>　　　　　　LEO : " + leo);
//				buf.append("<br>　　　　　　LAG : " + (leo - meta.offset()));
//				buf.append("<br>　　　　--------------------");
//			});
//			buf.append("<br>　　----------------------------------------");
//		});
//		
//		return buf.toString();		
//	}
	
	
	public static String getGroupId() {
		String uuid = GeneDataUtil.geneUuid();
		String now = DateUtil.getNowTime_EN().replace("-", "");
		String groupId = now + "-" + uuid;
		return groupId;
	}
	/**
	 * poll得到的消息数量
	 */
	public static int MAX_POLL_RECORDS_NUM = 5;
	/**
	 * 新建消费者
	 */
	public static KafkaConsumer getKafkaConsumer(String groupId) {
		KafkaResearchConfig config = SpringBeanTool.getBean(KafkaResearchConfig.class);
		//取得application.properties的属性
		Map<String, Object> properties = config.getKafkaProperties().buildConsumerProperties();
		//覆盖application.properties的属性
		
		if(StringUtils.isBlank(groupId)) {
			groupId = getGroupId();
		}
		String clientId = groupId;
		
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
		properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS_NUM); //poll一波儿得到的消息数量
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); //自动提交
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); //从头儿消费
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
		return consumer;
	}
	
}
