package com.bdaim.online.unicom.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.service.impl.SeatsService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.online.unicom.service.UnicomService;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.util.IDHelper;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/unicom")
public class UnicomAction extends BasicAction {

    public static final Logger LOG = LoggerFactory.getLogger(UnicomAction.class);
    @Resource
    private CustomerSeaService customerSeaService;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private SeatsService seatsService;
    @Resource
    private CustomerService customerService;
    @Resource
    private UnicomService unicomService;

    /**
     * 联通外呼
     *
     * @param superidlist
     * @param customerGroupId
     * @param marketTaskId
     * @param seaId
     * @return
     */
    @RequestMapping(value = "/markCall", method = RequestMethod.POST)
    public String useMarketResource(String superidlist, String customerGroupId, String marketTaskId, String seaId) {
        String touchId = opUser().getId() + IDHelper.getTouchId().toString();
        Map<Object, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        int code = 1;
        String message = "调用为成功";
        String superId = superidlist;
        // 查询用户是否设置主叫号码
        String workNum = marketResourceService.selectPhoneSet(opUser().getId());
        if (StringUtil.isEmpty(workNum) || "000".equals(workNum)) {
            map.put("message", "未设主叫置电话或者主叫电话状态异常");
            map.put("code", "1004");
            json.put("data", map);
            return json.toJSONString();
        }
        String resourceId = null;
        try {
            resourceId = seatsService.checkSeatConfigStatus(String.valueOf(opUser().getId()), opUser().getCustId());
        } catch (Exception e) {
            LOG.error("获取用户配置的呼叫渠道ID异常,", e);
        }
        // 查询企业是否设置双向呼叫外显号码
        String showNumber = customerService.getCustomerApparentNumber(opUser().getCustId(), "", resourceId);
        if (StringUtil.isEmpty(showNumber)) {
            showNumber = customerService.getCustomerApparentNumber(opUser().getCustId(), "");
            if (StringUtil.isEmpty(showNumber)) {
                // 穿透查询一次之前配置的外显号
                showNumber = marketResourceService.selectCustCallBackApparentNumber(opUser().getCustId());
            }
        }
        if (StringUtil.isEmpty(showNumber)) {
            map.put("message", "未申请外显号码");
            map.put("code", "1006");
            json.put("data", map);
            return json.toJSONString();
        }
        // 判断是余额是否充足
        boolean amountStatus = marketResourceService.judRemainAmount(opUser().getCustId());
        if (!amountStatus) {
            map.put("message", "余额不足");
            map.put("code", "1003");
            json.put("data", map);
            return json.toJSONString();
        }
        boolean success = false;
        // 联通外呼
        JSONObject callResult = unicomService.unicomSeatMakeCall(opUser().getCustId(), opUser().getId(), superId, showNumber);
        // {"msg":"success","code":"01000","data":{"callId":"1539516627199289733","msg":"呼叫成功","code":"000","uuid":"01539516627199289733","callNum":"1/600","todayCallNum":"1/30","monthCallNum":"1/90"}}
        LOG.info("调用联通双呼接口返回:{}", callResult);
        // 更新通话次数(客戶群和公海的)
        if (StringUtil.isNotEmpty(customerGroupId)) {
            LOG.info("客户群id是：" + customerGroupId);
            marketResourceService.updateCallCountV2(customerGroupId, superId);
        } else {
            //根据公海id还有supplier查询客户群id
            Map<String, Object> customerSeaBysupplierId = customerSeaService.getCustomerSeaBysupplierId(seaId, superId);
            if (customerSeaBysupplierId != null) {
                customerGroupId = String.valueOf(customerSeaBysupplierId.get("batch_id"));
                LOG.info("公海详情的用户群id是：" + customerGroupId);
            }
        }
        if (StringUtil.isNotEmpty(seaId)) {
            LOG.info("公海id是：" + seaId);
            marketResourceService.updateSeaCallCount(seaId, superId);
        }
        MarketResourceLogDTO dto = new MarketResourceLogDTO(touchId, opUser().getCustId(), opUser().getId(), "1",
                "voice", "", "", 1002, "", "", superId, null, "", "", null, null);
        // 发送双向呼叫请求成功
        if (callResult != null && "01000".equals(callResult.getString("code"))) {
            // 唯一请求ID
            dto.setCallSid(callResult.getJSONObject("data").getString("uuid"));
            dto.setStatus(1001);
            code = 10000;
            message = "电话已经拨打";
        } else {
            // 如果失败则把错误信息返回
            LOG.warn("请求发送双向呼叫失败,返回数据:{}", callResult);
            message = callResult.getString("msg");
            // 主叫失败
            dto.setStatus(1002);
        }
        // 当前登录人所属的职场ID
        dto.setCugId(opUser().getJobMarketId());
        // 客群ID
        dto.setCustomerGroupId(NumberConvertUtil.parseInt(customerGroupId));
        // 营销任务Id
        dto.setMarketTaskId(StringUtil.isNotEmpty(marketTaskId) ? marketTaskId : "");
        // 公海ID
        dto.setCustomerSeaId(StringUtil.isNotEmpty(seaId) ? seaId : "");
        // 判断是否管理员进行的外呼
        dto.setCallOwner("1".equals(opUser().getUserType()) ? 2 : 1);
        LOG.info("联通双呼保存数据库数据:{}", JSON.toJSONString(dto));
        marketResourceService.insertLogV3(dto);
        map.put("tranOrderId", touchId);
        map.put("code", code);
        map.put("message", message);
        json.put("data", map);
        return json.toJSONString();
    }
}