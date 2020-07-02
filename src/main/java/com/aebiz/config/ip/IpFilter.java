package com.aebiz.config.ip;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aebiz.controller.ProducerController;
import com.aebiz.util.KaResearchConstant;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IpFilter implements Filter {

	@Autowired
	private ProducerController producerController;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		String uri = req.getRequestURI();
		
		//访问了首页
		if("/".equals(uri)) {
			String ip = getIp(req);
			String info = IpConfig.getIpInfo(ip);
			
			//向IP主题插入1条消息
			String topicName = KaResearchConstant.TOPIC_IP;
			String keyName = null;
			String msg = ip + " " + info;
			producerController.addOneMsg(topicName, keyName, msg);
			log.debug("[" + ip + "]访问了首页，该IP详情为" + msg);
		}
		
		chain.doFilter(req, res);
		
	}
	
	private String getIp(HttpServletRequest req) {
		String ip = req.getRemoteAddr();
		return ip;
	}

}
