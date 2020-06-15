package com.aebiz.config.ip;

import java.io.IOException;

import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.aebiz.config.SpringBeanTool;

import lombok.extern.slf4j.Slf4j;


/**
 * 查询IP归属地。比如
 * 根据113.45.69.39，查询出归属信息
 * 215|中国|0|北京|北京市|鹏博士|28388
 * 
 * https://github.com/lionsoul2014/ip2region
 */
@Component
@Slf4j
public class IpConfig {

	private String filePath = "classpath:data/ip/ip2region.db";
	
	@Bean
	public DbSearcher initIp() throws Exception {
		//IP数据，是在一个ip2region.db的文件里
		PathMatchingResourcePatternResolver p = new PathMatchingResourcePatternResolver();
		Resource r = p.getResource(filePath);
		String dbPath = r.getFile().getAbsolutePath();
		
		DbConfig config = new DbConfig();
		DbSearcher searcher = new DbSearcher(config, dbPath);
		return searcher;
	}
	
	public static DataBlock getIpInfo(String ip) {
		DbSearcher searcher = SpringBeanTool.getBean(DbSearcher.class);
		try {
			DataBlock dataBlock = searcher.memorySearch(ip);
			return dataBlock;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
