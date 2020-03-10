package com.bdaim.crm.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.LkCrmAdminRoleDao;
import com.bdaim.crm.dao.LkCrmOaAnnouncementDao;
import com.bdaim.crm.entity.LkCrmAdminRoleEntity;
import com.bdaim.crm.entity.LkCrmOaAnnouncementEntity;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.entity.OaAnnouncement;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 公告
 */
@Service
@Transactional
public class OaAnnouncementService {

    //添加日志
    @Resource
    private OaActionRecordService oaActionRecordService;
    @Autowired
    private LkCrmAdminRoleDao adminRoleDao;
    @Autowired
    private LkCrmOaAnnouncementDao crmOaAnnouncementDao;

    /**
     * 添加或修改
     */
    public R saveAndUpdate(LkCrmOaAnnouncementEntity oaAnnouncement) {
        LkCrmAdminRoleEntity adminRole = adminRoleDao.queryAnnouncementByUserId(oaAnnouncement.getCreateUserId());
        //AdminRole adminRole = AdminRole.dao.findFirst(Db.getSql("admin.role.queryAnnouncementByUserId"), oaAnnouncement.getCreateUserId());

        if (adminRole == null && BaseUtil.getUserType() != 1) {
            return R.error("没有发布公告权限，不能发布公告！");
        }
        boolean flag;
        if (oaAnnouncement.getStartTime() != null && oaAnnouncement.getEndTime() != null) {
            if ((oaAnnouncement.getStartTime().compareTo(oaAnnouncement.getEndTime())) == 1) {
                return R.error("结束时间早于开始时间");
            }
        }
        if (oaAnnouncement.getStartTime() == null) {
            return R.error("开始时间不能为空！");
        }
        if (oaAnnouncement.getEndTime() == null) {
            return R.error("结束时间不能为空！");
        }
        oaAnnouncement.setDeptIds(TagUtil.fromString(oaAnnouncement.getDeptIds()));
        oaAnnouncement.setOwnerUserIds(TagUtil.fromString(oaAnnouncement.getOwnerUserIds()));
        if (oaAnnouncement.getAnnouncementId() == null) {
            oaAnnouncement.setCreateTime(DateUtil.date().toTimestamp());
            flag = (int) crmOaAnnouncementDao.saveReturnPk(oaAnnouncement) > 0;
        } else {
            oaAnnouncement.setUpdateTime(DateUtil.date().toTimestamp());
            LkCrmOaAnnouncementEntity dbEntity = crmOaAnnouncementDao.get(oaAnnouncement.getAnnouncementId());
            crmOaAnnouncementDao.update(dbEntity);
            BeanUtils.copyProperties(oaAnnouncement, dbEntity, JavaBeanUtil.getNullPropertyNames(oaAnnouncement));
            flag = true;
        }
        LoginUser user = BaseUtil.getUser();
        oaActionRecordService.addRecord(oaAnnouncement.getAnnouncementId(), OaEnum.ANNOUNCEMENT_TYPE_KEY.getTypes(), oaAnnouncement.getUpdateTime() == null ? 1 : 2, oaActionRecordService.getJoinIds(user.getUserId().intValue(), oaAnnouncement.getOwnerUserIds()), oaActionRecordService.getJoinIds(user.getDeptId(), oaAnnouncement.getDeptIds()));
        return R.isSuccess(flag);
    }

    /**
     * 删除
     */
    @Before(Tx.class)
    public R delete(Integer id) {
        LkCrmAdminRoleEntity adminRole = adminRoleDao.queryAnnouncementByUserId(BaseUtil.getUser().getUserId());
        if (adminRole == null && !BaseUtil.getUser().getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
            return R.error("没有删除公告权限，不能删除公告！");
        }
        oaActionRecordService.deleteRecord(OaEnum.ANNOUNCEMENT_TYPE_KEY.getTypes(), id);
        return R.ok().put("status", OaAnnouncement.dao.deleteById(id) ? 1 : 0);
    }

    /**
     * 根据ID查询详情
     */
    public R queryById(Integer id) {
        String sql = "    select an.* , us.user_id,us.username,us.img,us.realname,us.parent_id ,LEFT(an.start_time,10) as" +
                "    startTime,LEFT(an.end_time,10) as endTime " +
                "    from lkcrm_oa_announcement as an " +
                "    LEFT JOIN lkcrm_admin_user as us on us.user_id = create_user_id " +
                "    where " +
                "    an.announcement_id = ?";
        Record record = JavaBeanUtil.mapToRecord(adminRoleDao.queryUniqueSql(sql, id));
        return R.ok().put("data", record);
    }

    /**
     * 倒叙查询公告集合
     */
    public R queryList(BasePageRequest<OaAnnouncement> basePageRequest, Integer type) {
        LkCrmAdminRoleEntity adminRole = adminRoleDao.queryAnnouncementByUserId(BaseUtil.getUser().getUserId());
        int status = 1;
        if (adminRole == null && BaseUtil.getUserType() != 1) {
            status = 0;
        }
        String tatal = "select count(*)";
        String queryList = "select an.* , us.user_id,us.username,us.img,us.realname,us.parent_id ,LEFT(an.start_time,10) as startTime,LEFT(an.end_time,10) as endTime ";

        StringBuffer sql = new StringBuffer(" from lkcrm_oa_announcement as an " +
                " LEFT JOIN lkcrm_admin_user as us on us.user_id = create_user_id");
        if (type != null && type == 2) {
            sql.append(" WHERE (unix_timestamp(now()) - unix_timestamp(an.end_time)) > 0 ");
        } else {
            sql.append(" WHERE (unix_timestamp(now()) - unix_timestamp(an.end_time)) <= 0 ");
        }

        String cc = " order by an.announcement_id desc";
        Page page = adminRoleDao.pageByFullSql(basePageRequest.getPage(), basePageRequest.getLimit(),
                tatal + sql, queryList + sql + cc);
        for (Map<String, Object> obj : (List<Map<String, Object>>) page.getData()) {
            Record r = JavaBeanUtil.mapToRecord(obj);
            r.set("is_update", status);
            r.set("is_delete", status);
            if (r.getStr("read_user_ids") != null) {
                r.set("is_read", r.getStr("read_user_ids").contains("," + BaseUtil.getUserId().intValue() + ",") ? 1 : 0);
            } else {
                r.set("is_read", 0);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("totalRow", page.getTotal());
//        map.put("totalPage", page.get());
        map.put("pageSize", page.getLimit());
        map.put("pageNumber", page.getPageIndex());
        map.put("list", page.getData());
        map.put("is_save", status);
        return R.ok().put("data", map);
    }

    public void readAnnouncement(Integer announcementId) {
        OaAnnouncement oaAnnouncement = OaAnnouncement.dao.findById(announcementId);
        HashSet<String> hashSet = new HashSet<>(StrUtil.splitTrim(oaAnnouncement.getReadUserIds(), ","));
        hashSet.add(BaseUtil.getUser().getUserId().toString());
        oaAnnouncement.setReadUserIds("," + String.join(",", hashSet) + ",");
        oaAnnouncement.update();
    }

}
