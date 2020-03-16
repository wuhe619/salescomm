package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InstrumentDao extends SimpleHibernateDao {

    public Map intraDay(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map yesterday(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map thisWeek(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map lastWeek(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now())  -1AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map theSameMonth(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map lastMonth(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }


    public Map currentSeason(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map precedingQuarter(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map thisYear(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map lastYear(String[] userIds) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        List<Map<String, Object>> list = sqlQuery(sql);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map custom(String[] userIds, String startTime, String endTime) {
        String sql = "select distinct " +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as businessCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contacts WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contactsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_contract WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_customer WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as customerCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_product WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as productCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_leads WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as leadsCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_receivables WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_admin_record WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordCount,\n" +
                "      (SELECT COUNT(*) FROM lkcrm_crm_business_change WHERE TO_DAYS(create_time) >= TO_DAYS(:startTime) and  TO_DAYS(create_time) <= TO_DAYS(:endTime) AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as recordStatusCount\n" +
                "      FROM lkcrm_crm_customer";
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        List<Map<String, Object>> list = findMapBySql(sql, params);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Map queryMoneys(String[] userIds, String startTime, String endTime) {
        String sql = "SELECT distinct " +
                "    (select IFNULL(SUM(money),0) FROM lkcrm_crm_contract where DATE_FORMAT(create_time,'%Y%m') between :startTime  and :endTime AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as contractMoneys, " +
                "    (select IFNULL(SUM(money),0) FROM lkcrm_crm_receivables where DATE_FORMAT(create_time,'%Y%m') between  :startTime  and :endTime AND create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") as receivablesMoneys " +
                "     FROM lkcrm_crm_contract ";
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        List<Map<String, Object>> list = findMapBySql(sql, params);
        return list.size() > 0 ? list.get(0) : null;
    }

    public Page queryBulletinInfo(int pageNum, int pageSize, String[] userIds, String viewName, String sortField, String orderType, boolean tn, String beginDate, String endDate) {
        List param = new ArrayList();
        param.add(beginDate);
        param.add(endDate);
        viewName = BaseUtil.getViewSqlNotASName(viewName);
        String sql = " select * from " + viewName + " as a where 1=1 and (a.create_time between ? and ?) and create_user_id in ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ";
        if (tn) {
            param.add(beginDate);
            param.add(endDate);
            sql += "and (SELECT COUNT(*) FROM lkcrm_crm_business_change as b WHERE b.business_id = a.business_id and (b.create_time between ? and ?))>0 ";
        }
        param.add(sortField);
        param.add(orderType);
        sql += "order by ? ? ";
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

   /* public List queryTargets(String[] userIds,String[] deptIds, int status, String year) {
        String sql = "SELECT january , february , march , april , may  , june , july , august  , september , october , november , december FROM lkcrm_crm_achievement\n" +
                     " WHERE year = ：year ";
        if(){}
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        List<Map<String, Object>> list = findMapBySql(sql, params);
        return list.size() > 0 ? list.get(0) : null;
    }*/

    public Map queryContractMoeny(String[] userIds, int type, String startTime, String endTime) {
        List param = new ArrayList();
        String sql = " SELECT IFNULL(SUM(money),0) as money\n" +
                "      FROM lkcrm_crm_contract\n" +
                "      where check_status = 2\n" +
                "      and  owner_user_id in  ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ";
        switch (type) {
            case 1:
                sql += " and to_days(NOW()) = TO_DAYS(order_date)";
                break;
            case 2:
                sql += " and to_days(NOW()) - TO_DAYS(order_date) = 1 ";
                break;
            case 3:
                sql += " and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) ";
                break;
            case 4:
                sql += "  and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
                break;
            case 5:
                sql += "  and date_format(order_date,'%Y-%m')=date_format(now(),'%Y-%m') ";
                break;
            case 6:
                sql += "  and date_format(order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')";
                break;
            case 7:
                sql += "  and QUARTER(order_date)=QUARTER(now()) AND YEAR(order_date)=YEAR(NOW())";
                break;
            case 8:
                sql += " and QUARTER(order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))";
                break;
            case 9:
                sql += "  and YEAR(order_date)=YEAR(NOW()) ";
                break;
            case 10:
                sql += "  and YEAR(order_date)=YEAR(date_sub(now(),interval 1 year)) ";
                break;
            case 11:
                param.add(startTime);
                param.add(endTime);
                sql += "  and  TO_DAYS(order_date) >= TO_DAYS(?) and  TO_DAYS(order_date) <= TO_DAYS(?)";
                break;

        }
        return queryUniqueSql(sql, param.toArray());
    }

    public Map queryReceivablesMoeny(String[] userIds, int type, String startTime, String endTime) {
        List param = new ArrayList();
        String sql = " SELECT IFNULL(SUM(money),0) as money\n" +
                "      FROM lkcrm_crm_receivables\n" +
                "      where check_status = 2" +
                "      and  owner_user_id in  ( " + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ";
        switch (type) {
            case 1:
                sql += " and to_days(NOW()) = TO_DAYS(return_time) ";
                break;
            case 2:
                sql += " and to_days(NOW()) - TO_DAYS(return_time) = 1 ";
                break;
            case 3:
                sql += " and YEARWEEK(date_format(return_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
                break;
            case 4:
                sql += "  and YEARWEEK(date_format(return_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
                break;
            case 5:
                sql += "  and date_format(return_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
                break;
            case 6:
                sql += "   and date_format(return_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')";
                break;
            case 7:
                sql += "  and QUARTER(return_time)=QUARTER(now()) AND YEAR(return_time)=YEAR(NOW()) ";
                break;
            case 8:
                sql += " and QUARTER(return_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(return_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) ";
                break;
            case 9:
                sql += "    and YEAR(return_time)=YEAR(NOW()) ";
                break;
            case 10:
                sql += "   and YEAR(return_time)=YEAR(date_sub(now(),interval 1 year)) ";
                break;
            case 11:
                param.add(startTime);
                param.add(endTime);
                sql += "  and  TO_DAYS(return_time) >= TO_DAYS(?) and  TO_DAYS(return_time) <= TO_DAYS(?)";
                break;

        }
        return queryUniqueSql(sql, param.toArray());
    }

}
