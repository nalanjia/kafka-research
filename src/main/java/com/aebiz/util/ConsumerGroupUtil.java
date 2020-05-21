package com.aebiz.util;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.WakeupException;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.config.SpringBeanTool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerGroupUtil {

	private static Map<String, KafkaConsumer> mapConsumerGroup = new HashMap<>();
	
	/**
	 * 新建消费者组
	 */
	public static String createConsumerGroup(String topicName, int num) {
		String uuid = GeneDataUtil.geneUuid();
		String now = DateUtil.getNowDate_EN().replace("-", "");
		String groupId = now + "-" + uuid;
		
		newConsumerGroupThread thread = new newConsumerGroupThread(groupId, topicName, num);
		thread.start();
		return groupId;
	}
	
	/**
	 * 删除消费者组
	 */
	public static String deleteConsumerGroup(String groupId) {
		//终止消费者组线程
		KafkaConsumer consumer = mapConsumerGroup.get(groupId);
		if(consumer != null) {
			//触发WakeupException，以便退出while
			//退出while之后，会consumer.close()
			consumer.wakeup();
			mapConsumerGroup.remove(groupId);
		}
		
		//删除远程kafka服务器的消费者组
		KafkaResearchConfig config = SpringBeanTool.getBean(KafkaResearchConfig.class);
		AdminClient adminClient = config.getAdminClient();
		
		DeleteConsumerGroupsResult res = adminClient.deleteConsumerGroups(Collections.singletonList(groupId));
		KafkaFuture<Void> future = res.all();
		try {
			//上边的consumer.wakeup()，导致consumer.close()，而close可能需要30秒
			//所以，这里多等会儿。给足时间让其顺利close
			Void v = future.get(40, TimeUnit.SECONDS);
			return KaResearchConstant.RES_SUCCESS;
		} catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	
	/**
	 * 新建消费者组
	 */
	static class newConsumerGroupThread extends Thread {
		private String topicName;
		private String groupId;
		/**
		 * 每秒消费数量
		 */
		private int num;

		public newConsumerGroupThread(String groupId, String topicName, int num) {
			super();
			this.groupId = groupId;
			this.topicName = topicName;
			this.num = num;
		}

		@Override
		public void run() {
			KafkaResearchConfig config = SpringBeanTool.getBean(KafkaResearchConfig.class);
			//application.properties的属性
			Map<String, Object> configs = config.getKafkaProperties().buildConsumerProperties();
			//覆盖application.properties的属性
			configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); //poll数量
			configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); //自动提交
			configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); //从头儿消费
			
			//缓存消费者组线程
			KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
			mapConsumerGroup.put(groupId, consumer);
			
			try {
				consumer.subscribe(Collections.singletonList(topicName));
				int totalCount = 0; //用于丰富日志
				
				int limitCount = 0; //达到num，不足1秒则sleep
				long limitStartTime = System.currentTimeMillis();
				while (true) {
					//poll不到消息，则阻塞。阻塞时间结束，往下执行
					//poll到消息，往下执行
					long t1 = System.currentTimeMillis();
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));
					long t2 = System.currentTimeMillis();
					System.out.println("poll耗时[" + (t2 - t1) + "]ms，查询到消息数量[" + records.count() + "]");
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
			} catch (WakeupException e) {
				//调用consumer.wakeup()会唤醒poll的阻塞
				log.debug("groupId[" + groupId + "] WakeupException.........................");
			} finally {
				log.debug("groupId[" + groupId + "]finally consumer is closed.........................");
				consumer.close();
			}
					
		}
		
	}
}
