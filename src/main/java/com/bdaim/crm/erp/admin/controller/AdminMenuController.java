package com.bdaim.crm.erp.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.dao.LkCrmAdminMenuDao;
import com.bdaim.crm.entity.LkCrmAdminMenuEntity;
import com.bdaim.crm.erp.admin.service.AdminMenuService;
import com.bdaim.crm.utils.R;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/system/menu")
public class AdminMenuController extends BasicAction {
    @Resource
    private AdminMenuService adminMenuService;
    @Resource
    private LkCrmAdminMenuDao crmAdminMenuDao;

    /**
     * @param roleId 角色id
     *               根据角色id查询菜单id
     * @author wyq
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/getRoleMenu", method = RequestMethod.POST)
    public R getRoleMenu(@Para("roleId") Integer roleId) {
        return (R.ok().put("data", adminMenuService.getMenuIdByRoleId(roleId)));
    }

    /**
     * @author wyq
     * 展示全部菜单
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/getAllMenuList", method = RequestMethod.POST)
    public R getAllMenuList() {
        return (R.ok().put("data", adminMenuService.getAllMenuList(0, 20)));
    }

    /**
     * @author hmb
     * 展示全部菜单
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/getWorkMenuList", method = RequestMethod.POST)
    public R getWorkMenuList() {
        Integer workMenuId = crmAdminMenuDao.queryForInt("select menu_id from `lkcrm_admin_menu` where parent_id = 0 and realm = 'work'");
        LkCrmAdminMenuEntity entity = crmAdminMenuDao.get(workMenuId);
        Map root = BeanUtil.beanToMap(entity);
        root.put("childMenu", adminMenuService.getWorkMenuList(entity.getMenuId(), 20));
        return (R.ok().put("data", root));
    }
}
