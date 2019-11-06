package com.bdaim.customer.dto;

import java.util.List;

public class ContentPage {
    private long count;
    private List<ContentDTO> countList;
    private int pageNum;
    private int pageSize;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<ContentDTO> getCountList() {
        return countList;
    }

    public void setCountList(List<ContentDTO> countList) {
        this.countList = countList;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
