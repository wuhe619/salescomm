package com.bdaim.batch.entity;

/**
 * 批次下客戶信息查询
 *
 * @author duanliying
 * @date 2018/9/7
 * @description
 */
public class BatchDetailDTO {
    private String id;
    private String idCard;
    private String batchid;
    private Long userId;
    private Integer allocation;

    public BatchDetailDTO() {
    }

    public BatchDetailDTO(String id, String batchid, Long userId, Integer allocation) {
        this.id = id;
        this.batchid = batchid;
        this.userId = userId;
        this.allocation = allocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchid() {
        return batchid;
    }

    public void setBatchid(String batchid) {
        this.batchid = batchid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAllocation() {
        return allocation;
    }

    public void setAllocation(Integer allocation) {
        this.allocation = allocation;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }


    @Override
    public String toString() {
        return "BatchDetailDTO{" +
                "id='" + id + '\'' +
                ", idCard='" + idCard + '\'' +
                ", batchid='" + batchid + '\'' +
                ", userId=" + userId +
                ", allocation=" + allocation +
                '}';
    }
}
