package com.bdaim.crm.erp.admin.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.entity.LkCrmAdminFileEntity;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.NumberConvertUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/file")
public class AdminFileController extends BasicAction {
    @Resource
    private AdminFileService adminFileService;

    @RequestMapping(value = "index", method = RequestMethod.POST)
    public R index() {
        return (R.ok());
    }

    /**
     * @author zhangzhiwei
     * 上传附件
     */
    @RequestMapping(value = "upload", method = RequestMethod.POST)
    public R upload(String batchId, String type) {
        String prefix = BaseUtil.getDate();
        return (adminFileService.upload0(BaseUtil.getRequest(), batchId, type, prefix));
    }

    /**
     * @author zhangzhiwei
     * 通过批次ID查询
     */
    @RequestMapping(value = "/queryByBatchId", method = RequestMethod.POST)
    public R queryByBatchId(String batchId) {
        return (R.ok().put("data", adminFileService.queryByBatchId(batchId)));
    }

    /**
     * 通过ID查询
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById() {
        return(adminFileService.queryById(getPara("id")));
    }

    /**
     * 通过ID删除
     */
    @RequestMapping(value = "/removeById", method = RequestMethod.POST)
    public R removeById() {
        return(adminFileService.removeById(getPara("id")));
    }

    /**
     * 通过批次ID删除
     */
    @RequestMapping(value = "/removeByBatchId", method = RequestMethod.POST)
    public R removeByBatchId() {
        adminFileService.removeByBatchId(getPara("batchId"));
        return (R.ok());
    }

    /**
     * 重命名文件
     */
    @RequestMapping(value = "/renameFileById", method = RequestMethod.POST)
    public R renameFileById() {
        LkCrmAdminFileEntity file = new LkCrmAdminFileEntity();
        file.setFileId(NumberConvertUtil.parseInt(getPara("fileId")));
        file.setName(getPara("name"));
        return (adminFileService.renameFileById(file) ? R.ok() : R.error());
    }
}
