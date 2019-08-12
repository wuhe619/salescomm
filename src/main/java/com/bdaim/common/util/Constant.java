package com.bdaim.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constant {
    /**
     * 标签状态
     */
    // 审核中
    public static final Integer AUDITING = 1;
    public static final String AUDITING_CN = "申请中";
    // 已上线
    public static final Integer ONLINE = 3;
    public static final String ONLINE_CN = "已上线";
    // 已下线
    public static final Integer OFFLINE = 4;
    public static final String OFFLINE_CN = "已下线";
    // 开发中
    public static final Integer DEVELOPING = 2;
    public static final String DEVELOPING_CN = "开发中";
    // 开发完成
    public static final Integer DEVELOP_FINISH = 5;
    public static final String DEVELOP_FINISH_CN = "开发完成";
    private static Map<Integer, String> STATUS = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(AUDITING, AUDITING_CN);
            put(DEVELOPING, DEVELOPING_CN);
            put(ONLINE, ONLINE_CN);
            put(OFFLINE, OFFLINE_CN);
            put(DEVELOP_FINISH, DEVELOP_FINISH_CN);
        }
    };
    public static final Map<Integer, String> STATUS_MAP = Collections
            .unmodifiableMap(STATUS);
    private static List<Integer> LIST_AUDIT_STATUS = new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;

        {
            add(AUDITING);
            add(DEVELOPING);
            add(ONLINE);
            add(OFFLINE);
            add(DEVELOP_FINISH);
        }
    };
    public static final List<Integer> AUDIT_STATUS = Collections
            .unmodifiableList(LIST_AUDIT_STATUS);
    /**
     * 审核类型
     */
    // 审核类型标签
    public static final Integer AUDIT_TYPE_LABEL = 1;
    public static final String AUDIT_TYPE_LABEL_CN = "标签";
    // 审核类型人群
    public static final Integer AUDIT_TYPE_GROUP = 2;
    public static final String AUDIT_TYPE_GROUP_CN = "人群";
    // 审核类型分类
    public static final Integer AUDIT_TYPE_CATEGORY = 3;
    public static final String AUDIT_TYPE_CATEGORYP_CN = "分类";

    // 导数申请审核
    public static final Integer AUDIT_TYPE_EXPORT = 4;
    public static final String AUDIT_TYPE_EXPORT_CN = "导数权限";
    // 导数申请审核
    public static final Integer AUDIT_TYPE_SIGNATURE = 5;
    public static final String AUDIT_TYPE_SIGNATURE_CN = "组合标签";

    private static Map<Integer, String> AUDIT_TYPE = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(AUDIT_TYPE_LABEL, AUDIT_TYPE_LABEL_CN);
            put(AUDIT_TYPE_GROUP, AUDIT_TYPE_GROUP_CN);
            put(AUDIT_TYPE_CATEGORY, AUDIT_TYPE_CATEGORYP_CN);
            put(AUDIT_TYPE_EXPORT, AUDIT_TYPE_EXPORT_CN);
            put(AUDIT_TYPE_SIGNATURE, AUDIT_TYPE_SIGNATURE_CN);
        }
    };
    public static final Map<Integer, String> AUDIT_TYPE_MAP = Collections
            .unmodifiableMap(AUDIT_TYPE);
    /**
     * 更新周期
     */
    // 日更新
    public static final Integer UPDATE_BY_DAY = 0;
    public static final String UPDATE_BY_DAY_CN = "日";
    // 周更新
    public static final Integer UPDATE_BY_WEEK = 1;
    public static final String UPDATE_BY_WEEK_CN = "周";
    // 月更新
    public static final Integer UPDATE_BY_MONTH = 2;
    public static final String UPDATE_BY_MONTH_CN = "月";
    // 年更新
    public static final Integer UPDATE_BY_YEAR = 3;
    public static final String UPDATE_BY_YEAR_CN = "年";
    private static Map<Integer, String> UPDATE_CYCLE = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(UPDATE_BY_DAY, UPDATE_BY_DAY_CN);
            put(UPDATE_BY_WEEK, UPDATE_BY_WEEK_CN);
            put(UPDATE_BY_MONTH, UPDATE_BY_MONTH_CN);
            put(UPDATE_BY_YEAR, UPDATE_BY_YEAR_CN);
        }
    };
    public static final Map<Integer, String> UPDATE_CYCLE_MAP = Collections
            .unmodifiableMap(UPDATE_CYCLE);
    /**
     * 数据格式
     */
    // 普通
    public static final Integer DATAFORMAT_COMMON = -1;
    public static final String DATAFORMAT_COMMON_CN = "普通";
    // 电商品类 E- business category
    public static final Integer DATAFORMAT_EB = 1;
    public static final String DATAFORMAT_EB_CN = "电商";
    // 媒体分类 media category
    public static final Integer DATAFORMAT_MC = 0;
    public static final String DATAFORMAT_MC_CN = "媒体";
    private static Map<Integer, String> DATA_FORMAT = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(DATAFORMAT_COMMON, DATAFORMAT_COMMON_CN);
            put(DATAFORMAT_EB, DATAFORMAT_EB_CN);
            put(DATAFORMAT_MC, DATAFORMAT_MC_CN);
        }
    };
    public static final Map<Integer, String> DATA_FORMAT_MAP = Collections
            .unmodifiableMap(DATA_FORMAT);
    /**
     * 标签来源
     */
    // 互联网数据
    public static final Integer LABEL_SOURCE_NET = 0;
    public static final String LABEL_SOURCE_NET_CN = "互联网数据";
    // CRM系统
    public static final Integer LABEL_SOURCE_CRM = 1;
    public static final String LABEL_SOURCE_CRM_CN = "CRM系统";
    private static Map<Integer, String> LABEL_SOURCE = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(LABEL_SOURCE_NET, LABEL_SOURCE_NET_CN);
            put(LABEL_SOURCE_CRM, LABEL_SOURCE_CRM_CN);
        }
    };
    public static final Map<Integer, String> LABEL_SOURCE_MAP = Collections
            .unmodifiableMap(LABEL_SOURCE);
    /**
     * 是否优先
     */
    // 优先
    public static final Integer IS_PRIOR_YES = 1;
    // 不优先
    public static final Integer IS_PRIOR_NO = 0;

    /**
     * 产出方法类型
     */
    // 统计
    public static final Integer METHOD_TYPE_COUNT = 0;
    public static final String METHOD_TYPE_COUNT_CN = "统计";
    // 模型
    public static final Integer METHOD_TYPE_MODEL = 1;
    public static final String METHOD_TYPE_MODEL_CN = "模型";
    // 算法
    public static final Integer METHOD_TYPE_SUANFA = 2;
    public static final String METHOD_TYPE_SUANFA_CN = "算法";
    private static Map<Integer, String> METHOD_TYPE = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(METHOD_TYPE_COUNT, METHOD_TYPE_COUNT_CN);
            put(METHOD_TYPE_MODEL, METHOD_TYPE_MODEL_CN);
            put(METHOD_TYPE_SUANFA, METHOD_TYPE_SUANFA_CN);
        }
    };
    public static final Map<Integer, String> METHOD_TYPE_MAP = Collections
            .unmodifiableMap(METHOD_TYPE);
    /**
     * 统计维度是否互斥
     */
    // 是互斥
    public static final Integer MUTEX_YES = 1;
    // 不互斥
    public static final Integer MUTEX_NO = 0;

    /**
     * 维度和是否为1
     */
    // 维度和为1
    public static final Integer DIMENSION_YES = 1;
    // 维度和不为1
    public static final Integer DIMENSION_NO = 0;

    /**
     * 标签是否有效
     */
    // 有效
    public static final Integer AVAILABLY = 1;
    // 无效
    public static final Integer UNAVAILABLY = 0;

    /**
     * 标签类型 1.分类2.基础标签 3.组合标签
     */
    // 标签分类
    public static final Integer LABLE_TYPE_CATEGORY = 1;
    public static final String LABLE_TYPE_CATEGORY_CN = "分类";
    // 基础标签类型
    public static final Integer LABLE_TYPE_BASE = 2;
    public static final String LABLE_TYPE_BASE_CN = "基础标签";
    // 组合标签
    public static final Integer LABLE_TYPE_SIGNATURE = 3;
    public static final String LABLE_TYPE_SIGNATURE_CN = "组合标签";

    private static Map<Integer, String> TYPE = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(LABLE_TYPE_CATEGORY, LABLE_TYPE_CATEGORY_CN);
            put(LABLE_TYPE_BASE, LABLE_TYPE_BASE_CN);
            put(LABLE_TYPE_SIGNATURE, LABLE_TYPE_SIGNATURE_CN);
        }
    };
    public static final Map<Integer, String> TYPE_MAP = Collections
            .unmodifiableMap(TYPE);
    // 基础标签下线申请
    public static final Integer APPLY_TYPE_BASELABEL_OFFLINE = 5;
    public static final String APPLY_TYPE_BASELABEL_OFFLINE_CN = "基础标签下线";
    // 组合标签下线申请
    public static final Integer APPLY_TYPE_SIGNATURE_OFFLINE = 6;
    public static final String APPLY_TYPE_SIGNATURE_OFFLINE_CN = "组合标签下线";
    // 基础标签创建
    public static final Integer APPLY_TYPE_BASELABEL_CREATE = 1;
    public static final String APPLY_TYPE_BASELABEL_CREATE_CN = "基础标签创建";
    // 组合标签创建
    public static final Integer APPLY_TYPE_SIGNATURE_CREATE = 2;
    public static final String APPLY_TYPE_SIGNATURE_CREATE_CN = "组合标签创建";
    // 人群创建
    public static final Integer APPLY_TYPE_CUSTOM_GROUP_CREATE = 3;
    public static final String APPLY_TYPE_CUSTOM_GROUP_CREATE_CN = "人群创建";
    // 人群创建
    public static final Integer APPLY_TYPE_CATEGORY_CREATE = 7;
    public static final String APPLY_TYPE_CATEGORY_CREATE_CN = "标签分类创建";
    // 人群创建
    public static final Integer APPLY_TYPE_CATEGORY_OFFLIN = 8;
    public static final String APPLY_TYPE_CATEGORY_OFFLINE_CN = "标签分类下线";

    // 导数权限申请
    public static final Integer APPLY_TYPE_EXPORT = 9;
    public static final String APPLY_TYPE_EXPORT_CN = "导数权限申请";

    private static Map<Integer, String> APPLY_TYPE = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(APPLY_TYPE_BASELABEL_OFFLINE, APPLY_TYPE_BASELABEL_OFFLINE_CN);
            put(APPLY_TYPE_SIGNATURE_OFFLINE, APPLY_TYPE_SIGNATURE_OFFLINE_CN);
            put(APPLY_TYPE_BASELABEL_CREATE, APPLY_TYPE_BASELABEL_CREATE_CN);
            put(APPLY_TYPE_SIGNATURE_CREATE, APPLY_TYPE_SIGNATURE_CREATE_CN);
            put(APPLY_TYPE_CUSTOM_GROUP_CREATE,
                    APPLY_TYPE_CUSTOM_GROUP_CREATE_CN);
            put(APPLY_TYPE_CATEGORY_CREATE, APPLY_TYPE_CATEGORY_CREATE_CN);
            put(APPLY_TYPE_CATEGORY_OFFLIN, APPLY_TYPE_CATEGORY_OFFLINE_CN);
            put(APPLY_TYPE_EXPORT, APPLY_TYPE_EXPORT_CN);
        }
    };
    public static final Map<Integer, String> APPLY_TYPE_MAP = Collections
            .unmodifiableMap(APPLY_TYPE);
    /**
     * 接口对接url
     */
    public static final String host = "http://hadoop02.test.bd-os.com:8080/jupiter";
    // 调度接口url 测试环境
    public static final String API_GET_TASKSTATUS_TEST = host
            + "/dataflow/DataflowMonitor!getLableTaskInfo.action?";

    //BDOS的任务查询接口url
    public static final String API_GET_TASKSTATUS_BM = PropertiesUtil.getStringValue("taskStatus_bm1");

    // 调度接口url 正式环境
    public static final String API_GET_TASKSTATUS_PRO = "http://192.168.11.154:8888/jupiter/dataflow/DataflowMonitor!getLableTaskInfo.action?";
    // bdms 接口地址
//	public static final String API_GET_TASKSTATUS_BDMS_TEST = "http://172.19.1.166:10086/ide/rest/taskstatus/?";

    public static final String API_GET_TASKSTATUS_BDMS_TEST = PropertiesUtil.getStringValue("BDMS_HOST") + ":"
            + PropertiesUtil.getStringValue("BDMS_PORT") + "/ide/rest/taskstatus/?";

    /**
     * 任务状态
     */
    // 正在执行
    public static final Integer TASK_RUNNING = 1;
    public static final String TASK_RUNNING_CN = "正在执行";
    // 任务完成
    public static final Integer TASK_FINISH = 2;
    public static final String TASK_FINISH_CN = "任务完成";
    // 任务暂停
    public static final Integer TASK_PAUSE = 4;
    public static final String TASK_PAUSE_CN = "任务暂停";
    // 任务停止
    public static final Integer TASK_STOP = 8;
    public static final String TASK_STOP_CN = "任务停止";
    // 任务警告
    public static final Integer TASK_WARN = 16;
    public static final String TASK_WARN_CN = "任务警告";
    // 综合状态
    public static final Integer TASK_MIX = 32;
    public static final String TASK_MIX_CN = "综合状态";
    // 任务异常
    public static final Integer TASK_ERROR = 64;
    public static final String TASK_ERROR_CN = "任务异常";
    public static final Integer TASK_WAITING = 0;
    public static final String TASK_WAITING_CN = "等待执行";

    private static Map<Integer, String> TASK = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(TASK_WAITING, TASK_WAITING_CN);
            put(TASK_RUNNING, TASK_RUNNING_CN);
            put(TASK_FINISH, TASK_FINISH_CN);
            put(TASK_PAUSE, TASK_PAUSE_CN);
            put(TASK_STOP, TASK_STOP_CN);
            put(TASK_WARN, TASK_WARN_CN);
            put(TASK_MIX, TASK_MIX_CN);
            put(TASK_ERROR, TASK_ERROR_CN);
        }
    };
    public static final Map<Integer, String> TASK_MAP = Collections
            .unmodifiableMap(TASK);
    // 审核中
    public static final Integer VIEW_STATUS_AUDITING = 2;
    // 下线
    public static final Integer VIEW_STATUS_OFFLINE = 0;
    // 上线
    public static final Integer VIEW_STATUS_ONLINE = 1;
    // 分隔符
    public static String SPLIT = "/";

    // 当前用户属性
    public static final String CURRENT_USER = "user";
    private static Map<Integer, Integer> VIEW_STATUS = new HashMap<Integer, Integer>() {
        private static final long serialVersionUID = 1L;

        {
            put(AUDITING, VIEW_STATUS_AUDITING);
            put(DEVELOPING, VIEW_STATUS_AUDITING);
            put(DEVELOP_FINISH, VIEW_STATUS_AUDITING);
            put(ONLINE, VIEW_STATUS_ONLINE);
            put(OFFLINE, VIEW_STATUS_OFFLINE);
        }
    };
    public static final Map<Integer, Integer> VIEW_STATUS_MAP = Collections
            .unmodifiableMap(VIEW_STATUS);
    // 最新审核状态 1
    public static final Integer AUDIT_LAST_FLAG_YES = 1;
    // 最新审核状态 0
    public static final Integer AUDIT_LAST_FLAG_NO = 0;

    // 审核申请状态
    private static final List<Integer> AUDIT = new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;

        {
            add(APPLY_TYPE_BASELABEL_CREATE);
            add(APPLY_TYPE_SIGNATURE_CREATE);
            add(APPLY_TYPE_CUSTOM_GROUP_CREATE);
            add(APPLY_TYPE_BASELABEL_OFFLINE);
            add(APPLY_TYPE_SIGNATURE_OFFLINE);
            add(APPLY_TYPE_CATEGORY_CREATE);
            add(APPLY_TYPE_CATEGORY_OFFLIN);
            add(APPLY_TYPE_EXPORT);

        }
    };
    public static final List<Integer> AUDIT_APPLYS = Collections
            .unmodifiableList(AUDIT);
    // 一级审核的请求类型
    private static final List<Integer> LEVLE1_AUDIT = new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;

        {
            add(APPLY_TYPE_SIGNATURE_CREATE);
            add(APPLY_TYPE_CUSTOM_GROUP_CREATE);
            add(APPLY_TYPE_CATEGORY_CREATE);
            add(APPLY_TYPE_CATEGORY_OFFLIN);
            add(APPLY_TYPE_EXPORT);
        }
    };
    public static final List<Integer> LEVLE1_AUDIT_APPLYS = Collections
            .unmodifiableList(LEVLE1_AUDIT);
    // 二级审核的请求类型
    private static final List<Integer> LEVLE2_AUDIT = new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;

        {
            add(APPLY_TYPE_BASELABEL_CREATE);
            add(APPLY_TYPE_BASELABEL_OFFLINE);
            add(APPLY_TYPE_SIGNATURE_OFFLINE);
        }
    };
    public static final List<Integer> LEVLE2_AUDIT_APPLYS = Collections
            .unmodifiableList(LEVLE2_AUDIT);
    // 创建请求类型
    private static final List<Integer> CREATE_AUDIT = new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;

        {
            add(APPLY_TYPE_BASELABEL_CREATE);
            add(APPLY_TYPE_CATEGORY_CREATE);
            add(APPLY_TYPE_SIGNATURE_CREATE);
            add(APPLY_TYPE_CUSTOM_GROUP_CREATE);
        }
    };
    public static final List<Integer> CREATE_AUDIT_APPLYS = Collections
            .unmodifiableList(CREATE_AUDIT);
    // 下线请求类型
    private static final List<Integer> OFFLINE_AUDIT = new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;

        {
            add(APPLY_TYPE_CATEGORY_OFFLIN);
            add(APPLY_TYPE_BASELABEL_OFFLINE);
            add(APPLY_TYPE_SIGNATURE_OFFLINE);
        }
    };
    public static final List<Integer> OFFLINE_AUDIT_APPLYS = Collections
            .unmodifiableList(OFFLINE_AUDIT);
    /**
     * 审批环节
     */
    public static final Integer NODE_APPLY = 1; // 申请环节
    public static final String NODE_APPLY_CN = "申请环节";
    public static final Integer NODE_AUDIT = 2;// 审批环节
    public static final String NODE_AUDIT_CN = "审批环节";
    public static final Integer NODE_DEVELOP = 3; // 开发环节
    public static final String NODE_DEVELOP_CN = "开发环节";
    public static final Integer NODE_PUBLISH = 4; // 发布环节
    public static final String NODE_PUBLISH_CN = "发布环节";
    /**
     * 审批环节映射表
     */
    private static final Map<Integer, String> NODE = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(NODE_APPLY, NODE_APPLY_CN);
            put(NODE_AUDIT, NODE_AUDIT_CN);
            put(NODE_DEVELOP, NODE_DEVELOP_CN);
            put(NODE_PUBLISH, NODE_PUBLISH_CN);
        }
    };
    public static final Map<Integer, String> NODE_MAP = Collections
            .unmodifiableMap(NODE);
    public static final String FILTER_KEY_PREFIX = "FILTER_KEY_";
    // 标签接口url
    public static final String LABEL_API = "http://"
            + PropertiesUtil.getStringValue("label.ip") + ":"
            + PropertiesUtil.getStringValue("label.port")
            + PropertiesUtil.getStringValue("label.project");
    public static String LABEL_API_ADDLABELVALUE_LOCK = "label_api_addlabelvalue_lock";
    // 审核结果
    public static final Integer AUDIT_APPROVE = 1; // 审核通过
    public static final Integer AUDIT_REFUSED = 0; // 审核拒绝

    public static final Integer DAYTYPE_DAY = 1;// 一天内
    public static final Integer DAYTYPE_WEEK = 7;// 一周内
    public static final Integer DAYTYPE_MONTH = 30;// 一月内

    public static final String USER_AUTH_KEY = "_USERAUTH";

    // 数据导出类型
    // GID
    public static final Integer DATA_EXPORT_GID_TYPE = 0;
    // CID
    public static final Integer DATA_EXPORT_CID_TYPE = 1;
    // 用户群
    public static final Integer DATA_EXPORT_GROUP_TYPE = 2;

    // 数据输出目的
    // 用户分析
    public static final Integer DATA_EXPORT_AIM0 = 0;
    public static final String DATA_EXPORT_AIM0STR = "用户分析";
    // 营销应用
    public static final Integer DATA_EXPORT_AIM1 = 1;
    public static final String DATA_EXPORT_AIM1STR = "营销应用";
    // 其它
    public static final Integer DATA_EXPORT_AIM2 = 2;
    public static final String DATA_EXPORT_AIM2STR = "其它";

    private static final Map<Integer, String> DATA_EXPORT = new HashMap<Integer, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(DATA_EXPORT_AIM0, DATA_EXPORT_AIM0STR);
            put(DATA_EXPORT_AIM1, DATA_EXPORT_AIM1STR);
            put(DATA_EXPORT_AIM2, DATA_EXPORT_AIM2STR);
        }

    };
    public static final Map<Integer, String> DATA_EXPORT_AIMS = Collections
            .unmodifiableMap(DATA_EXPORT);
    // 微观画像url
    public static final String MIC_PIC_URL = PropertiesUtil
            .getStringValue("mic_pic_url");
    // 查询类型为品类
    public static final Integer QUERY_TYPE_CATEGORY = 0;
    // 查询类型为品牌
    public static final Integer QUERY_TYPE_BRAND = 1;
    // 查询类型为属性
    public static final Integer QUERY_TYPE_ATTR = 2;

    public static final Integer LABEL_CHANNEL1 = 1;
    public static final String LABEL_CHANNEL_1str = "美菱生活";

    public static final Integer LABEL_CHANNEL2 = 2;
    public static final String LABEL_CHANNEL_2str = "论坛";

    public static final Integer LABEL_CHANNEL3 = 3;
    public static final String LABEL_CHANNEL_3str = "冰箱";

    public static final Integer LABEL_CHANNEL4 = 4;
    public static final String LABEL_CHANNEL_4str = "空调";

    public static final Integer LABEL_CHANNEL5 = 5;
    public static final String LABEL_CHANNEL_5str = "电视";

    public static final Integer LABEL_CHANNEL6 = 6;
    public static final String LABEL_CHANNEL_6str = "售后服务";

    public static final Integer LABEL_CHANNEL7 = 7;
    public static final String LABEL_CHANNEL_7str = "官网";

    private static final Map<Integer, String> CHANNELS = new HashMap<Integer, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(LABEL_CHANNEL1, LABEL_CHANNEL_1str);
            put(LABEL_CHANNEL2, LABEL_CHANNEL_2str);
            put(LABEL_CHANNEL3, LABEL_CHANNEL_3str);
            put(LABEL_CHANNEL4, LABEL_CHANNEL_4str);
            put(LABEL_CHANNEL5, LABEL_CHANNEL_5str);
            put(LABEL_CHANNEL6, LABEL_CHANNEL_6str);
            put(LABEL_CHANNEL7, LABEL_CHANNEL_7str);
        }

    };
    public static final Map<Integer, String> LABEL_CHANNELS = Collections
            .unmodifiableMap(CHANNELS);
    //下载申请
    public static final Integer DOWNLOAD_APPLYFAILED = -1;
    public static final Integer DOWNLOAD_NOTAPPLY = 0;
    public static final Integer DOWNLOAD_APPLY = 1;
    public static final Integer DOWNLOAD_FINISH = 2;
    public static final Integer DOWNLOAD_FAILED = 3;
    public static final Map<Integer, String> DOWNLOAD_STATUS = Collections
            .unmodifiableMap(new HashMap<Integer, String>() {
                private static final long serialVersionUID = 8158928519852582528L;

                {
                    this.put(-1, "申请失败");
                    this.put(0, "未申请");
                    this.put(1, "已申请");
                    this.put(2, "下载完成");
                    this.put(3, "任务下载失败");
                }
            });

    //推送数据的json的businessType映射到labelType
    public static final Map<String, String> BUSINESS_LABEL_MAP = new HashMap<String, String>() {{
        put("user", "1");
        put("shop", "2");
        put("bus", "3");
    }};
    //调用接口，labelType映射到businessType
    public static final Map<String, String> LABEL_BUSINESS__MAP = new HashMap<String, String>() {{
        put("1", "user");
        put("2", "shop");
        put("3", "bus");
    }};
    //Customer
    public static final String CUSTOMER_ID_MAP_KEY = "customerId";
    //User Status
    public static final int USER_ACTIVE_STATUS = 0;
    //enterprise status
    public static final int ENTERPRISE_ACTIVE_STATUS = 0;
    //account status
    public static final int ACCOUT_ACTIVE_STATUS = 0;

    /**
     * 管理员用户类型
     */
    public static final int ADMIN_USER_TYPE = 1;

    /**
     * 普通用户
     */
    public static final int STAFF_USER_TYPE = 0;

    /**
     * excel文件类型 2003
     */
    public static final String XLS = ".xls";
    /**
     * excel文件类型 2007
     */
    public static final String XLSX = ".xlsx";
    /**
     * pdf文件类型
     */
    public static final String PDF = ".pdf";
    /**
     * zip文件类型
     */
    public static final String ZIP = ".zip";
    /**
     * 校验中
     */
    public static final String CHECKING = "1";
    /**
     * 校验失败
     */
    public static final String CHECKING_FAILED = "2";
    /**
     * 待上传
     */
    public static final String TO_UPLOAD_FILE = "3";
    /**
     * 待发件
     */
    public static final String TO_SEND_EXPRESS = "4";
    /**
     * 待取件
     */
    public static final String TO_GET_EXPRESS = "5";
    /**
     * 已发件
     */
    public static final String ALREADY_SEND_EXPRESS = "6";

    /**
     * ROLE_USER
     */
    public static final String ROLE_USER = "ROLE_USER";
    /**
     * admin
     */
    public static final String ADMIN = "admin";
}
