package com.bdaim.crm.erp.bi.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bdaim.crm.dao.LkCrmOaExamineCategoryDao;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.plugin.activerecord.Record;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chacker
 */
@Service
@Transactional
public class BiCustomerService {
    @Resource
    BiTimeUtil biTimeUtil;
    @Autowired
    private LkCrmOaExamineCategoryDao categoryDao;

    /**
     * 客户总量分析图
     *
     * @author Chacker
     */
    public R totalCustomerStats(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,IFNULL((select count(customer_id) from customerview where cust_id='")
                    .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and owner_user_id in (").append(userIds)
                    .append(")),0) as customerNum,IFNULL(count(DISTINCT a.customer_id),0) as dealCustomerNum from lkcrm_crm_customer as a left join lkcrm_crm_contract as b on a.customer_id = b.customer_id where DATE_FORMAT(b.order_date,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and b.cust_id='").append(BaseUtil.getCustId())
                    .append("' and b.owner_user_id in (").append(userIds).append(")");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> recordListMap = categoryDao.findMapBySql(sqlStringBuffer.toString(), null);
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 客户总量分析表
     *
     * @author Chacker
     */
    public R totalCustomerTable(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", new String[]{});
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        String[] userIdsArr = userIds.split(",");
        StringBuffer sqlStringBuffer = new StringBuffer();
        String custId = BaseUtil.getCustId();
        for (int i = 1; i <= userIdsArr.length; i++) {
            sqlStringBuffer.append("select (select realname from lkcrm_admin_user where user_id = ").append(userIdsArr[i - 1])
                    .append(") as realname,count(a.customer_id) as customerNum,IFNULL((select count(distinct b.customer_id) from " +
                            "lkcrm_crm_contract as b left join lkcrm_crm_customer as c on b.customer_id = c.customer_id where " +
                            "b.cust_id='" + custId +
                            "' and c.customer_id = a.customer_id and b.check_status = 2 ),0) as dealCustomerNum,(select IFNULL(SUM(money),0) " +
                            "from lkcrm_crm_contract where DATE_FORMAT(order_date,'").append(sqlDateFormat).append("') between '")
                    .append(beginTime).append("' and '").append(finalTime).append("' and owner_user_id = ").append(userIdsArr[i - 1])
                    .append(" ) as contractMoney,(select IFNULL(SUM(d.money),0) from lkcrm_crm_receivables as d left join lkcrm_crm_contract" +
                            " as e on d.contract_id = e.contract_id where DATE_FORMAT(e.order_date,'").append(sqlDateFormat).append("') between '")
                    .append(beginTime).append("' and '").append(finalTime).append("' and e.owner_user_id = ").append(userIdsArr[i - 1])
                    .append(" ) as receivablesMoney from customerview as a where DATE_FORMAT(create_time,'").append(sqlDateFormat)
                    .append("') between '").append(beginTime).append("' and '").append(finalTime).append("' and cust_id='")
                    .append(custId)
                    .append("' and owner_user_id = ")
                    .append(userIdsArr[i - 1]);
            if (i != userIdsArr.length) {
                sqlStringBuffer.append(" union all ");
            }
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        recordList.forEach(r -> {
            r.set("dealCustomerRate", r.getInt("customerNum") != 0 ? r.getInt("dealCustomerNum") * 100 / r.getInt("customerNum") : 0);
            r.set("unreceivedMoney", r.getInt("contractMoney") - r.getInt("receivablesMoney"));
            r.set("completedRate", r.getInt("contractMoney") != 0 ? r.getInt("receivablesMoney") * 100 / r.getInt("contractMoney") : 0);
        });
        return R.ok().put("data", recordList);
    }

    /**
     * 客户跟进次数分析图
     *
     * @author Chacker
     */
    public R customerRecordStats(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,IFNULL((select count(record_id) from lkcrm_admin_record where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and types = 'crm_customer' and cust_id='")
                    .append(BaseUtil.getCustId()).append("' and create_user_id in (").append(userIds)
                    .append(")),0) as recordCount,IFNULL(count(DISTINCT types_id),0) as customerCount from lkcrm_admin_record where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and types = 'crm_customer' and cust_id='")
                    .append(BaseUtil.getCustId()).append("' and create_user_id in (").append(userIds).append(")");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 客户跟进次数分析表
     *
     * @author Chacker
     */
    public R customerRecordInfo(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", new String[]{});
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        String[] userIdsArr = userIds.split(",");
        for (int i = 1; i <= userIdsArr.length; i++) {
            sqlStringBuffer.append("select b.realname,IFNULL(count(a.record_id),0) as recordCount,IFNULL(count(DISTINCT a.types_id),0) " +
                    "as customerCount from lkcrm_admin_record as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id where DATE_FORMAT(a.create_time,'")
                    .append(sqlDateFormat).append("') between '").append(beginTime).append("' and '").append(finalTime)
                    .append("' and a.cust_id='").append(BaseUtil.getCustId())
                    .append("' and a.types = 'crm_customer' and b.user_id = ").append(userIdsArr[i - 1]);
            if (i != userIdsArr.length) {
                sqlStringBuffer.append(" union all ");
            }
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 客户跟进方式分析
     *
     * @author Chacker
     */
    public R customerRecodCategoryStats(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        sqlStringBuffer.append("select category,IFNULL(count(record_id),0) as recordNum," +
                "IFNULL(count(record_id)*100/(select count(*) from lkcrm_admin_record where cust_id='")
                .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(create_time,'")
                .append(sqlDateFormat).append("') between '").append(beginTime).append("' and '")
                .append(finalTime).append("' and create_user_id in (")
                .append(userIds).append(")),0) as proportion from lkcrm_admin_record where cust_id='")
                .append(BaseUtil.getCustId()).append("' and (DATE_FORMAT(create_time,'")
                .append(sqlDateFormat)
                .append("') between '").append(beginTime).append("' and '").append(finalTime)
                .append("') and create_user_id in (").append(userIds)
                .append(") group by category");
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 客户转化率分析图
     *
     * @author Chacker
     */
    public R customerConversionStats(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,IFNULL((select count(customer_id) from lkcrm_crm_customer where cust_id='")
                    .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and owner_user_id in (").append(userIds)
                    .append(")),0) as customerNum,IFNULL(count(a.customer_id)*100/(select count(customer_id) from lkcrm_crm_customer where cust_id='")
                    .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and owner_user_id in (").append(userIds)
                    .append(")),0) as pro,IFNULL(count(a.customer_id),0) as dealCustomerNum from lkcrm_crm_customer as a left join lkcrm_crm_contract as b on a.customer_id = b.customer_id where a.cust_id='")
                    .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(a.create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and a.owner_user_id in (").append(userIds)
                    .append(") and b.check_status = 2");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 客户转化率分析表
     *
     * @author Chacker
     */
    public R customerConversionInfo(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        sqlStringBuffer.append("select a.customer_name,b.name as contract_name,b.money as contract_money,IFNULL(c.money,0) as receivables_money," +
                "a.`客户行业`,a.`客户来源`,a.owner_user_name,a.create_user_name,a.create_time,a.update_time,b.order_date from customerview " +
                "as a inner join lkcrm_crm_contract as b on a.customer_id = b.customer_id left join lkcrm_crm_receivables as c on " +
                "b.contract_id = c.contract_id where b.cust_id='")
                .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(a.create_time,'")
                .append(sqlDateFormat).append("') between '").append(beginTime)
                .append("' and '").append(finalTime).append("' and b.check_status = 2 and a.owner_user_id in (")
                .append(userIds).append(")");
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 公海客户分析图
     *
     * @author Chacker
     */
    public R poolStats(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,count(type_id) as putInNum,(select count(type_id)" +
                    " from lkcrm_crm_owner_record where DATE_FORMAT(create_time,'").append(sqlDateFormat).append("') = '")
                    .append(beginTime).append("' and type = 8 and post_owner_user_id in (").append(userIds).append(")) as receiveNum " +
                    "from lkcrm_crm_owner_record where DATE_FORMAT(create_time,'").append(sqlDateFormat).append("') = '")
                    .append(beginTime).append("' and type = 8 and pre_owner_user_id in (").append(userIds).append(")");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 公海客户分析表
     *
     * @author Chacker
     */
    public R poolTable(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        List<Map<String, Object>> recordMaps = categoryDao.poolTable(record);
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordMaps);
        return R.ok().put("data", recordList);
    }

    /**
     * 员工客户成交周期图
     *
     * @author Chacker
     */
    public R employeeCycle(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,IFNULL(AVG(TIMESTAMPDIFF(DAY,a.create_time,b.order_date)),0)" +
                    " as cycle, count(a.customer_id) as customerNum from lkcrm_crm_customer as a left join lkcrm_crm_contract as b on " +
                    "a.customer_id = b.customer_id where a.cust_id='")
                    .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(a.create_time,'").append(sqlDateFormat).append("') = '").append(beginTime)
                    .append("' and b.check_status = 2 and a.owner_user_id in (").append(userIds).append(")");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 员工客户成交周期表
     *
     * @author Chacker
     */
    public R employeeCycleInfo(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", new String[]{});
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        String[] userIdsArr = userIds.split(",");
        for (int i = 1; i <= userIdsArr.length; i++) {
            sqlStringBuffer.append("select a.realname,IFNULL(AVG(TIMESTAMPDIFF(DAY,b.create_time,c.order_date)),0) as cycle, count(b.customer_id) " +
                    "as customerNum from lkcrm_admin_user as a left join customerview as b on a.user_id = b.owner_user_id left join " +
                    "lkcrm_crm_contract as c on b.customer_id = c.customer_id where b.cust_id='")
                    .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(b.create_time,'").append(sqlDateFormat)
                    .append("') between '").append(beginTime).append("' and '").append(finalTime).append("' and c.check_status = 2 and a.user_id = ")
                    .append(userIdsArr[i - 1]);
            if (i != userIdsArr.length) {
                sqlStringBuffer.append(" union all ");
            }
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 地区成交周期
     *
     * @author Chacker
     */
    public R districtCycle(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        String[] districtArr = new String[]{"北京", "上海", "天津", "广东", "浙江", "海南", "福建", "湖南", "湖北", "重庆", "辽宁", "吉林", "黑龙江", "河北", "河南", "山东", "陕西", "甘肃", "青海", "新疆", "山西", "四川", "贵州", "安徽", "江西", "江苏", "云南", "内蒙古", "广西", "西藏", "宁夏"};
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= districtArr.length; i++) {
            sqlStringBuffer.append("select '").append(districtArr[i - 1]).append("' as type,IFNULL(AVG(TIMESTAMPDIFF(DAY,a.create_time,b.order_date)),0)" +
                    " as cycle, count(a.customer_id) as customerNum from lkcrm_crm_customer as a left join lkcrm_crm_contract as b on " +
                    "a.customer_id = b.customer_id where a.cust_id='")
                    .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(a.create_time,'").append(sqlDateFormat).append("') between '").append(beginTime)
                    .append("' and '").append(finalTime).append("' and b.check_status = 2 and a.owner_user_id in (").append(userIds)
                    .append(") and a.address like '%").append(districtArr[i - 1]).append("%'");
            if (i != districtArr.length) {
                sqlStringBuffer.append(" union all ");
            }
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    /**
     * 产品成交周期
     *
     * @author Chacker
     */
    public R productCycle(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        String sql = "select product_id,name from lkcrm_crm_product";
        List<Map<String, Object>> maps = categoryDao.queryListBySql(sql);
        List<Record> productList = JavaBeanUtil.mapToRecords(maps);
        if (CollectionUtil.isEmpty(productList)) {
            return R.ok().put("data", new ArrayList<>());
        }
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= productList.size(); i++) {
            sqlStringBuffer.append("select '").append(productList.get(i - 1).getStr("name")).append("' as productName,IFNULL(AVG(TIMESTAMPDIFF(DAY,a.create_time,b.order_date)),0)" +
                    " as cycle, count(a.customer_id) as customerNum from lkcrm_crm_customer as a left join lkcrm_crm_contract as b on " +
                    "a.customer_id = b.customer_id left join lkcrm_crm_contract_product as c on b.contract_id = c.contract_id where DATE_FORMAT(a.create_time,'")
                    .append(sqlDateFormat).append("') between '").append(beginTime).append("' and '").append(finalTime)
                    .append("' and b.check_status = 2 and a.cust_id='")
                    .append(BaseUtil.getCustId()).append("' and a.owner_user_id in (").append(userIds).append(") and c.product_id = ")
                    .append(productList.get(i - 1).getInt("product_id"));
            if (i != productList.size()) {
                sqlStringBuffer.append(" union all ");
            }
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }
}
