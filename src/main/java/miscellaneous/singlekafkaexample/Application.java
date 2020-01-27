package miscellaneous.singlekafkaexample;

import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import com.aebiz.util.KaResearchConstant;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@Slf4j
public class Application 
implements CommandLineRunner 
{

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private KafkaTemplate<String, String> template;

    private final CountDownLatch latch = new CountDownLatch(3);

    @Override
    public void run(String... args) throws Exception {
//        this.template.send(KaResearchConstant.TOPIC_HEALTH, "ttttttttttttt1");
//        this.template.send(KaResearchConstant.TOPIC_HEALTH, "ttttttttttttt2");
//        this.template.send(KaResearchConstant.TOPIC_HEALTH, "ttttttttttttt3");
//        latch.await(60, TimeUnit.SECONDS);
//        log.info("All received");
    }

    @KafkaListener(topics = KaResearchConstant.TOPIC_HEALTH)
    public void listen(ConsumerRecord<?, ?> cr) throws Exception {
        log.info("消费消息 : " + cr.toString());
//        latch.countDown();
    }

	/**
	 * KafkaAdmin扫描NewTopic，并负责新建
	 */
	@Bean
    public NewTopic initTopicHealth() {
		//分区
		int numPartitions = 1;
		//副本
		short replicationFactor = 1;
		
		NewTopic topic = new NewTopic(KaResearchConstant.TOPIC_HEALTH, numPartitions, replicationFactor);
		return topic;
    }
}