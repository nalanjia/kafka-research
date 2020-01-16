package com.aebiz.util;

/**
 * 一些莫名的。。。杂的。。。
 */
public class OtherUtil {

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getNow() {
		String now = DateUtil.getNowTime_EN();
		return "[" + now + "]";
	}
	
}
