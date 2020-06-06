package com.aebiz.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.config.SpringBeanTool;
import com.aebiz.vo.ResearchPartitionInfoDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerGroupUtil {

	/**
	 * 新建的消费者保存起来，以便销毁消费者时使用
	 */
	private static Map<String, NewConsumerGroupThread> mapConsumerGroup = new HashMap<>();
	
	/**
	 * 新建消费者组
	 */
	public static String createConsumerGroup(String topicName, int num) {
		String groupId = ConsumerUtil.getGroupId();
		
		NewConsumerGroupThread thread = new NewConsumerGroupThread(groupId, topicName, num);
		mapConsumerGroup.put(groupId, thread);
		thread.start();
		return groupId;
	}
	
	/**
	 * 删除消费者组
	 */
	public static String deleteConsumerGroup(String groupId) {
		//终止消费者组线程
		NewConsumerGroupThread thread = mapConsumerGroup.get(groupId);
		if(thread != null) {
			//结束线程
			thread.stopRunning();
			//从缓存中删除
			mapConsumerGroup.remove(groupId);
		}
		
		//删除远程kafka服务器的消费者组
		int length = MAX_POLL_RECORDS_NUM * 2;
		for(int i = 0; i < length; i++) {
			KafkaResearchConfig config = SpringBeanTool.getBean(KafkaResearchConfig.class);
			AdminClient adminClient = config.getAdminClient();
			
			DeleteConsumerGroupsResult res = adminClient.deleteConsumerGroups(Collections.singletonList(groupId));
			KafkaFuture<Void> future = res.all();
			try {
				Void v = future.get(5, TimeUnit.SECONDS);
				System.out.println("尝试关闭消费者组第[" + (i+1) + "/" + length + "]次，成功！");
				return KaResearchConstant.RES_SUCCESS;
			} catch(Exception e) {
				System.out.println("尝试关闭消费者组第[" + (i+1) + "/" + length + "]次，失败！报错为" + e.getMessage());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		return KaResearchConstant.RES_SUCCESS;
	}
	
	/**
	 * 查询消费者组
	 */
	public static List<ResearchPartitionInfoDTO> getConsumerGroup(String groupId) {
		List<ResearchPartitionInfoDTO> retList = new ArrayList<>();
		
		KafkaResearchConfig config = SpringBeanTool.getBean(KafkaResearchConfig.class);
		AdminClient adminClient = config.getAdminClient();
		//所有主题的所有分区、及其消费情况
		ListConsumerGroupOffsetsResult result = adminClient.listConsumerGroupOffsets(groupId);
		KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> future = result.partitionsToOffsetAndMetadata();
		Map<TopicPartition, OffsetAndMetadata> map;
		try {
			map = future.get(5, TimeUnit.SECONDS);
		} catch(Exception e) {
			return retList;
		}
		
		map.forEach((k, v) -> {
			ResearchPartitionInfoDTO ele = new ResearchPartitionInfoDTO();
			ele.setTopicName(k.topic());
			ele.setPartition(k.partition());
			ele.setOffset(v.offset());
			retList.add(ele);
		});
		
		//排序
		retList.stream()
		.sorted(Comparator.comparing(ResearchPartitionInfoDTO::getPartition))
		.collect(Collectors.toList());
		
		return retList;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * poll得到的消息数量
	 */
	private static int MAX_POLL_RECORDS_NUM = 5;
	/**
	 * 新建消费者
	 */
	static class NewConsumerGroupThread extends Thread {
		//用于关闭消费者
		private AtomicBoolean running = new AtomicBoolean(true);
		
		//消费者组
		private String groupId;
		//用于新建KafkaConsumer
		private String topicName;
		
		//新建出来的KafkaConsumer
		private KafkaConsumer consumer;
		
		//每秒消费数量，用于控制消费速度
		private int num;
		
		//处理消息数量累加，用于丰富日志
		int totalCount = 0;
		
		//达到num，不足1秒则sleep，凑够1秒。以此控制消费速度
		int limitCount = 0; 
		//达到num的起始时刻
		long limitStartTime = System.currentTimeMillis();

		public NewConsumerGroupThread(String groupId, String topicName, int num) {
			this.groupId = groupId;
			this.topicName = topicName;
			this.num = num;
		}

		@Override
		public void run() {
			//新建消费者
			this.consumer = ConsumerUtil.getKafkaConsumer(this.groupId);
			
			try {
				//订阅主题
				this.consumer.subscribe(Collections.singletonList(topicName));
				
				//循环取消息
				while (this.running.get()) {
					//poll不到消息，则阻塞。阻塞时间结束，往下执行
					//poll到消息，往下执行
					long t1 = System.currentTimeMillis();
					ConsumerRecords<String, String> records = consumer.poll(
							Duration.ofMillis(5000)
					);
					
					long t2 = System.currentTimeMillis();
					System.out.println("poll耗时[" + (t2 - t1) + "]ms，查询到消息数量[" + records.count() + "]");
					
					//处理消息
					this.handleRecords(records);
				}
			} catch (WakeupException e) {
				//调用consumer.wakeup()会唤醒poll的阻塞
				log.debug("groupId[" + this.groupId + "] WakeupException.........................");
			} finally {
				log.debug("groupId[" + this.groupId + "] finally consumer is closed.........................");
				//关闭consumer
				ConsumerUtil.closeKafkaConsumer(this.consumer);
			}
					
		}
		
		private void handleRecords(ConsumerRecords<String, String> records) {
			for (ConsumerRecord<String, String> record : records) {
                Map<String, Object> data = new HashMap<>();
                data.put("partition", record.partition());
                data.put("offset", record.offset());
                data.put("value", record.value());
                System.out.println("第[" + (++totalCount) + "]个 : " + data);
                
                //攒数量
                limitCount++;
                //数量够了，看时间够否
                if(limitCount >= num) {
                	long limitEndTime = System.currentTimeMillis();
                	long costMs = limitEndTime - limitStartTime;
                	long leftMs = 1000 - costMs;
                	
                	//多余的时间，sleep吧
                	if(leftMs > 0) {
                		System.out.println("消费数量达到了[" + num + "]，耗时[" + costMs + "]ms，剩余的[" + leftMs + "]ms将sleep......");
                		try {
                			Thread.sleep(leftMs);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
                	} else {
                		System.out.println("消费数量达到了[" + num + "]，但是耗时[" + costMs + "]ms超过了1000ms");
                	}
                	//数量、时间重置
                	limitCount = 0;
                	limitStartTime = System.currentTimeMillis();
                }
            }
		}
		
		/**
		 * 结束线程的2个方式，同时使用，以确保顺利结束线程
		 * 1.直接退出while
		 * 2.退出poll的阻塞，达到退出while的目的
		 */
		public void stopRunning() {
			log.debug("groupId[" + this.groupId + "] stopRunning.........................");
			//退出while
			this.running.set(false);
			
			//触发WakeupException，用于跳出poll的阻塞
			this.consumer.wakeup();
		}
		
	}
}
