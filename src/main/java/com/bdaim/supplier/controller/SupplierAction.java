package com.bdaim.supplier.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.supplier.dto.SupplierDTO;
import com.bdaim.supplier.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final Logger LOG = LoggerFactory.getLogger(SupplierAction.class);

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
                data = supplierService.listNoloseAllSupplier();
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
    public ResponseInfo listSupplierByPage(@Valid PageParam page, BindingResult error, String supplierId, String supplierName, String person, String phone, String serviceType) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                Page data = supplierService.listSupplierByPage(page, supplierId, supplierName, person, phone, serviceType);
                resultMap.put("total", data.getTotal());
                resultMap.put("list", data.getData());
            }
        } catch (Exception e) {
            LOG.error("查询供应商列表失败,", e);
            return new ResponseInfoAssemble().failure(-1, "查询供应商列表异常");
        }
        return new ResponseInfoAssemble().success(resultMap);
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
    public ResponseInfo supplierChange(@RequestBody SupplierDTO supplierDTO) {
        Map<String, Object> map = new HashMap<>();
        LoginUser lu = opUser();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            List<String> type = Arrays.asList(supplierDTO.getType().split(","));
            if (type.size() > 0) {
                for (int i = 0; i < type.size(); i++) {
                    if (type.get(i).equals("1")) {
                        try {
                            supplierService.updatesupplier(supplierDTO);
                            return new ResponseInfoAssemble().success(null);
                        } catch (Exception e) {
                            LOG.error("配置供应商异常", e);
                            return new ResponseInfoAssemble().failure(-1, "配置供应商失败");
                        }
                    } else if (type.get(i).equals("2")) {
                        try {
                            supplierService.updatePrice(supplierDTO);
                            return new ResponseInfoAssemble().success(null);
                        } catch (Exception e) {
                            LOG.error("供应商定价失败", e);
                            return new ResponseInfoAssemble().failure(-1, "供应商定价失败");
                        }
                    } else if (type.get(i).equals("3")) {
                        try {
                            supplierService.supplierStatus(supplierDTO);
                            return new ResponseInfoAssemble().success(null);
                        } catch (Exception e) {
                            LOG.error("编辑供应商状态失败", e);
                            map.put("code", "1");
                            map.put("_message", "编辑供应商状态失败");
                            return new ResponseInfoAssemble().failure(-1, "编辑供应商状态失败");
                        }
                    }
                }
            }
        } else {
            LOG.info("后台登陆账号错误");
        }
        return new ResponseInfoAssemble().success(null);
    }

    /**
     * 供应商详情查询
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getSupplierPrice", method = RequestMethod.GET)
    public ResponseInfo searchSupplierPrice(String supplierId) {
        if (StringUtil.isEmpty(supplierId)) {
            return new ResponseInfoAssemble().failure(-1, "supplierId不允许为空");
        }
        LoginUser lu = opUser();
        JSONObject result = new JSONObject();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            try {
                result = supplierService.searchSupplierPrice(supplierId);
            } catch (Exception e) {
                LOG.error("供应商详情查询" + e);
            }
        }
        return new ResponseInfoAssemble().success(result);
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

    /**
     * @description 修改供应商服务优先级
     * @metho
     */
    @RequestMapping(value = "/setSupPriority", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo setSupplierPriority(@RequestBody List<Map<String, Object>> list) {
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                supplierService.setSupplierPriority(list);
            }
        } catch (Exception e) {
            LOG.error("保存服务优先级异常,", e);
            return new ResponseInfoAssemble().failure(-1, "保存服务优先级失败");
        }
        return new ResponseInfoAssemble().success(null);
    }

    /**
     * @description 根据供应商id查询服务资源
     * @metho
     */
    @RequestMapping(value = "/getSupResource", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo getSupResourceBySupplierId(String supplierId) {
        List<MarketResourceLogDTO> supResourceBySupplierId = null;
        try {
            supResourceBySupplierId = supplierService.getSupResourceBySupplierId(supplierId);
        } catch (Exception e) {
            LOG.error("保存服务优先级异常,", e);
            return new ResponseInfoAssemble().failure(-1, "保存服务优先级失败");
        }
        return new ResponseInfoAssemble().success(supResourceBySupplierId);
    }

}
