package com.bdaim.common.util.page;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.bdaim.common.dto.PageParam;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
public class Pagination extends JdbcDaoSupport {
    private static final Logger LOGGER = Logger.getLogger(Pagination.class);
    /**
     * 一页显示的记录数
     */
    private int pageSize;
    /**
     * 记录总数
     */
    private int totalRows;
    /**
     * 总页数
     */
    private int totalPages;
    /**
     * 当前页码
     */
    private int currentPage;
    /**
     * 起始行数
     */
    private int startIndex;
    /**
     * 结束行数
     */
    private int lastIndex;
    /**
     * 结果集存放List
     */
    private List resultList;

    /**
     * 分页构造函数
     *
     * @param sql       根据传入的sql语句得到一些基本分页信息
     * @param params    参数列表
     * @param page      分页参数对象
     * @param jTemplate JdbcTemplate实例
     */
    public Page getPageData(String sql, Object[] params, PageParam page, JdbcTemplate jTemplate) {
        if (jTemplate == null) {
            logger.info("JdbcTemplate is null,please initial it first. ");
            throw new IllegalArgumentException("JdbcTemplate is null,please initial it first. ");
        } else if (sql == null || sql.equals("")) {
            throw new IllegalArgumentException("sql is empty,please initial it first. ");
        }
        //设置每页显示记录数
        setPageSize(page.getPageSize());
        //设置要显示的页数
        setCurrentPage(page.getPageNum());
        //计算总记录数
        StringBuffer totalSQL = new StringBuffer(" SELECT count(*) FROM ( ");
        totalSQL.append(sql);
        totalSQL.append(" ) totalTable ");
        //给JdbcTemplate赋值
        setJdbcTemplate(jTemplate);
        //总记录数
        setTotalRows(getJdbcTemplate().queryForObject(totalSQL.toString(), params, Integer.class));
        //计算总页数
        setTotalPages();


        //计算起始行数
        setStartIndex();
        //计算结束行数
        setLastIndex();
        //装入结果集
        setResultList(getJdbcTemplate().queryForList(getMySQLPageSQL(new StringBuilder(sql), page), params));
        Page data = new Page();
        data.setTotal(this.totalRows);
        data.setList(this.resultList);
        return data;
    }


    /**
     * 构造MySQL数据分页SQL
     *
     * @param queryString
     * @return
     */
    public String getMySQLPageSQL(StringBuilder queryString, PageParam page) {
        String resultSql = null;
        if (page != null && page.getSort() != null && page.getDir() != null) {
            queryString.append(" order by ").append(page.getSort()).append(" ").append(page.getDir());
        }
        if (0 != pageSize) {
            resultSql = queryString.append(" limit ").append(startIndex).append(",").append(pageSize).toString();
        } else {
            resultSql = queryString.toString();
        }
        LOGGER.info("page sql:" + resultSql);
        return resultSql;
    }


    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        //如果当前页码<1，则默认加载第一页数据
        if (currentPage < 0) {
            this.currentPage = 1;
        } else {
            this.currentPage = currentPage;
        }
    }


    public List getResultList() {
        return resultList;
    }

    public void setResultList(List resultList) {
        this.resultList = resultList;
    }

    public int getTotalPages() {
        return totalPages;
    }

    /**
     * 计算总页数
     */
    public void setTotalPages() {
        if (pageSize == 0) {
            totalPages = 0;
        } else {
            if (totalRows % pageSize == 0) {
                this.totalPages = totalRows / pageSize;
            } else {
                this.totalPages = (totalRows / pageSize) + 1;
            }
        }
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex() {
        if (currentPage <= 1) {
            this.startIndex = 0;
        } else {
            //如果总页数>当前页,则默认加载最后一页
            if (currentPage > totalPages) {
                currentPage = totalPages;
            }
            this.startIndex = (currentPage - 1) * pageSize;
        }

    }

    public int getLastIndex() {
        return lastIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 计算结束时候的索引
     */
    public void setLastIndex() {
        LOGGER.debug("totalRows=" + totalRows);
        LOGGER.debug("pageSize=" + pageSize);
        if (pageSize != 0) {
            if (totalRows < pageSize) {
                this.lastIndex = totalRows;
            } else if ((totalRows % pageSize == 0) || (totalRows % pageSize != 0 && currentPage < totalPages)) {
                this.lastIndex = currentPage * pageSize;
                //最后一页
            } else if (totalRows % pageSize != 0 && currentPage == totalPages) {
                this.lastIndex = totalRows;
            }
        }
    }
}
