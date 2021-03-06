package com.bdaim.util;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author chengning@salescomm.net
 * @date 2018/10/16
 * @description
 */
public class SqlAppendUtil {

    /**
     * sql in语句转换方法
     *
     * @param whereIns
     * @return
     */
    public static String sqlAppendWhereIn(Set<String> whereIns) {
        if (whereIns == null || (whereIns != null && whereIns.size() == 0)) {
            return "";
        }
        StringBuilder appendSql = new StringBuilder();
        for (String in : whereIns) {
            appendSql.append("'")
                    .append(in)
                    .append("',");
        }
        appendSql.deleteCharAt(appendSql.length() - 1);
        return appendSql.toString();
    }

//    public static String sqlAppendWhereIn(List<String> whereIns) {
//        if (whereIns == null || (whereIns != null && whereIns.size() == 0)) {
//            return "";
//        }
//        StringBuilder appendSql = new StringBuilder();
//        for (String in : whereIns) {
//            appendSql.append("'")
//                    .append(in)
//                    .append("',");
//        }
//        appendSql.deleteCharAt(appendSql.length() - 1);
//        return appendSql.toString();
//    }

    public static String sqlAppendWhereIn(Object[] whereIns) {
        if (whereIns == null || (whereIns != null && whereIns.length == 0)) {
            return "";
        }
        StringBuilder appendSql = new StringBuilder();
        for (Object in : whereIns) {
            appendSql.append("'")
                    .append(in)
                    .append("',");
        }
        appendSql.deleteCharAt(appendSql.length() - 1);
        return appendSql.toString();
    }

    public static String sqlAppendWhereIn(List params) {
        if (CollectionUtils.isEmpty(params)) {
            return "";
        }
        StringBuilder appendSql = new StringBuilder();
        for (Object in : params) {
            appendSql.append("'")
                    .append(in)
                    .append("',");
        }
        appendSql.deleteCharAt(appendSql.length() - 1);
        return appendSql.toString();
    }
}
