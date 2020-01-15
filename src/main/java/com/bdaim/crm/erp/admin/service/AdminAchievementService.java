package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.util.StrUtil;
import com.bdaim.crm.dao.LkCrmAchievementDao;
import com.bdaim.crm.dao.LkCrmAdminDeptDao;
import com.bdaim.crm.entity.LkCrmAchievementEntity;
import com.bdaim.crm.entity.LkCrmAdminDeptEntity;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.bdaim.crm.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminAchievementService {

    @Autowired
    LkCrmAchievementDao adminAchievementDao;
    @Autowired
    LkCrmAdminDeptDao lkCrmAdminDeptDao;

    public R setAchievement(List<LkCrmAchievementEntity> achievementList) {
        achievementList.forEach(achievement -> {
            adminAchievementDao.executeUpdateSQL("delete lkcrm_crm_achievement from 72crm_crm_achievement where obj_id = ? and type = ? and year = ? and status = ?", achievement.getObjId(), achievement.getType(), achievement.getYear(), achievement.getStatus());
            //Db.delete(Db.getSql("admin.achievement.deleteAchievement"),achievement.getObjId(),achievement.getType(),achievement.getYear(),achievement.getStatus());
            adminAchievementDao.save(achievement);
        });
        return R.ok();
    }

    public R queryAchievementList(LkCrmAchievementEntity achievement, String userId, Integer deptId) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (achievement.getType() == null) {
            achievement.setType(2);
        }
        if (achievement.getType() == 2) {
            List<Map<String, Object>> deptList = new ArrayList<>();
            //查询部门
            LkCrmAdminDeptEntity lkCrmAdminDeptEntity = lkCrmAdminDeptDao.get(deptId);
            //Record record = Db.findFirst(Db.getSql("admin.achievement.queryDeptById"), deptId);
            //Record recordInfo = Db.findFirst(Db.getSql("admin.achievement.queryDeptInfo"), achievement.getYear(), achievement.getType(), lkCrmAdminDeptEntity.getDeptId(), achievement.getStatus());
            Map<String, Object> recordInfo = adminAchievementDao.queryDeptInfo(achievement.getYear(), achievement.getType(), lkCrmAdminDeptEntity.getDeptId(), achievement.getStatus());
            if (recordInfo == null) {
                recordInfo = new HashMap<>();
                recordInfo.put("november", 0);
                recordInfo.put("yeartarget", 0);
                recordInfo.put("may", 0);
                recordInfo.put("august", 0);
                recordInfo.put("february", 0);
                recordInfo.put("july", 0);
                recordInfo.put("april", 0);
                recordInfo.put("march", 0);
                recordInfo.put("june", 0);
                recordInfo.put("september", 0);
                recordInfo.put("january", 0);
                recordInfo.put("december", 0);
                recordInfo.put("october", 0);
                recordInfo.put("obj_id", deptId);
                recordInfo.put("year", achievement.getYear());
                recordInfo.put("type", achievement.getType());
                recordInfo.put("status", achievement.getStatus());
            }
            //record.setColumns(recordInfo);
            Map<String, Object> stringObjectMap = JavaBeanUtil.convertBeanToMap(lkCrmAdminDeptEntity);
            stringObjectMap.putAll(recordInfo);
            deptList.add(stringObjectMap);
            //List<Record> deptIdList = Db.find(Db.getSql("admin.achievement.queryDeptByPid"), deptId);
            List<Map<String, Object>> deptIdList = adminAchievementDao.queryDeptByPid(deptId);
            if (deptIdList != null && deptIdList.size() > 0) {
                deptIdList.forEach(record1 -> {
                    Map<String, Object> info = adminAchievementDao.queryDeptInfo(achievement.getYear(), achievement.getType(), NumberConvertUtil.parseInt(record1.get("dept_id")), achievement.getStatus());
                    if (info == null) {
                        info = new HashMap<>();
                        info.put("november", 0);
                        info.put("yeartarget", 0);
                        info.put("may", 0);
                        info.put("august", 0);
                        info.put("february", 0);
                        info.put("july", 0);
                        info.put("april", 0);
                        info.put("march", 0);
                        info.put("june", 0);
                        info.put("september", 0);
                        info.put("january", 0);
                        info.put("december", 0);
                        info.put("october", 0);
                        info.put("obj_id", deptId);
                        info.put("year", achievement.getYear());
                        info.put("type", achievement.getType());
                        info.put("status", achievement.getStatus());
                    }
                    record1.putAll(info);
                });
                deptList.addAll(deptIdList);
            }
            return R.ok().put("data", deptList);
        } else if (achievement.getType() == 3) {
            List<Map<String, Object>> userIdList = null;
            if (StrUtil.isEmpty(userId)) {
                //userIdList = Db.find(Db.getSql("admin.achievement.queryUserByDeptId"), deptId);
                userIdList = adminAchievementDao.queryUserByDeptId(deptId);
            } else {
                //userIdList = Db.find(Db.getSql("admin.achievement.queryUserById"), userId);
                userIdList = adminAchievementDao.queryUserById(userId);
            }
            if (userIdList != null && userIdList.size() > 0) {
                userIdList.forEach(record -> {
                    Map<String, Object> info = adminAchievementDao.queryUserInfo(achievement.getYear(), achievement.getType(), record.get("user_id").toString(), achievement.getStatus());
                    if (info == null) {
                        info = new HashMap<>();
                        info.put("november", 0);
                        info.put("yeartarget", 0);
                        info.put("may", 0);
                        info.put("august", 0);
                        info.put("february", 0);
                        info.put("july", 0);
                        info.put("april", 0);
                        info.put("march", 0);
                        info.put("june", 0);
                        info.put("september", 0);
                        info.put("january", 0);
                        info.put("december", 0);
                        info.put("october", 0);
                        info.put("obj_id", deptId);
                        info.put("year", achievement.getYear());
                        info.put("type", achievement.getType());
                        info.put("status", achievement.getStatus());
                    }
                    record.putAll(info);
                });
            }
            return R.ok().put("data", userIdList);
        }
        return null;
    }

}
