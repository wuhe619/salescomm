package com.bdaim.crm.erp.admin.entity.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseAdminConfig<M extends BaseAdminConfig<M>> extends Model<M> implements IBean {

	public void setSettingId(Integer settingId) {
		set("setting_id", settingId);
	}

	public Integer getSettingId() {
		return getInt("setting_id");
	}

	public void setStatus(Integer status) {
		set("status", status);
	}

	public Integer getStatus() {
		return getInt("status");
	}

	public void setName(String name) {
		set("name", name);
	}

	public String getName() {
		return getStr("name");
	}

	public void setValue(String value) {
		set("value", value);
	}

	public String getValue() {
		return getStr("value");
	}

	public void setDescription(String description) {
		set("description", description);
	}

	public String getDescription() {
		return getStr("description");
	}

}
