package com.bdaim.batch.service;

import com.alibaba.fastjson.JSONArray;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.page.Page;
import com.bdaim.slxf.entity.DetailQueryParam;

/**
 * @author duanliying
 * @date 2018/9/6
 * @description 查询批次下的客户
 */
public interface BatchDetaiService {

    /**
     * 根据批次ID查询批次下的客户集合(对外接口)
     *
     * @description
     * @author:duanliying
     * @method
     * @date: 2018/9/6 16:52
     */
    Page getDetailListById(PageParam page, String batchId, String custId);

    /**
     * @description 列表搜索--某批次被叫客户列表搜索
     * @author:duanliying
     * @method
     * @date: 2018/9/6 11:23
     */
    Page getDetailList(DetailQueryParam detailQueryParam, Long userId, String userType, JSONArray custProperty,String role);

    /**
     * @description 查询批次详情属性列表
     * @author:duanliying
     * @method
     * @date: 2018/9/6 11:23
     */
    Object getPropertyList(String custId);

}
