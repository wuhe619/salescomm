package com.bdaim.customs.dao;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.HBusiDataManager;
import org.hibernate.Query;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
public class HBusiDataManagerDao extends SimpleHibernateDao<HBusiDataManager, Serializable> {

    /**
     * 根据字段名称和值查询单个对象
     *
     * @param propertyName
     * @param value
     * @return
     */
    public HBusiDataManager getHBusiDataManager(String propertyName, String value) {
        List<HBusiDataManager> list = this.findBy(propertyName, value);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 通过主单查询分单集合
     *
     * @param pid
     * @return
     */
    public List<HBusiDataManager> listHBusiDataManager0(int pid, String type) {
        String hql = "FROM HBusiDataManager WHERE type = ? AND ext_4 = (SELECT ext_3 FROM HBusiDataManager WHERE id = ?)";
        return this.find(hql, type, pid);
    }


    public List<HBusiDataManager> listHBusiDataManager(List ids, String type) {
        String hql = "FROM HBusiDataManager WHERE type = ? AND id IN (:ids)";
        Query query = super.createQuery(hql, type);
        query.setParameterList("ids", ids);
        return query.list();
    }

    /**
     * 查询主单下的分单
     * @param pid
     * @param type
     * @param idCardPhotoStatus 1-有身份证图片 2-无
     * @param idCardCheckStatus 1-身份证校验通过 2-无
     * @return
     */
    public List<HBusiDataManager> listFDIdCard(int pid, String type, int idCardPhotoStatus, int idCardCheckStatus) {
        StringBuilder hql = new StringBuilder("FROM HBusiDataManager WHERE type = ? AND JSON_EXTRACT(content, '$.pid')=?  ");
        // 有身份照片
        if (1 == idCardPhotoStatus) {
            hql.append(" AND ext_6 IS NOT NULL AND ext_6 <>'' ");
        } else if (2 == idCardPhotoStatus) {
            hql.append(" AND (ext_6 IS NULL OR ext_6 ='') ");
        }
        //身份核验结果通过
        if (1 == idCardCheckStatus) {
            hql.append(" AND ext_7 = 1 ");
        } else if (2 == idCardCheckStatus) {
            hql.append(" AND (ext_7 IS NULL OR ext_7 ='' OR ext_7 =2) ");
        }
        return this.find(hql.toString(), type, pid);
    }

    /**
     * 查询主单下分单的身份证照片数量
     *
     * @param pid
     * @param type
     * @return
     */
    public int countMainDIdCardNum(int pid, String type) {
        String sql = "select id from h_data_manager where type=? AND ext_6 IS NOT NULL AND ext_6 <>'' and ( CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')="+pid +" ELSE null END  or CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')='"+pid+"' ELSE null END)";
        List<Map<String,Object>> list2 = jdbcTemplate.queryForList(sql, type);
        List<HBusiDataManager> list = JSON.parseArray(JSON.toJSONString(list2),HBusiDataManager.class);
        if(list!=null){
            return list.size();
        }
        return 0;
    }

}
