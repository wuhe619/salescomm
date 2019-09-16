package com.bdaim.customs.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="h_receipt_record")
public class HReceiptRecord implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(name="main_no")
	private String main_no;
	@Column(name="part_no")
	private String part_no;
	@Column(name="type")
	private String type;
	@Column(name="customs_code")
	private String customs_code;
	@Column(name="content")
	private String content;
	@Column(name="status")
	private String status;
	@Column(name="create_time")
	private Date create_time;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMain_no() {
		return main_no;
	}

	public void setMain_no(String main_no) {
		this.main_no = main_no;
	}

	public String getPart_no() {
		return part_no;
	}

	public void setPart_no(String part_no) {
		this.part_no = part_no;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCustoms_code() {
		return customs_code;
	}

	public void setCustoms_code(String customs_code) {
		this.customs_code = customs_code;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
}
