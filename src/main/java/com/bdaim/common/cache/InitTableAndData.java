package com.bdaim.common.cache;

import com.bdaim.common.helper.DB_TYPE;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 */
public class InitTableAndData {

    public void init() throws SQLException,NullPointerException {
        DataSource ds = ((DataSourceManager) BeanCache.getBean(ConfigReader.DATA_SOURCE)).getDataSource();
        Connection con= ds.getConnection();
        String driverName=con.getMetaData().getDriverName();
        DB_TYPE type = DB_TYPE.getDBType(con.getMetaData().getDriverName());
        switch (type) {
            case ORACLE:exeSQL(con,"oracle");break;
            case MYSQL5:exeSQL(con,"mysql");break;
            default:throw new NullPointerException("当前连接的数据库的没有匹配的脚本");
        }
        con.close();
    }

    public void exeSQL(Connection con, String key) throws SQLException {
        Statement st= con.createStatement();
        for (String sql:ConfigReader.getConf(key+".tables").toString().split(";")){
            try {
                st.execute(sql);
            }catch (Exception e){

            }

//            st.addBatch(sql);
        }
        if (ConfigReader.getConf("mrp.data")!=null){
            for (String sql:ConfigReader.getConf("mrp.data").toString().split(":")){
                st.addBatch(sql);
            }
        }

        st.executeBatch();
    }
}
