package com.bdaim.crm.erp.admin.controller;

import com.bdaim.common.response.ResponseInfo;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.admin.entity.AdminScene;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.utils.R;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wyq
 */
@RestController
@RequestMapping("/scene")
public class AdminSceneController extends Controller {
    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * @param label 模块类型
     * 查询场景字段
     */
    @NotNullValidate(value = "label",message = "label不能为空")
    public void queryField(@Para("label") Integer label){
        renderJson(adminSceneService.queryField(label));
    }

    /**
     * @author wyq
     * 增加场景
     */
    @NotNullValidate(value = "type",message = "type不能为空")
    @NotNullValidate(value = "name",message = "场景名称不能为空")
    @NotNullValidate(value = "data",message = "data不能为空")
    public void addScene(@Para("") AdminScene adminScene){
        renderJson(adminSceneService.addScene(adminScene));
    }

    /**
     * @author wyq
     * 更新场景
     */
    @NotNullValidate(value = "sceneId",message = "场景id不能为空")
    public void updateScene(@Para("")AdminScene adminScene){
        renderJson(adminSceneService.updateScene(adminScene));
    }

    /**
     * @author wyq
     * @param sceneId 场景id
     * 设置默认场景
     */
    @NotNullValidate(value = "sceneId",message = "场景id不能为空")
    public void setDefaultScene(@Para("sceneId") Integer sceneId){
        renderJson(adminSceneService.setDefaultScene(sceneId));
    }

    /**
     * @author wyq
     * 删除场景
     */
    @NotNullValidate(value = "sceneId",message = "场景id不能为空")
    public void deleteScene(@Para("")AdminScene adminScene){
        renderJson(adminSceneService.deleteScene(adminScene));
    }

    /**
     * @author wyq
     * 查询场景
     */
    @NotNullValidate(value = "type",message = "type不能为空")
    @ResponseBody
    @RequestMapping(value = "/queryScene", method = RequestMethod.POST)
    public R queryScene(@Para("type") Integer type){
        ResponseInfo resp = new ResponseInfo();
        //renderJson(adminSceneService.queryScene(type));
        return (adminSceneService.queryScene(type));
        //return resp;
    }

    /**
     * @author wyq
     * 查询场景设置
     */
    @NotNullValidate(value = "type",message = "type不能为空")
    public void querySceneConfig(@Para("") AdminScene adminScene){
        renderJson(adminSceneService.querySceneConfig(adminScene));
    }

    /**
     * @author wyq
     * 设置场景
     */
    @NotNullValidate(value = "type",message = "type不能为空")
    @NotNullValidate(value = "noHideIds",message = "显示场景不能为空")
    public void sceneConfig(@Para("") AdminScene adminScene){
        renderJson(adminSceneService.sceneConfig(adminScene));
    }

    /**
     * @author wyq
     * Crm列表页查询
     */
    public void queryPageList(BasePageRequest basePageRequest){
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }
}
