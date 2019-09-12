package com.bdaim.common.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 这里主要是完成JDBC的辅助类
 */
public class JDBCHelper {

    public void execute(Connection con,String sql,Object... args) throws SQLException {
        PreparedStatement pst=con.prepareStatement(sql);
        if (args!=null&&args.length>0){
            int index=0;
            for (Object obj:args){
                pst.setObject(index++,obj);
            }
        }
        pst.execute();
    }

    public static ResultSet query(Connection con,String sql,Object... args) throws SQLException {
        PreparedStatement pst=con.prepareStatement(sql);
        if (args!=null&&args.length>0){
            int index=0;
            for (Object obj:args){
                pst.setObject(++index,obj);
            }
        }
        return pst.executeQuery();
    }
    public static List<Map<String,Object>> queryMap(Connection con,String sql,Object... args) throws SQLException {
        ResultSet rs=JDBCHelper.query(con,sql, args);
        List<Map<String,Object>> rsList=new ArrayList<Map<String, Object>>();

        Map<String,Object> entity=null;
        rs.getMetaData().getColumnCount();
        while (rs.next()){
            entity=new HashMap<String, Object>();
            for (int i=0;i<rs.getMetaData().getColumnCount();i++){
                entity.put(rs.getMetaData().getColumnName(i),rs.getObject(i));
            }
            rsList.add(entity);
        }
        return rsList;
    }

    public static void rollBack(Connection con){
        try {
            if (con!=null&&!con.isClosed())con.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Connection con){
        try {
            if (con!=null&&!con.isClosed())con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void closeStmt(PreparedStatement stmt){
        try {
            if (stmt!=null&&!stmt.isClosed())stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void closeRs(ResultSet rs){
        try {
            if (rs!=null&&!rs.isClosed())rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
