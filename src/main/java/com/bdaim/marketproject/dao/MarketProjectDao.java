package com.bdaim.marketproject.dao;


import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dto.CustomerPropertyEnum;
import com.bdaim.marketproject.dto.MarketProjectDTO;
import com.bdaim.marketproject.dto.MarketProjectParam;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.marketproject.entity.MarketProjectProperty;
import com.bdaim.rbac.dto.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/19
 * @description
 */
@Component
public class MarketProjectDao extends SimpleHibernateDao<MarketProject, Integer> {


    public MarketProjectProperty getProperty(String marketProjectId, String propertyName) {
        MarketProjectProperty cp = null;
        String hql = "from MarketProjectProperty m where m.marketProjectId=? and m.propertyName=?";
        List<MarketProjectProperty> list = this.find(hql, marketProjectId, propertyName);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    /**
     * 营销项目单个查询
     *
     * @param id
     * @return
     */
    public MarketProject selectMarketProject(int id) {
        String hql = "from MarketProject m WHERE m.id = ? ORDER BY m.createTime DESC ";
        MarketProject result = this.findUnique(hql, id);
        return result;
    }

    /**
     * 营销项目分页列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageMarketProject(int pageNum, int pageSize, MarketProjectDTO marketProjectDTO) {
        List<Object> params = new ArrayList<>();
        StringBuffer hql = new StringBuffer();
        hql.append("from MarketProject m where 1=1 ");
        if (marketProjectDTO.getId() != null) {
            hql.append(" AND m.id = " + marketProjectDTO.getId());
        }
        if (StringUtil.isNotEmpty(marketProjectDTO.getName())) {
            hql.append(" AND m.name LIKE '%" + marketProjectDTO.getName() + "%'");
        }
        //type  0:全部 1：全局 2 企业项目
        if ("2".equals(marketProjectDTO.getType()) || StringUtil.isNotEmpty(marketProjectDTO.getEnterpriseName())) {
           /* hql.append(" AND ( m.id IN (SELECT p.propertyValue from Customer c,CustomerPropertyDO p where p.custId = c.custId ");
            if (StringUtil.isNotEmpty(marketProjectDTO.getEnterpriseName())) {
                hql.append(" AND c.enterpriseName LIKE '%" + marketProjectDTO.getEnterpriseName() + "%'");
            }
            hql.append(" AND p.propertyName LIKE '" + CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + "%')");*/
            hql.append(" AND ( m.custId IS NOT NULL ");
            if (StringUtil.isNotEmpty(marketProjectDTO.getEnterpriseName())) {
                hql.append(" AND m.custId in (SELECT custId FROM Customer WHERE  enterpriseName LIKE '%" + marketProjectDTO.getEnterpriseName() + "%')");
            }
            //全部时企业项目需要添加上全局的项目进行展示
            if ("0".equals(marketProjectDTO.getType()) && StringUtil.isNotEmpty(marketProjectDTO.getEnterpriseName())) {
                hql.append(" OR (m.custId is null AND  (SELECT coalesce(COUNT(p.propertyValue),0)");
                hql.append(" FROM CustomerPropertyDO p");
                hql.append("  WHERE  p.propertyName LIKE 'marketProjectId_%' AND p.propertyValue=m.id )=0)");
            }
            hql.append("))");
        }
        if ("1".equals(marketProjectDTO.getType())) {
            if (StringUtil.isEmpty(marketProjectDTO.getEnterpriseName())) {
                hql.append(" AND m.custId is null ");
            } else {
                return null;
            }
        }
        hql.append(" ORDER BY m.createTime DESC ");
        Page page = page(hql.toString(), params, pageNum, pageSize);
        return page;
    }

    /**
     * 营销项目列表
     *
     * @return
     */
    public List<MarketProjectDTO> listMarketProject(MarketProjectDTO marketProjectDTO) {
        StringBuffer hql = new StringBuffer();
        hql.append("from MarketProject m where 1=1 ");
        if (marketProjectDTO.getId() != null) {
            hql.append(" AND m.id = " + marketProjectDTO.getId());
        }
        if (StringUtil.isNotEmpty(marketProjectDTO.getName())) {
            hql.append(" AND m.name LIKE '%" + marketProjectDTO.getName() + "%'");
        }
        if (marketProjectDTO.getStatus() != null) {
            hql.append(" AND m.status = " + marketProjectDTO.getStatus());
        }
        if (marketProjectDTO.getCustId() != null) {
            hql.append(" AND m.custId = '" + marketProjectDTO.getCustId() + "'");
        }
        //type  0:全部 1：全局 2 企业项目
        if ("2".equals(marketProjectDTO.getType()) || StringUtil.isNotEmpty(marketProjectDTO.getEnterpriseName())) {
            hql.append(" AND ( m.custId IS NOT NULL ");
            if (StringUtil.isNotEmpty(marketProjectDTO.getEnterpriseName())) {
                hql.append(" AND m.custId in (SELECT custId FROM Customer WHERE  enterpriseName LIKE '%" + marketProjectDTO.getEnterpriseName() + "%')");
            }
            //全部时企业项目需要添加上全局的项目进行展示
            if ("0".equals(marketProjectDTO.getType()) && StringUtil.isNotEmpty(marketProjectDTO.getEnterpriseName())) {
                hql.append(" OR (m.custId is null AND  (SELECT coalesce(COUNT(p.propertyValue),0)");
                hql.append(" FROM CustomerPropertyDO p");
                hql.append("  WHERE  p.propertyName LIKE 'marketProjectId_%' AND p.propertyValue=m.id )=0)");
            }
            hql.append("))");
        }
        if ("1".equals(marketProjectDTO.getType())) {
            if (StringUtil.isEmpty(marketProjectDTO.getEnterpriseName())) {
                hql.append(" AND m.custId is null ");
            } else {
                return null;
            }
        }

        hql.append(" ORDER BY m.createTime DESC ");
        List<MarketProject> list = this.find(hql.toString());
        List<MarketProjectDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            for (MarketProject s : list) {
                result.add(new MarketProjectDTO(s.getId(), s.getName(), s.getIndustryId(), s.getStatus(), s.getCreateTime()));
            }
        }
        return result;
    }

    /**
     * 客户关联的营销项目
     *
     * @return
     */
    public List<MarketProjectDTO> listCustomerMarketProject(String custId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from MarketProject m where 1=1 ");
        hql.append(" AND m.id IN (SELECT propertyValue FROM CustomerPropertyDO WHERE propertyName LIKE '" + CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + "%' AND custId = ? AND propertyValue <> '')");
        hql.append(" AND m.status = 1 ");
        hql.append(" ORDER BY m.createTime DESC ");
        List<MarketProject> list = this.find(hql.toString(), custId);
        List<MarketProjectDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            for (MarketProject s : list) {
                result.add(new MarketProjectDTO(s.getId(), s.getName(), s.getIndustryId(), s.getStatus(), s.getCreateTime()));
            }
        }
        return result;
    }

    /**
     * 前台项目分页
     *
     * @param pageNum
     * @param pageSize
     * @param param
     * @param projectIds
     * @return
     */
    public Page pageCustomerMarketProject(int pageNum, int pageSize, MarketProjectParam param, List<String> projectIds) {
        List<Object> params = new ArrayList<>();
        params.add(param.getCustId());
        StringBuffer hql = new StringBuffer();
        hql.append(" from MarketProject m where 1=1 ");
        hql.append(" AND m.id IN (SELECT propertyValue FROM CustomerPropertyDO WHERE propertyName LIKE '" + CustomerPropertyEnum.MARKET_PROJECT_ID_PREFIX.getKey() + "%' AND custId = ? AND propertyValue <> '')");
        hql.append(" AND m.status = 1 ");
        if (param.getId() != null) {
            hql.append(" AND m.id = ?");
            params.add(param.getId());
        }
        if (StringUtil.isNotEmpty(param.getName())) {
            hql.append(" AND m.name LIKE '%" + param.getName() + "%'");
        }
        if (param.getStatus() != null) {
            hql.append(" AND m.status = ? ");
            params.add(param.getStatus());
        }
        // 项目管理员检索
        if (projectIds != null && projectIds.size() > 0) {
            hql.append(" AND m.id (?) ");
            params.add(projectIds);
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) && StringUtil.isNotEmpty(param.getEndTime())) {
            hql.append(" AND m.createTime BETWEEN ? AND ? ");
            params.add(param.getStartTime());
            params.add(param.getEndTime());
        } else if (StringUtil.isNotEmpty(param.getStartTime())) {
            hql.append(" AND m.createTime >= ? ");
            params.add(param.getStartTime());
        } else if (StringUtil.isNotEmpty(param.getEndTime())) {
            hql.append(" AND m.createTime <= ? ");
            params.add(param.getEndTime());
        }

        hql.append(" ORDER BY m.createTime DESC ");
        return page(hql.toString(), params, pageNum, pageSize);
    }
}
