package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.InstrumentDao;
import com.bdaim.crm.dao.LkCrmBiDao;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.CrmPage;
import com.bdaim.crm.utils.ParamsUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.SqlAppendUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class InstrumentService {
    @Resource
    private BiTimeUtil biTimeUtil;
    @Resource
    private InstrumentDao instrumentDao;
    @Autowired
    private LkCrmBiDao crmBiDao;

    /**
     * 销售简报
     */
    public R queryBulletin(String status, String userIds, String startTime, String endTime) {
        //1.今天 2.昨天 3.本周 4.上周 5.本月6.上月7.本季度8.上季度9.本年10上年
        String[] userIdss = userIds.split(",");
        Integer type = biTimeUtil.analyzeType(status);
        if (type == 1) {
            return R.ok().put("data", instrumentDao.intraDay(userIdss));
        } else if (type == 2) {
            return R.ok().put("data", instrumentDao.yesterday(userIdss));
        } else if (type == 3) {
            return R.ok().put("data", instrumentDao.thisWeek(userIdss));
        } else if (type == 4) {
            return R.ok().put("data", instrumentDao.lastWeek(userIdss));
        } else if (type == 5) {
            return R.ok().put("data", instrumentDao.theSameMonth(userIdss));
        } else if (type == 6) {
            return R.ok().put("data", instrumentDao.lastMonth(userIdss));
        } else if (type == 7) {
            return R.ok().put("data", instrumentDao.currentSeason(userIdss));
        } else if (type == 8) {
            return R.ok().put("data", instrumentDao.precedingQuarter(userIdss));
        } else if (type == 9) {
            return R.ok().put("data", instrumentDao.thisYear(userIdss));
        } else if (type == 10) {
            return R.ok().put("data", instrumentDao.lastYear(userIdss));
        } else if (type == 11) {
            return R.ok().put("data", instrumentDao.custom(userIdss, startTime, endTime));
        }
        return R.error();
    }

    /**
     * @return
     * @author zhang
     * 销售简报的数据查看详情
     */
    public CrmPage queryBulletinInfo(BasePageRequest basePageRequest, String userIds, String type, Integer label) {
        Record record = new Record().set("type", type);
        biTimeUtil.analyzeType(record);
        String viewName;
        boolean tn = false;
        switch (label) {
            case 2:
                viewName = "customerview";
                break;
            case 3:
                viewName = "contactsview";
                break;
            case 5:
                viewName = "businessview";
                break;
            case 6:
                viewName = "contractview";
                break;
            case 7:
                viewName = "receivablesview";
                break;
            case 0:
                viewName = "businessview";
                record.set("tn", true);
                tn = true;
                break;
            default:
                return new CrmPage();
        }
        record.set("userIds", userIds.split(",")).set("viewName", viewName);
        String sortField = basePageRequest.getJsonObject().getString("sortField");
        if (!ParamsUtil.isValid(sortField)) {
            return new CrmPage();
        }
        if (StrUtil.isEmpty(sortField)) {
            sortField = "update_time";
        }
        Integer order = basePageRequest.getJsonObject().getInteger("order");
        if (order == null || (order != 1 && order != 2)) {
            order = 1;
        }
        String orderType = order == 1 ? "desc" : "asc";
        record.set("sortField", sortField);
        record.set("orderType", orderType);
        return BaseUtil.crmPage(instrumentDao.queryBulletinInfo(basePageRequest.getPage(), basePageRequest.getLimit(), userIds.split(","), viewName, sortField, orderType,
                tn, record.getStr("beginDate"), record.getStr("endDate")));
        //return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.Instrument.queryBulletinInfo", record));

    }

    /**
     * 业绩指标
     */
    public R queryPerformance(String startTime, String endTime, String userIds, String deptIds, Integer status, String type, String allUsetIds) {
        String[] userIdss = {};
        String[] deptIdss = {};
        String[] allUsetIdss = {};
        if (StrUtil.isNotEmpty(userIds)) {
            userIdss = userIds.split(",");
        }
        if (StrUtil.isNotEmpty(deptIds)) {
            deptIdss = deptIds.split(",");
        }
        if (StrUtil.isNotEmpty(allUsetIds)) {
            allUsetIdss = allUsetIds.split(",");
        }
        if (StrUtil.isNotEmpty(type)) {
            Record r = getTime(type);
            startTime = r.getStr("startTime");
            endTime = r.getStr("endTime");
        }
        //status 1 合同 2.回款
        Record record = JavaBeanUtil.mapToRecord(instrumentDao.queryMoneys(userIdss, startTime, endTime));
        //Record record = Db.findFirst(Db.getSqlPara("crm.Instrument.queryMoneys", Kv.by("startTime", startTime).set("endTime", endTime).set("userIds", allUsetIdss)));
        if (record == null) {
            return R.ok().put("data", new Record().set("contractMoneys", 0).set("receivablesMoneys", 0).set("achievementMoneys", 0).set("proportion", 0));
        }
        List<Integer> list = getYear(startTime, endTime);
        BigDecimal money = new BigDecimal(0);
        if (list.size() == 1) {
            List<Record> starts = new ArrayList<>();
            if (StrUtil.isNotEmpty(deptIds)) {
                starts.addAll(Db.find(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(0)).set("status", status).set("deptIds", deptIdss))));
            }

            if (StrUtil.isNotEmpty(userIds)) {
                starts.addAll(Db.find(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(0)).set("status", status).set("userIds", userIdss))));
            }
            Integer sta = DateUtil.month(DateUtil.parse(startTime, "yyyyMM")) + 1;
            Integer en = DateUtil.month(DateUtil.parse(endTime, "yyyyMM")) + 1;
            for (Record start : starts) {
                if (start != null) {
                    if (sta <= 1 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("january")));
                    }
                    if (sta <= 2 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("february")));
                    }
                    if (sta <= 3 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("march")));
                    }
                    if (sta <= 4 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("april")));
                    }
                    if (sta <= 5 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("may")));
                    }
                    if (sta <= 6 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("june")));
                    }
                    if (sta <= 7 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("july")));
                    }
                    if (sta <= 8 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("august")));
                    }
                    if (sta <= 9 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("september")));
                    }
                    if (sta <= 10 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("october")));
                    }
                    if (sta <= 11 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("november")));
                    }
                    if (sta <= 12 && en >= 1) {
                        money = money.add(new BigDecimal(start.getStr("december")));
                    }
                }
            }

        } else {
            for (int i = 1; i < list.size() - 1; i++) {
                Record r = new Record();
                if (StrUtil.isNotEmpty(userIds)) {
                    r = Db.findFirst(Db.getSqlPara("crm.Instrument.queryTarget", Kv.by("year", list.get(i)).set("status", status).set("userIds", userIdss)));
                    money = money.add(new BigDecimal(r.getStr("achievementTarget")));
                }
                if (StrUtil.isNotEmpty(deptIds)) {
                    r = Db.findFirst(Db.getSqlPara("crm.Instrument.queryTarget", Kv.by("year", list.get(i)).set("status", status).set("deptIds", deptIdss)));
                    money = money.add(new BigDecimal(r.getStr("achievementTarget")));
                }
            }
            List<Record> starts = new ArrayList<>();
            if (StrUtil.isNotEmpty(deptIds)) {
                starts.addAll(Db.find(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(0)).set("deptIds", deptIdss).set("status", status))));
            }
            if (StrUtil.isNotEmpty(userIds)) {
                starts.addAll(Db.find(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(0)).set("userIds", userIdss).set("status", status))));
            }
            Integer sta = DateUtil.month(DateUtil.parse(startTime, "yyyyMM")) + 1;
            for (Record start : starts) {
                if (start != null) {
                    if (sta <= 1) {
                        money = money.add(new BigDecimal(start.getStr("january")));
                    }
                    if (sta <= 2) {
                        money = money.add(new BigDecimal(start.getStr("february")));
                    }
                    if (sta <= 3) {
                        money = money.add(new BigDecimal(start.getStr("march")));
                    }
                    if (sta <= 4) {
                        money = money.add(new BigDecimal(start.getStr("april")));
                    }
                    if (sta <= 5) {
                        money = money.add(new BigDecimal(start.getStr("may")));
                    }
                    if (sta <= 6) {
                        money = money.add(new BigDecimal(start.getStr("june")));
                    }
                    if (sta <= 7) {
                        money = money.add(new BigDecimal(start.getStr("july")));
                    }
                    if (sta <= 8) {
                        money = money.add(new BigDecimal(start.getStr("august")));
                    }
                    if (sta <= 9) {
                        money = money.add(new BigDecimal(start.getStr("september")));
                    }
                    if (sta <= 10) {
                        money = money.add(new BigDecimal(start.getStr("october")));
                    }
                    if (sta <= 11) {
                        money = money.add(new BigDecimal(start.getStr("november")));
                    }
                    if (sta <= 12) {
                        money = money.add(new BigDecimal(start.getStr("december")));
                    }
                }
            }
            List<Record> ends = new ArrayList<>();
            if (StrUtil.isNotEmpty(deptIds)) {
                ends.addAll(Db.find(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(list.size() - 1)).set("deptIds", deptIdss).set("status", status))));
            }
            if (StrUtil.isNotEmpty(userIds)) {
                ends.addAll(Db.find(Db.getSqlPara("crm.Instrument.queryTargets", Kv.by("year", list.get(list.size() - 1)).set("userIds", userIdss).set("status", status))));
            }
            Integer en = DateUtil.month(DateUtil.parse(endTime, "yyyyMM")) + 1;
            for (Record end : ends) {
                if (end != null) {
                    if (en >= 1) {
                        money = money.add(new BigDecimal(end.getStr("january")));
                    }
                    if (en >= 2) {
                        money = money.add(new BigDecimal(end.getStr("february")));
                    }
                    if (en >= 3) {
                        money = money.add(new BigDecimal(end.getStr("march")));
                    }
                    if (en >= 4) {
                        money = money.add(new BigDecimal(end.getStr("april")));
                    }
                    if (en >= 5) {
                        money = money.add(new BigDecimal(end.getStr("may")));
                    }
                    if (en >= 6) {
                        money = money.add(new BigDecimal(end.getStr("june")));
                    }
                    if (en >= 7) {
                        money = money.add(new BigDecimal(end.getStr("july")));
                    }
                    if (en >= 8) {
                        money = money.add(new BigDecimal(end.getStr("august")));
                    }
                    if (en >= 9) {
                        money = money.add(new BigDecimal(end.getStr("september")));
                    }
                    if (en >= 10) {
                        money = money.add(new BigDecimal(end.getStr("october")));
                    }
                    if (en >= 11) {
                        money = money.add(new BigDecimal(end.getStr("november")));
                    }
                    if (en >= 12) {
                        money = money.add(new BigDecimal(end.getStr("december")));
                    }
                }
            }
        }
        record.set("achievementMoneys", money);
        if (money.compareTo(new BigDecimal(0)) == 0) {
            record.set("proportion", 0);
        } else {
            if (status == 2) {
                record.set("proportion", new BigDecimal(record.getStr("receivablesMoneys")).multiply(new BigDecimal(100)).divide(money, 2, BigDecimal.ROUND_HALF_UP));
            } else if (status == 1) {
                record.set("proportion", new BigDecimal(record.getStr("contractMoneys")).multiply(new BigDecimal(100)).divide(money, 2, BigDecimal.ROUND_HALF_UP));
            }
        }
        return R.ok().put("data", record);
    }

    /**
     * 获取传过来的年份
     */
    private List<Integer> getYear(String startTime, String endTime) {
        List<Integer> list = new ArrayList<>();
        Integer start = DateUtil.year(DateUtil.parse(startTime, "yyyyMM"));
        Integer end = DateUtil.year(DateUtil.parse(endTime, "yyyyMM"));
        for (int i = start; i <= end; i++) {
            list.add(i);
        }
        return list;
    }

    private Record getTime(String type) {
        Record record = new Record();
        String startTime;
        String endTime;
        Integer strYear = 0;
        Integer strMonth = 0;
        Integer endYear = 0;
        Integer endMonth = 0;
        Date date = DateUtil.date();
        if ("year".equals(type)) {
            strYear = DateUtil.year(date);
            endYear = DateUtil.year(date);
            strMonth = 1;
            endMonth = 12;
        } else if ("lastYear".equals(type)) {
            // status = 10;
            strYear = DateUtil.year(date) - 1;
            endYear = DateUtil.year(date) - 1;
            strMonth = 1;
            endMonth = 12;
        } else if ("quarter".equals(type)) {
            // status = 7;
            int q = DateUtil.quarter(date);
            if (q == 1) {
                strMonth = 1;
                endMonth = 3;
            } else if (q == 2) {
                strMonth = 4;
                endMonth = 6;
            } else if (q == 3) {
                strMonth = 7;
                endMonth = 9;
            } else if (q == 4) {
                strMonth = 10;
                endMonth = 12;
            }
            strYear = DateUtil.year(DateUtil.beginOfQuarter(date));
            endYear = DateUtil.year(DateUtil.endOfQuarter(date));
        } else if ("lastQuarter".equals(type)) {
            int q = DateUtil.quarter(date);
            if (q == 1) {
                strMonth = 10;
                endMonth = 12;
                strYear = DateUtil.year(date) - 1;
                endYear = DateUtil.year(date) - 1;
            } else if (q == 2) {
                strMonth = 1;
                endMonth = 3;
                strYear = DateUtil.year(date);
                endYear = DateUtil.year(date);
            } else if (q == 3) {
                strMonth = 4;
                endMonth = 6;
                strYear = DateUtil.year(date);
                endYear = DateUtil.year(date);
            } else if (q == 4) {
                strMonth = 7;
                endMonth = 9;
                strYear = DateUtil.year(date);
                endYear = DateUtil.year(date);
            }
        } else if ("month".equals(type)) {
            // status = 5;
            strYear = DateUtil.year(DateUtil.offsetDay(DateUtil.nextMonth(), -1));
            endYear = DateUtil.year(DateUtil.offsetDay(DateUtil.nextMonth(), -1));
            strMonth = DateUtil.month(DateUtil.offsetDay(DateUtil.nextMonth(), -1)) + 1;
            endMonth = DateUtil.month(DateUtil.offsetDay(DateUtil.nextMonth(), -1)) + 1;
        } else if ("lastMonth".equals(type)) {

            strYear = DateUtil.year(DateUtil.offsetMonth(date, -1));
            endYear = DateUtil.year(DateUtil.offsetMonth(date, -1));
            strMonth = DateUtil.month(DateUtil.offset(date, DateField.MONTH, -1)) + 1;
            endMonth = DateUtil.month(DateUtil.offset(date, DateField.MONTH, -1)) + 1;
            //  status = 6;
        } else if ("week".equals(type)) {
            // status = 3;
            strYear = DateUtil.year(DateUtil.beginOfWeek(date));
            endYear = DateUtil.year(DateUtil.endOfWeek(date));
            strMonth = DateUtil.month(DateUtil.beginOfWeek(date)) + 1;
            endMonth = DateUtil.month(DateUtil.endOfWeek(date)) + 1;
        } else if ("lastWeek".equals(type)) {
            // status = 4;
            strYear = DateUtil.year(DateUtil.lastWeek());
            endYear = DateUtil.year(DateUtil.offsetDay(DateUtil.lastWeek(), 7));
            strMonth = DateUtil.month(DateUtil.lastWeek()) + 1;
            endMonth = DateUtil.month(DateUtil.offsetDay(DateUtil.lastWeek(), 7)) + 1;
        } else if ("today".equals(type)) {
            // status = 1;
            strYear = DateUtil.year(date);
            endYear = DateUtil.year(date);
            strMonth = DateUtil.month(date) + 1;
            endMonth = DateUtil.month(date) + 1;

        } else if ("yesterday".equals(type)) {
            // status = 2;
            strYear = DateUtil.year(DateUtil.offsetDay(date, -1));
            endYear = DateUtil.year(DateUtil.offsetDay(date, -1));
            strMonth = DateUtil.month(DateUtil.offsetDay(date, -1)) + 1;
            endMonth = DateUtil.month(DateUtil.offsetDay(date, -1)) + 1;
        }
        if (strMonth < 10) {
            startTime = strYear + "0" + strMonth;
        } else {
            startTime = strYear + "" + strMonth;
        }
        if (endMonth < 10) {
            endTime = endYear + "0" + endMonth;
        } else {
            endTime = endYear + "" + endMonth;
        }
        record.set("startTime", startTime);
        record.set("endTime", endTime);
        return record;
    }

    /**
     * 销售漏斗
     */
    public R queryBusiness(String userIds, String deptIds, Integer typeId, Date startTime, Date endTime) {
        String[] userIdss = userIds.split(",");
        String[] deptIdss = deptIds.split(",");
        Record record = Db.findFirst(Db.getSqlPara("crm.Instrument.queryBusiness",
                Kv.by("userIds", userIdss).set("typeId", typeId)
                        .set("startTime", startTime).set("endTime", endTime).set("deptIds", deptIdss)));
        List<Record> records = Db.find(Db.getSqlPara("crm.Instrument.queryBusinessStatistics",
                Kv.by("userIds", userIdss).set("typeId", typeId)
                        .set("startTime", startTime).set("endTime", endTime).set("deptIds", deptIdss)));
        if (record != null) {
            record.set("record", records);
        }
        return R.ok().put("data", record);
    }

    /**
     * 销售趋势（新）
     * type 1.今天 2.昨天 3.本周 4.上周 5.本月6.上月7.本季度8.上季度9.本年10上年11.自定义
     * userIds 员工id拼写id之间用‘,’隔开
     * deptIds 部门id
     */
    public R salesTrend(String type, String userIds, String startTime, String endTime) {
        String[] userIdss = userIds.split(",");
        Record record = new Record();
        record.set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        Integer beginTime = record.getInt("beginTime");
        List<Record> recordList = new ArrayList<>();
        for (int i = 1; i <= cycleNum; i++) {
            String sql = "select '"+beginTime+"' as type,IFNULL(SUM(money),0) as contractMoneys, (SELECT IFNULL(SUM(money),0) FROM lkcrm_crm_receivables WHERE DATE_FORMAT( return_time,? ) = ?  " +
                    "and check_status = 2 AND owner_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds.split(",")) + ")) as receivablesMoneys FROM lkcrm_crm_contract as ccco where DATE_FORMAT(ccco.order_date,?)=? and ccco.check_status = 2 AND owner_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds.split(",")) + ")";
            recordList.addAll(JavaBeanUtil.mapToRecords(crmBiDao.salesTrend(sqlDateFormat, beginTime, userIdss)));
            recordList.addAll(JavaBeanUtil.mapToRecords(instrumentDao.sqlQuery(sql, sqlDateFormat, beginTime, sqlDateFormat, beginTime)));
            beginTime = biTimeUtil.estimateTime(beginTime);
        }

        Integer ststus = biTimeUtil.analyzeType(type);
        Record totlaContractMoney = JavaBeanUtil.mapToRecord(instrumentDao.queryContractMoeny(userIdss, ststus, startTime, endTime));
        //Record totlaContractMoney = Db.findFirst(Db.getSqlPara("crm.Instrument.queryContractMoeny", Kv.by("userIds", userIdss).set("type", ststus).set("startTime", startTime).set("endTime", endTime)));
        Record totlaReceivablesMoney = JavaBeanUtil.mapToRecord(instrumentDao.queryReceivablesMoeny(userIdss, ststus, startTime, endTime));
        //Record totlaReceivablesMoney = Db.findFirst(Db.getSqlPara("crm.Instrument.queryReceivablesMoeny", Kv.by("userIds", userIdss).set("type", ststus).set("startTime", startTime).set("endTime", endTime)));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("list", recordList);
        jsonObject.put("totlaContractMoney", totlaContractMoney != null ? totlaContractMoney.getBigDecimal("money") : 0);
        jsonObject.put("totlaReceivablesMoney", totlaReceivablesMoney != null ? totlaReceivablesMoney.getBigDecimal("money") : 0);
        return R.ok().put("data", jsonObject);
    }

    public R sellFunnel(String type, String userIds, String startTime, String endTime, Integer typeId) {
        String[] userIdss = userIds.split(",");
        Record record = new Record();
        record.set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer ststus = biTimeUtil.analyzeType(type);
        List<Record> list = crmBiDao.sellFunnel(userIdss, ststus, startTime, endTime, typeId);
        Record sum_money = JavaBeanUtil.mapToRecord(crmBiDao.sellFunnelSum(null, userIdss, ststus, startTime, endTime, typeId));
        //Record sum_money = Db.findFirst(Db.getSqlPara("bi.funnel.sellFunnelSum", Kv.by("userIds", userIdss).set("type", ststus).set("startTime", startTime).set("endTime", endTime).set("typeId", typeId)));
        Record sum_shu = JavaBeanUtil.mapToRecord(crmBiDao.sellFunnelSum(2, userIdss, ststus, startTime, endTime, typeId));
        //Record sum_shu = Db.findFirst(Db.getSqlPara("bi.funnel.sellFunnelSum", Kv.by("userIds", userIdss).set("type", ststus).set("startTime", startTime).set("endTime", endTime).set("typeId", typeId).set("isEnd", 2)));
        Record sum_ying = JavaBeanUtil.mapToRecord(crmBiDao.sellFunnelSum(1, userIdss, ststus, startTime, endTime, typeId));
        //Record sum_ying = Db.findFirst(Db.getSqlPara("bi.funnel.sellFunnelSum", Kv.by("userIds", userIdss).set("type", ststus).set("startTime", startTime).set("endTime", endTime).set("typeId", typeId).set("isEnd", 1)));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("list", list);
        jsonObject.put("sum_money", sum_money != null ? sum_money.getBigDecimal("money") : 0);
        jsonObject.put("sum_shu", sum_shu != null ? sum_shu.getBigDecimal("money") : 0);
        jsonObject.put("sum_ying", sum_ying != null ? sum_ying.getBigDecimal("money") : 0);
        return R.ok().put("data", jsonObject);
    }

}
