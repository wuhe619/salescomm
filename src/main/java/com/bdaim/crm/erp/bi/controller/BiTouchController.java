package com.bdaim.crm.erp.bi.controller;

import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.erp.bi.service.BiTouchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseInfo phone(@RequestParam Integer deptId, @RequestParam Long userId,
                              @RequestParam String type) {
        return biTouchService.phone(deptId, userId, type);
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
    public ResponseInfo textMessage(@RequestParam Integer deptId, @RequestParam Long userId,
                                    @RequestParam String type) {
        return biTouchService.textMessage(deptId, userId, type);
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
    public ResponseInfo phoneList(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                  @RequestParam Integer deptId,
                                  @RequestParam Long userId, @RequestParam String type) {
        if (pageNum == null || pageSize == null) {
            return new ResponseInfoAssemble().failure(500, "请选择分页");
        }
        return biTouchService.phoneList(pageNum, pageSize, deptId, userId, type);
    }
}
