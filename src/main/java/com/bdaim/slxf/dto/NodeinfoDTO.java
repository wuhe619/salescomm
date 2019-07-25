package com.bdaim.slxf.dto;

import java.io.Serializable;

/**
 * 基础DTO
 * 日期：2017/2/21
 * @author lich@bdcsdk.com
 *
 */
public class NodeinfoDTO  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String verCode;
	private long timestamp;
	private int num;
	
	public void setVerCode(String verCode) {
		this.verCode = verCode;
	}

	public void setTimeStamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getVerCode() { 
		return this.verCode;
	}
	
	public long getTimeStamp() {
		return this.timestamp;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
}
