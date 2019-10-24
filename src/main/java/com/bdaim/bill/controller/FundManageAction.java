package com.bdaim.bill.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.ParamException;
import com.bdaim.customer.user.service.UserGroupService;
import com.bdaim.util.StringUtil;

import org.apache.log4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 资金管理
 *
 */
@RestController
@RequestMapping("/fund")
public class FundManageAction extends BasicAction {

    private final static Logger LOG = Logger.getLogger(FundManageAction.class);

    @Resource
    private UserGroupService userGroupService;

    /**
     * 职场结算单管理列表查询、职场付款申请单列表查询 list
     * @param projectId
     * @param customerId
     * @param jobId
     * @param pageParam
     * @param error
     * @return
     */
    @RequestMapping(value = "/settlementtotallist", method = RequestMethod.GET)
    public ResponseJson jobSettlementPriceList(String serviceCode, String projectId, String customerId, String jobId,
                                               String billDate, @Valid PageParam pageParam, BindingResult error,
                                               String operator, String status) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        if(StringUtil.isEmpty(serviceCode)){
            throw new ParamException("参数 serviceCode 不能为空");
        }
        LoginUser lu = opUser();
        if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
            return responseJson;
        }

        Page result = userGroupService.pageCommonInfoSearch(serviceCode,pageParam.getPageNum(),
                pageParam.getPageSize(),projectId ,customerId,jobId,billDate,operator,status,lu);
        responseJson.setData(getPageData(result));
        return responseJson;
    }

    /**
     * 结算单详情列表查询
     * @param serviceCode
     * @param projectId
     * @param pageParam
     * @param error
     * @return
     */
    @RequestMapping(value = "/settlementdetaillist", method = RequestMethod.GET)
    public ResponseJson jobSettlementDetailList(String serviceCode, String pid, String projectId, String type, String status,
                                                String startTime, String endTime, String operator, @Valid PageParam pageParam,
                                                BindingResult error, String batchNo) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        if (StringUtil.isEmpty(serviceCode)) {
            throw new ParamException("参数 serviceCode 不能为空");
        }
        if (StringUtil.isEmpty(pid) && StringUtil.isEmpty(batchNo)) {
            throw new ParamException("参数 pid 和 batchNo 不能同时为空");
        }
         Page page = userGroupService.jobSettlementDetailList(pageParam.getPageNum(),
                 pageParam.getPageSize(),pid,serviceCode,projectId,type,status,operator,startTime,endTime,batchNo,true);
         responseJson.setData(getPageData(page));

        return responseJson;
    }

    /**
     * 结算单提交付款/客审单提交收款
     * @param pid
     * @param isall
     * @param commitStr
     * @return
     */
    @RequestMapping(value = "/jobSelementCommit", method = RequestMethod.POST)
    public ResponseJson jobSelementCommit(String serviceCode,String pid,String isall,String commitStr,String remark){
        ResponseJson responseJson = new ResponseJson();

        LoginUser lu = opUser();
        if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
            responseJson.setCode(-1);
            responseJson.setMessage("无权操作");
            return responseJson;
        }
        if(StringUtil.isEmpty(pid) || StringUtil.isEmpty(serviceCode)){
            throw new ParamException("参数错误");
        }
        if(StringUtil.isEmpty(isall) && StringUtil.isEmpty(commitStr)){
            throw new ParamException("参数错误");
        }
        try {
            userGroupService.jobSelementCommit(serviceCode, pid, isall, commitStr, lu,remark);
            responseJson.setCode(0);
            responseJson.setMessage("成功");
        }catch(Exception e){
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage("失败");
        }
        return responseJson;
    }

    /**
     * 获取提交数据时的记录数和成功单总量
     * @param serviceCode
     * @param pid
     * @return
     */
    @RequestMapping(value = "/commititems", method = RequestMethod.GET)
    public ResponseJson getCommitItems(String serviceCode,String pid){
        ResponseJson responseJson = new ResponseJson();
        LoginUser lu = opUser();
        if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
            responseJson.setCode(-1);
            responseJson.setMessage("无权操作");
            return responseJson;
        }
        if(StringUtil.isEmpty(pid) || StringUtil.isEmpty(serviceCode)){
            throw new ParamException("参数错误");
        }
        try {
            JSONObject json=userGroupService.getCommitItems(serviceCode, pid);
            responseJson.setData(json);
            responseJson.setCode(0);
            responseJson.setMessage("成功");
        }catch(Exception e){
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage("失败");
        }
        return responseJson;
    }



    /**
     * 职场申请付款确认付款时间/项目收款列表确认收款完成
     * @param serviceCode
     * @param id
     * @param paymentTime
     * @param remark
     * @return
     */
    @RequestMapping(value = "/confirmpayJobSettlement", method = RequestMethod.POST)
    public ResponseJson confirmpayJobSettlement(String serviceCode,String id,String paymentTime,String remark){
        ResponseJson responseJson = new ResponseJson();

        LoginUser lu = opUser();
        if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
            responseJson.setCode(-1);
            responseJson.setMessage("无权操作");
            return responseJson;
        }
        if(StringUtil.isEmpty(id) || StringUtil.isEmpty(serviceCode) || StringUtil.isEmpty(paymentTime)){
            throw new ParamException("参数错误");
        }
        try {
            userGroupService.confirmpayJobSettlement(serviceCode, id, paymentTime,remark,lu);
            responseJson.setCode(0);
            responseJson.setMessage("成功");
        }catch(Exception e){
            responseJson.setCode(-1);
            responseJson.setMessage("失败");
        }
        return responseJson;
    }



    /**
     * 导出详情数据
     * @param serviceCode
     * @param pid
     * @param projectId
     * @param type
     * @param status
     * @param startTime
     * @param endTime
     * @param operator
     * @param batchNo
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/exportdetail", method = RequestMethod.GET)
    public ResponseJson exportDetailList(String serviceCode, String pid, String projectId, String type, String status,
                                         String startTime, String endTime, String operator, String batchNo,
                                         HttpServletRequest request, HttpServletResponse response) {
        ResponseJson responseJson = new ResponseJson();
        if (StringUtil.isEmpty(serviceCode)) {
            throw new ParamException("参数 serviceCode 不能为空");
        }
        if (StringUtil.isEmpty(pid) && StringUtil.isEmpty(batchNo)) {
            throw new ParamException("参数 pid 和 batchNo 不能同时为空");
        }
        userGroupService.exportDetailList(serviceCode, pid, projectId, type, status,
                startTime, endTime, operator,  batchNo, response);
        return responseJson;
    }
}
