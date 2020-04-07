package com.bdaim.crm.erp.bi.controller;

import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.erp.bi.service.BiTouchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/biTouch")
public class BiTouchController {

    @Autowired
    private BiTouchService biTouchService;

    /**
     * 电话触达接通率分析
     *
     * @return
     * @author Chacker
     */
    @RequestMapping(value = "/phone")
    public ResponseInfo phone(Integer deptId, Long userId, String type,
                              String startTime, String endTime) {
        return biTouchService.phone(deptId, userId, type, startTime, endTime);
    }

    /**
     * 短信触达送达率分析
     *
     * @param deptId
     * @param userId
     * @param type
     * @return
     * @author Chacker
     */
    @RequestMapping(value = "/textMessage")
    public ResponseInfo textMessage(Integer deptId, Long userId,
                                    String type, String startTime, String endTime) {
        return biTouchService.textMessage(deptId, userId, type, startTime, endTime);
    }

    /**
     * 电话统计分析列表
     *
     * @param pageNum
     * @param pageSize
     * @param deptId
     * @param userId
     * @param type
     * @return
     */
    @RequestMapping(value = "/phoneList")
    public ResponseInfo phoneList(Integer pageNum, Integer pageSize,
                                  Integer deptId, Long userId, String type,
                                  String startTime, String endTime) {
        if (pageNum == null || pageSize == null) {
            return new ResponseInfoAssemble().failure(500, "请选择页码");
        }
        return biTouchService.phoneList(pageNum, pageSize, deptId, userId, type, startTime, endTime);
    }

    /**
     * 短信统计分析列表
     *
     * @param pageNum
     * @param pageSize
     * @param deptId
     * @param userId
     * @param type
     * @return
     */
    @RequestMapping(value = "/messageList")
    public ResponseInfo messageList(Integer pageNum, Integer pageSize,
                                    Integer deptId, Long userId, String type,
                                    String startTime, String endTime) {
        if (pageNum == null || pageSize == null) {
            return new ResponseInfoAssemble().failure(500, "请选择页码");
        }
        return biTouchService.messageList(pageNum, pageSize, deptId, userId, type, startTime, endTime);

    }
}
