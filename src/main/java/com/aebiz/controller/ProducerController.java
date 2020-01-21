package com.aebiz.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aebiz.config.KafkaResearchConfig;
import com.aebiz.util.GeneDataUtil;
import com.aebiz.util.OtherUtil;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/producer")
public class ProducerController {

	@Autowired
	private KafkaResearchConfig kafkaTemplateConfig;
	
	/**
	 * 向指定topic生产1条消息
	 * http://localhost:9201/producer/addOneMsg?topicName=topic_1p_1r&keyName=key1
	 */
	@RequestMapping("/addOneMsg")
//	@Transactional
	public String addOneMsg(@RequestParam(value="topicName", required=false) String topicName
			, @RequestParam(value="keyName", required=false) String keyName
			, @RequestParam(value="msg", required=false) String msg
			) {
		if(StringUtils.isBlank(topicName)) {
			return "主题是必输项";
		}
		if(StringUtils.isBlank(keyName)) {
			return "消息KEY是必输项";
		}
		
		long t1 = System.currentTimeMillis();
		//生成一条消息，将就着用
		String oneMsg = GeneDataUtil.geneOneMessage();
		//不行，不将就。就要用自己的
		if(StringUtils.isNotBlank(msg)) {
			oneMsg = msg;
		}
		
		KafkaTemplate<String, String> template = kafkaTemplateConfig.getKafkaTemplate();
		
		ListenableFuture<SendResult<String, String>> future = 
				template.send(topicName,
//						null,
//						System.currentTimeMillis() + 100000000l, //测试时间戳
						keyName, oneMsg);

		try {
			//异步，1ms
//			future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
//				@Override
//				public void onSuccess(SendResult<String, String> result) {
//					long t2 = System.currentTimeMillis();
//					System.out.println("成功！" + (t2 -t1) + "ms");
//				}
//				@Override
//				public void onFailure(Throwable ex) {
//					long t2 = System.currentTimeMillis();
//					System.out.println("失败！" + (t2 -t1) + "ms" + "，错误原因为" + ex.getMessage());
//				}
//			});
			
			//同步，10ms，比异步慢10倍
//			SendResult<String, String> ret = future.get(10, TimeUnit.SECONDS);
//			ProducerRecord<String, String> pr = ret.getProducerRecord();
//			RecordMetadata ma = ret.getRecordMetadata();
//			log.debug("ProducerRecord is : " + pr);
//			log.debug("RecordMetadata is : " + ma);
		} catch(Exception e) {
			e.printStackTrace();
			long t2 = System.currentTimeMillis();
			return OtherUtil.getNow() + " Fail, COST IS " + (t2 - t1) + "ms, Exception IS : " + e.getMessage();
		}
		long t2 = System.currentTimeMillis();
		return OtherUtil.getNow() + " Success, COST IS " + (t2 - t1) + "ms";
	}
	
	/**
	 * 向指定topic生产无数条消息，可以设置毫秒间隔
	 * http://localhost:9201/producer/addNMsgs?topicName=topic_1p_1r&keyName=key1&period=5000
	 */
	@RequestMapping("/addNMsgs")
	public String addNMsgs(@RequestParam("topicName") String topicName
			, @RequestParam("keyName") String keyName
			, @RequestParam("period") long period) {
		int count = 0;
		while(true) {
			try {
				//生产一条消息
				this.addOneMsg(topicName, keyName, null);
				count++;
				
				log.debug("第[" + count + "]个消息发送成功！");
				
				Thread.sleep(period);
			} catch(Exception e) {
				log.debug("第[" + count + "]个消息发送失败：" + e.getMessage());
			}
		}
	}
	

}
