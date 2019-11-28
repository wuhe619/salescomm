package com.bdaim.online.zhianxin.dto;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/17 11:33
 */
public class PageData {

    private int total;
    private int pageNo;
    private int pageSize;
    private List list;

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "PageData{" +
                "total=" + total +
                ", pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", list=" + list +
                '}';
    }
}