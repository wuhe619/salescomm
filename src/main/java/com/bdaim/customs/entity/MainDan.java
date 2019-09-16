package com.bdaim.customs.entity;

import java.util.List;

public class MainDan {
    private String I_E_FLAG;
    private String I_E_PORT;
    private String I_E_DATE;
    private String  D_DATE;
    private String DECL_PORT;
    private String SEND_COUNTRY;
    private String AGENT_TYPE;
    private String LAND_PROXY;
    private String CURR_CODE;
//    private String TRADE_COUNTRY;
    private String TRAF_MODE;
    private String WRAP_TYPE;
    private String WRAP_WOOD;
    private String IS_OLD;
    private String ID_TYPE;
    private String L_T_TRANS;
    private String ENTRY_TYPE;
    private String ONE_TO_BOTTOM;   //是否一单到底
    private String RECEIPT_DATE;    //接单日期
    private String BILL_NO;         //主单号
    private String PACK_NO;     //件数
    private String GROSS_WT;    //主单毛重
    private String CHARGE_WT;   //主单计费重量
    private String SINGLE_BATCH_NUM;    //分单数量
    private String PRODUCT_NUM; //商品数量
    private String TRAF_NAME;   //运输工具名称
    private String TRAF_NAME_EN;    //运输工具英文名称
    private String VOYAGE_NO;   //航班号
    private String VOYAGE_TIMES;    //航次
    private String VOYAGE_DATE; //航班日期
    private String TRADE_COUNTRY;   //起运国家
    private String E_AIRPORT;   //出港机场
    private String I_D_DATE;    //进港日期
    private String QIYUN_PORT;  //起运港口
    private String USA_PROV;    //美国城市所属州
    private String WAREHOUSE_CODE;  //仓库编码
    private String SEND_NAME;   //发件人
    private String SEND_NAME_EN;    //发件人英文
    private String SEND_CITY;   //发件人城市
    private String SEND_CITY_EN;    //发件人城市英文
    private String	SEND_ADDRESS;   //发件人地址
    private String	SEND_ADDRESS_EN;    //发件人地址英文
    private String	SEND_TEL; //发件人电话
    private String	STOP_ADDRESS;//经停地址
    private String	STOP_ADDRESS_EN;//经停地址英文
    private String	S_C_CODE_DECL;//申报单位社会信用代码
    private String	S_C_CODE_SEND_REC;//收发货人社会信用代码
    private String	S_C_CODE_SHIPPER;//货主社会信用代码
    private String	S_C_CODE_BUSI_UNIT;//经营单位代码
    private String	TRADE_MODE;//监管方式
    private String  TRANS_MODE;//成交方式
    private String	FEE_MARK; //运费标记
    private String	FEE_CURR;//运费币制
    private String	FEE_RATE;//运费率
    private String	INSUR_MARK ;//保险费标记
    private String	INSUR_CURR ;//保险费币制
    private String	INSUR_RATE    ;//保险费／率
    private String	OTHER_MARK;// 杂费标记
    private String	OTHER_CURR ;// 杂费币制
    private String	OTHER_RATE;// 杂费／率
    private String	INSPCTION_TYPE;//   报检类型
    private String	SPLIT_LUGGAGE; //分运行李
    private String  NOTE_S; //备注

    private List<PartyDan> SINGLES;

    public String getI_E_FLAG() {
        return I_E_FLAG;
    }

    public void setI_E_FLAG(String i_E_FLAG) {
        I_E_FLAG = i_E_FLAG;
    }

    public String getI_E_PORT() {
        return I_E_PORT;
    }

    public void setI_E_PORT(String i_E_PORT) {
        I_E_PORT = i_E_PORT;
    }

    public String getI_E_DATE() {
        return I_E_DATE;
    }

    public void setI_E_DATE(String i_E_DATE) {
        I_E_DATE = i_E_DATE;
    }

    public String getD_DATE() {
        return D_DATE;
    }

    public void setD_DATE(String d_DATE) {
        D_DATE = d_DATE;
    }

    public String getDECL_PORT() {
        return DECL_PORT;
    }

    public void setDECL_PORT(String DECL_PORT) {
        this.DECL_PORT = DECL_PORT;
    }

    public String getSEND_COUNTRY() {
        return SEND_COUNTRY;
    }

    public void setSEND_COUNTRY(String SEND_COUNTRY) {
        this.SEND_COUNTRY = SEND_COUNTRY;
    }

    public String getAGENT_TYPE() {
        return AGENT_TYPE;
    }

    public void setAGENT_TYPE(String AGENT_TYPE) {
        this.AGENT_TYPE = AGENT_TYPE;
    }

    public String getLAND_PROXY() {
        return LAND_PROXY;
    }

    public void setLAND_PROXY(String LAND_PROXY) {
        this.LAND_PROXY = LAND_PROXY;
    }

    public String getCURR_CODE() {
        return CURR_CODE;
    }

    public void setCURR_CODE(String CURR_CODE) {
        this.CURR_CODE = CURR_CODE;
    }

    public String getTRADE_COUNTRY() {
        return TRADE_COUNTRY;
    }

    public void setTRADE_COUNTRY(String TRADE_COUNTRY) {
        this.TRADE_COUNTRY = TRADE_COUNTRY;
    }

    public String getE_AIRPORT() {
        return E_AIRPORT;
    }

    public void setE_AIRPORT(String e_AIRPORT) {
        E_AIRPORT = e_AIRPORT;
    }

    public String getI_D_DATE() {
        return I_D_DATE;
    }

    public void setI_D_DATE(String i_D_DATE) {
        I_D_DATE = i_D_DATE;
    }

    public String getQIYUN_PORT() {
        return QIYUN_PORT;
    }

    public void setQIYUN_PORT(String QIYUN_PORT) {
        this.QIYUN_PORT = QIYUN_PORT;
    }

    public String getUSA_PROV() {
        return USA_PROV;
    }

    public void setUSA_PROV(String USA_PROV) {
        this.USA_PROV = USA_PROV;
    }

    public String getWAREHOUSE_CODE() {
        return WAREHOUSE_CODE;
    }

    public void setWAREHOUSE_CODE(String WAREHOUSE_CODE) {
        this.WAREHOUSE_CODE = WAREHOUSE_CODE;
    }

    public String getSEND_NAME() {
        return SEND_NAME;
    }

    public void setSEND_NAME(String SEND_NAME) {
        this.SEND_NAME = SEND_NAME;
    }

    public String getSEND_NAME_EN() {
        return SEND_NAME_EN;
    }

    public void setSEND_NAME_EN(String SEND_NAME_EN) {
        this.SEND_NAME_EN = SEND_NAME_EN;
    }

    public String getSEND_CITY() {
        return SEND_CITY;
    }

    public void setSEND_CITY(String SEND_CITY) {
        this.SEND_CITY = SEND_CITY;
    }

    public String getSEND_CITY_EN() {
        return SEND_CITY_EN;
    }

    public void setSEND_CITY_EN(String SEND_CITY_EN) {
        this.SEND_CITY_EN = SEND_CITY_EN;
    }

    public String getSEND_ADDRESS() {
        return SEND_ADDRESS;
    }

    public void setSEND_ADDRESS(String SEND_ADDRESS) {
        this.SEND_ADDRESS = SEND_ADDRESS;
    }

    public String getSEND_ADDRESS_EN() {
        return SEND_ADDRESS_EN;
    }

    public void setSEND_ADDRESS_EN(String SEND_ADDRESS_EN) {
        this.SEND_ADDRESS_EN = SEND_ADDRESS_EN;
    }

    public String getSEND_TEL() {
        return SEND_TEL;
    }

    public void setSEND_TEL(String SEND_TEL) {
        this.SEND_TEL = SEND_TEL;
    }

    public String getSTOP_ADDRESS() {
        return STOP_ADDRESS;
    }

    public void setSTOP_ADDRESS(String STOP_ADDRESS) {
        this.STOP_ADDRESS = STOP_ADDRESS;
    }

    public String getSTOP_ADDRESS_EN() {
        return STOP_ADDRESS_EN;
    }

    public void setSTOP_ADDRESS_EN(String STOP_ADDRESS_EN) {
        this.STOP_ADDRESS_EN = STOP_ADDRESS_EN;
    }

    public String getS_C_CODE_DECL() {
        return S_C_CODE_DECL;
    }

    public void setS_C_CODE_DECL(String s_C_CODE_DECL) {
        S_C_CODE_DECL = s_C_CODE_DECL;
    }

    public String getS_C_CODE_SEND_REC() {
        return S_C_CODE_SEND_REC;
    }

    public void setS_C_CODE_SEND_REC(String s_C_CODE_SEND_REC) {
        S_C_CODE_SEND_REC = s_C_CODE_SEND_REC;
    }

    public String getS_C_CODE_SHIPPER() {
        return S_C_CODE_SHIPPER;
    }

    public void setS_C_CODE_SHIPPER(String s_C_CODE_SHIPPER) {
        S_C_CODE_SHIPPER = s_C_CODE_SHIPPER;
    }

    public String getS_C_CODE_BUSI_UNIT() {
        return S_C_CODE_BUSI_UNIT;
    }

    public void setS_C_CODE_BUSI_UNIT(String s_C_CODE_BUSI_UNIT) {
        S_C_CODE_BUSI_UNIT = s_C_CODE_BUSI_UNIT;
    }

    public String getTRADE_MODE() {
        return TRADE_MODE;
    }

    public void setTRADE_MODE(String TRADE_MODE) {
        this.TRADE_MODE = TRADE_MODE;
    }

    public String getTRANS_MODE() {
        return TRANS_MODE;
    }

    public void setTRANS_MODE(String TRANS_MODE) {
        this.TRANS_MODE = TRANS_MODE;
    }

    public String getFEE_MARK() {
        return FEE_MARK;
    }

    public void setFEE_MARK(String FEE_MARK) {
        this.FEE_MARK = FEE_MARK;
    }

    public String getFEE_CURR() {
        return FEE_CURR;
    }

    public void setFEE_CURR(String FEE_CURR) {
        this.FEE_CURR = FEE_CURR;
    }

    public String getFEE_RATE() {
        return FEE_RATE;
    }

    public void setFEE_RATE(String FEE_RATE) {
        this.FEE_RATE = FEE_RATE;
    }

    public String getINSUR_MARK() {
        return INSUR_MARK;
    }

    public void setINSUR_MARK(String INSUR_MARK) {
        this.INSUR_MARK = INSUR_MARK;
    }

    public String getINSUR_CURR() {
        return INSUR_CURR;
    }

    public void setINSUR_CURR(String INSUR_CURR) {
        this.INSUR_CURR = INSUR_CURR;
    }

    public String getINSUR_RATE() {
        return INSUR_RATE;
    }

    public void setINSUR_RATE(String INSUR_RATE) {
        this.INSUR_RATE = INSUR_RATE;
    }

    public String getOTHER_MARK() {
        return OTHER_MARK;
    }

    public void setOTHER_MARK(String OTHER_MARK) {
        this.OTHER_MARK = OTHER_MARK;
    }

    public String getOTHER_CURR() {
        return OTHER_CURR;
    }

    public void setOTHER_CURR(String OTHER_CURR) {
        this.OTHER_CURR = OTHER_CURR;
    }

    public String getOTHER_RATE() {
        return OTHER_RATE;
    }

    public void setOTHER_RATE(String OTHER_RATE) {
        this.OTHER_RATE = OTHER_RATE;
    }

    public String getINSPCTION_TYPE() {
        return INSPCTION_TYPE;
    }

    public void setINSPCTION_TYPE(String INSPCTION_TYPE) {
        this.INSPCTION_TYPE = INSPCTION_TYPE;
    }

    public String getSPLIT_LUGGAGE() {
        return SPLIT_LUGGAGE;
    }

    public void setSPLIT_LUGGAGE(String SPLIT_LUGGAGE) {
        this.SPLIT_LUGGAGE = SPLIT_LUGGAGE;
    }

    public String getNOTE_S() {
        return NOTE_S;
    }

    public void setNOTE_S(String NOTE_S) {
        this.NOTE_S = NOTE_S;
    }

    public List<PartyDan> getSINGLES() {
        return SINGLES;
    }

    public void setSINGLES(List<PartyDan> SINGLES) {
        this.SINGLES = SINGLES;
    }

    public String getTRAF_MODE() {
        return TRAF_MODE;
    }

    public void setTRAF_MODE(String TRAF_MODE) {
        this.TRAF_MODE = TRAF_MODE;
    }

    public String getWRAP_TYPE() {
        return WRAP_TYPE;
    }

    public void setWRAP_TYPE(String WRAP_TYPE) {
        this.WRAP_TYPE = WRAP_TYPE;
    }

    public String getWRAP_WOOD() {
        return WRAP_WOOD;
    }

    public void setWRAP_WOOD(String WRAP_WOOD) {
        this.WRAP_WOOD = WRAP_WOOD;
    }

    public String getIS_OLD() {
        return IS_OLD;
    }

    public void setIS_OLD(String IS_OLD) {
        this.IS_OLD = IS_OLD;
    }

    public String getID_TYPE() {
        return ID_TYPE;
    }

    public void setID_TYPE(String ID_TYPE) {
        this.ID_TYPE = ID_TYPE;
    }

    public String getL_T_TRANS() {
        return L_T_TRANS;
    }

    public void setL_T_TRANS(String l_T_TRANS) {
        L_T_TRANS = l_T_TRANS;
    }

    public String getENTRY_TYPE() {
        return ENTRY_TYPE;
    }

    public void setENTRY_TYPE(String ENTRY_TYPE) {
        this.ENTRY_TYPE = ENTRY_TYPE;
    }

    public String getONE_TO_BOTTOM() {
        return ONE_TO_BOTTOM;
    }

    public void setONE_TO_BOTTOM(String ONE_TO_BOTTOM) {
        this.ONE_TO_BOTTOM = ONE_TO_BOTTOM;
    }

    public String getRECEIPT_DATE() {
        return RECEIPT_DATE;
    }

    public void setRECEIPT_DATE(String RECEIPT_DATE) {
        this.RECEIPT_DATE = RECEIPT_DATE;
    }

    public String getBILL_NO() {
        return BILL_NO;
    }

    public void setBILL_NO(String BILL_NO) {
        this.BILL_NO = BILL_NO;
    }

    public String getPACK_NO() {
        return PACK_NO;
    }

    public void setPACK_NO(String PACK_NO) {
        this.PACK_NO = PACK_NO;
    }

    public String getGROSS_WT() {
        return GROSS_WT;
    }

    public void setGROSS_WT(String GROSS_WT) {
        this.GROSS_WT = GROSS_WT;
    }

    public String getCHARGE_WT() {
        return CHARGE_WT;
    }

    public void setCHARGE_WT(String CHARGE_WT) {
        this.CHARGE_WT = CHARGE_WT;
    }

    public String getSINGLE_BATCH_NUM() {
        return SINGLE_BATCH_NUM;
    }

    public void setSINGLE_BATCH_NUM(String SINGLE_BATCH_NUM) {
        this.SINGLE_BATCH_NUM = SINGLE_BATCH_NUM;
    }

    public String getPRODUCT_NUM() {
        return PRODUCT_NUM;
    }

    public void setPRODUCT_NUM(String PRODUCT_NUM) {
        this.PRODUCT_NUM = PRODUCT_NUM;
    }

    public String getTRAF_NAME() {
        return TRAF_NAME;
    }

    public void setTRAF_NAME(String TRAF_NAME) {
        this.TRAF_NAME = TRAF_NAME;
    }

    public String getTRAF_NAME_EN() {
        return TRAF_NAME_EN;
    }

    public void setTRAF_NAME_EN(String TRAF_NAME_EN) {
        this.TRAF_NAME_EN = TRAF_NAME_EN;
    }

    public String getVOYAGE_NO() {
        return VOYAGE_NO;
    }

    public void setVOYAGE_NO(String VOYAGE_NO) {
        this.VOYAGE_NO = VOYAGE_NO;
    }

    public String getVOYAGE_TIMES() {
        return VOYAGE_TIMES;
    }

    public void setVOYAGE_TIMES(String VOYAGE_TIMES) {
        this.VOYAGE_TIMES = VOYAGE_TIMES;
    }

    public String getVOYAGE_DATE() {
        return VOYAGE_DATE;
    }

    public void setVOYAGE_DATE(String VOYAGE_DATE) {
        this.VOYAGE_DATE = VOYAGE_DATE;
    }
}
