package com.bdaim.log.service;

import com.bdaim.common.dto.Page;
import com.bdaim.log.entity.OperLog;

import java.util.Date;
import java.util.List;

public interface OperLogService {
	//get top n count by type and date.
	public List<OperLog> getTopnObjectIdByDateAndType(String typeuri, int topn, Date date);
	
	//get top n count by page and date
	public List<OperLog> getTopnPageByDate(Date date1, Date date2, int topn);
	
	//get info by starttime, endtime, username, pagename, indexstart, indexend
	public List<OperLog> getOperLogInfo(OperLog entity, Page page, Date date1, Date date2, String order_field, String order_asc);
	public long getOperLogInfoTotalCount(OperLog ol, Date date1, Date date2);
	
	//add one operation log
	public int addOneOperLogInfo(OperLog entity);
	
}
