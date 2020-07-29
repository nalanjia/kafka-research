package com.aebiz.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
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
		//ack为all时，多少副本收到消息才算已提交。通常设置为副本数减1
		int minInsyncReplicas = this.prepareMinInsync(replicationFactor);
				
		
		NewTopic topic = TopicBuilder.name(KaResearchConstant.TOPIC_HEALTH)
		        .partitions(numPartitions)
		        .replicas(replicationFactor)
		        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInsyncReplicas + "")
		        
		        .build();
		
		return topic;
    }
	
	@Bean
    public NewTopic initTopic1p1r() {
		//分区
		int numPartitions = 1;
		//副本
		short replicationFactor = 1;
		//ack为all时，多少副本收到消息才算已提交。通常设置为副本数减1
		int minInsyncReplicas = this.prepareMinInsync(replicationFactor);
		
		NewTopic topic = TopicBuilder.name(KaResearchConstant.TOPIC_1P_1R)
					.partitions(numPartitions)
					.replicas(replicationFactor)
					.config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInsyncReplicas + "")
					
					.build();
		return topic;
    }
	
	@Bean
    public NewTopic initTopic2p1r() {
		//分区
		int numPartitions = 2;
		//副本
		short replicationFactor = 1;
		//ack为all时，多少副本收到消息才算已提交。通常设置为副本数减1
		int minInsyncReplicas = this.prepareMinInsync(replicationFactor);
		
		NewTopic topic = TopicBuilder.name(KaResearchConstant.TOPIC_2P_1R)
					.partitions(numPartitions)
					.replicas(replicationFactor)
					.config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInsyncReplicas + "")
					
					.build();
		return topic;
    }	

	@Bean
    public NewTopic initTopicIp() {
		//分区
		int numPartitions = 1;
		//副本
		short replicationFactor = 1;
		//ack为all时，多少副本收到消息才算已提交。通常设置为副本数减1
		int minInsyncReplicas = this.prepareMinInsync(replicationFactor);
		
		NewTopic topic = TopicBuilder.name(KaResearchConstant.TOPIC_IP)
					.partitions(numPartitions)
					.replicas(replicationFactor)
					.config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInsyncReplicas + "")
					
					.build();
		return topic;
    }
	
	private int prepareMinInsync(short replicationFactor) {
		if(replicationFactor == 1) {
			return 1;
		}
		return replicationFactor - 1;
	}
	
}
