package com.bdaim.appointmentcallback.dao;

import com.bdaim.appointmentcallback.dto.AppointmentCallbackQueryParam;
import com.bdaim.appointmentcallback.entity.AppointmentCallback;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/12
 * @description
 */
@Component
public class AppointmentCallbackDao extends SimpleHibernateDao<AppointmentCallback, Serializable> {

    public Page pageSearch(AppointmentCallbackQueryParam model, String userGroupId, List<String> cgIds) {
        List<Object> params = new ArrayList<>();
        params.add(model.getCustId());
        StringBuilder sql = new StringBuilder();
        sql.append("FROM AppointmentCallback t WHERE t.custId = ? ");
        if (model.getCustomerGroupId() != null) {
            sql.append(" AND t.customerGroupId = ? ");
            params.add(model.getCustomerGroupId());
        }
        if (StringUtil.isNotEmpty(model.getSuperid())) {
            sql.append(" AND t.superid = ? ");
            params.add(model.getSuperid());
        }
        if (StringUtil.isNotEmpty(model.getOperator())) {
            sql.append(" AND t.operator = ? ");
            params.add(model.getOperator());
        }
        if (model.getStatus() != null) {
            sql.append(" AND t.status = ? ");
            params.add(NumberConvertUtil.parseInt(model.getStatus()));
        }
        if (StringUtil.isNotEmpty(model.getAppointmentStartTime())
                && StringUtil.isNotEmpty(model.getAppointmentEndTime())) {
            sql.append(" AND t.appointmentTime BETWEEN ? AND ? ");
            params.add(model.getAppointmentStartTime());
            params.add(model.getAppointmentEndTime());
        }
        if (StringUtil.isNotEmpty(userGroupId)) {
            sql.append(" AND t.operator IN (SELECT userId FROM CustomerUserGroupRel WHERE groupId = ? AND status = 1 ) ");
            params.add(userGroupId);
        }
        // 任务ID搜索
        if (StringUtil.isNotEmpty(model.getMarketTaskId())) {
            sql.append(" AND t.marketTaskId = ? ");
            params.add(model.getMarketTaskId());
        }
        // 任务名称模糊搜索
        if (StringUtil.isNotEmpty(model.getMarketTaskName())) {
            sql.append(" AND t.marketTaskId IN (SELECT id FROM MarketTask WHERE name LIKE ?) ");
            params.add("%" + model.getMarketTaskName() + "%");
        }
        //如果登陆人是项目管理员，只能查看自己负责的项目
        if (cgIds != null && cgIds.size() > 0) {
            sql.append(" and t.customerGroupId in(" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ")");
        }

        return this.page(sql.toString(), params, model.getPageNum(), model.getPageSize());
    }
}
