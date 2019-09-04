package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dto.CustomerLabelDTO;
import com.bdaim.customer.entity.CustomerLabel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @date 2019/6/19
 * @description
 */
@Component
public class CustomerLabelDao extends SimpleHibernateDao<CustomerLabel, String> {

    public CustomerLabel getLabelByProjectId(String projectId, String custId, String labelName) {
        CustomerLabel cp = null;
        String hql = "from CustomerLabel m where  custId =? and status = 1 and labelName = ?";
        if (StringUtil.isNotEmpty(projectId)) {
            hql += " and marketProjectId = '" + projectId + "'";
        } else {
            hql += " and marketProjectId is NULL";
        }
        List<CustomerLabel> list = this.find(hql, custId, labelName);
        if (list.size() > 0)
            cp = (CustomerLabel) list.get(0);
        return cp;
    }

    /**
     * 检查自建属性名称是否存在
     *
     * @param includeLabelId
     * @param name
     * @param status
     * @return
     */
    public List<CustomerLabel> listCustomerLabelNameExist(String includeLabelId, String name, int status, String custId) {
        List<Object> param = new ArrayList<>();
        param.add(name);
        param.add(custId);
        StringBuilder hql = new StringBuilder("FROM CustomerLabel m where m.labelName=? AND (m.custId = ? OR m.custId = 0) ");
        if (StringUtil.isNotEmpty(includeLabelId)) {
            hql.append(" AND m.labelId NOT IN(?) ");
            param.add(includeLabelId);
        }
        if (status > 0) {
            hql.append(" AND m.status = ? ");
            param.add(String.valueOf(status));
        }
        return this.find(hql.toString(), param);
    }


    public CustomerLabel getCustomerLabel(String labelId) {
        CustomerLabel cp = null;
        String hql = "from CustomerLabel m where m.labelId=? ";
        List<CustomerLabel> list = this.find(hql, labelId);
        if (list.size() > 0)
            cp = (CustomerLabel) list.get(0);
        return cp;
    }

    public CustomerLabel getCustomerLabel(String labelId, String projectId, int noStatus) {
        CustomerLabel cp = null;
        String hql = "from CustomerLabel m where m.labelId=?  AND m.status<>? ";
        if (StringUtil.isEmpty(projectId)) {
            hql += " AND m.marketProjectId IS NULL ";
        } else {
            hql += " AND m.marketProjectId = '" + projectId + "'";
        }
        List<CustomerLabel> list = this.find(hql, labelId, String.valueOf(noStatus));
        if (list.size() > 0)
            cp = (CustomerLabel) list.get(0);
        return cp;
    }

    public CustomerLabel getCustomerLabel(int id) {
        CustomerLabel cp = null;
        String hql = "from CustomerLabel m where m.id=?";
        List<CustomerLabel> list = this.find(hql, id);
        if (list.size() > 0)
            cp = (CustomerLabel) list.get(0);
        return cp;
    }

    /**
     * @param labelId
     * @param noStatus
     * @return
     */
    public List<CustomerLabel> listCustomerLabel(String labelId, int noStatus) {
        String hql = "from CustomerLabel m where m.labelId=? AND m.status<> ?";
        List<CustomerLabel> list = this.find(hql, labelId, String.valueOf(noStatus));
        return list;
    }

    /**
     * 查询客户下所有自建属性
     *
     * @param custId
     * @return
     */
    public List<CustomerLabel> listCustomerLabel(String custId) {
        String hql = "from CustomerLabel m where m.custId=? ";
        List<CustomerLabel> list = this.find(hql, custId);
        return list;
    }

    /**
     * 查询所有系统自建属性
     *
     * @return
     */
    public List<CustomerLabel> listSystemCustomerLabel() {
        String hql = "from CustomerLabel m where m.custId=? ";
        List<CustomerLabel> list = this.find(hql, "0");
        return list;
    }

    public int updateCustomerLabel(CustomerLabel customerLabel) {
        this.saveOrUpdate(customerLabel);
        return 1;
    }

    public CustomerLabel getCustomerLabelByName(String labelName, String custId) {
        CustomerLabel cp = null;
        String hql = "from CustomerLabel m where m.labelName=? and custId=?";
        List<CustomerLabel> list = this.find(hql, labelName, custId);
        if (list.size() > 0)
            cp = (CustomerLabel) list.get(0);
        return cp;
    }

    /**
     * 获取自建属性列表
     *
     * @param custId          客户ID
     * @param marketProjectId 项目ID
     * @param sort            是否需要排序
     * @return
     */
    public List<CustomerLabelDTO> listLabelIds(String custId, int marketProjectId, boolean sort) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, label_name, label_id, type FROM t_customer_label WHERE status = 1 AND (cust_id =? OR cust_id = '0') AND (market_project_id = 0 OR market_project_id is null OR market_project_id =?) ");
        if (sort) {
            sql.append(" ORDER BY sort IS NULL, sort, label_name ");
        }
        List<Map<String, Object>> list = this.sqlQuery(sql.toString(), custId, marketProjectId);
        List<CustomerLabelDTO> result = new ArrayList<>();
        CustomerLabelDTO dto;
        for (Map<String, Object> map : list) {
            dto = new CustomerLabelDTO();
            dto.setId(NumberConvertUtil.parseInt(map.get("id")));
            dto.setLabelId(String.valueOf(map.get("label_id")));
            dto.setLabelName(String.valueOf(map.get("label_name")));
            dto.setType(NumberConvertUtil.parseInt(map.get("type")));
            result.add(dto);
        }
        return result;
    }

}
