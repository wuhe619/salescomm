package com.bdaim.express.dto;


import java.util.List;

public class ExpressOrderData {

    private int tradeNoType;//渠道类型,0圆通,1中通
    private String txLogisticID;//物流订单号(clientID +数字)
    private int orderType;//订单类型(0-COD,1-普通订单,2-便携式订单,3-退货单,4-到付)
    private long serviceType = 0;//服务类型(1-上门揽收, 0-自己联系)。默认为0
    private int flag = 0;
    private Sender sender;//发件人信息
    private Receiver receiver;//收件人信息

    private List<Items> itemList;


    private String mailNo;//运单号

    public String getMailNo() {
        return mailNo;
    }

    public void setMailNo(String mailNo) {
        this.mailNo = mailNo;
    }

    public List<Items> getItemList() {
        return itemList;
    }

    public void setItemList(List<Items> itemList) {
        this.itemList = itemList;
    }


    public int getTradeNoType() {
        return tradeNoType;
    }

    public void setTradeNoType(int tradeNoType) {
        this.tradeNoType = tradeNoType;
    }

    public String getTxLogisticID() {
        return txLogisticID;
    }

    public void setTxLogisticID(String txLogisticID) {
        this.txLogisticID = txLogisticID;
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

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }


}
