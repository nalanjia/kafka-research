package com.aebiz.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.util.OtherUtil;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

	@Autowired
	private KafkaResearchConfig kafkaTemplateConfig;
	
	/**
	 * 关闭
	 */
	@RequestMapping("/stop")
	public String stop(@RequestParam("listenerId") String listenerId) {
		MessageListenerContainer container = kafkaTemplateConfig.getkKafkaListenerEndpointRegistry()
		.getListenerContainer(listenerId);
		
		if(container == null) {
			return OtherUtil.getNow() + listenerId + "不存在！请从列表中选好listenerId之后再试" + list();
		}
		
		container.stop();
		return OtherUtil.getNow() + listenerId + "已关闭，请看列表：" + list();
	}	
	
	/**
	 * 打开
	 */
	@RequestMapping("/start")
	public String start(@RequestParam("listenerId") String listenerId) {
		MessageListenerContainer container = kafkaTemplateConfig.getkKafkaListenerEndpointRegistry()
		.getListenerContainer(listenerId);
		
		if(container == null) {
			return OtherUtil.getNow() + listenerId + "不存在！请从列表中选好listenerId之后再试" + list();
		}
		
		container.start();
		return OtherUtil.getNow() + listenerId + "已打开，请看列表：" + list();
	}	
	
	/**
	 * 恢复
	 */
	@RequestMapping("/resume")
	public String resume(@RequestParam("listenerId") String listenerId) {
		MessageListenerContainer container = kafkaTemplateConfig.getkKafkaListenerEndpointRegistry()
		.getListenerContainer(listenerId);
		
		if(container == null) {
			return OtherUtil.getNow() + listenerId + "不存在！请从列表中选好listenerId之后再试" + list();
		}
		
		container.resume();
		return OtherUtil.getNow() + listenerId + "已恢复，请看列表：" + list();
	}
	
	/**
	 * 暂停
	 */
	@RequestMapping("/pause")
	public String pause(@RequestParam("listenerId") String listenerId) {
		MessageListenerContainer container = kafkaTemplateConfig.getkKafkaListenerEndpointRegistry()
			.getListenerContainer(listenerId);
		
		if(container == null) {
			return OtherUtil.getNow() + listenerId + "不存在！请从列表中选好listenerId之后再试" + list();
		}
		
		container.pause();
		return OtherUtil.getNow() + listenerId + "已暂停，请看列表：" + list();
	}
	
	/**
	 * 查询监听器容器列表
	 */
	@RequestMapping("/list")
	public String list() {
		StringBuffer buf = new StringBuffer();
		Collection<MessageListenerContainer> list = kafkaTemplateConfig.getkKafkaListenerEndpointRegistry()
			.getAllListenerContainers();
		
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
			.append(parepareMetricStr(metrics))
			.append("<br>------------------------------------------------------------------------------------------")
			;
		});
		
		return buf.toString();
	}

	

	private String parepareMetricStrComplex(Map<String, Map<MetricName, ? extends Metric>> metrics) {
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
					buf.append("<br>　　　　名称：" + metric.name());
					buf.append("<br>　　　　描述：" + metric.description());
					buf.append("<br>　　　　标签：" + metric.tags());
					
					Metric value = v.get(metric);
					buf.append("<br>　　　　值　：" + value.metricValue());
					
					if(groupList.size() > 1) {
						buf.append("<br>　　-------------");
					}
				});
				
				buf.append("<br>　　---------------------------------------");
			});
		});
		
		return buf.toString();
	
	}
	private String parepareMetricStrSimple(Map<String, Map<MetricName, ? extends Metric>> metrics) {
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
	private String parepareMetricStr(Map<String, Map<MetricName, ? extends Metric>> metrics) {
//		String ret = this.parepareMetricStrSimple(metrics);
		String ret = this.parepareMetricStrComplex(metrics);
		return ret;
	}
	
	
}
