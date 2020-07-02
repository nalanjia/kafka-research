package com.aebiz.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.util.ConsumerGroupUtil;
import com.aebiz.util.ConsumerUtil;
import com.aebiz.util.DateUtil;
import com.aebiz.vo.ResearchPager;
import com.aebiz.vo.ResearchPagerEleDTO;

@RestController
@RequestMapping("/consumer/pager")
public class ConsumerPagerController {

	/**
	 * http://localhost:9201/consumer/pager/queryList?topicName=research_ip&nowPage=1&pageShow=10
	 */
	@RequestMapping("/queryList")
	public ResearchPager queryList(@RequestParam("topicName") String topicName,
			@RequestParam("nowPage") int nowPage,
			@RequestParam("pageShow") int pageShow,
			@RequestParam(value = "beginTime", required = false) String beginTime,
			@RequestParam(value = "endTime", required = false) String endTime) {
		long t1 = System.currentTimeMillis();
		ResearchPager pager = new ResearchPager();
		pager.setNowPage(nowPage);
		pager.setPageShow(pageShow);
		
		String groupId = ConsumerUtil.getGroupId();
		KafkaConsumer consumer = ConsumerUtil.getKafkaConsumer(groupId);
		
		//查询该主题的所有分区
		List<TopicPartition> tpList = this.prepareTpList(consumer, topicName);
		//为消费者指定分区
		consumer.assign(tpList);
		
		//查询各个分区的first offset，end offset
		Map<TopicPartition, Long> fMap = consumer.beginningOffsets(tpList);
		Map<TopicPartition, Long> eMap = consumer.endOffsets(tpList);
		
		//消息总数
		long msgNum = this.prepareAllMsgNum(tpList, fMap, eMap);
		if(msgNum == 0) {
			return pager;
		}
		pager.setAllMsgNum(Long.valueOf(msgNum).intValue());
		
		//根据起始时刻，查询出对应的消息位置（每个分区）
		Map<TopicPartition, OffsetAndTimestamp> startMap
			= this.prepareStartOffset(consumer, tpList, beginTime);
		//根据结束时刻，查询出对应的消息位置（每个分区）
		Map<TopicPartition, OffsetAndTimestamp> endMap 
			= this.prepareEndOffset(consumer, tpList, endTime);
		
		//查询条件下的消息总数量
		long logSize = this.prepareTotalNum(consumer, tpList, startMap, endMap, eMap);
		if(logSize == 0) {
			return pager;
		}
		pager.setTotalNum(Long.valueOf(logSize).intValue());
		
		//消费消息之前，设置消息的起点位置
		for(TopicPartition p : tpList) {
			OffsetAndTimestamp o1 = startMap.get(p);
			consumer.seek(p, o1.offset());
		}
		
		//已经跳过的数量，用于跳过页数
		int acceptedCount = 0;
		//应该跳过的数量
		int shouldSkipNum = (nowPage - 1) * pageShow;
		
		long all2 = System.currentTimeMillis();
		//System.out.println("before : " + (all2 - t1));
		
//		System.out.println("nowPage : " + nowPage);
//		System.out.println("pageShow : " + pageShow);
//		System.out.println("acceptedCount : " + acceptedCount);
//		System.out.println("shouldSkipNum : " + shouldSkipNum);
		
		//消费消息
		outerFor:
		for(;;) {
			long c1 = System.currentTimeMillis();
			ConsumerRecords<String, String> records = consumer.poll(
					Duration.ofMillis(100)
			);
			
			long c2 = System.currentTimeMillis();
			//System.out.println("poll : " + (c2 - c1));
			
			if(records.isEmpty()) {
				break outerFor;
			}
			
			long a1 = System.currentTimeMillis();
			innerFor:
			for(ConsumerRecord<String, String> record : records) {
				//这条消息不属于目标页，则跳过这条消息
				acceptedCount++;
				if(acceptedCount <= shouldSkipNum) {
					continue innerFor;
				}
				
				//够一页了
				if(pager.getResults().size() >= pager.getPageShow()) {
					break outerFor;
				}
				
				//结束位置（找不到则取尾消息）
				long endOffset = 0;
				TopicPartition tp = new TopicPartition(record.topic(), record.partition());
				OffsetAndTimestamp o2 = endMap.get(tp);
				if(o2 == null) {
					Long tailLong = eMap.get(tp);
					endOffset = tailLong.longValue();
				} else {
					endOffset = o2.offset();
				}
				
				//超过查询条件所限制的结束位置了
				if(record.offset() >= endOffset) {
					break outerFor;
				}
				
				ResearchPagerEleDTO ele = new ResearchPagerEleDTO();
				ele.setTopicName(record.topic());
				ele.setPartition(record.partition());
				ele.setOffset(record.offset());
				ele.setMsgKey(record.key());
				ele.setMsgTimestamp(record.timestamp());
				ele.setMsgTimestampType(record.timestampType().name());
				ele.setMsg(record.value());
				
				String str = DateUtil.formatLong(record.timestamp());
				ele.setMsgTimestampStr(str);
				
				pager.getResults().add(ele);
			}
			long a2 = System.currentTimeMillis();
			//System.out.println("handle : " + (a2 - a1));
		}
		
		long b1 = System.currentTimeMillis();
		ConsumerGroupUtil.deleteConsumerGroup(groupId);
		long b2 = System.currentTimeMillis();
		//System.out.println("close :" + (b2 - b1));

		long t2 = System.currentTimeMillis();
		pager.setCostMs(t2 - t1);
		return pager;
	}
	
	/**
	 * 根据时刻，查询对应的offset
	 */
	private Map<TopicPartition, OffsetAndTimestamp> prepareStartOffset(KafkaConsumer consumer, List<TopicPartition> tpList, String beginTime) {
		String aTime = "1970-01-01 08:00:00";
		if(StringUtils.isNotBlank(beginTime)) {
			aTime = beginTime;
		}
		long aLong = DateUtil.getLong(aTime);
		
		Map<TopicPartition, Long> qm = new HashMap();
		//所有分区，都设置为这个时刻
		tpList.forEach(t -> {
			qm.put(t, aLong);
		});
		
		Map<TopicPartition, OffsetAndTimestamp> startMap = consumer.offsetsForTimes(qm);
		return startMap;
	}
	
	/**
	 * 根据时刻，查询对应的offset
	 */
	private Map<TopicPartition, OffsetAndTimestamp> prepareEndOffset(KafkaConsumer consumer, List<TopicPartition> tpList, String endTime) {
		String bTime = "2100-01-01 08:00:00";
		if(StringUtils.isNotBlank(endTime)) {
			bTime = endTime;
		}
		long bLong = DateUtil.getLong(bTime);	
		
		Map<TopicPartition, Long> qm = new HashMap();
		//所有分区，都设置为这个时刻
		tpList.forEach(t -> {
			qm.put(t, bLong);
		});
		
		//查询出结束位置
		Map<TopicPartition, OffsetAndTimestamp> endMap = consumer.offsetsForTimes(qm);
		return endMap;
	}
	
	private long prepareTotalNum(KafkaConsumer consumer,
			List<TopicPartition> tpList,
			Map<TopicPartition, OffsetAndTimestamp> startMap,
			Map<TopicPartition, OffsetAndTimestamp> endMap,
			Map<TopicPartition, Long> eMap) {
		long logSize = 0;
		for(TopicPartition p : tpList) {
			OffsetAndTimestamp o1 = startMap.get(p);
			OffsetAndTimestamp o2 = endMap.get(p);

			//起点时刻，找不到对应的offset
			if(o1 == null) {
				continue;
			}
			
			//结束时刻，找不到对应的offset，则查询该分区末尾offset
			if(o2 == null) {
				//
				Long tailLong = eMap.get(p);
				logSize += tailLong.longValue() - o1.offset();
			} else {
				//两者相减，得到消息数量
				logSize += o2.offset() - o1.offset();
			}
		}
		return logSize;
	}
	
	private List<TopicPartition> prepareTpList(KafkaConsumer consumer, String topicName) {
		List<TopicPartition> tpList = new ArrayList<>();
		
		List<PartitionInfo> pList = consumer.partitionsFor(topicName);
		pList.forEach(p -> {
			TopicPartition tp = new TopicPartition(topicName, p.partition());
			tpList.add(tp);
		});		
		return tpList;
	}
	
	private long prepareAllMsgNum(List<TopicPartition> tpList,
			Map<TopicPartition, Long> fMap,
			Map<TopicPartition, Long> eMap) {
		//所有分区的消息总数
		long msgNum = 0;
		for(TopicPartition t : tpList) {
			//不会返回null的
			Long beginOffset = fMap.get(t);
			Long endOffset = eMap.get(t);
			
			msgNum += endOffset.longValue() - beginOffset.longValue();
		}
		return msgNum;
	}
	
}
