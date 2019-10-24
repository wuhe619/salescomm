package com.bdaim.customs.entity;


import javax.persistence.*;

import com.bdaim.util.StringUtil;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="h_meta_data_def")
public class HMetaDataDef implements Serializable {


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(name="type")
	private String type;
	@Column(name="property_name")
	private String property_name;
	@Column(name="property_name_en")
	private String property_name_en;
	@Column(name="property_code")
	private String property_code;
	@Column(name="filed_type")
	private String filed_type;
	@Column(name="value_range")
	private String value_range;
	@Column(name="default_value")
	private String default_value;

	@Column(name="business_rule")
	private String business_rule;

	@Column(name="insertdb_rule")
	private String insertdb_rule;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProperty_name() {
		return property_name;
	}

	public void setProperty_name(String property_name) {
		this.property_name = property_name;
	}

	public String getProperty_name_en() {
		return property_name_en;
	}

	public void setProperty_name_en(String property_name_en) {
		this.property_name_en = property_name_en;
	}

	public String getProperty_code() {
		return property_code;
	}

	public void setProperty_code(String property_code) {
		this.property_code = property_code;
	}

	public String getFiled_type() {
		return filed_type;
	}

	public void setFiled_type(String filed_type) {
		this.filed_type = filed_type;
	}

	public String getValue_range() {
		return value_range;
	}

	public void setValue_range(String value_range) {
		this.value_range = value_range;
	}

	public String getDefault_value() {
		return default_value;
	}

	public void setDefault_value(String default_value) {
		this.default_value = default_value;
	}

	public String getBusiness_rule() {
		return business_rule;
	}

	public void setBusiness_rule(String business_rule) {
		this.business_rule = business_rule;
	}

	public String getInsertdb_rule() {
		return insertdb_rule;
	}

	public void setInsertdb_rule(String insertdb_rule) {
		this.insertdb_rule = insertdb_rule;
	}

	public static String getTable(String type,String time){
		StringBuffer table_name = new StringBuffer("h_data_manager");
		if(StringUtil.isNotEmpty(type)){
			table_name.append("_").append(type);
		}
		if(StringUtil.isNotEmpty(time)){
			table_name.append("_").append(time);
		}
		return table_name.toString();
	}
}
