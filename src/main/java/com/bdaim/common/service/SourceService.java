package com.bdaim.common.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 */
public interface SourceService {
	
	/**
	 * 数据源概览
	 * @return
	 */
	public String listDataSource();
	
	/**
	 * 数据源状态修改
	 * @param sourceId
	 * @param status
	 * @return
	 */
	public String updateSourceStatus(Integer sourceId, Integer status);

	/**
	 * 查询数据源一级标签树
	 * @param id
	 * @param status
	 * @return
	 */
	public String listLabelsByCondition(String id, String status);

	/**
	 * 查询数据源子标签树
	 * @param id
	 * @param status
	 * @return
	 */
	public String listLabelsChildrenById(String id, String status);

	/**
	 * 查询数据源标签
	 * @param sourceId 数据源Id
	 * @param topCategory 一级标签分类Id
	 * @param secondCategory 二级标签分类Id
	 * @param labelName 标签名称
	 * @param labelId 标签Id
	 * @param createTimeStart 创建时间（开始）
	 * @param createTimeEnd 创建时间（结束）
	 * @param pageNum 页码
	 * @param pageSize 条数
	 * @return
	 */
	public String listSourceLabelsByCondition(JSONObject json);

	/**
	 * 设置成本价
	 * @param price
	 * @param priceId
	 * @return
	 */
	public String updateLabelSourcePrice(Double price, Integer priceId, String operator);

	/**
	 * 批量设置成本价
	 * @param price
	 * @param priceId
	 * @return
	 */
	public String updateLabelSourcePriceBatch(String state, Double price, String[] idList, String operator);

	/**
	 * 查询成本价定价记录
	 * @param priceId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public String listLabelSourcePriceLog(Integer priceId, Integer labelId, Integer pageNum, Integer pageSize);

	/**
	 * 获取数据源 一级菜单  二级菜单
	 * @param state
	 * @param id
	 * @return
	 */
	public String listLabelList(String state, String id);
	
	
	
}
