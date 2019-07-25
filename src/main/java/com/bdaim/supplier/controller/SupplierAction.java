package com.bdaim.supplier.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.StringUtil;
import com.bdaim.rbac.dto.Page;
import com.bdaim.slxf.dto.LoginUser;
import com.bdaim.supplier.dto.SupplierDTO;
import com.bdaim.supplier.service.SupplierService;
import com.hazelcast.com.eclipsesource.json.JsonObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 供应商
 *
 * @author duanliying
 * @date 2019/2/28
 * @description
 */
@Controller
@RequestMapping("/supplier")
public class SupplierAction extends BasicAction {
    public static final Logger LOG = Logger.getLogger(SupplierAction.class);

    @Resource
    SupplierService supplierService;

    /**
     * 查询所有可用的供应商
     *
     * @return
     */
    @RequestMapping(value = "/listEffectiveSupplier", method = RequestMethod.GET)
    @ResponseBody
    public String listAllSupplier(@Valid PageParam page) {
        List<Map<String, Object>> data = null;
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                data = supplierService.listAllSupplier();
            }
        } catch (Exception e) {
            LOG.error("查询单个供应商详情失败,", e);
        }
        return returnJsonData(data);
    }


    /**
     * 分页查询供应商
     *
     * @param serviceType
     * @return
     */
    @RequestMapping(value = "/getSupplierList", method = RequestMethod.GET)
    @ResponseBody
    public String listSupplierByPage(@Valid PageParam page, BindingResult error, String supplierId, String supplierName, String person, String phone, String serviceType) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        Map<String, Object> resultMap = new HashMap<>();
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                Page data = supplierService.listSupplierByPage(page, supplierId, supplierName, person, phone, serviceType);
                resultMap.put("total", data.getTotal());
                resultMap.put("list", data.getData());
                return returnJsonData(resultMap);
            }
        } catch (Exception e) {
            LOG.error("查询供应商列表失败,", e);
        }
        return returnJsonData("");
    }


    /**
     * 编辑、定价、修改状态
     * type=1 编辑
     * type=2 修改价格
     * type=3 修改状态
     *
     * @param supplierDTO
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/supplierChange", method = RequestMethod.POST)
    public String supplierChange(@RequestBody SupplierDTO supplierDTO) {
        Map<String, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            List<String> type = Arrays.asList(supplierDTO.getType().split(","));
            if (type.size() > 0) {
                for (int i = 0; i < type.size(); i++) {
                    if (type.get(i).equals("1")) {
                        try {
                            supplierService.updatesupplier(supplierDTO);
                            map.put("code", "0");
                            map.put("_message", "编辑供应商成功");
                        } catch (Exception e) {
                            LOG.error("配置供应商异常" + e);
                            map.put("code", "1");
                            map.put("_message", "编辑供应商失败");
                        }
                        json.put("data", map);
                    } else if (type.get(i).equals("2")) {
                        try {
                            supplierService.updatePrice(supplierDTO);
                            map.put("code", "0");
                            map.put("_message", "供应商定价成功");
                        } catch (Exception e) {
                            LOG.error("供应商定价失败" + e);
                            map.put("code", "1");
                            map.put("_message", "供应商定价失败");
                        }
                        json.put("data", map);
                    } else if (type.get(i).equals("3")) {
                        try {
                            supplierService.supplierStatus(supplierDTO);
                            map.put("code", "0");
                            map.put("_message", "编辑供应商状态成功");
                        } catch (Exception e) {
                            LOG.error("编辑供应商状态失败" + e);
                            map.put("code", "1");
                            map.put("_message", "编辑供应商状态失败");
                        }
                        json.put("data", map);
                    }
                }
            }
        } else {
            LOG.info("后台登陆账号错误");
        }
        return json.toJSONString();
    }

    /**
     * 供应商详情查询
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getSupplierPrice", method = RequestMethod.GET)
    public String searchSupplierPrice(String supplierId) {
        if (StringUtil.isEmpty(supplierId)) {
            return "supplierId不允许为空";
        }
        LoginUser lu = opUser();
        JSONObject result= new JSONObject();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            try {
                result = supplierService.searchSupplierPrice(supplierId);
            } catch (Exception e) {
                LOG.error("供应商详情查询" + e);
            }
        }

        return returnJsonData(result);
    }

    /**
     * 后台 账单管理--供应商调账功能
     */
    @RequestMapping(value = "/balanceChange", method = RequestMethod.POST)
    @ResponseBody
    public Object banlanceChange(@RequestBody CustomerBillQueryParam param) {
        Map<String, Object> resultMap = new HashMap<>();
        Boolean flag = false;
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            if (opUser().getId() != null) {
                param.setUserId(opUser().getId());
                flag = supplierService.changeSupplierBalance(param);
            }
            if (flag) {
                resultMap.put("result", "0");
                resultMap.put("_message", "供应商调账成功！");
            } else {
                resultMap.put("result", "1");
                resultMap.put("_message", "供应商调账失败！");
            }
        }
        return JSONObject.toJSON(resultMap);
    }
}
