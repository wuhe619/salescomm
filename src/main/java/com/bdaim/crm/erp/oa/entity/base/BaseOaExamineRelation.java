package com.bdaim.crm.erp.oa.entity.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOaExamineRelation<M extends BaseOaExamineRelation<M>> extends Model<M> implements IBean {

	public void setRId(Integer rId) {
		set("r_id", rId);
	}

	public Integer getRId() {
		return getInt("r_id");
	}

	public void setExamineId(Integer examineId) {
		set("examine_id", examineId);
	}

	public Integer getExamineId() {
		return getInt("examine_id");
	}

	public void setCustomerIds(String customerIds) {
		set("customer_ids", customerIds);
	}

	public String getCustomerIds() {
		return getStr("customer_ids");
	}

	public void setContactsIds(String contactsIds) {
		set("contacts_ids", contactsIds);
	}

	public String getContactsIds() {
		return getStr("contacts_ids");
	}

	public void setBusinessIds(String businessIds) {
		set("business_ids", businessIds);
	}

	public String getBusinessIds() {
		return getStr("business_ids");
	}

	public void setContractIds(String contractIds) {
		set("contract_ids", contractIds);
	}

	public String getContractIds() {
		return getStr("contract_ids");
	}

	public void setStatus(Integer status) {
		set("status", status);
	}

	public Integer getStatus() {
		return getInt("status");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

}
