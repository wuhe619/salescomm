package com.bdaim.crm.erp.bi.controller;

import com.bdaim.common.response.ResponseInfo;
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
}
