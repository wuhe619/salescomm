package com.bdaim.crm.erp.admin.entity.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseAdminDept<M extends BaseAdminDept<M>> extends Model<M> implements IBean {

	public void setDeptId(Integer deptId) {
		set("dept_id", deptId);
	}

	public Integer getDeptId() {
		return getInt("dept_id");
	}

	public void setPid(Integer pid) {
		set("pid", pid);
	}

	public Integer getPid() {
		return getInt("pid");
	}

	public void setName(String name) {
		set("name", name);
	}

	public String getName() {
		return getStr("name");
	}

	public void setNum(Integer num) {
		set("num", num);
	}

	public Integer getNum() {
		return getInt("num");
	}

	public void setRemark(String remark) {
		set("remark", remark);
	}

	public String getRemark() {
		return getStr("remark");
	}

}
