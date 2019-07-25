package com.bdaim.slxf.service.impl;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.entity.BatchDetailDTO;
import com.bdaim.batch.service.AllocationService;
import com.mysql.jdbc.PreparedStatement;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2018/9/7
 * @description
 */
@Service
@Transactional
public class AllocationImp implements AllocationService {
    private static Log log = LogFactory.getLog(AllocationImp.class);
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private BatchDetailDao batchDetailDao;

    /**
     * @description 获取所有员工姓名
     * @author:duanliying
     * @method
     * @date: 2018/9/7 14:29
     */
    @Override
    public String getStaffName(String custId) {
        Map<String, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        StringBuilder sql = new StringBuilder("SELECT CAST(t.id AS CHAR) id,t.cust_id,t.realname from  t_customer_user t  WHERE  t.cust_id = ? AND STATUS = 0 AND user_type = 2");
        List list = jdbcTemplate.queryForList(sql.toString(), custId);
        map.put("staff", list);
        json.put("staffJson", map);
        return json.toJSONString();
    }

    /**
     * @description 为批次下单个客户进行分配负责人
     * @author:duanliying
     * @method
     * @date: 2018/9/7 15:51
     */
    @Override
    public String updateAssignedOne(String id, Long userId, String batchId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();

        try {
            sb.append("update nl_batch_detail set");
            sb.append(" allocation =1 ,");
            sb.append(" user_id =? ");
            sb.append(" where id = ?");
            sb.append(" AND batch_id = ?");
            int code = jdbcTemplate.update(sb.toString(), new Object[]{userId, id, batchId});

            log.info("分配负责人，sql：" + sb.toString());
            map.put("code", code);
            map.put("message", "分配负责人成功");
            json.put("data", map);

        } catch (Exception e) {
            map.put("code", 000);
            map.put("message", "分配负责人失败");
            json.put("data", map);
        }

        return json.toJSONString();
    }

    /**
     * @description 为批次下已选客户进行分配负责人
     * @author:duanliying
     * @method
     * @date: 2018/9/7 16:02
     */
    @Override
    public String updateAssignedMany(List<BatchDetailDTO> list) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        String sql = "update nl_batch_detail"
                + "  SET allocation = 1,  user_id = ?  WHERE id = ?"
                + "  AND batch_id = ?";
        try {

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                public int getBatchSize() {
                    return list.size();
                }

                public void setValues(PreparedStatement ps, int i) throws SQLException {
                }

                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws SQLException {
                    BatchDetailDTO batchDetailDTO = (BatchDetailDTO) list.get(i);
                    ps.setLong(1, batchDetailDTO.getUserId());
                    ps.setString(2, batchDetailDTO.getId());
                    ps.setString(3, batchDetailDTO.getBatchid());
                }
            });
            map.put("code", 1);
            map.put("message", "分配负责人成功");
            json.put("data", map);
        } catch (Exception e) {
            map.put("code", 0);
            map.put("message", "分配负责人失败");
            json.put("data", map);
        }
        return json.toJSONString();
    }

    /**
     * @description 根据批次id获取所有未分配的客户信息(allocation = 0)
     * @author:duanliying
     * @method
     * @date: 2018/9/7 11:23
     */
    @Override
    public List<Map<String, Object>> queryNoAssigned(String batchId) {
        Map<String, Object> map = new HashMap<>();

        StringBuilder sql = new StringBuilder("SELECT id FROM nl_batch_detail"
                + " WHERE allocation = 0 and status = 1 and batch_id = " + batchId);
        List list = jdbcTemplate.queryForList(sql.toString());

        return list;
    }
}



