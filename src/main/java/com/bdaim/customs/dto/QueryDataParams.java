package com.bdaim.customs.dto;

import com.bdaim.customs.entity.MainDan;

/**
 * @author duanliying
 * @date 2019/9/18
 * @description es查询参数类
 */
public class QueryDataParams extends MainDan {
    private Integer pageNum;
    private Integer pageSize;
    private String index;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "QueryDataParams{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", index='" + index + '\'' +
                '}';
    }
}
