package com.bdaim.crm.erp.oa.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.interceptor.ClassTypeCheck;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.entity.OaLog;
import com.bdaim.crm.erp.oa.entity.OaLogRelation;
import com.bdaim.crm.erp.oa.service.OaLogService;
import com.bdaim.crm.erp.work.service.TaskService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * OA日志模块
 */
@RestController
@RequestMapping("/OaLog")
public class OaLogController extends BasicAction {

    @Resource
    private OaLogService oaLogService;
    @Resource
    private TaskService taskService;

    /**
     * 分页条件查询日志
     * @author zhangzhiwei
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    @ClassTypeCheck(classType = OaLog.class)
    public R queryList(BasePageRequest<OaLog> basePageRequest){
        Page<Record> recordList=oaLogService.queryList(basePageRequest);
        return(R.ok().put("data",recordList));
    }

    /**
     * 根据日志id获取日志
     * @param logId 日志ID
     * @author zhangzhiwei
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById(@Para("logId") Integer logId){
        Record record=oaLogService.queryById(logId);
        return(R.ok().put("data",record));
    }
    /**
     * 添加日志
     * @author zhangzhiwei
     */
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public R addOrUpdate(@RequestBody JSONObject jsonObject){
        return(oaLogService.saveAndUpdate(jsonObject));
    }

    /**
     * 根据日志id删除日志
     * @param logId 日志ID
     * @author zhangzhiwei
     */
    @RequestMapping(value = "/deleteById", method = RequestMethod.POST)
    public R deleteById(@Para("logId") Integer logId){
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.LOG_TYPE_KEY.getTypes(), logId);
        if(oaAuth){return(R.noAuth());
        //return;
        }
        return(oaLogService.deleteById(logId) ? R.ok() : R.error("删除失败"));
    }

    /**
     * 日志设为已读
     * @param logId 日志ID
     * @author zhangzhiwei
     */
    @RequestMapping(value = "/readLog", method = RequestMethod.POST)
    public R readLog(@Para("logId") Integer logId){
        oaLogService.readLog(logId);
        return(R.ok());
    }

    /**
     * 查询crm关联日志
     */
    @RequestMapping(value = "/queryLogRelation", method = RequestMethod.POST)
    public R queryLogRelation(BasePageRequest<OaLogRelation> basePageRequest,OaLogRelation oaLogRelation){
        basePageRequest.setData(oaLogRelation);
        return(oaLogService.queryLogRelation(basePageRequest));
    }
}
