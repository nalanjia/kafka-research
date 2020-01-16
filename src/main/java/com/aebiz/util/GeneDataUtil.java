package com.aebiz.util;

import java.util.concurrent.atomic.AtomicInteger;

public class GeneDataUtil {

	private static AtomicInteger atomicInt = new AtomicInteger(0);
	/**
	 * 生成一条消息
	 */
	public static String geneOneMessage() {
		String now = DateUtil.getNowTime_CN();
		int count = atomicInt.getAndIncrement();
		String retStr = now + " - " + count;
		return retStr;
	}
	
}
