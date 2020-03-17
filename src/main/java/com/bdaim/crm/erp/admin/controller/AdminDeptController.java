package com.bdaim.crm.erp.admin.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.entity.LkCrmAdminDeptEntity;
import com.bdaim.crm.erp.admin.service.LkAdminDeptService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hmb
 */
@RestController
@RequestMapping("/system/dept")
public class AdminDeptController extends BasicAction {

    @Resource
    private LkAdminDeptService adminDeptService;

    /**
     * @param adminDept 部门对象
     * @author hmb
     * 设置部门
     */
    @Permissions("manage:user")
    @RequestMapping(value = "/setDept", method = RequestMethod.POST)
    public R setDept(LkCrmAdminDeptEntity adminDept) {
        return (adminDeptService.setDept(adminDept));
    }

    /**
     * @author hmb
     * 查询部门tree列表
     */
    @RequestMapping(value = "/queryDeptTree", method = RequestMethod.POST)
    public R queryDeptTree() {
        String type = getPara("type");
        Integer id = getParaToInt("id");
        return (R.ok().put("data", adminDeptService.queryDeptTree(type, id)));
    }

    /**
     * @author zhangzhiwie
     * 查询权限内部门
     */
    @RequestMapping(value = "/queryDeptByAuth", method = RequestMethod.POST)
    public R queryDeptByAuth() {
        long timePre = System.currentTimeMillis();
        List<Record> result = adminDeptService.queryDeptByAuth(BaseUtil.getUser().getUserId());
        long timeSuf = System.currentTimeMillis();
        System.out.println("耗时  " + (timeSuf - timePre));
        return (R.ok().put("data", result));
    }

    /**
     * @author hmb
     * 删除部门
     */
    @Permissions("manage:user")
    @RequestMapping(value = "/deleteDept", method = RequestMethod.POST)
    public R deleteDept() {
        String id = getPara("id");
        return (adminDeptService.deleteDept(id));
    }
}
