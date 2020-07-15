package com.aebiz.config.ip;

/**
 * 查询IP归属地
 */
public class IpConfig {

	public static String getIpInfo(String ip) {
		String region = IpQueryHelper.queryIP(ip);
		return region;
	}
	
//	public static void main(String[] args) {
//		String str = IpConfig.getIpInfo("113.45.108.166");
//		System.out.println(str);
//	}
}
