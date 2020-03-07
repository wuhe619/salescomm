package com.bdaim.crm.erp.oa.service;

import com.bdaim.crm.dao.LkCrmOaEventDao;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.kit.Kv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * @author wyq
 */
@Service
@Transactional
public class OaBackLogService {
    @Autowired
    private LkCrmOaEventDao eventDao;

    /**
     * oa代办事项提醒
     */
    public R backLogNum() {
        Integer userId = BaseUtil.getUserId().intValue();
        String sql1 = "  select count(*) from lkcrm_oa_event " +
                "  where TO_DAYS(start_time) <= TO_DAYS(NOW()) and TO_DAYS(end_time) >= TO_DAYS(NOW()) " +
                "  and (create_user_id = ? or owner_user_ids like CONCAT('%',?,'%'))";
        Integer eventNum = eventDao.queryForInt(sql1, userId, userId);
        String sql2 = "select count(*) from lkcrm_task where status = 1 and " +
                "(main_user_id = ? or owner_user_id like CONCAT('%,',?,',%'))";
        Integer taskNum = eventDao.queryForInt(sql2, userId, userId);
        String sql3 = "  select count(*) from lkcrm_oa_announcement\n" +
                "  where TO_DAYS(start_time) <= TO_DAYS(NOW()) and TO_DAYS(end_time) >= TO_DAYS(NOW())\n" +
                "  and (read_user_ids not like CONCAT('%',?,'%') or read_user_ids is null)";
        Integer announcementNum = eventDao.queryForInt(sql3, userId);
        String sql4 = "  select count(*) from lkcrm_oa_log\n" +
                "  where read_user_ids not like CONCAT('%,',?,',%') and (send_user_ids like CONCAT('%,',?,',%')" +
                " or send_dept_ids like CONCAT('%,',?,',%'))";
        Integer logNum = eventDao.queryForInt(sql4, userId, userId, BaseUtil.getUser().getDeptId());
        String sql5 = "  select count(*) from lkcrm_oa_examine_record as a left join lkcrm_oa_examine_log as b ON a.record_id = b.record_id\n" +
                "  where b.examine_user = ? and b.examine_status = 0 and " +
                "ifnull(a.examine_step_id, 1) = ifnull(b.examine_step_id, 1) and b.is_recheck != 1";
        Integer examineNum = eventDao.queryForInt(sql5, userId);
        return R.ok().put("data", Kv.by("eventNum", eventNum).set("taskNum", taskNum).set("announcementNum", announcementNum)
                .set("logNum", logNum).set("examineNum", examineNum));
    }
}
