package com.aebiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.aebiz.config.SpringBeanTool;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class Main {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Main.class, args);
		//填充上下文
		SpringBeanTool.setApplicationContext(context);
		log.debug("kafka-research started !");
	}
}
