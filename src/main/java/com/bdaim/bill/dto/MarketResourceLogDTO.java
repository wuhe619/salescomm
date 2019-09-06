package com.bdaim.bill.dto;

import java.io.Serializable;

/**
 * 
 */
public class MarketResourceLogDTO implements Serializable{
	/**
	 * id
	 */
	private String touch_id;
	/**
	 * 客户id
	 */
	private String cust_id;
	/**
	 * 用户id
	 */
	private Long user_id;
	/**
	 * '资源类型（1.voice 2.SMS 3.email）'
	 */
	private String type_code;
	/**
	 * 资源名称
	 */
	private String resname;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 创建时间
	 */
	private String create_time;
	/**
	 * 接听状态/发送结果(成功,失败)/
	 */
	private int status;
	/**
	 * 短信内容
	 */
	private String sms_content;
	/**
	 *邮件内容
	 */
	private String email_content;
	/**
	 * 数据唯一标识id
	 */
	private String superId;
	
	/**
	 * 数据唯一标识id
	 */
	private Integer templateId ;
	
	private String callSid;
	
	private String batchNumber;

    private Integer customerGroupId;

    private Integer callOwner;
	/**
	 * 职场ID
	 */
	private String cugId;

	/**
	 * 营销任务ID
	 */
	private String marketTaskId;

    /**
     * 公海ID
     */
	private String customerSeaId;

	public MarketResourceLogDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

    public MarketResourceLogDTO(String touch_id, String cust_id, Long user_id, String type_code, String resname, String remark,
                                String create_time, int status, String sms_content, String email_content, String superId, Integer templateId, String callSid, String batchNumber, Integer customerGroupId, Integer callOwner) {
        this.touch_id = touch_id;
        this.cust_id = cust_id;
        this.user_id = user_id;
        this.type_code = type_code;
        this.resname = resname;
        this.remark = remark;
        this.create_time = create_time;
        this.status = status;
        this.sms_content = sms_content;
        this.email_content = email_content;
        this.superId = superId;
        this.templateId = templateId;
        this.callSid = callSid;
        this.batchNumber = batchNumber;
        this.customerGroupId = customerGroupId;
        this.callOwner = callOwner;
    }

    public String getTouch_id() {
		return touch_id;
	}

	public void setTouch_id(String touch_id) {
		this.touch_id = touch_id;
	}

	public String getCust_id() {
		return cust_id;
	}

	public void setCust_id(String cust_id) {
		this.cust_id = cust_id;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public String getType_code() {
		return type_code;
	}

	public void setType_code(String type_code) {
		this.type_code = type_code;
	}

	public String getResname() {
		return resname;
	}

	public void setResname(String resname) {
		this.resname = resname;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getSms_content() {
		return sms_content;
	}

	public void setSms_content(String sms_content) {
		this.sms_content = sms_content;
	}

	public String getEmail_content() {
		return email_content;
	}

	public void setEmail_content(String email_content) {
		this.email_content = email_content;
	}

	public String getSuperId() {
		return superId;
	}

	public void setSuperId(String superId) {
		this.superId = superId;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public String getCallSid() {
		return callSid;
	}

	public void setCallSid(String callSid) {
		this.callSid = callSid;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

    public Integer getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Integer customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public Integer getCallOwner() {
        return callOwner;
    }

    public void setCallOwner(Integer callOwner) {
        this.callOwner = callOwner;
    }

	public String getCugId() {
		return cugId;
	}

	public void setCugId(String cugId) {
		this.cugId = cugId;
	}

	public String getMarketTaskId() {
		return marketTaskId;
	}

	public void setMarketTaskId(String marketTaskId) {
		this.marketTaskId = marketTaskId;
	}

    public String getCustomerSeaId() {
        return customerSeaId;
    }

    public void setCustomerSeaId(String customerSeaId) {
        this.customerSeaId = customerSeaId;
    }

    @Override
    public String toString() {
        return "MarketResourceLogDTO{" +
                "touch_id='" + touch_id + '\'' +
                ", cust_id='" + cust_id + '\'' +
                ", user_id=" + user_id +
                ", type_code='" + type_code + '\'' +
                ", resname='" + resname + '\'' +
                ", remark='" + remark + '\'' +
                ", create_time='" + create_time + '\'' +
                ", status=" + status +
                ", sms_content='" + sms_content + '\'' +
                ", email_content='" + email_content + '\'' +
                ", superId='" + superId + '\'' +
                ", templateId=" + templateId +
                ", callSid='" + callSid + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", customerGroupId=" + customerGroupId +
                ", callOwner=" + callOwner +
                ", cugId='" + cugId + '\'' +
                ", marketTaskId='" + marketTaskId + '\'' +
                ", customerSeaId='" + customerSeaId + '\'' +
                '}';
    }
}
