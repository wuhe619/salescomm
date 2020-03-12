package com.bdaim.crm.erp.oa.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.entity.LkCrmOaAnnouncementEntity;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.oa.entity.OaAnnouncement;
import com.bdaim.crm.erp.oa.service.OaAnnouncementService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 公告
 *
 * @author zxy
 */
@RestController
@RequestMapping(value = "/OaAnnouncement")
public class OaAnnouncementController extends BasicAction {

    @InitBinder
    protected void init(ServletRequestDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @Resource
    private OaAnnouncementService announcementService;

    /**
     * 添加或修改
     */
    @RequestMapping(value = "/saveAndUpdate")
    public R saveAndUpdate(LkCrmOaAnnouncementEntity oaAnnouncement) {
        oaAnnouncement.setCreateUserId(BaseUtil.getUser().getUserId());
//        renderJson(announcementService.saveAndUpdate(oaAnnouncement));
        return announcementService.saveAndUpdate(oaAnnouncement);
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/delete")
    public R delete(@Para("id") Integer id) {
//        renderJson(announcementService.delete(id));
        return announcementService.delete(id);
    }

    /**
     * 根据ID查询详情
     */
    @RequestMapping(value = "/queryById")
    public R queryById(@Para("id") Integer id) {
//        renderJson(announcementService.queryById(id));
        return announcementService.queryById(id);
    }

    /**
     * 倒叙查询公告集合
     */
    @RequestMapping(value = "/queryList")
    public R queryList(BasePageRequest<OaAnnouncement> basePageRequest, @Para("type") Integer type,OaAnnouncement oaAnnouncement) {
        basePageRequest.setData(oaAnnouncement);
//        renderJson(announcementService.queryList(basePageRequest,type));
        return announcementService.queryList(basePageRequest, type);
    }

    /**
     * 公告设为已读
     *
     * @param announcementId 公告ID
     * @author wyq
     */
    @RequestMapping(value = "/readAnnouncement")
    public R readAnnouncement(@Para("announcementId") Integer announcementId) {
        announcementService.readAnnouncement(announcementId);
//        renderJson(R.ok());
        return R.ok();
    }
}
