package com.aebiz.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.RequestMapping;
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
	
}
