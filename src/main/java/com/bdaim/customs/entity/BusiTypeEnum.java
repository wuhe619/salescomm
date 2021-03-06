package com.bdaim.customs.entity;


import java.util.ArrayList;
import java.util.List;

public enum BusiTypeEnum {
    SZ("SZ", "申报单-主单", "sbd_z"),
    SF("SF", "申报单-分单", "sbd_f"),
    SS("SS", "申报单-税单", "sbd_s"),
    CZ("CZ", "舱单-主单", "cd_z"),
    CF("CF", "舱单-分单", "cd_f"),
    CS("CS", "舱单-税单", "cd_s"),
    BZ("BZ", "报关单-主单", "bgd_z"),
    BF("BF", "报关单-分单", "bgd_f"),
    BS("BS", "报关单-税单", "bgd_s"),
    ST("ST", "场站", "station"),
    BGD_HZ("BGD_HZ", "报关单-海关回执", "bgd_hz"),
    CD_HZ("CD_HZ", "舱单-海关回执", "cd_hz"),
    CDF_HZ("CDF_HZ", "舱单分单-海关回执", "cdf_hz"),
    HG_HZ("HG_HZ", "海关回执", "hg_hz"),

    TAX_MANAGE("TAX_MANAGE", "纳税汇总", "tax_manage"),
    TAX_DETAIL("TAX_DETAIL", "纳税详情", "tax_detail"),
    PARAM_PROXY("PARAM_PROXY", "地面代理参数", "param_proxy"),
    /**
     * api
     */
    HY_PIC_Z("HY_PIC_Z", "行业画像-主批次", "hy_pic_z"),
    HY_PIC_X("HY_PIC_X", "行业画像-批次详情", "hy_pic_x"),
    BATCH_TEST_TASK("BATCH_TEST_TASK", "批量测试任务", "b_test_task"),
    BATCH_TEST_TASK_Z("BATCH_TEST_TASK_Z", "批量测试任务-主批次信息", "b_test_task_z"),
    BATCH_TEST_TASK_X("BATCH_TEST_TASK_X", "批量测试任务-批次详情", "b_test_task_x"),
    PATROL_TASK("PATROL_TASK","巡检任务","patrol_task"),
    PATROL_TASK_APIS("PATROL_TASK_APIS","巡检任务API","patrol_task_apis"),
    PATROL_TASK_CALLAPI_HISTORY("PATROL_TASK_CALLAPI_HISTORY","巡检任务调用api历史","patrol_task_callapi_history"),

    //HY_PIC_X("HY_PIC_X", "行业画像-批次详情", "hy_pic_x"),

    /**
     * 企业套餐
     */
    B2B_TC("B2B_TCB", "B2B企业套餐", "b2b_tcb"),
    /**
     * 企业套餐领取记录
     */
    B2B_TC_LOG("B2B_TC_LOG", "B2B企业套餐", "b2b_tcb_log"),

    EXPRESS_ORDER("EXPRESS_ORDER","快递订单","express_order"),

    EXPRESS_TRAJECTORY("EXPRESS_TRAJECTORY","快递轨迹","express_trajectory"),
    /**
     * 企业数据检索条件
     */
    ENT_SEARCH_CONDITION("ENT_SEARCH_CONDITION", "企业数据检索条件", "ent_search_condition"),

    ENT_MSG_REMIND("ENT_MSG_REMIND", "消息提醒", "ent_msg_remind"),

    ENT_EXPORT_TASK("ENT_EXPORT_TASK", "企业数据导出任务", "ent_export_task");

    private String key;
    private String name;
    private String type;

    BusiTypeEnum(String key, String name, String type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }


    public static BusiTypeEnum get(String busiType) {
        for (BusiTypeEnum v : BusiTypeEnum.values()) {
            if (v.getType().equals(busiType)) {
                return v;
            }
        }
        return null;
    }

    public static String getEsIndex(String busiType) {
        BusiTypeEnum busiTypeEnum = get(busiType);
        switch (busiTypeEnum) {
            case SZ:
                return Constants.SZ_INFO_INDEX;
            case CZ:
                return Constants.CZ_INFO_INDEX;
            case BZ:
                return Constants.BZ_INFO_INDEX;
            case SF:
                return Constants.SF_INFO_INDEX;
            case CF:
                return Constants.CF_INFO_INDEX;
            case BF:
                return Constants.BF_INFO_INDEX;
            case SS:
                return Constants.SS_INFO_INDEX;
            case CS:
                return Constants.CS_INFO_INDEX;
            case BS:
                return Constants.BS_INFO_INDEX;
            default:
                return "-1";
        }
    }

    /**
     * 获取type
     *
     * @param key
     * @return
     */
    public static String getType(String key) {
        for (BusiTypeEnum v : BusiTypeEnum.values()) {
            if (v.getKey().equals(key)) {
                return v.getType();
            }
        }
        return "";
    }

    public static String getParentType(String busiType) {
        String parentType = "";
        if (busiType.endsWith("_f")) {
            parentType = busiType.replaceAll("_f", "_z");
        } else if (busiType.endsWith("_s")) {
            parentType = busiType.replaceAll("_s", "_f");
        }
        return parentType;
    }

    public String getKey() {
        return key;
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static List<String> getTypeList() {
        List types = new ArrayList();
        for (BusiTypeEnum v : BusiTypeEnum.values()) {
            types.add(v.getType());
        }
        return types;
    }

}
