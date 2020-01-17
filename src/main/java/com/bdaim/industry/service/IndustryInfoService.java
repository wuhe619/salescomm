package com.bdaim.industry.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.label.dao.IndustryInfoDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.transform.Transformers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Service("IndustryInfoService")
@Transactional
public class IndustryInfoService {
    private static Log log = LogFactory.getLog(IndustryInfoService.class);
    @Resource
    private IndustryInfoDao industryInfoDao;
    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 查看行业
     */
    public List getIndustryInfoList(Integer pageNum, Integer pageSize, String industryInfoId) {
        StringBuffer sql = new StringBuffer();
        List args=new ArrayList();
        sql.append(
                "SELECT industy_name,status,industry_info_id,create_time,FORMAT(price/100,2) as price ,description from t_industry_info where 1=1 ");
        if (null != industryInfoId && !"".equals(industryInfoId)) {
            sql.append("and industry_info_id =?" );
            args.add(industryInfoId);
        }
        sql.append(" ORDER BY create_time desc");
//        sql.append(" LIMIT " + pageNum + "," + pageSize);
        sql.append(" limit ?,?");
        args.add(pageNum);
        args.add(pageSize);
        List list = jdbcTemplate.queryForList(sql.toString(),args);
//        List list = industryInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
        return list;
    }

    public List getIndustryInfoListTotal(String industryInfoId) {
        StringBuffer sql = new StringBuffer();
        List args = new ArrayList();
        sql.append(
                "SELECT count(*)as total from t_industry_info where 1=1 ");
        if (null != industryInfoId && !"".equals(industryInfoId)) {
            sql.append("and industry_info_id =" + industryInfoId);
            args.add(industryInfoId);
        }
        //List list = industryInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
        List list = jdbcTemplate.queryForList(sql.toString(),args);
        return list;
    }

    /**
     * 新增行业信息
     */
    public String addIndustryInfo(String industryName, String description, Integer status, Double price) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        String sql = "insert INTO t_industry_info SET industy_name=?,`STATUS`=?,create_time=NOW(),price=?,description=? ";
        int code = jdbcTemplate.update(sql, new Object[]{industryName, status, price * 100, description});
        map.put("code", code);
        if (code == 1) {
            map.put("message", "成功");
        } else {
            map.put("message", "失败");
        }
        log.info("新增行业信息，sql：" + sql);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 修改行业信息
     */
    public String updateIndustryInfo(String industryInfoId, String industryName, String description, Integer status,
                                     Double price) {
        JSONObject json = new JSONObject();
        List list = new ArrayList();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("update t_industry_info set  ");
        // 行业名称
        if (null != industryName && !"".equals(industryName)) {
            sb.append("industy_name=?,");
            list.add(industryName);
        }
        // 行业描述
        if (null != description && !"".equals(description)) {
            sb.append("description=?,");
            list.add(description);
        }
        // 状态
        if (null != status && !"".equals(status)) {
            sb.append("status=?,");
            list.add(status);
        }
        // 价格
        if (null != price && !"".equals(price)) {
            sb.append("price=?,");
            list.add(price * 100);
        }
        sb.append(" modify_time=now()");
        sb.append(" where industry_info_id=?");
        list.add(industryInfoId);
        int code = jdbcTemplate.update(sb.toString(),list.toArray());
        map.put("code", code);
        if (code == 1) {
            map.put("message", "成功");
        } else {
            map.put("message", "失败");
        }
        log.info("修改行业信息，sql:" + sb.toString()+String.valueOf(list.toArray().toString()));
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 查询所有有效行业
     *
     * @return
     */
    public List listIndustryInfo() {
        StringBuffer hql = new StringBuffer();
        hql.append(
                " FROM Industry m WHERE m.status = 1 ");
        hql.append(" ORDER BY m.createTime DESC");
        List list = industryInfoDao.find(hql.toString());
        return list;
    }

}
