package com.bdaim.customeruser.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dto.CommonInfoDTO;
import com.bdaim.customer.dto.CommonInfoServiceCodeEnum;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.customer.dto.CustomerUserGroupDTO;
import com.bdaim.customer.entity.CustomerUserGroup;
import com.bdaim.customeruser.service.UserGroupService;
import org.apache.log4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 坐席组
 * @author chengning@salescomm.net
 * @date 2018/9/27
 * @description
 */
@RestController
@RequestMapping("/userGroup")
public class CustomerUserGroupAction extends BasicAction {

    private final static Logger LOG = Logger.getLogger(CustomerUserGroupAction.class);

    @Resource
    private UserGroupService userGroupService;

    /**
     * 保存用户群组
     *
     * @param params
     * @return int
     * @author chengning@salescomm.net
     * @date 2018/9/27 14:14
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseCommon addUserGroup(@RequestBody Map<String, Object> params) {
        Object leavel = params.getOrDefault("leavel",null);
        if(null == leavel || "".equals(leavel)){
            throw new ParamException("参数leavel必填");
        }
        CustomerUserGroup userGroupEntity = new CustomerUserGroup();
        userGroupEntity.setName(String.valueOf(params.get("name")));
        userGroupEntity.setCustId(opUser().getCustId());
        // 用户群组创建人
        userGroupEntity.setCreateUser(String.valueOf(opUser().getId()));
        userGroupEntity.setLeavel(Integer.valueOf(leavel.toString()));
        List<String> userIds = null;
        String groupLeaderUserId = null;
        if("0".equals(leavel.toString())){ //职场
            userGroupEntity.setProvince(params.get("province")==null?"":params.get("province").toString());
            userGroupEntity.setCity(params.get("city")==null?"":params.get("city").toString());
            userGroupEntity.setRemark(params.get("remark")==null?"":params.get("remark").toString());
        }else if("1".equals(leavel.toString())){
            if(null == params.get("pid") || "".equals(params.get("pid"))){
                throw new ParamException("参数pid不能为空");
            }
            //组长Id
             groupLeaderUserId = String.valueOf(params.get("groupLeaderUserId") != null ? params.get("groupLeaderUserId") : "");
            if (params.get("userIds") != null) {
                userIds = (List<String>) params.get("userIds");
            }
            userGroupEntity.setPid(params.get("pid")==null?"":params.get("pid").toString());
        }
        ResponseCommon responseCommon = new ResponseCommon();
        int result = userGroupService.addUserGroup(userGroupEntity, userIds, groupLeaderUserId);
        if (result == 1) {
            return responseCommon.success();
        }
        return responseCommon.fail();
    }

    @RequestMapping(value = "/getUserGroup", method = RequestMethod.GET)
    public ResponseJson addUserGroup(String groupId) {
        ResponseJson responseJson = new ResponseJson();
        if (StringUtil.isEmpty(groupId)) {
            LOG.error("groupId不能为空");
            return responseJson;
        }
        CustomerUserGroupDTO result = userGroupService.getUserGroup(groupId);
        responseJson.setData(result);
        return responseJson;
    }

    /**
     * 设置组长
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/setGroupLeader", method = RequestMethod.POST)
    public ResponseCommon addGroupLeader(@RequestBody Map<String, Object> params) {
        //组长Id
        String userId = String.valueOf(params.get("groupLeaderUserId"));
        String groupId = String.valueOf(params.get("groupId"));
        int result = 0;
        try {
            result = userGroupService.addGroupLeader(groupId, userId);
        } catch (Exception e) {
            result = 0;
            LOG.error("设置组长失败,", e);
        }
        ResponseCommon responseCommon = new ResponseCommon();
        if (result == 1) {
            return responseCommon.success();
        }
        return responseCommon.fail();
    }

    /**
     * 更新用户群组
     *
     * @param userGroupEntity
     * @return int
     * @author chengning@salescomm.net
     * @date 2018/9/27 14:14
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseCommon updateUserGroup(@RequestBody CustomerUserGroup userGroupEntity) {
        userGroupEntity.setCustId(opUser().getCustId());
        int result = userGroupService.updateUserGroup(userGroupEntity);
        ResponseCommon responseCommon = new ResponseCommon();
        if (result == 1) {
            return responseCommon.success();
        }
        return responseCommon.fail();
    }

    /**
     * 删除用户群组
     *
     * @param userGroupEntity
     * @return int
     * @author chengning@salescomm.net
     * @date 2018/9/27 14:12
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseCommon deleteUserGroup(CustomerUserGroup userGroupEntity) {
        int result = userGroupService.deleteUserGroup(userGroupEntity);
        ResponseCommon responseCommon = new ResponseCommon();
        if (result == 1) {
            return responseCommon.success();
        }
        return responseCommon.fail();
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseJson search(CustomerUserGroup userGroupEntity, @Valid PageParam pageParam, BindingResult error) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        if(userGroupEntity.getLeavel()==null){
            throw new ParamException("参数leavel不能为空");
        }

        userGroupEntity.setCustId(opUser().getCustId());
        Page result = userGroupService.searchList(pageParam.getPageNum(), pageParam.getPageSize(), userGroupEntity, opUser());
        responseJson.setData(getPageData(result));
        return responseJson;
    }

    @RequestMapping(value = "/listCustomerUserGroup", method = RequestMethod.GET)
    public ResponseJson listCustomerUserGroup(CustomerUserGroup userGroupEntity,String custId) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser lu=opUser();
        if("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())){
            if(StringUtil.isNotEmpty(custId)){
                userGroupEntity.setCustId(custId);
            }else{
                throw new ParamException("custId参数不能为空");
            }
        }else{
            userGroupEntity.setCustId(opUser().getCustId());
        }
        if(userGroupEntity.getLeavel()==null){
            throw new ParamException("leavel参数不能为空");
        }
        List<CustomerUserGroupDTO> result = userGroupService.listCustomerUserGroup(userGroupEntity, lu);
        responseJson.setData(result);
        return responseJson;
    }

    /**
     * 查询群组下已选择员工列表和客户下未选择的员工列表
     *
     * @param groupId
     * @return
     */
    @RequestMapping(value = "/listSelectUserByGroupId", method = RequestMethod.GET)
    public ResponseJson listUserByGroupId(String groupId, String sStartAccount, String sEndAccount, String uStartAccount, String uEndAccount) {
        ResponseJson responseJson = new ResponseJson();
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("selectList", userGroupService.listSelectCustomerUserByUserGroupId(groupId, opUser().getCustId(), sStartAccount, sEndAccount));
            result.put("unSelectList", userGroupService.listNotInUserGroupByCustomerId(opUser().getCustId(), uStartAccount, uEndAccount));
        } catch (Exception e) {
            LOG.error("查询群组下已选择员工列表和客户下未选择的员工列表失败,", e);
            result.put("selectList", null);
            result.put("unSelectList", null);
        }
        responseJson.setData(result);
        return responseJson;
    }

    @RequestMapping(value = "/distributionGroupUser", method = RequestMethod.POST)
    public ResponseCommon distributionGroupUser(@RequestBody Map<String, Object> param) {
        String groupId = null;
        List<String> userIds = null;
        if (param.get("groupId") != null) {
            groupId = String.valueOf(param.get("groupId"));
        }
        if (param.get("userIds") != null) {
            userIds = (List<String>) param.get("userIds");
        }
        ResponseCommon responseCommon = new ResponseCommon();
        int result = userGroupService.distributionGroupUser(groupId, userIds);
        if (result == 1) {
            return responseCommon.success();
        }
        return responseCommon.fail();
    }

    @RequestMapping(value = "/listUserByGroupId", method = RequestMethod.GET)
    public ResponseJson listUserByGroupId1(String groupId) {
        ResponseJson responseJson = new ResponseJson();
        List<CustomerUserDTO> users;
        try {
            users = userGroupService.listSelectCustomerUserByUserGroupId(groupId, opUser().getCustId(), null, null);
        } catch (Exception e) {
            LOG.error("查询群组下员工列表失败,", e);
            users = new ArrayList<>();
        }
        responseJson.setData(users);
        return responseJson;
    }

    /**
     * 职场结算价列表查询 list
     * @param projectId
     * @param customerId
     * @param jobId
     * @param pageParam
     * @param error
     * @return
     */
    @RequestMapping(value = "/jobsettlementpricelist", method = RequestMethod.GET)
    public ResponseJson jobSettlementPriceList(String serviceCode, String projectId, String customerId, String jobId, String billDate, @Valid PageParam pageParam, BindingResult error) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        LoginUser lu = opUser();
        if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
            return responseJson;
        }

        Page result = userGroupService.searchSettlementList(serviceCode,pageParam.getPageNum(), pageParam.getPageSize(),projectId ,customerId,jobId,billDate,lu);
        responseJson.setData(getPageData(result));
        return responseJson;
    }

    /**
     * 设置售价/修改售价,结算单录入，项目审单录入
     * @param infoDTO
     * @return
     */
    @RequestMapping(value = "/settingSettlementPrice", method = RequestMethod.POST)
    public ResponseJson settingSettlementPrice(@RequestBody CommonInfoDTO infoDTO) {
        ResponseJson responseJson = new ResponseJson();
        if (StringUtil.isEmpty(infoDTO.getServiceCode())) {
            throw new ParamException("参数serviceCode不能为空");
        }
        if (StringUtil.isEmpty(infoDTO.getProjectId())) {
            throw new ParamException("参数projectId不能为空");
        }
        if (StringUtil.isEmpty(infoDTO.getCustId())) {
            throw new ParamException("参数custId不能为空");
        }
        if(infoDTO.getServiceCode().equals(CommonInfoServiceCodeEnum.SETTING_JOB_SETTLEMENT_PRICE.getKey().toString())) {
            if (StringUtil.isEmpty(infoDTO.getJobId())) {
                throw new ParamException("参数jobId不能为空");
            }
        }
        LoginUser lu = opUser();
        if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
            responseJson.setCode(-1);
            responseJson.setMessage("无权操作");
            return responseJson;
        }
        try {
            userGroupService.settingSettlementPrice(infoDTO,lu);
            responseJson.setCode(0);
            responseJson.setMessage("操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage("操作失败");
            return responseJson;
        }
        return responseJson;
    }


}
