package com.bdaim.rbac.vo;


import com.bdaim.common.dto.Page;

public class QueryDataParam {
	private Long userId;
	private Long deptId;
	private Long roleId;
	private String condition;
	private Page page;
	
	
	public QueryDataParam(){
		
	}

	public Long getDeptId() {
		return deptId;
	}


	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}


	public Long getRoleId() {
		return roleId;
	}


	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}


	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
