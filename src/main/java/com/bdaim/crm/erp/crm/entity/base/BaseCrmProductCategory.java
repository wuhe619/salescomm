package com.bdaim.crm.erp.crm.entity.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseCrmProductCategory<M extends BaseCrmProductCategory<M>> extends Model<M> implements IBean {

	public void setCategoryId(Integer categoryId) {
		set("category_id", categoryId);
	}

	public Integer getCategoryId() {
		return getInt("category_id");
	}

	public void setName(String name) {
		set("name", name);
	}

	public String getName() {
		return getStr("name");
	}

	public void setPid(Integer pid) {
		set("pid", pid);
	}

	public Integer getPid() {
		return getInt("pid");
	}

}
