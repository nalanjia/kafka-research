package com.aebiz.controller;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.util.TopicUtil;
import com.aebiz.vo.ResearchTopicInfoDTO;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/topic")
public class TopicController {

	@Autowired
	private KafkaResearchConfig kafkaTemplateConfig;
	
	/**
	 * 查询主题列表
	 * http://localhost:9201/topic/getTopicList
	 */
	@RequestMapping("/getTopicList")
	public Set<String> getTopicList() {
		AdminClient adminClient = kafkaTemplateConfig.getAdminClient();
		ListTopicsResult result = adminClient.listTopics();
		
		KafkaFuture<Set<String>> future = result.names();
		Set<String> list = null;
		try {
			list = future.get(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 查询主题具体信息
	 * http://localhost:9201/topic/getTopicDetail?topicName=topic_2p_1r
	 */
	@RequestMapping("/getTopicDetail")
	public ResearchTopicInfoDTO getTopicDetail(@RequestParam("topicName") String topicName) {
		ResearchTopicInfoDTO dto = TopicUtil.getTopicDetail(topicName);
		return dto;
	}

}
