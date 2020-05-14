package com.aebiz.config;

import org.springframework.context.ApplicationContext;

public class SpringBeanTool {
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    /**
     * 启动类负责填充上下文
     */
    public static void setApplicationContext(ApplicationContext applicationContext){
    	SpringBeanTool.applicationContext = applicationContext;
    }

    public static <T> T getBean(String name){
        return (T)applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }
}
