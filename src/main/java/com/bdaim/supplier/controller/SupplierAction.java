package com.bdaim.supplier.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customgroup.dto.CustomerGrpOrdParam;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.supplier.dto.SupplierDTO;
import com.bdaim.supplier.service.SupplierService;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 供应商
 *
 * @author duanliying
 * @date 2019/2/28
 * @description
 */
@RestController
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

    @Resource
    CustomGroupService customGroupService;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public Object saveSupplier(@RequestBody String body) {
        int code = 0;
        SupplierDTO supplierDTO = null;
        try {
            supplierDTO = JSONObject.parseObject(body, SupplierDTO.class);
            supplierDTO.setResourceConfig(JSONObject.parseObject(body));
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "供应商保存参数异常");
        }
        try {
            if (supplierDTO.getSupplierId() == null) {
                code = supplierService.saveSupplier(supplierDTO);
            } else {
                code = supplierService.updateSupplierPrice(supplierDTO);
            }
        } catch (Exception e) {
            LOG.error("保存供应商失败,", e);
        }
        if (1 == code) {
            return returnSuccess();
        }
        return returnError();
    }

    @RequestMapping(value = "/saveResource", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public Object saveResource(@RequestBody String body) {
        int code = 0;
        SupplierDTO supplierDTO = null;
        try {
            supplierDTO = JSONObject.parseObject(body, SupplierDTO.class);
            supplierDTO.setResourceConfig(JSONObject.parseObject(body));
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "供应商保存参数异常");
        }
        try {
            code = supplierService.saveResConfig(supplierDTO);
        } catch (Exception e) {
            LOG.error("保存供应商失败,", e);
        }
        if (1 == code) {
            return returnSuccess();
        }
        return returnError();
    }

    @RequestMapping(value = "/selectSupplier", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String selectSupplier(String supplierId) {
        JSONObject supplierDTO = null;
        try {
            supplierDTO = supplierService.selectSupplierAndResourceProperty0(supplierId);
        } catch (Exception e) {
            LOG.error("查询单个供应商详情失败,", e);
        }
        return returnJsonData(supplierDTO);
    }

    @RequestMapping(value = "/listResource", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String listResource(String supplierId) {
        if (StringUtil.isEmpty(supplierId)) {
            throw new ParamException("supplierId参数不能为空");
        }
        List<MarketResourceDTO> marketResourceDTOList = null;
        try {
            marketResourceDTOList = supplierService.listResourceBySupplierId(supplierId);
        } catch (Exception e) {
            LOG.error("查询供应商下的资源列表失败,", e);
        }
        return returnJsonData(marketResourceDTOList);
    }

    /**
     * 查询所有通话资源（区分呼叫中心、双呼）
     *
     * @param custId 查询某客户分配的呼叫线路资源
     * @return
     */
    @RequestMapping(value = "/listVoiceResource", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER,ROLE_CUSTOMER")
    public String listVoiceResourceByType(String custId) {
        JSONObject result = null;
        try {
            LoginUser lu = opUser();
            if (lu == null) return null;
            if ("ROLE_CUSTOMER".equals(lu.getRole())) {
                custId = lu.getCustId();
            }
            if (StringUtil.isEmpty(custId)) {
                result = supplierService.listVoiceResourceByType("1");
            } else {
                result = supplierService.getCustomerCallPriceConfig(custId);
            }
        } catch (Exception e) {
            LOG.error("查询资源列表失败,", e);
        }
        return returnJsonData(result);
    }

    @RequestMapping(value = "/selectResource", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String selectResource(String type) {
        List<MarketResourceDTO> marketResourceDTOList = null;
        try {
            marketResourceDTOList = supplierService.listResource(type);
        } catch (Exception e) {
            LOG.error("查询资源列表失败,", e);
        }
        return returnJsonData(marketResourceDTOList);
    }

    @PostMapping(value = "/pageResource")
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String pageResource(@Valid PageParam page, BindingResult error, String type) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        Page pageData = null;
        try {
            pageData = supplierService.pageResource(type, page.getPageNum(), page.getPageSize());
        } catch (Exception e) {
            LOG.error("查询资源列表失败,", e);
        }
        return returnJsonData(getPageData(pageData));
    }

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String page(@Valid PageParam page, BindingResult error, String supplierId, String supplierName, String supplierType, String status) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        Page pageData = null;
        try {
            pageData = supplierService.pageSupplier(page.getPageNum(), page.getPageSize(), supplierName, supplierId, supplierType, StringUtil.isNotEmpty(status) ? NumberConvertUtil.parseInt(status) : 0);
        } catch (Exception e) {
            LOG.error("查询单个供应商详情失败,", e);
        }
        return returnJsonData(getPageData(pageData));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public Object updateSupplier(@RequestBody SupplierDTO supplierDTO) {
        int code = 0;
        try {
            code = supplierService.updateSupplier(supplierDTO);
        } catch (Exception e) {
            LOG.error("修改供应商主信息失败,", e);
        }
        if (1 == code) {
            return returnSuccess();
        }
        return returnError();
    }

    @RequestMapping(value = "/listEffectiveSupplier0", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String listAllSupplier() {
        List<Map<String, Object>> data = null;
        try {
            data = supplierService.listOnlineAllSupplier();
        } catch (Exception e) {
            LOG.error("查询单个供应商详情失败,", e);
        }
        return returnJsonData(data);
    }

    @RequestMapping(value = "/listMonthBill", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String page(@Valid PageParam page, BindingResult error, String yearMonth, String supplierId, String supplierName, String supplierType, String status) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        if (StringUtil.isEmpty(yearMonth)) {
            throw new ParamException("yearMonth参数不能为空");
        }
        Map<String, Object> data = null;
        try {
            data = supplierService.listSupplierMonthBill(yearMonth, page.getPageNum(), page.getPageSize(), supplierName, supplierId, supplierType, NumberConvertUtil.parseInt(status));
        } catch (Exception e) {
            LOG.error("查询供应商月账单失败,", e);
        }
        return returnJsonData(data);
    }

    @RequestMapping(value = "/listBill", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String listBill(@Valid PageParam page, BindingResult error, String supplierId, String resourceId, String type, String orderNo, String custId, String startTime, String endTime) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        if (StringUtil.isEmpty(supplierId)) {
            throw new ParamException("supplierId参数不能为空");
        }
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        if (StringUtil.isEmpty(startTime)) {
            throw new ParamException("startTime参数不能为空");
        }
        if (StringUtil.isEmpty(endTime)) {
            throw new ParamException("endTime参数不能为空");
        }
        int typeCode = -1;
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        typeCode = NumberConvertUtil.parseInt(type);
        Map<String, Object> data = null;
        String account = custId;
        try {
            data = supplierService.listSupplierBill(supplierId, resourceId, typeCode, orderNo, account, startTime, endTime, page.getPageNum(), page.getPageSize());
        } catch (Exception e) {
            LOG.error("查询供应商月账单详情列表失败,", e);
        }
        return returnJsonData(data);
    }

    @RequestMapping(value = "/exportExcelListBillByType", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    public void exportExcelListBillByType(HttpServletResponse response, String supplierId, String resourceId, String type, String orderNo, String custId, String startTime, String endTime) {
        if (StringUtil.isEmpty(supplierId)) {
            throw new ParamException("supplierId参数不能为空");
        }
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        if (StringUtil.isEmpty(startTime)) {
            throw new ParamException("startTime参数不能为空");
        }
        if (StringUtil.isEmpty(endTime)) {
            throw new ParamException("endTime参数不能为空");
        }
        int typeCode = -1;
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        typeCode = NumberConvertUtil.parseInt(type);
        response.setContentType("application/json;charset=utf-8");
        try {
            supplierService.exportExcelListBillByType(response, supplierId, resourceId, typeCode, orderNo, custId, startTime, endTime);
        } catch (Exception e) {
            LOG.error("供应商月账单详情导出失败,", e);
        }
    }


    @RequestMapping(value = "/exportExcelMonthBill", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    public void exportExcelMonthBill(HttpServletResponse response, String supplierId, String year, String month) {
        if (StringUtil.isEmpty(supplierId)) {
            throw new ParamException("supplierId参数不能为空");
        }
        if (StringUtil.isEmpty(year)) {
            throw new ParamException("year参数不能为空");
        }
        if (StringUtil.isEmpty(month)) {
            throw new ParamException("month参数不能为空");
        }
        response.setContentType("application/json;charset=utf-8");
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startTime = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), 1, 0, 0, 0, 0);
            LocalDateTime endTime = startTime.plusMonths(1);
            supplierService.exportExcelMonthBill(response, supplierId, startTime.format(dateTimeFormatter), endTime.format(dateTimeFormatter));
        } catch (Exception e) {
            LOG.error("供应商月账单总数据导出失败,", e);
        }
    }

    /**
     * 供应商客群分页
     *
     * @param param
     * @param error
     * @return
     */
    @RequestMapping(value = "/listSupplierCustGroup", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String listSupplierCustGroup(@Valid CustomerGrpOrdParam param, BindingResult error) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        Page page = null;
        try {
            String userId = "";
            if ("ROLE_USER".equals(opUser().getRole())) {
                userId = String.valueOf(opUser().getId());
            }
            page = customGroupService.pageSupplierCustGroup(userId, param);
        } catch (Exception e) {
            LOG.error("供应商客群分页失败,", e);
        }
        return returnJsonData(getPageData(page));
    }

    /**
     * 导出供应商调用统计数据
     *
     * @param response
     * @param param
     */
    @RequestMapping(value = "/exportSupplierCustGroup", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    public void exportSupplierCustGroup(HttpServletResponse response, CustomerGrpOrdParam param) {
        try {
            param.setPageNum(0);
            param.setPageSize(Integer.MAX_VALUE);
            String userId = "";
            if ("ROLE_USER".equals(opUser().getRole())) {
                userId = String.valueOf(opUser().getId());
            }
            supplierService.exportSupplierCustGroupExcel(response, userId, param);
        } catch (Exception e) {
            LOG.error("供应商调用统计数据导出导出失败,", e);
        }
    }

    /**
     * 供应商数据提取量和标记成单量按月统计
     *
     * @return
     */
    @RequestMapping(value = "/statSupplierMonthData", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String statSupplierData() {
        String userId = "";
        if ("ROLE_USER".equals(opUser().getRole())) {
            userId = String.valueOf(opUser().getId());
        }
        Map<String, Object> list = supplierService.statSupplierMonthData(userId, 6);
        return returnJsonData(list);
    }

    /**
     * 供供应商数据提取量和标记成单量按天统计
     *
     * @param supplierId
     * @param yearMonth
     * @param type
     * @return
     */
    @RequestMapping(value = "/statSupplierDayData", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String statSupplierDayData(String supplierId, String yearMonth, Integer type) {
        if (StringUtil.isEmpty(supplierId)) {
            throw new ParamException("supplierId参数不能为空");
        }
        if (StringUtil.isEmpty(yearMonth)) {
            throw new ParamException("yearMonth参数不能为空");
        }
        if (type == null) {
            throw new ParamException("type参数不能为空");
        }
        String userId = "";
        if ("ROLE_USER".equals(opUser().getRole())) {
            userId = String.valueOf(opUser().getId());
        }
        List<String> list = supplierService.statSupplierDayData(userId, type, supplierId, yearMonth);
        return returnJsonData(list);
    }

    /**
     * 供应商保存及修改
     *
     * @param body
     * @param id
     * @return
     */
    @PostMapping("/info/{id}")
    public ResponseInfo saveSupplier(@RequestBody String body, @PathVariable(name = "id", required = false) Integer id) {
        ResponseInfo resp = new ResponseInfo();
        SupplierDTO supplierDTO = null;
        try {
            supplierDTO = JSONObject.parseObject(body, SupplierDTO.class);
            supplierDTO.setResourceConfig(JSONObject.parseObject(body));
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "供应商保存参数异常");
        }
        try {
            if (id == null || id == 0) {
                resp.setData(supplierService.saveSupplier1(supplierDTO));
            } else {
                supplierDTO.setSupplierId(id);
                resp.setData(supplierService.updateSupplierPrice1(supplierDTO));
            }
        } catch (Exception e) {
            LOG.error("保存供应商失败,", e);
            return new ResponseInfoAssemble().failure(-1, "供应商保存参数异常");
        }

        return resp;
    }


}
