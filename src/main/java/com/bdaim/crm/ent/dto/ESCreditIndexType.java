package com.bdaim.crm.ent.dto;

import com.bdaim.util.NumberConvertUtil;

/**
 * @author chengning@salescomm.net
 * @description
 * @date 2020/6/5 10:56
 */
public enum ESCreditIndexType {

    ABNORMAL(1, "abnormal", "tag", "", "mid"),
    CHANGE_RECORD(2, "change_record", "tag", "", "mid"),
    COPYRIGHT(3, "copyright", "tag", "", "mid"),
    DEVELOP_PROJECT_LIST(4, "develop_projectlist", "tag", "", "mid"),
    EQUITYPLEDGE(5, "equitypledge", "tag", "", "mid"),
    INVEST(6, "invest", "tag", "", "mid"),
    PENALTIES(7, "penalties", "tag", "", "mid"),
    STOCK_FREEZE(8, "stockfreeze", "tag", "", "mid"),
    ANNUAL(9, "annual", "tag", "", "mid"),
    CHATTEL_MORTGAGE(10, "chattelmortgage", "tag", "", "mid"),
    DEVELOP_FINANCING(11, "develop_financing", "tag", "", "mid"),
    DEVELOP_SIMILARS(12, "develop_similars", "tag", "", "mid"),
    HOLD(13, "hold", "tag", "", "mid"),
    LAW_WENSHU(14, "lawwenshu", "tag", "", "mid"),
    RETRIEVE(15, "retrieve", "tag", "", "mid"),
    TAX_VIOLATION(16, "taxviolation", "tag", "", "mid"),
    BASIC(17, "basic", "tag", "", "mid"),
    CLEAR_ACCOUNT(18, "clearaccount", "tag", "", "mid"),
    DEVELOP_INVESTOR_LIST(19, "develop_investorlist", "tag", "", "mid"),
    DIRECTORS(20, "directors", "tag", "", "mid"),
    ILLEGAL(21, "illegal", "tag", "", "mid"),
    MARK(22, "mark", "tag", "", "mid"),
    SHAREHOLDER(23, "shareholder", "tag", "", "mid"),
    WEB_RECORD(24, "web_record", "tag", "", "mid"),
    BRANCH(25, "branch", "tag", "", "mid"),
    CONDITION_LICENSE(26, "condition_license", "tag", "", "mid"),
    DEVELOP_MEMBER(27, "develop_member", "tag", "", "mid"),
    DISCREDIT(28, "discredit", "tag", "失信被执行人", "mid"),
    INTELLECTUAL(29, "intellectual", "tag", "", "mid"),
    PATENT(30, "patent", "tag", "", "mid"),
    SIMPLE_CANCELLATION(31, "simplecancellation", "tag", "", "mid"),
    ATAXPAYER(32, "ataxpayer", "tag", "", "mid"),
    TRAFFIC_TRUST(33, "traffic_trust", "tag", "交通运输工程建设守信", "mid"),
    CUSTOMS_CERTIFICATE(34, "customs_certificate", "tag", "海关高级认证", "mid"),
    LABOURER(35, "labourer", "tag", "", "mid"),
    ELECDISHONES_RELATE(36, "elec_dishones_relate", "tag", "涉电领域失信关联企业", "mid"),
    ELEC_DISHONES_SUPERVISE(37, "elec_dishones_supervise", "tag", "涉电领域失信监管企业", "mid"),
    SAFE_DISHONEST(38, "safe_dishonest", "tag", "安全生产领域失信", "mid"),
    STATIS_DISHONEST(39, "statis_dishonest", "tag", "统计领域严重失信", "mid"),
    CUSTOMS_DISHONEST(40, "customs_dishonest", "tag", "海关失信", "mid"),
    ILLEGAL_LAW(41, "illegal_law", "tag", "重大税收违法", "mid"),
    WAGEARREAS(42, "wagearreas", "tag", "拖欠农民工工资", "mid");

    ESCreditIndexType(int type, String indexName, String typeName, String remark, String uuid) {
        this.type = type;
        this.indexName = indexName;
        this.typeName = typeName;
        this.remark = remark;
        this.uuid = uuid;
    }

    private int type;
    private String indexName;
    private String typeName;
    private String remark;
    private String uuid;

    public int getType() {
        return type;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getRemark() {
        return remark;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTypeName() {
        return typeName;
    }

    public static ESCreditIndexType get(String type) {
        for (ESCreditIndexType v : ESCreditIndexType.values()) {
            if (v.getType() == NumberConvertUtil.parseInt(type)) {
                return v;
            }
        }
        return null;
    }
}
