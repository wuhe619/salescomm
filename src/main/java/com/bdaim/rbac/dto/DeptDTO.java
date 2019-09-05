package com.bdaim.rbac.dto;

import java.util.Date;


/**
 * @author lin.han
 *
 */
public class DeptDTO implements Manager<Long>{
	
	private Long id;
	
	private String name;
	
	private String optuser;
	
	private Date createTime;
	
	private Date modifyTime;
	
	private int type;
	
	public DeptDTO(){
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOptuser() {
		return optuser;
	}

	public void setOptuser(String optuser) {
		this.optuser = optuser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	@Override
	public Long getKey() {
		return this.id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}


}
