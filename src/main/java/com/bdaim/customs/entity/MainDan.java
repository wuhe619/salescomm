package com.bdaim.customs.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MainDan {
    private Integer id;
    @JsonProperty(value = "I_E_FLAG")
    private String i_e_flag;
    @JsonProperty(value = "I_E_PORT")
    private String i_e_port;
    @JsonProperty(value = "I_E_DATE")
    private String i_e_date;
    @JsonProperty(value = "D_DATE")
    private String d_date;
    @JsonProperty(value = "DECL_PORT")
    private String decl_port;
    @JsonProperty(value = "SEND_COUNTRY")
    private String send_country;
    @JsonProperty(value = "AGENT_TYPE")
    private String agent_type;
    @JsonProperty(value = "LAND_PROXY")
    private String land_proxy;
    @JsonProperty(value = "CURR_CODE")
    private String curr_code;
    //    private String TRADE_COUNTRY;
    @JsonProperty(value = "TRAF_MODE")
    private String traf_mode;
    @JsonProperty(value = "WRAP_TYPE")
    private String wrap_type;
    @JsonProperty(value = "WRAP_WOOD")
    private String wrap_wood;
    @JsonProperty(value = "IS_OLD")
    private String is_old;
    @JsonProperty(value = "ID_TYPE")
    private String id_type;
    @JsonProperty(value = "L_T_TRANS")
    private String l_t_trans;
    @JsonProperty(value = "ENTRY_TYPE")
    private String entry_type;
    @JsonProperty(value = "ONE_TO_BOTTOM")
    private String one_to_bottom;   //是否一单到底
    @JsonProperty(value = "RECEIPT_DATE")
    private String receipt_date;    //接单日期
    @JsonProperty(value = "BILL_NO")
    private String bill_no;         //主单号
    @JsonProperty(value = "PACK_NO")
    private String pack_no;     //件数
    @JsonProperty(value = "GROSS_WT")
    private String gross_wt;    //主单毛重
    @JsonProperty(value = "CHARGE_WT")
    private String charge_wt;   //主单计费重量
    @JsonProperty(value = "SINGLE_BATCH_NUM")
    private String single_batch_num;    //分单数量
    @JsonProperty(value = "PRODUCT_NUM")
    private String product_num; //商品数量
    @JsonProperty(value = "TRAF_NAME")
    private String traf_name;   //运输工具名称
    @JsonProperty(value = "TRAF_NAME_EN")
    private String traf_name_en;    //运输工具英文名称
    @JsonProperty(value = "VOYAGE_NO")
    private String voyage_no;   //航班号
    @JsonProperty(value = "VOYAGE_TIMES")
    private String voyage_times;    //航次
    @JsonProperty(value = "VOYAGE_DATE")
    private String voyage_date; //航班日期
    @JsonProperty(value = "TRADE_COUNTRY")
    private String trade_country;   //起运国家
    @JsonProperty(value = "E_AIRPORT")
    private String e_airport;   //出港机场
    @JsonProperty(value = "I_D_DATE")
    private String i_d_date;    //进港日期
    @JsonProperty(value = "QIYUN_PORT")
    private String qiyun_port;  //起运港口
    @JsonProperty(value = "USA_PROV")
    private String usa_prov;    //美国城市所属州
    @JsonProperty(value = "WAREHOUSE_CODE")
    private String warehouse_code;  //仓库编码
    @JsonProperty(value = "SEND_NAME")
    private String send_name;   //发件人
    @JsonProperty(value = "SEND_NAME_EN")
    private String send_name_en;    //发件人英文
    @JsonProperty(value = "SEND_CITY")
    private String send_city;   //发件人城市
    @JsonProperty(value = "SEND_CITY_EN")
    private String send_city_en;    //发件人城市英文
    @JsonProperty(value = "SEND_ADDRESS")
    private String send_address;   //发件人地址
    @JsonProperty(value = "SEND_ADDRESS_EN")
    private String send_address_en;    //发件人地址英文
    @JsonProperty(value = "SEND_TEL")
    private String send_tel; //发件人电话
    @JsonProperty(value = "STOP_ADDRESS")
    private String stop_access;//经停地址
    @JsonProperty(value = "STOP_ADDRESS_EN")
    private String stop_address_en;//经停地址英文
    @JsonProperty(value = "S_C_CODE_DECL")
    private String s_c_code_decl;//申报单位社会信用代码
    @JsonProperty(value = "S_C_CODE_SEND_REC")
    private String s_c_code_send_rec;//收发货人社会信用代码
    @JsonProperty(value = "S_C_CODE_SHIPPER")
    private String s_c_code_shipper;//货主社会信用代码
    @JsonProperty(value = "S_C_CODE_BUSI_UNIT")
    private String s_c_code_busi_unit;//经营单位代码
    @JsonProperty(value = "TRADE_MODE")
    private String trade_mode;//监管方式
    @JsonProperty(value = "TRANS_MODE")
    private String trans_mode;//成交方式
    @JsonProperty(value = "FEE_MARK")
    private String fee_mark; //运费标记
    @JsonProperty(value = "FEE_CURR")
    private String fee_curr;//运费币制
    @JsonProperty(value = "FEE_RATE")
    private String fee_rate;//运费率
    @JsonProperty(value = "INSUR_MARK")
    private String insur_mark;//保险费标记
    @JsonProperty(value = "INSUR_CURR")
    private String insur_curr;//保险费币制
    @JsonProperty(value = "INSUR_RATE")
    private String insur_rate;//保险费／率
    @JsonProperty(value = "OTHER_MARK")
    private String other_mark;// 杂费标记
    @JsonProperty(value = "OTHER_CURR")
    private String other_curr;// 杂费币制
    @JsonProperty(value = "OTHER_RATE")
    private String other_rate;// 杂费／率
    @JsonProperty(value = "INSPCTION_TYPE")
    private String inspction_type;//   报检类型
    @JsonProperty(value = "SPLIT_LUGGAGE")
    private String split_luggage; //分运行李
    @JsonProperty(value = "DEPART_ARRIVAL_PORT")
    private String depart_arrival_port;//起运/运抵港
    @JsonProperty(value = "NOTE_S")
    private String note_S; //备注
    private String entrusted_unit;//委托单位
    private String business_unit_name;//经营单位名称
    @JsonProperty(value = "CUST_ID")
    private String custId;//报关单位
    @JsonProperty(value = "RECEIPTZ_STATUS")
    private String receiptz_status;//最新回执
    @JsonProperty(value = "MAIN_GNAME")
    private String main_gname;//主要货物名称
    @JsonProperty(value = "STATION_ID")
    private String stationId;//场站id
    @JsonProperty(value = "COMMIT_CANGDAN_STATUS")
    private String commit_sangdan_status;//舱单已提交 Y
    @JsonProperty(value = "COMMIT_BAODAN_STATUS")
    private String commit_baodan_status;//报单已提交 Y
    @JsonProperty(value = "LOWER_GOODS")
    private String lower_goods;//低价商品
    @JsonProperty(value = "OVER_WARP")
    private String over_warp;//溢短装
    @JsonProperty(value = "CREATE_DATE")
    private String create_date;//导入日期
    @JsonProperty(value = "SINGLES")
    private List<PartyDan> singles;

    /**
     * 低价商品数
     */
    private Integer low_price_goods;

    /**
     * 身份证图片数量
     */
    private Integer id_card_pic_number;

    /**
     * 身份证核验数量
     */
    private Integer id_card_check_number;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getI_e_flag() {
        return i_e_flag;
    }

    public void setI_e_flag(String i_e_flag) {
        this.i_e_flag = i_e_flag;
    }

    public String getI_e_port() {
        return i_e_port;
    }

    public void setI_e_port(String i_e_port) {
        this.i_e_port = i_e_port;
    }

    public String getI_e_date() {
        return i_e_date;
    }

    public void setI_e_date(String i_e_date) {
        this.i_e_date = i_e_date;
    }

    public String getD_date() {
        return d_date;
    }

    public void setD_date(String d_date) {
        this.d_date = d_date;
    }

    public String getDecl_port() {
        return decl_port;
    }

    public void setDecl_port(String decl_port) {
        this.decl_port = decl_port;
    }

    public String getSend_country() {
        return send_country;
    }

    public void setSend_country(String send_country) {
        this.send_country = send_country;
    }

    public String getAgent_type() {
        return agent_type;
    }

    public void setAgent_type(String agent_type) {
        this.agent_type = agent_type;
    }

    public String getLand_proxy() {
        return land_proxy;
    }

    public void setLand_proxy(String land_proxy) {
        this.land_proxy = land_proxy;
    }

    public String getCurr_code() {
        return curr_code;
    }

    public void setCurr_code(String curr_code) {
        this.curr_code = curr_code;
    }

    public String getTraf_mode() {
        return traf_mode;
    }

    public void setTraf_mode(String traf_mode) {
        this.traf_mode = traf_mode;
    }

    public String getWrap_type() {
        return wrap_type;
    }

    public void setWrap_type(String wrap_type) {
        this.wrap_type = wrap_type;
    }

    public String getWrap_wood() {
        return wrap_wood;
    }

    public void setWrap_wood(String wrap_wood) {
        this.wrap_wood = wrap_wood;
    }

    public String getIs_old() {
        return is_old;
    }

    public void setIs_old(String is_old) {
        this.is_old = is_old;
    }

    public String getId_type() {
        return id_type;
    }

    public void setId_type(String id_type) {
        this.id_type = id_type;
    }

    public String getL_t_trans() {
        return l_t_trans;
    }

    public void setL_t_trans(String l_t_trans) {
        this.l_t_trans = l_t_trans;
    }

    public String getEntry_type() {
        return entry_type;
    }

    public void setEntry_type(String entry_type) {
        this.entry_type = entry_type;
    }

    public String getOne_to_bottom() {
        return one_to_bottom;
    }

    public void setOne_to_bottom(String one_to_bottom) {
        this.one_to_bottom = one_to_bottom;
    }

    public String getReceipt_date() {
        return receipt_date;
    }

    public void setReceipt_date(String receipt_date) {
        this.receipt_date = receipt_date;
    }

    public String getBill_no() {
        return bill_no;
    }

    public void setBill_no(String bill_no) {
        this.bill_no = bill_no;
    }

    public String getPack_no() {
        return pack_no;
    }

    public void setPack_no(String pack_no) {
        this.pack_no = pack_no;
    }

    public String getGross_wt() {
        return gross_wt;
    }

    public void setGross_wt(String gross_wt) {
        this.gross_wt = gross_wt;
    }

    public String getCharge_wt() {
        return charge_wt;
    }

    public void setCharge_wt(String charge_wt) {
        this.charge_wt = charge_wt;
    }

    public String getSingle_batch_num() {
        return single_batch_num;
    }

    public void setSingle_batch_num(String single_batch_num) {
        this.single_batch_num = single_batch_num;
    }

    public String getProduct_num() {
        return product_num;
    }

    public void setProduct_num(String product_num) {
        this.product_num = product_num;
    }

    public String getTraf_name() {
        return traf_name;
    }

    public void setTraf_name(String traf_name) {
        this.traf_name = traf_name;
    }

    public String getTraf_name_en() {
        return traf_name_en;
    }

    public void setTraf_name_en(String traf_name_en) {
        this.traf_name_en = traf_name_en;
    }

    public String getVoyage_no() {
        return voyage_no;
    }

    public void setVoyage_no(String voyage_no) {
        this.voyage_no = voyage_no;
    }

    public String getVoyage_times() {
        return voyage_times;
    }

    public void setVoyage_times(String voyage_times) {
        this.voyage_times = voyage_times;
    }

    public String getVoyage_date() {
        return voyage_date;
    }

    public void setVoyage_date(String voyage_date) {
        this.voyage_date = voyage_date;
    }

    public String getTrade_country() {
        return trade_country;
    }

    public void setTrade_country(String trade_country) {
        this.trade_country = trade_country;
    }

    public String getE_airport() {
        return e_airport;
    }

    public void setE_airport(String e_airport) {
        this.e_airport = e_airport;
    }

    public String getI_d_date() {
        return i_d_date;
    }

    public void setI_d_date(String i_d_date) {
        this.i_d_date = i_d_date;
    }

    public String getQiyun_port() {
        return qiyun_port;
    }

    public void setQiyun_port(String qiyun_port) {
        this.qiyun_port = qiyun_port;
    }

    public String getUsa_prov() {
        return usa_prov;
    }

    public void setUsa_prov(String usa_prov) {
        this.usa_prov = usa_prov;
    }

    public String getWarehouse_code() {
        return warehouse_code;
    }

    public void setWarehouse_code(String warehouse_code) {
        this.warehouse_code = warehouse_code;
    }

    public String getSend_name() {
        return send_name;
    }

    public void setSend_name(String send_name) {
        this.send_name = send_name;
    }

    public String getSend_name_en() {
        return send_name_en;
    }

    public void setSend_name_en(String send_name_en) {
        this.send_name_en = send_name_en;
    }

    public String getSend_city() {
        return send_city;
    }

    public void setSend_city(String send_city) {
        this.send_city = send_city;
    }

    public String getSend_city_en() {
        return send_city_en;
    }

    public void setSend_city_en(String send_city_en) {
        this.send_city_en = send_city_en;
    }

    public String getSend_address() {
        return send_address;
    }

    public void setSend_address(String send_address) {
        this.send_address = send_address;
    }

    public String getSend_address_en() {
        return send_address_en;
    }

    public void setSend_address_en(String send_address_en) {
        this.send_address_en = send_address_en;
    }

    public String getSend_tel() {
        return send_tel;
    }

    public void setSend_tel(String send_tel) {
        this.send_tel = send_tel;
    }

    public String getStop_access() {
        return stop_access;
    }

    public void setStop_access(String stop_access) {
        this.stop_access = stop_access;
    }

    public String getStop_address_en() {
        return stop_address_en;
    }

    public void setStop_address_en(String stop_address_en) {
        this.stop_address_en = stop_address_en;
    }

    public String getS_c_code_decl() {
        return s_c_code_decl;
    }

    public void setS_c_code_decl(String s_c_code_decl) {
        this.s_c_code_decl = s_c_code_decl;
    }

    public String getS_c_code_send_rec() {
        return s_c_code_send_rec;
    }

    public void setS_c_code_send_rec(String s_c_code_send_rec) {
        this.s_c_code_send_rec = s_c_code_send_rec;
    }

    public String getS_c_code_shipper() {
        return s_c_code_shipper;
    }

    public void setS_c_code_shipper(String s_c_code_shipper) {
        this.s_c_code_shipper = s_c_code_shipper;
    }

    public String getS_c_code_busi_unit() {
        return s_c_code_busi_unit;
    }

    public void setS_c_code_busi_unit(String s_c_code_busi_unit) {
        this.s_c_code_busi_unit = s_c_code_busi_unit;
    }

    public String getTrade_mode() {
        return trade_mode;
    }

    public void setTrade_mode(String trade_mode) {
        this.trade_mode = trade_mode;
    }

    public String getTrans_mode() {
        return trans_mode;
    }

    public void setTrans_mode(String trans_mode) {
        this.trans_mode = trans_mode;
    }

    public String getFee_mark() {
        return fee_mark;
    }

    public void setFee_mark(String fee_mark) {
        this.fee_mark = fee_mark;
    }

    public String getFee_curr() {
        return fee_curr;
    }

    public void setFee_curr(String fee_curr) {
        this.fee_curr = fee_curr;
    }

    public String getFee_rate() {
        return fee_rate;
    }

    public void setFee_rate(String fee_rate) {
        this.fee_rate = fee_rate;
    }

    public String getInsur_mark() {
        return insur_mark;
    }

    public void setInsur_mark(String insur_mark) {
        this.insur_mark = insur_mark;
    }

    public String getInsur_curr() {
        return insur_curr;
    }

    public void setInsur_curr(String insur_curr) {
        this.insur_curr = insur_curr;
    }

    public String getInsur_rate() {
        return insur_rate;
    }

    public void setInsur_rate(String insur_rate) {
        this.insur_rate = insur_rate;
    }

    public String getOther_mark() {
        return other_mark;
    }

    public void setOther_mark(String other_mark) {
        this.other_mark = other_mark;
    }

    public String getOther_curr() {
        return other_curr;
    }

    public void setOther_curr(String other_curr) {
        this.other_curr = other_curr;
    }

    public String getOther_rate() {
        return other_rate;
    }

    public void setOther_rate(String other_rate) {
        this.other_rate = other_rate;
    }

    public String getInspction_type() {
        return inspction_type;
    }

    public void setInspction_type(String inspction_type) {
        this.inspction_type = inspction_type;
    }

    public String getSplit_luggage() {
        return split_luggage;
    }

    public void setSplit_luggage(String split_luggage) {
        this.split_luggage = split_luggage;
    }

    public String getDepart_arrival_port() {
        return depart_arrival_port;
    }

    public void setDepart_arrival_port(String depart_arrival_port) {
        this.depart_arrival_port = depart_arrival_port;
    }

    public String getNote_S() {
        return note_S;
    }

    public void setNote_S(String note_S) {
        this.note_S = note_S;
    }

    public String getEntrusted_unit() {
        return entrusted_unit;
    }

    public void setEntrusted_unit(String entrusted_unit) {
        this.entrusted_unit = entrusted_unit;
    }

    public String getBusiness_unit_name() {
        return business_unit_name;
    }

    public void setBusiness_unit_name(String business_unit_name) {
        this.business_unit_name = business_unit_name;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getReceiptz_status() {
        return receiptz_status;
    }

    public void setReceiptz_status(String receiptz_status) {
        this.receiptz_status = receiptz_status;
    }

    public String getMain_gname() {
        return main_gname;
    }

    public void setMain_gname(String main_gname) {
        this.main_gname = main_gname;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getCommit_sangdan_status() {
        return commit_sangdan_status;
    }

    public void setCommit_sangdan_status(String commit_sangdan_status) {
        this.commit_sangdan_status = commit_sangdan_status;
    }

    public String getCommit_baodan_status() {
        return commit_baodan_status;
    }

    public void setCommit_baodan_status(String commit_baodan_status) {
        this.commit_baodan_status = commit_baodan_status;
    }

    public String getLower_goods() {
        return lower_goods;
    }

    public void setLower_goods(String lower_goods) {
        this.lower_goods = lower_goods;
    }

    public String getOver_warp() {
        return over_warp;
    }

    public void setOver_warp(String over_warp) {
        this.over_warp = over_warp;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public List<PartyDan> getSingles() {
        return singles;
    }

    public void setSingles(List<PartyDan> singles) {
        this.singles = singles;
    }

    public Integer getLow_price_goods() {
        return low_price_goods;
    }

    public void setLow_price_goods(Integer low_price_goods) {
        this.low_price_goods = low_price_goods;
    }

    public Integer getId_card_pic_number() {
        return id_card_pic_number;
    }

    public void setId_card_pic_number(Integer id_card_pic_number) {
        this.id_card_pic_number = id_card_pic_number;
    }

    public Integer getId_card_check_number() {
        return id_card_check_number;
    }

    public void setId_card_check_number(Integer id_card_check_number) {
        this.id_card_check_number = id_card_check_number;
    }
}
