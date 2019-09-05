package com.bdaim.common.service;

import java.sql.Connection;
import java.sql.SQLException;

/**
 */
public interface DaoService<T> {
    public void insert(Connection con, T t) throws SQLException;

    public void delete(Connection con, T t) throws SQLException;

    public void update(Connection con, T t) throws SQLException;

    /**
     * 查询一个对象，当CON为空时，会默认生成一个CON
     * @param con 连接对象
     * @param t 查询的实体
     * @return
     */
    public T getObj(Connection con, T t);


}
