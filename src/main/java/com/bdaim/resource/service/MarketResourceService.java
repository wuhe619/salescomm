package com.bdaim.resource.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.page.Page;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.slxf.entity.*;
import com.bdaim.supplier.dto.SupplierListParam;
import com.bdaim.template.dto.TemplateParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author yanls@bdaim.com
 * @Description: 营销资源服务接口
 * @date 2018/9/10 9:31
 */
public interface MarketResourceService {
    /**
     * 短信历史查询
     */
    Page querySmsHistory(PageParam page, SmsqueryParam smsqueryParm);

    /**
     * 通话记录查询
     */
    Page queryRecordVoicelog(PageParam page, String cust_id, Long userid, String user_type, String superId,
                             String realName, String createTimeStart, String createTimeEnd, String enterpriseId, String batchId, int touchStatus, String enterpriseName);

    /**
     * 保存营销触达记录
     *
     * @param marketResourceLogDTO
     * @return
     */
    int insertLog(MarketResourceLogDTO marketResourceLogDTO);

    /**
     * 坐席重置
     *
     * @param customerId
     * @param userId
     * @author chengning@salescomm.net
     * @date 2018/9/12 11:15
     */
    Map<String, Object> seatAgentReset(String customerId, String userId, int type);

    /**
     * 获取坐席状态接口
     *
     * @param customerId
     * @param userId
     * @param type
     * @return java.util.Map<java.lang.String
                    */
    Map<String, Object> seatGetCurrentStatus(String customerId, String userId, int type);

    /**
     * 坐席呼叫接口
     *
     * @param customerId
     * @param userId
     * @param idCard
     * @param batchId
     * @param
     * @return java.util.Map<java.lang.String
                    */
    Map<String, Object> seatMakeCallEx(String customerId, String userId, String idCard, String batchId);

    /**
     * 根据客户ID查询余额是否充足
     *
     * @param customerId
     * @return boolean
     * @author chengning@salescomm.net
     * @date 2018/9/13 19:20
     */
    boolean judRemainAmount(String customerId);

    /**
     * 保存呼叫中心通话记录
     *
     * @param entId
     * @param endPwd
     * @return void
     * @author chengning@salescomm.net
     * @date 2018/9/13 19:22
     */
    void saveCallCenterVoiceLog(String entId, String endPwd);


    /**
     * 设置外呼主叫号码(用于双向呼叫)
     *
     * @param workNum
     * @param userId
     * @return java.lang.String
     * @author chengning@salescomm.net
     * @date 2018/9/14 17:14
     */
    String setWorkPhoneNum(String workNum, String userId);

    /**
     * 查询用户主叫号码
     *
     * @param userId
     * @return java.lang.String
     * @author chengning@salescomm.net
     * @date 2018/9/14 17:22
     */
    String selectWorkPhoneNum(long userId, String seatName);

    Page getSupplierList(PageParam page, SupplierListParam supplierListParam);

    void setPrice(SupplierListParam supplierListParam) throws Exception;

    /**
     * 计算营销数据
     *
     * @param customerId
     * @author chengning@salescomm.net
     * @date 2018/8/21 13:43
     */
    Map<String, Object> countMarketData(String customerId);

    Page getSmsTemplateList(PageParam page, TemplateParam templateParam);

    Map<String, Object> updateSmsTemplate(TemplateParam templateParam);

    List<Map<String, Object>> soundUrllist(RecordVoiceQueryParam recordVoiceQueryParam);

    List<Map<String, Object>> soundUrl(RecordVoiceQueryParam recordVoiceQueryParam);

    /**
     * @description 坐席外呼（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/15 15:52
     */
    Map<String, Object> seatCallCenter(String apparentNumber, String custId, String userId, String id, String batchId);

    /**
     * @description 验证外显号是否一致和是否设置了销售定价（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/15 18:40
     */
    String checkPropertyValue(String resourceId, String batchId, String userId, String mainNumber, String seatAccount, String apparentNumber, String custId, String channel) throws Exception;

    /**
     * @description 获取短信模板内容(对外接口)
     * @author:duanliying
     * @method
     * @date: 2018/11/19 9:41
     */
    String getSmsTemplateMessage(int templateId, int typeCode, String custId);

    /**
     * @description 根据批次发送短信(对外接口)
     * @author:duanliying
     * @method
     * @date: 2018/11/19 10:19
     */
    Map<String, Object> sendSmsbyBatch(int channel, int templateId, String seatAccount, String custId, String variables, int i, String batchId, String customerIds, int i1, int i2);


    /**
     * 短信历史记录查询(对外接口)
     */
    Page openSmsHistory(PageParam page, String custId);

    /**
     * 获取失联人员信息时查询触达记录
     */
    String queryCallHistory(String batchId, String superId, String custId);

    /**
     * 保存话单推送信息  进行通话扣费
     *
     * @param callBackInfoParam
     */
    int addCallBackInfoMessage(CallBackInfoParam callBackInfoParam) throws Exception;

    /**
     * 联通录音文件推送
     *
     * @param
     */
    String getUnicomRecordfile(JSONObject param) throws Exception;

    Object exportreach(String cust_id, Long userid, String user_type, String superId, String realName, String createTimeStart, String createTimeEnd, String enterpriseId, String batchId, int touchStatus, String enterpriseName, HttpServletResponse response);

    /**
     * 查询供应商销售定价
     *
     * @param supplierListParam
     * @return
     */
    Map<String, Object> searchSupplierPrice(SupplierListParam supplierListParam);

    /**
     * 根据supplierId和type 查询resourceId
     */
    String queryResourceId(String supplierId, int type);

    /**
     * 调用云讯外呼资源
     *
     * @param
     */
    Map<Object, Object> xZCallResource(String userType, String batchId, Long userId, String id, String custId, int certifyType);

    Map<Object, Object> xZCallResourceV1(String userType, String batchId, Long userId, String id, String custId);

    //查询企业是否配置了外显号码
    String selectCustCallBackApparentNumber(String custId, String apparentNumber);

}
