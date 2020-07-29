package com.aebiz.interceptor;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class ConsumerInterceptor2 implements ConsumerInterceptor {

	@Override
	public void configure(Map<String, ?> configs) {
		
	}

	/**
	 * 该方法在消息返回给 Consumer 程序之前调用。也就是说在开始正式处理消息之前，拦截器会先拦一道，搞一些事情，之后再返回给你
	 */
	@Override
	public ConsumerRecords onConsume(ConsumerRecords records) {
		System.out.println("ConsumerInterceptor2");
		
		return records;
	}

	/**
	 * Consumer 在提交位移之后调用该方法。通常你可以在该方法中做一些记账类的动作，比如打日志等
	 */
	@Override
	public void onCommit(Map offsets) {
		
	}

	@Override
	public void close() {
		
	}

}
