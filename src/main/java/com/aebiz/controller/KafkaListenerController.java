package com.aebiz.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.util.ConsumerUtil;
import com.aebiz.vo.ResearchListenerContainerDTO;

@RestController
@RequestMapping("/kafkalistener")
public class KafkaListenerController {

	@Autowired
	private KafkaResearchConfig kafkaTemplateConfig;
	
	/**
	 * 查询监听器容器列表
	 * http://localhost:9201/kafkalistener/list
	 */
	@RequestMapping("/list")
	public List<ResearchListenerContainerDTO> list() {
		Collection<MessageListenerContainer> list = kafkaTemplateConfig.getKafkaListenerEndpointRegistry()
			.getAllListenerContainers();
		
		List<ResearchListenerContainerDTO> retList = ConsumerUtil.listListener(list);
		return retList;
	}
	
	/**
	 * 暂停
	 * http://localhost:9201/kafkalistener/pause?listenerId=mygroup
	 */
	@RequestMapping("/pause")
	public void pause(@RequestParam("listenerId") String listenerId) {
		MessageListenerContainer container = kafkaTemplateConfig.getKafkaListenerEndpointRegistry()
			.getListenerContainer(listenerId);
		
		container.pause();
	}
	
	/**
	 * 将暂停恢复为正常
	 * http://localhost:9201/kafkalistener/resume?listenerId=mygroup
	 */
	@RequestMapping("/resume")
	public void resume(@RequestParam("listenerId") String listenerId) {
		MessageListenerContainer container = kafkaTemplateConfig.getKafkaListenerEndpointRegistry()
				.getListenerContainer(listenerId);
		
		container.resume();
	}
	
	/**
	 * 关闭
	 * http://localhost:9201/kafkalistener/stop?listenerId=mygroup
	 */
	@RequestMapping("/stop")
	public void stop(@RequestParam("listenerId") String listenerId) {
		MessageListenerContainer container = kafkaTemplateConfig.getKafkaListenerEndpointRegistry()
			.getListenerContainer(listenerId);
		
		container.stop();
	}
	
	/**
	 * 启动
	 * http://localhost:9201/kafkalistener/start?listenerId=mygroup
	 */
	@RequestMapping("/start")
	public void start(@RequestParam("listenerId") String listenerId) {
		MessageListenerContainer container = kafkaTemplateConfig.getKafkaListenerEndpointRegistry()
				.getListenerContainer(listenerId);
		
		container.start();
	}
	
	
}
