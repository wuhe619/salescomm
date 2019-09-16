package com.bdaim.industry.dto;

import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class IndustryPoolDTO implements RowMapper<IndustryPoolDTO>, Serializable{

	 private static final long serialVersionUID = -8823504831198719837L;  
	  
	    private Integer industryPoolId;  
	  
	    private String name;  
	  
	    private Integer status; 
	    
	    private String description;  
	    
	    private String creator;
	    
	    private Timestamp createTime;
		  
	

		public Integer getIndustryPoolId() {
			return industryPoolId;
		}



		public void setIndustryPoolId(Integer industryPoolId) {
			this.industryPoolId = industryPoolId;
		}



		public String getName() {
			return name;
		}



		public void setName(String name) {
			this.name = name;
		}



		public Integer getStatus() {
			return status;
		}



		public void setStatus(Integer status) {
			this.status = status;
		}



		public String getDescription() {
			return description;
		}



		public void setDescription(String description) {
			this.description = description;
		}



		public String getCreator() {
			return creator;
		}



		public void setCreator(String creator) {
			this.creator = creator;
		}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public static long getSerialversionuid() {
			return serialVersionUID;
		}



		@Override  
	    public IndustryPoolDTO mapRow(ResultSet rs, int rowNum) throws SQLException {  
	    	IndustryPoolDTO industryPool = new IndustryPoolDTO(); 
	    	industryPool.setIndustryPoolId(rs.getInt("industryPoolId"));
	    	industryPool.setName(rs.getString("name")); 
	    	industryPool.setStatus(rs.getInt("status"));
	    	industryPool.setDescription(rs.getString("description")); 
	    	industryPool.setCreator(rs.getString("creator"));
	    	industryPool.setCreateTime(rs.getTimestamp("createTime"));
	        return industryPool;  
	    } 


}
