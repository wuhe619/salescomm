package com.bdaim.crm.erp.admin.controller;

import com.bdaim.crm.erp.admin.entity.AdminFile;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.core.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/file")
public class AdminFileController extends Controller {
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
     * @author zhangzhiwei
     * 通过ID查询
     */
    public void queryById() {
        renderJson(adminFileService.queryById(getPara("id")));
    }

    /**
     * @author zhangzhiwei
     * 通过ID删除
     */
    public void removeById() {
        renderJson(adminFileService.removeById(getPara("id")));
    }

    /**
     * @author zhangzhiwei
     * 通过批次ID删除
     */
    public void removeByBatchId() {
        adminFileService.removeByBatchId(getPara("batchId"));
        renderJson(R.ok());
    }

    /**
     * @author zhangzhiwei
     * 重命名文件
     */
    public void renameFileById() {
        AdminFile file = new AdminFile();
        file.setFileId(getInt("fileId"));
        file.setName(getPara("name"));
        renderJson(adminFileService.renameFileById(file) ? R.ok() : R.error());
    }
}
