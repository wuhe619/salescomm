package com.bdaim.customs.entity;

import com.bdaim.customs.dto.StationDto;

import javax.persistence.*;
import java.sql.Timestamp;


@Entity
@Table(name = "h_station_info", schema = "", catalog = "")
public class Station {
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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "province")

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    @Basic
    @Column(name = "city")

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Basic
    @Column(name = "port_code")

    public String getPortCode() {
        return portCode;
    }

    public void setPortCode(String portCode) {
        this.portCode = portCode;
    }

    @Basic
    @Column(name = "status")

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "create_id")
    public Integer getCreateId() {
        return createId;
    }

    public void setCreateId(Integer createId) {
        this.createId = createId;
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
    @Column(name = "operator")


    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Basic
    @Column(name = "operator_mobile")


    public String getOperatorMobile() {
        return operatorMobile;
    }

    public void setOperatorMobile(String operatorMobile) {
        this.operatorMobile = operatorMobile;
    }

    public Station(StationDto dto) {
        if (dto.getId() != null) {
            this.id = dto.getId();
        }
        this.name = dto.getName();
        this.province = dto.getProvince();

        this.city = dto.getCity();
        this.portCode = dto.getPortCode();
        this.status = dto.getStatus();
        this.createId = dto.getCreateId();
        this.createTime = dto.getCreateTime();
        this.operator = dto.getOperator();
        this.operatorMobile = dto.getOperatorMobile();
    }

    public Station() {
    }

    @Override
    public String toString() {
        return "Station{" +
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
                '}';
    }
}
