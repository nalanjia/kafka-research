package com.aebiz.config.consumer;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageHeaderAccessor;

import com.aebiz.util.DateUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Data
public class KafkaListenerConfig {

	/**
	 * 可配置在application.properties
	 */
	@Value("${kafkalistener.client.id:myCID}")
	private String myClientIdPrefix;
	
	//topicPattern：可在配置文件配置，支持正则
	//id：可在配置文件配置，用于支持暂停、恢复
	//errorHandler：错误处理
	
	//beanRef：当前类的别名
	//clientIdPrefix：从当前类的普通属性中取值，这叫SpEl表达式方式取值
	@KafkaListener(
			topicPattern = "${kafkalistener.topicpattern:research_2p_1r.*}"
			, id = "${kafkalistener.id:mygroup}"
			, errorHandler = "myErrorHandler"
			, autoStartup = "true"
			
			, beanRef = "__myLisBean"
			, clientIdPrefix = "#{__myLisBean.myClientIdPrefix}"
			
			, concurrency = "1"
			)
    public void listen(
    		String msg, //消息体
    		Acknowledgment ack, //手动提交
    		ConsumerRecord consumerRecord, //不仅消息，以及其他全部元数据
    		@Payload String payload, //消息体
    		@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, //单个元数据
    		@Header(KafkaHeaders.RECEIVED_PARTITION_ID) String partition, //单个元数据
    		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic, //单个元数据
    		@Header(KafkaHeaders.RECEIVED_TIMESTAMP) String timestamp, //单个元数据
    		@Headers Map headers, //全部元数据，比ConsumerRecord还全
    		MessageHeaders messageHeaders, //和@Headers一样
    		MessageHeaderAccessor messageHeaderAccessor //全部元数据，比@Headers还全
    		) {
		//睡几秒。测试超过max.poll.interval.ms是否会报CommitFailedException
		//继而导致消费者组重平衡
//		OtherUtil.sleep(6000);
		
		//测试myErrorHandler，错误会被myErrorHandler处理
		if("specialMsg".equals(msg)) {
			throw new RuntimeException("处理消息失败，不提交消息会怎么样？");
		}
		
		log.debug("正常reveive"
				+ "\nmsg : " + msg
				+ "\nconsumerRecord : " + consumerRecord
				+ "\npayload : " + payload
				+ "\nkey : " + key
				+ "\npartition : " + partition
				+ "\ntopic : " + topic
				+ "\ntimestamp : " + timestamp
				+ "\ntimestampStr : " + DateUtil.parseTime_EN(Long.valueOf(timestamp))
				+ "\nheaders : " + headers
				+ "\nmessageHeaders : " + messageHeaders
				+ "\nMessageHeaderAccessor : " + messageHeaderAccessor
				
				);
		//手动提交
		ack.acknowledge();
    }
	
}
