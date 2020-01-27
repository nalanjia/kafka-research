package com.aebiz.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import kafka.utils.Json;
import lombok.extern.slf4j.Slf4j;

/**
 * springboot的kafka用法：
 * https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/htmlsingle/#legal
 * 4.13.3. Apache Kafka Support
 * 
 * spring-kafka的用法：
 * https://docs.spring.io/spring-kafka/docs/2.3.1.RELEASE/reference/html/#introduction
 * 
 * springboot的kafka配置文件：
 * https://github.com/spring-projects/spring-boot/blob/v2.2.1.RELEASE/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/kafka/KafkaProperties.java
 * kafka官方的配置：
 * http://kafka.apache.org/23/documentation.html
 */
@Component
@Slf4j
public class KafkaResearchConfig {

	private KafkaTemplate<String, String> kafkaTemplate;
	private KafkaAdmin kafkaAdmin;
	/**
	 * 管理kafka
	 */
	private AdminClient adminClient;
	/**
	 * 监听器生命周期。
	 * 监听器KafkaListener注册在kafkaListenerEndpointRegistry
	 * 而不是IOC容器中注册为Bean
	 * 所以管理KafkaListener是通过kafkaListenerEndpointRegistry来操作的
	 */
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
	/**
	 * spring kafka配置，主要为了拿到配置
	 */
	private KafkaProperties kafkaProperties;
	/**
	 * 消费者（没有默认的bean暴露，得自己新建）
	 */
	private KafkaConsumer kafkaConsumer;
	
	/**
	 * 消费者（没有默认的bean暴露，得自己新建）
	 */
	@Bean
	@ConditionalOnMissingBean(ProducerFactory.class)
	public KafkaConsumer initKafkaConsumer() {
		if(this.kafkaConsumer == null) {
			Map<String, Object> configs = this.getKafkaProperties().buildConsumerProperties();
			KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
			this.kafkaConsumer = consumer;
		}
		return this.kafkaConsumer;
	}
	
	
	@Value("${spring.kafka.admin.fail-fast:false}")
	private String failFast;
	
    @Autowired
    public void initKafkaListenerEndpointRegistry(KafkaListenerEndpointRegistry ele) {
    	this.kafkaListenerEndpointRegistry = ele;
    	log.debug("项目启动时，已初始化KafkaListenerEndpointRegistry : " + ele);
    }
    @Autowired
    public void initKafkaProperties(KafkaProperties ele) {
    	this.kafkaProperties = ele;
    	log.debug("项目启动时，已初始化KafkaProperties : " + ele);
    }
    @Autowired
    public void initKafkaTemplate(KafkaTemplate<String, String> ele) {
    	this.kafkaTemplate = ele;
    	log.debug("项目启动时，已初始化kafkaTemplate : " + ele);
    }
    
    /**
     * KafkaAutoConfiguration负责初始化KafkaAdmin
     */
    @Autowired
    public void initKafkaAdmin(KafkaAdmin ele) {
    	this.kafkaAdmin = ele;
    	log.debug("项目启动时，已初始化kafkaAdmin : " + ele);

    	//spring.kafka.admin.properties.bootstrap.servers
    	//spring.kafka.bootstrap-servers
    	Object kafkaServer = ele.getConfig().get("bootstrap.servers");
    	if("true".equals(failFast)) {
    		log.debug("正在连接" + Json.encodeAsString(kafkaServer) + "并新建主题，无法连通的话，项目是启动不了的！不报错，那表示一切正常！");
    	}
    	
    	if("false".equals(failFast)) {
    		log.debug("正在连接" + Json.encodeAsString(kafkaServer) + "并新建主题，无法连通的话，项目启动不受影响");
    	}
    }

	public KafkaTemplate<String, String> getKafkaTemplate() {
		return this.kafkaTemplate;
	}
	public KafkaAdmin getKafkaAdmin() {
		return this.kafkaAdmin;
	}
	public KafkaListenerEndpointRegistry getKafkaListenerEndpointRegistry() {
		return kafkaListenerEndpointRegistry;
	}
	public AdminClient getAdminClient() {
		if(this.adminClient == null) {
			KafkaAdmin kafkaAdmin = this.getKafkaAdmin();
			AdminClient client = AdminClient.create(kafkaAdmin.getConfig());
			this.adminClient = client;
		}
		return this.adminClient;
	}
	public KafkaProperties getKafkaProperties() {
		return kafkaProperties;
	}
	public KafkaConsumer getKafkaConsumer() {
		return kafkaConsumer;
	}
	
}
