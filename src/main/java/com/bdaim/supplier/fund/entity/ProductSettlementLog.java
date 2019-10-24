package com.bdaim.supplier.fund.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author duanliying
 * @date 2019/6/11
 * @description 结算记录表
 */

@Entity
@Table(name = "t_product_settlement_log")
public class ProductSettlementLog {
    private Integer id;
    private String objId;//结算对象id（推广和机构）
    private String productType;//产品类型
    private String product;//产品结算信息
    private Integer regeditNum;//注册数量
    private Integer activeNum;//激活数
    private Integer firstgetNum;//首提数
    private Double commission;//佣金
    private String attachmentPath;//附件路径
    private String remark;//备注
    private String settlementCycle;//结算周期
    private String createUser;//录入人
    private Timestamp createTime;//录入时间
    private String settlementObj;//结算方  1：机构  2：推广活动
    private String type;//录入类型  1 首次录入  2 扣量录入

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "obj_id")
    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }
    @Basic
    @Column(name = "type")

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "product")
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Basic
    @Column(name = "product_type")
    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    @Basic
    @Column(name = "regedit_num")
    public Integer getRegeditNum() {
        return regeditNum;
    }

    public void setRegeditNum(Integer regeditNum) {
        this.regeditNum = regeditNum;
    }

    @Basic
    @Column(name = "active_num")
    public Integer getActiveNum() {
        return activeNum;
    }

    public void setActiveNum(Integer activeNum) {
        this.activeNum = activeNum;
    }

    @Basic
    @Column(name = "firstget_num")
    public Integer getFirstgetNum() {
        return firstgetNum;
    }


    public void setFirstgetNum(Integer firstgetNum) {
        this.firstgetNum = firstgetNum;
    }

    @Basic
    @Column(name = "commission")
    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    @Basic
    @Column(name = "attachment_path")
    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "settlement_cycle")
    public String getSettlementCycle() {
        return settlementCycle;
    }

    public void setSettlementCycle(String settlementCycle) {
        this.settlementCycle = settlementCycle;
    }

    @Basic
    @Column(name = "create_user")
    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }


    @Basic
    @Column(name = "settlement_obj")
    public String getSettlementObj() {
        return settlementObj;
    }

    public void setSettlementObj(String settlementObj) {
        this.settlementObj = settlementObj;
    }

    @Override
    public String toString() {
        return "ProductSettlementLog{" +
                "id=" + id +
                ", objId='" + objId + '\'' +
                ", productType='" + productType + '\'' +
                ", product='" + product + '\'' +
                ", regeditNum=" + regeditNum +
                ", activeNum=" + activeNum +
                ", firstgetNum=" + firstgetNum +
                ", commission=" + commission +
                ", attachmentPath='" + attachmentPath + '\'' +
                ", remark='" + remark + '\'' +
                ", settlementCycle='" + settlementCycle + '\'' +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", settlementObj='" + settlementObj + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

