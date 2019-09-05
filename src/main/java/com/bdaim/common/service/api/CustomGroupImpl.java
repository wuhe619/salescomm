package com.bdaim.common.service.api;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.LogUtil;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.entity.CustomGroup;
import org.hibernate.HibernateException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.Timestamp;

/**
 *  创建客户群
 *
 * @author chengning@salescomm.net
 * @date 2019/3/11 14:04
 */
@Service("CustomGroupService")
@Transactional
public class CustomGroupImpl {

    @Resource
    private CustomGroupDao customGroupDao;

    public String execute(HttpServletRequest request) {
        boolean flag = false;
        int status = 0;
        JSONObject json = new JSONObject();
        int id = 0;
        try {
            //必传用户群名称
            String name = request.getParameter("name");
            //企业id
            String custId = CallRecordImpl.ZK_USER_ID;
            //用户群描述
            String description = request.getParameter("description");
            //插入订单表
            StringBuffer insertOrder = new StringBuffer();
            String orderId = String.valueOf(IDHelper.getTransactionId());
            insertOrder.append("INSERT INTO t_order (`order_id`, `cust_id`, `order_type`, `create_time`,  `remarks`, `amount`, order_state, `cost_price`) ");
            insertOrder.append(" VALUES ('" + orderId + "','" + custId + "','1','" + new Timestamp(System.currentTimeMillis()) + "','客户群创建','0','2','0')");
            int b = customGroupDao.executeUpdateSQL(insertOrder.toString());
            LogUtil.info("存入order表状态:" + b);
            CustomGroup cg = new CustomGroup();
            cg.setName(name);
            cg.setDesc(description);
            cg.setOrderId(orderId);
            cg.setStatus(1);
            cg.setCustId(custId);
            cg.setCreateTime(new Timestamp(System.currentTimeMillis()));
            LogUtil.info("插入customer_group表的数据:" + cg);
            id = (int) customGroupDao.saveReturnPk(cg);
            LogUtil.info("返回主键id是:" + id);
            if (id > 0) {
                //创建t_customer_group_list+id表
                // 0.建分表(客户群ID为后缀)
                StringBuffer sb = new StringBuffer();
                sb.append(" create table IF NOT EXISTS t_customer_group_list_");
                sb.append(id);
                sb.append(" like t_customer_group_list");
                try {
                    customGroupDao.executeUpdateSQL(sb.toString(), new Object[]{});
                    status = 1;
                } catch (HibernateException e) {
                    LogUtil.error("创建用户群表失败,", e);
                    status = 0;
                }
                LogUtil.info("创建用户群表状态:" + status + "创建用户群表sql" + sb);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.info("创建用户群表异常:" + e);
        }
        if (status > 0) {
            json.put("code", 0);
            json.put("message", "创建用户群表成功");
            //返回客户群id
            json.put("id", id);
        } else {
            json.put("code", -1);
            json.put("message", "创建用户群表失败");
        }

        return json.toJSONString();
    }
}
