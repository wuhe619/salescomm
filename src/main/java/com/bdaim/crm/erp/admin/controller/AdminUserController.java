package com.bdaim.crm.erp.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.annotation.ClassTypeCheck;
import com.bdaim.crm.entity.LkCrmAdminUserEntity;
import com.bdaim.crm.erp.admin.entity.AdminUser;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.erp.admin.service.LkAdminUserService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Chacker
 */
@RestController
@RequestMapping("/system/user")
public class AdminUserController extends BasicAction {

    @Resource
    private LkAdminUserService adminUserService;

    @Resource
    private AdminFileService adminFileService;

    /**
     * @param adminUser
     * @author Chacker
     * 设置系统用户
     */
    @Permissions("manage:user")
    @RequestMapping(value = "/setUser")
    public R setUser(LkCrmAdminUserEntity adminUser) {
//        renderJson(adminUserService.setUser(adminUser, getPara("roleIds")));
        return adminUserService.setUser(adminUser, getPara("roleIds"));
    }

    /**
     * @author Chacker
     * 更新状态
     */
    @Permissions("manage:user")
    @RequestMapping(value = "/setUserStatus")
    public R setUserStatus() {
        String ids = getPara("userIds");
        String status = getPara("status");
//        renderJson(adminUserService.setUserStatus(ids, status));
        return adminUserService.setUserStatus(ids, status);
    }

    /**
     * @param basePageRequest 分页对象
     * @author Chacker
     * 查询系统用户列表
     */
    @ResponseBody
    @RequestMapping(value = "/queryUserList", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R queryUserList(BasePageRequest basePageRequest, AdminUser adminUser, String roleId, String roleName) {
        //String roleId = getPara("roleId");
        basePageRequest.setData(adminUser);
//        renderJson(adminUserService.queryUserList(basePageRequest, roleId));
        return adminUserService.queryUserList(basePageRequest, roleId, roleName);
    }

    /**
     * @author Chacker
     * 重置密码
     */
    @Permissions("manage:user")
    @RequestMapping(value = "/resetPassword")
    public R resetPassword() {
        String ids = getPara("userIds");
        String pwd = getPara("password");
//        renderJson(adminUserService.resetPassword(ids, pwd));
        return adminUserService.resetPassword(ids, pwd);
    }

    /**
     * @author Chacker
     * 查询上级列表
     */
    @Permissions("manage:user")
    @RequestMapping(value = "/querySuperior")
    public R querySuperior() {
        String realName = getPara("realName");
//        renderJson(adminUserService.querySuperior(realName));
        return adminUserService.querySuperior(realName);
    }

    /**
     * 查询所用用户列表
     *
     * @author Chacker
     */
    @Permissions("manage:user")
    @RequestMapping(value = "/queryAllUserList")
    public R queryAllUserList() {
//        renderJson(adminUserService.queryAllUserList());
        return adminUserService.queryAllUserList();
    }

    /**
     * @author zxy
     * 查询系统所有用户名称
     */
    @RequestMapping(value = "/queryListName")
    public R queryListName(@Para("search") String search) {
//        renderJson(adminUserService.queryListName(search));
        return adminUserService.queryListName(search);
    }

    /**
     * @author zxy
     * 查询部门属用户列表
     */
    @RequestMapping(value = "/queryListNameByDept")
    public R queryListNameByDept(@Para("name") String name) {
//        renderJson(adminUserService.queryListNameByDept(name));
        return adminUserService.queryListNameByDept(name);
    }

    /**
     * 查询当前登录的用户
     *
     * @author zhangzhiwei
     */
    @ResponseBody
    @RequestMapping(value = "/queryLoginUser", method = RequestMethod.POST)
    public R queryLoginUser() {
        ResponseInfo resp = new ResponseInfo();
        resp.setData(adminUserService.resetUser());
        LkCrmAdminUserEntity lkCrmAdminUserEntity = adminUserService.resetUser();
        Map map = BeanUtil.beanToMap(lkCrmAdminUserEntity);
        map.put("userId", String.valueOf(map.get("userId")));
        map.remove("salt");
        map.remove("num");
        return (R.ok().put("data", map));
        //renderJson(R.ok().put("data",adminUserService.resetUser()));
    }

    @RequestMapping(value = "/updateImg")
    public R updateImg() {
        String prefix = BaseUtil.getDate();
        //UploadFile uploadFile = getFile("file", prefix);
        R r = adminFileService.upload0(BaseUtil.getRequest(), null, "file", "/" + prefix);
        if (r.isSuccess()) {
            String url = (String) r.get("url");
            if (adminUserService.updateImg(url, getLong("userId"))) {
//                renderJson(R.ok());
                return R.ok();
            }
        }
//        renderJson(R.error("修改头像失败"));
        return R.error("修改头像失败");
    }
    /*public void updatePassword(){
        String oldPass=getPara("oldPwd");
        String newPass=getPara("newPwd");
        LoginUser adminUser=BaseUtil.getUser();
        if(!BaseUtil.verify(adminUser.getUsername()+oldPass,adminUser.getSalt(),adminUser.getPassword())){
            renderJson(R.error("密码输入错误"));
            return;
        }
        adminUser.setPassword(newPass);
        boolean b=adminUserService.updateUser(adminUser);
        if(b){
            RedisManager.getRedis().del(BaseUtil.getToken());
            removeCookie("Admin-Token");
        }
        renderJson(R.isSuccess(b));
    }*/

    @NotNullValidate(value = "realname", message = "姓名不能为空")
    @NotNullValidate(value = "username", message = "用户名不能为空")
    @RequestMapping(value = "/updateUser")
    public R updateUser(@Para("") LkCrmAdminUserEntity adminUser) {
        boolean b = adminUserService.updateUser(adminUser);
//        renderJson(R.isSuccess(b, "修改信息失败"));
        return R.isSuccess(b, "修改信息失败");
    }

    /**
     * @param id       用户ID
     * @param username 用户新账号
     * @param password 用户新密码
     * @author zhangzhiwei
     */
    @Permissions("manage:user")
    @NotNullValidate(value = "username", message = "账号不能为空")
    @NotNullValidate(value = "password", message = "密码不能为空")
    @NotNullValidate("id")
    @RequestMapping(value = "/usernameEdit")
    public R usernameEdit(@Para("id") Long id, @Para("username") String username, @Para("password") String password) {
//        renderJson(adminUserService.usernameEdit(id, username, password));
        return adminUserService.usernameEdit(id, username, password);
    }

    @RequestMapping(value = "/queryUserByDeptId")
    public R queryUserByDeptId(@Para("deptId") Integer deptId) {
//        renderJson(R.ok().put("data", adminUserService.queryUserByDeptId(deptId)));
//        ;
        return R.ok().put("data", adminUserService.queryUserByDeptId(deptId));
    }
}
