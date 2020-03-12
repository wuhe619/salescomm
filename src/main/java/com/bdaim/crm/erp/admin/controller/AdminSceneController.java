package com.bdaim.crm.erp.admin.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.interceptor.ClassTypeCheck;
import com.bdaim.crm.entity.LkCrmAdminSceneEntity;
import com.bdaim.crm.erp.admin.entity.AdminScene;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.utils.R;
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
public class AdminSceneController extends BasicAction {
    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @param label 模块类型
     *              查询场景字段
     * @author wyq
     */
    @NotNullValidate(value = "label", message = "label不能为空")
    @RequestMapping(value = "/queryField", method = RequestMethod.POST)
    public R queryField(@Para("label") Integer label) {
        return (adminSceneService.queryField(label));
    }

    /**
     * @author wyq
     * 增加场景
     */
    @NotNullValidate(value = "type", message = "type不能为空")
    @NotNullValidate(value = "name", message = "场景名称不能为空")
    @NotNullValidate(value = "data", message = "data不能为空")
    @RequestMapping(value = "/addScene", method = RequestMethod.POST)
    public R addScene(@Para("") LkCrmAdminSceneEntity adminScene) {
        return (adminSceneService.addScene(adminScene));
    }

    /**
     * @author wyq
     * 更新场景
     */
    @NotNullValidate(value = "sceneId", message = "场景id不能为空")
    @RequestMapping(value = "/updateScene", method = RequestMethod.POST)
    public R updateScene(@Para("") LkCrmAdminSceneEntity adminScene) {
        return (adminSceneService.updateScene(adminScene));
    }

    /**
     * @param sceneId 场景id
     *                设置默认场景
     * @author wyq
     */
    @NotNullValidate(value = "sceneId", message = "场景id不能为空")
    @RequestMapping(value = "/setDefaultScene", method = RequestMethod.POST)
    public R setDefaultScene(@Para("sceneId") Integer sceneId) {
        return (adminSceneService.setDefaultScene(sceneId));
    }

    /**
     * @author wyq
     * 删除场景
     */
    @NotNullValidate(value = "sceneId", message = "场景id不能为空")
    @RequestMapping(value = "/deleteScene", method = RequestMethod.POST)
    public R deleteScene(@Para("") AdminScene adminScene) {
        return (adminSceneService.deleteScene(adminScene));
    }

    /**
     * @author wyq
     * 查询场景
     */
    @NotNullValidate(value = "type", message = "type不能为空")
    @ResponseBody
    @RequestMapping(value = "/queryScene", method = RequestMethod.POST)
    public R queryScene(@Para("type") Integer type) {
        return (renderCrmJson(adminSceneService.queryScene(type)));
    }

    /**
     * @author wyq
     * 查询场景设置
     */
    @NotNullValidate(value = "type", message = "type不能为空")
    @RequestMapping(value = "/querySceneConfig", method = RequestMethod.POST)
    public R querySceneConfig(@Para("") AdminScene adminScene) {
        return (adminSceneService.querySceneConfig(adminScene));
    }

    /**
     * @author wyq
     * 设置场景
     */
    @NotNullValidate(value = "type", message = "type不能为空")
    @NotNullValidate(value = "noHideIds", message = "显示场景不能为空")
    @RequestMapping(value = "/sceneConfig", method = RequestMethod.POST)
    public R sceneConfig(@Para("") AdminScene adminScene) {
        return (adminSceneService.sceneConfig(adminScene));
    }

    /**
     * @author wyq
     * Crm列表页查询
     */
    @RequestMapping(value = "/queryPageList", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R queryPageList(BasePageRequest basePageRequest) {
        return (adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }
}
