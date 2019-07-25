package com.bdaim.callcenter.service;

import com.bdaim.slxf.entity.UnicomSendSmsParam;

import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/10
 * @description
 */
public interface CallCenterService {

    /**
     * 获取客户呼叫中心配置信息
     *
     * @param customerId
     * @param userId
     * @param
     * @author chengning@salescomm.net
     * @date 2018/9/12 14:38
     */
    Map<String, Object> getCallCenterConfigData(String customerId, String userId);

    Map<String, Object> getCallCenterConfigDataV1(String customerId, String userId, String resourceId);

    /**
     * 联通坐席登录
     *
     * @param entId
     * @param userId
     * @param tel
     * @param type
     * @author chengning@salescomm.net
     * @date 2018/9/10 17:56
     */
    Map<String, Object> unicomSeatLogin(String entId, String userId, String userPwd, String tel, int type);

    /**
     * 联通坐席登出
     *
     * @param entId
     * @param userId
     * @author chengning@salescomm.net
     * @date 2018/9/10 18:37
     */
    Map<String, Object> unicomSeatLogout(String entId, String userId);

    /**
     * 获取坐席状态
     *
     * @param entId
     * @param userId
     * @return int
     * @author chengning@salescomm.net
     * @date 2018/9/11 14:35
     */
    Map<String, Object> unicomGetSeatStatus(String entId, String userId);

    /**
     * 联通坐席重置接口
     *
     * @param entId  企业ID
     * @param userId 坐席账号
     * @author chengning@salescomm.net
     * @date 2018/9/10 18:41
     */
    Map<String, Object> unicomAgentReset(String entId, String userId);

    /**
     * 联通坐席外呼
     *
     * @param entId      企业ID
     * @param userId     坐席ID
     * @param activityId 活动ID
     * @param provideId  省份ID
     * @param customerId 客户ID
     * @param showNumber 外显号(非必填)
     * @author chengning@salescomm.net
     * @date 2018/9/10 18:37
     */
    Map<String, Object> unicomSeatMakeCallEx(String entId, String userId, String activityId, String provideId, String customerId, String showNumber);

    /**
     * 联通坐席挂断
     *
     * @param entId
     * @param userId
     * @param uuid
     * @author chengning@salescomm.net
     * @date 2018/9/11 14:37
     */
    Map<String, Object> unicomSeatHangUp(String entId, String userId, String uuid);

    /**
     * 联通获取通话记录
     *
     * @param uuid        外呼流水号
     * @param callStatus  营销情况  1：成功 2：失败
     * @param callReply   坐席反馈
     * @param entId       企业ID
     * @param activityId  活动ID
     * @param entPassword 企业密码
     * @author chengning@salescomm.net
     * @date 2018/9/11 15:15
     */
    Map<String, Object> unicomGetCallData(String uuid, String callStatus, String callReply, String entId, String activityId, String entPassword);

    /**
     * 联通获取通话录音文件信息
     *
     * @param uuid
     * @param entId
     * @author chengning@salescomm.net
     * @date 2018/9/11 15:21
     */
    Map<String, Object> unicomRecordByRequestId(String uuid, String entId);

    /**
     * 4.10	坐席注册接口
     *
     * @param entId
     * @param userId
     * @param userName
     * @param password
     * @author chengning@salescomm.net
     * @date 2018/9/14 16:21
     */
    Map<String, Object> unicomAddSeatAccount(String entId, String userId, String userName, String password);

    /**
     * 坐席修改密码接口
     *
     * @param entId
     * @param userId
     * @param password
     * @author chengning@salescomm.net
     * @date 2018/9/14 16:22
     */
    Map<String, Object> unicomUpdateSeatPasswd(String entId, String userId, String password);

    /**
     * 分机注册接口
     *
     * @param entId
     * @param tel
     * @param type  0:网络电话 1:固话,手机
     * @author chengning@salescomm.net
     * @date 2018/9/14 16:25
     */
    Map<String, Object> unicomExtensionRegister(String entId, String tel, int type);

    /**
     * 根据模板发送短信
     *
     * @param unicomSendSmsParam
     * @return java.util.Map<java.lang.String               ,               java.lang.Object>
     * @author chengning@salescomm.net
     * @date 2018/9/14 17:05
     */
    Map<String, Object> unicomSendMessageData(UnicomSendSmsParam unicomSendSmsParam);

    /**
     * @description 刪除坐下主叫号码（分机号码）
     * @author:duanliying
     * @method
     * @date: 2018/10/25 15:35
     */
    Map<String, Object> unicomExtensionDelete(String entId, String extension);

    /**
     * @description 讯众外呼获取外呼配置信息
     * @author:duanliying
     * @method
     * @date: 2019/4/9 10:41
     */
    Map<String, Object> getXzConfigData(String custId, String s, String resourceId);
}
