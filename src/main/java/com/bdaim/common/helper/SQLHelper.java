package com.bdaim.common.helper;

import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

/**
 * 这个类主要是帮助处理一些有关数据库SQL问题的类。由于测试的问题，所以当前只提供Oracle和Mysql数据库信息
 */
public class SQLHelper {
    /**
     * 将SQL根据O不同数据库的的方言进行分页
     *
     * @param con
     * @param sql
     * @return
     */
    public static String getPageSQL(final Connection con, final String sql) {
        String rsSQL = "";
        try {
            DB_TYPE type = DB_TYPE.getDBType(con.getMetaData().getDriverName());
            con.prepareStatement("").getParameterMetaData().getParameterCount();
            if (type == DB_TYPE.ORACLE) {
                String rowNumName = "_rowNum";
                while (sql.indexOf(rowNumName) > 0) rowNumName = "_" + rowNumName;
                rsSQL = "select * from (" + sql.replaceFirst("select", "select rownum as " + rowNumName + ",") + ") as _temp where " + rowNumName + ">=? and " + rowNumName + "<?";
            } else if (type == DB_TYPE.MYSQL5) {
                rsSQL = "select * from (" + sql + ") as _temp limit ?,?";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsSQL;
    }

    public static String getInSQL(String[] args) {
        if (args == null || args.length == 0) return "";
        else {
            StringBuffer sb = new StringBuffer();
            for (String item : args) {
                sb.append("'").append(item).append("',");
            }
            return sb.substring(0, sb.length() - 1).toString();
        }
    }

    public static String getInSQL(Set<Long> params) {
        if (CollectionUtils.isEmpty(params)) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer();
            for (Object item : params) {
                sb.append("'").append(item).append("',");
            }
            return sb.substring(0, sb.length() - 1).toString();
        }
    }
//    public static ResultSet execPageQuery(PreparedStatement st,int begin,int end){
//        try {
//            st.getConnection().getMetaData().getDriverName();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

}
