package com.bdaim.api.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "am_api")
public class ApiEntity implements Serializable {
    public static final Integer API_RELEASE = 2;//发布
    public static final Integer API_OFFLINE = 1;//下线
    public static final Integer API_FOUND = 0;//创建
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "API_ID")
    private int apiId;
    @Column(name = "API_PROVIDER")
    private String provider;
    @Column(name = "API_NAME")
    private String name;
    @Column(name = "API_VERSION")
    private String version;
    @Column(name = "CONTEXT")
    private String context;
    @Column(name = "CONTEXT_TEMPLATE")
    private String contextTexplate;
    @Column(name = "CREATED_BY")
    private String createBy;
    @Column(name = "CREATED_TIME")
    private Date createTime;
    @Column(name = "UPDATED_BY")
    private String updateBy;
    @Column(name = "UPDATED_TIME")
    private Date updateTime;
    @Column(name = "LIST_PRICE")
    private Integer listPrice;
    @Column(name = "TELECO_PRICE")
    private Integer telecoPrice;

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContextTexplate() {
        return contextTexplate;
    }

    public void setContextTexplate(String contextTexplate) {
        this.contextTexplate = contextTexplate;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getListPrice() {
        return listPrice;
    }

    public void setListPrice(Integer listPrice) {
        this.listPrice = listPrice;
    }

    public Integer getTelecoPrice() {
        return telecoPrice;
    }

    public void setTelecoPrice(Integer telecoPrice) {
        this.telecoPrice = telecoPrice;
    }

}
