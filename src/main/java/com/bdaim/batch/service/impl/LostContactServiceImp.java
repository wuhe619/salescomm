package com.bdaim.batch.service.impl;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.dao.LostContactDao;
import com.bdaim.batch.service.LostContactService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.service.CustomerPropertyService;
import com.bdaim.slxf.dto.TouchInfoDTO;
import com.bdaim.slxf.entity.DetailQueryParam;

import org.hibernate.transform.Transformers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2018/9/10
 * @description
 */
@Service
@Transactional
public class LostContactServiceImp implements LostContactService {

    private static Log log = LogFactory.getLog(LostContactServiceImp.class);

    private final static String VOIVE_LOG_SEND_REMARK_SPLIT = "{}";

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private CustomerPropertyService customerPropertyService;
    @Resource
    private LostContactDao lostContactDao;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerDao customerDao;

    /**
     * @description 获取失联人员信息根据唯一标识+批次id
     * @author:duanliying
     * @method
     * @date: 2018/9/10 9:59
     */
    @Override
    public String getMessageById(String batchId, String id, String type, Integer pageNum, Integer pageSize, String custId, String userId) {

        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<>();
        StringBuffer selectSql = new StringBuffer();
        //获取客户详细信息（super_id+batch_id）
        selectSql.append(" SELECT t1.super_name,t1.super_age,t1.super_sex,t1.super_phone,t1.super_telphone,t1.super_address_province_city,t1.super_address_street,t1.voice_info_id,t2.batch_id,t2.id,t2.label_one,t2.label_two,t2.label_three,t2.enterprise_id\n");
        selectSql.append(" FROM nl_batch_detail t2");
        selectSql.append(" LEFT JOIN t_touch_voice_info t1 ON t2.id = t1.super_id");
        selectSql.append(" AND t2.batch_id = t1.batch_id WHERE");
        selectSql.append(" t2.id =? AND t2.batch_id = ?");
        try {
            List touchInfoList = jdbcTemplate.queryForList(selectSql.toString(), new Object[]{id, batchId});
            //查询所有的自建属性标签
            List<Map<String, Object>> customlabelAll = getCustomlabel(null, null, custId);
            //查询已选自建属性标签
            List selLabel = getSelLabel(id, batchId);
            //获取最后一次通话备注信息
            //type用于区分是1打电话查询用户信息 还是2个人资料
            String remarkSql = null;
            //打电话个人信息查询最后倒数第二次通话备注
            if (type.equals("1")) {
                remarkSql = "select remark from t_touch_voice_log where superid=? AND batch_id=?  ORDER BY create_time DESC  LIMIT 1,1";
            }
            //个人信息查询最后一次通话备注
            if (type.equals("2")) {
                remarkSql = "select remark from t_touch_voice_log where superid=? AND batch_id=?   ORDER BY create_time DESC  LIMIT 0,1";
            }
            List<Map<String, Object>> remarkList = lostContactDao.sqlQuery(remarkSql, id, batchId);
            //对备注进行处理截取第一个{}
            String remark = "";
            if (remarkList.size() > 0) {
                remark = String.valueOf(remarkList.get(0).get("remark"));
                //备注{}企业{}批次   对备注信息进行截取
                if (remark.contains("{}")) {
                    //截取字符串
                    List<String> remarks = Arrays.asList(remark.split("\\{}"));
                    remark = remarks.get(0);
                } else {
                    remark = String.valueOf(remarkList.get(0).get("remark"));
                }
            }
            map.put("code", 1);
            map.put("message", "查询信息成功");
            map.put("lostContactMessage", touchInfoList.size() > 0 ? touchInfoList.get(0) : "");
            map.put("customlabelAll", customlabelAll);
            map.put("selLabel", selLabel);
            map.put("remark", remark);
            json.put("data", map);
        } catch (Exception e) {
            log.error("查询用户资料错误", e);
            map.put("code", 0);
            map.put("message", "查询信息失败");
            json.put("data", map);
        }
        return json.toJSONString();
    }

    /**
     * @description 查询用户已选择属性信息
     * @author:duanliying
     * @method
     * @date: 2018/9/14 10:33
     */
    public List getSelLabel(String id, String batchId) {
        Map<String, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        StringBuffer sb = new StringBuffer();
        sb.append("  SELECT  CAST(t1.label_id AS CHAR) label_id, type FROM t_super_label t1")
                .append("  LEFT JOIN t_customer_label t2")
                .append("  ON t1.label_id = t2.label_id AND t2.status =1")
                .append("  WHERE 1=1 ")
                .append("  and  t1.super_id  = ? ")
                .append("  AND  t1.batch_id = ?");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString(), id, batchId);
        // 处理自定义属性和标签的对应关系
        String superLabelSql = "SELECT option_value FROM t_super_label WHERE label_id = ? AND super_id = ? AND batch_id = ? ";
        List<Map<String, Object>> superLabelOptionList;
        Map<String, Object> labelMap;
        for (Map<String, Object> superLabel : list) {
            if (superLabel.get("label_id") != null) {
                labelMap = new HashMap<>();
                superLabelOptionList = jdbcTemplate.queryForList(superLabelSql, new Object[]{superLabel.get("label_id"), id, batchId});
                if (superLabelOptionList.size() > 0 && superLabelOptionList.get(0).get("option_value") != null) {
                    //文本框不拆分为数组
                    if ("1".equals(String.valueOf(superLabel.get("type")))) {
                        superLabel.put("optionValue", superLabelOptionList.get(0).get("option_value"));
                    } else {
                        superLabel.put("optionValue", String.valueOf(superLabelOptionList.get(0).get("option_value")).split(","));
                    }
                }
            }
        }
        return list;
    }

    /**
     * @description 查询所有自建属性信息
     * @author:duanliying
     * @method
     * @date: 2018/9/14 11:09
     */
    @Override
    public List<Map<String, Object>> getCustomlabel(Integer pageNum, Integer pageSize, String custId) {
        List<Map<String, Object>> staticList = null;
        StringBuffer sql = new StringBuffer();

        sql.append("  SELECT t1.id,t1.cust_id,t1.user_id,t1.label_id,t1.status,t1.label_name,")
                .append("  t1.create_time,t1.label_desc,t1.type, t1.`option`")
                .append("  FROM  t_customer_label t1");
        sql.append("  WHERE t1.cust_id ='" + custId + "'");
        sql.append("  AND  t1.status =1 ");
        if (pageNum == null || "".equals(pageNum) || pageSize == null || "".equals(pageSize)) {
            sql.append("  GROUP BY t1.id");
            sql.append("  ORDER BY t1.create_time DESC");
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString());
            staticList = staticCustomerLabels(list, true);
        } else {
            sql.append("  GROUP BY t1.id");
            sql.append("  ORDER BY t1.create_time DESC");
            staticList = staticCustomerLabels(lostContactDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                    .setFirstResult(pageNum).setMaxResults(pageSize).list(), true);
        }
        return staticList;
    }

    /**
     * @description 构造客户资料列表数据, 处理option
     * @author:duanliying
     * @method
     * @date: 2018/9/14 11:24
     */
    private List<Map<String, Object>> staticCustomerLabels(List<Map<String, Object>> list, boolean staticStatus) {
        for (Map<String, Object> map : list) {
            // 单选或者多选的选项处理为数组
            if (map.get("type") != null && ("2".equals(String.valueOf(map.get("type")))
                    || "3".equals(String.valueOf(map.get("type"))))) {
                map.put("option", String.valueOf(map.get("option")).split(","));
            }
        }
        return list;
    }


    /**
     * @description 修改坐席打完电话后修改电话备注信息
     * @author:duanliying
     * @method
     * @date: 2018/9/13 18:26
     */
    @Override
    public String updateVoiceLog(String touchId, String remark, String userId, String custId) {
        // 拼装备注字段: 备注{}操作人姓名{}企业名称
        String userName = customerUserDao.getName(userId);
        String custName = customerDao.getEnterpriseName(custId);
        remark += VOIVE_LOG_SEND_REMARK_SPLIT + userName + VOIVE_LOG_SEND_REMARK_SPLIT + custName;

        log.info("通话备注touchId:" + touchId + ",remark:" + remark);

        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE  t_touch_voice_log SET ");
        sb.append(" remark=?");
        sb.append(" where touch_id = ?");
        jdbcTemplate.update(sb.toString(), new Object[]{remark, touchId});
        return null;
    }

    /**
     * @description 删除用户原来保存的自建属性信息
     * @author:duanliying
     * @method
     * @date: 2018/9/13 18:24
     */
    @Override
    public String deleteSuperLable(String cardId, String batchId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();

        try {

            sb.append(" DELETE from t_super_label ");

            sb.append(" where  super_id = ?");
            sb.append(" and batch_id = ?");

            int code = jdbcTemplate.update(sb.toString(), new Object[]{cardId, batchId});

            log.info("删除客户自建标签对应联系客户，sql：" + sb.toString());
            map.put("code", code);
            map.put("message", "成功");
            json.put("data", map);

        } catch (Exception e) {
            map.put("code", 000);
            map.put("message", "失败");
            json.put("data", map);
        }

        return json.toJSONString();
    }

    /**
     * @description 插入客户购买的用户标签表
     * @author:duanliying
     * @method
     * @date: 2018/9/13 18:27
     */
    @Override
    public boolean insertSuperLable(String id, String cardId, String labelId, String batchId, String optionValue) {
        try {
            String sql = "  INSERT INTO t_super_label(id,super_id,batch_id,label_id,create_time,option_value)VALUES(?,?,?,?,NOW(),?)";
            jdbcTemplate.update(sql, new Object[]{id, cardId, batchId, labelId, optionValue});
            log.info("插入客户购买资源用户标签表sql:" + sql);
        } catch (Exception e) {
            log.error("插入客户购买资源用户标签表出错", e);
        }
        return true;
    }

    /**
     * @description 新增打电话获取的用户信息
     * @author:duanliying
     * @method
     * @date: 2018/9/13 18:28
     */
    @Override
    public boolean updateTouchInfo(TouchInfoDTO dto) {

        StringBuffer sql = new StringBuffer();
        boolean judge = false;
        try {
            sql.append("SELECT voice_info_id FROM t_touch_voice_info WHERE voice_info_id = ?");
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), new Object[]{dto.getVoice_info_id()});
            if (list != null && list.size() > 0) {
                sql.setLength(0);
                sql.append(
                        "UPDATE t_touch_voice_info SET ");
                sql.append(" voice_info_id = ?, ");
                sql.append(" cust_id= ?, ");
                sql.append(" user_id= ?, ");
                sql.append(" cust_group_id= ?, ");
                sql.append(" super_id= ?, ");
                sql.append(" super_name= ?, ");
                sql.append(" super_age= ?, ");
                sql.append(" super_sex= ?, ");
                sql.append(" super_telphone= ?, ");
                sql.append(" super_phone= ?, ");
                sql.append(" super_address_province_city= ?, ");
                sql.append(" super_address_street= ? ,");
                sql.append(" batch_id= ? ");
                sql.append(" WHERE voice_info_id = ?");
                jdbcTemplate.update(sql.toString(), new Object[]{dto.getVoice_info_id(), dto.getCust_id(), dto.getUser_id(), dto.getBantch_id(),
                        dto.getSuper_id(), dto.getSuper_name(), dto.getSuper_age(), dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                        dto.getSuper_address_province_city(), dto.getSuper_address_street(), dto.getBantch_id(), dto.getVoice_info_id()});
            } else {
                //如果voice_info_id不存在直接添加数据信息
                insertTouchInfo(dto);
            }
            judge = true;
        } catch (Exception e) {
            log.error("更新t_touch_voice_info的信息失败", e);
        }
        return judge;
    }

    /**
     * @description 如果当前voice_info_id 不存在直接添加一条信息
     * @author:duanliying
     * @method
     * @date: 2018/9/13 18:38
     */
    @Override
    public boolean insertTouchInfo(TouchInfoDTO dto) {
        StringBuffer sql = new StringBuffer();

        boolean judge = false;
        try {
            sql.append(
                    "REPLACE  into t_touch_voice_info values ( ");
            sql.append("'" + dto.getVoice_info_id() + "',");
            sql.append("'" + dto.getCust_id() + "',");
            sql.append("'" + dto.getUser_id() + "',");
            sql.append("'" + dto.getCust_group_id() + "',");
            sql.append("'" + dto.getSuper_id() + "',");

            sql.append("now(),");

            sql.append("'" + dto.getSuper_name() + "',");
            sql.append("'" + dto.getSuper_age() + "',");
            sql.append("'" + dto.getSuper_sex() + "',");
            sql.append("'" + dto.getSuper_telphone() + "',");
            sql.append("'" + dto.getSuper_phone() + "',");
            sql.append("'" + dto.getSuper_address_province_city() + "',");
            sql.append("'" + dto.getSuper_address_street() + "',");
            sql.append("'" + dto.getBantch_id() + "')");

            lostContactDao.insertLog(sql.toString());
            judge = true;
        } catch (Exception e) {
            // TODO: handle exception
            log.info("更新super的信息失败");
        }

        return judge;

    }
}
