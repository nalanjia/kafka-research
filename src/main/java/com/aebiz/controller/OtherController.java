package com.aebiz.controller;

import java.util.List;

import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.util.KaResearchConstant;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/other")
public class OtherController {

	@Autowired
	private KafkaResearchConfig kafkaTemplateConfig;
	/**
	 * 集群健康检查
	 * http://localhost:9201/other/healthCheck
	 */
	@RequestMapping("/healthCheck")
	public String healthCheck() {
		KafkaTemplate template = kafkaTemplateConfig.getKafkaTemplate();
		List<PartitionInfo> partitions;
		try {
			//broker如果允许自动新建索引，auto.create.topics.enable=true
			//那么可以查询到数量
			partitions = template.partitionsFor(KaResearchConstant.TOPIC_HEALTH);
		} catch(Exception e) {
			//broker如果不允许自动新建索引，auto.create.topics.enable=false
			//并且真的没有topic_health这个主题
			//那么则超时，spring.kafka.producer.max-block-ms=4000
			return "Fail, " + e.getMessage();
		}
		return "Success, num is " + partitions.size();
	}

}
