package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.service.CrmRecordService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;

import javax.annotation.Resource;
import java.util.List;


public class CrmRecordController extends Controller {

    @Resource
    private CrmRecordService crmRecordService;

    /**
     * @author hmb
     * 查询操作记录列表
     */
    public void queryRecordList(){
        String actionId = getPara("actionId");
        String types = getPara("types");
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.getSign(Integer.valueOf(types))), Integer.valueOf(actionId));
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmRecordService.queryRecordList(actionId,types));
    }

    /**
     * @author wyq
     * 删除跟进记录
     */
    public void deleteFollowRecord(@Para("recordId") Integer recordId){
        renderJson(crmRecordService.deleteFollowRecord(recordId));
    }

    /**
     * @author wyq
     * 跟进记录类型设置
     */
    public void queryRecordOptions(){
        renderJson(crmRecordService.queryRecordOptions());
    }

    /**
     * @author wyq
     * 设置跟进记录类型
     */
    public void setRecordOptions(){
        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("value"));
        List<String> list = jsonArray.toJavaList(String.class);
        renderJson(crmRecordService.setRecordOptions(list));
    }
}
