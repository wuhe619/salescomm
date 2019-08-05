package com.bdaim.smscenter.service;

import com.bdaim.batch.dto.ExpressLog;
import com.bdaim.batch.entity.SenderInfo;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.page.Page;

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

    Page sendlist(PageParam page, String compId);

    Map<Object, Object> senddelete(String id);

    Map<Object, Object> sendupdate(SenderInfo senderInfo);

    Map<Object, Object> defaultupdate(String id, String compId);


    Page pageList(PageParam page, ExpressLog expressLog);

    Object exportExportRecords(ExpressLog expressLog, HttpServletResponse response);


   Object repairDetailsderive(String batchid, String name, String phone, String touch_id, Integer status, Integer status1, HttpServletResponse response);


    void add(String fileName, String batch_id, String id_card);

    List<Map<String, Object>> repairDetails(Integer pageNum, Integer pageSize, String batchid, String name, String phone, String touch_id, Integer status, Integer status1);


    Map<String, Object> submitCourier(String siteid, String companyid, String bachid);

    Map<String, Object> express(String touch_id);

    Map<String, Object> time(String batchid);

    Map<String, Object> senderList(Map<String, Object> map);











    /*  Object uploadFiles(HttpServletRequest request, HttpServletResponse response, String cust_id, String pictureName);*/
}
