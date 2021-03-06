package com.bdaim.crm.erp.admin.entity.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseAdminExamineStep<M extends BaseAdminExamineStep<M>> extends Model<M> implements IBean {

	public M setStepId(Long stepId) {
		set("step_id", stepId);
		return (M)this;
	}

	public Long getStepId() {
		return getLong("step_id");
	}

	public M setStepType(Integer stepType) {
		set("step_type", stepType);
		return (M)this;
	}

	public Integer getStepType() {
		return getInt("step_type");
	}

	public M setExamineId(Integer examineId) {
		set("examine_id", examineId);
		return (M)this;
	}

	public Integer getExamineId() {
		return getInt("examine_id");
	}

	public M setCheckUserId(String checkUserId) {
		set("check_user_id", checkUserId);
		return (M)this;
	}

	public String getCheckUserId() {
		return getStr("check_user_id");
	}

	public M setStepNum(Integer stepNum) {
		set("step_num", stepNum);
		return (M)this;
	}

	public Integer getStepNum() {
		return getInt("step_num");
	}

	public M setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
		return (M)this;
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public M setRemarks(String remarks) {
		set("remarks", remarks);
		return (M)this;
	}

	public String getRemarks() {
		return getStr("remarks");
	}

}
