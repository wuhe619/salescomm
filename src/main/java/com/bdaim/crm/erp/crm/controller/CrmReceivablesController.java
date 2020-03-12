package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.interceptor.ClassTypeCheck;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.erp.crm.entity.CrmReceivables;
import com.bdaim.crm.erp.crm.service.CrmReceivablesService;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 回款
 */
@RestController
@RequestMapping("/CrmReceivables")
public class CrmReceivablesController extends BasicAction {

    @Resource
    private CrmReceivablesService crmReceivablesService;

    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:receivables:index"})
    @RequestMapping(value = "queryPageList", method = RequestMethod.POST)
    public R queryPageList(@RequestBody JSONObject jsonObject) {
        BasePageRequest<Void> basePageRequest = new BasePageRequest<>(jsonObject.getIntValue("page"),jsonObject.getIntValue("limit"));
        jsonObject.fluentPut("type", 7);
        basePageRequest.setJsonObject(jsonObject);
        return (adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author zxy
     * 分页查询回款
     */
    @RequestMapping(value = "queryPage", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmReceivables.class)
    public R queryPage(BasePageRequest<CrmReceivables> basePageRequest) {
        return (R.ok().put("data", crmReceivablesService.queryPage(basePageRequest)));
    }

    /**
     * @author zxy
     * 添加或者修改
     */
    @Permissions({"crm:receivables:save", "crm:receivables:update"})
    @RequestMapping(value = "saveOrUpdate", method = RequestMethod.POST)
    public R saveOrUpdate(@RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = JSON.parseObject(getRawData());
        return (crmReceivablesService.saveOrUpdate(jsonObject));
    }

    /**
     * @author zxy
     * 根据回款id查询
     */
    @Permissions("crm:receivables:read")
    @NotNullValidate(value = "receivablesId", message = "回款id不能为空")
    @RequestMapping(value = "queryById", method = RequestMethod.POST)
    public R queryById(@RequestParam("receivablesId") Integer receivablesId) {
        return (crmReceivablesService.queryById(receivablesId));
    }

    /**
     * @author zxy
     * 根据回款id删除
     */
    @Permissions("crm:receivables:delete")
    @NotNullValidate(value = "receivablesIds", message = "回款id不能为空")
    @RequestMapping(value = "deleteByIds", method = RequestMethod.POST)
    public R deleteByIds(@RequestParam("receivablesIds") String receivablesIds) {
        return (crmReceivablesService.deleteByIds(receivablesIds));
    }

    /**
     * 根据条件查询回款
     *
     * @author zxy
     */
    @RequestMapping(value = "queryListByType", method = RequestMethod.POST)
    public R queryListByType(@RequestParam("type") String type, @RequestParam("id") Integer id) {
        return (R.ok().put("data", crmReceivablesService.queryListByType(type, id)));
    }

    /**
     * 根据条件查询回款
     *
     * @author zxy
     */
    @RequestMapping(value = "queryList", method = RequestMethod.POST)
    public R queryList(CrmReceivables receivables) {
        return (R.ok().put("data", crmReceivablesService.queryList(receivables)));
    }
}
