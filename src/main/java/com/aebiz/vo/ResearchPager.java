package com.aebiz.vo;

import java.util.ArrayList;
import java.util.List;

public class ResearchPager {
	private int pageShow = 10;
	private int nowPage = 1;
	/**
	 * 查询条件下的消息数
	 */
	private int totalNum = 0;
	private int totalPage = 0;
	private List<ResearchPagerEleDTO> results = new ArrayList<>();

	/**
	 * 消息总数
	 */
	private int allMsgNum = 0;
	
	private long costMs;
	
	public static ResearchPager getPager(int nowPage, int pageShow) {
		ResearchPager pager = new ResearchPager();
		pager.setNowPage(nowPage);
		pager.setPageShow(pageShow);
		return pager;
	}

	public int getFromNum() {
		return (this.getNowPage() - 1) * this.pageShow;
	}

	public List getResults() {
		return this.results;
	}

	public void setResults(List results) {
		this.results = results;
	}

	public int getPageShow() {
		if (this.pageShow <= 0) {
			this.pageShow = 1;
		}
		return this.pageShow;
	}

	public void setPageShow(int pageShow) {
		this.pageShow = pageShow;
	}

	public int getNowPage() {
		if (this.nowPage <= 0) {
			this.nowPage = 1;
		}
		return this.nowPage;
	}

	public void setNowPage(int nowPage) {
		this.nowPage = nowPage;
	}

	public int getTotalNum() {
		return this.totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public int getTotalPage() {
		return this.totalNum > 0 ? (int) Math.ceil((double) this.totalNum * 1.0D / (double) this.pageShow)
				: this.totalPage;
	}

	public int getAllMsgNum() {
		return allMsgNum;
	}

	public void setAllMsgNum(int allMsgNum) {
		this.allMsgNum = allMsgNum;
	}

	public long getCostMs() {
		return costMs;
	}

	public void setCostMs(long costMs) {
		this.costMs = costMs;
	}
	
}
