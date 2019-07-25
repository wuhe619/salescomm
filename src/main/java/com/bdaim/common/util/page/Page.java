package com.bdaim.common.util.page;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
public class Page {
    /**
     * total
     */
    private int total;
    /**
     * 结果集存放List
     */
    private List list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "Page{" +
                "total=" + total +
                ", list=" + list +
                '}';
    }
}
