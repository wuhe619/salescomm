package com.bdaim.log.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.common.util.DatetimeUtils;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.log.dto.UserOperLogDTO;
import com.bdaim.log.entity.OperLog;
import org.hibernate.Query;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class OperLogDao extends SimpleHibernateDao<OperLog, Serializable> {

    public void insert(OperLog t) {
        this.insert(t);
    }


    public OperLog getObj(OperLog t) {
        // TODO Auto-generated method stub
        return null;
    }


    public List<OperLog> getTopnObjectIdByDateAndType(String typeuri, int topn) {
        StringBuilder sb = new StringBuilder();
        sb.append("  SELECT t.oper_object_id ");
        sb.append("       , tli.labelName ");
        sb.append("		  , count(1) ");
        sb.append("    FROM OperLog t, LabelInfo tli ");
        sb.append("   where t.oper_object_id > 0 and tli.labelName is not null ");
        sb.append("     and t.oper_uri = :oper_uri ");
        sb.append("     and t.oper_object_id = tli.id ");
        sb.append("group by t.oper_object_id, tli.labelName ");
        sb.append("order by count(1) desc ");
//		Map mmp = new HashMap();
//		mmp.put("oper_uri", typeuri);
        Query query = super.createQuery(sb.toString());
        query.setString("oper_uri", typeuri);
        query.setMaxResults(topn);
        List<Object[]> lst = (List<Object[]>) query.list();
        List<OperLog> lstret = new ArrayList<OperLog>(lst.size() + 1);
        for (Object[] objs : lst) {
            try {
                OperLog onelog = new OperLog();
                onelog.setOper_object_id((Integer) objs[0]);
                onelog.setObject_name((String) objs[1]);
                onelog.setObject_count((Long) objs[2]);
                lstret.add(onelog);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return lstret;

    }

    public List<OperLog> getTopnPageByDate(Date date1, Date date2, int topn) {
        StringBuilder sb = new StringBuilder();
        sb.append("	SELECT t.oper_page_name, count(1) as page_count");
        sb.append("		FROM OperLog t ");
        sb.append("   where t.oper_page_name is not null ");
        sb.append("     and t.oper_datetime > ? and t.oper_datetime < ?");
        sb.append("	 group by t.oper_page_name ");
        sb.append("	 order by count(1) desc ");
        Query query = super.createQuery(sb.toString());
        query.setDate(0, date1);
        query.setDate(1, date2);
        query.setMaxResults(topn);
        List<Object[]> lst = (List<Object[]>) query.list();
        List<OperLog> lstret = new ArrayList<OperLog>(lst.size() + 1);
        for (Object[] objs : lst) {
            try {
                OperLog onelog = new OperLog();
                onelog.setOper_page_name((String) objs[0]);
                onelog.setPage_count((Long) objs[1]);
                lstret.add(onelog);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return lstret;
    }

    public long getTopnPageTotalCount(Date date1, Date date2) {
        StringBuilder sb = new StringBuilder();
        sb.append("	SELECT count(1) ");
        sb.append("		FROM OperLog t ");
        sb.append("   where t.oper_datetime > ? and t.oper_datetime < ?");
        sb.append("	 ");
        Query query = super.createQuery(sb.toString());
        query.setDate(0, date1);
        query.setDate(1, date2);
        return (Long) query.list().get(0);
    }

    public List<OperLog> getOperLogInfo(OperLog ol, Page page, Date date1, Date date2, String order_field, String order_asc) {
        StringBuilder sb = new StringBuilder();
        sb.append("	SELECT t.oper_uname");
        sb.append("		 , t.oper_source_ip ");
        sb.append("		 , t.oper_page_name ");
        sb.append("		 , count(1) as visit_count");
        sb.append("		 , max(oper_datetime) as oper_datetime");
        sb.append("		FROM OperLog t ");
        sb.append("	 where t.oper_uname is not null ");
        sb.append("	   and t.oper_source_ip is not null ");
        sb.append("	   and t.oper_page_name is not null ");
        if (null != ol.getOper_uname() && !"".equals(ol.getOper_uname()))
            sb.append("	     and t.oper_uname like :oper_uname ");
        if (null != ol.getOper_page_name() && !"".equals(ol.getOper_page_name()))
            sb.append("		 and t.oper_page_name like :oper_page_name ");
        if (null != date1)
            sb.append("		 and t.oper_datetime > :date1 ");
        if (null != date2)
            sb.append("		 and t.oper_datetime < :date2 ");
        sb.append(" group by t.oper_uname, t.oper_source_ip, t.oper_page_name ");
        sb.append(" order by '' ");
        if ("visit_count".equals(order_field))
            sb.append(" , count(1) ");
        else
            sb.append(" , count(1) ");
        if ("1".equals(order_asc))
            sb.append(" asc");
        else
            sb.append(" desc");
        Query query = super.createQuery(sb.toString());
        query.setFirstResult(page.getStart());
        query.setMaxResults(page.getLimit());
        if (null != date1)
            query.setTimestamp("date1", date1);
        if (null != date2)
            query.setTimestamp("date2", date2);
        if (null != ol.getOper_page_name() && !"".equals(ol.getOper_page_name()))
            query.setString("oper_page_name", "%" + ol.getOper_page_name() + "%");
        if (null != ol.getOper_uname() && !"".equals(ol.getOper_uname()))
            query.setString("oper_uname", "%" + ol.getOper_uname() + "%");
        List<Object[]> lst = (List<Object[]>) query.list();
        List<OperLog> lstret = new ArrayList<OperLog>(lst.size() + 1);
        for (Object[] objs : lst) {
            try {
                OperLog onelog = new OperLog();
                onelog.setOper_uname((String) objs[0]);
                onelog.setOper_source_ip((String) objs[1]);
                onelog.setOper_page_name((String) objs[2]);
                onelog.setVisit_count((Long) objs[3]);
                onelog.setOper_datetime((Date) objs[4]);
                lstret.add(onelog);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        float a = 0;
        return lstret;
    }

    public long getOperLogInfoTotalCount(OperLog ol, Date date1, Date date2) {
        StringBuilder sb = new StringBuilder();
        sb.append("	SELECT count(1) ");
        sb.append("		FROM OperLog t ");
        sb.append("	 where t.oper_uname is not null and t.oper_uname != '' ");
        sb.append("	   and t.oper_source_ip is not null ");
        sb.append("	   and t.oper_page_name is not null ");
        if (null != ol.getOper_uname() && !"".equals(ol.getOper_uname()))
            sb.append("	     and t.oper_uname like :oper_uname ");
        if (null != ol.getOper_page_name() && !"".equals(ol.getOper_page_name()))
            sb.append("		 and t.oper_page_name like :oper_page_name ");
        if (null != date1)
            sb.append("		 and t.oper_datetime > :date1 ");
        if (null != date2)
            sb.append("		 and t.oper_datetime < :date2 ");
        sb.append("group by t.oper_uname, t.oper_source_ip, t.oper_page_name ");
        Query query = super.createQuery(sb.toString());
        if (null != date1)
            query.setDate("date1", date1);
        if (null != date2)
            query.setDate("date2", date2);
        if (null != ol.getOper_page_name() && !"".equals(ol.getOper_page_name()))
            query.setString("oper_page_name", "%" + ol.getOper_page_name() + "%");
        if (null != ol.getOper_uname() && !"".equals(ol.getOper_uname()))
            query.setString("oper_uname", "%" + ol.getOper_uname() + "%");
        List<Object> lst = (List<Object>) query.list();
        return lst.size();
    }

    /**
     * 用户行为记录分页
     *
     * @param pageNum
     * @param pageSize
     * @param param
     * @return
     */
    public Page pageUserOperlog(int pageNum, int pageSize, UserOperLogDTO param, boolean groupBy, int beforeMonth) {
        StringBuilder hql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        hql.append(" FROM UserOperLog m WHERE 1=1");
        if (StringUtil.isNotEmpty(param.getUserId())) {
            hql.append(" AND m.userId = ?");
            params.add(NumberConvertUtil.parseLong(param.getUserId()));
        }
        if (StringUtil.isNotEmpty(param.getIp())) {
            hql.append(" AND m.ip = ?");
            params.add(param.getIp());
        }
        if (StringUtil.isNotEmpty(param.getClient())) {
            hql.append(" AND m.client = ?");
            params.add(param.getClient());
        }
        if (param.getEventType() > 0) {
            hql.append(" AND m.eventType = ?");
            params.add(param.getEventType());
        }
        if (StringUtil.isNotEmpty(param.getObjectCode())) {
            hql.append(" AND m.objectCode = ?");
            params.add(param.getObjectCode());
        }
        if (StringUtil.isNotEmpty(param.getProductType())) {
            hql.append(" AND m.objectCode IN(SELECT id FROM Dic WHERE dicTypeId = ?)");
            params.add(param.getProductType());
        }
        if (beforeMonth > 0) {
            LocalDateTime time = LocalDateTime.now().minusMonths(beforeMonth);
            hql.append(" AND m.createTime >= '" + DatetimeUtils.DATE_TIME_FORMATTER.format(time) + "' ");
        }
        Page page = page(hql.toString() + " ORDER BY m.createTime DESC ", params, pageNum, pageSize);
        if (groupBy) {
            hql.append(" GROUP BY m.objectCode, m.ip ");
            page.setData(this.createQuery(hql.toString(), params).setFirstResult(pageNum).setMaxResults(pageSize).list());
        }
        return page;
    }
}
