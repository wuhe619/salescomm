package com.bdaim.common.dto;


import com.bdaim.common.util.PropertiesUtil;

/**
 * 字典类型
 *
 * @author chengning@salescomm.net
 * @date 2019/6/5
 * @description
 */
public enum DicTypeEnum {
    A("A", "品牌", ""),
    B("B", "贷款", PropertiesUtil.getStringValue("finance.h5.host") + "/#/loanDetail?id="),
    C("C", "活动", ""),
    E("E", "广告位", ""),
    F("F", "广告", ""),
    G("G", "信用卡", PropertiesUtil.getStringValue("finance.h5.host") + "/#/creditDetail?id="),
    H("H", "理财", PropertiesUtil.getStringValue("finance.h5.host") + "/#/moneyDetail?id="),
    I("I", "渠道", ""),
    J("J", "活动页", PropertiesUtil.getStringValue("finance.h5.host") + "/#/activity?id="),
    K("K", "推广活动", "");

    private String id;
    private String name;
    private String url;

    DicTypeEnum(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    /**
     * 判断是否为商品类型
     *
     * @param id
     * @return
     */
    public static boolean isProductType(String id) {
        if (DicTypeEnum.B.getId().equals(id) || DicTypeEnum.G.getId().equals(id) ||
                DicTypeEnum.H.getId().equals(id)) {
            return true;
        }
        return false;
    }

    public static boolean checkName(String id) {
        if (DicTypeEnum.B.getId().equals(id) || DicTypeEnum.G.getId().equals(id) ||
                DicTypeEnum.H.getId().equals(id) || DicTypeEnum.A.getId().equals(id)
                || DicTypeEnum.I.getId().equals(id)) {
            return true;
        }
        return false;
    }

    /**
     * 获取url
     *
     * @param id
     * @return
     */
    public static String getUrl(String id) {
        for (DicTypeEnum v : DicTypeEnum.values()) {
            if (v.getId().equals(id)) {
                return v.getUrl();
            }
        }
        return "";
    }
}
