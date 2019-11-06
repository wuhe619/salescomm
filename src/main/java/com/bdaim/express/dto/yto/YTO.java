package com.bdaim.express.dto.yto;

import com.bdaim.express.dto.ExpressOrderData;

public class YTO {
    private String clientID;//商家代码
    private String logisticProviderID;//物流公司id
    private String customerId;//商家代码

    private String txLogisticID;//物流订单号(clientID +数字)
    private String tradeNo;//渠道名称
    private int orderType;//订单类型(0-COD,1-普通订单,2-便携式订单,3-退货单,4-到付)
    private long serviceType = 0;//服务类型(1-上门揽收, 0-自己联系)。默认为0
    private int flag = 0;
    private YTOSender sender;//发件人信息
    private YTOReceiver receiver;//收件人信息

    public YTO() {
    }

    public YTO(ExpressOrderData data) {
        this.clientID = "K21000119";
        this.logisticProviderID = "YTO";
        this.customerId = this.clientID;
        this.txLogisticID = data.getTxLogisticID();
        this.tradeNo = this.customerId;
        this.orderType = data.getOrderType();
        this.serviceType = data.getServiceType();
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getLogisticProviderID() {
        return logisticProviderID;
    }

    public void setLogisticProviderID(String logisticProviderID) {
        this.logisticProviderID = logisticProviderID;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTxLogisticID() {
        return txLogisticID;
    }

    public void setTxLogisticID(String txLogisticID) {
        this.txLogisticID = txLogisticID;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public long getServiceType() {
        return serviceType;
    }

    public void setServiceType(long serviceType) {
        this.serviceType = serviceType;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public YTOSender getSender() {
        return sender;
    }

    public void setSender(YTOSender sender) {
        this.sender = sender;
    }

    public YTOReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(YTOReceiver receiver) {
        this.receiver = receiver;
    }

}
