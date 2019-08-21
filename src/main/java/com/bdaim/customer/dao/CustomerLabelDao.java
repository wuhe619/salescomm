package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dto.CustomerLabelDTO;
import com.bdaim.customer.entity.CustomerLabelDO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @date 2019/6/19
 * @description
 */
@Component
public class CustomerLabelDao extends SimpleHibernateDao<CustomerLabelDO, String> {

    public CustomerLabelDO getLabelByProjectId(String projectId, String custId, String labelName) {
        CustomerLabelDO cp = null;
        String hql = "from CustomerLabelDO m where  custId =? and status = 1 and labelName = ?";
        if (StringUtil.isNotEmpty(projectId)) {
            hql += " and marketProjectId = '" + projectId + "'";
        } else {
            hql += " and marketProjectId is NULL";
        }
        List<CustomerLabelDO> list = this.find(hql, custId, labelName);
        if (list.size() > 0)
            cp = (CustomerLabelDO) list.get(0);
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
    public List<CustomerLabelDO> listCustomerLabelNameExist(String includeLabelId, String name, int status) {
        List<Object> param = new ArrayList<>();
        param.add(name);
        StringBuilder hql = new StringBuilder("FROM CustomerLabelDO m where m.labelName=? ");
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


    public CustomerLabelDO getCustomerLabel(String labelId) {
        CustomerLabelDO cp = null;
        String hql = "from CustomerLabelDO m where m.labelId=? ";
        List<CustomerLabelDO> list = this.find(hql, labelId);
        if (list.size() > 0)
            cp = (CustomerLabelDO) list.get(0);
        return cp;
    }

    public CustomerLabelDO getCustomerLabel(String labelId, String projectId, int noStatus) {
        CustomerLabelDO cp = null;
        String hql = "from CustomerLabelDO m where m.labelId=?  AND m.status<>? ";
        if (StringUtil.isEmpty(projectId)) {
            hql += " AND m.marketProjectId IS NULL ";
        } else {
            hql += " AND m.marketProjectId = '" + projectId + "'";
        }
        List<CustomerLabelDO> list = this.find(hql, labelId, String.valueOf(noStatus));
        if (list.size() > 0)
            cp = (CustomerLabelDO) list.get(0);
        return cp;
    }

    public CustomerLabelDO getCustomerLabel(int id) {
        CustomerLabelDO cp = null;
        String hql = "from CustomerLabelDO m where m.id=?";
        List<CustomerLabelDO> list = this.find(hql, id);
        if (list.size() > 0)
            cp = (CustomerLabelDO) list.get(0);
        return cp;
    }

    /**
     * @param labelId
     * @param noStatus
     * @return
     */
    public List<CustomerLabelDO> listCustomerLabel(String labelId, int noStatus) {
        String hql = "from CustomerLabelDO m where m.labelId=? AND m.status<> ?";
        List<CustomerLabelDO> list = this.find(hql, labelId, String.valueOf(noStatus));
        return list;
    }

    /**
     * 查询客户下所有自建属性
     *
     * @param custId
     * @return
     */
    public List<CustomerLabelDO> listCustomerLabel(String custId) {
        String hql = "from CustomerLabelDO m where m.custId=? ";
        List<CustomerLabelDO> list = this.find(hql, custId);
        return list;
    }

    /**
     * 查询所有系统自建属性
     *
     * @return
     */
    public List<CustomerLabelDO> listSystemCustomerLabel() {
        String hql = "from CustomerLabelDO m where m.custId=? ";
        List<CustomerLabelDO> list = this.find(hql, "0");
        return list;
    }

    public int updateCustomerLabel(CustomerLabelDO customerLabel) {
        this.saveOrUpdate(customerLabel);
        return 1;
    }

    public CustomerLabelDO getCustomerLabelByName(String labelName, String custId) {
        CustomerLabelDO cp = null;
        String hql = "from CustomerLabelDO m where m.labelName=? and custId=?";
        List<CustomerLabelDO> list = this.find(hql, labelName, custId);
        if (list.size() > 0)
            cp = (CustomerLabelDO) list.get(0);
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
