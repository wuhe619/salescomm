package com.bdaim.rbac.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.rbac.dto.DeptDto;
import com.bdaim.rbac.service.DeptService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/3/15
 * @description
 */
@Controller
@RequestMapping("/dept")
public class DeptAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(DeptAction.class);
    @Resource
    private DeptService deptService;

    /**
     * 编辑部门信息
     */
    @RequestMapping(value = "/editDeptMessage", method = RequestMethod.POST)
    @ResponseBody
    public Object editDeptMessage(@RequestBody DeptDto deptDto) {
        Map<String, String> resultMap = new HashMap<>();
        //获取当前操作人
        LoginUser lu = opUser();
        String optUser = lu.getUsername();
        resultMap = deptService.updateDeptMessage(deptDto, optUser);
        return JSONObject.toJSON(resultMap);
    }


    /**
     * 删除部门信息
     */
    @RequestMapping(value = "/delDept", method = RequestMethod.GET)
    @ResponseBody
    public Object delDeptMessage(String deptId) {
        Map<String, String> resultMap = new HashMap<>();
        //获取当前操作人
        resultMap = deptService.delDeptMessage(deptId);
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 部门列表查询
     */
    @RequestMapping(value = "/queryDeptList", method = RequestMethod.GET)
    @ResponseBody
    public String queryDeptList(@RequestParam Map<String,Object> map) {
        //获取当前操作人
        Map<String,Object> resultMap = deptService.queryDeptList(map);
        return JSON.toJSONString(resultMap);
    }
    /**
     * 获取部门信息以及部门下职位信息
     */
    @RequestMapping(value = "/getDeptAndRoles", method = RequestMethod.GET)
    @ResponseBody
    public Object getDeptAndRoles( String deptId) {
        List<Map<String, Object>> deptAndRoles = null;
        try {
            deptAndRoles = deptService.getDeptAndRoles(deptId);
        } catch (Exception e) {
            logger.error("查询部门信息以及职位信息异常" + e);
        }
        return JSONObject.toJSON(deptAndRoles);
    }

}
