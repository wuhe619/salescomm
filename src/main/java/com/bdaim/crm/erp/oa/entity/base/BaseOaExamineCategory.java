package com.bdaim.crm.erp.oa.entity.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOaExamineCategory<M extends BaseOaExamineCategory<M>> extends Model<M> implements IBean {

	public void setCategoryId(Integer categoryId) {
		set("category_id", categoryId);
	}

	public Integer getCategoryId() {
		return getInt("category_id");
	}

	public void setTitle(String title) {
		set("title", title);
	}

	public String getTitle() {
		return getStr("title");
	}

	public void setRemarks(String remarks) {
		set("remarks", remarks);
	}

	public String getRemarks() {
		return getStr("remarks");
	}

	public void setCreateUserId(Integer createUserId) {
		set("create_user_id", createUserId);
	}

	public Integer getCreateUserId() {
		return getInt("create_user_id");
	}

	public void setType(Integer type) {
		set("type", type);
	}

	public Integer getType() {
		return getInt("type");
	}

	public void setStatus(Integer status) {
		set("status", status);
	}

	public Integer getStatus() {
		return getInt("status");
	}

	public void setIsSys(Integer isSys) {
		set("is_sys", isSys);
	}

	public Integer getIsSys() {
		return getInt("is_sys");
	}

	public void setExamineType(Integer examineType) {
		set("examine_type", examineType);
	}

	public Integer getExamineType() {
		return getInt("examine_type");
	}

	public void setUserIds(String userIds) {
		set("user_ids", userIds);
	}

	public String getUserIds() {
		return getStr("user_ids");
	}

	public void setDeptIds(String deptIds) {
		set("dept_ids", deptIds);
	}

	public String getDeptIds() {
		return getStr("dept_ids");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
	}

	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public void setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
	}

	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

	public void setIsDeleted(Integer isDeleted) {
		set("is_deleted", isDeleted);
	}

	public Integer getIsDeleted() {
		return getInt("is_deleted");
	}

	public void setDeleteTime(java.util.Date deleteTime) {
		set("delete_time", deleteTime);
	}

	public java.util.Date getDeleteTime() {
		return get("delete_time");
	}

	public void setDeleteUserId(Integer deleteUserId) {
		set("delete_user_id", deleteUserId);
	}

	public Integer getDeleteUserId() {
		return getInt("delete_user_id");
	}


}
