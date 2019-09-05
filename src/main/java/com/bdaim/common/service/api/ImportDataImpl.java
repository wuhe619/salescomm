package com.bdaim.common.service.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.redis.RedisUtil;
import com.bdaim.common.util.LogUtil;
import com.bdaim.common.util.MD5Util;
import com.bdaim.common.util.StringUtil;
import com.bdaim.resource.dao.MarketResourceDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

/**
 *  客户群数据导入
 *
 * @author chengning@salescomm.net
 * @date 2019/3/11 14:04
 */
@Service("ImportDataService")
@Transactional
public class ImportDataImpl {

    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private RedisUtil redisUtil;

    public String execute(HttpServletRequest request) {
        int count = 0;
        JSONObject json = new JSONObject();
        //客户群id
        String groupId = request.getParameter("id");
        String data = request.getParameter("data");
        JSONArray maps;
        try {
            maps = JSON.parseArray(data);
        } catch (Exception e) {
            LogUtil.error("数据转换异常:" + e);
            json.put("code", -3);
            json.put("message", "传递参数格式不正确");
            return json.toJSONString();
        }
        if (maps == null) {
            LogUtil.error("数据异常:" + JSON.toJSONString(maps));
            json.put("code", -4);
            json.put("message", "参数异常");
            return json.toJSONString();
        }
        if (maps.size() > 10000) {
            json.put("code", -2);
            json.put("message", "单次最大上传条数不可以超过10000");
            return json.toJSONString();
        }
        String phone, md5Phone;
        int status = 0;
        String flag = "0";
        int updateQuantity;
        //String insertSql = "INSERT INTO u (id,phone) VALUES(?,?)";
        StringBuffer sb;
        if (maps.size() > 0) {
            List list;
            for (int i = 0; i < maps.size(); i++) {
                phone = String.valueOf(maps.getJSONObject(i).get("phone"));
                //手机号码通过c+手机号  进行MD5加密 作为id  同时存入u表
                md5Phone = MD5Util.encode32Bit("c" + phone);
                //先判断手机号码是否存在 存在不插入
                //list = marketResourceDao.sqlQuery("SELECT id FROM t_customer_group_list_" + groupId + " WHERE id ='" + md5Phone + "'");
                String _phone = redisUtil.get(md5Phone);
                if (StringUtil.isNotEmpty(_phone)) {
                    //说明已经存在暂时不能添加
                    LogUtil.warn(phone + "此号码已经存在未添加成功");
                } else {
                    //将数据存储进数据库
                    sb = new StringBuffer();
                    sb.append(" INSERT INTO t_customer_group_list_" + groupId + " (id)");
                    sb.append(" VALUES(?)");
                    LogUtil.info("插入用户群表的sql:" + sb.toString());
                    status = marketResourceDao.executeUpdateSQL(sb.toString(), new Object[]{md5Phone});
                    LogUtil.info("插入用户群表的状态:" + status);
                    //返回主键id更新u表
                    if (status > 0) {
                        count++;
                        flag = redisUtil.set(md5Phone,phone);
//                        flag = marketResourceDao.executeUpdateSQL(insertSql, new Object[]{md5Phone, phone});
                        LogUtil.info("插入u表的状态:" + flag);
                    }
                }
            }
            if (count > 0) {
                // 更新客户群客户数量
                updateQuantity = marketResourceDao.executeUpdateSQL("UPDATE customer_group SET user_count = user_count + ? WHERE id = ?", new Object[]{count, groupId});
                LogUtil.info("更新客户群客户数量状态:" + updateQuantity + ",数量:" + count);
            }
        }
        if (count > 0) {
            json.put("code", 0);
            json.put("count", count);
            json.put("message", "数据保存成功");
        } else {
            json.put("code", -1);
            json.put("message", "数据保存失败");
        }
        return json.toJSONString();
    }
}
