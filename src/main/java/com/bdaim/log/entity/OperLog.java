package com.bdaim.log.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_operlog")
public class OperLog 
{
	@Id
//	@SequenceGenerator(name="T_OPERLOG_SEQ", sequenceName="T_OPERLOG_SEQ")
//	@GeneratedValue(strategy = GenerationType., generator="T_OPERLOG_SEQ")
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	@Column
	long oper_uid;
	@Column
	String oper_uname;
	@Column
	String oper_source_ip;
	@Column
	int oper_source_port;
	@Column
	String oper_target_ip;
	@Column
	int oper_target_port;
	@Column
	String oper_uri;
	@Column
	String oper_project;
	@Column
	String oper_page_name;
	@Column
	Date oper_datetime = new Date();
	@Column
	int oper_object_id = -1;
	@Transient
	long page_count;
	@Transient
	double page_count_percent;
	//根当前这个日志记录关联的对象名称，例如标签名称
	@Transient
	String object_name;
	//跟当前这个日志记录关联的对象数量，例如标签数量
	@Transient
	long object_count;
	@Transient
	long visit_count;
	
	

	public long getVisit_count() {
		return visit_count;
	}
	public void setVisit_count(long visit_count) {
		this.visit_count = visit_count;
	}
	public void setPage_count(long page_count) {
		this.page_count = page_count;
	}
	public void setObject_count(long object_count) {
		this.object_count = object_count;
	}
	public long getObject_count() {
		return object_count;
	}
	public void setObject_count(int object_count) {
		this.object_count = object_count;
	}
	public String getObject_name() {
		return object_name;
	}
	public void setObject_name(String object_name) {
		this.object_name = object_name;
	}
	public double getPage_count_percent() {
		return page_count_percent;
	}
	public void setPage_count_percent(double page_count_percent) {
		this.page_count_percent = page_count_percent;
	}
	public long getPage_count() {
		return page_count;
	}
	public void setPage_count(int page_count) {
		this.page_count = page_count;
	}
	public OperLog(int objectid)
	{
		this.setOper_object_id(objectid);
	}
	public OperLog()
	{
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getOper_uid() {
		return oper_uid;
	}
	public void setOper_uid(long oper_uid) {
		this.oper_uid = oper_uid;
	}
	public String getOper_uname() {
		return oper_uname;
	}
	public void setOper_uname(String oper_uname) {
		this.oper_uname = oper_uname;
	}
	public String getOper_source_ip() {
		return oper_source_ip;
	}
	public void setOper_source_ip(String oper_source_ip) {
		this.oper_source_ip = oper_source_ip;
	}
	public int getOper_source_port() {
		return oper_source_port;
	}
	public void setOper_source_port(int oper_source_port) {
		this.oper_source_port = oper_source_port;
	}
	public String getOper_target_ip() {
		return oper_target_ip;
	}
	public void setOper_target_ip(String oper_target_ip) {
		this.oper_target_ip = oper_target_ip;
	}
	public int getOper_target_port() {
		return oper_target_port;
	}
	public void setOper_target_port(int oper_target_port) {
		this.oper_target_port = oper_target_port;
	}
	public String getOper_uri() {
		return oper_uri;
	}
	public void setOper_uri(String oper_uri) {
		this.oper_uri = oper_uri;
	}
	public String getOper_project() {
		return oper_project;
	}
	public void setOper_project(String oper_project) {
		this.oper_project = oper_project;
	}
	public Date getOper_datetime() {
		return oper_datetime;
	}
	public void setOper_datetime(Date oper_datetime) {
		this.oper_datetime = oper_datetime;
	}
	public int getOper_object_id() {
		return oper_object_id;
	}
	public void setOper_object_id(int oper_object_id) {
		this.oper_object_id = oper_object_id;
	}
	public String getOper_page_name() {
		return oper_page_name;
	}
	public void setOper_page_name(String oper_page_name) {
		this.oper_page_name = oper_page_name;
	}

	
}
