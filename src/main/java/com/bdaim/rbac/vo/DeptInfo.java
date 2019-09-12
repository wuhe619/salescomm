package com.bdaim.rbac.vo;


/**
 * 该类用作界面展示信息的载体
 * @author lin.han
 * 2015-3-23
 * 上午11:01:39
 */
public class DeptInfo {
	
	private Long id;
	
	private String name;
	
	private int roleNum;
	
	private int userNum;
	
	private String source;
	
	private String modifyTime;
	
	public DeptInfo(){
		
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

	public int getRoleNum() {
		return roleNum;
	}

	public void setRoleNum(int roleNum) {
		this.roleNum = roleNum;
	}

	public int getUserNum() {
		return userNum;
	}

	public void setUserNum(int userNum) {
		this.userNum = userNum;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}

}
