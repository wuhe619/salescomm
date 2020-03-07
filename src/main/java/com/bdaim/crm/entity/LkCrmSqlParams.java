package com.bdaim.crm.entity;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmSqlParams {
    private String sql;
    private List<Object> params;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
