package com.bdaim.crm;

/**
 * 商标信息
 */
public class TrademarkEntity {

    /**
     * 商标图片地址
     */
    private String trademarkPic;

    /**
     * 商标名称
     */
    private String name;

    /**
     * 注册号
     */
    private String regNo;

    /**
     * 国际分类
     */
    private String classify;

    /**
     * 申请时间
     */

    private Long applyTime;

    /**
     * 申请人
     */
    private String applicant;

    /**
     * 商标类型
     */
    private String type;

    /**
     * 商标状态
     */
    private String status;

    public String getTrademarkPic() {
        return trademarkPic;
    }

    public void setTrademarkPic(String trademarkPic) {
        this.trademarkPic = trademarkPic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public Long getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Long applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TrademarkInfo{" +
                "trademarkPic='" + trademarkPic + '\'' +
                ", name='" + name + '\'' +
                ", regNo='" + regNo + '\'' +
                ", classify='" + classify + '\'' +
                ", applyTime=" + applyTime +
                ", applicant='" + applicant + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
