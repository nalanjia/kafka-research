package com.aebiz.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.util.ConsumerGroupUtil;
import com.aebiz.vo.ResearchGroupDTO;
import com.aebiz.vo.ResearchPartitionInfoDTO;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

	@Autowired
	private KafkaResearchConfig kafkaTemplateConfig;
	
	/**
	 * 查询消费者组列表
	 * http://localhost:9201/consumer/listGroupId
	 */
	@RequestMapping("/listGroupId")
	public List<ResearchGroupDTO> listGroupId() {
		List<ResearchGroupDTO> retList = new ArrayList<>();
		
		AdminClient adminClient = kafkaTemplateConfig.getAdminClient();
		ListConsumerGroupsResult result = adminClient.listConsumerGroups();
		KafkaFuture<Collection<ConsumerGroupListing>> future = result.all();
		
		Collection<ConsumerGroupListing> list = null;
		try {
			list = future.get(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			return Collections.EMPTY_LIST;
		}
		
		list.forEach(t -> {
			ResearchGroupDTO groupEle = new ResearchGroupDTO();
			groupEle.setCustomerGroupId(t.groupId());
			groupEle.setSimpleConsumerGroup(t.isSimpleConsumerGroup());
			retList.add(groupEle);
		});
		
		return retList;
	}
	
	/**
	 * 查询消费者组详情
	 * http://localhost:9201/consumer/getConsumerGroup?groupId=mygroup
	 */
	@RequestMapping("/getConsumerGroup")
	public List<ResearchPartitionInfoDTO> getConsumerGroup(@RequestParam("groupId") String groupId) {
		List<ResearchPartitionInfoDTO> list = ConsumerGroupUtil.getConsumerGroup(groupId);
		return list;
	}	
	
	/**
	 * 删除消费者组
	 * http://localhost:9201/consumer/deleteConsumerGroup?groupId=mygroup
	 */
	@RequestMapping("/deleteConsumerGroup")
	public String deleteConsumerGroup(@RequestParam("groupId") String groupId) {
		String res = ConsumerGroupUtil.deleteConsumerGroup(groupId);
		return res;
	}
	
	/**
	 * 新建消费者组
	 * http://localhost:9201/consumer/createConsumerGroup?topicName=topic_2p_1r&num=2
	 */
	@RequestMapping("/createConsumerGroup")
	public String createConsumerGroup(@RequestParam("topicName") String topicName,
			@RequestParam("num") int num) {
		String res = ConsumerGroupUtil.createConsumerGroup(topicName, num);
		return res;
	}
	
}
