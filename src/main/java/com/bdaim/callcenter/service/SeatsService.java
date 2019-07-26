package com.bdaim.callcenter.service;

import com.bdaim.callcenter.dto.SeatsInfo;
import com.bdaim.callcenter.dto.SeatsMessageParam;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.page.Page;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.CustomerUserProperty;

import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2018/9/19
 * @description
 */
public interface SeatsService {
    /**
     * @description 修改坐席主信息
     * @author:duanliying
     * @method
     * @date: 2018/9/19 17:57
     */
    void updateMainMessage(List<SeatsMessageParam> channelsList) throws Exception;

    /**
     * @description 修改坐席登陆账户有效状态
     * @author:duanliying
     * @method
     * @date: 2018/9/21 10:51
     */
    void updateSeatsStatus(String id, int status, String channel, String custId) throws Exception;

    /**
     * @description 单独添加坐席信息
     * @author:duanliying
     * @method
     * @date: 2018/9/27 13:44
     */
    int updateSeatMessage(SeatsInfo seatsInfo);

    /**
     * @description 修改平台信息
     * @author:duanliying
     * @method
     * @date: 2018/9/27 14:50
     */
    String updatePlatformMessage(SeatsInfo seatsInfo);

    /**
     * 获取企业坐席列表
     *
     * @param page
     * @param customerRegistDTO
     * @return
     */
    Page getCustomerInfo(PageParam page, CustomerRegistDTO customerRegistDTO);

    /**
     * @description 获取坐席列表集合信息
     * @author:duanliying
     * @method
     * @date: 2018/9/27 14:50
     */
    Map<String, Object> getSeatsMessage(String custId, Integer pageNum, Integer pageSize);

    /**
     * @description 查询批次详情属性列表
     * @author:duanliying
     * @method
     * @date: 2018/9/6 11:23
     */
    Object getChannelList(String custId);

    /**
     * @description 查询外显号码根据企业id和批次ID
     */
    Map getApparentNum(String batchId, String cust_id);

    /**
     * @description 为批次设置外显号
     * @author:duanliying
     * @date: 2018/10/24 14:52
     */
    void updateApparentNum(String batchId, String apparentNums) throws Exception;

    /**
     * @description 验证坐席账号和平台账号不可重复
     * @author:duanliying
     * @method
     * @date: 2018/11/2 14:02
     */
    int checkAccount(String account, String seatId);

    /**
     * 获取用户添加的所有自建属性
     *
     * @param userId
     * @return
     */
    List<CustomerUserProperty> getUserAllProperty(String userId);

    /**
     * @description 查询主叫号码（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/20 16:01
     */
    Map<String, String> getExtensionNum(String seatAccount, String cust_id, String channel);

    int updateCallerID(SeatsMessageParam seatsMessageParam, String custId);
}
