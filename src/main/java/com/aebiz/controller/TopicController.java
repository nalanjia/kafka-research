package com.aebiz.controller;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.util.OtherUtil;

import kafka.utils.Json;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/topic")
public class TopicController {

	@Autowired
	private KafkaResearchConfig kafkaTemplateConfig;
	
	/**
	 * 展示主题列表
	 */
	@RequestMapping("/getTopicList")
	public String getTopicList() {
		AdminClient adminClient = kafkaTemplateConfig.getAdminClient();
		ListTopicsResult result = adminClient.listTopics();
		
		KafkaFuture<Collection<TopicListing>> future = result.listings();
		
		Collection<TopicListing> list = null;
		try {
			list = future.get(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			return OtherUtil.getNow() + "<br>没有任何主题";
		}
		
		StringBuffer buf = new StringBuffer(OtherUtil.getNow());
		list.forEach(topic -> {
			buf.append("<br>　　topic : " + topic.name());
			buf.append("<br>　　isInternal : " + topic.isInternal());
			buf.append("<br>　　-------------");
		});
		return buf.toString();
	}
	
	/**
	 * 展示主题列表下拉框
	 */
	@RequestMapping("/getTopicListJson")
	public String getTopicListJson() {
		AdminClient adminClient = kafkaTemplateConfig.getAdminClient();
		ListTopicsResult result = adminClient.listTopics();
		
		KafkaFuture<Set<String>> future = result.names();
		
		Set<String> list = null;
		try {
			list = future.get(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			return OtherUtil.getNow() + "<br>没有任何主题";
		}
		
		String str = Json.encodeAsString(list);
		
		return str;
	}

}
