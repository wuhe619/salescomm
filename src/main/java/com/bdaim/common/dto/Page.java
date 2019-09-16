package com.bdaim.common.dto;

import java.util.List;

public class Page {
	private int start = 0;
	private int limit = 10;
	private int total = 0;
	private List data = null;
	private int perPageCount=0;
	private int pageIndex=0;
	private int countPerPage=0;
	private int count = 0;
    
	public Page() {
		
	}
	public Page(int pageIndex, int countPerPage) {
		this.pageIndex = pageIndex;
		this.countPerPage = countPerPage;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List getData() {
		return data;
	}
	public void setData(List data) {
		this.data = data;
	}
	public int getPerPageCount() {
		return perPageCount;
	}
	public void setPerPageCount(int perPageCount) {
		this.perPageCount = perPageCount;
	}
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public int getCountPerPage() {
		return countPerPage;
	}
	public void setCountPerPage(int countPerPage) {
		this.countPerPage = countPerPage;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "Page{" +
				"start=" + start +
				", limit=" + limit +
				", total=" + total +
				", data=" + data +
				", perPageCount=" + perPageCount +
				", pageIndex=" + pageIndex +
				", countPerPage=" + countPerPage +
				", count=" + count +
				'}';
	}
}
