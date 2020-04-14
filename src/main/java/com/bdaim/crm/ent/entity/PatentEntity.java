package com.bdaim.crm.ent.entity;

/**
 * 专利信息
 */
public class PatentEntity {

    /**
     * 专利名称
     */
    private String name;

    /**
     * 申请公布号
     */
    private String applyNo;

    /**
     * 专利类型(需要专利类型字典表)
     */
    private String type;

    /**
     * 法律状态
     */
    private String legalStatus;

    /**
     * 公布日期
     */
    private Long announceTime;

    /**
     * 公开公告号
     */
    private String publicNo;

    /**
     * 公开公告日期
     */
    private Long publicTime;

    /**
     * 发明人
     */
    private String inventor;

    /**
     * 专利申请人
     */
    private String applicant;

    /**
     * 专利代理人
     */
    private String agent;

    /**
     * 专利代理机构
     */
    private String agentAgency;

    /**
     * 主分类号
     */
    private String mainClassifyNo;

    /**
     * 住所
     */
    private String residence;

    /**
     * 摘要
     */
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplyNo() {
        return applyNo;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLegalStatus() {
        return legalStatus;
    }

    public void setLegalStatus(String legalStatus) {
        this.legalStatus = legalStatus;
    }

    public Long getAnnounceTime() {
        return announceTime;
    }

    public void setAnnounceTime(Long announceTime) {
        this.announceTime = announceTime;
    }

    public String getPublicNo() {
        return publicNo;
    }

    public void setPublicNo(String publicNo) {
        this.publicNo = publicNo;
    }

    public Long getPublicTime() {
        return publicTime;
    }

    public void setPublicTime(Long publicTime) {
        this.publicTime = publicTime;
    }

    public String getInventor() {
        return inventor;
    }

    public void setInventor(String inventor) {
        this.inventor = inventor;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getAgentAgency() {
        return agentAgency;
    }

    public void setAgentAgency(String agentAgency) {
        this.agentAgency = agentAgency;
    }

    public String getMainClassifyNo() {
        return mainClassifyNo;
    }

    public void setMainClassifyNo(String mainClassifyNo) {
        this.mainClassifyNo = mainClassifyNo;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "PatentInfo{" +
                "name='" + name + '\'' +
                ", applyNo='" + applyNo + '\'' +
                ", type='" + type + '\'' +
                ", legalStatus='" + legalStatus + '\'' +
                ", announceTime=" + announceTime +
                ", publicNo='" + publicNo + '\'' +
                ", publicTime=" + publicTime +
                ", inventor='" + inventor + '\'' +
                ", applicant='" + applicant + '\'' +
                ", agent='" + agent + '\'' +
                ", agentAgency='" + agentAgency + '\'' +
                ", mainClassifyNo='" + mainClassifyNo + '\'' +
                ", residence='" + residence + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
