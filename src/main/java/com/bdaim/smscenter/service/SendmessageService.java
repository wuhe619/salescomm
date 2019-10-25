package com.bdaim.smscenter.service;

import com.bdaim.batch.dto.ExpressLog;
import com.bdaim.batch.entity.SenderInfo;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author wangxx@bdaim.com
 * @Description:
 * @date 2018/12/27 10:31
 */

public interface SendmessageService {
    Map<Object, Object> sendadd(SenderInfo senderInfo, String compId);

    PageList sendlist(PageParam page, String compId);

    Map<Object, Object> senddelete(String id);

    Map<Object, Object> sendupdate(SenderInfo senderInfo);

    Map<Object, Object> defaultupdate(String id, String compId);


    PageList pageList(PageParam page, ExpressLog expressLog);

    Object exportExportRecords(ExpressLog expressLog, HttpServletResponse response);


    Object repairDetailsderive(String batchid, String name, String phone, String touch_id, Integer status, Integer status1, HttpServletResponse response);


    void add(String fileName, String batch_id, String id_card);

    List<Map<String, Object>> repairDetails(Integer pageNum, Integer pageSize, String batchid, String name, String phone, String touch_id, Integer status, Integer status1);


    Map<String, Object> submitCourier(String siteid, String companyid, String bachid);

    Map<String, Object> express(String touch_id);

    Map<String, Object> time(String batchid);

    /**
     * 发件人信息列表
     *
     * @param map
     * @return
     * @auther Chacker
     * @date 2019/8/5 15:20
     */
    Map<String, Object> senderList(Map<String, Object> map);

    /**
     * 添加发件人信息
     *
     * @param map
     * @return
     * @auther Chacker
     * @date 2019/8/5 15:20
     */
    void senderAdd(Map<String, Object> map);

    /**
     * 删除发件人信息
     *
     * @param id
     * @return
     * @auther Chacker
     * @date 2019/8/5 16:36
     */
    void senderDelete(String id);

    /**
     * 设为默认发件人/发件地址
     *
     * @param id
     * @param cust_id 企业ID
     * @return
     * @auther Chacker
     * @date 2019/8/5 16:58
     */
    void defaultUpdate(String id, String cust_id);

    /**
     * 修改发件人信息/发件地址
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/5 17:23
     */
    void senderUpdate(Map<String, Object> map);











    /*  Object uploadFiles(HttpServletRequest request, HttpServletResponse response, String cust_id, String pictureName);*/
}
