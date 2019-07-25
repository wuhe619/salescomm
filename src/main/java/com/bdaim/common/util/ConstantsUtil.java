package com.bdaim.common.util;

import java.util.HashMap;
import java.util.Map;

public class ConstantsUtil {
    //-----------mysql business info-----------
    public final static String MYSQL_LABEL_CONFIG_TABLE_NAME = "label_config";
    public final static String MYSQL_LABEL_CATEGORY_TABLE_NAME = "label_category";
    public final static String MYSQL_ADD_COMBINE_LABELS_TABLE_NAME = "combine_labels_to_add";


    //-----------es business info--------------
    public final static int ES_AGGS_TOP = 10;

    //-----------hbase--------------------
    public final static String HBASE_ZOOKEEPER_QUORUM = ConfigUtil.getInstance().get("hbase.zookeeper.quorum");
    public final static String ZOOKEEPER_ZNODE_PARENT = ConfigUtil.getInstance().get("zookeeper.znode.parent");
    public final static String HBASE_CHARSET = ConfigUtil.getInstance().get("hbase.charset", "UTF-8");

    //-----------hbase business info
    //根据superId查询用户信息的表名
    public final static String HBASE_USER_INFO_TABLE_NAME = "relation_graph";
    //用户信息表的列族
    public final static String HBASE_USER_INFO_TABLE_COLUMN_FAMILY = "ids";
    //rowKey的前缀
    public final static String HBASE_USER_INFO_TABLE_ROWKEY_PREFIX = "global";
    //输出的前缀(返回前会进行替换,将HBASE_USER_INFO_TABLE_ROWKEY_PREFIX替换为指定输出前缀)
    public final static String HBASE_USER_INFO_TABLE_OUT_ROWKEY_PREFIX = "global";

    //V3表
    public final static String HBASE_V3_TABLE_NAME = "OfflineUserProfileV3";
    public final static String HBASE_V3_7_TABLE_NAME = "OfflineUserProfileV3_7";
    public final static String HBASE_V3_30_TABLE_NAME = "OfflineUserProfileV3_30";

    //only business info
    //BQ0004
    public final static String BQ0004_DEFAULT_DOWNLOAD_PATH = "/tmp/SuperID-Download";

    //stream方式
    public final static String BQ0004_DOWNLOAD_STEAM_FILE_NAME = "SuperID-Download";

    //csv方式
    public final static String BQ0004_DOWNLOAD_CSV_FILE_NAME = "SuperID-Download";

    public final static String[] BQ0004_DOWNLOAD_CSV_TITLES = {"TEL", "EHR", "UID", "CITIZENID", "EMAIL", "BARCODE", "QQ", "WEIBO", "GID"};
    public final static String[] BQ0004_DOWNLOAD_CSV_HBASE_COLS = {"cp", "ehr", "uid", "citizenid", "em", "barcode", "qq", "weibo", "gid"};


    //xls方式
    public final static String BQ0004_DOWNLOAD_XLS_FILE_NAME = "SuperID-Download";

    public final static String[] BQ0004_DOWNLOAD_XLS_TITLES = {"TEL", "EHR", "UID", "CITIZENID", "EMAIL", "BARCODE", "QQ", "WEIBO", "GID"};
    public final static String[] BQ0004_DOWNLOAD_XLS_HBASE_COLS = {"cp", "ehr", "uid", "citizenid", "em", "barcode", "qq", "weibo", "gid"};


    //BQ0006
    public final static Map BQ0006_SEARCH_CONDITION_COLS_MAPPER = new HashMap() {
        {
            put("tel", "cp");
            put("email", "em");
        }
    };

    public final static String[] BQ0006_RETURN_COLS = new String[]{"gid", "cp", "qq", "em"};


    //------------------------------推送服务---------------------------------------

    //-------------------推送接口------------------------
    //-----btw-----
    public final static String BTW_SYSTEM_NAME = "BTW";
    public final static String BTW_REST_CUSTOMERNUM = "/api/label/labelCustomerNum";
    public final static String BTW_REST_COVERUSER = "/api/label/coverUser";
    public final static String BTW_REST_AVGLABEL = "/api/user/avgLabel";

    public final static String COMPLEXTAG = "complexTag";
    public final static String COMPLEXTAGSECOND = "complex";
    public final static String SECOND_LEVEL_TYPE = "second_level_type";
    public final static String SECOND_LEVEL_TYPES = "second_level_type,first_cate,second_cate,third_cate,forth_cate,fifth_cate";


    /*-------------------------默认用户的设置------------------------*/
    public final static String DEFAULT_LOGIN_USER = "bfd.tag.defaultUserId";
    /*-------------------------供应商------------------------*/
    public final static String SUPPLIERID__XZ = "1";
    public final static String SUPPLIERID__CUC = "2";
    public final static String SUPPLIERID__CTC = "3";
    public final static String SUPPLIERID__CMC = "4";
    public final static String SUPPLIERID__JD = "5";
    public final static String SUPPLIERID__YD = "6";

    /*-------------------------资源类型------------------------*/
    public final static int CALL_TYPE = 4;
    public final static String CALL_PRICE_KEY = "callPrice";
    public final static int SMS_TYPE = 3;
    public final static String SMS_PRICE_KEY = "smsPrice";
    public final static int SEATS_TYPE = 5;
    public final static String SEATS_PRICE_KEY = "seatPrice";
    //身份证修复
    public final static int IDCARD_FIX_TYPE = 6;
    public final static String IDCARD_FIX_PRICE_KEY = "fixPrice";
    public final static int IMEI_FIX_TYPE = 11;
    public final static String IMEI_PRICE_KEY = "imeiFixPrice";
    public final static int MAC_FIX_TYPE = 10;
    public final static String MAC_PRICE_KEY = "macFixPrice";

    public final static int JD_FIX_TYPE = 12;
    public final static String JD_FIX_PRICE_KEY = "jdFixPrice";
    public final static int EXPRESS_TYPE = 2;
    public final static String EXPRESS_PRICE_KEY = "expressPrice";
    //扣减
    public final static int DEDUCT_TYPE = 7;
    //调账
    public final static int UPDATE_BALANCE_TYPE = 9;
}
