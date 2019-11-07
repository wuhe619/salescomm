package com.bdaim.express.dto.yto;

import com.bdaim.express.dto.Items;
import com.bdaim.express.dto.Receiver;
import com.bdaim.express.dto.Sender;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class) //返回xml格式时非必须，此注解是转json的蛇形命名用的
@XmlRootElement(name = "Response") // 定义xml根节点
@XmlType(propOrder = {"clientID", "code", "distributeInfo", "logisticProviderID", "mailNo", "originateOrgCode", "qrCode", "success", "txLogisticID"})
// 定义xml节点的顺序
public class ElectronOrderResponse {

    private String clientID;
    private int code;
    private String logisticProviderID;
    private String mailNo;
    private String originateOrgCode;
    private String qrCode;
    private String success;
    private String txLogisticID;
    private DistributeInfo distributeInfo;
    private YTOSender sender;
    private YTOReceiver receiver;
    private List<Items> itemsList;

    public YTOReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(YTOReceiver receiver) {
        this.receiver = receiver;
    }

    public YTOSender getSender() {

        return sender;
    }

    public void setSender(YTOSender sender) {
        this.sender = sender;
    }

    public List<Items> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<Items> itemsList) {
        this.itemsList = itemsList;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getLogisticProviderID() {
        return logisticProviderID;
    }

    public void setLogisticProviderID(String logisticProviderID) {
        this.logisticProviderID = logisticProviderID;
    }

    public String getMailNo() {
        return mailNo;
    }

    public void setMailNo(String mailNo) {
        this.mailNo = mailNo;
    }

    public String getOriginateOrgCode() {
        return originateOrgCode;
    }

    public void setOriginateOrgCode(String originateOrgCode) {
        this.originateOrgCode = originateOrgCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getTxLogisticID() {
        return txLogisticID;
    }

    public void setTxLogisticID(String txLogisticID) {
        this.txLogisticID = txLogisticID;
    }

    public DistributeInfo getDistributeInfo() {
        return distributeInfo;
    }

    public void setDistributeInfo(DistributeInfo distributeInfo) {
        this.distributeInfo = distributeInfo;
    }

    @Override
    public String toString() {
        return "YTOResponse{" +
                "clientID='" + clientID + '\'' +
                ", code=" + code +
                ", logisticProviderID='" + logisticProviderID + '\'' +
                ", mailNo='" + mailNo + '\'' +
                ", originateOrgCode='" + originateOrgCode + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", success='" + success + '\'' +
                ", txLogisticID='" + txLogisticID + '\'' +
                ", distributeInfo=" + distributeInfo +
                '}';
    }
}
