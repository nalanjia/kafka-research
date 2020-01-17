package com.aebiz.config.consumer;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import com.aebiz.util.OtherUtil;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaListenerConfig {

	//正则监听主题，注意*前边的.
	@KafkaListener(topicPattern = "${topic.1p.1r:topic.1p.1r.*}"
			, id = "group-1p1r"
			, errorHandler = "myErrorHandler"
			)
    public void listen(String msg, Acknowledgment ack) {
		//睡几秒。测试超过max.poll.interval.ms是否会报CommitFailedException
		//继而导致消费者组重平衡
//		OtherUtil.sleep(6000);
		
		//测试myErrorHandler，错误会被myErrorHandler处理
		if("specialMsg".equals(msg)) {
			throw new RuntimeException("处理消息失败，不提交消息会怎么样？");
		}
		
		log.debug("正常reveive msg : " + msg);
		ack.acknowledge();
    }
	
}