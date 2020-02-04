package com.bdaim.crm.erp.admin.entity.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseAdminRoleMenu<M extends BaseAdminRoleMenu<M>> extends Model<M> implements IBean {

	public void setId(Integer id) {
		set("id", id);
	}

	public Integer getId() {
		return getInt("id");
	}

	public void setRoleId(Integer roleId) {
		set("role_id", roleId);
	}

	public Integer getRoleId() {
		return getInt("role_id");
	}

	public void setMenuId(Integer menuId) {
		set("menu_id", menuId);
	}

	public Integer getMenuId() {
		return getInt("menu_id");
	}

}
