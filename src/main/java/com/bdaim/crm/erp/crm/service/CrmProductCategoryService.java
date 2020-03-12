package com.bdaim.crm.erp.crm.service;

import com.bdaim.crm.dao.LkCrmProductCategoryDao;
import com.bdaim.crm.entity.LkCrmProductCategoryEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class CrmProductCategoryService {

    @Resource
    private LkCrmProductCategoryDao crmProductCategoryDao;

    /**
     * 根据id查询类别
     *
     * @return
     */
    public LkCrmProductCategoryEntity queryById(Integer id) {
        return crmProductCategoryDao.get(id);
    }

    /**
     * 添加或修改类别
     */
    public R saveAndUpdate(LkCrmProductCategoryEntity category) {
        category.setCustId(BaseUtil.getCustId());
        if (category.getCategoryId() == null) {
            if (category.getPid() == null) {
                category.setPid(0);
            }
            return (int) crmProductCategoryDao.saveReturnPk(category) > 0 ? R.ok() : R.error();
        } else {
            crmProductCategoryDao.update(category);
            return R.ok();
        }
    }

    /**
     * 根据ID删除类别
     */
    public R deleteById(Integer id) {
        Integer number = crmProductCategoryDao.queryByCategoryId(id);
        Integer catagoryNum = crmProductCategoryDao.queryCategoryByParentId(id);
        if (number > 0) {
            return R.error("该产品类别已关联产品，不能删除！");
        }
        if (catagoryNum > 0) {
            return R.error("该类别下有其他产品类别！");
        }
        crmProductCategoryDao.delete(id);
        return R.ok();
    }

    /**
     * 迭代查询全部产品类别
     */
    public R queryList() {
        return R.ok().put("data", queryListById(null, 0));
    }

    public List<Record> queryListByPid(Integer pid) {
        StringBuffer sql = new StringBuffer("select * from lkcrm_crm_product_category WHERE cust_id = ? ");
        List param = new ArrayList();
        param.add(BaseUtil.getCustId());
        if (pid != null) {
            param.add(pid);
            sql.append(" AND  pid = ?");
        }
        return JavaBeanUtil.mapToRecords(crmProductCategoryDao.sqlQuery(sql.toString(), param.toArray()));
    }

    /**
     * 递归查询全部产品类别
     */
    public List<Record> queryListById(List<Record> records, Integer level) {
        if (records == null) {
            records = queryListByPid(0);
            return queryListById(records, level);
        } else {
            level = level + 1;
            for (Record c : records) {
                c.set("level", level);
                c.set("label", c.getStr("name"));
                List<Record> list = queryListById(queryListByPid(c.getInt("category_id")), level);
                if (list != null && list.size() != 0) {
                    c.set("children", list);
                }

            }
            return records;
        }
    }

    public List<Integer> queryId(List<Integer> list, Integer categoryId) {

        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(categoryId);
        LkCrmProductCategoryEntity productCategory = crmProductCategoryDao.get(categoryId);
        if (productCategory != null && productCategory.getPid() != 0) {
            queryId(list, productCategory.getPid());
        } else {
            Collections.reverse(list);
        }

        return list;
    }
}
