package com.bdaim.common.helper;

public enum DB_TYPE{

    ORACLE("Oracle JDBC driver"),
    MYSQL5("MySQL-AB JDBC Driver"),
    //MYSQL_OLD("3"),
//    SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
//    DB2_NATIVE("COM.ibm.db2.jdbc.app.DB2Driver"),
//    DB2_REMOTE("COM.ibm.db2.jdbc.net.DB2Driver"),
//    POSTGRESQL("org.postgresql.Driver"),
    OTHER("");

    private String driverClass;
    DB_TYPE(String driverClass) {
        this.driverClass=driverClass;
    }

    public static DB_TYPE getDBType(String driverClassName){
        if (driverClassName.equalsIgnoreCase(ORACLE.driverClass))return ORACLE;
        else if (driverClassName.equalsIgnoreCase(MYSQL5.driverClass))return MYSQL5;
//        else if (driverClassName.equalsIgnoreCase(SQL_SERVER.driverClass))return SQL_SERVER;
//        else if (driverClassName.equalsIgnoreCase(DB2_NATIVE.driverClass))return DB2_NATIVE;
//        else if (driverClassName.equalsIgnoreCase(DB2_REMOTE.driverClass))return DB2_REMOTE;
//        else if (driverClassName.equalsIgnoreCase(POSTGRESQL.driverClass))return POSTGRESQL;
        else return OTHER;
    }
}
