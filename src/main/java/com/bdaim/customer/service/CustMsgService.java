package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CustMsgService {

    public static final Logger logger = LoggerFactory.getLogger(CustMsgService.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 生成客户消息。
     * cust_user_id=客户账号ID，不空时只为这一个账号生成消息
     * cust_id=客户ID，当cust_user_id空时有效，为客户下所有账号生成消息
     * msg_type=消息类型，自定义
     * content=消息内容
     * level=消息级别，>=4级时会发送微信通知
     */
    public void createMsg(String cust_id, String cust_user_id, String msg_type, String content, int level) throws Exception {
        if (level < 0)
            level = 0;
        else if (level > 5)
            level = 5;

        Object to_cust_id = "";
        List<Object> to_cust_user_ids = new ArrayList();

        if (content == null || "".equals(content.trim()))
            throw new Exception("创建消息异常:空内容");
        if ((cust_id == null || "".equals(cust_id)) && (cust_user_id == null || "".equals(cust_user_id)))
            throw new Exception("创建消息异常:无消息接收人");

        try {
            if (cust_user_id != null && !"".equals(cust_user_id)) {
                List<Map<String, Object>> cts = jdbcTemplate.queryForList("select cust_id from t_customer_user where id=?", cust_user_id);
                if (cts.size() <= 0)
                    throw new Exception("创建消息异常:错误的消息接收人");
                to_cust_id = cts.get(0).get("cust_id");
                to_cust_user_ids.add(cust_user_id);
            } else {
                List<Map<String, Object>> cus = jdbcTemplate.queryForList("select id from t_customer_user where cust_id=?", cust_id);
                if (cus.size() <= 0)
                    throw new Exception("创建消息异常:错误的消息接收客户");
                for (Map cu : cus)
                    to_cust_user_ids.add(cu.get("id"));
                to_cust_id = cust_id;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception("解析消息接收人异常");
        }

        String sql = "insert into h_customer_msg(cust_id, cust_user_id, msg_type, content, create_time, status, level) value(?,?,?,?, now(), 0, ?)";
        try {
            List params = new ArrayList();

            for (Object to_cust_user_id : to_cust_user_ids) {
                Object[] p1 = new Object[]{to_cust_id, to_cust_user_id, msg_type, content, level};
                params.add(p1);
            }

            jdbcTemplate.batchUpdate(sql, params);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception("创建消息异常:" + cust_id + "." + cust_user_id);
        }
    }


    /*
     * 查询消息
     */
    public Page query(String cust_id, String user_id, JSONObject params) throws Exception {
        Page p = new Page();

        List sqlParams = new ArrayList();

        sqlParams.clear();
        StringBuffer sqlstr = new StringBuffer("select id, msg_type, content, create_time, status, level from h_customer_msg where cust_user_id=?  ");

        sqlParams.add(user_id);

        if (params.containsKey("status")) {
            sqlstr.append(" and status=?");
            sqlParams.add(params.get("status"));
        }
        if (params.containsKey("level")) {
            sqlstr.append(" and level=?");
            sqlParams.add(params.get("level"));
        }
        if (params.containsKey("msg_type")) {
            sqlstr.append(" and msg_type=?");
            sqlParams.add(params.get("msg_type"));
        }


        int pageNum = 1;
        int pageSize = 10;
        try {
            pageNum = params.getIntValue("pageNum");
        } catch (Exception e) {
        }
        try {
            pageSize = params.getIntValue("pageSize");
        } catch (Exception e) {
        }
        if (pageNum <= 0)
            pageNum = 1;
        if (pageSize <= 0)
            pageSize = 10;
        if (pageSize > 1000)
            pageSize = 1000;

        try {
            List<Map<String, Object>> ds = jdbcTemplate.queryForList(sqlstr + " order by id desc limit " + (pageNum - 1) * pageSize + ", " + pageSize, sqlParams.toArray());

            p.setData(ds);
            p.setTotal(ds.size());
            p.setPerPageCount(pageSize);
            p.setStart((pageNum - 1) * pageSize + 1);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception("查询异常");
        }

        return p;
    }


    /*
     * 按ID获取记录
     */
    public JSONObject getInfo(String cust_id, String cust_user_id, Long id) throws Exception {
        JSONObject jo = null;

        String sql = "select id, msg_type, content, create_time, status, level from h_customer_msg where cust_user_id=? and id=? ";

        Map data = null;
        try {
            data = jdbcTemplate.queryForMap(sql, cust_user_id, id);
        } catch (EmptyResultDataAccessException e) {
            logger.warn("查询:{},失败", id);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception("获取消息异常:" + id);
        }
        if (data == null)
            return jo;
        try {
            jdbcTemplate.update("update h_customer_msg set status=1 where cust_user_id=? and id=?", cust_user_id, id);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        try {
            jo = JSONObject.parseObject(JSONObject.toJSONString(data));

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception("消息数据格式错误！");
        }

        return jo;
    }
}
