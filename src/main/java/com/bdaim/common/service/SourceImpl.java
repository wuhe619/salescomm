package com.bdaim.common.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.label.entity.LabelInfo;
import com.bdaim.label.service.CommonService;
import org.apache.log4j.Logger;
import org.hibernate.transform.Transformers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Service("SourceService")
@Transactional
public class SourceImpl implements SourceService {
    private static Logger logger = Logger.getLogger(SourceImpl.class);
    @Resource
    com.bdaim.resource.dao.SourceDao sourceDao;

    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    private CommonService commonService;

    @Override
    public String listDataSource() {
        JSONObject json = new JSONObject();
        String sql = "SELECT source_name,source_id,label_num,status,source_price/100  source_price from t_source ";
        List list = sourceDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
        if (list != null && list.size() > 0) {
            Map<String, Object> map;
            for (int i = 0; i < list.size(); i++) {
                map = (Map<String, Object>) list.get(i);
                if (map != null) {
                    if ("null".equals(String.valueOf(map.get("label_num")))) {
                        map.put("label_num", 0);
                    }
                    if ("null".equals(String.valueOf(map.get("source_price")))) {
                        map.put("source_price", 0);
                    }
                }
            }
        }
        json.put("data", list);
        logger.info("查询数据源信息。");
        return json.toJSONString();
    }

    @Override
    public String updateSourceStatus(Integer sourceId, Integer status) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        String sql = "update  t_source set status=? where source_id=?";
        int code = jdbcTemplate.update(sql, new Object[]{status, sourceId});
        map.put("code", code);
        if (code == 1) {
            map.put("message", "成功");
        } else {
            map.put("message", "失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }

    @Override
    public String listLabelsByCondition(String id, String status) {
        JSONObject json = new JSONObject();
        String level1 = "SELECT id,label_name,label_id,parent_id,uri,`level`,data_format FROM label_info where `level` =1 and `status`=3 and availably=1";
        String level2 = "SELECT id,label_name,label_id,parent_id,uri,`level`,data_format FROM label_info where `level` =2 and `status`=3 and availably=1";
        String level3 = "SELECT id,label_name,label_id,parent_id,uri,`level`,data_format FROM label_info where `level` =3 and `status`=3 and availably=1";
        List<Map<String, Object>> levelMap1 = jdbcTemplate.queryForList(level1);
        List<Map<String, Object>> levelMap2 = jdbcTemplate.queryForList(level2);
        List<Map<String, Object>> levelMap3 = jdbcTemplate.queryForList(level3);
        // 所有一级
        List<Object> oneList = new ArrayList<Object>();
        // 所有二级
        List<Object> twoList = new ArrayList<Object>();
        Map<Object, Object> twoMap = new HashMap<Object, Object>();
        // 所有三级
        Map<Object, Object> threeMap = new HashMap<Object, Object>();
        List<Object> threeList = new ArrayList<Object>();
        // 循环生成三级目录集合
        String parent_id_f = "";
        for (int i = 0; i < levelMap3.size(); i++) {
            Map<String, Object> map3 = levelMap3.get(i);
            String parent_id = map3.get("parent_id").toString();
            if (i != 0) {
                if (parent_id.equals(parent_id_f)) {
                    // 添加list
                    threeList.add(map3);
                } else {
                    threeMap.put(parent_id_f, threeList);
                    threeList = new ArrayList<Object>();
                    threeList.add(map3);
                }
            } else {
                // 添加list
                threeList.add(map3);
            }
            // id变换
            parent_id_f = parent_id;
        }
        // 循环生成二级目录集合
        String parent_id_tow = "";
        for (int i = 0; i < levelMap2.size(); i++) {
            Map<String, Object> map2 = levelMap2.get(i);
            String parent_id = map2.get("parent_id").toString();
            String TheId = map2.get("id").toString();
            List<Object> TheThreeList = (List<Object>) threeMap.get(TheId);
            map2.put("data", TheThreeList);
            if (i != 0) {
                if (parent_id.equals(parent_id_tow)) {
                    // 添加list
                    twoList.add(map2);
                } else {
                    twoMap.put(parent_id_tow, twoList);
                    twoList = new ArrayList<Object>();
                    twoList.add(map2);
                }
            } else {
                // 添加list
                twoList.add(map2);
            }
            // id变换
            parent_id_tow = parent_id;
        }
        // 循环生成一级目录集合
        for (int i = 0; i < levelMap1.size(); i++) {
            Map<String, Object> map1 = levelMap1.get(i);
            String TheId = map1.get("id").toString();
            List<Object> TheTwoList = (List<Object>) twoMap.get(TheId);
            map1.put("data", TheTwoList);
            oneList.add(map1);
        }
        json.put("data", oneList);
        return json.toJSONString();
    }

    @Override
    public String listLabelsChildrenById(String id, String status) {
        JSONObject json = new JSONObject();
        String hql = "From LabelInfo t where availably =1 and type>1 and  1 = 1 ";
        Map<String, String> map = new HashMap<String, String>();
        map.put("parent.id", id);
        map.put("status", status);
        List<LabelInfo> labelList = (List<LabelInfo>) sourceDao
                .getHqlQuery(hql, map, new HashMap<String, String>(), "id").list();
        json.put("data", commonService.getLabelMapList(labelList));
        return json.toJSONString();
    }

    @Override
    public String listSourceLabelsByCondition(JSONObject json) {
        JSONObject jsonReturn = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        // 判断状态值
        StringBuffer sb = new StringBuffer();
        // 页码
        Integer pageNum = json.getInteger("pageNum");
        // 条数
        Integer pageSize = json.getInteger("pageSize");
        sb.append(" SELECT ");
        sb.append(" 	t.id,");
        sb.append(" 	t.label_name,");
        sb.append(" 	t.path,");
        sb.append(" 	t.label_id,");
      /*  sb.append(" 	price.price/100 price,");
        sb.append(" 	price_id ");*/
        sb.append(" 	t.price/100 price");
        sb.append(" FROM ");
        sb.append(" 	label_info t ");
        sb.append(" LEFT JOIN t_label_source_price price ON price.label_id = t.label_id ");
        // sb.append(" LEFT JOIN t_source_label_rel source ON source.label_id =
        // t.label_id ");
        sb.append(" WHERE t.`level` = 3 ");
        sb.append(" AND t.`status` = 3 ");
        sb.append(" AND t.availably = 1 ");
        String queryState = json.getString("queryState");
        if ("0".equals(queryState)) {
            // 默认查询
            // String level1 = "SELECT id,label_name,label_id,parent_id FROM
            // label_info where `level` =1 and `status`=3 and availably=1";
            // String level2 = "SELECT id,label_name,label_id,parent_id FROM
            // label_info where `level` =2 and `status`=3 and availably=1";
            // String source = "SELECT source_name,source_id FROM `t_source`
            // WHERE `STATUS`=1";
            // List<Map<String, Object>> listLevel1 =
            // jdbcTemplate.queryForList(level1);
            // List<Map<String, Object>> listLevel2 =
            // jdbcTemplate.queryForList(level2);
            // List<Map<String, Object>> listSource =
            // jdbcTemplate.queryForList(source);
            // map.put("level1", listLevel1);
            // map.put("level2", listLevel2);
            // map.put("source", listSource);
        } else {
            // 数据源Id
            Integer sourceId = json.getInteger("sourceId");
            // 一级标签分类Id
            Integer topCategory = json.getInteger("topCategory");
            // 二级标签分类Id
            Integer secondCategory = json.getInteger("secondCategory");
            // 标签名称
            String labelName = json.getString("labelName");
            // 标签Id
            String labelId = json.getString("labelId");
            // 创建时间（开始）
            String createTimeStart = json.getString("createTimeStart");
            // 创建时间（结束）
            String createTimeEnd = json.getString("createTimeEnd");
           /* if (null != sourceId && !"".equals(sourceId)) {
                sb.append(" AND price.source_id=" + sourceId);
            }*/
            /*String uri = "";
            if (null != topCategory && !"".equals(topCategory)) {
                uri = "/" + topCategory;
            }
            if (null != secondCategory && !"".equals(secondCategory)) {
                uri = uri + "/" + secondCategory;
            }
            if (!"".equals(uri)) {
                sb.append(" AND t.uri= '" + uri + "/'");
            }*/
            if (null != secondCategory && !"".equals(secondCategory)) {
                sb.append(" AND t.parent_id=" + secondCategory);
            }
            if (null != labelName && !"".equals(labelName)) {
                sb.append(" AND t.label_name like '%" + labelName + "%'");
            }
            if (null != labelId && !"".equals(labelId)) {
                sb.append(" AND t.label_id=" + labelId);
            }
            if (null != createTimeStart && !"".equals(createTimeStart) && null != createTimeEnd
                    && !"".equals(createTimeEnd)) {
                sb.append(" AND t.create_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
            } else {
                if (null != createTimeStart && !"".equals(createTimeStart)) {
                    sb.append(" AND t.create_time > '" + createTimeStart + "'");
                }
                if (null != createTimeEnd && !"".equals(createTimeEnd)) {
                    sb.append(" AND t.create_time < '" + createTimeEnd + "'");
                }
            }
        }
        sb.append(" ORDER BY t.create_time");
        sb.append(" LIMIT ?,? ");
        List<Map<String, Object>> listLevel3 = jdbcTemplate.queryForList(sb.toString(),
                new Object[]{pageNum, pageSize});
        List<Map<String, Object>> listLevel3Size = jdbcTemplate.queryForList(sb.toString(), new Object[]{0, 100000});
        map.put("level3", listLevel3);
        map.put("total", listLevel3Size.size());
        jsonReturn.put("data", map);
        return jsonReturn.toJSONString();
    }

    @Override
    public String updateLabelSourcePrice(Double price, Integer labelId, String operator) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        // 查询旧的价格
        String sqlOld = "SELECT price FROM `label_info` where id=? ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlOld, new Object[]{labelId});
        String priceOld = list.get(0).get("price").toString();
        // 更新价格
        String sql = "update label_info set price =? where  id=?";
        int code = jdbcTemplate.update(sql, new Object[]{price * 100, labelId});
        // 记录log
       /* String insertSql = "INSERT into t_label_source_price_modify_log set  label_id=?,old_price=?,new_price=?,create_time=NOW(),modify_time=NOW(),operator=?";
        int codeLog = jdbcTemplate.update(insertSql, new Object[]{labelId, priceOld, price * 100, operator});
        if (code == 1 && codeLog == 1) {
            map.put("message", "成功");
            map.put("code", 1);
        } else {
            map.put("message", "失败");
            map.put("code", 0);
        }*/
        if (code == 1) {
            map.put("message", "成功");
            map.put("code", 1);
        } else {
            map.put("message", "失败");
            map.put("code", 0);
        }
        json.put("data", map);
        return json.toJSONString();
    }

    @Override
    public String listLabelSourcePriceLog(Integer priceId, Integer labelId, Integer pageNum, Integer pageSize) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT price_log_id,operator,create_time,new_price/100 new_price from t_label_source_price_modify_log where label_id =? ORDER BY create_time DESC ");
        List<Map<String, Object>> listSize = jdbcTemplate.queryForList(sql.toString(), new Object[]{labelId});

        sql.append(" LIMIT ?,? ");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), new Object[]{labelId, pageNum, pageSize});
        map.put("list", list);
        map.put("total", listSize.size());
        json.put("data", map);
        return json.toJSONString();
    }

    @Override
    public String updateLabelSourcePriceBatch(String state, Double price, String[] idList, String operator) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        Map<Object, Object> oldPriceMap = new HashMap<Object, Object>();
        StringBuffer strSqlList = new StringBuffer();
        if (state.equals("ALL")) {
            //批量更新所有价格
            //旧价格
            String sqlOld = "SELECT price_id,price FROM `t_label_source_price` ";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlOld);
            // 获取id
            for (int i = 0; i < list.size(); i++) {
                oldPriceMap.put(list.get(i).get("price_id"), list.get(i).get("price"));
            }
            for (Object price_id : oldPriceMap.keySet()) {
                // map.keySet()返回的是所有key的值
                String priceOld = oldPriceMap.get(price_id).toString();
                // 价格更新
                String updateSql = "update t_label_source_price set price =" + price * 100 + " where  price_id = " + price_id + " ";
                // 记录log
                String insertSql = "INSERT into t_label_source_price_modify_log set  price_id=" + price_id + ",old_price="
                        + priceOld + ",new_price=" + price * 100 + ",create_time=NOW(),modify_time=NOW(),operator="
                        + operator + " ";
                //记录sql
                strSqlList.append(updateSql + "#");
                strSqlList.append(insertSql + "#");
            }
        } else {
            // 更新价格<少量>
            String[] arrayList = idList[0].split(",");
            for (int y = 0; y < arrayList.length; y++) {
                // 查询价格
                String thePriceId = arrayList[y];
                String sqlOld = "SELECT price_id,price FROM `t_label_source_price` where price_id =? ";
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlOld, new Object[]{thePriceId});
                Object thePrice = list.get(0).get("price");
                // 价格更新
                String updateSql = "update t_label_source_price set price =" + price * 100 + " where  price_id = "
                        + thePriceId + " ";
                // 记录log
                String insertSql = "INSERT into t_label_source_price_modify_log set  price_id=" + thePriceId + ",old_price="
                        + thePrice + ",new_price=" + price * 100 + ",create_time=NOW(),modify_time=NOW(),operator="
                        + operator + " ";
                strSqlList.append(updateSql + "#");
                strSqlList.append(insertSql + "#");
            }
        }
        // 批量更新插入
        String[] sqlList = strSqlList.toString().split("#");
        int code[] = jdbcTemplate.batchUpdate(sqlList);
        //spf
        if (code[0] == 1 && code[1] == 1) {
            map.put("message", "成功");
            map.put("code", 1);
        } else {
            map.put("message", "失败");
            map.put("code", 0);
        }
        json.put("data", map);
        return json.toJSONString();
    }

    @Override
    public String listLabelList(String state, String id) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        // 根据state查询
        // state 0 默认
        if (state.equals("0")) {
            String sourceSql = "SELECT source_id,source_name from t_source";
            List<Map<String, Object>> listSource = jdbcTemplate.queryForList(sourceSql);
            /*
             * String labelOne =
             * "SELECT id,label_name,label_id,parent_id  FROM label_info where `level` =1 and `status`=3 and availably=1"
             * ; List<Map<String, Object>> listOne =
             * jdbcTemplate.queryForList(labelOne); String labelTwo =
             * "SELECT id,label_name,label_id,parent_id  FROM label_info where `level` =2 and `status`=3 and availably=1"
             * ; List<Map<String, Object>> listTwo =
             * jdbcTemplate.queryForList(labelTwo);
             */
            map.put("listSource", listSource);
            /// map.put("listOne", listOne);
            /// map.put("listTwo", listTwo);
        }
        // 一级
        if (state.equals("1")) {
            // 默认查询
            String level1 = "SELECT id,label_name,label_id,parent_id FROM label_info where `level` =1 and `status`=3 and availably=1";
            List<Map<String, Object>> listOne = jdbcTemplate.queryForList(level1);
            // String labelOne = "SELECT id,label_name,i.label_id,parent_id FROM
            // label_info i LEFT JOIN t_source_label_rel rel on
            // i.label_id=rel.label_id WHERE `level` = 1 AND i.status = 3 AND
            // availably = 1 AND rel.source_id=? ";
            // List<Map<String, Object>> listOne =
            // jdbcTemplate.queryForList(labelOne, new Object[] { id });
            map.put("listOne", listOne);
        }
        // 二级
        if (state.equals("2")) {
            String labelTwo = "SELECT id,label_name,label_id,parent_id  FROM label_info where `level` =2 and `status`=3 and availably=1 AND parent_id = ? ";
            List<Map<String, Object>> listTwo = jdbcTemplate.queryForList(labelTwo, new Object[]{id});
            map.put("listTwo", listTwo);
        }
        json.put("data", map);
        return json.toJSONString();
    }
}
