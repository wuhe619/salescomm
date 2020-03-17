package com.bdaim.crm.erp.bi.service;

import com.bdaim.crm.dao.LkCrmBiDao;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.plugin.activerecord.Record;
import com.bdaim.crm.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BiService {
    @Autowired
    private LkCrmBiDao biDao;

    /**
     * @author Chacker
     * 根据商机id查询合同
     */
    public R queryCrmBusinessStatistics(Long userId, Integer deptId, Integer productId, Date startTime, Date endTime) {
        List<Map<String, Object>> maps = biDao.queryCrmBusinessStatistics(userId, productId, startTime,
                endTime, deptId);
        List<Record> records = JavaBeanUtil.mapToRecords(maps);
        return R.ok().put("data", records);
    }

    /**
     * 产品销售情况统计
     * startTime 开始时间 endTime 结束时间 userId用户ID deptId部门ID
     */
    public R queryProductSell(Date startTime, Date endTime, Integer userId, Integer deptId) {
        List<Map<String, Object>> maps = biDao.queryProductSell(startTime, endTime, userId, deptId);
        List<Record> categorys = JavaBeanUtil.mapToRecords(maps);
        return R.ok().put("data", categorys);
    }

    /**
     * 回款统计，根据月份获取合同信息
     * userId用户ID deptId部门ID
     */
    public R queryByUserIdOrYear(Integer userId, Integer deptId, String year, String month) {
        List<Map<String, Object>> maps = biDao.queryByUserIdOrYear(year, month, userId, deptId);
        List<Record> categorys = JavaBeanUtil.mapToRecords(maps);
        return R.ok().put("data", categorys);
    }

    /**
     * 获取商业智能业绩目标完成情况
     *
     * @author Chacker
     */
    public R taskCompleteStatistics(String year, Integer type, Integer deptId, Long userId) {
        if (type == 1) {
            if (userId == null) {
                List<Map<String, Object>> maps = biDao.queryContractByDeptId(year, deptId);
                List<Record> recordList = JavaBeanUtil.mapToRecords(maps);
                return R.ok().put("data", recordList);
            } else {
                List<Map<String, Object>> maps = biDao.queryContractByUserId(year, userId);
                List<Record> recordList = JavaBeanUtil.mapToRecords(maps);
                return R.ok().put("data", recordList);
            }
        } else if (type == 2) {
            if (userId == null) {
                List<Map<String, Object>> maps = biDao.queryReceivablesByDeptId(year, deptId);
                List<Record> recordList = JavaBeanUtil.mapToRecords(maps);
                return R.ok().put("data", recordList);
            } else {
                List<Map<String, Object>> maps = biDao.queryReceivablesByUserId(year, userId);
                List<Record> recordList = JavaBeanUtil.mapToRecords(maps);
                return R.ok().put("data", recordList);
            }
        } else {
            return R.error("type不符合要求");
        }

    }
}
