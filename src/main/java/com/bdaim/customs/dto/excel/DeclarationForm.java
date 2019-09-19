package com.bdaim.customs.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 报检单导出excel实体类
 *
 * @author chengning@salescomm.net
 * @date 2019/9/19
 * @description
 */
public class DeclarationForm {

    class MainData {

        /**
         * 主单号
         */
        @ExcelProperty(value = "主单号", index = 0)
        private String bill_no;

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


        @ExcelProperty(value = "到货日期", index = 4)
        private String arrivalDate;

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
         * 起运国家
         */
        @ExcelProperty(value = "起运国家", index = 8)
        private String trade_country;

        /**
         * 起运港口
         */
        @ExcelProperty(value = "起运港口", index = 9)
        private String qiyun_port;

        /**
         * 进出口岸
         */
        @ExcelProperty(value = "进出口岸", index = 10)
        private String import_export_port;


        /**
         * 运输工具名称
         */
        @ExcelProperty(value = "运输工具名称", index = 11)
        private String traf_name;
        /**
         * 地面代理
         */
        @ExcelProperty(value = "地面代理", index = 12)
        private String ground_agent;

        /**
         * 报关类别
         */
        @ExcelProperty(value = "是否纯个人", index = 13)
        private String isPersonal;
        /**
         * 是否一单到底
         */
        @ExcelProperty(value = "是否一单到底", index = 14)
        private String one_to_bottom;
        /**
         * 报检类型
         */
        @ExcelProperty(value = "报检类型", index = 15)
        private String inspction_type;

        /**
         * 仓库编码
         */
        @ExcelProperty(value = "仓库编码", index = 16)
        private String warehouse_code;

        /**
         * 备注
         */
        @ExcelProperty(value = "备注", index = 17)
        private String note_S;

        public String getBill_no() {
            return bill_no;
        }

        public void setBill_no(String bill_no) {
            this.bill_no = bill_no;
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

        public String getArrivalDate() {
            return arrivalDate;
        }

        public void setArrivalDate(String arrivalDate) {
            this.arrivalDate = arrivalDate;
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

        public String getTrade_country() {
            return trade_country;
        }

        public void setTrade_country(String trade_country) {
            this.trade_country = trade_country;
        }

        public String getQiyun_port() {
            return qiyun_port;
        }

        public void setQiyun_port(String qiyun_port) {
            this.qiyun_port = qiyun_port;
        }

        public String getImport_export_port() {
            return import_export_port;
        }

        public void setImport_export_port(String import_export_port) {
            this.import_export_port = import_export_port;
        }

        public String getTraf_name() {
            return traf_name;
        }

        public void setTraf_name(String traf_name) {
            this.traf_name = traf_name;
        }

        public String getGround_agent() {
            return ground_agent;
        }

        public void setGround_agent(String ground_agent) {
            this.ground_agent = ground_agent;
        }

        public String getIsPersonal() {
            return isPersonal;
        }

        public void setIsPersonal(String isPersonal) {
            this.isPersonal = isPersonal;
        }

        public String getOne_to_bottom() {
            return one_to_bottom;
        }

        public void setOne_to_bottom(String one_to_bottom) {
            this.one_to_bottom = one_to_bottom;
        }

        public String getInspction_type() {
            return inspction_type;
        }

        public void setInspction_type(String inspction_type) {
            this.inspction_type = inspction_type;
        }

        public String getWarehouse_code() {
            return warehouse_code;
        }

        public void setWarehouse_code(String warehouse_code) {
            this.warehouse_code = warehouse_code;
        }

        public String getNote_S() {
            return note_S;
        }

        public void setNote_S(String note_S) {
            this.note_S = note_S;
        }
    }

    class PartyData {

        private String id;
        private String main_bill_NO;//主单号
        /**
         * 分单号
         */
        @ExcelProperty(value = "分单号", index = 0)
        private String bill_NO;
        /**
         * 重量
         */
        @ExcelProperty(value = "重量", index = 1)
        private String weight;
        /**
         * 净重
         */
        @ExcelProperty(value = "净重", index = 2)
        private String netWeight;

        @ExcelProperty(value = "发货人姓名", index = 3)
        private String send_person_name;


        /**
         * 证件号码
         */
        @ExcelProperty(value = "证件号码", index = 5)
        private String id_NO;
        /**
         * 收件人姓名
         */
        @ExcelProperty(value = "收件人姓名", index = 4)
        private String receive_name;
        /**
         * 收件人电话
         */
        @ExcelProperty(value = "收件人电话", index = 7)
        private String receive_tel;
        /**
         * // 收件人地址
         */
        @ExcelProperty(value = "收件人地址", index = 6)
        private String receive_address;

        /**
         * 收件人姓名(申报)
         */
        @ExcelProperty(value = "收件人姓名(申报)", index = 8)
        private String receive_name_declare;
        /**
         * 收件人电话(申报)
         */
        @ExcelProperty(value = "收件人电话(申报)", index = 10)
        private String receive_tel_declare;
        /**
         * 收件人地址(申报)
         */
        @ExcelProperty(value = "收件人地址(申报)", index = 9)
        private String receive_address_declare;


        @ExcelProperty(value = "城市", index = 10)
        private String city;

        @ExcelProperty(value = "邮编", index = 11)
        private String zipCode;

        @ExcelProperty(value = "快递类型", index = 12)
        private String expressType;

        @ExcelProperty(value = "快递单号", index = 13)
        private String expressNo;

        @ExcelProperty(value = "快递客户单号", index = 14)
        private String expressCustNo;

        @ExcelProperty(value = "转运公司", index = 15)
        private String transportCompany;

        @ExcelProperty(value = "网站", index = 16)
        private String webSite;

        /**
         * 报关类别
         */
        @ExcelProperty(value = "报关类别", index = 17)
        private String customs_category;

        @ExcelProperty(value = "保价", index = 18)
        private String insuredPrice;

        @ExcelProperty(value = "RFIDCode", index = 19)
        private String RFIDCode;

        @ExcelProperty(value = "溢装原主单号", index = 20)
        private String overloadOldMainNo;

        @ExcelProperty(value = "报检状态", index = 21)
        private String inspectionStatus;

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

        public String getSend_person_name() {
            return send_person_name;
        }

        public void setSend_person_name(String send_person_name) {
            this.send_person_name = send_person_name;
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
    }

    class ProductData {

        private String id;

        @ExcelProperty(value = "分单单号", index = 0)
        private String party_No;

        @ExcelProperty(value = "商品编码", index = 1)
        private String code_ts;

        @ExcelProperty(value = "物品名称", index = 2)
        private String g_name;

        @ExcelProperty(value = "申报单价", index = 3)
        private String declPrice;

        @ExcelProperty(value = "数量", index = 4)
        private String g_qty;

        @ExcelProperty(value = "申报实价", index = 5)
        private String trade_total;

        @ExcelProperty(value = "商品ID", index = 7)
        private String g_id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getParty_No() {
            return party_No;
        }

        public void setParty_No(String party_No) {
            this.party_No = party_No;
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

        public String getDeclPrice() {
            return declPrice;
        }

        public void setDeclPrice(String declPrice) {
            this.declPrice = declPrice;
        }

        public String getG_qty() {
            return g_qty;
        }

        public void setG_qty(String g_qty) {
            this.g_qty = g_qty;
        }

        public String getTrade_total() {
            return trade_total;
        }

        public void setTrade_total(String trade_total) {
            this.trade_total = trade_total;
        }

        public String getG_id() {
            return g_id;
        }

        public void setG_id(String g_id) {
            this.g_id = g_id;
        }
    }
}
