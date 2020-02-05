package com.bdaim.crm.erp.oa.service;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * @author wyq
 */
@Service
@Transactional
public class OaBackLogService {
    /**
     * oa代办事项提醒
     */
    public R backLogNum(){
        Integer userId = BaseUtil.getUserId().intValue();
        Integer eventNum = Db.queryInt(Db.getSql("oa.backLog.queryEventNum"),userId,userId);
        Integer taskNum = Db.queryInt(Db.getSql("oa.backLog.queryTaskNum"),userId,userId);
        Integer announcementNum = Db.queryInt(Db.getSql("oa.backLog.queryAnnouncementNum"),userId);
        Integer logNum = Db.queryInt(Db.getSql("oa.backLog.queryLogNum"),userId,userId,BaseUtil.getUser().getDeptId());
        Integer examineNum = Db.queryInt(Db.getSql("oa.backLog.queryExamineNum"),userId);
        return R.ok().put("data", Kv.by("eventNum",eventNum).set("taskNum",taskNum).set("announcementNum",announcementNum)
                .set("logNum",logNum).set("examineNum",examineNum));
    }
}