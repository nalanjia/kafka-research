package com.aebiz.vo;

import lombok.Data;

@Data
public class ResearchMetricDTO {

	/**
	 * 度量名称
	 */
	private String name;
	
	/**
	 * 度量的描述
	 */
	private String desc;
	
	/**
	 * 度量的值
	 */
	private String value;
	
}
