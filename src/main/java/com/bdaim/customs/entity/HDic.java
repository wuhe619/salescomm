package com.bdaim.customs.entity;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="h_dic")
public class HDic implements Serializable {

	@Column(name="type")
	private String type;
	@Column(name="code")
	private String code;
	@Column(name="name_zh")
	private String name_zh;
	@Column(name="name_en")
	private String name_en;
	@Column(name="desc")
	private String desc;
	@Column(name="status")
	private String status;
	@Column(name="p_code")
	private String p_code;

	@Column(name="ext_1")
	private String ext_1;

	@Column(name="ext_2")
	private String ext_2;

    @Column(name="ext_3")
    private String ext_3;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName_zh() {
        return name_zh;
    }

    public void setName_zh(String name_zh) {
        this.name_zh = name_zh;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getP_code() {
        return p_code;
    }

    public void setP_code(String p_code) {
        this.p_code = p_code;
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
}
