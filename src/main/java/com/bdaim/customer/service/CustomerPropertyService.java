package com.bdaim.customer.service;

import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.page.Page;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerPropertyParam;

import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/7
 * @description
 */
public interface CustomerPropertyService {

    int save(CustomerPropertyParam customerPropertyParam);

    int update(CustomerPropertyParam customerPropertyParam);
    /**
     * 分页接口
     * @param page
     * @param customerPropertyParam
     * @return
     */
    Page pageList(PageParam page, CustomerPropertyParam customerPropertyParam);

    /**
     * 查询已经选择的自建属性列表
     *
     * @author chengning@salescomm.net
     * @date 2018/9/10 9:22
     * @param superId
     * @param customerGroupId
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getSelectedLabelsBySuperId(String superId, String customerGroupId);

    String getListenterprise(String custId,String channel);


    void addenterprise(CustomerProperty customerProperty) throws Exception;

    /**
     * 查询企业渠道信息
     */
    List<Map<String,Object>> listCustSupplier(String custId);
}
