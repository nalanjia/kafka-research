package com.aebiz.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
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
import com.aebiz.util.ConsumerUtil;
import com.aebiz.util.OtherUtil;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

	@Autowired
	private KafkaResearchConfig kafkaTemplateConfig;
	
	/**
	 * 关闭。关闭之后消费者就不存在了
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
	 * 打开。打开关闭的消费者，其实是新建了一个新的消费者
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
	 * 恢复。消费者还是那个消费者，又可以消费了
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
	 * 暂停。消费者不会被销毁，仅仅是暂停一下，不能消费而已
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
			.append(ConsumerUtil.prepareMetricStr(metrics))
			.append("<br>------------------------------------------------------------------------------------------")
			;
		});
		
		return buf.toString();
	}

	
	/**
	 * 查询消费者组列表
	 */
	@RequestMapping("/listGroupId")
	public String listGroupId() {
		AdminClient adminClient = kafkaTemplateConfig.getAdminClient();
		ListConsumerGroupsResult result = adminClient.listConsumerGroups();
		KafkaFuture<Collection<ConsumerGroupListing>> future = result.all();
		
		Collection<ConsumerGroupListing> list = null;
		try {
			list = future.get(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			return OtherUtil.getNow() + "消费者组不存在，没有一个！";
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append(OtherUtil.getNow());
		list.forEach(consumerGroup -> {
			buf.append("<br>groupId : " + consumerGroup.groupId());
			buf.append("<br>isSimpleConsumerGroup : " + consumerGroup.isSimpleConsumerGroup());
			buf.append("<br>---------------------");
		});
		return buf.toString();
	}
	
	/**
	 * 查询消费者组的消费进度
	 */
	@RequestMapping("/detailGroupId")
	public String detailGroupId(String groupId) {
		if(StringUtils.isBlank(groupId)) {
			return "groupId为空，请输入";
		}
		AdminClient adminClient = kafkaTemplateConfig.getAdminClient();
		//所有分区、及其消费情况
		ListConsumerGroupOffsetsResult result = adminClient.listConsumerGroupOffsets(groupId);
		KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> future = result.partitionsToOffsetAndMetadata();
		
		Map<TopicPartition, OffsetAndMetadata> map;
		try {
			map = future.get(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			String msg = OtherUtil.getNow() + "消费者组[" + groupId + "]不存在，请从列表中选择存在的消费者组";
			msg += "<br>" +  this.listGroupId();
			return msg;
		}
		
		if(map == null || map.size() == 0) {
			String msg = OtherUtil.getNow() + "消费者组[" + groupId + "]不存在，请从列表中选择存在的消费者组";
			msg += "<br>" +  this.listGroupId();
			return msg;
		}
		
		String str = ConsumerUtil.prepareGroupIdDetails(map);
		return str;
	}

	
	
}
