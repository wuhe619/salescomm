package com.bdaim.common.service.api;

import com.alibaba.fastjson.JSONObject;import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.LogUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.entity.CustomGroup;
import org.hibernate.HibernateException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.Timestamp;


/**
 * 尚德-京东创建客户群
 *
 * @author chengning@salescomm.net
 * @date 2019/2/14 10:49
 */
@Service("JdSdCustomGroupService")
@Transactional
public class JdSdCustomGroupServiceImpl {

    @Resource
    private CustomGroupDao customGroupDao;

    private final static String CUST_ID = "1903120348469162";

    //private final static String DB_NAME = "label_dev.";
    private final static String DB_NAME = " ";

    public String execute(HttpServletRequest request) {
        int status = 0;
        JSONObject json = new JSONObject();
        int id = 0;
        try {
            //必传用户群名称
            String name = request.getParameter("name");
            if (StringUtil.isEmpty(name)) {
                LogUtil.warn("客户" + CUST_ID + "创建客户群name参数为空,name:" + name);
                json.put("code", -2);
                json.put("message", "参数异常");
                return json.toJSONString();
            }
            //用户群描述
            String description = request.getParameter("description");
            if (StringUtil.isEmpty(description)) {
                description = "";
            }

            long time = System.currentTimeMillis();
            //插入订单表
            StringBuffer insertOrder = new StringBuffer();
            String orderId = String.valueOf(IDHelper.getTransactionId());
            insertOrder.append("INSERT INTO " + DB_NAME + " t_order (`order_id`, `cust_id`, `order_type`, `create_time`,  `remarks`, `amount`, `order_state`, `cost_price`) ");
            insertOrder.append(" VALUES ('" + orderId + "','" + CUST_ID + "','1','" + new Timestamp(time) + "','客户群创建','0','2','0')");
            int b = customGroupDao.executeUpdateSQL(insertOrder.toString(), new Object[]{});
            LogUtil.info("存入order表状态:" + b);
            if (b == 0) {
                json.put("code", -1);
                json.put("message", "创建用户群失败");
                return json.toJSONString();
            }

            CustomGroup cg = new CustomGroup();
            cg.setName(name);
            cg.setDesc(description);
            cg.setOrderId(orderId);
            cg.setStatus(1);
            cg.setCustId(CUST_ID);
            cg.setCreateTime(new Timestamp(System.currentTimeMillis()));
            LogUtil.info("插入customer_group表的数据:" + cg);
            id = (int) customGroupDao.saveReturnPk(cg);
            LogUtil.info("插入customer_group返回主键id是:" + id);
            if (id > 0) {
                //创建t_customer_group_list+id表
                // 0.建分表(客户群ID为后缀)
                StringBuffer sb = new StringBuffer();
                sb.append(" create table IF NOT EXISTS " + DB_NAME + " t_customer_group_list_");
                sb.append(id);
                sb.append(" like " + DB_NAME + " t_customer_group_list");
                try {
                    customGroupDao.executeUpdateSQL(sb.toString());
                    status = 1;
                } catch (HibernateException e) {
                    LogUtil.error("创建用户群表失败,", e);
                    status = 0;
                }
                LogUtil.info("创建用户群表状态:" + status + ",创建用户群表sql" + sb);
            }

        } catch (Exception e) {
            LogUtil.error("创建用户群异常:", e);
            json.put("code", -1);
            json.put("message", "创建用户群失败");
        }
        if (status > 0) {
            json.put("code", 0);
            json.put("message", "创建用户群成功");
            //返回客户群id
            json.put("id", id);
        } else {
            json.put("code", -1);
            json.put("message", "创建用户群失败");
        }

        return json.toJSONString();
    }
}
