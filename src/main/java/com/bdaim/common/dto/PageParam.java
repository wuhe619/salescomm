package com.bdaim.common.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
public class PageParam {

    @NotNull(message = "pageNum参数必填")
    @Min(value = 0, message = "pageNum最小值为0")
    private Integer pageNum;
    @NotNull(message = "pageSize参数必填")
    @Min(value = 1, message = "pageSize最小值为1")
    private Integer pageSize;
    /**
     * 默认为10条
     */
    public static final int PAGE_SIZE = 10;
    /**
     * 排序字段
     */
    private String sort;
    /**
     * asc or desc
     */
    private String dir;

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

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PageParam{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", sort='" + sort + '\'' +
                ", dir='" + dir + '\'' +
                '}';
    }
}
