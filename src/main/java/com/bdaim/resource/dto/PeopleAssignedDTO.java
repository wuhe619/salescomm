package com.bdaim.resource.dto;

public class PeopleAssignedDTO {
	
	private String id;
	private Long  userId;
	private Integer custGroupId;
	private String userGroupId;
	public PeopleAssignedDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PeopleAssignedDTO(String id, Long userId, Integer custGroupId) {
		super();
		this.id = id;
		this.userId = userId;
		this.custGroupId = custGroupId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Integer getCustGroupId() {
		return custGroupId;
	}
	public void setCustGroupId(Integer custGroupId) {
		this.custGroupId = custGroupId;
	}

	public String getUserGroupId() {
		return userGroupId;
	}

	public void setUserGroupId(String userGroupId) {
		this.userGroupId = userGroupId;
	}

	@Override
	public String toString() {
		return "PeopleAssignedDTO{" +
				"id='" + id + '\'' +
				", userId=" + userId +
				", custGroupId=" + custGroupId +
				", userGroupId='" + userGroupId + '\'' +
				'}';
	}
}
