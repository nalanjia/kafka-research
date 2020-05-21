package com.aebiz.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GeneDataUtil {

	private static AtomicInteger atomicInt = new AtomicInteger(1);
	/**
	 * 生成一条消息
	 */
	public static String geneOneMessage() {
		String now = DateUtil.getNowTime_EN();
		int count = atomicInt.getAndIncrement();
		String retStr = now + " - " + count;
		return retStr;
	}
	
	/**
	 * 生成随机ID
	 */
	public static String geneUuid() {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		return uuid;
	}
	
}
