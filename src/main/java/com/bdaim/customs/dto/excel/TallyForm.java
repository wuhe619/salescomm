package com.bdaim.customs.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 理货单导出excel实体类
 *
 * @author chengning@salescomm.net
 * @date 2019/9/19
 * @description
 */
public class TallyForm {

    class MainData {
        /**
         * 进出口岸
         */
        @ExcelProperty(value = "进出口岸", index = 10)
        private String import_export_port;
        /**
         * 地面代理
         */
        @ExcelProperty(value = "地面代理", index = 12)
        private String ground_agent;

        /**
         * 报关类别
         */
        @ExcelProperty(value = "报关类别", index = 13)
        private String customs_category;
        /**
         * 是否一单到底
         */
        @ExcelProperty(value = "是否一单到底", index = 13)
        private String one_to_bottom;
        /**
         * 主单号
         */
        @ExcelProperty(value = "主单号", index = 0)
        private String bill_no;
        /**
         * 件数
         */
        @ExcelProperty(value = "件数", index = 5)
        private String pack_no;
        /**
         * 重量
         */
        @ExcelProperty(value = "重量", index = 6)
        private String weight;
        /**
         * 分单数量
         */
        @ExcelProperty(value = "分单数量", index = 7)
        private String single_batch_num;
        /**
         * 运输工具名称
         */
        @ExcelProperty(value = "运输工具名称", index = 11)
        private String traf_name;
        /**
         * 运输工具英文名称
         */

        private String traf_name_en;
        /**
         * 航班号
         */
        @ExcelProperty(value = "航班号", index = 1)
        private String voyage_no;
        /**
         * 航次
         */
        @ExcelProperty(value = "航次", index = 2)
        private String voyage_times;
        /**
         * 航班日期
         */
        @ExcelProperty(value = "航班日期", index = 3)
        private String voyage_date;
        /**
         * 起运国家
         */
        @ExcelProperty(value = "起运国家", index = 8)
        private String trade_country;
        /**
         * 出港机场
         */
        @ExcelProperty(value = "出港机场", index = 8)
        private String e_airport;
        /**
         * 进港日期
         */
        @ExcelProperty(value = "进港日期", index = 4)
        private String i_d_date;
        /**
         * 起运港口
         */
        @ExcelProperty(value = "起运港口", index = 9)
        private String qiyun_port;
        /**
         * 美国城市所属州
         */
        @ExcelProperty(value = "美国城市所属州", index = 8)
        private String usa_prov;
        /**
         * 仓库编码
         */
        @ExcelProperty(value = "仓库编码", index = 8)
        private String warehouse_code;
        /**
         * 发件人
         */
        @ExcelProperty(value = "发件人", index = 8)
        private String send_name;
        /**
         * 发件人英文
         */
        @ExcelProperty(value = "发件人英文", index = 8)
        private String send_name_en;
        /**
         * 发件人城市
         */
        @ExcelProperty(value = "发件人城市", index = 8)
        private String send_city;
        /**
         * 发件人城市英文
         */
        @ExcelProperty(value = "发件人城市英文", index = 8)
        private String SEND_CITY_EN;
        /**
         * 发件人地址
         */
        @ExcelProperty(value = "发件人地址", index = 8)
        private String send_address;
        /**
         * 发件人地址英文
         */
        @ExcelProperty(value = "发件人地址英文", index = 8)
        private String send_address_en;
        /**
         * 发件人电话
         */
        @ExcelProperty(value = "发件人电话", index = 8)
        private String send_tel;
        /**
         * 经停地址英文
         */
        @ExcelProperty(value = "经停地址英文", index = 8)
        private String stop_address_en;
        /**
         * 申报单位社会信用代码
         */
        @ExcelProperty(value = "申报单位社会信用代码", index = 8)
        private String s_c_code_decl;
        /**
         * 收发货人社会信用代码
         */
        @ExcelProperty(value = "收发货人社会信用代码", index = 8)
        private String s_c_code_send_rec;
        /**
         * 货主社会信用代码
         */
        @ExcelProperty(value = "货主社会信用代码", index = 8)
        private String s_c_code_shipper;
        /**
         * 经营单位代码
         */
        @ExcelProperty(value = "经营单位代码", index = 8)
        private String s_c_code_busi_unit;
        /**
         * 监管方式
         */
        @ExcelProperty(value = "监管方式", index = 8)
        private String trade_mode;
        /**
         * 成交方式
         */
        @ExcelProperty(value = "成交方式", index = 8)
        private String trans_mode;
        /**
         * 运费标记
         */
        @ExcelProperty(value = "运费标记", index = 8)
        private String fee_mark;
        /**
         * 运费币制
         */
        @ExcelProperty(value = "运费币制", index = 8)
        private String fee_curr;
        /**
         * 运费率
         */
        @ExcelProperty(value = "运费率", index = 8)
        private String fee_rate;
        /**
         * 保险费标记
         */
        @ExcelProperty(value = "仓库编码", index = 8)
        private String insur_mark;
        /**
         * 保险费币制
         */
        @ExcelProperty(value = "保险费币制", index = 8)
        private String insur_curr;
        /**
         * 保险费／率
         */
        @ExcelProperty(value = "保险费率", index = 8)
        private String insur_rate;
        /**
         * 杂费标记
         */
        @ExcelProperty(value = "杂费标记", index = 8)
        private String other_mark;
        /**
         * 杂费币制
         */
        @ExcelProperty(value = "杂费币制", index = 8)
        private String other_curr;
        /**
         * 杂费／率
         */
        @ExcelProperty(value = "杂费率", index = 8)
        private String other_rate;
        /**
         * 报检类型
         */
        @ExcelProperty(value = "报检类型", index = 8)
        private String inspction_type;
        /**
         * 分运行李
         */
        @ExcelProperty(value = "分运行李", index = 8)
        private String split_luggage;

        /**
         * 备注
         */
        @ExcelProperty(value = "备注", index = 8)
        private String note_S;
        /**
         * 经营单位名称
         */
        @ExcelProperty(value = "经营单位名称", index = 8)
        private String business_unit_name;
        /**
         * 报关单位
         */
        @ExcelProperty(value = "报关单位", index = 8)
        private String cust_id;

        public String getImport_export_port() {
            return import_export_port;
        }

        public void setImport_export_port(String import_export_port) {
            this.import_export_port = import_export_port;
        }

        public String getGround_agent() {
            return ground_agent;
        }

        public void setGround_agent(String ground_agent) {
            this.ground_agent = ground_agent;
        }

        public String getCustoms_category() {
            return customs_category;
        }

        public void setCustoms_category(String customs_category) {
            this.customs_category = customs_category;
        }

        public String getOne_to_bottom() {
            return one_to_bottom;
        }

        public void setOne_to_bottom(String one_to_bottom) {
            this.one_to_bottom = one_to_bottom;
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

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getSingle_batch_num() {
            return single_batch_num;
        }

        public void setSingle_batch_num(String single_batch_num) {
            this.single_batch_num = single_batch_num;
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

        public String getSEND_CITY_EN() {
            return SEND_CITY_EN;
        }

        public void setSEND_CITY_EN(String SEND_CITY_EN) {
            this.SEND_CITY_EN = SEND_CITY_EN;
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

        public String getNote_S() {
            return note_S;
        }

        public void setNote_S(String note_S) {
            this.note_S = note_S;
        }

        public String getBusiness_unit_name() {
            return business_unit_name;
        }

        public void setBusiness_unit_name(String business_unit_name) {
            this.business_unit_name = business_unit_name;
        }

        public String getCust_id() {
            return cust_id;
        }

        public void setCust_id(String cust_id) {
            this.cust_id = cust_id;
        }

    }

    class PartyData {

        private String id;
        private String main_bill_NO;//主单号
        /**
         * 分单号
         */
        @ExcelProperty(value = "分单号", index = 8)
        private String bill_NO;
        /**
         * 重量
         */
        @ExcelProperty(value = "重量", index = 8)
        private String weight;
        /**
         * 净重
         */
        @ExcelProperty(value = "净重", index = 8)
        private String netWeight;
        /**
         * 件数
         */
        @ExcelProperty(value = "件数", index = 8)
        private String pack_NO;
        private String main_gName;//主要货物名称

        @ExcelProperty(value = "证件类型", index = 10)
        private String id_type;

        private String total_value;//价值
        private String curr_code;//币制
        /**
         * 贸易国别
         */
        @ExcelProperty(value = "贸易国别", index = 8)
        private String trade_country;
        /**
         * 证件号码
         */
        @ExcelProperty(value = "证件号码", index = 8)
        private String id_NO;
        /**
         * 收件人姓名
         */
        @ExcelProperty(value = "收件人姓名", index = 8)
        private String receive_name;
        /**
         * 收件人电话
         */
        @ExcelProperty(value = "收件人电话", index = 8)
        private String receive_tel;
        /**
         * // 收件人地址
         */
        @ExcelProperty(value = "收件人地址", index = 8)
        private String receive_address;

        /**
         * 收件人姓名(申报)
         */
        @ExcelProperty(value = "收件人姓名(申报)", index = 8)
        private String receive_name_declare;
        /**
         * 收件人电话(申报)
         */
        @ExcelProperty(value = "收件人电话(申报)", index = 8)
        private String receive_tel_declare;
        /**
         * 收件人地址(申报)
         */
        @ExcelProperty(value = "收件人地址(申报)", index = 8)
        private String receive_address_declare;

        /**
         * 收件人城市
         */
        @ExcelProperty(value = "收件人城市", index = 8)
        private String receive_city;
        /**
         * 收件人国别
         */
        @ExcelProperty(value = "收件人国别", index = 8)
        private String receive_country;
        /**
         * 收件人英文
         */
        @ExcelProperty(value = "收件人英文", index = 8)
        private String receive_name_en;
        /**
         * 收件人城市英文
         */
        @ExcelProperty(value = "收件人城市英文", index = 8)
        private String receive_city_en;
        /**
         * 收件人地址英文
         */
        @ExcelProperty(value = "收件人地址英文", index = 8)
        private String receive_address_en;

        @ExcelProperty(value = "城市", index = 8)
        private String city;

        @ExcelProperty(value = "邮编", index = 8)
        private String zipCode;

        @ExcelProperty(value = "快递类型", index = 8)
        private String expressType;

        @ExcelProperty(value = "快递单号", index = 8)
        private String expressNo;

        @ExcelProperty(value = "快递客户单号", index = 8)
        private String expressCustNo;

        @ExcelProperty(value = "转运公司", index = 8)
        private String transportCompany;

        @ExcelProperty(value = "网站", index = 8)
        private String webSite;

        /**
         * 报关类别
         */
        @ExcelProperty(value = "报关类别", index = 13)
        private String customs_category;

        @ExcelProperty(value = "保价", index = 9)
        private String insuredPrice;

        @ExcelProperty(value = "RFIDCode", index = 10)
        private String RFIDCode;

        @ExcelProperty(value = "溢装原主单号", index = 11)
        private String overloadOldMainNo;

        @ExcelProperty(value = "报检状态", index = 12)
        private String inspectionStatus;

        /**
         * 经营单位名称
         */
        @ExcelProperty(value = "经营单位名称", index = 8)
        private String business_unit_name;

        /**
         * 经营单位代码
         */
        @ExcelProperty(value = "经营单位代码", index = 8)
        private String s_c_code_busi_unit;

        /**
         * 币制
         */
        @ExcelProperty(value = "币制", index = 8)
        private String fee_curr;

        /**
         * 监管方式
         */
        @ExcelProperty(value = "监管方式", index = 8)
        private String trade_mode;

        /**
         * 备注
         */
        @ExcelProperty(value = "备注", index = 8)
        private String note_S;


        /**
         * 是否含木质包装
         */
        @ExcelProperty(value = "是否含木质包装", index = 8)
        private String isWoodPackaging;


        /**
         * 是否为旧物品
         */
        @ExcelProperty(value = "是否为旧物品", index = 8)
        private String isOldArticles;

        /**
         * 是否为低温运输
         */
        @ExcelProperty(value = "是否为低温运输", index = 8)
        private String isLowTemperature;


        /**
         * 包装种类
         */
        @ExcelProperty(value = "包装种类", index = 8)
        private String packagingType;


        private String send_name;

        private String send_name_en;    //发件人英文
        private String send_city;   //发件人城市
        private String SEND_CITY_EN;    //发件人城市英文
        private String send_address;   //发件人地址
        private String send_address_en;    //发件人地址英文

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMain_bill_NO() {
            return main_bill_NO;
        }

        public void setMain_bill_NO(String main_bill_NO) {
            this.main_bill_NO = main_bill_NO;
        }

        public String getBill_NO() {
            return bill_NO;
        }

        public void setBill_NO(String bill_NO) {
            this.bill_NO = bill_NO;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getNetWeight() {
            return netWeight;
        }

        public void setNetWeight(String netWeight) {
            this.netWeight = netWeight;
        }

        public String getPack_NO() {
            return pack_NO;
        }

        public void setPack_NO(String pack_NO) {
            this.pack_NO = pack_NO;
        }

        public String getMain_gName() {
            return main_gName;
        }

        public void setMain_gName(String main_gName) {
            this.main_gName = main_gName;
        }

        public String getId_type() {
            return id_type;
        }

        public void setId_type(String id_type) {
            this.id_type = id_type;
        }

        public String getTotal_value() {
            return total_value;
        }

        public void setTotal_value(String total_value) {
            this.total_value = total_value;
        }

        public String getCurr_code() {
            return curr_code;
        }

        public void setCurr_code(String curr_code) {
            this.curr_code = curr_code;
        }

        public String getTrade_country() {
            return trade_country;
        }

        public void setTrade_country(String trade_country) {
            this.trade_country = trade_country;
        }

        public String getId_NO() {
            return id_NO;
        }

        public void setId_NO(String id_NO) {
            this.id_NO = id_NO;
        }

        public String getReceive_name() {
            return receive_name;
        }

        public void setReceive_name(String receive_name) {
            this.receive_name = receive_name;
        }

        public String getReceive_tel() {
            return receive_tel;
        }

        public void setReceive_tel(String receive_tel) {
            this.receive_tel = receive_tel;
        }

        public String getReceive_address() {
            return receive_address;
        }

        public void setReceive_address(String receive_address) {
            this.receive_address = receive_address;
        }

        public String getReceive_name_declare() {
            return receive_name_declare;
        }

        public void setReceive_name_declare(String receive_name_declare) {
            this.receive_name_declare = receive_name_declare;
        }

        public String getReceive_tel_declare() {
            return receive_tel_declare;
        }

        public void setReceive_tel_declare(String receive_tel_declare) {
            this.receive_tel_declare = receive_tel_declare;
        }

        public String getReceive_address_declare() {
            return receive_address_declare;
        }

        public void setReceive_address_declare(String receive_address_declare) {
            this.receive_address_declare = receive_address_declare;
        }

        public String getReceive_city() {
            return receive_city;
        }

        public void setReceive_city(String receive_city) {
            this.receive_city = receive_city;
        }

        public String getReceive_country() {
            return receive_country;
        }

        public void setReceive_country(String receive_country) {
            this.receive_country = receive_country;
        }

        public String getReceive_name_en() {
            return receive_name_en;
        }

        public void setReceive_name_en(String receive_name_en) {
            this.receive_name_en = receive_name_en;
        }

        public String getReceive_city_en() {
            return receive_city_en;
        }

        public void setReceive_city_en(String receive_city_en) {
            this.receive_city_en = receive_city_en;
        }

        public String getReceive_address_en() {
            return receive_address_en;
        }

        public void setReceive_address_en(String receive_address_en) {
            this.receive_address_en = receive_address_en;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public String getExpressType() {
            return expressType;
        }

        public void setExpressType(String expressType) {
            this.expressType = expressType;
        }

        public String getExpressNo() {
            return expressNo;
        }

        public void setExpressNo(String expressNo) {
            this.expressNo = expressNo;
        }

        public String getExpressCustNo() {
            return expressCustNo;
        }

        public void setExpressCustNo(String expressCustNo) {
            this.expressCustNo = expressCustNo;
        }

        public String getTransportCompany() {
            return transportCompany;
        }

        public void setTransportCompany(String transportCompany) {
            this.transportCompany = transportCompany;
        }

        public String getWebSite() {
            return webSite;
        }

        public void setWebSite(String webSite) {
            this.webSite = webSite;
        }

        public String getCustoms_category() {
            return customs_category;
        }

        public void setCustoms_category(String customs_category) {
            this.customs_category = customs_category;
        }

        public String getInsuredPrice() {
            return insuredPrice;
        }

        public void setInsuredPrice(String insuredPrice) {
            this.insuredPrice = insuredPrice;
        }

        public String getRFIDCode() {
            return RFIDCode;
        }

        public void setRFIDCode(String RFIDCode) {
            this.RFIDCode = RFIDCode;
        }

        public String getOverloadOldMainNo() {
            return overloadOldMainNo;
        }

        public void setOverloadOldMainNo(String overloadOldMainNo) {
            this.overloadOldMainNo = overloadOldMainNo;
        }

        public String getInspectionStatus() {
            return inspectionStatus;
        }

        public void setInspectionStatus(String inspectionStatus) {
            this.inspectionStatus = inspectionStatus;
        }

        public String getBusiness_unit_name() {
            return business_unit_name;
        }

        public void setBusiness_unit_name(String business_unit_name) {
            this.business_unit_name = business_unit_name;
        }

        public String getS_c_code_busi_unit() {
            return s_c_code_busi_unit;
        }

        public void setS_c_code_busi_unit(String s_c_code_busi_unit) {
            this.s_c_code_busi_unit = s_c_code_busi_unit;
        }

        public String getFee_curr() {
            return fee_curr;
        }

        public void setFee_curr(String fee_curr) {
            this.fee_curr = fee_curr;
        }

        public String getTrade_mode() {
            return trade_mode;
        }

        public void setTrade_mode(String trade_mode) {
            this.trade_mode = trade_mode;
        }

        public String getNote_S() {
            return note_S;
        }

        public void setNote_S(String note_S) {
            this.note_S = note_S;
        }

        public String getIsWoodPackaging() {
            return isWoodPackaging;
        }

        public void setIsWoodPackaging(String isWoodPackaging) {
            this.isWoodPackaging = isWoodPackaging;
        }

        public String getIsOldArticles() {
            return isOldArticles;
        }

        public void setIsOldArticles(String isOldArticles) {
            this.isOldArticles = isOldArticles;
        }

        public String getIsLowTemperature() {
            return isLowTemperature;
        }

        public void setIsLowTemperature(String isLowTemperature) {
            this.isLowTemperature = isLowTemperature;
        }

        public String getPackagingType() {
            return packagingType;
        }

        public void setPackagingType(String packagingType) {
            this.packagingType = packagingType;
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

        public String getSEND_CITY_EN() {
            return SEND_CITY_EN;
        }

        public void setSEND_CITY_EN(String SEND_CITY_EN) {
            this.SEND_CITY_EN = SEND_CITY_EN;
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

        public String getSend_name() {
            return send_name;
        }

        public void setSend_name(String send_name) {
            this.send_name = send_name;
        }
    }

    class ProductData {

        private String id;

        @ExcelProperty(value = "分单单号", index = 0)
        private String bill_no;

        @ExcelProperty(value = "商品编码", index = 1)
        private String code_ts;

        @ExcelProperty(value = "物品名称", index = 2)
        private String g_name;


        @ExcelProperty(value = "英文商品名称", index = 7)
        private String g_name_en;


        @ExcelProperty(value = "规格", index = 15)
        private String g_model;

        @ExcelProperty(value = "产销城市", index = 6)
        private String origin_city;

        @JsonProperty(value = "ORIGIN_COUNTRY")
        private String origin_country;// 产销国

        private String tradeCurr;//成交币制

        @ExcelProperty(value = "申报实价", index = 8)
        private String trade_total;//成交总价

        @ExcelProperty(value = "申报单价", index = 3)
        private String declPrice;

        @ExcelProperty(value = "申报单位", index = 14)
        private String decl_unit;


        private String decl_total;//申报总价

        @ExcelProperty(value = "用途", index = 16)
        private String use_to;

        @ExcelProperty(value = "用途", index = 17)
        private String manufacturer;

        private String duty_mode;//征减免税方式

        @ExcelProperty(value = "数量", index = 4)
        private String g_qty;//申报数量

        private String g_unit;//申报计量单位

        @ExcelProperty(value = "法定数量1", index = 10)
        private String qty_1;

        @ExcelProperty(value = "法定单位1", index = 11)
        private String unit_1;

        @ExcelProperty(value = "法定数量2", index = 12)
        private String qty_2;
        @ExcelProperty(value = "法定单位2", index = 13)
        private String unit_2;

        @ExcelProperty(value = "商品重量", index = 5)
        private String ggrossWt;

        @ExcelProperty(value = "备用字段1", index = 18)
        private String remark1;
        @ExcelProperty(value = "备用字段2", index = 19)
        private String remark2;
        @ExcelProperty(value = "备用字段3", index = 20)
        private String remark3;
        @ExcelProperty(value = "备用字段4", index = 21)
        private String remark4;
        @ExcelProperty(value = "备用字段5", index = 22)
        private String remark5;
        @ExcelProperty(value = "商品ID", index = 7)
        private String g_id;
        @ExcelProperty(value = "生产厂商", index = 7)
        private String manufactor;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBill_no() {
            return bill_no;
        }

        public void setBill_no(String bill_no) {
            this.bill_no = bill_no;
        }

        public String getCode_ts() {
            return code_ts;
        }

        public void setCode_ts(String code_ts) {
            this.code_ts = code_ts;
        }

        public String getG_name() {
            return g_name;
        }

        public void setG_name(String g_name) {
            this.g_name = g_name;
        }

        public String getG_name_en() {
            return g_name_en;
        }

        public void setG_name_en(String g_name_en) {
            this.g_name_en = g_name_en;
        }

        public String getG_model() {
            return g_model;
        }

        public void setG_model(String g_model) {
            this.g_model = g_model;
        }

        public String getOrigin_city() {
            return origin_city;
        }

        public void setOrigin_city(String origin_city) {
            this.origin_city = origin_city;
        }

        public String getOrigin_country() {
            return origin_country;
        }

        public void setOrigin_country(String origin_country) {
            this.origin_country = origin_country;
        }

        public String getTradeCurr() {
            return tradeCurr;
        }

        public void setTradeCurr(String tradeCurr) {
            this.tradeCurr = tradeCurr;
        }

        public String getTrade_total() {
            return trade_total;
        }

        public void setTrade_total(String trade_total) {
            this.trade_total = trade_total;
        }

        public String getDeclPrice() {
            return declPrice;
        }

        public void setDeclPrice(String declPrice) {
            this.declPrice = declPrice;
        }

        public String getDecl_unit() {
            return decl_unit;
        }

        public void setDecl_unit(String decl_unit) {
            this.decl_unit = decl_unit;
        }

        public String getDecl_total() {
            return decl_total;
        }

        public void setDecl_total(String decl_total) {
            this.decl_total = decl_total;
        }

        public String getUse_to() {
            return use_to;
        }

        public void setUse_to(String use_to) {
            this.use_to = use_to;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }

        public String getDuty_mode() {
            return duty_mode;
        }

        public void setDuty_mode(String duty_mode) {
            this.duty_mode = duty_mode;
        }

        public String getG_qty() {
            return g_qty;
        }

        public void setG_qty(String g_qty) {
            this.g_qty = g_qty;
        }

        public String getG_unit() {
            return g_unit;
        }

        public void setG_unit(String g_unit) {
            this.g_unit = g_unit;
        }

        public String getQty_1() {
            return qty_1;
        }

        public void setQty_1(String qty_1) {
            this.qty_1 = qty_1;
        }

        public String getUnit_1() {
            return unit_1;
        }

        public void setUnit_1(String unit_1) {
            this.unit_1 = unit_1;
        }

        public String getQty_2() {
            return qty_2;
        }

        public void setQty_2(String qty_2) {
            this.qty_2 = qty_2;
        }

        public String getUnit_2() {
            return unit_2;
        }

        public void setUnit_2(String unit_2) {
            this.unit_2 = unit_2;
        }

        public String getGgrossWt() {
            return ggrossWt;
        }

        public void setGgrossWt(String ggrossWt) {
            this.ggrossWt = ggrossWt;
        }

        public String getRemark1() {
            return remark1;
        }

        public void setRemark1(String remark1) {
            this.remark1 = remark1;
        }

        public String getRemark2() {
            return remark2;
        }

        public void setRemark2(String remark2) {
            this.remark2 = remark2;
        }

        public String getRemark3() {
            return remark3;
        }

        public void setRemark3(String remark3) {
            this.remark3 = remark3;
        }

        public String getRemark4() {
            return remark4;
        }

        public void setRemark4(String remark4) {
            this.remark4 = remark4;
        }

        public String getRemark5() {
            return remark5;
        }

        public void setRemark5(String remark5) {
            this.remark5 = remark5;
        }

        public String getG_id() {
            return g_id;
        }

        public void setG_id(String g_id) {
            this.g_id = g_id;
        }

        public String getManufactor() {
            return manufactor;
        }

        public void setManufactor(String manufactor) {
            this.manufactor = manufactor;
        }


    }
}
