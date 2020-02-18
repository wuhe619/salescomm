package com.bdaim.crm.erp.oa.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.oa.entity.OaAnnouncement;
import com.bdaim.crm.erp.oa.service.OaAnnouncementService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;

import javax.annotation.Resource;

/**
 * 公告
 * @author zxy
 */
public class OaAnnouncementController extends Controller {
    @Resource
    private OaAnnouncementService announcementService;
    /**
     * 添加或修改
     */
    public void saveAndUpdate(@Para("") OaAnnouncement oaAnnouncement){
        oaAnnouncement.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        renderJson(announcementService.saveAndUpdate(oaAnnouncement));
    }
    /**
     * 删除
     */
    public void delete(@Para("id") Integer id){
        renderJson(announcementService.delete(id));
    }
    /**
     * 根据ID查询详情
     */
    public void queryById(@Para("id") Integer id){
        renderJson(announcementService.queryById(id));
    }
    /**
     * 倒叙查询公告集合
     */
    public void queryList(BasePageRequest<OaAnnouncement> basePageRequest,@Para("type") Integer type){
        renderJson(announcementService.queryList(basePageRequest,type));
    }

    /**
     * 公告设为已读
     * @param announcementId 公告ID
     * @author wyq
     */
    public void readAnnouncement(@Para("announcementId") Integer announcementId){
        announcementService.readAnnouncement(announcementId);
        renderJson(R.ok());
    }
}
