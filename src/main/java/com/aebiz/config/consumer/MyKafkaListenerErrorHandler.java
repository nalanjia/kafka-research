package com.aebiz.config.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("myErrorHandler")
@Slf4j
public class MyKafkaListenerErrorHandler implements KafkaListenerErrorHandler {
	@Override
	public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
		log.error("监听出错了 : " + message.getPayload().toString());
		return null;
	}
	@Override
	public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
		log.error("监听真的出错了1 : " + message);
		log.error("监听真的出错了2 : " + exception);
		log.error("监听真的出错了3 : " + consumer);
		return null;
	}
}
