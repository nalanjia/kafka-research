package com.aebiz.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.stereotype.Component;

import com.aebiz.util.KaResearchConstant;

/**
 * 新建主题
 * KafkaAdmin扫描NewTopic，并负责新建
 * 
 * KafkaAdmin的扫描代码如下：
 * Collection<NewTopic> newTopics = this.applicationContext.getBeansOfType(NewTopic.class, false, false).values();
 */
@Component
public class NewTopicConfig {
	
	@Bean
    public NewTopic initTopicHealth() {
		//分区
		int numPartitions = 1;
		//副本
		short replicationFactor = 1;
		
		NewTopic topic = TopicBuilder.name(KaResearchConstant.TOPIC_HEALTH)
		        .partitions(numPartitions)
		        .replicas(replicationFactor)
		        .compact()
		        .build();
		
		return topic;
    }
	
	@Bean
    public NewTopic initTopic1p1r() {
		//分区
		int numPartitions = 1;
		//副本
		short replicationFactor = 1;
		
		NewTopic topic = TopicBuilder.name(KaResearchConstant.TOPIC_1P_1R)
					.partitions(numPartitions)
					.replicas(replicationFactor)
					.compact()
					.build();
		return topic;
    }
	
	@Bean
    public NewTopic initTopic2p1r() {
		//分区
		int numPartitions = 2;
		//副本
		short replicationFactor = 1;
		
		NewTopic topic = TopicBuilder.name(KaResearchConstant.TOPIC_2P_1R)
					.partitions(numPartitions)
					.replicas(replicationFactor)
					.compact()
					.build();
		return topic;
    }	
	
}
