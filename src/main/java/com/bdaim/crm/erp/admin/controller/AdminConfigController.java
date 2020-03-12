package com.bdaim.crm.erp.admin.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.entity.LkCrmAdminConfigEntity;
import com.bdaim.crm.erp.admin.service.AdminConfigService;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/config")
public class AdminConfigController extends BasicAction {
    @Resource
    private AdminConfigService adminConfigService;

    /**
     * 保存系统参数
     */
    @PostMapping(value = "saveOrUpdate")
    public R saveOrUpdate(@RequestBody LkCrmAdminConfigEntity entity) {
        return (adminConfigService.saveOrUpdate(entity));
    }

    /**
     * 根据名称查询参数
     */
    @PostMapping(value = "/queryByName")
    public R queryByName(@RequestParam("name") String name) {
        return adminConfigService.queryByName(name);
    }

}
