package com.bdaim.station.dto;
import java.sql.Timestamp;

/**
 * @author duanliying
 * @date 2019/9/16
 * @description 场站信息
 */
public class StationDto {
    private Integer id;
    private String name;
    private String province;
    private String city;
    private String portCode;
    private String status;
    private Integer createId;
    private Timestamp createTime;
    private String operator;
    private String operatorMobile;
    //0：新增  1 修改  2 修改状态
    private Integer optType;
    private Integer pageNum;
    private Integer pageSize;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getOptType() {
        return optType;
    }

    public void setOptType(Integer optType) {
        this.optType = optType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getPortCode() {
        return portCode;
    }

    public void setPortCode(String portCode) {
        this.portCode = portCode;
    }

    public Integer getCreateId() {
        return createId;
    }

    public void setCreateId(Integer createId) {
        this.createId = createId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getOperator() {
        return operator;
    }

    public String getOperatorMobile() {
        return operatorMobile;
    }

    public void setOperatorMobile(String operatorMobile) {
        this.operatorMobile = operatorMobile;
    }

    @Override
    public String toString() {
        return "StationDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", portCode='" + portCode + '\'' +
                ", status='" + status + '\'' +
                ", createId=" + createId +
                ", createTime=" + createTime +
                ", operator='" + operator + '\'' +
                ", operatorMobile='" + operatorMobile + '\'' +
                ", optType=" + optType +
                '}';
    }
}
