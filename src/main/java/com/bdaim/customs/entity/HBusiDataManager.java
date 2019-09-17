package com.bdaim.customs.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="h_data_manager")
public class HBusiDataManager implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(name="type")
	private String type;
	@Column(name="content")
	private String content;
	@Column(name="create_id")
	private Long createId;
	@Column(name="crate_time")
	private Date createTime;
	@Column(name="cust_id")
	private Long cust_id;
	@Column(name="ext_date1")
	private Date ext_date1;
	@Column(name="ext_date2")
	private Date ext_date2;

	@Column(name="ext_1")
	private String ext_1;

	@Column(name="ext_2")
	private String ext_2;

	@Column(name="ext_3")
	private String ext_3;

	@Column(name="ext_4")
	private String ext_4;

	@Column(name="ext_5")
	private String ext_5;

	@Column(name="ext_6")
	private String ext_6;

	@Column(name="ext_7")
	private String ext_7;

	@Column(name="ext_8")
	private String ext_8;

	@Column(name="ext_9")
	private String ext_9;

	@Column(name="ext_10")
	private String ext_10;

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getCreateId() {
		return createId;
	}

	public void setCreateId(Long createId) {
		this.createId = createId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getCust_id() {
		return cust_id;
	}

	public void setCust_id(Long cust_id) {
		this.cust_id = cust_id;
	}

	public Date getExt_date1() {
		return ext_date1;
	}

	public void setExt_date1(Date ext_date1) {
		this.ext_date1 = ext_date1;
	}

	public Date getExt_date2() {
		return ext_date2;
	}

	public void setExt_date2(Date ext_date2) {
		this.ext_date2 = ext_date2;
	}

	public String getExt_1() {
		return ext_1;
	}

	public void setExt_1(String ext_1) {
		this.ext_1 = ext_1;
	}

	public String getExt_2() {
		return ext_2;
	}

	public void setExt_2(String ext_2) {
		this.ext_2 = ext_2;
	}

	public String getExt_3() {
		return ext_3;
	}

	public void setExt_3(String ext_3) {
		this.ext_3 = ext_3;
	}

	public String getExt_4() {
		return ext_4;
	}

	public void setExt_4(String ext_4) {
		this.ext_4 = ext_4;
	}

	public String getExt_5() {
		return ext_5;
	}

	public void setExt_5(String ext_5) {
		this.ext_5 = ext_5;
	}

	public String getExt_6() {
		return ext_6;
	}

	public void setExt_6(String ext_6) {
		this.ext_6 = ext_6;
	}

	public String getExt_7() {
		return ext_7;
	}

	public void setExt_7(String ext_7) {
		this.ext_7 = ext_7;
	}

	public String getExt_8() {
		return ext_8;
	}

	public void setExt_8(String ext_8) {
		this.ext_8 = ext_8;
	}

	public String getExt_9() {
		return ext_9;
	}

	public void setExt_9(String ext_9) {
		this.ext_9 = ext_9;
	}

	public String getExt_10() {
		return ext_10;
	}

	public void setExt_10(String ext_10) {
		this.ext_10 = ext_10;
	}
}
