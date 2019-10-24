package com.bdaim.dic.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.DicTypeEnum;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.dic.service.DicService;
import com.bdaim.fund.dto.SearchPropertyDTO;
import com.bdaim.fund.entity.FundProductApply;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/8/6
 * @description 字典公用
 */
@RestController
@RequestMapping("/dic")
public class DicAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(DicAction.class);

    @Resource
    private DicService dicService;

    @Resource
    private TokenCacheService tokenCacheService;

   /**
     * 根据id查询字典表信息
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ResponseInfo getDicProperty(Long id) {
        try {
            List<DicProperty> list = dicService.getDicProperty(id);
            return new ResponseInfoAssemble().success(list);
        } catch (Exception e) {
            logger.error("查询字典表信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询字典表信息异常");
        }
    }

    /**
     * 分页查询
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/page/query", method = RequestMethod.POST)
    public String getAccountList(@RequestBody SearchPropertyDTO searchPropertyDto) {
        Integer pageNum = searchPropertyDto.getPageNum();
        Integer pageSize = searchPropertyDto.getPageSize();
        ResponseJson responseJson = new ResponseJson();
        if (pageSize == null || pageNum == null) {
            responseJson.setMessage("参数错误");
            responseJson.setCode(-1);
            return JSON.toJSONString(responseJson);
        }
        JSONObject r = dicService.page(pageSize, pageNum, searchPropertyDto);
        responseJson.setData(r);
        responseJson.setCode(200);
        return JSON.toJSONString(responseJson);
    }

    /**
     * 根据属性id查询属性值
     *
     * @param typeId
     * @param typeProdId
     * @return
     */
    @RequestMapping(value = "/queryType", method = RequestMethod.GET)
    public String getAccountList(String typeId, String typeProdId) {
        ResponseJson responseJson = new ResponseJson();
        if (StringUtil.isEmpty(typeId) || StringUtil.isEmpty(typeProdId)) {
            responseJson.setMessage("参数不正确");
            responseJson.setCode(-1);
            return JSON.toJSONString(responseJson);
        }
        String dicProdValues = dicService.getDicProdValues(typeId, typeProdId);
        responseJson.setData(dicProdValues);
        responseJson.setCode(200);
        return JSON.toJSONString(responseJson);

    }

    /**
     * 保存
     *
     * @param body
     * @return
     */
    @ValidatePermission(role = "admin,ROLE_USER")
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String save(@RequestBody JSONObject body) {
        ResponseJson responseJson = new ResponseJson();
        try {
            // 检查名称是否存在
            synchronized (this) {
                if (DicTypeEnum.checkName(body.getString("dicTypeId"))) {
                    boolean b;
                    if (DicTypeEnum.isProductType(body.getString("dicTypeId"))) {
                        b = dicService.checkExistProductName(body.getLong("id"), body.getString("name"), body.getString("dicTypeId"), 0);
                    } else {
                        b = dicService.checkExistProductName(body.getLong("id"), body.getString("name"), body.getString("dicTypeId"), 1);
                    }
                    if (b) {
                        responseJson.setCode(-1);
                        responseJson.setMessage("已存在相同名称");
                        return JSON.toJSONString(responseJson);
                    }
                }
                String id = dicService.saveOrUpdate(body, opUser());
                responseJson.setData(id);
                responseJson.setCode(200);
                responseJson.setMessage("success");
            }
        } catch (Exception e) {
            logger.error("商品保存异常:", e);
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }


    /**
     * 查看详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public String getDetail(@PathVariable("id") Long id) {
        ResponseJson responseJson = new ResponseJson();
        try {
            JSONObject detail = dicService.getDetailById(id);
            responseJson.setData(detail);
            responseJson.setCode(200);
            responseJson.setMessage("success");
            return JSON.toJSONString(responseJson);
        } catch (Exception e) {
            logger.error("查看详情异常:", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
            return JSON.toJSONString(responseJson);
        }
    }

    /**
     * 申请产品
     *
     * @param body
     * @return
     */
    @RequestMapping(value = "/productApply", method = RequestMethod.POST)
    public String productApply(@RequestBody JSONObject body) {
        ResponseJson responseJson = new ResponseJson();
        try {
            FundProductApply fundOrder = new FundProductApply();
            fundOrder.setApplyValue(body.toJSONString());
            long userId = 0L;
            LoginUser userDetail = (LoginUser) tokenCacheService.getToken(request.getHeader("Authorization"));
            if (userDetail != null) {
                userId = userDetail.getId();
            }
            fundOrder.setUserId(userId);
            fundOrder.setMobilePhone(body.getString("phone"));
            // 产品ID
            fundOrder.setProductId(body.getString("productId"));
            // 申请来源
            fundOrder.setFromClient(body.getString("client"));
            // 渠道ID
            fundOrder.setChannel(body.getString("channel"));
            // 活动ID
            fundOrder.setActivityId(body.getString("activityId"));
            fundOrder.setLoanAmount(body.getString("loanAmount"));
            fundOrder.setLoanLate(body.getString("loanLate"));
            fundOrder.setLoanTerm(body.getString("loanTerm"));
            if (body.getJSONObject("applyValue") != null) {
                fundOrder.setApplyValue(body.getJSONObject("applyValue").toJSONString());
            }
            boolean status = dicService.saveProductApply(fundOrder);
            responseJson.setData(status);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            logger.error("商品申请异常:", e);
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 更新状态
     *
     * @param id
     * @param status
     * @return
     */
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ValidatePermission(role = "admin")
    public String updatePicStatus(@PathVariable("id") String id, String status) {
        ResponseJson responseJson = new ResponseJson();
        try {
            dicService.updatePicStatus(status, id);
            responseJson.setCode(200);
            responseJson.setMessage("success");
            return JSON.toJSONString(responseJson);
        } catch (Exception e) {
            logger.error("更新商品异常:", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
            return JSON.toJSONString(responseJson);
        }
    }


    /**
     * 商品申请列表
     */
    @RequestMapping(value = "/getApplyList", method = RequestMethod.GET)
    public String getProductApplyList(Integer pageNum, Integer pageSize, String productName, String productId, String productType, String userId, String type) {
        ResponseJson responseJson = new ResponseJson();
        try {
            if (pageSize == null || pageNum == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            JSONObject JSONobject = dicService.getProductApplyList(pageNum, pageSize, productName, productId, productType, userId, type);
            responseJson.setData(JSONobject);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            logger.error("查看商品申请列表异常：", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 商品申请详情
     */
    @RequestMapping(value = "/getApplyDetail", method = RequestMethod.GET)
    public String getProductApplyDetail(Integer pageNum, Integer pageSize, String id, String phone, String startTime, String stopTime, String channel, String status, String activityName, String productType, String userId) {
        ResponseJson responseJson = new ResponseJson();
        try {
            if (pageSize == null || pageNum == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            JSONObject JSONobject = dicService.getProductApplyDetail(pageNum, pageSize, id, phone, startTime, stopTime, channel, status, activityName, productType, userId);
            responseJson.setData(JSONobject);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            logger.error("查看商品申请详情异常：", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }


    /**
     * 申请用户详细信息
     */
    @RequestMapping(value = "/getUserDetail", method = RequestMethod.GET)
    public String getApplyUserDetail(String userId, String id) {
        ResponseJson responseJson = new ResponseJson();
        try {
            if (userId == null || id == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            Map<String, Object> userDetail = dicService.getUserDetail(userId, id);
            responseJson.setData(userDetail);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            logger.error("申请用户详细信息异常：", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }


    /**
     * 导出产品申请用户列表信息
     *
     * @return
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public String exportSettlementBill(HttpServletResponse response, String id, String phone, String startTime, String stopTime, String channel, String status) {
        return dicService.exportApplyInfo(response, id, phone, startTime, stopTime, channel, status);
    }

    /**
     * 广告位展示列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listShowAdSpace", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String listShowAdSpace(@RequestBody SearchPropertyDTO dto) {
        ResponseJson responseJson = new ResponseJson();
        List<Map<String, Object>> list;
        try {
            dto.setDicType(DicTypeEnum.E.getId());
            list = dicService.listShowAdSpace(dto);
            responseJson.setData(list);
            responseJson.setCode(200);
        } catch (Exception e) {
            logger.error("申请广告位展示列表异常:", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }


    /**
     * 添加意见反馈
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/saveFeedBack", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String saveFeedBack(@RequestBody JSONObject body) {
        ResponseJson responseJson = new ResponseJson();
        try {
            dicService.saveFeedBack(body);
            responseJson.setCode(200);
        } catch (Exception e) {
            logger.error("添加意见反馈异常:", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * @description 计算产品相关费用
     * @method
     * @date: 2019/7/3 19:14
     */
    @ResponseBody
    @RequestMapping(value = "/getLoanCost", method = RequestMethod.GET)
    public String getLoanCost(Integer loanAmount, Integer loanTerm, String productId) {
        ResponseJson responseJson = new ResponseJson();
        try {
            if (loanAmount == null || loanTerm == null || loanAmount < 0 || loanTerm < 0 || productId == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            Map<Object, Object> map = dicService.getLoanCost(loanAmount, loanTerm, productId);
            responseJson.setData(map);
            responseJson.setCode(200);
        } catch (Exception e) {
            logger.error("获取贷款费用异常:", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 获取品牌列表（品牌下有商品）
     *
     * @return
     * @para
     */
    @RequestMapping(value = "/getBrandList", method = RequestMethod.GET)
    public String getBrandList(String dicType) {
        ResponseJson responseJson = new ResponseJson();
        try {
            List<Map<String, Object>> brandList = dicService.getBrandList(dicType);
            responseJson.setData(brandList);
            responseJson.setCode(200);
            responseJson.setMessage("success");
            return JSON.toJSONString(responseJson);
        } catch (Exception e) {
            logger.error("更新商品异常:", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
            return JSON.toJSONString(responseJson);
        }
    }
}
