package com.bdaim.express.dto.zto;

import com.bdaim.express.dto.ExpressOrderData;

public class ZTO {
    private String id = "xfs101100111011";//订单号
    private String typeid = "1";//1为电子面单
    private ZTOSender sender;
    private ZTOReceiver receiver;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeid() {
        return typeid;
    }

    public void setTypeid(String typeid) {
        this.typeid = typeid;
    }

    public ZTOSender getSender() {
        return sender;
    }

    public void setSender(ZTOSender sender) {
        this.sender = sender;
    }

    public ZTOReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(ZTOReceiver receiver) {
        this.receiver = receiver;
    }

    public ZTO() {
    }

}
