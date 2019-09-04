package com.bdaim.log.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.StringUtil;
import com.bdaim.log.dto.SuperDataOperLogQuery;
import org.springframework.stereotype.Component;
import com.bdaim.common.dto.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/7/9
 * @description
 */
@Component
public class SuperDataOperLogDao extends SimpleHibernateDao {

    /**
     * 转交记录分页
     *
     * @param param
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageSuperDataOperLog(SuperDataOperLogQuery param, int pageNum, int pageSize) {
        StringBuilder hql = new StringBuilder();
        hql.append(" FROM SuperDataOperLog m WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
       /* if (StringUtil.isNotEmpty(param.getDataId())) {
            hql.append(" AND m.listId = ? ");
            params.add(param.getDataId());
        }*/
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            hql.append(" AND m.listId = ? ");
            params.add(param.getDataId());
        }
        if (StringUtil.isNotEmpty(param.getCustomerGroupId())) {
            hql.append(" AND m.customerGroupId = ? ");
            params.add(param.getCustomerGroupId());
        }
        if (StringUtil.isNotEmpty(param.getSeaId())) {
            //hql.append(" AND m.marketSeaId = ? ");
            hql.append(" AND m.customerSeaId = ? ");
            params.add(param.getSeaId());
        }
        if (param.getUserId() != null && param.getUserId() > 0) {
            hql.append(" AND m.userId = ? ");
            params.add(param.getUserId());
        }
        if (param.getEventType() != null && param.getEventType() > 0) {
            hql.append(" AND m.eventType = ? ");
            params.add(param.getEventType());
        }
        return page(hql.toString(), params, pageNum, pageSize);
    }
}
